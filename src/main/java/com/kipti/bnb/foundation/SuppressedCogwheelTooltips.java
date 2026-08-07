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
        if (!CogwheelSuppression.isSuppressed(event.getItemStack())) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.bits_n_bobs.suppressed_cogwheel").withStyle(ChatFormatting.RED));
    }

}
