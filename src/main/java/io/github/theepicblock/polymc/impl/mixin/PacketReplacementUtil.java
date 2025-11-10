/*
 * PolyMc
 * Copyright (C) 2020-2021 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.impl.Util;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PacketReplacementUtil {
    public static void syncWorldEvent(Level world, Player exception, int eventId, BlockPos pos, BlockState data) {
        if (world.getServer() != null) {
            sendToAround(world.getServer().getPlayerList(), exception, pos.getX(), pos.getY(), pos.getZ(), 64, world.dimension(), (playerEntity) -> {
                playerEntity.connection.send(new ClientboundLevelEventPacket(eventId, pos, Util.getPolydRawIdFromState(data, playerEntity), false));
            });
        }
    }

    public static void sendToAround(PlayerList manager, Player exception, double x, double y, double z, double distance, ResourceKey<Level> worldKey, Consumer<ServerPlayer> consumer) {
        for (int i = 0; i < manager.getPlayers().size(); ++i) {
            ServerPlayer serverPlayerEntity = manager.getPlayers().get(i);
            if (serverPlayerEntity != exception && serverPlayerEntity.level().dimension() == worldKey) {
                double d = x - serverPlayerEntity.getX();
                double e = y - serverPlayerEntity.getY();
                double f = z - serverPlayerEntity.getZ();
                if (d * d + e * e + f * f < distance * distance) {
                    consumer.accept(serverPlayerEntity);
                }
            }
        }
    }
}
