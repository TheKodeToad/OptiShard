package me.mcblueparrot.optishard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

import com.github.glassmc.loader.api.GlassLoader;
import com.github.glassmc.loader.api.Listener;
import com.github.glassmc.loader.impl.GlassLoaderImpl;
import com.github.jezza.Toml;
import com.github.jezza.TomlTable;

import me.mcblueparrot.optishard.util.OptiFineVersion;
import me.mcblueparrot.optishard.util.Utils;

public class OptiShard implements Listener {

	private File optiFineFolder = new File("optifine");
//	private String optiFineVersion;
	private static OptiShard instance;
	private JarFile optiFine;

	public static OptiShard getInstance() {
		return instance;
	}

	public JarFile getOptiFine() {
		return optiFine;
	}

	@Override
	public void run() {
		instance = this;
//		loadConfig();

		try {
			Optional<OptiFineVersion> optionalVersion = OptiFineVersion.getNewestForMinecraftVersion(Utils.VERSION);

			if(!optionalVersion.isPresent()) {
				System.err.println("Could not find OptiFine version for " + Utils.VERSION);
				return;
			}

			OptiFineVersion version = optionalVersion.get();

			File out = new File(optiFineFolder, version.getFilename());
			File mod = new File(optiFineFolder, version.getFilename().substring(0, version.getFilename().lastIndexOf(".")) + "-Mod.jar");

			if(!Utils.isValidJar(out)) {
				URL url = version.getDownloadURL();
				Utils.download(url, out);
			}

			try {
				optiFine = new JarFile(mod);
			}
			catch(IOException error) {
				URLClassLoader classLoader = new URLClassLoader(new URL[] { out.toURI().toURL() }, null);
				Class<?> patcher = Class.forName("optifine.Patcher", false, classLoader);
				Method processMethod = patcher.getMethod("process", File.class, File.class, File.class);
				processMethod.invoke(processMethod, Utils.getMinecraftJar(), out, mod);
				optiFine = new JarFile(mod);
			}

			((GlassLoaderImpl) GlassLoader.getInstance()).addURL(mod);

			Class.forName("javax.vecmath.Matrix4f", true,  GlassLoaderImpl.class.getClassLoader());

			GlassLoader.getInstance().registerTransformer(OptiFineTransformer.class);
		}
		catch(Throwable error) {
			error.printStackTrace();
		}
	}
//
//	private void loadConfig() throws FileNotFoundException, IOException {
//		if(!optiFineConfig.exists()) {
//			optiFineConfig.createNewFile();
//			return;
//		}
//
//		TomlTable toml = Toml.from(new FileInputStream(optiFineConfig));
//
//		if(toml.containsKey("")) {
//
//		}
//
//		optiFineVersion = toml.get("optiFineVersion").toString();
//
//		saveConfig();
//	}
//
//	private void saveConfig() {
//
//	}

}
