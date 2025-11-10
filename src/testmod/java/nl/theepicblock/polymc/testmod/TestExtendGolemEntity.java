package nl.theepicblock.polymc.testmod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;

public class TestExtendGolemEntity extends AbstractGolem {
    public TestExtendGolemEntity(EntityType<? extends AbstractGolem> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ANVIL_USE;
    }
}
