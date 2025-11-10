package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

/**
 * Represents nothing more then an entity id. You can instruct packets to be sent with this id.
 * No other state about the entity is stored
 */
public interface VirtualEntity {
    EntityType<?> getEntityType();

    int getId();

    void spawn(PacketConsumer player, Vec3 pos);

    void remove(PacketConsumer player);
}
