package io.brennerofhell.fasttradingvx.config;

import net.minecraft.world.item.ItemStack;

public enum TradeBlockBehavior {
    DAMAGEABLE, UNSTACKABLE, DISABLED;

    public boolean isBlocked(ItemStack stack) {
        return switch (this) {
            case DAMAGEABLE -> stack.isDamageableItem();
            case UNSTACKABLE -> !stack.isStackable();
            case DISABLED -> false;
        };
    }
}
