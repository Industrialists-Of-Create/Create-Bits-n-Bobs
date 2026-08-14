package com.kipti.bnb.foundation;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Shared registry of {@link Predicate item filters} contributed by the suppression entries of Bits 'n' Bobs (and
 * dependents such as Bits 'n' Dyes). Each {@link BnbSuppression} entry registers itself here so a single consumer,
 * like the {@code CreativeModeTab} mixin, can filter all suppressed items at once.
 */
public final class SuppressionFilters {

    public static final List<Predicate<ItemStack>> ITEM_FILTERS = new ArrayList<>();

    private SuppressionFilters() {
    }

    public static boolean isSuppressed(final ItemStack stack) {
        for (final Predicate<ItemStack> filter : ITEM_FILTERS) {
            if (filter.test(stack)) {
                return true;
            }
        }
        return false;
    }

}
