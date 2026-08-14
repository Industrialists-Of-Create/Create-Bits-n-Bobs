package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableBlockItemHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTankItem.class, remap = false)
public class FluidTankItemMixin {

    @Inject(method = "place", at = @At("HEAD"))
    private void bnb$savePendingDyeColor(final BlockPlaceContext ctx,
                                         final CallbackInfoReturnable<InteractionResult> cir) {
        DyeableBlockItemHelper.beginPlacement(
                ctx,
                DyeableTankBehaviour.TYPE,
                direction -> direction.getAxis().isVertical()
        );
    }

    @Inject(
            method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
                    shift = At.Shift.AFTER)
    )
    private void bnb$applyEarlyClientColor(final BlockPlaceContext ctx,
                                           final CallbackInfoReturnable<InteractionResult> cir) {
        DyeableBlockItemHelper.applyEarlyClientColor(ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$applyDyeAfterPlacement(final BlockPlaceContext ctx,
                                            final CallbackInfoReturnable<InteractionResult> cir) {
        DyeableBlockItemHelper.endPlacement(ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE);
    }

    @Redirect(
            method = "tryMultiPlace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
            remap = false
    )
    private InteractionResult bnb$wrapSubPlacement(final BlockItem instance, final BlockPlaceContext context) {
        return DyeableBlockItemHelper.wrapSubPlacement(instance, context, DyeableTankBehaviour.TYPE);
    }

    @Inject(method = "updateCustomBlockEntityTag", at = @At("RETURN"))
    private void bnb$applyDyeToBlockEntity(
            final BlockPos pos, final Level level, @Nullable final Player player,
            final ItemStack stack, final BlockState state,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        DyeableBlockItemHelper.applyDyeToBlockEntity(level, pos, level.isClientSide(), player, DyeableTankBehaviour.TYPE);
    }

}
