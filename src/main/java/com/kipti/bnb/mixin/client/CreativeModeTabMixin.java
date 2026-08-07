package com.kipti.bnb.mixin.client;

import com.kipti.bnb.foundation.CogwheelSuppression;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Set;

/**
 * Hides suppressed cogwheel items from creative tabs while {@link CogwheelSuppression} is enabled. The removal on
 * {@code buildContents} keeps rebuilt tabs and search trees clean, while the {@code getDisplayItems} filter applies
 * the change to already-built tabs immediately when the config is toggled in-game.
 */
@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Shadow
    private Collection<ItemStack> displayItems;

    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Inject(method = "buildContents", at = @At("TAIL"))
    private void bnb$removeSuppressedFromTab(final CreativeModeTab.ItemDisplayParameters parameters, final CallbackInfo ci) {
        if (!CogwheelSuppression.isEnabled()) {
            return;
        }
        this.displayItems.removeIf(CogwheelSuppression::isSuppressed);
        this.displayItemsSearchTab.removeIf(CogwheelSuppression::isSuppressed);
    }

    @Inject(method = "getDisplayItems", at = @At("HEAD"), cancellable = true)
    private void bnb$filterSuppressedFromTab(final CallbackInfoReturnable<Collection<ItemStack>> cir) {
        if (!CogwheelSuppression.isEnabled()) {
            return;
        }
        cir.setReturnValue(this.displayItems.stream()
                .filter(stack -> !CogwheelSuppression.isSuppressed(stack))
                .toList());
    }

}
