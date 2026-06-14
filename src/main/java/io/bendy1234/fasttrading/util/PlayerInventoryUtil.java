package io.bendy1234.fasttrading.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class PlayerInventoryUtil {
    public static boolean areItemsEqual(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static boolean listContainsStack(NonNullList<ItemStack> list, ItemStack stack) {
        if (stack.isEmpty())
            return true;
        int count = 0;
        for (ItemStack itemStack : list) {
            if (areItemsEqual(itemStack, stack))
                count += itemStack.getCount();
        }
        return count >= stack.getCount();
    }

    public static boolean playerHasStack(Inventory playerInventory, ItemStack stack) {
        return listContainsStack(playerInventory.getNonEquipmentItems(), stack);
    }

    public static boolean playerCanPerformTrade(Inventory playerInventory, MerchantOffer offer) {
        return playerHasStack(playerInventory, offer.getCostA()) && playerHasStack(playerInventory, offer.getCostB());
    }

    public static boolean playerCanAcceptStack(Inventory playerInventory, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.isStackable())
            if (playerInventory.getSlotWithRemainingSpace(stack) >= 0)
                return true;
        return playerInventory.getFreeSlot() >= 0;
    }
}
