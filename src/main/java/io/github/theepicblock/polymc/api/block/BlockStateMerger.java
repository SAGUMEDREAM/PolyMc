package io.github.theepicblock.polymc.api.block;

import java.util.function.BiFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * It's recommended to read the comments inside the code of {@link io.github.theepicblock.polymc.impl.poly.block.FunctionBlockStatePoly#FunctionBlockStatePoly(Block, BiFunction, BlockStateMerger)}, which is where the merger will be used.
 */
@FunctionalInterface
public interface BlockStateMerger {
    BlockStateMerger DEFAULT = new PropertyMerger<>(BlockStateProperties.STAGE)
            .combine(new PropertyMerger<>(BlockStateProperties.DISTANCE))
            .combine(new PropertyMerger<>(BlockStateProperties.STABILITY_DISTANCE))
            .combine(new PropertyMerger<>(BlockStateProperties.AGE_15))
            .combine(new PropertyMerger<>(BlockStateProperties.POWERED, state -> state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock))
            .combine(new PropertyMerger<>(BlockStateProperties.TRIGGERED))
            .combine(new PropertyMerger<>(BlockStateProperties.PERSISTENT))
            .combine(new PropertyMerger<>(BlockStateProperties.NOTE))
            .combine(new PropertyMerger<>(BlockStateProperties.NOTEBLOCK_INSTRUMENT))
            .combine((state) -> {
                if (state.hasProperty(BlockStateProperties.MOISTURE)) {
                    // Moisture lower than 7 are the same
                    if (state.getValue(BlockStateProperties.MOISTURE) < 7) {
                        return state.setValue(BlockStateProperties.MOISTURE, 0);
                    }
                }
                return state;
            }).combine((state) -> {
                if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    // Waterlogged double slabs do not need to exist
                    if (state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE && state.getValue(BlockStateProperties.WATERLOGGED)) {
                        return state.setValue(BlockStateProperties.WATERLOGGED, false);
                    }
                }
                return state;
            });
    BlockStateMerger ALL = (a) -> {
        for (var property : a.getProperties()) {
            a = normalizeProperty(a, property);
        }
        return a;
    };

    BlockState normalize(BlockState b);

    default BlockStateMerger combine(BlockStateMerger other) {
        return (a) -> this.normalize(other.normalize(a));
    }

    static <T extends Comparable<T>> BlockState normalizeProperty(BlockState state, Property<T> property) {
        return state.setValue(property, property.getPossibleValues().iterator().next());
    }
}
