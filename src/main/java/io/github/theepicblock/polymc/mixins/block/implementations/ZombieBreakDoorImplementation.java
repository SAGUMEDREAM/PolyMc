package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BreakDoorGoal.class)
public abstract class ZombieBreakDoorImplementation extends DoorInteractGoal {
    public ZombieBreakDoorImplementation(Mob mob) {
        super(mob);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V", ordinal = 1))
    private void redirectWorldEvent(Level world, int eventId, BlockPos pos, int data) {
        PacketReplacementUtil.syncWorldEvent(world, null, eventId, pos, this.mob.level().getBlockState(this.doorPos));
    }
}
