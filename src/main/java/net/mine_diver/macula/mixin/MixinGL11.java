package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GL11.class)
public class MixinGL11 {

    @Inject(
        method = "glEnable",
        at = @At("TAIL")
    )
    private static void suffix$glEnable(int cap, CallbackInfo ci) {
        if (Shaders.shaderPackLoaded) {
            Shaders.suffix$glEnable(cap);
        }
    }

    @Inject(
        method = "glDisable",
        at = @At("HEAD")
    )
    private static void suffix$glDisable(int cap, CallbackInfo ci) {
        if (Shaders.shaderPackLoaded) {
            Shaders.suffix$glDisable(cap);
        }
    }

    @Inject(
        method = "glFogi",
        at = @At("HEAD")
    )
    private static void suffix$glFogi(int pname, int param, CallbackInfo ci) {
        if (Shaders.shaderPackLoaded) {
            Shaders.suffix$glFogi(pname, param);
        }
    }
}
