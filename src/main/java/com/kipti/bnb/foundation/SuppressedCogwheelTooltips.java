package com.kipti.bnb.foundation;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber
@IncludeLangDefaults({
        @LangDefault(key = "tooltip.bits_n_bobs.suppressed_cogwheel", value = "This wooden cogwheel is being hidden due to Bits 'n' Bobs' config")
})
public class SuppressedCogwheelTooltips {

    @SubscribeEvent
    public static void onTooltip(final ItemTooltipEvent event) {
        for (final BnbSuppression suppression : BnbSuppression.values()) {
            if (suppression.isSuppressed(event.getItemStack())) {
                event.getToolTip().add(Component.translatable(suppression.tooltipTranslationKey()).withStyle(ChatFormatting.RED));
                return;
            }
        }
    }

}
