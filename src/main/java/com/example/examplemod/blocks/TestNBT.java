package com.example.examplemod.blocks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.HolderLookup;

public class TestNBT {
    public void test(ServerLevel level) {
        HolderLookup.Provider p = level.registryAccess();
    }
}
