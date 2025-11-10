package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.mixins.wizards.PistonBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PistonWizardInfo implements WizardInfo {
    private final PistonMovingBlockEntity be;

    public PistonWizardInfo(PistonMovingBlockEntity be) {
        this.be = be;
    }

    @Override
    public @NotNull Vec3 getPosition() {
        var accessor = (PistonBlockEntityAccessor)be;
        var d = accessor.callGetExtendedProgress(accessor.getProgress());

        return Vec3.atLowerCornerOf(be.getBlockPos()).add(
                0.5+d*be.getDirection().getStepX(),
                d*be.getDirection().getStepY(),
                0.5+d*be.getDirection().getStepZ());
    }

    @Override
    public @NotNull Vec3 getPosition(UpdateInfo info) {
        var accessor = (PistonBlockEntityAccessor)be;
        var d = accessor.callGetExtendedProgress(be.getProgress(info.getTickDelta()));  // TODO ensure that the progress of the piston is threadsafe

        return Vec3.atLowerCornerOf(be.getBlockPos()).add(
                0.5+d*be.getDirection().getStepX(),
                d*be.getDirection().getStepY(),
                0.5+d*be.getDirection().getStepZ());
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return be.getBlockPos();
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return be.getMovedState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public @Nullable ServerLevel getWorld() {
        return (ServerLevel)be.getLevel();
    }
}
