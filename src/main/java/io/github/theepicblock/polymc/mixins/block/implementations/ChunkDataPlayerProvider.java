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
package io.github.theepicblock.polymc.mixins.block.implementations;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerChunkSender.class)
public class ChunkDataPlayerProvider {

    @WrapMethod(method = "sendChunk")
    private static void setPlayerContext(ServerGamePacketListenerImpl handler, ServerLevel world, LevelChunk chunk, Operation<Void> original) {
        PolymerCommonUtils.executeWithNetworkingLogic(handler, () -> original.call(handler, world, chunk));
    }
}
