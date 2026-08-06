package com.kipti.bnb.content.decoration.cogwheel_material;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Shared cogwheel-material logic for the visual mixins. Implemented by the leaf mixins targeting each visual class;
 * each leaf only declares its {@code @Shadow} cog instance field, its constructor wrap and its {@code update} hook,
 * everything else lives here.
 * <p>
 * This must stay in a regular package (not a defined mixin package): the merged visual classes get it on their
 * {@code implements} list, and Mixin forbids non-mixin code from referencing classes in mixin packages.
 * <p>
 * A {@link State} is kept per leaf (in a {@code @Unique} field) to remember the material the cog instance was built
 * with, and the face it was rotated to. It must be created lazily inside the constructor wrap: Mixin does not
 * execute field initialisers, so the field itself must start out {@code null}.
 */
public interface CogwheelMaterialVisualSupport {

    final class State {
        @Nullable
        public BlockState lastMaterial;
        @Nullable
        public Direction face;
        @Nullable
        public Direction orientation;
    }

    /**
     * Swaps the model of the cog instancer while a cogwheel material is applied, and records it as the last built
     * material. Blocks without a {@link CogwheelMaterialBehaviour} (or without a material) pass through untouched.
     *
     * @param face        the direction the cog model is rotated from ({@code null} for the default top-down orientation)
     * @param orientation the direction the cog model is rotated to ({@code null} for the positive rotation-axis direction)
     */
    default Instancer<RotatingInstance> bnb$materialInstancer(
            final InstancerProvider provider,
            final InstanceType<RotatingInstance> type,
            final Model model,
            final Operation<Instancer<RotatingInstance>> original,
            @Nullable final Direction face,
            @Nullable final Direction orientation,
            final State state,
            final KineticBlockEntity blockEntity,
            final BlockState blockState
    ) {
        state.face = face;
        state.orientation = orientation;

        final CogwheelMaterialBehaviour behaviour = SuperBlockEntityBehaviour.get(blockEntity, CogwheelMaterialBehaviour.TYPE);
        if (behaviour == null || behaviour.material == null)
            return original.call(provider, type, model);

        final BlockState material = behaviour.material;
        state.lastMaterial = material;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(blockState);
        if (variant == null)
            return original.call(provider, type, model);

        return original.call(
                provider, type, CogwheelMaterialVisual.MODEL_CACHE.get(
                        new CogwheelMaterialVisual.ModelKey(variant, material)
                )
        );
    }

    /**
     * Rebuilds the cog instance with the current material if it changed since the instance was last built. Deletes
     * the given instance and returns the replacement, or {@code null} when nothing needs to change (no material
     * engaged, material unchanged, or the block is not a materialisable cogwheel).
     *
     * @param current the cog instance currently assigned to the visual
     */
    @Nullable
    default RotatingInstance bnb$updateCog(
            final State state,
            final KineticBlockEntity blockEntity,
            final BlockState blockState,
            final InstancerProvider provider,
            final BlockPos position,
            final RotatingInstance current
    ) {
        if (state == null || state.lastMaterial == null)
            return null;

        final CogwheelMaterialBehaviour behaviour = SuperBlockEntityBehaviour.get(blockEntity, CogwheelMaterialBehaviour.TYPE);
        if (behaviour == null || behaviour.material == null)
            return null;

        final BlockState material = behaviour.material;
        if (state.lastMaterial == material)
            return null;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(blockState);
        if (variant == null)
            return null;

        state.lastMaterial = material;
        current.delete();

        final Direction orientation = state.orientation != null
                ? state.orientation
                : Direction.get(Direction.AxisDirection.POSITIVE, KineticBlockEntityVisual.rotationAxis(blockState));

        final RotatingInstance updated = provider.instancer(
                        AllInstanceTypes.ROTATING, CogwheelMaterialVisual.MODEL_CACHE.get(
                                new CogwheelMaterialVisual.ModelKey(variant, material)
                        )
                )
                .createInstance()
                .rotateToFace(state.face != null ? state.face : Direction.UP, orientation)
                .setup(blockEntity)
                .setPosition(position);

        updated.setChanged();
        return updated;
    }

}
