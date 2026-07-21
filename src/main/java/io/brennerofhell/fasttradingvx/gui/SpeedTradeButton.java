package io.brennerofhell.fasttradingvx.gui;

import io.brennerofhell.fasttradingvx.FastTrading;
import io.brennerofhell.fasttradingvx.ModKeyBindings;
import io.brennerofhell.fasttradingvx.SpeedTradeTimer;
import io.brennerofhell.fasttradingvx.config.AutofillBehavior;
import io.brennerofhell.fasttradingvx.config.ModConfig;
import io.brennerofhell.fasttradingvx.duck.MerchantScreenHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.Locale;

import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT;
import static io.brennerofhell.fasttradingvx.ModKeyBindings.keyOverrideBlock;

public class SpeedTradeButton extends AbstractButton {

    private static final Identifier BUTTON_LOCATION = FastTrading.id("textures/gui/fasttradingvx.png");
    private static final Style STYLE_GRAY = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private final MerchantScreenHooks hooks;
    private Phase phase;
    private MerchantOffer actionTradeOffer;

    public SpeedTradeButton(int x, int y, MerchantScreenHooks hooks) {
        super(x, y, 18, 20, Component.empty());
        this.hooks = hooks;
        phase = Phase.INACTIVE;
    }

    private int targetIndex() {
        int pinned = hooks.fasttradingvx$getPinnedIndex();
        return pinned >= 0 ? pinned : hooks.fasttradingvx$getSelectedIndex();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (visible && event.button() == MOUSE_BUTTON_RIGHT && isCoordinateOverButton(event.x(), event.y())) {
            hooks.fasttradingvx$togglePin();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isCoordinateOverButton(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseY >= getY() && mouseX < getRight() && mouseY < getBottom();
    }

    // True when there's no pin, or the pinned trade is the one currently selected/shown.
    private boolean isTargetLive() {
        int pinned = hooks.fasttradingvx$getPinnedIndex();
        return pinned < 0 || pinned == hooks.fasttradingvx$getSelectedIndex();
    }

    private boolean checkPrimed() {
        int index = targetIndex();
        active = phase == Phase.INACTIVE
                && hooks.fasttradingvx$computeState(index) == MerchantScreenHooks.State.CAN_PERFORM
                && (ModKeyBindings.isDown(keyOverrideBlock) || !hooks.fasttradingvx$isTradeOfferBlocked(index));
        return active;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (checkPrimed()) {
            phase = Phase.AUTOFILL;
            actionTradeOffer = hooks.fasttradingvx$getTradeOffer(targetIndex());
            SpeedTradeTimer.start();
        }
    }

    //checks if the trade is still performable, and (unless pinned) that the player hasn't switched to another trade
    private boolean checkState() {
        boolean pinned = hooks.fasttradingvx$getPinnedIndex() >= 0;
        int index = targetIndex();
        if (hooks.fasttradingvx$computeState(index) != MerchantScreenHooks.State.CAN_PERFORM
                || (!pinned && actionTradeOffer != hooks.fasttradingvx$getTradeOffer(index))) {
            phase = Phase.INACTIVE;
            hooks.fasttradingvx$clearSellSlots();
            SpeedTradeTimer.stop();
            return false;
        }
        return true;
    }

    public void tick() {
        if (phase == Phase.INACTIVE) {
            checkPrimed();
            return;
        }
        active = false;

        if (!isTargetLive()) {
            // Pinned trade isn't what's currently shown - freeze exactly as-is (no slot
            // access, timer not consumed) so manual use of whatever the player IS looking
            // at is completely untouched. Resumes on its own once they browse back.
            return;
        }

        while (SpeedTradeTimer.shouldDoAction()) {
            if (!checkState())
                return;

            SpeedTradeTimer.onDoAction();

            if (phase == Phase.AUTOFILL) {
                hooks.fasttradingvx$autofillSellSlots(targetIndex());
                phase = Phase.TRADE;
            } else {
                hooks.fasttradingvx$performTrade();
                phase = Phase.AUTOFILL;
            }
        }
        checkState();
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int v = 36;
        if (checkPrimed()) {
            v = isHovered() ? 18 : 0;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_LOCATION, getX(), getY(), 0, v, 20, 18, 20, 54);
        applyTooltip();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    protected void applyTooltip() {
        if (!isHovered())
            return;

        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null) {
            return;
        }

        int index = targetIndex();
        boolean pinned = hooks.fasttradingvx$getPinnedIndex() >= 0;

        ArrayList<FormattedCharSequence> textList = new ArrayList<>();
        if (phase != Phase.INACTIVE) {
            textList.add(Component.translatable("fasttradingvx.tooltip.in_progress").withStyle(
                    style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.DARK_GREEN)
            ).getVisualOrderText());
            if (pinned) {
                textList.add(Component.translatable(isTargetLive() ? "fasttradingvx.tooltip.pinned" : "fasttradingvx.tooltip.pinned_paused").withStyle(
                        style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)
                ).getVisualOrderText());
            }
        } else {
            MerchantScreenHooks.State state = hooks.fasttradingvx$computeState(index);
            if (state == MerchantScreenHooks.State.CAN_PERFORM) {
                boolean isBlocked = hooks.fasttradingvx$isTradeOfferBlocked(index);
                boolean isOverriden = ModKeyBindings.isDown(keyOverrideBlock);
                if (isBlocked && !isOverriden) {
                    textList.add(Component.translatable("fasttradingvx.tooltip.cannot_perform").withStyle(
                            style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.RED)
                    ).getVisualOrderText());
                    textList.add(Component.translatable("fasttradingvx.tooltip.blocked").withStyle(
                            style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)
                    ).getVisualOrderText());
                    if (keyOverrideBlock.isUnbound()) {
                        textList.add(Component.translatable("fasttradingvx.tooltip.unblock_hint.unbound[0]",
                                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyOverrideBlock.saveString())
                                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                        textList.add(Component.translatable("fasttradingvx.tooltip.unblock_hint.unbound[1]")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    } else {
                        textList.add(Component.translatable("fasttradingvx.tooltip.unblock_hint",
                                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyOverrideBlock.saveString())
                                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    }
                } else {
                    textList.add(Component.translatable("fasttradingvx.tooltip.can_perform").withStyle(
                            style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.GREEN)
                    ).getVisualOrderText());
                    if (isBlocked) {
                        textList.add(Component.translatable("fasttradingvx.tooltip.can_perform.unblock_hint")
                                .withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    }
                }
            } else {
                textList.add(Component.translatable("fasttradingvx.tooltip.cannot_perform").withStyle(
                        style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.RED)
                ).getVisualOrderText());
                textList.add(
                        Component.translatable("fasttradingvx.tooltip." + state.name().toLowerCase(Locale.ROOT)).withStyle(
                                style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)
                        ).getVisualOrderText());
                if (state == MerchantScreenHooks.State.NOT_ENOUGH_BUY_ITEMS && ModConfig.autofillBehavior == AutofillBehavior.STRICT) {
                    textList.add(Component.translatable("fasttradingvx.tooltip.strict_autofill_config_hint").withStyle(
                            style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)).getVisualOrderText());
                }
            }
            if (pinned) {
                textList.add(Component.translatable("fasttradingvx.tooltip.pinned").withStyle(
                        style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)).getVisualOrderText());
                textList.add(Component.translatable("fasttradingvx.tooltip.unpin_hint").withStyle(
                        style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
            } else {
                textList.add(Component.translatable("fasttradingvx.tooltip.pin_hint").withStyle(
                        style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
            }
            textList.add(Component.empty().getVisualOrderText());
            appendTradeDescription(hooks.fasttradingvx$getTradeOffer(index), textList);
        }
        var tt = Tooltip.create(null);
        tt.cachedTooltip = textList;
        tt.splitWithLanguage = Language.getInstance();
        this.setTooltip(tt);
    }

    private void appendTradeDescription(MerchantOffer offer, ArrayList<FormattedCharSequence> destList) {
        if (offer == null)
            return;
        ItemStack originalFirstBuyItem = offer.getBaseCostA();
        ItemStack adjustedFirstBuyItem = offer.getCostA();
        ItemStack secondBuyItem = offer.getCostB();
        ItemStack sellItem = offer.getResult();
        destList.add(Component.translatable("fasttradingvx.tooltip.current_trade.is")
                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
        destList.add(createItemStackDescription(originalFirstBuyItem, adjustedFirstBuyItem)
                .withStyle(STYLE_GRAY).getVisualOrderText());
        if (!secondBuyItem.isEmpty())
            destList.add(Component.translatable("fasttradingvx.tooltip.current_trade.and",
                            createItemStackDescription(secondBuyItem))
                    .withStyle(STYLE_GRAY).getVisualOrderText());
        destList.add(Component.translatable("fasttradingvx.tooltip.current_trade.for",
                        createItemStackDescription(sellItem))
                .withStyle(STYLE_GRAY).getVisualOrderText());
    }

    private MutableComponent createItemStackDescription(ItemStack stack, ItemStack adjustedStack) {
        if (stack.getCount() == adjustedStack.getCount())
            return createItemStackDescription(stack);
        else {
            return getItemStackName(stack)
                    .append(Component.literal(" "))
                    .append(Component.literal("x" + stack.getCount())
                            .withStyle(style -> style.applyFormats(ChatFormatting.STRIKETHROUGH, ChatFormatting.RED)))
                    .append(Component.literal(" x" + adjustedStack.getCount())
                            .withStyle(style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.GREEN)));
        }
    }

    private MutableComponent createItemStackDescription(ItemStack stack) {
        return getItemStackName(stack)
                .append(Component.literal(" x" + stack.getCount()));
    }

    private MutableComponent getItemStackName(ItemStack stack) {
        return ComponentUtils.wrapInSquareBrackets(Component.literal("").append(stack.getHoverName()).withStyle(stack.getRarity().getStyleModifier()));
    }

    public enum Phase {
        INACTIVE,
        AUTOFILL,
        TRADE
    }
}
