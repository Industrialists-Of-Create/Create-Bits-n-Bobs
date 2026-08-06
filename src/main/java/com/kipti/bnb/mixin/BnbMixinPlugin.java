package com.kipti.bnb.mixin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conditionally applies mixins that target classes from optional mods, so that Bits 'n' Bobs keeps working when
 * those mods are not installed.
 */
public class BnbMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, String> MIXIN_MODS = Map.of(
            "com.kipti.bnb.mixin.cogwheel_material.create_connected.CrankWheelVisualMixin", "create_connected",
            "com.kipti.bnb.mixin.cogwheel_material.createadditionallogistics.LazyCogVisualMixin", "createadditionallogistics",
            "com.kipti.bnb.mixin.cogwheel_material.createadditionallogistics.PackageAcceleratorVisualMixin", "createadditionallogistics"
    );

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        final String modId = MIXIN_MODS.get(mixinClassName);
        if (modId == null)
            return true;
        return isModLoaded(modId);
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final org.objectweb.asm.tree.ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final org.objectweb.asm.tree.ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    private static boolean isModLoaded(final String modId) {
        try {
            final ModFileInfo modFile = FMLLoader.getLoadingModList()
                    .getModFileById(modId);
            if (modFile != null)
                return true;
        } catch (final Throwable ignored) {
        }
        try {
            return net.neoforged.fml.ModList.get()
                    .isLoaded(modId);
        } catch (final Throwable ignored) {
            return false;
        }
    }

}
