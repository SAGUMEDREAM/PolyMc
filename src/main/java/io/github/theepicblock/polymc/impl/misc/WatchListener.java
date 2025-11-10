package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.server.level.ServerPlayer;

public interface WatchListener {
    void polymc$addPlayer(ServerPlayer playerEntity);

    void polymc$removePlayer(ServerPlayer playerEntity);

    void polymc$removeAllPlayers();
}
