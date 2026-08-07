package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.IFlangedCogWheel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flanged cogwheels mesh like normal cogwheels, except that they never mesh with other
 * flanged cogwheels, and large flanged cogwheels never mesh with large cogwheels.
 * Shaft connections along the rotation axis are unaffected.
 */
@Mixin(RotationPropagator.class)
public class FlangedCogwheelRotationMixin {

    @Inject(method = "isLargeToLargeGear", at = @At("HEAD"), cancellable = true)
    private static void bnb$largeFlangedCogwheelsNeverMesh(final BlockState from,
                                                           final BlockState to,
                                                           final BlockPos diff,
                                                           final CallbackInfoReturnable<Boolean> cir) {
        if (IFlangedCogWheel.isFlanged(from) || IFlangedCogWheel.isFlanged(to))
            cir.setReturnValue(false);
    }

    @WrapOperation(method = "getRotationSpeedModifier",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/simpleRelays/ICogWheel;isSmallCog(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean bnb$flangedCogwheelsNeverMesh(final BlockState state,
                                                         final Operation<Boolean> original,
                                                         @Local(argsOnly = true, ordinal = 0) final KineticBlockEntity from,
                                                         @Local(argsOnly = true, ordinal = 1) final KineticBlockEntity to) {
        if (IFlangedCogWheel.isFlanged(from.getBlockState()) && IFlangedCogWheel.isFlanged(to.getBlockState()))
            return false;
        return original.call(state);
    }
}
