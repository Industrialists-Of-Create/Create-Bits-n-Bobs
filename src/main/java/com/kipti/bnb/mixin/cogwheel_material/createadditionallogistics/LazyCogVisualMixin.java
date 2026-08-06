package com.kipti.bnb.mixin.cogwheel_material.createadditionallogistics;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialRenderer;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisual;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.khloeleclair.create.additionallogistics.client.content.kinetics.lazy.LazyCogVisual;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the lazy cogwheel's rotating cog model with a material-keyed one while a cogwheel material is applied.
 * Only the first (cog) instancer call in the constructor is swapped; the shaft halves are left untouched.
 */
@Mixin(LazyCogVisual.class)
public abstract class LazyCogVisualMixin extends KineticBlockEntityVisual<KineticBlockEntity> {

    @Mutable
    @Shadow
    @Final
    protected RotatingInstance rotatingModel;

    @Unique
    protected BlockState bnb$lastMaterial;

    public LazyCogVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", ordinal = 0, target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialCog(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original) {
        final BlockState material = SuperBlockEntityBehaviour.get(this.blockEntity, CogwheelMaterialBehaviour.TYPE).material;
        this.bnb$lastMaterial = material;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(this.blockState);
        if (variant == null)
            return original.call(provider, type, model);

        return original.call(
                provider, type, CogwheelMaterialVisual.MODEL_CACHE.get(
                        new CogwheelMaterialVisual.ModelKey(variant, material)
                )
        );
    }

    @Inject(method = "update", at = @At("HEAD"))
    public void bnb$update(final float pt, final CallbackInfo ci) {
        final BlockState material = SuperBlockEntityBehaviour.get(this.blockEntity, CogwheelMaterialBehaviour.TYPE).material;
        if (this.bnb$lastMaterial == material)
            return;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(this.blockState);
        if (variant == null)
            return;

        this.bnb$lastMaterial = material;
        this.rotatingModel.delete();
        this.rotatingModel = this.instancerProvider().instancer(
                        AllInstanceTypes.ROTATING, CogwheelMaterialVisual.MODEL_CACHE.get(
                                new CogwheelMaterialVisual.ModelKey(variant, material)
                        )
                )
                .createInstance();
        this.rotatingModel.setup(this.blockEntity)
                .setPosition(this.getVisualPosition())
                .rotateToFace(this.rotationAxis());

        this.rotatingModel.setChanged();
        this.relight(this.rotatingModel);
    }

}
