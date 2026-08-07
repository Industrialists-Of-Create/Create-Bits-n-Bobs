package com.kipti.bnb.mixin.compat.createcasing;

import com.kipti.bnb.foundation.CogwheelSuppression;
import fr.iglee42.createcasing.transmissions.TransmissionSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents Create: Encased from converting cogwheels into its own wooden cogwheels while the suppression config is
 * enabled, so the interaction falls through to Bits 'n' Bobs' own cogwheel material behaviour instead. Only
 * conversions whose result is a suppressed (tagged) cogwheel are blocked; shafts and untagged sets are unaffected.
 */
@Mixin(targets = "fr.iglee42.createcasing.utils.ItemChangeBlockManager")
public abstract class ItemChangeBlockManagerMixin {

    @Shadow
    private static TransmissionSet getSetForItem(final Item item) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    private static boolean isCogwheel(final BlockState state) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    private static boolean isLargeCogwheel(final BlockState state) {
        throw new UnsupportedOperationException();
    }

    @Inject(method = "onRightClick", at = @At("HEAD"), cancellable = true)
    private static void bnb$suppressWoodenCogwheelConversion(final PlayerInteractEvent.RightClickBlock event, final CallbackInfo ci) {
        if (!CogwheelSuppression.isEnabled()) {
            return;
        }
        final TransmissionSet set = ItemChangeBlockManagerMixin.getSetForItem(event.getItemStack().getItem());
        if (set == null) {
            return;
        }
        final BlockState state = event.getLevel().getBlockState(event.getPos());
        if (ItemChangeBlockManagerMixin.isCogwheel(state) && set.getCogwheel() != null
                && CogwheelSuppression.isSuppressed(set.getCogwheel().asItem())) {
            ci.cancel();
            return;
        }
        if (ItemChangeBlockManagerMixin.isLargeCogwheel(state) && set.getLargeCogwheel() != null
                && CogwheelSuppression.isSuppressed(set.getLargeCogwheel().asItem())) {
            ci.cancel();
        }
    }

}
