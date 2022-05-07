package com.github.glassmc.example.client.v1_7_10;

import com.github.glassmc.loader.api.GlassLoader;
import com.github.glassmc.loader.api.Listener;
import com.github.glassmc.mixin.Mixin;

public class ExampleMixinListener implements Listener {

    @Override
    public void run() {
        GlassLoader.getInstance().getAPI(Mixin.class).addConfiguration("mixins.example-1.7.10.json");
    }

}
