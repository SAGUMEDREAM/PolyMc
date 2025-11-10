package io.github.theepicblock.polymc.impl.poly.wizard;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public class SinglePlayerView extends AbstractPacketConsumer {
    private final ServerPlayer player;

    public SinglePlayerView(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        player.connection.send(packet);
    }
}
