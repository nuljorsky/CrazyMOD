package pl.crazymod.module;

import java.util.Locale;

import org.joml.Matrix4f;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pl.crazymod.setting.BooleanSetting;
import pl.crazymod.setting.NumberSetting;

/** NAMETAG - duże, czytelne tabliczki z nickiem, HP i dystansem widoczne przez ściany. */
public class NametagModule extends CrazyModule {
    private final BooleanSetting onlyPlayers = add(new BooleanSetting("players", "Tylko gracze", true));
    private final BooleanSetting health = add(new BooleanSetting("health", "Pokaż HP", true));
    private final BooleanSetting distance = add(new BooleanSetting("distance", "Pokaż dystans", true));
    private final NumberSetting scale = add(new NumberSetting("scale", "Rozmiar", 1.5, 0.5, 4.0, 0.1));
    private final NumberSetting range = add(new NumberSetting("range", "Zasięg", 128, 8, 512, 8));

    public NametagModule() {
        super("nametag", "NAMETAG");
    }

    @Override
    public void onWorldRender(WorldRenderContext ctx, MinecraftClient mc) {
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;

        float delta = ctx.tickCounter().getTickDelta(true);
        Camera camera = ctx.camera();
        Vec3d cam = camera.getPos();
        TextRenderer tr = mc.textRenderer;
        VertexConsumerProvider.Immediate imm = mc.getBufferBuilders().getEntityVertexConsumers();
        boolean any = false;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (onlyPlayers.get() && !(e instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(e);
            if (dist > range.get()) continue;

            Vec3d p = e.getLerpedPos(delta);
            double x = p.x - cam.x;
            double y = p.y + e.getHeight() + 0.75 - cam.y;
            double z = p.z - cam.z;
            float s = 0.025f * scale.getFloat() * Math.max(1f, (float) dist / 8f);

            ms.push();
            ms.translate(x, y, z);
            ms.multiply(camera.getRotation());
            ms.scale(-s, -s, s);
            Matrix4f m = ms.peek().getPositionMatrix();

            String name = e.getName().getString();
            float w = tr.getWidth(name);
            tr.draw(name, -w / 2f, 0, 0xFFFFFFFF, false, m, imm,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0x90000000, 0xF000F0);

            if (health.get() || distance.get()) {
                StringBuilder sb = new StringBuilder();
                float hp = le.getHealth() + le.getAbsorptionAmount();
                if (health.get()) sb.append(String.format(Locale.ROOT, "%.1f HP", hp));
                if (health.get() && distance.get()) sb.append("  ");
                if (distance.get()) sb.append(String.format(Locale.ROOT, "%.0fm", dist));
                float ratio = MathHelper.clamp(hp / Math.max(1f, le.getMaxHealth()), 0f, 1f);
                int col = 0xFF000000 | ((int) ((1f - ratio) * 255) << 16) | ((int) (ratio * 255) << 8) | 0x30;
                float w2 = tr.getWidth(sb.toString());
                tr.draw(sb.toString(), -w2 / 2f, 10, col, false, m, imm,
                        TextRenderer.TextLayerType.SEE_THROUGH, 0x90000000, 0xF000F0);
            }
            ms.pop();
            any = true;
        }
        if (any) imm.draw();
    }
}
