package com.github.glassmc.example.client.v1_8_9;

import com.github.glassmc.loader.api.GlassLoader;
import com.github.glassmc.loader.api.Listener;
import com.github.glassmc.loader.api.loader.TransformerOrder;

public class ExampleInitializeListener implements Listener {

    @Override
    public void run() {
        System.out.println("Hello from 1.8.9!");

        GlassLoader.getInstance().registerTransformer(MinecraftClientTransformer2.class, TransformerOrder.LAST);
        GlassLoader.getInstance().registerTransformer(MinecraftClientTransformer.class);
    }

}
