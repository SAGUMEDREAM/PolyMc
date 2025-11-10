package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.NOPPolyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public class BlockTests {
    /*@CustomTestProvider
    public Collection<TestFunction> testItem() {
        var list = new ArrayList<TestFunction>();
        // Different ways in which we can test itemstacks being transformed by PolyMc
        var reserializationMethods = new HashMap<String, ReserializationMethod>();
        reserializationMethods.put("reencode", this::reencodeMethod);
//        reserializationMethods.put("place", this::placeBlockMethod); // Too flaky

        var i = 0;
        for (var isBlockVanilla : new boolean[]{true, false}) {
            for (var useNopMap : new Boolean[]{false, true}) {
                for (var method : reserializationMethods.entrySet()) {
                    var block = isBlockVanilla ? Blocks.DIRT : Testmod.TEST_BLOCK;
                    list.add(new TestFunction(
                            "blockbatch_"+i++,
                            String.format("blocktests (%s, %s, %s)", isBlockVanilla, useNopMap, method.getKey()),
                            EMPTY_STRUCTURE,
                            100,
                            0,
                            true,
                            (ctx) -> {
                                // The actual test function
                                var packetCtx = new PacketTester(ctx);
                                if (useNopMap) {
                                    packetCtx.setMap(new NOPPolyMap());
                                }

                                var originalState = block.getDefaultState();
                                method.getValue().reserialize(originalState, packetCtx, newState -> {
                                    if (isBlockVanilla || useNopMap) {
                                        TestUtil.assertEq(newState, originalState, "Block shouldn't have been transformed by PolyMc");
                                    } else {
                                        TestUtil.assertDifferent(newState, originalState, "Block should've been transformed by PolyMc");
                                    }

                                    packetCtx.close();
                                    ctx.complete();
                                });
                            }
                    ));
                }
            }
        }

        return list;
    }
*/
    public void reencodeMethod(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer) {
        newStateConsumer.accept(
                ctx.reencode(new ClientboundBlockUpdatePacket(new BlockPos(0,0,0), state)).getBlockState()
        );
    }

    public void placeBlockMethod(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer) {
        // This test actually places a block and ensures the packet comes out right on the other end
        // Might be a bit flaky though…
        ctx.getTestContext().runAfterDelay(1, () -> {
            ctx.clearPackets();
            ctx.getTestContext().setBlock(BlockPos.ZERO, state);
            ctx.getTestContext().runAfterDelay(1, () -> {
                var packet = ctx.getFirstOfType(ClientboundBlockUpdatePacket.class);
                newStateConsumer.accept(packet.getBlockState());
            });
        });
    }

    public interface ReserializationMethod {
        void reserialize(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer);
    }
}
