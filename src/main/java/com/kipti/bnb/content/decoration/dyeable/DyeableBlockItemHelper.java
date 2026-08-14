package com.kipti.bnb.content.decoration.dyeable;

import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class DyeableBlockItemHelper {

    private static final ThreadLocal<DyeColor> PLACEMENT_DYE = new ThreadLocal<>();

    @Nullable
    public static DyeColor getOffhandDyeColor(final BlockPlaceContext context) {
        return DyeableBlockItemHelper.getOffhandDyeColor(context.getPlayer());
    }

    @Nullable
    public static DyeColor getOffhandDyeColor(@Nullable final Player player) {
        if (player == null) {
            return null;
        }
        final ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    public static void beginPlacement(
            final BlockPlaceContext ctx,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            final Predicate<Direction> copyColorFaces
    ) {
        final DyeColor dye = getDyeOffhandOrSurface(ctx, type, copyColorFaces);
        PLACEMENT_DYE.set(dye);

        if (dye != null) {
            savePendingPlacementColor(ctx, ctx.getClickedPos(), dye);
        }
    }

    public static void applyEarlyClientColor(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type
    ) {
        applyColorClientOnly(level, pos, type, PLACEMENT_DYE.get());
    }

    public static void endPlacement(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type
    ) {
        final DyeColor dye = PLACEMENT_DYE.get();
        PLACEMENT_DYE.remove();

        try {
            applyColorClientOnly(level, pos, type, dye);
        } finally {
            if (dye != null) {
                consumePendingPlacementColor(level, pos);
            }
        }
    }

    public static InteractionResult wrapSubPlacement(
            final BlockItem instance,
            final BlockPlaceContext context,
            final BehaviourType<? extends BaseDyeableBehaviour> type
    ) {
        final DyeColor dye = PLACEMENT_DYE.get();
        if (dye != null) {
            savePendingPlacementColor(context, context.getClickedPos(), dye);
        }
        try {
            final InteractionResult result = instance.place(context);
            applyColorClientOnly(context.getLevel(), context.getClickedPos(), type, dye);
            return result;
        } finally {
            if (dye != null) {
                consumePendingPlacementColor(context.getLevel(), context.getClickedPos());
            }
        }
    }

    public static void applyDyeToBlockEntity(
            final Level level,
            final BlockPos pos,
            final boolean isClientSide,
            @Nullable final Player player,
            final BehaviourType<? extends BaseDyeableBehaviour> type
    ) {
        if (isClientSide || player == null) {
            return;
        }
        final DyeColor dye = PLACEMENT_DYE.get();
        if (dye != null) {
            setColor(level, pos, type, dye);
        }
    }

    @Nullable
    private static DyeColor getDyeOffhandOrSurface(
            final BlockPlaceContext ctx,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            final Predicate<Direction> copyColorFaces
    ) {
        DyeColor dye = getOffhandDyeColor(ctx);

        if (copyColorFaces.test(ctx.getClickedFace())) {
            final BlockPos placedOnPos = ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite());
            final BaseDyeableBehaviour surfaceBehaviour = BlockEntityBehaviour.get(ctx.getLevel(), placedOnPos, type);
            if (surfaceBehaviour != null && surfaceBehaviour.getColor() != null) {
                dye = surfaceBehaviour.getColor();
            }
        }
        return dye;
    }

    public static void savePendingPlacementColor(
            final BlockPlaceContext context,
            final BlockPos pos,
            @Nullable final DyeColor color
    ) {
        if (color == null) {
            return;
        }
        BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(context.getPlayer());
        DyeableTransitionHelper.savePendingPlacementColor(context.getLevel(), pos, color);
    }

    public static void consumePendingPlacementColor(final Level level, final BlockPos pos) {
        DyeableTransitionHelper.consumePendingPlacementColor(level, pos);
    }

    public static void applyColorClientOnly(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            @Nullable final DyeColor color
    ) {
        if (color == null || !level.isClientSide) {
            return;
        }
        final BaseDyeableBehaviour behaviour = BlockEntityBehaviour.get(level, pos, type);
        if (behaviour != null) {
            behaviour.applyColorClientOnly(color);
        }
    }

    public static void setColor(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            @Nullable final DyeColor color
    ) {
        if (level.isClientSide) {
            return;
        }
        final BaseDyeableBehaviour behaviour = BlockEntityBehaviour.get(level, pos, type);
        if (behaviour != null) {
            behaviour.setColor(color);
        }
    }

}
