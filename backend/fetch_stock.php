<?php

header('Content-Type: application/json');

// --- Configuration ---
define('GROWAGARDEN_URL', 'https://growagarden.gg/stocks');
define('VULCANVALUES_URL', 'https://vulcanvalues.com/grow-a-garden/stock');
define('CACHE_FILE', __DIR__ . '/stock_cache.json');
define('CACHE_DURATION', 20); // 20 seconds
define('FETCH_TIMEOUT', 10); // 10 seconds for cURL timeout

// --- Cache Handling (Serve from fresh cache if available) ---
if (file_exists(CACHE_FILE) && (time() - filemtime(CACHE_FILE) < CACHE_DURATION)) {
    readfile(CACHE_FILE);
    exit;
}

// --- Helper Functions ---

/**
 * Fetches HTML content from a given URL.
 * @param string $url The URL to fetch.
 * @return string|false HTML content on success, false on failure.
 */
function fetchUrlContent($url) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_USERAGENT, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36');
    curl_setopt($ch, CURLOPT_TIMEOUT, FETCH_TIMEOUT);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true); // Follow redirects
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true); // Keep true for security, ensure cacert.pem is configured in php.ini if issues arise
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);

    $html = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if (curl_errno($ch) || $httpCode !== 200) {
        // Optionally log detailed error: curl_error($ch) or $httpCode
        curl_close($ch);
        return false;
    }

    curl_close($ch);
    return $html;
}

/**
 * Parses stock data from GrowAGarden.gg HTML content.
 */
function parseGrowAGardenStock($htmlContent, $sectionTitle) {
    $items = [];
    $sectionPattern = '/<h3[^>]*>\s*' . preg_quote($sectionTitle, '/') . '\s*in Grow a Garden\s*<\/h3>\s*<ul[^>]*>(.*?)<\/ul>/is';
    if (preg_match($sectionPattern, $htmlContent, $matches)) {
        $ulContent = $matches[1];
        $itemPattern = '/<li>\s*(.+?)\s*(?:<!--.*?-->\s*)*-\s*Available Stock:\s*(?:<!--.*?-->\s*)*(\d+)\s*<\/li>/is';
        if (preg_match_all($itemPattern, $ulContent, $itemMatches, PREG_SET_ORDER)) {
            foreach ($itemMatches as $match) {
                $itemName = trim(strip_tags($match[1]));
                $stock = (int)$match[2];
                $items[] = ['name' => $itemName, 'stock' => $stock];
            }
        }
    }
    return $items;
}

/**
 * Parses stock data from VulcanValues.com HTML content.
 */
function parseVulcanoStock($htmlContent, $sectionTitle) {
    $items = [];
    // Section title for VulcanValues "GEAR STOCK"
    $sectionPattern = '/<h2[^>]*>\s*' . preg_quote($sectionTitle, '/') . '\s*<\/h2>\s*<p[^>]*>.*?<\/p>\s*<ul[^>]*>(.*?)<\/ul>/is';
    if (preg_match($sectionPattern, $htmlContent, $matches)) {
        $ulContent = $matches[1];
        // Item pattern for VulcanValues: <span>Item Name <span class="text-gray-400">xSTOCK</span></span>
        $itemPattern = '/<li[^>]*>.*?<span>\s*([^<]+?)\s*<span[^>]*>\s*x(\d+)\s*<\/span>\s*<\/span>.*?<\/li>/is';
        if (preg_match_all($itemPattern, $ulContent, $itemMatches, PREG_SET_ORDER)) {
            foreach ($itemMatches as $match) {
                $itemName = trim(strip_tags($match[1]));
                $stock = (int)$match[2];
                $items[] = ['name' => $itemName, 'stock' => $stock];
            }
        }
    }
    return $items;
}

// --- Main Logic ---
$allStockData = null;

// Try primary source: Grow A Garden
$htmlGrow = fetchUrlContent(GROWAGARDEN_URL);
if ($htmlGrow !== false) {
    $seeds = parseGrowAGardenStock($htmlGrow, 'Current Seed Shop Stock');
    $gear = parseGrowAGardenStock($htmlGrow, 'Current Gear Shop Stock');
    $eggs = parseGrowAGardenStock($htmlGrow, 'Current Egg Shop Stock');

    if (!empty($seeds) || !empty($gear) || !empty($eggs)) {
        $allStockData = [
            'source' => 'growagarden.gg',
            'seeds' => $seeds,
            'gear' => $gear,
            'eggs' => $eggs
        ];
    }
}

// If primary source failed or yielded no useful data, try fallback: VulcanValues
if ($allStockData === null) {
    $htmlVulcano = fetchUrlContent(VULCANVALUES_URL);
    if ($htmlVulcano !== false) {
        $seeds = parseVulcanoStock($htmlVulcano, 'SEEDS STOCK');
        $gear = parseVulcanoStock($htmlVulcano, 'GEAR STOCK');
        $eggs = parseVulcanoStock($htmlVulcano, 'EGG STOCK');

        if (!empty($seeds) || !empty($gear) || !empty($eggs)) {
            $allStockData = [
                'source' => 'vulcanvalues.com',
                'seeds' => $seeds,
                'gear' => $gear,
                'eggs' => $eggs
            ];
        }
    }
}

// Output results or error
if ($allStockData !== null) {
    $jsonOutput = json_encode($allStockData, JSON_PRETTY_PRINT);
    file_put_contents(CACHE_FILE, $jsonOutput); // Cache the successful result
    echo $jsonOutput;
} else {
    http_response_code(503); // Service Unavailable
    // Attempt to serve stale cache if all sources fail and cache exists
    if (file_exists(CACHE_FILE)) {
        $staleData = file_get_contents(CACHE_FILE);
        $decodedStaleData = json_decode($staleData, true);
        if ($decodedStaleData !== null) {
            $decodedStaleData['error_message'] = 'All live data sources are currently unavailable. Serving stale data from cache.';
            $decodedStaleData['source_original'] = isset($decodedStaleData['source']) ? $decodedStaleData['source'] : 'unknown';
            $decodedStaleData['source'] = 'cache (stale)';
            echo json_encode($decodedStaleData, JSON_PRETTY_PRINT);
            exit;
        }
    }
    // If no stale cache, output primary error
    echo json_encode(['error_message' => 'All data sources are currently unavailable and no cache exists.', 'source' => 'none']);
}

exit;

?>
