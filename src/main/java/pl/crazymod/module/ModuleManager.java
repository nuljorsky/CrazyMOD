package pl.crazymod.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private static final List<CrazyModule> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.clear();
        MODULES.add(new AutoNielotkaModule());
        MODULES.add(new TracersModule());
        MODULES.add(new NametagModule());
        MODULES.add(new KopiowanieModule());
    }

    public static List<CrazyModule> all() {
        return Collections.unmodifiableList(MODULES);
    }
}
