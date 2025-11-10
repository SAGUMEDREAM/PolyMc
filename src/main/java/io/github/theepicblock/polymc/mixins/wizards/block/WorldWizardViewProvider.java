package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class WorldWizardViewProvider implements WizardView {
    @Shadow public abstract LevelChunk getChunk(int i, int j);

    @Override
    public PolyMapMap<Wizard> getWizards(BlockPos pos) {
        LevelChunk worldChunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return ((WizardView)worldChunk).getWizards(pos);
    }

    @Override
    public PolyMapMap<Wizard> removeWizards(BlockPos pos, boolean move) {
        LevelChunk worldChunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return ((WizardView)worldChunk).removeWizards(pos, move);
    }
}
