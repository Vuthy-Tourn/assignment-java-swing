package com.pos.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seeds 100 sample products with generated PNG images.
 *
 * Images are written to src/main/resources/images/products/ and committed to git,
 * so every teammate who pulls the branch automatically gets the same images.
 * The DB stores a relative path (images/products/filename.png) that
 * ImageRenderer.loadIcon() resolves via classpath or the file system.
 *
 * OOP: single-responsibility (generate images / ensure categories / insert products
 * are separate private methods), encapsulation (all helpers are private).
 */
public class DataSeeder {

    // Relative path used inside the DB — matches classpath location after mvn compile
    private static final String DB_PATH_PREFIX = "images/products/";

    // Absolute path where images are written (src/main/resources is on the git tree)
    private static final String RESOURCES_DIR =
            System.getProperty("user.dir")
            + File.separator + "src"
            + File.separator + "main"
            + File.separator + "resources"
            + File.separator + "images"
            + File.separator + "products";

    // Category → {gradient top color, gradient bottom color}
    private static final Map<String, Color[]> CATEGORY_COLORS = new LinkedHashMap<>();
    static {
        CATEGORY_COLORS.put("Beverages",     new Color[]{new Color(14,  165, 233), new Color(56,  189, 248)});
        CATEGORY_COLORS.put("Snacks",        new Color[]{new Color(245, 158,  11), new Color(251, 191,  36)});
        CATEGORY_COLORS.put("Dairy",         new Color[]{new Color(99,  102, 241), new Color(129, 140, 248)});
        CATEGORY_COLORS.put("Electronics",   new Color[]{new Color(30,   41,  59), new Color(71,   85, 105)});
        CATEGORY_COLORS.put("Stationery",    new Color[]{new Color(16,  185, 129), new Color(52,  211, 153)});
        CATEGORY_COLORS.put("Clothing",      new Color[]{new Color(239,  68,  68), new Color(248, 113, 113)});
        CATEGORY_COLORS.put("Personal Care", new Color[]{new Color(168,  85, 247), new Color(196, 132, 252)});
        CATEGORY_COLORS.put("Household",     new Color[]{new Color(20,  184, 166), new Color(45,  212, 191)});
        CATEGORY_COLORS.put("Fresh Food",    new Color[]{new Color(34,  197,  94), new Color(74,  222, 128)});
        CATEGORY_COLORS.put("Bakery",        new Color[]{new Color(234,  88,  12), new Color(249, 115,  22)});
    }

    // {name, barcode, category, costPrice, sellingPrice, stockQty}
    private static final Object[][] PRODUCTS = {
        // Beverages (10)
        {"Coca-Cola 330ml",     "BEV001", "Beverages",    0.60, 1.00, 150},
        {"Pepsi 330ml",         "BEV002", "Beverages",    0.60, 1.00, 130},
        {"Sprite 330ml",        "BEV003", "Beverages",    0.60, 1.00, 120},
        {"Fanta Orange",        "BEV004", "Beverages",    0.60, 1.00, 100},
        {"Mineral Water 1.5L",  "BEV005", "Beverages",    0.30, 0.75, 200},
        {"Green Tea 500ml",     "BEV006", "Beverages",    0.80, 1.50,  90},
        {"Orange Juice 1L",     "BEV007", "Beverages",    1.20, 2.50,  80},
        {"Red Bull 250ml",      "BEV008", "Beverages",    1.00, 2.00,  75},
        {"Iced Coffee 250ml",   "BEV009", "Beverages",    0.90, 1.80,  60},
        {"Milk Tea 500ml",      "BEV010", "Beverages",    0.80, 1.50,  85},
        // Snacks (10)
        {"Lay's Original",      "SNK001", "Snacks",       0.80, 1.50, 200},
        {"Doritos Nacho",       "SNK002", "Snacks",       0.90, 1.75, 180},
        {"Pringles Original",   "SNK003", "Snacks",       1.20, 2.25, 150},
        {"Oreo Cookies",        "SNK004", "Snacks",       1.00, 2.00, 160},
        {"KitKat 4-Finger",     "SNK005", "Snacks",       0.70, 1.50, 140},
        {"Snickers Bar",        "SNK006", "Snacks",       0.80, 1.50, 130},
        {"M&Ms Peanut",         "SNK007", "Snacks",       1.00, 2.00, 100},
        {"Cheetos Puffs",       "SNK008", "Snacks",       0.80, 1.50, 110},
        {"Twix Bar",            "SNK009", "Snacks",       0.80, 1.50, 120},
        {"Popcorn Butter",      "SNK010", "Snacks",       0.60, 1.25,  90},
        // Dairy (10)
        {"Fresh Milk 1L",       "DAI001", "Dairy",        1.20, 2.00, 100},
        {"Yogurt Strawberry",   "DAI002", "Dairy",        0.80, 1.50,  80},
        {"Cheese Slice 200g",   "DAI003", "Dairy",        1.50, 2.75,  60},
        {"Butter 250g",         "DAI004", "Dairy",        1.00, 1.80,  70},
        {"Cream Cheese 200g",   "DAI005", "Dairy",        1.20, 2.25,  50},
        {"Ice Cream Vanilla",   "DAI006", "Dairy",        1.50, 3.00,  40},
        {"Condensed Milk",      "DAI007", "Dairy",        0.80, 1.50,  90},
        {"Whipping Cream",      "DAI008", "Dairy",        1.00, 2.00,  45},
        {"Eggs 10pcs",          "DAI009", "Dairy",        1.50, 2.50, 100},
        {"Sour Cream 200g",     "DAI010", "Dairy",        0.90, 1.75,  55},
        // Electronics (10)
        {"USB-C Cable 1m",      "ELC001", "Electronics",  2.00,  4.99, 200},
        {"Phone Case Silicone", "ELC002", "Electronics",  1.50,  4.99, 150},
        {"Screen Protector",    "ELC003", "Electronics",  1.00,  3.99, 180},
        {"Earbuds Basic",       "ELC004", "Electronics",  5.00, 12.99,  80},
        {"Power Bank 10000mAh", "ELC005", "Electronics", 10.00, 24.99,  50},
        {"USB Flash 32GB",      "ELC006", "Electronics",  4.00,  9.99, 120},
        {"Charging Adapter",    "ELC007", "Electronics",  3.00,  7.99, 100},
        {"Laptop Stand",        "ELC008", "Electronics",  8.00, 19.99,  40},
        {"Wireless Mouse",      "ELC009", "Electronics",  6.00, 14.99,  60},
        {"Keyboard Cover",      "ELC010", "Electronics",  3.50,  8.99,  90},
        // Stationery (10)
        {"Notebook A5",         "STA001", "Stationery",   0.50, 1.25, 300},
        {"Ballpoint Pen Blue",  "STA002", "Stationery",   0.20, 0.50, 500},
        {"Pencil HB",           "STA003", "Stationery",   0.15, 0.35, 500},
        {"Eraser Large",        "STA004", "Stationery",   0.20, 0.50, 400},
        {"Ruler 30cm",          "STA005", "Stationery",   0.30, 0.75, 200},
        {"Scissors 20cm",       "STA006", "Stationery",   0.80, 2.00, 150},
        {"Stapler Mini",        "STA007", "Stationery",   1.50, 3.99,  80},
        {"Highlighter Set",     "STA008", "Stationery",   1.20, 2.99, 100},
        {"Correction Fluid",    "STA009", "Stationery",   0.50, 1.25, 120},
        {"Sticky Notes 100pc",  "STA010", "Stationery",   0.80, 1.99, 150},
        // Clothing (10)
        {"White T-Shirt M",     "CLO001", "Clothing",     3.00,  7.99,  80},
        {"Blue Jeans 32",       "CLO002", "Clothing",     8.00, 19.99,  60},
        {"Black Polo M",        "CLO003", "Clothing",     5.00, 12.99,  70},
        {"Cotton Socks 3pk",    "CLO004", "Clothing",     2.00,  4.99, 120},
        {"Baseball Cap",        "CLO005", "Clothing",     3.00,  7.99,  90},
        {"Canvas Tote Bag",     "CLO006", "Clothing",     2.00,  5.99, 100},
        {"Denim Shorts",        "CLO007", "Clothing",     5.00, 13.99,  55},
        {"Hoodie Grey L",       "CLO008", "Clothing",     8.00, 22.99,  40},
        {"Sun Hat Wide",        "CLO009", "Clothing",     3.50,  9.99,  65},
        {"Leather Belt M",      "CLO010", "Clothing",     4.00, 10.99,  50},
        // Personal Care (10)
        {"Shampoo 400ml",       "PER001", "Personal Care", 2.00, 4.99, 100},
        {"Conditioner 400ml",   "PER002", "Personal Care", 2.00, 4.99,  80},
        {"Body Wash 500ml",     "PER003", "Personal Care", 2.50, 5.99,  90},
        {"Toothbrush Soft",     "PER004", "Personal Care", 0.80, 1.99, 200},
        {"Toothpaste 150g",     "PER005", "Personal Care", 1.00, 2.50, 150},
        {"Deodorant Roll-On",   "PER006", "Personal Care", 1.50, 3.99, 100},
        {"Face Wash 150ml",     "PER007", "Personal Care", 2.00, 5.99,  80},
        {"Hand Cream 75ml",     "PER008", "Personal Care", 1.50, 3.99,  70},
        {"Sunscreen SPF50",     "PER009", "Personal Care", 3.00, 7.99,  60},
        {"Razor 5-Blade",       "PER010", "Personal Care", 2.50, 5.99,  90},
        // Household (10)
        {"Dish Soap 500ml",     "HOU001", "Household",    0.80, 1.99, 150},
        {"Laundry Det. 1kg",    "HOU002", "Household",    2.00, 4.99, 100},
        {"Toilet Paper 4pk",    "HOU003", "Household",    1.50, 3.49, 200},
        {"Kitchen Sponge 3pk",  "HOU004", "Household",    0.60, 1.49, 250},
        {"Trash Bag 30L 20pk",  "HOU005", "Household",    1.00, 2.49, 150},
        {"Glass Cleaner 500ml", "HOU006", "Household",    1.20, 2.99,  80},
        {"Mop Refill",          "HOU007", "Household",    2.00, 4.99,  60},
        {"Air Freshener",       "HOU008", "Household",    1.50, 3.49,  90},
        {"Aluminum Foil 10m",   "HOU009", "Household",    1.00, 2.49, 100},
        {"Zip Lock Bags 20pk",  "HOU010", "Household",    0.80, 1.99, 120},
        // Fresh Food (10)
        {"Banana 1kg",          "FRE001", "Fresh Food",   0.60, 1.25,  50},
        {"Apple Red 1kg",       "FRE002", "Fresh Food",   1.00, 2.00,  40},
        {"Orange 1kg",          "FRE003", "Fresh Food",   0.80, 1.75,  45},
        {"Tomato 500g",         "FRE004", "Fresh Food",   0.50, 1.00,  60},
        {"Carrot 500g",         "FRE005", "Fresh Food",   0.40, 0.85,  55},
        {"Lettuce Head",        "FRE006", "Fresh Food",   0.60, 1.25,  30},
        {"Mango 1kg",           "FRE007", "Fresh Food",   1.20, 2.50,  35},
        {"Watermelon Whole",    "FRE008", "Fresh Food",   2.00, 4.00,  15},
        {"Avocado 2pcs",        "FRE009", "Fresh Food",   1.50, 3.00,  25},
        {"Strawberry 500g",     "FRE010", "Fresh Food",   2.00, 4.50,  20},
        // Bakery (10)
        {"White Bread Loaf",    "BAK001", "Bakery",       1.00, 2.25,  40},
        {"Croissant Plain",     "BAK002", "Bakery",       0.60, 1.50,  50},
        {"Chocolate Muffin",    "BAK003", "Bakery",       0.80, 2.00,  35},
        {"Donut Glazed",        "BAK004", "Bakery",       0.50, 1.25,  45},
        {"Bagel Plain",         "BAK005", "Bakery",       0.70, 1.75,  30},
        {"Banana Bread Slice",  "BAK006", "Bakery",       0.90, 2.25,  25},
        {"Cinnamon Roll",       "BAK007", "Bakery",       1.00, 2.50,  30},
        {"Baguette 60cm",       "BAK008", "Bakery",       1.20, 2.75,  20},
        {"Cheese Danish",       "BAK009", "Bakery",       0.80, 2.00,  25},
        {"Rye Bread Loaf",      "BAK010", "Bakery",       1.20, 2.75,  20},
    };

    /**
     * Runs the seed: downloads real product photos (requires internet on first run),
     * saves them to src/main/resources/images/products/ (committed to git so
     * teammates get them on pull), then inserts products into the database.
     * Falls back to generated placeholder images if the network is unavailable.
     *
     * @return number of new products inserted
     */
    public static int seed() {
        new File(RESOURCES_DIR).mkdirs();
        int inserted = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<String, Long> catIds = ensureCategories(conn);
            inserted = insertProducts(conn, catIds);
        } catch (SQLException e) {
            throw new RuntimeException("Seed failed: " + e.getMessage(), e);
        }
        return inserted;
    }

    private static Map<String, Long> ensureCategories(Connection conn) throws SQLException {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (String name : CATEGORY_COLORS.keySet()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM categories WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { ids.put(name, rs.getLong("id")); continue; }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO categories (name, created_at) VALUES (?, NOW())",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) ids.put(name, keys.getLong(1));
                }
            }
        }
        return ids;
    }

    private static int insertProducts(Connection conn,
                                      Map<String, Long> catIds) throws SQLException {
        int count = 0;
        for (Object[] row : PRODUCTS) {
            String name     = (String) row[0];
            String barcode  = (String) row[1];
            String category = (String) row[2];
            double cost     = (double)  row[3];
            double price    = (double)  row[4];
            int    stock    = (int)     row[5];

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM products WHERE barcode = ?")) {
                ps.setString(1, barcode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) continue; // already exists
                }
            }

            // Fetch real image (internet) or fall back to generated placeholder
            String fileName = slug(name) + ".png";
            File   imgFile  = new File(RESOURCES_DIR, fileName);
            if (!imgFile.exists()) {
                System.out.print("  [img] " + name + " ... ");
                BufferedImage downloaded = downloadImage(toSearchKeyword(name));
                if (downloaded != null) {
                    try { ImageIO.write(downloaded, "PNG", imgFile); System.out.println("downloaded"); }
                    catch (IOException e) { System.out.println("save error — using generated"); generateImage(name, category, imgFile); }
                } else {
                    System.out.println("no network — using generated");
                    generateImage(name, category, imgFile);
                }
            }
            String dbPath = DB_PATH_PREFIX + fileName;

            long productId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (name, barcode, image_url, selling_price, cost_price," +
                    " category_id, status, created_at) VALUES (?,?,?,?,?,?,'ACTIVE',NOW())",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, barcode);
                ps.setString(3, dbPath);
                ps.setBigDecimal(4, bd(price));
                ps.setBigDecimal(5, bd(cost));
                Long catId = catIds.get(category);
                if (catId != null) ps.setLong(6, catId);
                else               ps.setNull(6, Types.BIGINT);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) continue;
                    productId = keys.getLong(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO stocks" +
                    " (product_id, quantity, low_stock_alert, updated_at) VALUES (?,?,10,NOW())")) {
                ps.setLong(1, productId);
                ps.setInt(2, stock);
                ps.executeUpdate();
            }
            count++;
        }
        return count;
    }

    /**
     * Downloads a 200×200 photo from loremflickr.com using the product keyword.
     * Returns null if the network is unavailable or the response is not an image.
     */
    private static BufferedImage downloadImage(String keyword) {
        try {
            String encoded = keyword.trim().toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "").trim().replace(" ", ",");
            URL               url  = new URL("https://loremflickr.com/200/200/" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(6_000);
            conn.setReadTimeout(12_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (conn.getResponseCode() == 200) {
                BufferedImage img = ImageIO.read(conn.getInputStream());
                if (img != null) return img;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Strips size/quantity tokens so the search keyword stays meaningful. */
    private static String toSearchKeyword(String name) {
        String kw = name
                .replaceAll("(?i)\\s+\\d+\\s*(ml|cl|l|g|kg|cm|m|gb|mah|pcs?|pk|pc)\\b", "")
                .replaceAll("(?i)\\s+\\d+$", "")
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ").trim();
        return kw.isEmpty() ? name : kw;
    }

    /** Generates a 200×200 colorful PNG label for a product. */
    private static void generateImage(String name, String category, File outFile) {
        if (outFile.exists()) return; // don't overwrite if already committed

        Color[] c = CATEGORY_COLORS.getOrDefault(category,
                new Color[]{new Color(99, 102, 241), new Color(129, 140, 248)});

        int size = 200;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gradient background
        g2.setPaint(new GradientPaint(0, 0, c[0], size, size, c[1]));
        g2.fillRoundRect(0, 0, size, size, 28, 28);

        // Decorative translucent circle (top-right)
        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillOval(size / 2, -size / 4, size, size);

        // Abbreviation (2 initials)
        String abbr = abbreviate(name);
        Font abbrFont = new Font("Arial", Font.BOLD, abbr.length() == 1 ? 78 : 52);
        g2.setFont(abbrFont);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(abbr,
                (size - fm.stringWidth(abbr)) / 2,
                size / 2 + fm.getAscent() / 2 - 12);

        // Dark strip + product name at bottom
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRect(0, size - 42, size, 42);
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        String label = name.length() > 19 ? name.substring(0, 17) + "…" : name;
        g2.drawString(label, (size - fm.stringWidth(label)) / 2, size - 14);

        g2.dispose();

        try {
            ImageIO.write(img, "PNG", outFile);
        } catch (IOException ignored) {}
    }

    private static String abbreviate(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length >= 2)
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        return String.valueOf(name.charAt(0)).toUpperCase();
    }

    private static String slug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "_").replaceAll("_+", "_");
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
