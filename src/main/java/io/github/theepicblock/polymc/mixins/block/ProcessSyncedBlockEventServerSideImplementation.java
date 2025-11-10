/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
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
package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Synced block events are called on the server, but executed on the client.
 * But with PolyMc the client often doesn't have enough information to process this event.
 * This code let's users of PolyMc make certain blocks be calculated on the server instead of the client.
 * See: config; misc.processSyncedBlockEventServerSide
 */
@Mixin(ServerLevel.class)
public class ProcessSyncedBlockEventServerSideImplementation {
    @Unique
    private final List<Block> serverCalculatedBlockEvents = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initInject(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, LevelStem levelStem, boolean bl, long l, List list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        List<String> serverCalculatedBlockEventsAsString = ConfigManager.getConfig().misc.getProcessSyncedBlockEventServerSide();
        for (String s : serverCalculatedBlockEventsAsString) {
            Block e = BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(s));
            serverCalculatedBlockEvents.add(e);
        }
    }

    @Inject(method = "blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V", at = @At("HEAD"), cancellable = true)
    public void addSyncedBlockEventInject(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
        //if the events for this block should be processed serverside, execute it immediately. Instead of adding it to a queue to be sent to the client.
        if (serverCalculatedBlockEvents.contains(block)) {
            ((ServerLevel)(Object)this).getBlockState(pos).triggerEvent(((ServerLevel)(Object)this), pos, type, data);
            ci.cancel();
        }
    }
}
