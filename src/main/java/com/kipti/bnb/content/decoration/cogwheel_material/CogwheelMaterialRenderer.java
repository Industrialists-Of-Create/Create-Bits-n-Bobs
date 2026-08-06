package com.kipti.bnb.content.decoration.cogwheel_material;

import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedModelHelper;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.catnip.render.StitchedSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CogwheelMaterialRenderer {

    public static final StitchedSprite SPRUCE_PLANKS_TEMPLATE = new StitchedSprite(ResourceLocation.withDefaultNamespace("block/spruce_planks"));
    public static final StitchedSprite STRIPPED_SPRUCE_LOG_TEMPLATE = new StitchedSprite(ResourceLocation.withDefaultNamespace("block/stripped_spruce_log"));
    public static final StitchedSprite STRIPPED_SPRUCE_LOG_TOP_TEMPLATE = new StitchedSprite(ResourceLocation.withDefaultNamespace("block/stripped_spruce_log_top"));

    public static BakedModel generateModel(final CogwheelMaterialRenderer.Variant variant, final BlockState material) {
        return generateModel(variant.model(), material);
    }

    public static BakedModel generateModel(final BakedModel template, final BlockState planksBlockState) {
        final Block planksBlock = planksBlockState.getBlock();
        final ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        final String wood = plankStateToWoodName(planksBlockState);

        if (wood == null)
            return BakedModelHelper.generateModel(template, sprite -> null);

        final String namespace = id.getNamespace();
        final BlockState strippedLogBlockState = getStrippedLogBlockState(namespace, wood);

        final Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
        map.put(SPRUCE_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
        map.put(STRIPPED_SPRUCE_LOG_TEMPLATE.get(), getSpriteOnSide(strippedLogBlockState, Direction.SOUTH));
        map.put(STRIPPED_SPRUCE_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(strippedLogBlockState, Direction.UP));

        return BakedModelHelper.generateModel(template, map::get);
    }

    private static TextureAtlasSprite getSpriteOnSide(final BlockState state, final Direction side) {
        final BakedModel model = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(state);
        if (model == null)
            return null;
        final RandomSource random = RandomSource.create();
        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(state, side, random, ModelData.EMPTY, null);
        if (!quads.isEmpty()) {
            return quads.get(0)
                    .getSprite();
        }
        random.setSeed(42L);
        quads = model.getQuads(state, null, random, ModelData.EMPTY, null);
        if (!quads.isEmpty()) {
            for (final BakedQuad quad : quads) {
                if (quad.getDirection() == side) {
                    return quad.getSprite();
                }
            }
        }
        return model.getParticleIcon(ModelData.EMPTY);
    }

    private static final String[] STRIPPED_LOG_LOCATIONS = new String[]{

            "stripped_x_log", "stripped_x_stem", "stripped_x_block", // Covers most wood types
            "wood/stripped_log/x" // TerraFirmaCraft

    };

    private static BlockState getStrippedLogBlockState(final String namespace, final String wood) {
        for (final String location : STRIPPED_LOG_LOCATIONS) {
            final Optional<BlockState> state =
                    BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, location.replace("x", wood))))
                            .map(Holder::value)
                            .map(Block::defaultBlockState);
            if (state.isPresent())
                return state.get();
        }
        return Blocks.OAK_LOG.defaultBlockState();
    }

    @Nullable
    private static String plankStateToWoodName(final BlockState planksBlockState) {
        final ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlockState.getBlock());
        final String path = id.getPath();

        if (path.endsWith("_planks"))
            return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);

        if (path.contains("wood/planks/"))
            return path.substring(12);

        return null;
    }

    public static Variant getVariant(final BlockState blockState) {
        return BnbTags.BnbBlockTags.COGWHEEL_MATERIAL_COGWHEEL_MODEL.matches(blockState) ? Variant.COGWHEEL :
                BnbTags.BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_COGWHEEL_MODEL.matches(blockState) ? Variant.SHAFTLESS_COGWHEEL :
                        BnbTags.BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_LARGE_COGWHEEL_MODEL.matches(blockState) ? Variant.SHAFTLESS_LARGE_COGWHEEL :
                                null;
    }

    public static void init() {
    }

    public enum Variant {
        COGWHEEL(AllPartialModels.COGWHEEL),
        SHAFTLESS_COGWHEEL(AllPartialModels.SHAFTLESS_COGWHEEL),
        SHAFTLESS_LARGE_COGWHEEL(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL);

        private final PartialModel partial;

        Variant(final PartialModel partial) {
            this.partial = partial;
        }

        public BakedModel model() {
            return this.partial.get();
        }

        public PartialModel partialModel() {
            return this.partial;
        }
    }

}
