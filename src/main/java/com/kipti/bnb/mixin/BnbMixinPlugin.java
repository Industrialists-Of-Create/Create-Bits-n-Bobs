package com.kipti.bnb.mixin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conditionally applies mixins that target classes from optional mods, so that Bits 'n' Bobs keeps working when
 * those mods are not installed. Because {@code shouldApplyMixin} is called once per declared target, a single
 * multi-target mixin can mix optional-mod classes in with regular ones as long as each optional target is gated
 * here (see {@code CogwheelMaterialVisualMixin} which also targets a Create: Additional Logistics visual).
 */
public class BnbMixinPlugin implements IMixinConfigPlugin {

    /**
     * Optional-mod target classes, keyed by mixin class and then by target class name, mapping each to the mod id
     * it comes from. Targets not listed here are always applied.
     */
    private static final Map<String, Map<String, String>> OPTIONAL_TARGETS = Map.of(
            "com.kipti.bnb.mixin.cogwheel_material.CogwheelMaterialVisualMixin", Map.of(
                    "dev.khloeleclair.create.additionallogistics.client.content.kinetics.lazy.LazyCogVisual", "createadditionallogistics"),
            "com.kipti.bnb.mixin.cogwheel_material.createadditionallogistics.PackageAcceleratorVisualMixin", Map.of(
                    "dev.khloeleclair.create.additionallogistics.client.content.logistics.packageAccelerator.PackageAcceleratorVisual", "createadditionallogistics"),
            "com.kipti.bnb.mixin.cogwheel_material.create_connected.CrankWheelVisualMixin", Map.of(
                    "com.hlysine.create_connected.content.crankwheel.CrankWheelVisual", "create_connected")
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
        final String modId = OPTIONAL_TARGETS.getOrDefault(mixinClassName, Map.of())
                .get(targetClassName);
        return modId == null || isModLoaded(modId);
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
