package pl.crazymod.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting {
    public final double min, max, step;
    private double value;

    public NumberSetting(String id, String label, double def, double min, double max, double step) {
        super(id, label);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = def;
    }

    public double get() { return value; }

    public int getInt() { return (int) Math.round(value); }

    public float getFloat() { return (float) value; }

    public void set(double v) {
        v = Math.round(v / step) * step;
        this.value = Math.max(min, Math.min(max, v));
    }

    public boolean isInteger() { return step >= 1.0 && step == Math.floor(step); }

    @Override
    public JsonElement save() { return new JsonPrimitive(value); }

    @Override
    public void load(JsonElement e) {
        if (e != null && e.isJsonPrimitive()) set(e.getAsDouble());
    }
}
