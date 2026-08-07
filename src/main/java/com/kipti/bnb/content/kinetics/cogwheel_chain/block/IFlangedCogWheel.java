package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker interface for flanged cogwheel variants (bare and encased).
 * <p>
 * Flanged cogwheels mesh with regular cogwheels, but never with each other, and
 * large flanged cogwheels never mesh with large cogwheels in any orientation.
 * The actual rules are enforced in {@code FlangedCogwheelRotationPropagatorMixin}.
 */
public interface IFlangedCogWheel {

    static boolean isFlanged(final Block block) {
        return block instanceof IFlangedCogWheel;
    }

    static boolean isFlanged(final BlockState state) {
        return isFlanged(state.getBlock());
    }
}
