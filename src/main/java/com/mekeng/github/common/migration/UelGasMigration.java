package com.mekeng.github.common.migration;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;

import appeng.api.migration.LegacyRewrites;
import appeng.api.migration.LegacyStacks;
import appeng.api.stacks.GenericStack;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.AEGasKey;
import com.mekeng.github.common.me.AEGasKeyType;

/**
 * What the original Mekanism Energistics left in a world saved on AE2 UEL: its gas entries and placeholder, its
 * cells, and the gas parts and interface that AE2UD's own parts replace.
 */
public final class UelGasMigration {

    private static final String AE = "appliedenergistics2:";
    private static final String GAS_TYPE = AEGasKeyType.INSTANCE.getId().toString();
    private static final int CAPACITY_CARD = 27;

    private UelGasMigration() {
    }

    public static void register() {
        LegacyStacks.addReader(stack -> stack.hasKey("gasName", Constants.NBT.TAG_STRING)
                ? gas(stack.getString("gasName"), stack.getLong("Cnt"))
                : null);
        LegacyStacks.addWrapper(id("dummy_gas"), stack -> {
            final NBTTagCompound tag = stack.getCompoundTag("tag");
            return gas(tag.getString("gasName"), (long) tag.getInteger("amount") * Math.max(1, stack.getInteger("Count")));
        });
        for (final String cell : new String[] { "gas_cell_1k", "gas_cell_4k", "gas_cell_16k", "gas_cell_64k",
                "portable_gas_cell" }) {
            LegacyStacks.addCell(id(cell));
        }

        // The cell components are AE2UD's own now.
        component("gas_core_1k", 35);
        component("gas_core_4k", 36);
        component("gas_core_16k", 37);
        component("gas_core_64k", 38);

        LegacyRewrites.remapBlock(id("gas_interface"), AE + "interface");
        LegacyRewrites.addTileRewriter(id("gas_interface"), tile -> {
            gasInterface(tile, "gas interface at " + tile.getInteger("x") + " " + tile.getInteger("y") + " "
                    + tile.getInteger("z"));
            tile.setString("id", AE + "interface");
            // The gas interface faced every way; AE2UD's would face the way the block was placed.
            tile.setBoolean("omniDirectional", true);
            return tile;
        });

        LegacyRewrites.remapItem(id("wireless_gas_terminal"), AE + "wireless_terminal");
        LegacyRewrites.addStackRewriter(id("wireless_gas_terminal"), stack -> {
            final NBTTagCompound tag = stack.getCompoundTag("tag");
            tag.setTag("enabledKeyTypes", onlyGas());
            stack.setTag("tag", tag);
        });

        part("gas_storage_bus", 220, part -> {
            tanks(part, "config", "config", "gas storage bus");
            part.setTag("enabledKeyTypes", onlyGas());
        });
        part("gas_import_bus", 240, part -> {
            tanks(part, "io_config", "config", "gas import bus");
            part.setTag("enabledKeyTypes", onlyGas());
        });
        part("gas_export_bus", 260, part -> tanks(part, "io_config", "config", "gas export bus"));
        part("gas_level_emitter", 280, part -> tanks(part, "config", "config", "gas level emitter"));
        part("gas_terminal", 380, part -> part.setTag("enabledKeyTypes", onlyGas()));
        part("gas_storage_monitor", 400, part -> monitor(part));
        part("gas_conversion_monitor", 420, part -> monitor(part));
        part("gas_interface_part", 440, part -> gasInterface(part, "gas interface part"));
        item("gas_interface_terminal", AE + "part", 521);
    }

    @Nullable
    private static GenericStack gas(final String name, final long amount) {
        final Gas gas = GasRegistry.getGas(name);
        return gas == null ? null : new GenericStack(AEGasKey.of(gas), amount);
    }

    private static void component(final String oldId, final int meta) {
        item(oldId, AE + "material", meta);
    }

    private static void item(final String oldId, final String newId, final int meta) {
        LegacyRewrites.remapItem(id(oldId), newId);
        LegacyRewrites.addStackRewriter(id(oldId), stack -> stack.setShort("Damage", (short) meta));
    }

    /**
     * A gas part becomes the AE2UD part at {@code meta}, its item when held and its data on a cable bus.
     */
    private static void part(final String oldId, final int meta, final PartData rewrite) {
        item(oldId, AE + "part", meta);
        LegacyRewrites.addPartRewriter(id(oldId), (item, part) -> rewrite.rewrite(part));
    }

    /**
     * The tanks kept their gas as {@code {stored: {gasName, amount}, maxGas}}, an empty one as {@code {maxGas}}.
     */
    private static void tanks(final NBTTagCompound data, final String from, final String to, final String where) {
        final NBTTagCompound tanks = data.getCompoundTag(from);
        final NBTTagCompound slots = new NBTTagCompound();
        for (final String key : tanks.getKeySet()) {
            final NBTTagCompound stored = tanks.getCompoundTag(key).getCompoundTag("stored");
            if (stored.isEmpty()) {
                continue;
            }
            final GenericStack read = gas(stored.getString("gasName"), stored.getInteger("amount"));
            if (read == null) {
                LegacyRewrites.log().lost(where + ", tank " + key + ": " + stored);
                continue;
            }
            final NBTTagCompound slot = new NBTTagCompound();
            GenericStack.writeTag(slot, read);
            slots.setTag(key, slot);
        }
        data.removeTag(from);
        data.setTag(to, slots);
    }

    /**
     * The gas interface filled each tank to what its capacity cards allowed, whatever its config said; AE2UD's
     * stocks what its config says, up to its slot's 32000.
     */
    private static void gasInterface(final NBTTagCompound data, final String where) {
        final int cards = capacityCards(data.getCompoundTag("upgrades"));
        final long stock = Math.min((long) Math.pow(4, cards + 1) * 1000, 32000);

        tanks(data, "config", "config", where + ", config");
        final NBTTagCompound config = data.getCompoundTag("config");
        for (final String key : config.getKeySet()) {
            config.getCompoundTag(key).setLong(GenericStack.AMOUNT_FIELD, stock);
        }
        tanks(data, "storage", "storage", where + ", tanks");
    }

    private static void monitor(final NBTTagCompound part) {
        final NBTTagCompound saved = part.getCompoundTag("configuredGas");
        part.removeTag("configuredGas");
        if (!saved.hasKey("gasName")) {
            return;
        }
        final GenericStack read = gas(saved.getString("gasName"), 0);
        if (read == null) {
            LegacyRewrites.log().lost("gas monitor, the gas it showed: " + saved);
            return;
        }
        final NBTTagCompound slot = new NBTTagCompound();
        GenericStack.writeTag(slot, read);
        final NBTTagCompound config = new NBTTagCompound();
        config.setTag("#0", slot);
        part.setTag("config", config);
    }

    private static int capacityCards(final NBTTagCompound upgrades) {
        final NBTTagList items = upgrades.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        int cards = 0;
        for (int i = 0; i < items.tagCount(); i++) {
            final NBTTagCompound item = items.getCompoundTagAt(i);
            if ((AE + "material").equals(item.getString("id")) && item.getShort("Damage") == CAPACITY_CARD) {
                cards += Math.max(1, item.getInteger("Count"));
            }
        }
        return cards;
    }

    private static NBTTagList onlyGas() {
        final NBTTagList types = new NBTTagList();
        types.appendTag(new NBTTagString(GAS_TYPE));
        return types;
    }

    private static String id(final String name) {
        return MekEng.MODID + ":" + name;
    }

    @FunctionalInterface
    private interface PartData {
        void rewrite(NBTTagCompound part);
    }
}
