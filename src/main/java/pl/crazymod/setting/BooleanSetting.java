package pl.crazymod.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String id, String label, boolean def) {
        super(id, label);
        this.value = def;
    }

    public boolean get() { return value; }

    public void set(boolean v) { this.value = v; }

    public void toggle() { this.value = !this.value; }

    @Override
    public JsonElement save() { return new JsonPrimitive(value); }

    @Override
    public void load(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) value = e.getAsBoolean();
    }
}
