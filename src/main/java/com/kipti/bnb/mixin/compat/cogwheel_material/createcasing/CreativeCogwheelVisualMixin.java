package com.kipti.bnb.mixin.compat.cogwheel_material.createcasing;

import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisualSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import fr.iglee42.createcasing.blockEntities.visuals.CreativeCogwheelVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeCogwheelVisual.class)
public abstract class CreativeCogwheelVisualMixin extends AbstractBlockEntityVisual<KineticBlockEntity> implements CogwheelMaterialVisualSupport {

    @Mutable
    @Shadow
    @Final
    protected RotatingInstance rotatingModel;

    @Unique
    private State bnb$materialState;

    protected CreativeCogwheelVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialBase(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original) {
        if (this.bnb$materialState == null)
            this.bnb$materialState = new State();
        return this.bnb$materialInstancer(provider,
                type,
                model,
                Direction.UP,
                Direction.fromAxisAndDirection(this.blockState.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE),
                this.bnb$materialState,
                this.blockEntity,
                this.blockState);
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
