package io.brennerofhell.fasttradingvx;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static com.mojang.blaze3d.platform.InputConstants.KEY_LALT;

public class ModKeyBindings {
    public static final KeyMapping keyOverrideBlock = new KeyMapping("key.fasttradingvx.overrideBlock",
            KEY_LALT, KeyMapping.Category.INVENTORY);
    public static final KeyMapping[] all = new KeyMapping[]{keyOverrideBlock};

    private ModKeyBindings() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyBinding : all) {
            event.register(keyBinding);
        }
    }

    public static boolean isDown(KeyMapping keyBinding) {
        if (keyBinding.isUnbound())
            return false;
        return keyBinding.isDown();
    }
}
