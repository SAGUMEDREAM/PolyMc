package nl.theepicblock.polymc.testmod.automated;

import com.mojang.authlib.GameProfile;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class PacketTester implements Closeable {
    public final ServerPlayer playerEntity;
    private final FakeNetworkHandler fakeNetworkHandler;
    private final GameTestHelper context;

    @SuppressWarnings("DataFlowIssue")
    public PacketTester(GameTestHelper context) {
        var world = context.getLevel();
        // Mock a player entity
        var profile = new GameProfile(UUID.randomUUID(), "Fake packet receiver");
        this.playerEntity = new ServerPlayer(world.getServer(), world, profile, ClientInformation.createDefault());
        this.playerEntity.setChunkTrackingView(ChunkTrackingView.of(new ChunkPos(context.absolutePos(BlockPos.ZERO)), 5));
        this.fakeNetworkHandler = new FakeNetworkHandler(world.getServer(), this.playerEntity);
        this.playerEntity.connection = this.fakeNetworkHandler;
        this.fakeNetworkHandler.player = this.playerEntity;
        this.context = context;
        PolyMapProvider provider = (PolyMapProvider) this.fakeNetworkHandler;

        // Since the regular fabric events don't get called on this
        PolyMapProvider.get(fakeNetworkHandler).refreshUsedPolyMap();

        world.addNewPlayer(playerEntity);
        world.getChunkSource().chunkMap.move(playerEntity);
        this.playerEntity.setPos(context.absoluteVec(Vec3.ZERO));
        world.getChunkSource().chunkMap.move(playerEntity);
    }

    public <T extends Packet<?>> T reencode(T packet) {
        return this.fakeNetworkHandler.reencode(packet);
    }

    public void setMap(PolyMap map) {
        PolyMapProvider.get(this.playerEntity).setPolyMap(map);
    }

    public void setGameMode(GameType v) {
        this.playerEntity.gameMode.changeGameModeForPlayer(v);
    }

    public void clearPackets() {
        this.fakeNetworkHandler.sentPackets.clear();
    }

    /**
     * Finds any packets of a certain type that have been sent recently. Will error if there isn't exactly one packet found.
     */
    public <T extends Packet<?>> T getFirstOfType(Class<T> packetType) {
        this.context.assertTrue(!this.fakeNetworkHandler.sentPackets.isEmpty(), Component.literal(String.format("Expected one packet of type %s, but no packet of any type has been received", packetType)));
        var packets = this.fakeNetworkHandler.sentPackets
                .stream()
                .filter(packet -> packet.getClass() == packetType)
                .map(packet -> (T)packet)
                .toList();
        this.context.assertTrue(packets.size() == 1, Component.literal(String.format("Expected one packet of type %s, found %d", packetType, packets.size())));
        return packets.get(0);
    }

    /**
     * Does the same as {@link #getFirstOfType(Class)}, but is limited to the scope of the Runnable
     */
    public <T extends Packet<?>> T capture(Class<T> packetType, Runnable run) {
        this.clearPackets();
        run.run();
        return getFirstOfType(packetType);
    }

    public List<Packet<?>> captureAll(Runnable run) {
        this.clearPackets();
        run.run();
        return new ArrayList<>(this.fakeNetworkHandler.sentPackets);
    }

    public Stream<Packet<?>> getReceived() {
        return this.fakeNetworkHandler.sentPackets.stream();
    }

    public <T extends Packet<?>> Stream<T> getReceived(Class<T> packetType) {
        return this.fakeNetworkHandler.sentPackets.stream().filter(p -> p.getClass() == packetType).map(p -> (T)p);
    }

    public void assertReceived(Packet<?> packet, String message) {
        //this.context.assertTrue(this.fakeNetworkHandler.sentPackets.contains(packet), message);
    }

    public GameTestHelper getTestContext() {
        return context;
    }

    @Override
    public void close() {
        this.playerEntity.remove(Entity.RemovalReason.KILLED);
    }
}
