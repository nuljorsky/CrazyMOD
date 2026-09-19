package pl.crazymod;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import pl.crazymod.module.CrazyModule;
import pl.crazymod.module.ModuleManager;
import pl.crazymod.setting.Setting;

/** Zapis/odczyt stanu (config/crazymod.json): pozycja panelu, włączone moduły, ustawienia. */
public final class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("CrazyMOD");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static int panelX = 40;
    public static int panelY = 40;

    private Config() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("crazymod.json");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            if (root.has("panelX")) panelX = root.get("panelX").getAsInt();
            if (root.has("panelY")) panelY = root.get("panelY").getAsInt();
            if (!root.has("modules")) return;
            JsonObject mods = root.getAsJsonObject("modules");
            for (CrazyModule m : ModuleManager.all()) {
                if (!mods.has(m.id)) continue;
                JsonObject mo = mods.getAsJsonObject(m.id);
                if (mo.has("enabled")) m.enabled = mo.get("enabled").getAsBoolean();
                if (mo.has("expanded")) m.expanded = mo.get("expanded").getAsBoolean();
                if (mo.has("settings")) {
                    JsonObject so = mo.getAsJsonObject("settings");
                    for (Setting s : m.settings) if (so.has(s.id)) s.load(so.get(s.id));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Nie udało się wczytać configu CrazyMOD", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("panelX", panelX);
            root.addProperty("panelY", panelY);
            JsonObject mods = new JsonObject();
            for (CrazyModule m : ModuleManager.all()) {
                JsonObject mo = new JsonObject();
                mo.addProperty("enabled", m.enabled);
                mo.addProperty("expanded", m.expanded);
                JsonObject so = new JsonObject();
                for (Setting s : m.settings) so.add(s.id, s.save());
                mo.add("settings", so);
                mods.add(m.id, mo);
            }
            root.add("modules", mods);
            try (Writer w = Files.newBufferedWriter(file())) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            LOGGER.warn("Nie udało się zapisać configu CrazyMOD", e);
        }
    }
}
