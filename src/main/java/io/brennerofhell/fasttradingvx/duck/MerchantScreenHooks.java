package io.brennerofhell.fasttradingvx.duck;

import net.minecraft.world.item.trading.MerchantOffer;

public interface MerchantScreenHooks {
    int fasttradingvx$getSelectedIndex();

    int fasttradingvx$getPinnedIndex();

    void fasttradingvx$togglePin();

    State fasttradingvx$computeState(int index);

    MerchantOffer fasttradingvx$getTradeOffer(int index);

    boolean fasttradingvx$isTradeOfferBlocked(int index);

    void fasttradingvx$autofillSellSlots(int index);

    void fasttradingvx$performTrade();

    void fasttradingvx$clearSellSlots();

    enum State {
        CAN_PERFORM, CLOSED, NO_SELECTION, OUT_OF_STOCK, NOT_ENOUGH_BUY_ITEMS, NO_ROOM_FOR_SELL_ITEM
    }
}
