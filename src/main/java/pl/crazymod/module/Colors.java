package pl.crazymod.module;

public final class Colors {
    private Colors() {}

    /** Odcień 0..360 -> 0xRRGGBB (pełne nasycenie i jasność). */
    public static int hsv(float hue) {
        float h = ((hue % 360f) + 360f) % 360f / 60f;
        float x = 1f - Math.abs(h % 2f - 1f);
        float r = 0, g = 0, b = 0;
        switch ((int) h) {
            case 0 -> { r = 1; g = x; }
            case 1 -> { r = x; g = 1; }
            case 2 -> { g = 1; b = x; }
            case 3 -> { g = x; b = 1; }
            case 4 -> { r = x; b = 1; }
            default -> { r = 1; b = x; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
