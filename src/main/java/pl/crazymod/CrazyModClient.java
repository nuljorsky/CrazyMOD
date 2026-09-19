package pl.crazymod;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import pl.crazymod.gui.ClickGuiScreen;
import pl.crazymod.module.CrazyModule;
import pl.crazymod.module.ModuleManager;

public class CrazyModClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Klawisz do zmiany w Ustawienia -> Sterowanie -> CrazyMOD (domyślnie N)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crazymod.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.crazymod"));

        ModuleManager.init();
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            if (client.player == null || client.world == null) return;
            for (CrazyModule m : ModuleManager.all()) {
                if (m.enabled) m.onTick(client);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;
            for (CrazyModule m : ModuleManager.all()) {
                if (m.enabled) m.onWorldRender(ctx, mc);
            }
        });
    }
}
