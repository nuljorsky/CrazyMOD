package pl.crazymod.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import pl.crazymod.setting.BooleanSetting;
import pl.crazymod.setting.NumberSetting;

/**
 * AUTO NIELOTKA - automatycznie zamienia elytrę na napierśnik, gdy stoisz na ziemi
 * (jesteś "nielotką"), a opcjonalnie zakłada elytrę, gdy spadasz z wysokości.
 */
public class AutoNielotkaModule extends CrazyModule {
    private static final Item[] CHESTPLATES = {
            Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE,
            Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_CHESTPLATE
    };

    private final BooleanSetting ground = add(new BooleanSetting("ground", "Napierśnik na ziemi", true));
    private final BooleanSetting air = add(new BooleanSetting("air", "Elytra w powietrzu", false));
    private final NumberSetting fall = add(new NumberSetting("fall", "Min. spadek (bloki)", 4, 1, 30, 1));
    private final NumberSetting delay = add(new NumberSetting("delay", "Opóźnienie (ticki)", 10, 1, 40, 1));

    private int cooldown;

    public AutoNielotkaModule() {
        super("auto_nielotka", "AUTO NIELOTKA");
    }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (p.isCreative() || p.isSpectator()) return;
        if (cooldown > 0) { cooldown--; return; }
        if (!p.currentScreenHandler.getCursorStack().isEmpty()) return;

        boolean elytraOn = p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);

        if (elytraOn && ground.get() && p.isOnGround()) {
            int slot = findChestplate(p);
            if (slot >= 0) { swapChest(mc, p, slot); cooldown = delay.getInt(); }
        } else if (!elytraOn && air.get() && !p.isOnGround() && !p.isTouchingWater()
                && p.fallDistance >= fall.get()) {
            int slot = findElytra(p);
            if (slot >= 0) { swapChest(mc, p, slot); cooldown = delay.getInt(); }
        }
    }

    private int findChestplate(ClientPlayerEntity p) {
        for (Item wanted : CHESTPLATES) {
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getStack(i).isOf(wanted)) return i;
            }
        }
        return -1;
    }

    private int findElytra(ClientPlayerEntity p) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(Items.ELYTRA) && s.getMaxDamage() - s.getDamage() > 1) return i;
        }
        return -1;
    }

    /** invIndex: 0-8 hotbar, 9-35 ekwipunek. Slot pancerza (klatka piersiowa) w PlayerScreenHandler = 6. */
    private void swapChest(MinecraftClient mc, ClientPlayerEntity p, int invIndex) {
        int syncId = p.playerScreenHandler.syncId;
        int from = invIndex < 9 ? invIndex + 36 : invIndex;
        mc.interactionManager.clickSlot(syncId, from, 0, SlotActionType.PICKUP, p);
        mc.interactionManager.clickSlot(syncId, 6, 0, SlotActionType.PICKUP, p);
        mc.interactionManager.clickSlot(syncId, from, 0, SlotActionType.PICKUP, p);
    }
}
