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

import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Redirects the {@link BlockState} in Block#spawnBreakParticles to send the particles for the polyd {@link BlockState} instead.
 */
@Mixin(Block.class)
public class BreakParticleImplementation {
    /**
     * Replaces the call to {@link Level#levelEvent(Player, int, BlockPos, int)} with a call to {@link PacketReplacementUtil#syncWorldEvent(Level, Player, int, BlockPos, BlockState)}
     * to respect different PolyMaps
     */
    @ModifyVariable(method = "spawnDestroyParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"), argsOnly = true)
    public Player onBreakParticlePacket(@Nullable Player player, Level world, @Nullable Player player2, BlockPos pos, BlockState state) {
        if (player instanceof ServerPlayer spe) {
            var needsCustomBreaking = CustomBlockBreakingCheck.needsCustomBreaking(spe, state);
            return needsCustomBreaking ? null : player;
        }
        return player;
    }
}
