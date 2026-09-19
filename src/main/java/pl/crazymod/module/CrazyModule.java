package pl.crazymod.module;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import pl.crazymod.setting.Setting;

public abstract class CrazyModule {
    public final String id;
    public final String name;
    public boolean enabled;
    public boolean expanded;
    public final List<Setting> settings = new ArrayList<>();

    protected CrazyModule(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onTick(MinecraftClient mc) {}

    public void onWorldRender(WorldRenderContext ctx, MinecraftClient mc) {}
}
