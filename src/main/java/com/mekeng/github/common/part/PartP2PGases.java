package com.mekeng.github.common.part;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;

import appeng.api.parts.IPartModel;
import appeng.api.parts.P2PTunnelModels;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnel;

import com.mekeng.github.MekEng;

/**
 * A P2P tunnel for gas: whatever is pushed into the input comes out of every output, split by how much each
 * one's neighbour takes. The gas counterpart of AE2UD's fluid tunnel; nothing can be drawn back through it.
 */
public class PartP2PGases extends PartP2PTunnel<PartP2PGases> implements IGasHandler {

    public static final P2PTunnelModels MODELS = new P2PTunnelModels(MekEng.id("part/p2p_tunnel_gases"));

    private static final ThreadLocal<Deque<PartP2PGases>> DEPTH = new ThreadLocal<>();
    private static final GasTankInfo[] ACTIVE_TANK = {new GasTank(100000)};
    private static final GasTankInfo[] INACTIVE_TANK = {new GasTank(0)};

    @Nullable
    private IGasHandler cachedTank;
    private int tmpUsed;

    public PartP2PGases(final ItemStack is) {
        super(is);
    }

    public float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void onTunnelNetworkChange() {
        this.cachedTank = null;
    }

    @Override
    public void onNeighborChanged(final IBlockAccess w, final BlockPos pos, final BlockPos neighbor) {
        this.cachedTank = null;

        if (this.isOutput()) {
            try {
                for (final PartP2PGases in : this.getInputs()) {
                    if (in != null) {
                        in.onTunnelNetworkChange();
                    }
                }
            } catch (final GridAccessException e) {
                // :P
            }
        }
    }

    @Override
    public boolean hasCapability(final Capability<?> capabilityClass) {
        return capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capabilityClass);
    }

    @Override
    public <T> T getCapability(final Capability<T> capabilityClass) {
        if (capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capabilityClass);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        if (!this.isOutput()) {
            try {
                for (final PartP2PGases tun : this.getInputs()) {
                    if (tun != null) {
                        return ACTIVE_TANK;
                    }
                }
            } catch (final GridAccessException e) {
                // :P
            }
        }
        return INACTIVE_TANK;
    }

    @Override
    public int receiveGas(final EnumFacing side, final GasStack resource, final boolean doTransfer) {
        if (resource == null || resource.amount <= 0 || side != null && side != this.getSide().getFacing()) {
            return 0;
        }

        // A tunnel whose output feeds back into its own input would otherwise recurse until the stack overflows.
        final Deque<PartP2PGases> stack = this.getDepth();
        for (final PartP2PGases t : stack) {
            if (t == this) {
                return 0;
            }
        }
        stack.push(this);
        try {
            return this.distribute(resource, doTransfer);
        } finally {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }
        }
    }

    private int distribute(final GasStack resource, final boolean doTransfer) {
        final List<PartP2PGases> list = this.getGasOutputs();
        int requestTotal = 0;

        Iterator<PartP2PGases> i = list.iterator();
        while (i.hasNext()) {
            final PartP2PGases l = i.next();
            final IGasHandler tank = l.getTarget();
            l.tmpUsed = tank == null ? 0 : tank.receiveGas(l.getSide().getFacing().getOpposite(), resource.copy(), false);

            if (l.tmpUsed <= 0) {
                i.remove();
            } else {
                requestTotal += l.tmpUsed;
            }
        }

        if (requestTotal <= 0) {
            return 0;
        }
        if (!doTransfer) {
            return Math.min(resource.amount, requestTotal);
        }

        int available = resource.amount;
        int used = 0;

        i = list.iterator();
        while (i.hasNext() && available > 0) {
            final PartP2PGases l = i.next();

            final GasStack insert = resource.copy();
            insert.amount = (int) Math.ceil(insert.amount * ((double) l.tmpUsed / (double) requestTotal));
            if (insert.amount > available) {
                insert.amount = available;
            }

            final IGasHandler tank = l.getTarget();
            l.tmpUsed = tank == null ? 0 : tank.receiveGas(l.getSide().getFacing().getOpposite(), insert, true);

            available -= insert.amount;
            used += l.tmpUsed;
        }

        return used;
    }

    @Override
    public GasStack drawGas(final EnumFacing side, final int amount, final boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(final EnumFacing side, final Gas type) {
        return true;
    }

    @Override
    public boolean canDrawGas(final EnumFacing side, final Gas type) {
        return false;
    }

    private Deque<PartP2PGases> getDepth() {
        Deque<PartP2PGases> s = DEPTH.get();
        if (s == null) {
            DEPTH.set(s = new ArrayDeque<>());
        }
        return s;
    }

    private List<PartP2PGases> getGasOutputs() {
        final List<PartP2PGases> outs = new ArrayList<>();
        try {
            for (final PartP2PGases l : this.getOutputs()) {
                if (l.getTarget() != null) {
                    outs.add(l);
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }
        return outs;
    }

    @Nullable
    private IGasHandler getTarget() {
        if (!this.getProxy().isActive()) {
            return null;
        }
        if (this.cachedTank != null) {
            return this.cachedTank;
        }

        final EnumFacing facing = this.getSide().getFacing();
        final TileEntity te = this.getTile().getWorld().getTileEntity(this.getTile().getPos().offset(facing));
        if (te != null && te.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing.getOpposite())) {
            return this.cachedTank = te.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing.getOpposite());
        }
        return null;
    }
}
