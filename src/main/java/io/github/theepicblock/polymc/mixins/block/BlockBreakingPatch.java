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

import io.github.theepicblock.polymc.impl.misc.BlockBreakingUtil;
import io.github.theepicblock.polymc.impl.mixin.BlockBreakingDuck;
import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla-like clients usually process block breaking client-side.
 * These mixins give vanilla-like clients mining fatigue and reimplement the block breaking server-side.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class BlockBreakingPatch implements BlockBreakingDuck {
    @Shadow @Final protected ServerPlayer player;
    @Shadow private int gameTicks;
    @Shadow private int destroyProgressStart;
    @Shadow protected ServerLevel level;
    @Shadow private int lastSentState;

    @Unique
    private int blockBreakingCooldown;
    @Unique
    private boolean isBreakingServerside = false;

    @Shadow
    public abstract void destroyAndAck(BlockPos pos, int sequence, String reason);

    /**
     * This breaks the block serverside if the client hasn't broken it already
     */
    @Inject(method = "incrementDestroyProgress", at = @At("TAIL"))
    public void breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, state)) {
            int j = gameTicks - i;
            float f = state.getDestroyProgress(this.player, this.player.level(), pos) * (float)(j);

            if (blockBreakingCooldown > 0) {
                --blockBreakingCooldown;
            }

            if (f >= 1.0F) {
                blockBreakingCooldown = 5;
                player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, -1));
                destroyAndAck(pos, 0, "destroyed");
            }
        }
    }

    @Inject(method = "incrementDestroyProgress", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, shift = At.Shift.AFTER, target = "Lnet/minecraft/server/level/ServerPlayerGameMode;lastSentState:I"))
    public void onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, state)) {
            //Send a packet that resembles the current mining progress
            player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, this.lastSentState));
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    public void packetReceivedInject(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, level.getBlockState(pos))) {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                // This prevents the client from trying to break the block themselves.
                if (this.level.getBlockState(pos).getDestroyProgress(this.player, this.player.level(), pos) < 1) {
                    disableClientBreaking();
                }
            } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                enableClientBreaking();
                player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, -1));
            }
        } else if (isBreakingServerside) {
            enableClientBreaking();
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("TAIL"))
    public void enforceBlockBreakingCooldown(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, level.getBlockState(pos))) {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                this.destroyProgressStart += blockBreakingCooldown;
            }
        } else if (isBreakingServerside) {
            enableClientBreaking();
        }
    }

    @Inject(method = "destroyAndAck", at = @At("HEAD"))
    private void clearEffects(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        if (isBreakingServerside) {
            enableClientBreaking();
        }
    }

    @Unique
    private void disableClientBreaking() {
        isBreakingServerside = true;
        // Make sure it's resynced
        this.player.getAttributes().getAttributesToSync().add(this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED));
    }

    @Unique
    private void enableClientBreaking() {
        isBreakingServerside = false;
        BlockBreakingUtil.removeBreakDisabler(this.player);
    }

    @Override
    public boolean polymc$isBreakingServerside() {
        return this.isBreakingServerside;
    }
}