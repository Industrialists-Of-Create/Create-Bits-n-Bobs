package com.kipti.bnb.content.kinetics.encased_blocks.cogwheel;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.IFlangedCogWheel;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class BnbEncasedFlangedCogBlock extends BnbEncasedCogwheelBlock implements IFlangedCogWheel {
    private final Supplier<Block> flangedCogwheel;

    public BnbEncasedFlangedCogBlock(final Properties properties, final boolean large, final Supplier<Block> casing, final Supplier<Block> flangedCogwheel) {
        super(properties, large, casing);
        this.flangedCogwheel = flangedCogwheel;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;
        context.getLevel()
                .levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(state));
        KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
                this.flangedCogwheel.get().defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull ItemStack getCloneItemStack(final BlockState state, final HitResult target, final LevelReader level, final BlockPos pos, final Player player) {
        if (target instanceof final BlockHitResult blockHitResult)
            return blockHitResult.getDirection()
                    .getAxis() != this.getRotationAxis(state)
                    ? this.flangedCogwheel.get().asItem().getDefaultInstance()
                    : this.getCasing().asItem().getDefaultInstance();
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, final BlockEntity be) {
        return ItemRequirement.of(this.flangedCogwheel.get().defaultBlockState(), be);
    }

    @Override
    public BlockEntityType<? extends SimpleKineticBlockEntity> getBlockEntityType() {
        return this.isLarge ? BnbBlockEntities.ENCASED_LARGE_FLANGED_COGWHEEL.get() : BnbBlockEntities.ENCASED_FLANGED_COGWHEEL.get();
    }
}
