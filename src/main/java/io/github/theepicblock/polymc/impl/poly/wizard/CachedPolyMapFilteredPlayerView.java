package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public class CachedPolyMapFilteredPlayerView extends AbstractPacketConsumer {
    private final List<ServerPlayer> players;

    public CachedPolyMapFilteredPlayerView(List<ServerPlayer> allPlayers, PolyMap filter) {
        players = new ArrayList<>();
        allPlayers.forEach(player -> {
            if (PolyMapProvider.getPolyMap(player) == filter) {
                players.add(player);
            }
        });
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        for (var player : players) {
            player.connection.send(packet);
        }
    }
}
