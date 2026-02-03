package com.example;

import sledgemc.dev.api.SledgeInit;
import sledgemc.dev.api.SledgeAPI;

public class ExampleMod implements SledgeInit {

    public static final String MOD_ID = "example-mod";

    private static ExampleMod instance;

    @Override
    public void onInitialize() {
        instance = this;

        boolean mcDetected = false;
        try {
            Class.forName("net.minecraft.client.main.Main");
            mcDetected = true;
        } catch (Exception e) {
        }

        boolean mixinActive = false;
        try {
            Class.forName("org.spongepowered.asm.launch.MixinBootstrap");
            mixinActive = true;
        } catch (Exception e) {
        }

        System.out.println("\n");
        System.out.println("====================================================");
        System.out.println("   [ExampleMod] HELLO FROM SLEDGE MC!");
        System.out.println("   [ExampleMod] Running on version: " + SledgeAPI.getInstance().getLoaderVersion());
        System.out.println("   [ExampleMod] Minecraft Access: " + (mcDetected ? "YES (Injected)" : "NO (Failed)"));
        System.out.println("   [ExampleMod] Mixin Active:     " + (mixinActive ? "YES" : "NO"));
        System.out.println("   [ExampleMod] Injection Verified!");
        System.out.println("====================================================");
        System.out.println("\n");

        // Mixins are applied automatically at runtime
        System.out.println("[ExampleMod] Mixins loaded. Watch console for TitleScreen hook!");

        SledgeAPI.getInstance().getEventBus().register(this);
    }

    @Override
    public String getModId() {
        return MOD_ID;
    }

    public static ExampleMod getInstance() {
        return instance;
    }
}
