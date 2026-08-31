package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.IExclusiveCogwheelChainBlock;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

public record CogwheelChainCandidate(Direction.Axis axis, boolean isLarge, boolean hasSmallCogwheelOffset) {

    private static final ConcurrentHashMap<Block, Boolean> VALID_BLOCKS_CACHE = new ConcurrentHashMap<>();

    public static Direction.Axis getAxis(final BlockState state) {
        if (state.getBlock() instanceof final ICogWheel cogwheelBlock)
            return cogwheelBlock.getRotationAxis(state);
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return exclusiveBlock.getRotationAxis(state);
        if (state.hasProperty(BlockStateProperties.AXIS))
            return state.getValue(BlockStateProperties.AXIS);
        if (state.hasProperty(BlockStateProperties.FACING))
            return state.getValue(BlockStateProperties.FACING).getAxis();
        return Direction.Axis.Y;
    }

    public static boolean isValidCandidate(final BlockState state) {
        final Block block = state.getBlock();
        return isValidCandidate(block);
    }

    public static boolean isValidCandidate(final Block blockToCheck) {
        return VALID_BLOCKS_CACHE.computeIfAbsent(blockToCheck, (block) -> {

            if (!(block instanceof ICogWheel ||
                    block instanceof IExclusiveCogwheelChainBlock ||
                    BnbTags.BnbBlockTags.EXTRA_COGWHEEL_CHAIN_CANDIDATES.matches(block)))
                return false;

            if (!BnbTags.BnbBlockTags.DEDICATED_COGWHEEL_CHAIN_COMPONENT.matches(block) &&
                    !BnbFeatureFlag.UNDEDICATED_COGWHEEL_CHAIN_DRIVES.isEnabled())
                return false;

            if (BnbTags.BnbBlockTags.FORBIDDEN_COGWHEEL_CHAIN_COMPONENT.matches(block))
                return false;

            if (!(block instanceof final IBE<?> ibe))
                return false;

            //Explanation: PetrolsParts has a thing where a block entity is registered for a block that in practice belongs to another BE
            //That other be may nto be a KBE even though the block entity we are looking at now is (in a kinetic multipart like a differential)
            //Here we check whether the REAL block entity of this block entitiy's block is a KBE
            //Unfortunately, this check is limited to only addons that use IBE (since EntityBlock causes circular loading order in the way ive built behaviour system)
            //This check has to be as late as possible to avoid impact (and gets cached)
            final Class<?> be = ibe.getBlockEntityClass();
            return KineticBlockEntity.class.isAssignableFrom(be);
        });
    }

    public static boolean isLargeCogwheel(final BlockState state) {
        if (state.getBlock() instanceof final ICogWheel cogwheelBlock)
            return cogwheelBlock.isLargeCog();
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return exclusiveBlock.isLargeCog();
        return false;
    }

    private static boolean hasSmallCogwheelOffset(final BlockState state) {
        if (BnbTags.BnbBlockTags.COGWHEEL_CHAIN_NO_SMALL_OFFSET.matches(state))
            return false;
        if (state.getBlock() instanceof final ICogWheel cogwheelBlock)
            return !cogwheelBlock.isLargeCog();
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return !exclusiveBlock.isLargeCog();
        return true;
    }

    public static @Nullable CogwheelChainCandidate getForBlock(final BlockState state) {
        if (state == null || !isValidCandidate(state))
            return null;
        return new CogwheelChainCandidate(getAxis(state), isLargeCogwheel(state), hasSmallCogwheelOffset(state));
    }

    public static @Nullable CogwheelChainCandidate getForBlock(final Block block) {
        return getForBlock(block.defaultBlockState());
    }

    public boolean isConsistentWithNode(final ICogwheelNode node) {
        if (node.isLarge() != this.isLarge)
            return false;
        if (node.rotationAxis() != this.axis)
            return false;
        return node.hasSmallCogwheelOffset() == this.hasSmallCogwheelOffset;
    }
}

