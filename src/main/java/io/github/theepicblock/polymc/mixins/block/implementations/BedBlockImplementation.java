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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * In the {@link BedBlock#playerWillDestroy(Level, BlockPos, BlockState, Player)} method, there is a call to create a WorldEvent for the breakage.
 */
@Mixin(BedBlock.class)
public class BedBlockImplementation {
    /**
     * Removes the call to {@link Level#levelEvent(Player, int, BlockPos, int)} so it can be replaced
     * @see #worldEventReplacement(Level, BlockPos, BlockState, Player, CallbackInfo, BedPart, BlockPos, BlockState)
     */
    @Redirect(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"))
    public void worldEventDisabler(Level instance, Entity entity, int i, BlockPos pos, int i2) {
        //Disabled
    }

    /**
     * Replaces the call to {@link Level#levelEvent(Player, int, BlockPos, int)} with a call to {@link PacketReplacementUtil#syncWorldEvent(Level, Player, int, BlockPos, BlockState)}
     * to respect different PolyMaps
     */
    @Inject(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"))
    public void worldEventReplacement(Level world, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir, @Local(ordinal = 1) BlockPos bedPos, @Local(ordinal = 1) BlockState bedState) {
        PacketReplacementUtil.syncWorldEvent(world, player, 2001, bedPos, bedState);
    }
}
