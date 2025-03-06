package net.mine_diver.macula.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSelectionList.class)
public interface ScrollableBaseAccessor {
    @Accessor("x1")
    int macula_getWidth();
}
