package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * =========================================================================
 * SLEDGE MC - EXAMPLE MIXIN
 * =========================================================================
 * 
 * This is a example mixin. Mixins are used to modify or inject code into
 * existing game code.
 * 
 * HOW TO USE?
 * -----------------
 * 1. @Mixin(<target>.class) -> Select the class to modify.
 * Example: @Mixin(net.minecraft.client.gui.screens.TitleScreen.class)
 * 
 * 2. @Inject -> Select the method to inject code into.
 * method = "init()V" -> Target method name and signature.
 * at = @At("HEAD") -> Where to inject? (HEAD: Head, RETURN: Return)
 */

@Mixin(targets = "net.minecraft.client.gui.screens.TitleScreen") /** Official Mojang name (mojmap but you can switch 
to another mapping from build.gradle file "mappings" property) */
public class ExampleMixin {

    /**
     * Targets "init" method in TitleScreen.
     * Mojang: init
     */
    @Inject(at = @At("HEAD"), method = "init", cancellable = true, remap = true)
    private void onInit(CallbackInfo info) {
    }
}
