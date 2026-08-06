package com.kipti.bnb.mixin.cogwheel_material;

import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialContext;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialContext.TransformKind;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

/**
 * Substitutes {@link CachedBuffers} output with material-keyed {@link SuperByteBuffer}s while a cogwheel material
 * context is set (see {@link SafeBlockEntityRendererMixin}). Only buffers for the cogwheel partials themselves are
 * swapped; metal parts ({@code SHAFT_HALF}, {@code COGWHEEL_SHAFT}, mixer pole/head) fall through to the original.
 */
@Mixin(CachedBuffers.class)
public abstract class CachedBuffersMixin {

    @WrapMethod(method = "partial(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialPartial(
            final PartialModel partial,
            final BlockState referenceState,
            final Operation<SuperByteBuffer> original
    ) {
        return CogwheelMaterialContext.partial(
                partial, referenceState, Direction.UP, TransformKind.NONE,
                () -> original.call(partial, referenceState)
        );
    }

    @WrapMethod(method = "partialFacing(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialPartialFacing(
            final PartialModel partial,
            final BlockState referenceState,
            final Operation<SuperByteBuffer> original
    ) {
        if (!referenceState.hasProperty(FACING))
            return original.call(partial, referenceState);

        return CogwheelMaterialContext.partial(
                partial, referenceState, referenceState.getValue(FACING), TransformKind.FACE,
                () -> original.call(partial, referenceState)
        );
    }

    @WrapMethod(method = "partialFacing(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialPartialFacing(
            final PartialModel partial,
            final BlockState referenceState,
            final Direction facing,
            final Operation<SuperByteBuffer> original
    ) {
        return CogwheelMaterialContext.partial(
                partial, referenceState, facing, TransformKind.FACE,
                () -> original.call(partial, referenceState, facing)
        );
    }

    @WrapMethod(method = "partialFacingVertical(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialPartialFacingVertical(
            final PartialModel partial,
            final BlockState referenceState,
            final Direction facing,
            final Operation<SuperByteBuffer> original
    ) {
        return CogwheelMaterialContext.partial(
                partial, referenceState, facing, TransformKind.FACE_VERTICAL,
                () -> original.call(partial, referenceState, facing)
        );
    }

    @WrapMethod(method = "block(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialBlock(
            final BlockState toRender,
            final Operation<SuperByteBuffer> original
    ) {
        return CogwheelMaterialContext.block(toRender, () -> original.call(toRender));
    }

    @WrapMethod(method = "block(Lnet/createmod/catnip/render/SuperByteBufferCache$Compartment;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;")
    private static SuperByteBuffer bnb$materialBlock(
            final SuperByteBufferCache.Compartment<BlockState> compartment,
            final BlockState toRender,
            final Operation<SuperByteBuffer> original
    ) {
        return CogwheelMaterialContext.block(toRender, () -> original.call(compartment, toRender));
    }

}
