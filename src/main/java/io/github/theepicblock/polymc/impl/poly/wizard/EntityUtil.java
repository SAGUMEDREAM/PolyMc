package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import java.util.ArrayList;
import java.util.List;

public class EntityUtil {
    public static int getNewEntityId() {
        return EntityAccessor.getEntityIdCounter().incrementAndGet();
    }

    public static ClientboundTeleportEntityPacket createEntityPositionPacket(
            int id, double x, double y, double z, byte yaw, byte pitch, boolean onGround) {
        if (UnsafeEntityUtil.UNSAFE != null) {
            try {
                return UnsafeEntityUtil.createEntityPositionPacketUnsafe(id, x, y, z, yaw, pitch, onGround);
            } catch (InstantiationException | IllegalAccessException e) {
                PolyMc.LOGGER.warn("Exception whilst creating entity position packet. Attempting to recover");
                e.printStackTrace();
            }
        }

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeVarInt(id);
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
        byteBuf.writeByte(yaw);
        byteBuf.writeByte(pitch);
        byteBuf.writeBoolean(onGround);

        return ClientboundTeleportEntityPacket.STREAM_CODEC.decode(byteBuf);
    }

    public static ClientboundSetEntityMotionPacket createEntityVelocityUpdate(int id, int x, int y, int z) {
        if (UnsafeEntityUtil.UNSAFE != null) {
            try {
                return UnsafeEntityUtil.createEntityVelocityUpdateUnsafe(id, x, y, z);
            } catch (InstantiationException | IllegalAccessException e) {
                PolyMc.LOGGER.warn("Exception whilst creating entity velocity packet. Attempting to recover");
                e.printStackTrace();
            }
        }

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeVarInt(id);
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
        byteBuf.writeShort(z);

        return ClientboundSetEntityMotionPacket.STREAM_CODEC.decode(byteBuf);
    }

    public static <T> ClientboundSetEntityDataPacket createDataTrackerUpdate(int id, EntityDataAccessor<T> tracker, T value) {
        List<SynchedEntityData.DataValue<?>> list = new ArrayList<>(1);
        list.add(SynchedEntityData.DataValue.create(tracker, value));

        return new ClientboundSetEntityDataPacket(id, list);
    }

    public static ClientboundSetEntityDataPacket createDataTrackerUpdate(int id, List<SynchedEntityData.DataItem<?>> customEntries) {
        List<SynchedEntityData.DataValue<?>> list = new ArrayList<>(customEntries.size());
        for (var entry : customEntries) {
            list.add(entry.value());
        }
        return new ClientboundSetEntityDataPacket(id, list);
    }
}
