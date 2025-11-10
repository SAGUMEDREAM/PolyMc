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

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BlockResyncManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla clients do client-side prediction when placing and removing blocks.
 * These predictions are wrong.
 * This mixin allows PolyMc to resync the client using {@link BlockResyncManager}
 */
@Mixin(ServerPlayerGameMode.class)
public class ResyncImplementation {
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Util.isPolyMapVanillaLike(player)) {
            BlockResyncManager.onBlockUpdate(null, pos, level, player, null);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private void onBlockPlace(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (Util.isPolyMapVanillaLike(player) && stack.getItem() instanceof BlockItem) {
            BlockResyncManager.onBlockUpdate(null, hitResult.getBlockPos().relative(hitResult.getDirection()), world, player, null);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ItemUsedOnLocationTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void onBlockUse(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (Util.isPolyMapVanillaLike(player)) {
            BlockResyncManager.onBlockUpdate(null, hitResult.getBlockPos(), world, player, null);
        }
    }
}
