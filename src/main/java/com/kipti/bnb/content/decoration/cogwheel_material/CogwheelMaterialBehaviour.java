package com.kipti.bnb.content.decoration.cogwheel_material;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

public class CogwheelMaterialBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<CogwheelMaterialBehaviour> TYPE = new BehaviourType<>("cogwheel_material");

    public BlockState material;

    public CogwheelMaterialBehaviour(final SmartBlockEntity be) {
        super(be);
        this.material = Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    @Override
    public void remove() {
        super.remove();
        this.tryTransferOnRemoval();
    }

    private void tryTransferOnRemoval() {
        final BlockEntity replacingBlockEntity = this.getLevel().getBlockEntity(this.getPos());
        final CogwheelMaterialBehaviour replacingBehaviour = this.getSameBehaviour(replacingBlockEntity);

        if (replacingBehaviour == null)
            return;

        replacingBehaviour.material = this.material;
        replacingBehaviour.sendData();

    }

    @Override
    public void onItemUse(final UseItemOnBlockEvent event) {
        final ItemInteractionResult result = this.applyMaterialIfValid(event.getItemStack());

        if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            event.cancelWithResult(result);
        }

        super.onItemUse(event);
    }

    public ItemInteractionResult applyMaterialIfValid(final ItemStack stack) {
        if (!(stack.getItem() instanceof final BlockItem blockItem))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        final BlockState material = blockItem.getBlock()
                .defaultBlockState();
        if (material == this.material)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!material.is(BlockTags.PLANKS))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (this.getLevel().isClientSide() && !this.blockEntity.isVirtual())
            return ItemInteractionResult.SUCCESS;
        this.material = material;
        this.blockEntity.notifyUpdate();
        this.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.getPos(), Block.getId(material));
        return ItemInteractionResult.SUCCESS;
    }

    private void redraw() {
        if (!this.blockEntity.isVirtual())
            this.blockEntity.requestModelDataUpdate();
        if (this.hasLevel()) {
            this.getLevel().sendBlockUpdated(this.getPos(), this.getBlockState(), this.getBlockState(), 16);
            this.getLevel().getChunkSource()
                    .getLightEngine()
                    .checkBlock(this.getPos());
        }
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        final BlockState prevMaterial = this.material;
        if (!compound.contains("Material"))
            return;

        this.material = NbtUtils.readBlockState(this.blockEntity.blockHolderGetter(), compound.getCompound("Material"));
        if (this.material.isAir())
            this.material = Blocks.SPRUCE_PLANKS.defaultBlockState();

        if (clientPacket && prevMaterial != this.material)
            this.redraw();
    }

    @Override
    public void writeSafe(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        tag.put("Material", NbtUtils.writeBlockState(this.material));
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("Material", NbtUtils.writeBlockState(this.material));
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
