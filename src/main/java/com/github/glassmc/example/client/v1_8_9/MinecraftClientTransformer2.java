package com.github.glassmc.example.client.v1_8_9;

import com.github.glassmc.loader.api.loader.Transformer;
import com.github.glassmc.loader.util.Identifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MinecraftClientTransformer2 implements Transformer {

    private final Identifier MINECRAFT_CLIENT = Identifier.parse("v1_8_9/net/minecraft/client/Minecraft");

    @Override
    public boolean canTransform(String name) {
        return name.equals(MINECRAFT_CLIENT.getClassName());
    }

    @Override
    public byte[] transform(String name, byte[] data) {
        System.out.println("Fake Transforming Minecraft");
        return data;
    }

}
