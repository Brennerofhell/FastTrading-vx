package io.brennerofhell.fasttradingvx.duck;

import net.minecraft.world.item.trading.MerchantOffer;

public interface MerchantScreenHooks {
    State fasttradingvx$computeState();

    MerchantOffer fasttradingvx$getCurrentTradeOffer();

    boolean fasttradingvx$isCurrentTradeOfferBlocked();

    void fasttradingvx$autofillSellSlots();

    void fasttradingvx$performTrade();

    void fasttradingvx$clearSellSlots();

    enum State {
        CAN_PERFORM, CLOSED, NO_SELECTION, OUT_OF_STOCK, NOT_ENOUGH_BUY_ITEMS, NO_ROOM_FOR_SELL_ITEM
    }
}
