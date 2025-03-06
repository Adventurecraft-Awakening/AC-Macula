package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileRenderer.class)
public class BlockRenderManagerMixin {
    @Inject(
            method = "renderFaceDown",
            at = @At("HEAD")
    )
    private void onRenderBottomFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(0.0F, -1.0F, 0.0F);
    }

    @Inject(
            method = "renderFaceUp",
            at = @At("HEAD")
    )
    private void onRenderTopFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(0.0F, 1.0F, 0.0F);
    }

    @Inject(
            method = "renderEast",
            at = @At("HEAD")
    )
    private void onRenderEastFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(0.0F, 0.0F, -1.0F);
    }

    @Inject(
            method = "renderWest",
            at = @At("HEAD")
    )
    private void onRenderWestFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(0.0F, 0.0F, 1.0F);
    }

    @Inject(
            method = "renderNorth",
            at = @At("HEAD")
    )
    private void onRenderNorthFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(-1.0F, 0.0F, 0.0F);
    }

    @Inject(
            method = "renderSouth",
            at = @At("HEAD")
    )
    private void onRenderSouthFace(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Tesselator.instance.normal(1.0F, 0.0F, 0.0F);
    }
}
