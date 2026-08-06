package com.kipti.bnb.mixin.cogwheel_material.create_connected;

import com.hlysine.create_connected.content.crankwheel.CrankWheelVisual;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the rotating crank-wheel base with a material-keyed cogwheel model while a cogwheel material is applied.
 * The crank handle ({@code TRANSFORMED} instance, created first in the constructor) is left untouched, so the second
 * instancer call ({@code ordinal = 1}) is the one being wrapped.
 * <p>
 * Unlike the other visuals its constructor takes no {@code Model} parameter (the cog base is created after the
 * {@code TRANSFORMED} crank handle) and it rotates to the block's {@code FACING} instead of an axis, so it gets its
 * own leaf mixin rather than joining {@link com.kipti.bnb.mixin.cogwheel_material.CogwheelMaterialVisualMixin}.
 */
@Mixin(CrankWheelVisual.class)
public abstract class CrankWheelVisualMixin extends AbstractBlockEntityVisual<KineticBlockEntity> implements CogwheelMaterialVisualSupport {

    @Mutable
    @Shadow
    @Final
    private RotatingInstance rotatingModel;

    @Unique
    private State bnb$materialState;

    protected CrankWheelVisualMixin(final VisualizationContext context, final KineticBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @WrapOperation(method = "<init>(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Lcom/hlysine/create_connected/content/crankwheel/CrankWheelBlockEntity;F)V", at = @At(value = "INVOKE", ordinal = 1, target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"))
    public Instancer<RotatingInstance> bnb$materialBase(final InstancerProvider provider, final InstanceType<RotatingInstance> type, final Model model, final Operation<Instancer<RotatingInstance>> original) {
        if (this.bnb$materialState == null)
            this.bnb$materialState = new State();
        return this.bnb$materialInstancer(provider, type, model, original, Direction.UP, this.blockState.getValue(BlockStateProperties.FACING), this.bnb$materialState, this.blockEntity, this.blockState);
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
