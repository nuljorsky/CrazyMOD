package pl.crazymod.setting;

import com.google.gson.JsonElement;

public abstract class Setting {
    public final String id;
    public final String label;

    protected Setting(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public abstract JsonElement save();

    public abstract void load(JsonElement element);
}
