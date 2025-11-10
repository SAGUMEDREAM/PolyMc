package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlacedWizardInfo implements WizardInfo {
    private final Vec3 position;
    private final BlockPos blockPos;
    private final ServerLevel world;

    public PlacedWizardInfo(BlockPos blockPos, ServerLevel world) {
        this.blockPos = blockPos;
        this.world = world;
        this.position = Vec3.atLowerCornerOf(blockPos).add(0.5, 0, 0.5);
    }

    @Override
    public @NotNull Vec3 getPosition() {
        return this.position;
    }

    @Override
    public @NotNull Vec3 getPosition(UpdateInfo info) {
        return this.position;
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return this.world.getBlockState(this.getBlockPos());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return this.world.getBlockEntity(this.getBlockPos());
    }

    @Override
    public @Nullable ServerLevel getWorld() {
        return this.world;
    }
}
