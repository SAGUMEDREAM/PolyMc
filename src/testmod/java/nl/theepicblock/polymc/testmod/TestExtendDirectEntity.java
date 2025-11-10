package nl.theepicblock.polymc.testmod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class TestExtendDirectEntity extends Creeper {
    public TestExtendDirectEntity(EntityType<? extends Creeper> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ANVIL_USE;
    }
}
