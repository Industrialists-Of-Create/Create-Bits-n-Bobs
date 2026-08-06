package com.kipti.bnb.mixin.cogwheel_material;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialRenderer;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisual;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SingleAxisRotatingVisual.class)
public abstract class SingleAxisRotatingVisualMixin extends KineticBlockEntityVisual<KineticBlockEntity> implements SimpleTickableVisual {

    @Mutable
    @Shadow
    @Final
    protected RotatingInstance rotatingModel;

    @Unique
    protected BlockState bnb$lastMaterial;
    @Unique
    protected Direction bnb$face;

    public SingleAxisRotatingVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;FLnet/minecraft/core/Direction;Ldev/engine_room/flywheel/api/model/Model;)V", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> updated(final InstancerProvider instance, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original, @Local(argsOnly = true) final Direction from) {
        this.bnb$face = from;

        final BlockState material = SuperBlockEntityBehaviour.get(this.blockEntity, CogwheelMaterialBehaviour.TYPE).material;
        this.bnb$lastMaterial = material;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(this.blockState);
        if (variant == null)
            return original.call(instance, AllInstanceTypes.ROTATING, model);

        return original.call(
                instance, type, CogwheelMaterialVisual.MODEL_CACHE.get(
                        new CogwheelMaterialVisual.ModelKey(
                                variant,
                                material
                        )
                )
        );
    }

    @Inject(method = "update", at = @At("HEAD"))
    public void update(final float pt, final CallbackInfo ci) {
        final BlockState material = SuperBlockEntityBehaviour.get(this.blockEntity, CogwheelMaterialBehaviour.TYPE).material;
        if (this.bnb$lastMaterial == material)
            return;

        final CogwheelMaterialRenderer.Variant variant = CogwheelMaterialRenderer.getVariant(this.blockState);
        if (variant == null)
            return;

        this.bnb$lastMaterial = (material);
        this.rotatingModel.delete();
        this.rotatingModel = this.instancerProvider().instancer(
                        AllInstanceTypes.ROTATING, CogwheelMaterialVisual.MODEL_CACHE.get(
                                new CogwheelMaterialVisual.ModelKey(
                                        variant,
                                        material
                                )
                        )
                )
                .createInstance()
                .rotateToFace(this.bnb$face, this.rotationAxis())
                .setup(this.blockEntity)
                .setPosition(this.getVisualPosition());

        this.rotatingModel.setChanged();
        this.relight(this.rotatingModel);
    }

}
