package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class AuditMixin  {
    @GameTest()
    public void testMixin(GameTestHelper ctx) {
        MixinEnvironment.getCurrentEnvironment().audit();
        ctx.succeed();
    }
}
