package com.kipti.bnb.content.decoration.dyeable.tanks;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public class DyeableTankBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<DyeableTankBehaviour> TYPE = new BehaviourType<>("dyeable_fluid_tank");

    @Nullable
    private GayDye gayDye;

    public DyeableTankBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.gayDye != null && this.gayDye.needsTicking()) {
            this.gayDye.tick();
            this.refreshRenderedModel();
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    protected void onColorChanged(@Nullable final DyeColor color) {
        this.refreshConnectedBlocks();
    }

    @Override
    protected void dye(@Nullable final DyeColor color, final boolean single) {
        if (single) {
            this.dyeSinglePart(color);
        } else {
            this.forEachMultiblockPart(behaviour -> behaviour.setColor(color));
        }
    }

    @Nullable
    public DyeColor getDisplayedColor() {
        if (this.gayDye == null || !(this.getBlockEntity() instanceof final FluidTankBlockEntity ftbe)) {
            return this.getColor();
        }
        final int localY = this.getPos().subtract(ftbe.getController()).getY();
        return this.gayDye.getDisplayedColor(localY);
    }

    @Override
    public boolean isDyeingEnabled() {
        return BnbFeatureFlag.DYEABLE_TANKS.isEnabled();
    }

    public void setGayDye(@Nullable final GayDye gayDye) {
        this.gayDye = gayDye == null ? null : gayDye.copy();
        this.refreshOrNotifyUpdate();
    }

    public void applyGayDyeToEntireTank(final GayDye gayDye) {
        this.forEachMultiblockPart(behaviour -> ((DyeableTankBehaviour) behaviour).setGayDye(gayDye));
    }

    @Override
    protected void writeAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries
    ) {
        if (this.gayDye != null) {
            final CompoundTag gay = new CompoundTag();
            this.gayDye.write(gay);
            nbt.put("Gay", gay);
        }
    }

    @Override
    protected boolean readAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
        final GayDye previousGayDye = this.gayDye;
        if (nbt.contains("Gay")) {
            this.gayDye = GayDye.read(nbt.getCompound("Gay"));
        } else {
            this.gayDye = null;
        }
        return previousGayDye == null ? this.gayDye != null : !previousGayDye.visuallyEquals(this.gayDye);
    }

}
