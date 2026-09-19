package pl.crazymod.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import pl.crazymod.setting.BooleanSetting;

/**
 * KOPIOWANIE! - środkowy przycisk myszy (wybór bloku) kopiuje do schowka dane
 * obiektu, na który patrzysz (nick/nazwa, współrzędne, UUID).
 */
public class KopiowanieModule extends CrazyModule {
    private final BooleanSetting name = add(new BooleanSetting("name", "Kopiuj nazwę/nick", true));
    private final BooleanSetting coords = add(new BooleanSetting("coords", "Kopiuj współrzędne", true));
    private final BooleanSetting uuid = add(new BooleanSetting("uuid", "Kopiuj UUID", false));
    private final BooleanSetting message = add(new BooleanSetting("message", "Pokaż komunikat", true));

    private boolean wasDown;

    public KopiowanieModule() {
        super("kopiowanie", "KOPIOWANIE!");
    }

    @Override
    public void onEnable() { wasDown = true; }

    @Override
    public void onTick(MinecraftClient mc) {
        boolean down = mc.options.pickItemKey.isPressed();
        if (down && !wasDown && mc.currentScreen == null) copy(mc);
        wasDown = down;
    }

    private void copy(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit == null || mc.player == null || mc.world == null) return;

        String label;
        BlockPos pos;
        String id = null;

        if (hit instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            label = e.getName().getString();
            pos = e.getBlockPos();
            id = e.getUuidAsString();
        } else if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            pos = bhr.getBlockPos();
            label = mc.world.getBlockState(pos).getBlock().getName().getString();
        } else {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (name.get()) sb.append(label);
        if (coords.get()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(pos.getX()).append(' ').append(pos.getY()).append(' ').append(pos.getZ());
        }
        if (uuid.get() && id != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(id);
        }
        if (sb.length() == 0) return;

        mc.keyboard.setClipboard(sb.toString());
        if (message.get()) {
            mc.player.sendMessage(Text.literal("Skopiowano: " + sb).formatted(Formatting.GREEN), true);
        }
    }
}
