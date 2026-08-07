package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.IFlangedCogWheel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = KineticBlockEntityVisual.class, remap = false)
public class KineticBlockEntityVisualMixin {

    @WrapOperation(method = "rotationOffset",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/simpleRelays/ICogWheel;isLargeCog(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean bnb$noCogwheelOffsetForFlanged(final BlockState state, final Operation<Boolean> original) {
        if (state.getBlock() instanceof IFlangedCogWheel)
            return false;
        return original.call(state);
    }
}
