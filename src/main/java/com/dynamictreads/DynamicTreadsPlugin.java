package com.dynamictreads;

import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@PluginDescriptor(
        name = "Dynamic Treads",
        description = "Visually swaps Avernic Treads variants for Primordial / Pegasian / Eternal boots based on the equipped weapon's attack type",
        tags = {"transmog", "cosmetic", "boots", "avernic", "primordial", "pegasian", "eternal"}
)
public class DynamicTreadsPlugin extends Plugin {
    // PlayerComposition stores items in the kit array as itemId + PlayerComposition.ITEM_OFFSET (2048).
    // Values below the offset are kit/skin ids (no item equipped in that slot).

    private enum Style {
        MELEE, RANGE, MAGE
    }

    // The Avernic Treads variants we manage, mapped to the set of attack styles each one is
    // permitted to transmog into. A variant only swaps when the wielded weapon's style is in
    // its allowed set; otherwise the source boot is shown unchanged.
    private static final Map<Integer, EnumSet<Style>> SOURCES;

    static {
        Map<Integer, EnumSet<Style>> m = new HashMap<>();
        m.put(31091, EnumSet.of(Style.MELEE));                              // Avernic Treads (pr)
        m.put(31092, EnumSet.of(Style.RANGE));                              // Avernic Treads (pe)
        m.put(31093, EnumSet.of(Style.MAGE));                               // Avernic Treads (et)
        m.put(31094, EnumSet.of(Style.MELEE, Style.RANGE));                 // Avernic Treads (pr)(pe)
        m.put(31095, EnumSet.of(Style.MELEE, Style.MAGE));                  // Avernic Treads (pr)(et)
        m.put(31096, EnumSet.of(Style.RANGE, Style.MAGE));                  // Avernic Treads (pe)(et)
        m.put(31097, EnumSet.of(Style.MELEE, Style.RANGE, Style.MAGE));     // Avernic Treads (max)
        SOURCES = Collections.unmodifiableMap(m);
    }

    // Every item id that any of the four dropdown configs could possibly hold. Used by isManagedBoot
    // so we still recognise a previously-applied transmog after the user changes their dropdown choice.
    private static final Set<Integer> ALL_TARGET_IDS;

    static {
        Set<Integer> s = new HashSet<>();
        for (DynamicTreadsConfig.DefaultBoot b : DynamicTreadsConfig.DefaultBoot.values()) {
            s.add(b.getItemId());
        }
        for (DynamicTreadsConfig.MeleeBoot b : DynamicTreadsConfig.MeleeBoot.values()) {
            s.add(b.getItemId());
        }
        for (DynamicTreadsConfig.RangeBoot b : DynamicTreadsConfig.RangeBoot.values()) {
            s.add(b.getItemId());
        }
        for (DynamicTreadsConfig.MageBoot b : DynamicTreadsConfig.MageBoot.values()) {
            s.add(b.getItemId());
        }
        ALL_TARGET_IDS = Collections.unmodifiableSet(s);
    }

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ItemManager itemManager;
    @Inject
    private DynamicTreadsConfig config;

    @Provides
    @SuppressWarnings("unused")
    DynamicTreadsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DynamicTreadsConfig.class);
    }

    @Override
    protected void shutDown() {
        clientThread.invoke(this::restoreOriginalBoots);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onGameTick(GameTick event) {
        applyTransmog();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerChanged(PlayerChanged event) {
        if (event.getPlayer() == client.getLocalPlayer()) {
            applyTransmog();
        }
    }

    private void applyTransmog() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }
        PlayerComposition comp = player.getPlayerComposition();
        if (comp == null) {
            return;
        }

        int[] kit = comp.getEquipmentIds();
        int bootsKitSlot = KitType.BOOTS.getIndex();
        int currentRaw = kit[bootsKitSlot];
        if (currentRaw < PlayerComposition.ITEM_OFFSET) {
            // Slot holds a kit/skin id, not an equipped item.
            return;
        }

        int currentItemId = currentRaw - PlayerComposition.ITEM_OFFSET;

        // Use the real equipment container as the source of truth — the kit may already hold a previous transmog.
        int equippedId = equippedBootsId();
        EnumSet<Style> allowed = SOURCES.get(equippedId);
        if (allowed == null) {
            // Not wearing an Avernic Treads variant. If the kit is still showing one of our
            // previously-applied transmog targets, restore it to whatever is actually equipped
            // so we never leave a transmog visible over a non-Avernic boot.
            if (equippedId > 0 && currentItemId != equippedId && ALL_TARGET_IDS.contains(currentItemId)) {
                kit[bootsKitSlot] = equippedId + PlayerComposition.ITEM_OFFSET;
                comp.setHash();
            }
            return;
        }

        // Only intervene when the kit currently shows a boot we manage (source variant or a prior transmog target).
        if (!isManagedBoot(currentItemId)) {
            return;
        }

        int targetItemId = pickTargetBoots(allowed);
        int targetRaw = targetItemId + PlayerComposition.ITEM_OFFSET;
        if (currentRaw == targetRaw) {
            return;
        }
        kit[bootsKitSlot] = targetRaw;
        comp.setHash();
    }

    private boolean isManagedBoot(int itemId) {
        return SOURCES.containsKey(itemId) || ALL_TARGET_IDS.contains(itemId);
    }

    private int equippedBootsId() {
        ItemContainer eq = client.getItemContainer(InventoryID.WORN);
        if (eq == null) {
            return -1;
        }
        Item boots = eq.getItem(EquipmentInventorySlot.BOOTS.getSlotIdx());
        return boots == null ? -1 : boots.getId();
    }

    private int pickTargetBoots(EnumSet<Style> allowed) {
        Style style = currentWeaponStyle();
        if (style == null || !allowed.contains(style)) {
            // Unarmed, or the variant doesn't carry this style's boot — show the configured default.
            return config.defaultBoot().getItemId();
        }
        switch (style) {
            case MAGE:
                return config.mageBoot().getItemId();
            case RANGE:
                return config.rangeBoot().getItemId();
            case MELEE:
                return config.meleeBoot().getItemId();
            default:
                return config.defaultBoot().getItemId();
        }
    }

    private Style currentWeaponStyle() {
        ItemContainer eq = client.getItemContainer(InventoryID.WORN);
        if (eq == null) {
            return null;
        }
        Item weapon = eq.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        if (weapon == null) {
            return null;
        }

        ItemStats stats = itemManager.getItemStats(weapon.getId());
        if (stats == null || !stats.isEquipable()) {
            return Style.MELEE;
        }
        ItemEquipmentStats eqStats = stats.getEquipment();
        if (eqStats == null) {
            return Style.MELEE;
        }

        int melee = Math.max(eqStats.getAstab(), Math.max(eqStats.getAslash(), eqStats.getAcrush()));
        int range = eqStats.getArange();
        int magic = eqStats.getAmagic();

        if (magic > melee && magic >= range) {
            return Style.MAGE;
        }
        if (range > melee && range >= magic) {
            return Style.RANGE;
        }
        return Style.MELEE;
    }

    private void restoreOriginalBoots() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }
        PlayerComposition comp = player.getPlayerComposition();
        if (comp == null) {
            return;
        }
        int real = equippedBootsId();
        if (real < 0) {
            return;
        }
        int[] kit = comp.getEquipmentIds();
        int slot = KitType.BOOTS.getIndex();
        int desired = real + PlayerComposition.ITEM_OFFSET;
        if (kit[slot] != desired) {
            kit[slot] = desired;
            comp.setHash();
        }
    }
}
