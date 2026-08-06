package com.kipti.bnb.mixin.cogwheel_material.createadditionallogistics;

import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisualSupport;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisualSupport.State;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.khloeleclair.create.additionallogistics.client.content.logistics.packageAccelerator.PackageAcceleratorVisual;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the package accelerator's rotating cog model with a material-keyed one while a cogwheel material is applied.
 * Only the first (cog) instancer call in the constructor is swapped; the shaft half is left untouched.
 * The cog instance lives in a field named {@code cog} rather than {@code rotatingModel}, so this visual can't join
 * {@link com.kipti.bnb.mixin.cogwheel_material.CogwheelMaterialVisualMixin} and gets its own leaf mixin instead.
 */
@Mixin(PackageAcceleratorVisual.class)
public abstract class PackageAcceleratorVisualMixin extends AbstractBlockEntityVisual<KineticBlockEntity> implements CogwheelMaterialVisualSupport {

    @Mutable
    @Shadow
    @Final
    protected RotatingInstance cog;

    @Unique
    protected State bnb$materialState;

    protected PackageAcceleratorVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Ldev/khloeleclair/create/additionallogistics/common/content/logistics/packageAccelerator/PackageAcceleratorBlockEntity;F)V", at = @At(value = "INVOKE", ordinal = 0, target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialCog(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original) {
        if (this.bnb$materialState == null)
            this.bnb$materialState = new State();
        return this.bnb$materialInstancer(provider, type, model, original, null, null, this.bnb$materialState, this.blockEntity, this.blockState);
    }

    @Inject(method = "update", at = @At("HEAD"))
    public void bnb$update(final float pt, final CallbackInfo ci) {
        final RotatingInstance updated = this.bnb$updateCog(this.bnb$materialState, this.blockEntity, this.blockState, this.instancerProvider(), this.getVisualPosition(), this.cog);
        if (updated != null) {
            this.cog = updated;
            this.relight(this.cog);
        }
    }

}
