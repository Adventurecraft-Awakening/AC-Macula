package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(
        method = "renderSky(F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getStarBrightness(F)F",
            shift = At.Shift.AFTER
        )
    )
    private void onGetStarBrightness(float par1, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.setCelestialPosition();
    }
}
