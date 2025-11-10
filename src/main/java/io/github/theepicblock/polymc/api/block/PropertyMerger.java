package io.github.theepicblock.polymc.api.block;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class PropertyMerger<T extends Comparable<T>> implements BlockStateMerger {
    private final Predicate<BlockState> activation;
    private final Property<T> property;
    private final T defaultValue;

    public PropertyMerger(Property<T> property) {
        this(property, (state) -> true);
    }

    public PropertyMerger(Property<T> property, Predicate<BlockState> activation) {
        this.activation = activation;
        this.property = property;
        this.defaultValue = property.getPossibleValues().iterator().next();
    }

    @Override
    public BlockState normalize(BlockState state) {
        if (activation.test(state) && state.hasProperty(property)) {
            return state.setValue(property, defaultValue);
        } else {
            return state;
        }
    }
}
