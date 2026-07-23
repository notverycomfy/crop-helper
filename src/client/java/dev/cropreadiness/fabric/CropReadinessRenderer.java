package dev.cropreadiness.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.ArrayList;
import java.util.List;

final class CropReadinessRenderer {
    private static final int RADIUS = 8;
    private static final int VERTICAL_RADIUS = 4;
    private static final int RESCAN_INTERVAL = 8;

    private static final List<Mark> MARKS = new ArrayList<>();
    private static long lastScan = Long.MIN_VALUE;
    private static BlockPos lastCenter = BlockPos.ZERO;

    private CropReadinessRenderer() {
    }

    static void render(LevelRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null || !holdingHoe(player)) {
            MARKS.clear();
            return;
        }

        BlockPos center = player.blockPosition();
        long time = level.getGameTime();
        if (time - lastScan >= RESCAN_INTERVAL || center.distManhattan(lastCenter) > 2) {
            scan(level, center);
            lastScan = time;
            lastCenter = center.immutable();
        }

        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack poseStack = context.poseStack();
        for (Mark mark : MARKS) {
            BlockState state = level.getBlockState(mark.pos);
            VoxelShape shape = state.getShape(level, mark.pos);
            if (shape.isEmpty()) {
                shape = Block.box(1, 1, 1, 15, 15, 15);
            }
            shape = Shapes.create(shape.bounds().inflate(0.003));
            poseStack.pushPose();
            poseStack.translate(
                    mark.pos.getX() - camera.x,
                    mark.pos.getY() - camera.y,
                    mark.pos.getZ() - camera.z
            );
            context.submitNodeCollector().submitShapeOutline(
                    poseStack, shape, RenderTypes.linesTranslucent(), mark.color, 1.0F, false
            );
            poseStack.popPose();
        }
    }

    private static boolean holdingHoe(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof HoeItem
                || player.getOffhandItem().getItem() instanceof HoeItem;
    }

    static boolean shouldRenderVanillaOutline(LevelRenderContext context, BlockOutlineRenderState outline) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !holdingHoe(minecraft.player)) return true;
        return classify(minecraft.level, outline.pos(), minecraft.level.getBlockState(outline.pos())) == 0;
    }

    private static void scan(ClientLevel level, BlockPos center) {
        MARKS.clear();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (x * x + z * z > RADIUS * RADIUS) continue;
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(cursor);
                    int color = classify(level, cursor, state);
                    if (color != 0) MARKS.add(new Mark(cursor.immutable(), color));
                }
            }
        }
    }

    private static int classify(ClientLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FarmlandBlock) {
            return state.getValue(FarmlandBlock.MOISTURE) == 0
                    && !level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND)
                    ? CropReadinessConfig.color(3) : 0;
        }
        if (state.getBlock() instanceof SaplingBlock) {
            return hasTreeRoom(level, pos) ? 0 : CropReadinessConfig.color(4);
        }
        if (!isCrop(state)) return 0;

        if (isMature(state)) return CropReadinessConfig.color(0);
        if (tooDarkToGrow(level, pos, state)) return CropReadinessConfig.color(2);
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof FarmlandBlock
                && below.getValue(FarmlandBlock.MOISTURE) == 0) {
            return CropReadinessConfig.color(1);
        }
        return 0;
    }

    private static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof NetherWartBlock
                || state.getBlock() instanceof CocoaBlock
                || state.getBlock() instanceof SweetBerryBushBlock;
    }

    private static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
        if (state.getBlock() instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) == 3;
        if (state.getBlock() instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) == 2;
        return state.getBlock() instanceof SweetBerryBushBlock
                && state.getValue(SweetBerryBushBlock.AGE) == 3;
    }

    private static boolean tooDarkToGrow(ClientLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock) return level.getRawBrightness(pos, 0) < 9;
        return state.getBlock() instanceof SweetBerryBushBlock
                && level.getRawBrightness(pos.above(), 0) < 9;
    }

    private static boolean hasTreeRoom(ClientLevel level, BlockPos sapling) {
        for (int y = 1; y <= 6; y++) {
            int radius = y < 3 ? 1 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockState state = level.getBlockState(sapling.offset(x, y, z));
                    if (!state.isAir() && !(state.getBlock() instanceof SaplingBlock)) return false;
                }
            }
        }
        return true;
    }

    private record Mark(BlockPos pos, int color) {
    }
}
