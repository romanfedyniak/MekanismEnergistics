package com.mekeng.github.common.item;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;

import com.mekeng.github.common.part.PartP2PGases;

/**
 * The gas P2P tunnel as an item, placed on a cable like any other part. Kept at {@code mekeng:gas_p2p}, the
 * original mod's id, so tunnels already in a world stay what they are.
 */
public final class ItemGasP2P extends Item implements IPartItem<PartP2PGases> {

    @Nullable
    @Override
    public PartP2PGases createPartFromItemStack(final ItemStack is) {
        return new PartP2PGases(is);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos,
            final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }
}
