package pl.crazymod.gui;

import java.util.Locale;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import pl.crazymod.Config;
import pl.crazymod.CrazyModClient;
import pl.crazymod.module.CrazyModule;
import pl.crazymod.module.ModuleManager;
import pl.crazymod.setting.BooleanSetting;
import pl.crazymod.setting.NumberSetting;
import pl.crazymod.setting.Setting;

/**
 * Menu (ClickGUI). Kategoria "CRAZY" jest jednym panelem: łapiesz za nagłówek i
 * przeciągasz - razem z nią przesuwają się moduły oraz ich ustawienia.
 * LPM na module = włącz/wyłącz, PPM = rozwiń/zwiń ustawienia.
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 150;
    private static final int HEADER_H = 20;
    private static final int ROW_H = 16;
    private static final int SET_H = 14;

    private static final int ACCENT = 0xFF8A2BE2;
    private static final int ACCENT_DARK = 0xFF4B1A80;

    private boolean draggingPanel;
    private double dragOffX, dragOffY;
    private NumberSetting draggingSlider;

    public ClickGuiScreen() {
        super(Text.literal("CrazyMOD"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
    }

    private int panelHeight() {
        int h = HEADER_H;
        for (CrazyModule m : ModuleManager.all()) {
            h += ROW_H;
            if (m.expanded) h += m.settings.size() * SET_H;
        }
        return h;
    }

    private void clampPanel() {
        Config.panelX = MathHelper.clamp(Config.panelX, 0, Math.max(0, this.width - W));
        Config.panelY = MathHelper.clamp(Config.panelY, 0, Math.max(0, this.height - panelHeight()));
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------ render

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        clampPanel();

        int x = Config.panelX, y = Config.panelY;
        int h = panelHeight();

        ctx.fill(x - 1, y - 1, x + W + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + W, y + HEADER_H, ACCENT);
        String title = "CRAZY";
        ctx.drawTextWithShadow(textRenderer, title, x + (W - textRenderer.getWidth(title)) / 2, y + 6, 0xFFFFFFFF);

        int cy = y + HEADER_H;
        for (CrazyModule m : ModuleManager.all()) {
            boolean hover = inside(mx, my, x, cy, W, ROW_H);
            int bg = m.enabled ? (hover ? 0xF0632AA8 : 0xF0502090) : (hover ? 0xF0303030 : 0xF01A1A1A);
            ctx.fill(x, cy, x + W, cy + ROW_H, bg);
            ctx.drawTextWithShadow(textRenderer, m.name, x + 6, cy + 4, m.enabled ? 0xFFFFFFFF : 0xFFAAAAAA);
            if (!m.settings.isEmpty()) {
                String mark = m.expanded ? "-" : "+";
                ctx.drawTextWithShadow(textRenderer, mark, x + W - 12, cy + 4, 0xFFCCCCCC);
            }
            cy += ROW_H;

            if (m.expanded) {
                for (Setting s : m.settings) {
                    renderSetting(ctx, s, x, cy, inside(mx, my, x, cy, W, SET_H));
                    cy += SET_H;
                }
            }
        }

        String hint = "LPM: włącz/wyłącz   PPM: ustawienia   Przeciągnij nagłówek CRAZY, aby przesunąć";
        ctx.drawTextWithShadow(textRenderer, hint, (this.width - textRenderer.getWidth(hint)) / 2, this.height - 14, 0xFFBBBBBB);
    }

    private void renderSetting(DrawContext ctx, Setting s, int x, int y, boolean hover) {
        ctx.fill(x, y, x + W, y + SET_H, hover ? 0xF0181818 : 0xF0101010);
        ctx.fill(x, y, x + 2, y + SET_H, ACCENT);

        if (s instanceof BooleanSetting bs) {
            ctx.drawTextWithShadow(textRenderer, s.label, x + 8, y + 3, 0xFFDDDDDD);
            String v = bs.get() ? "ON" : "OFF";
            ctx.drawTextWithShadow(textRenderer, v, x + W - 6 - textRenderer.getWidth(v), y + 3,
                    bs.get() ? 0xFF55FF55 : 0xFFFF5555);
        } else if (s instanceof NumberSetting ns) {
            ctx.drawTextWithShadow(textRenderer, s.label, x + 8, y + 1, 0xFFDDDDDD);
            String v = ns.isInteger() ? String.valueOf(ns.getInt()) : String.format(Locale.ROOT, "%.1f", ns.get());
            ctx.drawTextWithShadow(textRenderer, v, x + W - 6 - textRenderer.getWidth(v), y + 1, 0xFFFFFFFF);
            int tx1 = x + 6, tx2 = x + W - 6;
            ctx.fill(tx1, y + SET_H - 3, tx2, y + SET_H - 1, ACCENT_DARK);
            double frac = (ns.get() - ns.min) / (ns.max - ns.min);
            ctx.fill(tx1, y + SET_H - 3, tx1 + (int) ((tx2 - tx1) * frac), y + SET_H - 1, ACCENT);
        }
    }

    // ------------------------------------------------------------ input

    private void updateSlider(NumberSetting ns, double mx) {
        int tx1 = Config.panelX + 6, tx2 = Config.panelX + W - 6;
        double frac = MathHelper.clamp((mx - tx1) / (double) (tx2 - tx1), 0.0, 1.0);
        ns.set(ns.min + frac * (ns.max - ns.min));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = Config.panelX, y = Config.panelY;

        if (inside(mx, my, x, y, W, HEADER_H)) {
            if (button == 0) {
                draggingPanel = true;
                dragOffX = mx - x;
                dragOffY = my - y;
            }
            return true;
        }

        int cy = y + HEADER_H;
        for (CrazyModule m : ModuleManager.all()) {
            if (inside(mx, my, x, cy, W, ROW_H)) {
                if (button == 0) m.toggle();
                else if (button == 1 && !m.settings.isEmpty()) m.expanded = !m.expanded;
                return true;
            }
            cy += ROW_H;

            if (m.expanded) {
                for (Setting s : m.settings) {
                    if (inside(mx, my, x, cy, W, SET_H)) {
                        if (button == 0) {
                            if (s instanceof BooleanSetting bs) bs.toggle();
                            else if (s instanceof NumberSetting ns) {
                                draggingSlider = ns;
                                updateSlider(ns, mx);
                            }
                        }
                        return true;
                    }
                    cy += SET_H;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0) {
            if (draggingPanel) {
                Config.panelX = (int) Math.round(mx - dragOffX);
                Config.panelY = (int) Math.round(my - dragOffY);
                clampPanel();
                return true;
            }
            if (draggingSlider != null) {
                updateSlider(draggingSlider, mx);
                return true;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0 && (draggingPanel || draggingSlider != null)) {
            draggingPanel = false;
            draggingSlider = null;
            Config.save();
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CrazyModClient.openGuiKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        Config.save();
    }
}
