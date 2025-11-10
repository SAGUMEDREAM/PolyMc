package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WizardInfo {
    @NotNull Vec3 getPosition();

    @NotNull Vec3 getPosition(UpdateInfo info);

    @Nullable BlockPos getBlockPos();

    @Nullable BlockState getBlockState();

    @Nullable BlockEntity getBlockEntity();

    @Nullable ServerLevel getWorld();
}
