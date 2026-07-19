package io.brennerofhell.fasttradingvx.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessor {
    @Accessor
    int getClickCount();

    @Accessor
    void setClickCount(int timesPressed);
}
