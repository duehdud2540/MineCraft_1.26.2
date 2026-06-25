package com.example.examplemod.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.Level;

public class Fisherman extends Villager {
    public Fisherman(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        // 항상 어부 직업을 가지도록 데이터 설정 (어부 텍스처가 렌더링되게 만듭니다.)
        this.setVillagerData(this.getVillagerData().withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.FISHERMAN)));
    }
}
