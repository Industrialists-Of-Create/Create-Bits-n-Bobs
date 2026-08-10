package com.kipti.bnb.mixin.cogwheel_material;

import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisualSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { SingleAxisRotatingVisual.class, EncasedCogVisual.class})
public abstract class CogwheelMaterialVisualMixin extends AbstractBlockEntityVisual<KineticBlockEntity> implements CogwheelMaterialVisualSupport {

    @Mutable
    @Shadow(remap = false)
    @Final
    protected RotatingInstance rotatingModel;

    @Unique
    protected State bnb$materialState;

    protected CogwheelMaterialVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = {
            "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;ZFLdev/engine_room/flywheel/api/model/Model;)V",
            "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Ldev/khloeleclair/create/additionallogistics/common/content/kinetics/lazy/cog/LazyCogWheelBlockEntity;ZFLdev/engine_room/flywheel/api/model/Model;)V"
    }, require = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialCog(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original) {
        if (this.bnb$materialState == null)
            this.bnb$materialState = new State();
        return this.bnb$materialInstancer(provider, type, model, original, null, null, this.bnb$materialState, this.blockEntity, this.blockState);
    }

    @WrapOperation(method = "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;FLnet/minecraft/core/Direction;Ldev/engine_room/flywheel/api/model/Model;)V", require = 0, at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialSingleAxis(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original, @Local(argsOnly = true) final Direction from) {
        if (this.bnb$materialState == null)
            this.bnb$materialState = new State();
        return this.bnb$materialInstancer(provider, type, model, original, from, null, this.bnb$materialState, this.blockEntity, this.blockState);
    }

    @Inject(method = "update", at = @At("HEAD"))
    public void bnb$update(final float pt, final CallbackInfo ci) {
        final RotatingInstance updated = this.bnb$updateCog(this.bnb$materialState, this.blockEntity, this.blockState, this.instancerProvider(), this.getVisualPosition(), this.rotatingModel);
        if (updated != null) {
            this.rotatingModel = updated;
            this.relight(this.rotatingModel);
        }
    }

}
