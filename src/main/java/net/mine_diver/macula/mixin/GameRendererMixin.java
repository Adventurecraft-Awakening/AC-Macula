package net.mine_diver.macula.mixin;

import net.mine_diver.macula.Shaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
        method = "render(FJ)V",
        at = @At("HEAD")
    )
    private void beginRender(float var1, long var2, CallbackInfo ci) {
        Shaders.beginRender(mc, var1, var2);
    }

    @Inject(
        method = "render(FJ)V",
        at = @At("RETURN")
    )
    private void endRender(CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endRender();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;setupCamera(FI)V"
        )
    )
    private void setClearColor(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.setClearColor(fogRed, fogGreen, fogBlue);
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;setupCamera(FI)V",
            shift = At.Shift.AFTER
        )
    )
    private void setCamera(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.setCamera(l);
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 0
        )
    )
    private void injectTerrainBegin(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginTerrain();
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void injectTerrainEnd(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endTerrain();
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 1
        )
    )
    private void injectWaterBegin1(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginWater();
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void injectWaterEnd1(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endWater();
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 2
        )
    )
    private void injectWaterBegin2(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginWater();
    }


    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lnet/minecraft/world/entity/LivingEntity;ID)I",
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private void injectWaterEnd2(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endWater();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSameAsLast(ID)V"
        )
    )
    private void injectBeginWater3(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginWater();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSameAsLast(ID)V",
            shift = At.Shift.AFTER
        )
    )
    private void injectEndWater3(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endWater();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderSnowAndRain(F)V"
        )
    )
    private void injectBeginWeather(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginWeather();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderSnowAndRain(F)V",
            shift = At.Shift.AFTER
        )
    )
    private void injectEndWeather(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endWeather();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(FI)V"
        )
    )
    private void injectBeginHand(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.beginHand();
    }

    @Inject(
        method = "render(FJ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(FI)V",
            shift = At.Shift.AFTER
        )
    )
    private void injectEndHand(float l, long par2, CallbackInfo ci) {
        if (!Shaders.shaderPackLoaded) return;
        Shaders.endHand();
    }

    @Shadow
    private Minecraft mc;

    @Shadow
    float fogRed;
    @Shadow
    float fogGreen;
    @Shadow
    float fogBlue;
}
