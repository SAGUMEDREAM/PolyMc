package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityWizardInfo implements WizardInfo {
    protected final Entity source;

    public EntityWizardInfo(Entity source) {
        this.source = source;
    }

    @Override
    public @NotNull Vec3 getPosition() {
        return source.position();
    }

    @Override
    public @NotNull Vec3 getPosition(UpdateInfo info) {
        return source.getPosition(info.getTickDelta());
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable BlockState getBlockState() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable ServerLevel getWorld() {
        return (ServerLevel)source.level();
    }
}
