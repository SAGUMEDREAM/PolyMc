package nl.theepicblock.polymc.testmod;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TestFlyingWaxedWeatheredCutCopperStairs extends ThrowableItemProjectile implements ItemSupplier {
    public TestFlyingWaxedWeatheredCutCopperStairs(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.WAXED_WEATHERED_CUT_COPPER_STAIRS;
    }
}
