package com.kipti.bnb.mixin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditionally applies mixins that target classes from optional mods, so that Bits 'n' Bobs keeps working when
 * those mods are not installed. Because {@code shouldApplyMixin} is called once per declared target, a single
 * multi-target mixin can mix optional-mod classes in with regular ones as long as each optional target is gated
 * here (see {@code CogwheelMaterialVisualMixin} which also targets a Create: Additional Logistics visual).
 */
public class BnbMixinPlugin implements IMixinConfigPlugin {

    private static final String COMPAT_PACKAGE = "com.kipti.bnb.mixin.compat";
    private static final String[] COMPAT_MOD_IDS = {
            "create_connected", "createadditionallogistics", "createcasing"
    };

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (mixinClassName.startsWith(COMPAT_PACKAGE)) {
            final String compatPackageSubpath = mixinClassName.substring(COMPAT_PACKAGE.length() + 1);
            for (final String modId : COMPAT_MOD_IDS) {
                if (compatPackageSubpath.contains(modId)) {
                    return isModLoaded(modId);
                }
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    private static boolean isModLoaded(final String modId) {
        final LoadingModList modList = FMLLoader.getLoadingModList();
        return modList != null && modList.getModFileById(modId) != null;
    }

}
