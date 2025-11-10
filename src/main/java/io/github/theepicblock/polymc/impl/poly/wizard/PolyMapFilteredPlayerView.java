package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class PolyMapFilteredPlayerView extends AbstractPacketConsumer {
    private final List<ServerPlayer> allPlayers;
    private final PolyMap filter;

    public PolyMapFilteredPlayerView(List<ServerPlayer> allPlayers, PolyMap filter) {
        this.allPlayers = allPlayers;
        this.filter = filter;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        for (ServerPlayer player : allPlayers) {
            if (PolyMapProvider.getPolyMap(player) == filter) {
                player.connection.send(packet);
            }
        }
    }

    public static List<ServerPlayer> getAll(ServerLevel world, BlockPos pos) {
        return getAll(world, new ChunkPos(pos));
    }

    public static List<ServerPlayer> getAll(ServerLevel world, ChunkPos pos) {
        return world.getChunkSource().chunkMap.getPlayers(pos, false);
    }
}
