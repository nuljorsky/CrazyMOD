package pl.crazymod.module;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import pl.crazymod.setting.BooleanSetting;
import pl.crazymod.setting.NumberSetting;

/** TRACERS - linie od celownika do graczy / mobów (widoczne przez ściany). */
public class TracersModule extends CrazyModule {
    private final BooleanSetting players = add(new BooleanSetting("players", "Gracze", true));
    private final BooleanSetting hostile = add(new BooleanSetting("hostile", "Wrogie moby", false));
    private final BooleanSetting passive = add(new BooleanSetting("passive", "Przyjazne moby", false));
    private final NumberSetting range = add(new NumberSetting("range", "Zasięg", 128, 8, 512, 8));
    private final NumberSetting width = add(new NumberSetting("width", "Grubość", 2, 1, 5, 1));
    private final NumberSetting hue = add(new NumberSetting("hue", "Kolor (odcień)", 0, 0, 360, 5));
    private final BooleanSetting rainbow = add(new BooleanSetting("rainbow", "Tęcza", false));

    public TracersModule() {
        super("tracers", "TRACERS");
    }

    private boolean accept(Entity e) {
        if (e instanceof PlayerEntity) return players.get();
        if (e instanceof Monster) return hostile.get();
        return e instanceof PassiveEntity && passive.get();
    }

    private int colorFor(Entity e) {
        if (e instanceof PlayerEntity) {
            float h = rainbow.get() ? (System.currentTimeMillis() / 15L) % 360L : hue.getFloat();
            return Colors.hsv(h);
        }
        if (e instanceof Monster) return 0xFF5555;
        return 0x55FF55;
    }

    @Override
    public void onWorldRender(WorldRenderContext ctx, MinecraftClient mc) {
        if (ctx.matrixStack() == null) return;
        float delta = ctx.tickCounter().getTickDelta(true);

        List<Entity> targets = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!accept(e)) continue;
            if (mc.player.distanceTo(e) > range.get()) continue;
            targets.add(e);
        }
        if (targets.isEmpty()) return;

        Camera camera = ctx.camera();
        Vec3d cam = camera.getPos();
        Vec3d look = Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(0.5);
        Matrix4f mat = ctx.matrixStack().peek().getPositionMatrix();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(width.getFloat());

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Entity e : targets) {
            Vec3d p = e.getLerpedPos(delta);
            float ex = (float) (p.x - cam.x);
            float ey = (float) (p.y + e.getHeight() / 2.0 - cam.y);
            float ez = (float) (p.z - cam.z);
            int c = colorFor(e);
            int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
            buf.vertex(mat, (float) look.x, (float) look.y, (float) look.z).color(r, g, b, 255);
            buf.vertex(mat, ex, ey, ez).color(r, g, b, 255);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
