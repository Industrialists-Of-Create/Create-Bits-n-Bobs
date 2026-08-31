package com.kipti.bnb.content.kinetics.encased_blocks.cogwheel;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;

import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BnbEncasedFlangedCogRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity> {

    private final boolean large;

    public static BnbEncasedFlangedCogRenderer small(final BlockEntityRendererProvider.Context context) {
        return new BnbEncasedFlangedCogRenderer(context, false);
    }

    public static BnbEncasedFlangedCogRenderer large(final BlockEntityRendererProvider.Context context) {
        return new BnbEncasedFlangedCogRenderer(context, true);
    }

    public BnbEncasedFlangedCogRenderer(final BlockEntityRendererProvider.Context context, final boolean large) {
        super(context);
        this.large = large;
    }

    @Override
    protected void renderSafe(final SimpleKineticBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        final BlockState blockState = be.getBlockState();
        final Block block = blockState.getBlock();
        if (!(block instanceof final IRotate def))
            return;

        final Axis axis = getRotationAxisOf(be);
        final BlockPos pos = be.getBlockPos();
        final float angle = this.large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, axis)
                : getAngleForBe(be, pos, axis);

        for (final Direction d : Iterate.directionsInAxis(getRotationAxisOf(be))) {
            if (!def.hasShaftTowards(be.getLevel(), be.getBlockPos(), blockState, d))
                continue;
            final SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), d);
            kineticRotationTransform(shaft, be, axis, angle, light);
            shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final SimpleKineticBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacingVertical(
                this.large ? BnbPartialModels.ENCASED_LARGE_FLANGED_COGWHEEL_BLOCK : BnbPartialModels.ENCASED_FLANGED_COGWHEEL_BLOCK, state,
                Direction.fromAxisAndDirection(state.getValue(EncasedCogwheelBlock.AXIS), AxisDirection.POSITIVE));
    }

}
