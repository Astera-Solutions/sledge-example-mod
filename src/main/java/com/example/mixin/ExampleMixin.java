package com.example.mixin;

import sledgemc.dev.injecta.core.annotations.*;
import sledgemc.dev.injecta.core.InjectionContext;

@Injecta(targets = "net.minecraft.client.gui.screens.TitleScreen")
public class ExampleMixin {

    @Insertion(method = "init", anchor = @Anchor(AnchorPoint.HEAD), cancellable = true)
    public static void onInit(InjectionContext context) {
        System.out.println("Hello from InjectaCore (YÖQ 1.21.11)!");
    }
}
