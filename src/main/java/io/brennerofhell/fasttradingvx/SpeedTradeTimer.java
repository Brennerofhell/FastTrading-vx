package io.brennerofhell.fasttradingvx;

import io.brennerofhell.fasttradingvx.config.ModConfig;

public class SpeedTradeTimer {
    public static double counter;
    private static boolean active;

    public static void start() {
        active = true;
        counter = 0;
    }

    public static void stop() {
        active = false;
    }

    public static boolean shouldDoAction() {
        return counter > 1;
    }

    public static void onDoAction() {
        counter--;
    }

    public static void onClientWorldTick() {
        if (active)
            counter += 1 / ModConfig.ticksBetweenActions;
    }
}
