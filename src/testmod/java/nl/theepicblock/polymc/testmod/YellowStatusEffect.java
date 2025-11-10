package nl.theepicblock.polymc.testmod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class YellowStatusEffect extends MobEffect {
    public static int YELLOW = 0xfff4e42c;
    /**
     * Helpful for automatic testing, to simulate the fact that
     * this status effect will not be a registered one on the client
     */
    public static boolean SIMULATE_UNAVAILABLE = false;

    protected YellowStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public int getColor() {
        return SIMULATE_UNAVAILABLE ? 0 : super.getColor();
    }

    @Override
    public String getDescriptionId() {
        return SIMULATE_UNAVAILABLE ? "translation.unavailable" : super.getDescriptionId();
    }
}
