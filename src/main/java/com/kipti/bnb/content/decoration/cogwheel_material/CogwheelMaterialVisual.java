package com.kipti.bnb.content.decoration.cogwheel_material;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public class CogwheelMaterialVisual {

    public static final RendererReloadCache<CogwheelMaterialVisual.ModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(CogwheelMaterialVisual::createModel);

    private static Model createModel(final CogwheelMaterialVisual.ModelKey key) {
        final BakedModel model = CogwheelMaterialRenderer.generateModel(key.variant(), key.material());
        return new BakedModelBuilder(model)
                .build();
    }

    public record ModelKey(CogwheelMaterialRenderer.Variant variant, BlockState material) {

    }

}
