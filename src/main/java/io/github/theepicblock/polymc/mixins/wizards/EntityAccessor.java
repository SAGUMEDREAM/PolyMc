package io.github.theepicblock.polymc.mixins.wizards;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("ENTITY_COUNTER")
    static AtomicInteger getEntityIdCounter() {
        throw new IllegalStateException();
    }

    @Accessor("DATA_SHARED_FLAGS_ID")
    static EntityDataAccessor<Byte> getFlagTracker() {
        throw new IllegalStateException();
    }

    @Accessor("DATA_NO_GRAVITY")
    static EntityDataAccessor<Boolean> getNoGravityTracker() {
        throw new IllegalStateException();
    }

    @Accessor("DATA_SILENT")
    static EntityDataAccessor<Boolean> getSilentTracker() {
        throw new IllegalStateException();
    }

    @Accessor("DATA_CUSTOM_NAME")
    static EntityDataAccessor<Optional<Component>> getCustomName() {
        throw new UnsupportedOperationException();
    }

    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> getNameVisible() {
        throw new UnsupportedOperationException();
    }
}
