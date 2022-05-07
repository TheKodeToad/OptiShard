package com.github.glassmc.example.client.v1_8_9;

import com.github.glassmc.loader.api.loader.Transformer;
import com.github.glassmc.loader.util.Identifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MinecraftClientTransformer implements Transformer {

    private final Identifier MINECRAFT_CLIENT = Identifier.parse("v1_8_9/net/minecraft/client/Minecraft");

    @Override
    public boolean canTransform(String name) {
        return name.equals(MINECRAFT_CLIENT.getClassName());
    }

    @Override
    public byte[] transform(String name, byte[] data) {
        System.out.println("Real Transforming Minecraft");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept(classNode, 0);

        Identifier runGameLoop = Identifier.parse("v1_8_9/net/minecraft/client/Minecraft#runGameLoop()V");

        for(MethodNode methodNode : classNode.methods) {
            if(methodNode.name.equals(runGameLoop.getMethodName()) && methodNode.desc.equals(runGameLoop.getMethodDesc())) {
                methodNode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Hook.class.getName().replace(".", "/"), "test", "()V"));
            }
        }

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

}
