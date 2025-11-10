package nl.theepicblock.polymc.testmod.automated;

import io.netty.channel.ChannelFutureListener;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;

public class FakeNetworkHandler extends ServerGamePacketListenerImpl {
    public ArrayList<Packet<?>> sentPackets = new ArrayList<>();
    private final ProtocolInfo<?> state;

    public FakeNetworkHandler(MinecraftServer server, ServerPlayer player) {
        super(server, new FakeClientConnection(), player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
        try {
            var field = this.getClass().getField("chunkSender");
            field.setAccessible(true);
            field.set(this, new FakeDataSender(true));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        state = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess()));
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener) {
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (var packet2 : bundle.subPackets()) {
                this.send(packet2, channelFutureListener);
            }
            return;
        }

        // reserialize packet
        this.sentPackets.add(reencode(packet));
    }

    public <T extends Packet<?>> T reencode(T packet) {
        if (packet instanceof ClientboundBundlePacket) {
            throw new IllegalArgumentException("Can't reencode bundles as of now");
        }

        var bytebuf = PacketByteBufs.create();
        // TODO not use internal stuff here
        PacketContext.runWithContext(connection, this, packet, () -> {
            state.codec().encode(bytebuf, (Packet<? super PacketListener>)packet);
        });
        var reconstructedPacket = state.codec().decode(bytebuf);

        return (T)reconstructedPacket;
    }

    private static final class FakeClientConnection extends Connection {
        private FakeClientConnection() {
            super(PacketFlow.CLIENTBOUND);
        }
    }

    private static final class FakeDataSender extends PlayerChunkSender {
        public FakeDataSender(boolean local) {
            super(local);
        }

        public boolean isInNextBatch(long chunkPos) {
            return false;
        }
    }
}
