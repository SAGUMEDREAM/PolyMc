package io.github.theepicblock.polymc.impl.poly.wizard;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FallingBlockWizardInfo extends EntityWizardInfo {

    public FallingBlockWizardInfo(FallingBlockEntity entity) {
        super(entity);
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return ((FallingBlockEntity)source).getBlockState();
    }
}
