package com.example.examplemod.blocks;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

public class BlockEventHandler {

    // 1. Block Placement Prevention (Flag Block Limit & Land Block Protection)
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        BlockState placedBlockState = event.getPlacedBlock();
        Block block = placedBlockState.getBlock();
        BlockPos pos = event.getPos();
        UUID playerUuid = player.getUUID();

        // A. Land Block Protection Check
        PlacedBlocksData.PlacedBlockInfo protectingLand = PlacedBlocksData.getProtectingLandBlock(pos, playerUuid);
        if (protectingLand != null) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c[보호] 다른 플레이어의 랜드블럭 보호 영역 내에는 블럭을 설치할 수 없습니다!"));
            return;
        }

        // B. Flag Block Count Limit Check
        boolean isFlagBlock = (block == ExampleMod.FLAG_BLOCK.get());
        if (isFlagBlock) {
            long flagCount = PlacedBlocksData.countBlocksForPlayer(playerUuid, "Flag Block");
            if (flagCount >= 4) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c[제한] 플래그블럭은 최대 4개까지만 설치할 수 있습니다!"));
                return;
            }
        }

        // C. Record Placement
        boolean isLandBlock = (block == ExampleMod.LAND_BLOCK.get());
        if (isLandBlock || isFlagBlock) {
            String blockName = isLandBlock ? "Land Block" : "Flag Block";
            PlacedBlocksData.addBlock(blockName, pos, playerUuid);

            String message = String.format("§a[설치 완료] §e%s§f 가 [X:%d, Y:%d, Z:%d] 위치에 설치되었습니다. (소유자 UUID: %s)",
                    blockName, pos.getX(), pos.getY(), pos.getZ(), playerUuid.toString());
            player.sendSystemMessage(Component.literal(message));

            ExampleMod.LOGGER.info("[Block Placed] Name: {}, Pos: {}, Owner: {} (UUID: {})",
                    blockName, pos.toShortString(), player.getName().getString(), playerUuid);
        }
    }

    // 2. Block Breaking Protection (Ownership & Protection Zones)
    @SubscribeEvent
    public void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        BlockPos pos = event.getPos();
        UUID playerUuid = player.getUUID();

        // A. If the block being broken is a registered Land/Flag Block, verify ownership
        PlacedBlocksData.PlacedBlockInfo placedBlock = PlacedBlocksData.getBlockAt(pos);
        if (placedBlock != null) {
            if (!placedBlock.playerUuid().equals(playerUuid)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c[보호] 이 블럭의 소유자만 부술 수 있습니다!"));
                return;
            } else {
                // If it is the owner, remove it from our data registry
                PlacedBlocksData.removeBlockAt(pos);
                player.sendSystemMessage(Component.literal(String.format("§e[제거 완료] §f본인의 %s 을(를) 성공적으로 제거했습니다.", placedBlock.blockName())));
                return;
            }
        }

        // B. Check if breaking normal blocks inside another player's Land Block territory
        PlacedBlocksData.PlacedBlockInfo protectingLand = PlacedBlocksData.getProtectingLandBlock(pos, playerUuid);
        if (protectingLand != null) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c[보호] 다른 플레이어의 랜드블럭 보호 영역 내의 블럭은 파괴할 수 없습니다!"));
        }
    }

    // 3. Block Interaction Protection (Left Click & Right Click Block)
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null) return;

        BlockPos pos = event.getPos();
        UUID playerUuid = player.getUUID();

        PlacedBlocksData.PlacedBlockInfo protectingLand = PlacedBlocksData.getProtectingLandBlock(pos, playerUuid);
        if (protectingLand != null) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c[보호] 다른 플레이어의 랜드블럭 보호 영역 내의 블럭과 상호작용할 수 없습니다!"));
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player == null) return;

        BlockPos pos = event.getPos();
        UUID playerUuid = player.getUUID();

        PlacedBlocksData.PlacedBlockInfo protectingLand = PlacedBlocksData.getProtectingLandBlock(pos, playerUuid);
        if (protectingLand != null) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c[보호] 다른 플레이어의 랜드블럭 보호 영역 내의 블럭과 상호작용할 수 없습니다!"));
        }
    }

    // 4. Territory Entry Protection (Push out unauthorized players)
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        // Run check on server-side to prevent client hacking and ensure consistent teleportation
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos playerBlockPos = player.blockPosition();
            UUID playerUuid = player.getUUID();

            PlacedBlocksData.PlacedBlockInfo protectingLand = PlacedBlocksData.getProtectingLandBlock(playerBlockPos, playerUuid);
            if (protectingLand != null) {
                // Reject entry! Push player out of the 16x16x16 protective region.
                BlockPos landPos = protectingLand.pos();
                Vec3 center = new Vec3(landPos.getX() + 0.5, landPos.getY() + 0.5, landPos.getZ() + 0.5);
                Vec3 playerPos = player.position();
                Vec3 dir = playerPos.subtract(center);

                double distance = dir.horizontalDistance();
                if (distance < 0.01) {
                    dir = new Vec3(1, 0, 0);
                    distance = 1.0;
                }

                // Push vector: Normalize the direction and set target coordinate outside the boundary (radius 9.5 to be safe)
                Vec3 pushDir = dir.multiply(1.0 / distance, 0, 1.0 / distance);
                double targetX = center.x + pushDir.x * 9.5;
                double targetZ = center.z + pushDir.z * 9.5;

                // Teleport player back outside the protective zone boundary
                player.teleportTo(targetX, player.getY(), targetZ);
                player.sendSystemMessage(Component.literal("§c[보호] 다른 플레이어의 랜드블럭 영역에 진입할 수 없습니다!"));
            }
        }
    }
}
