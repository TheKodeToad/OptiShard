package com.github.glassmc.example.client.v1_7_10.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v1_7_10.net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void onTick(CallbackInfo callbackInfo) {
        System.out.println("1.7.10 tick");
    }

}
