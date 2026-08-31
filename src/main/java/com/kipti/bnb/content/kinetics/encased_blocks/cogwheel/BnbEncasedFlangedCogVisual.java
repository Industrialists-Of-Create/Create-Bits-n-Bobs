package com.kipti.bnb.content.kinetics.encased_blocks.cogwheel;

import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialVisualSupport;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BnbEncasedFlangedCogVisual extends KineticBlockEntityVisual<KineticBlockEntity> implements CogwheelMaterialVisualSupport {

    private final boolean large;

    protected RotatingInstance rotatingModel;
    @Nullable
    protected final RotatingInstance rotatingTopShaft;
    @Nullable
    protected final RotatingInstance rotatingBottomShaft;

    protected CogwheelMaterialVisualSupport.State materialState = new State();

    public static BnbEncasedFlangedCogVisual small(final VisualizationContext modelManager, final KineticBlockEntity blockEntity, final float partialTick) {
        return new BnbEncasedFlangedCogVisual(modelManager, blockEntity, false, partialTick, Models.partial(BnbPartialModels.ENCASED_FLANGED_COGWHEEL_BLOCK));
    }

    public static BnbEncasedFlangedCogVisual large(final VisualizationContext modelManager, final KineticBlockEntity blockEntity, final float partialTick) {
        return new BnbEncasedFlangedCogVisual(modelManager, blockEntity, true, partialTick, Models.partial(BnbPartialModels.ENCASED_LARGE_FLANGED_COGWHEEL_BLOCK));
    }

    public BnbEncasedFlangedCogVisual(final VisualizationContext modelManager, final KineticBlockEntity blockEntity, final boolean large, final float partialTick, final Model model) {
        super(modelManager, blockEntity, partialTick);
        this.large = large;

        this.rotatingModel = this.bnb$materialInstancer(this.instancerProvider(),
                        AllInstanceTypes.ROTATING,
                        model,
                        Direction.UP,
                        Direction.fromAxisAndDirection(this.blockState.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE),
                        this.materialState,
                        this.blockEntity,
                        this.blockState)
                .createInstance();

        this.rotatingModel.setup(blockEntity)
                .setPosition(this.getVisualPosition())
                .rotateToFace(this.rotationAxis())
                .setChanged();

        RotatingInstance rotatingTopShaft = null;
        RotatingInstance rotatingBottomShaft = null;

        final Block block = this.blockState.getBlock();
        if (block instanceof final IRotate def) {
            for (final Direction d : Iterate.directionsInAxis(this.rotationAxis())) {
                if (!def.hasShaftTowards(blockEntity.getLevel(), blockEntity.getBlockPos(), this.blockState, d))
                    continue;
                final RotatingInstance instance = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                        .createInstance();
                instance.setup(blockEntity)
                        .setPosition(this.getVisualPosition())
                        .rotateToFace(Direction.SOUTH, d)
                        .setChanged();

                if (large) {
                    instance.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(this.rotationAxis(), this.pos));
                }

                if (d.getAxisDirection() == AxisDirection.POSITIVE) {
                    rotatingTopShaft = instance;
                } else {
                    rotatingBottomShaft = instance;
                }
            }
        }

        this.rotatingTopShaft = rotatingTopShaft;
        this.rotatingBottomShaft = rotatingBottomShaft;
    }

    @Override
    public void update(final float pt) {
        final RotatingInstance updated = this.bnb$updateCog(this.materialState, this.blockEntity, this.blockState, this.instancerProvider(), this.getVisualPosition(), this.rotatingModel);
        if (updated != null) {
            this.rotatingModel = updated;
            this.relight(this.rotatingModel);
        }
        this.rotatingModel.setup(this.blockEntity)
                .setChanged();
        if (this.rotatingTopShaft != null) this.rotatingTopShaft.setup(this.blockEntity)
                .setChanged();
        if (this.rotatingBottomShaft != null) this.rotatingBottomShaft.setup(this.blockEntity)
                .setChanged();
    }

    @Override
    public void updateLight(final float partialTick) {
        this.relight(this.rotatingModel, this.rotatingTopShaft, this.rotatingBottomShaft);
    }

    @Override
    protected void _delete() {
        this.rotatingModel.delete();
        if (this.rotatingTopShaft != null) this.rotatingTopShaft.delete();
        if (this.rotatingBottomShaft != null) this.rotatingBottomShaft.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        consumer.accept(this.rotatingModel);
        consumer.accept(this.rotatingTopShaft);
        consumer.accept(this.rotatingBottomShaft);
    }
}
