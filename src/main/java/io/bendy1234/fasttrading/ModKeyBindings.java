package io.bendy1234.fasttrading;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import static com.mojang.blaze3d.platform.InputConstants.KEY_LALT;

public class ModKeyBindings {
    public static final KeyMapping keyOverrideBlock = new KeyMapping("key.fasttrading.overrideBlock",
            KEY_LALT, KeyMapping.Category.INVENTORY);
    public static final KeyMapping[] all = new KeyMapping[]{keyOverrideBlock};

    private ModKeyBindings() {
    }

    public static void register() {
        for (KeyMapping keyBinding : all) {
            KeyMappingHelper.registerKeyMapping(keyBinding);
        }
    }

    public static boolean isDown(KeyMapping keyBinding) {
        if (keyBinding.isUnbound())
            return false;
        return keyBinding.isDown();
    }
}
