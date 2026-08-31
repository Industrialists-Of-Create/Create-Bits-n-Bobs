package com.kipti.bnb.content.decoration.dyeable;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class BaseDyeableBehaviour extends SuperBlockEntityBehaviour {

    @Nullable
    private DyeColor color;

    protected BaseDyeableBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void onItemUse(final UseItemOnBlockEvent event) {
        if (!this.isDyeingEnabled()) return;

        final ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof final DyeItem dyeItem)) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(event.getPlayer());
            this.dye(dyeItem.getDyeColor(), event.getPlayer() != null && event.getPlayer().isShiftKeyDown());
        }

        event.setCanceled(true);
        event.setCancellationResult(ItemInteractionResult.SUCCESS);
    }

    protected void dye(@Nullable final DyeColor color, final boolean single) {
        this.setColor(color);
    }

    public boolean isDyeingEnabled() {
        return BnbFeatureFlag.DYEABLE_PIPES.isEnabled();
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    @Nullable
    public DyeColor getDisplayedColor() {
        return this.getColor();
    }

    public void applyColorClientOnly(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.refreshRenderedModel();
    }

    public void setColor(@Nullable final DyeColor color) {
        if (!this.isDyeingEnabled() && this.color == null) {
            return;
        }
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.onColorChanged(color);
        this.refreshOrNotifyUpdate();
    }

    protected void onColorChanged(@Nullable final DyeColor color) {
    }

    protected final void refreshConnectedBlocks() {
        if (!this.hasLevel()) {
            return;
        }
        this.getLevel()
                .updateNeighborsAt(this.getPos(), this.getBlockState().getBlock());
    }

    protected final void dyeSinglePart(@Nullable final DyeColor color) {
        if (!(this.blockEntity instanceof final IMultiBlockEntityContainer be)) {
            this.setColor(color);
            return;
        }
        this.dyeSinglePart((BlockEntity & IMultiBlockEntityContainer) be, color);
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity & IMultiBlockEntityContainer> void dyeSinglePart(final T be, @Nullable final DyeColor color) {
        final T controllerBE = be.getControllerBE();
        final boolean wasMulti = controllerBE != null && (controllerBE.getWidth() > 1 || controllerBE.getHeight() > 1);

        if (wasMulti) {
            ConnectivityHandler.splitMulti(controllerBE);
        }

        this.setColor(color);
        ConnectivityHandler.formMulti(be);

        if (wasMulti && controllerBE != be) {
            ConnectivityHandler.formMulti(controllerBE);
        }
    }

    protected final void forEachMultiblockPart(final Consumer<BaseDyeableBehaviour> action) {
        if (!this.hasLevel()) {
            return;
        }

        if (!(this.blockEntity instanceof final IMultiBlockEntityContainer be)) {
            action.accept(this);
            return;
        }

        final IMultiBlockEntityContainer controllerBE = be.getControllerBE();
        if (controllerBE == null) {
            action.accept(this);
            return;
        }

        final Level level = this.getLevel();
        final BlockPos origin = ((BlockEntity) controllerBE).getBlockPos();
        final Direction.Axis axis = controllerBE.getMainConnectionAxis();
        final int width = controllerBE.getWidth();
        final int length = controllerBE.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < length; y++) {
                for (int z = 0; z < width; z++) {
                    final BlockPos pos = switch (axis) {
                        case X -> origin.offset(y, x, z);
                        case Y -> origin.offset(x, y, z);
                        case Z -> origin.offset(x, z, y);
                    };
                    final BaseDyeableBehaviour behaviour = DyeableMultiblockTypes.get(level, pos);
                    if (behaviour != null) {
                        action.accept(behaviour);
                    }
                }
            }
        }
    }

    protected void refreshOrNotifyUpdate() {
        if (this.hasLevel() && this.getLevel().isClientSide) {
            this.refreshRenderedModel();
        } else {
            this.blockEntity.notifyUpdate();
        }
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        this.writeDyeColor(nbt);
        this.writeAdditionalDyeData(nbt, registries);
    }

    @Override
    public void writeSafe(final CompoundTag nbt, final HolderLookup.Provider registries) {
        super.writeSafe(nbt, registries);
        this.writeDyeColor(nbt);
        this.writeAdditionalDyeData(nbt, registries);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    protected void writeDyeColor(final CompoundTag nbt) {
        if (this.color != null) {
            nbt.putInt("DyeColor", this.color.getId());
        }
    }

    protected void writeAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries
    ) {
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        final boolean dyeColorChanged = this.readDyeColor(nbt);
        final boolean additionalDyeDataChanged = this.readAdditionalDyeData(nbt, registries, clientPacket);
        if (clientPacket && (dyeColorChanged || additionalDyeDataChanged)) {
            this.refreshRenderedModel();
        }
    }

    protected boolean readDyeColor(final CompoundTag nbt) {
        final DyeColor previousColor = this.color;
        this.color = getDyeColorFromTag(nbt);
        return previousColor != this.color;
    }

    public static @Nullable DyeColor getDyeColorFromTag(final CompoundTag nbt) {
        if (nbt.contains("DyeColor")) {
            return DyeColor.byId(nbt.getInt("DyeColor"));
        } else {
            return null;
        }
    }

    protected boolean readAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
        return false;
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
        if (this.hasLevel()) {
            final Level level = this.getLevel();
            final BlockPos pos = this.getPos();
            level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 16);
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(this.getBlockEntity()));
        }
    }

}
