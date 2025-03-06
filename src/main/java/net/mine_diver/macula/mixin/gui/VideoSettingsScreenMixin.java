package net.mine_diver.macula.mixin.gui;

import net.mine_diver.macula.gui.ShadersScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin extends Screen {
    @Unique
    private static final int MACULA$SHADERS_BUTTON_ID = "macula:shaders".hashCode();

    @ModifyVariable(
            method = "init",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=gui.done"
            ),
            index = 2
    )
    private int macula_addShadersButton(int y) {
        //noinspection unchecked
        buttons.add(new Button(MACULA$SHADERS_BUTTON_ID, width / 2 - 155 + y % 2 * 160, height / 6 + 24 * (y >> 1), 150, 20, "Shaders..."));
        return y + 1;
    }

    @Inject(
            method = "buttonClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void macula_shadersButtonClicked(Button button, CallbackInfo ci) {
        if (button.id == MACULA$SHADERS_BUTTON_ID) {
            minecraft.options.save();
            minecraft.setScreen(new ShadersScreen(this));
            ci.cancel();
        }
    }
}
