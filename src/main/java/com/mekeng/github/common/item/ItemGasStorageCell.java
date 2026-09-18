package com.mekeng.github.common.item;

import java.util.Collections;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.stacks.AEKeyType;
import appeng.items.materials.MaterialType;
import appeng.items.storage.AbstractStorageCell;
import appeng.util.InventoryAdaptor;

import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.me.AEGasKeyType;

/**
 * A gas cell: AE2UD's universal component in a gas housing. Bytes per type and idle drain match AE2UD's fluid
 * cells; 15 types, as the original mod's cells had.
 */
public class ItemGasStorageCell extends AbstractStorageCell {

    private final int perType;
    private final double idleDrain;

    public ItemGasStorageCell(final MaterialType component, final int kilobytes, final int perType, final double idleDrain) {
        super(component, kilobytes);
        this.perType = perType;
        this.idleDrain = idleDrain;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public Set<AEKeyType> getKeyTypes() {
        return Collections.singleton(AEGasKeyType.INSTANCE);
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 15;
    }

    @Override
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player) {
        final ItemStack extra = ia.addItems(new ItemStack(ItemAndBlocks.GAS_CELL_HOUSING));
        if (!extra.isEmpty()) {
            player.dropItem(extra, false);
        }
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        return new ItemStack(ItemAndBlocks.GAS_CELL_HOUSING);
    }
}
