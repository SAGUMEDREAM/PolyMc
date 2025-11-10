package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import io.github.theepicblock.polymc.mixins.entity.EntityTrackerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import java.util.List;
import java.util.Map;

public class RegularWizardUpdater {
    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(RegularWizardUpdater::tick);
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            PacketCountManager.INSTANCE.adjust(server.getTickCount());
        });
    }

    private static void tick(ServerLevel world) {
        var tick = world.getServer().getTickCount();
        var updateInfo = new ThreadedWizardUpdater.UpdateInfoImpl(tick, 1); // Called at the end of the tick, therefore delta is 1
        var packetCountManager = PacketCountManager.INSTANCE;
        packetCountManager.updateWatchRadius(((TACSAccessor)world.getChunkSource().chunkMap).getServerViewDistance());
        var entityTrackers = ((TACSAccessor)world.getChunkSource().chunkMap).getEntityMap();
        int seed = 0;
        for (var tracker : entityTrackers.values()) {
            var trackerEntry = (EntityTrackerEntryDuck)((EntityTrackerAccessor)tracker).getServerEntity();
            var wizards = trackerEntry.polymc$getWizards();
            var listeners = ((EntityTrackerAccessor)tracker).getSeenBy();
            for (Map.Entry<PolyMap,Wizard> entry : wizards.entrySet()) {
                var polyMap = entry.getKey();
                var wizard = entry.getValue();
                if (wizard == null) continue;
                var playerView = packetCountManager.getView(listeners, polyMap, ((EntityTrackerAccessor)tracker).getEntity().trackingPosition(), tick, seed++);
                wizard.update(playerView, updateInfo);
                playerView.sendBatched();
            }
        }

        for (Map.Entry<PolyMap,Map<ChunkPos,List<Wizard>>> e : ((WizardTickerDuck)world).polymc$getBlockTickers().entrySet()) {
            var polyMap = e.getKey();
            var wizardsPerPos = e.getValue();
            for (Map.Entry<ChunkPos,List<Wizard>> entry : wizardsPerPos.entrySet()) {
                var pos = entry.getKey();
                var wizards = entry.getValue();
                var playerView = packetCountManager.getView(world, pos, polyMap, tick, seed++);
                wizards.forEach(wizard -> {
                    wizard.update(playerView, updateInfo);
                    wizard.onTick(playerView);
                });
                playerView.sendBatched();
            }
        }
    }
}
