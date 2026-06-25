package com.example.examplemod.blocks;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlacedBlocksData {

    // Record to store block information
    public record PlacedBlockInfo(String blockName, BlockPos pos, UUID playerUuid) {
        @Override
        public String toString() {
            return String.format("Block: %s, Position: [%d, %d, %d], Player UUID: %s",
                    blockName, pos.getX(), pos.getY(), pos.getZ(), playerUuid);
        }
    }

    // Thread-safe list to hold the placed blocks data
    private static final List<PlacedBlockInfo> PLACED_BLOCKS = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a newly placed block info to the registry.
     */
    public static void addBlock(String blockName, BlockPos pos, UUID playerUuid) {
        // Prevent duplicate entries for the same position
        removeBlockAt(pos);
        PLACED_BLOCKS.add(new PlacedBlockInfo(blockName, pos, playerUuid));
    }

    /**
     * Removes a block at the specified position.
     * Returns true if a block was removed.
     */
    public static boolean removeBlockAt(BlockPos pos) {
        return PLACED_BLOCKS.removeIf(info -> info.pos().equals(pos));
    }

    /**
     * Counts how many blocks of a certain type a player has placed.
     */
    public static long countBlocksForPlayer(UUID playerUuid, String blockName) {
        return PLACED_BLOCKS.stream()
                .filter(info -> info.playerUuid().equals(playerUuid) && info.blockName().equals(blockName))
                .count();
    }

    /**
     * Checks if a given position is within another player's Land Block protection range.
     * Protection range: 16x16x16 centered at the land block (X, Y, Z coordinates +/- 8 blocks).
     * Returns the protecting block's info if protected, otherwise null.
     */
    public static PlacedBlockInfo getProtectingLandBlock(BlockPos pos, UUID playerUuid) {
        for (PlacedBlockInfo info : PLACED_BLOCKS) {
            if ("Land Block".equals(info.blockName())) {
                // Ignore if it's the player's own Land Block
                if (info.playerUuid().equals(playerUuid)) {
                    continue;
                }

                BlockPos landPos = info.pos();
                // 16x16x16 bounding box protection check (within radius 8 blocks in all directions)
                if (Math.abs(pos.getX() - landPos.getX()) <= 8 &&
                    Math.abs(pos.getY() - landPos.getY()) <= 8 &&
                    Math.abs(pos.getZ() - landPos.getZ()) <= 8) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the specific placed block info at a position, if any exists.
     */
    public static PlacedBlockInfo getBlockAt(BlockPos pos) {
        return PLACED_BLOCKS.stream()
                .filter(info -> info.pos().equals(pos))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the list of all recorded placed blocks.
     */
    public static List<PlacedBlockInfo> getPlacedBlocks() {
        return new ArrayList<>(PLACED_BLOCKS);
    }

    /**
     * Clears all recorded blocks.
     */
    public static void clear() {
        PLACED_BLOCKS.clear();
    }
}
