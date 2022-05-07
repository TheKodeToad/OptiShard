package me.mcblueparrot.optishard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.github.glassmc.loader.api.GlassLoader;
import com.github.glassmc.loader.api.loader.Transformer;
import com.github.glassmc.loader.impl.GlassLoaderImpl;
import com.github.glassmc.loader.impl.loader.GlassClassLoader;

public class OptiFineTransformer implements Transformer {

	private JarFile optiFine = OptiShard.getInstance().getOptiFine();
	private Map<String, JarEntry> entryCache = new WeakHashMap<>();

	@Override
	public boolean canTransform(String className) {
		return getEntryForClass(className) != null;
	}

	private JarEntry getEntryForClass(String className) {
		className = className.replace(".", "/").concat(".class");
		return entryCache.computeIfAbsent(className, optiFine::getJarEntry);
	}

	@Override
	public byte[] transform(String className, byte[] bytes) {
		try {
			InputStream in = optiFine.getInputStream(getEntryForClass(className));
			byte[] result = IOUtils.toByteArray(in);
			in.close();
			return result;
		}
		catch(IOException error) {
			error.printStackTrace();
		}
		return bytes;
	}

}
