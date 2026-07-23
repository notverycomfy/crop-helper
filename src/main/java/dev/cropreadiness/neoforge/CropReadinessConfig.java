package dev.cropreadiness.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class CropReadinessConfig {
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("crop-helper.properties");
    private static final int[] DEFAULTS = {0x32FF6A, 0xFF8C1A, 0x4DA3FF, 0xFFE14A, 0xFF3B30};
    private static final String[] KEYS = {"mature", "needs_water", "blocked", "dry_farmland", "sapling_cramped"};
    private static final int[] colors = DEFAULTS.clone();
    private CropReadinessConfig() {}

    static void load() {
        Properties properties = new Properties();
        if (Files.isRegularFile(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) { properties.load(reader); }
            catch (IOException ignored) {}
        }
        for (int i = 0; i < KEYS.length; i++) colors[i] = parse(properties.getProperty(KEYS[i]), DEFAULTS[i]);
    }
    static void save(int[] values) throws IOException {
        System.arraycopy(values, 0, colors, 0, colors.length);
        Properties properties = new Properties();
        for (int i = 0; i < KEYS.length; i++) properties.setProperty(KEYS[i], hex(colors[i]));
        Files.createDirectories(FILE.getParent());
        try (Writer writer = Files.newBufferedWriter(FILE)) { properties.store(writer, "Crop Helper outline colors"); }
    }
    static int color(int index) { return 0x88000000 | colors[index]; }
    static int defaultColor(int index) { return DEFAULTS[index]; }
    static String hex(int color) { return String.format("#%06X", color & 0xFFFFFF); }
    static int parse(String value, int fallback) {
        if (value == null) return fallback;
        String clean = value.trim().replace("#", "");
        if (clean.length() != 6) return fallback;
        try { return Integer.parseInt(clean, 16); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
