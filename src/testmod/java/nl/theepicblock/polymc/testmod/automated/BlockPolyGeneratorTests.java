package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.function.BiPredicate;

public class BlockPolyGeneratorTests {
    @GameTest()
    public void testDoor(GameTestHelper ctx) {
        assertPoly(
                ctx,
                Testmod.TEST_DOOR,
                (sState, cState) -> (cState.getBlock() instanceof DoorBlock && opensAfterRightClick(ctx, cState)),
                "should be a door that's openable"
        );
        assertPoly(
                ctx,
                Testmod.TEST_IRON_DOOR,
                (sState, cState) -> cState.getBlock() == Blocks.IRON_DOOR,
                "as of 1.20.1, doors that can't be opened by hand (such as Testmod.TEST_IRON_DOOR), should only be polied with minecraft:iron_door"
        );
        ctx.succeed();
    }

    @GameTest()
    public void testTrapDoor(GameTestHelper ctx) {
        assertPoly(
                ctx,
                Testmod.TEST_TRAP_DOOR,
                (sState, cState) -> (cState.getBlock() instanceof TrapDoorBlock && opensAfterRightClick(ctx, cState)),
                "should be a trap door that's openable"
        );
        assertPoly(
                ctx,
                Testmod.TEST_IRON_TRAP_DOOR,
                (sState, cState) -> cState.getBlock() == Blocks.IRON_TRAPDOOR,
                "as of 1.20.1, doors that can't be opened by hand (such as Testmod.TEST_IRON_DOOR), should only be polied with minecraft:iron_trapdoor"
        );
        ctx.succeed();
    }

    @GameTest()
    public void testSlab(GameTestHelper ctx) {
        assertPoly(
                ctx,
                Testmod.TEST_SLAB,
                (sState, cState) -> (sState.getCollisionShape(null, null).equals(cState.getCollisionShape(null, null))),
                "slab should have matching collisions"
        );
        ctx.succeed();
    }

    public static void assertPoly(GameTestHelper ctx, Block a, BiPredicate<BlockState, BlockState> check, String message) {
        var poly = TestUtil.getMap().getBlockPoly(a);
        a.getStateDefinition().getPossibleStates().forEach(serverState -> {
            var polied = poly.getClientBlock(serverState);
            ctx.assertTrue(check.test(serverState, polied), Component.literal(serverState+" didn't get polied correctly: "+message+ " but found "+polied+" instead"));
        });
    }

    public static boolean opensAfterRightClick(GameTestHelper ctx, BlockState a) {
        var startOpenedState = a.getValue(BlockStateProperties.OPEN);
        ctx.setBlock(new BlockPos(1,1,1), a);
        ctx.useBlock(new BlockPos(1,1,1));
        var endOpenedState = ctx.getBlockState(new BlockPos(1,1,1)).getValue(BlockStateProperties.OPEN);
        return startOpenedState != endOpenedState;
    }
}
