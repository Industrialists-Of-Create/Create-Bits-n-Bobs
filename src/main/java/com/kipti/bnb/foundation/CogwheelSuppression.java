package com.kipti.bnb.foundation;

import com.kipti.bnb.registry.core.BnbConfigs;
import com.kipti.bnb.registry.core.BnbTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CogwheelSuppression {

    public static boolean isEnabled() {
        return BnbConfigs.common().SUPPRESS_EXTERNAL_WOODEN_COGWHEELS.get();
    }

    public static boolean isSuppressed(final ItemStack stack) {
        return isEnabled() && BnbTags.BnbItemTags.SUPPRESSIBLE_COGWHEELS.matches(stack);
    }

    public static boolean isSuppressed(final Item item) {
        return isEnabled() && BnbTags.BnbItemTags.SUPPRESSIBLE_COGWHEELS.matches(item);
    }

}
