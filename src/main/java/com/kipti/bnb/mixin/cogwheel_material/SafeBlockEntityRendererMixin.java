package com.kipti.bnb.mixin.cogwheel_material;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialBehaviour;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialContext;
import com.kipti.bnb.content.decoration.cogwheel_material.CogwheelMaterialRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Pushes the {@link CogwheelMaterialBehaviour} material for the duration of a {@code SafeBlockEntityRenderer.render}
 * call. This is the single funnel every Create-ecosystem renderer goes through for both world and contraption block
 * entities (the dispatcher and {@code BlockEntityRenderHelper} both call {@code renderer.render}).
 */
@Mixin(SafeBlockEntityRenderer.class)
public abstract class SafeBlockEntityRendererMixin {

    @WrapMethod(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    private void bnb$pushMaterialContext(
            final BlockEntity be,
            final float partialTicks,
            final PoseStack ms,
            final MultiBufferSource bufferSource,
            final int light,
            final int overlay,
            final Operation<Void> original
    ) {
        final BlockState material = bnb$getMaterialIfApplicable(be);
        if (material == null) {
            original.call(be, partialTicks, ms, bufferSource, light, overlay);
            return;
        }

        CogwheelMaterialContext.CURRENT_RENDER_MATERIAL.set(material);
        try {
            original.call(be, partialTicks, ms, bufferSource, light, overlay);
        } finally {
            CogwheelMaterialContext.CURRENT_RENDER_MATERIAL.remove();
        }
    }

    @Unique
    private static @Nullable BlockState bnb$getMaterialIfApplicable(final BlockEntity be) {
        final CogwheelMaterialBehaviour behaviour = SuperBlockEntityBehaviour.get(be, CogwheelMaterialBehaviour.TYPE);
        if (behaviour == null)
            return null;

        final BlockState material = behaviour.material;
        if (material == null || material.is(Blocks.SPRUCE_PLANKS))
            return null;

        if (CogwheelMaterialRenderer.getVariant(be.getBlockState()) == null)
            return null;

        return material;
    }

}
