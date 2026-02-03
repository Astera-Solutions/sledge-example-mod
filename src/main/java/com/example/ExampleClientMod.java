package com.example;

import sledgemc.dev.api.ClientInit;
import sledgemc.dev.api.OnlyIn;
import sledgemc.dev.api.Environment;

@OnlyIn(Environment.CLIENT_MODE)
public class ExampleClientMod implements ClientInit {

    @Override
    public void onInitializeClient() {
        System.out.println("[ExampleMod] Client initialized");
    }
}
