package com.kipti.bnb.content.decoration.dyeable;

import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DyeableMultiblockTypes {

    private static final List<BehaviourType<? extends BaseDyeableBehaviour>> TYPES = new ArrayList<>();

    static {
        TYPES.add(DyeableTankBehaviour.TYPE);
    }

    public static void register(final BehaviourType<? extends BaseDyeableBehaviour> type) {
        TYPES.add(type);
    }

    @Nullable
    public static BaseDyeableBehaviour get(final Level level, final BlockPos pos) {
        for (final BehaviourType<? extends BaseDyeableBehaviour> type : TYPES) {
            final BaseDyeableBehaviour behaviour = BlockEntityBehaviour.get(level, pos, type);
            if (behaviour != null) {
                return behaviour;
            }
        }
        return null;
    }

}
