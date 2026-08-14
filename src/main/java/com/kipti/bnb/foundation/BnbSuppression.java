package com.kipti.bnb.foundation;

import com.kipti.bnb.registry.core.BnbConfigs;
import com.kipti.bnb.registry.core.BnbTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public enum BnbSuppression {

    EXTERNAL_WOODEN_COGWHEELS(
            () -> BnbConfigs.common().SUPPRESS_EXTERNAL_WOODEN_COGWHEELS.get(),
            BnbTags.BnbItemTags.SUPPRESSIBLE_COGWHEELS,
            "tooltip.bits_n_bobs.suppressed_cogwheel",
            () -> ModList.get().isLoaded("createcasing"),
            "Suppression of external wooden cogwheels is enabled. This may cause some items from Create: Encased to be hidden from creative tabs and recipe viewers."
    );

    private final Supplier<Boolean> enabled;
    private final BnbTags.BnbItemTags tag;
    private final String tooltipTranslationKey;
    private final BooleanSupplier targetModLoaded;
    private final String warning;

    BnbSuppression(final Supplier<Boolean> enabled, final BnbTags.BnbItemTags tag, final String tooltipTranslationKey,
                   final BooleanSupplier targetModLoaded, final String warning) {
        this.enabled = enabled;
        this.tag = tag;
        this.tooltipTranslationKey = tooltipTranslationKey;
        this.targetModLoaded = targetModLoaded;
        this.warning = warning;
        SuppressionFilters.ITEM_FILTERS.add(this::isSuppressed);
    }

    public boolean isEnabled() {
        return this.enabled.get();
    }

    public boolean isSuppressed(final ItemStack stack) {
        return isEnabled() && this.tag.matches(stack);
    }

    public boolean isSuppressed(final Item item) {
        return isEnabled() && this.tag.matches(item);
    }

    public boolean isTargetModLoaded() {
        return this.targetModLoaded.getAsBoolean();
    }

    public String tooltipTranslationKey() {
        return this.tooltipTranslationKey;
    }

    public String warning() {
        return this.warning;
    }

    public static void warnIfSuppressing(final Logger logger) {
        for (final BnbSuppression suppression : values()) {
            if (suppression.isEnabled() && suppression.isTargetModLoaded()) {
                logger.warn(suppression.warning());
            }
        }
    }

}
