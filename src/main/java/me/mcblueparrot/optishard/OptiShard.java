package me.mcblueparrot.optishard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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

			String base = version.getFilename().substring(0, version.getFilename().lastIndexOf("."));

			File optiFineInstaller = new File(optiFineFolder, version.getFilename());
			File mod = new File(optiFineFolder, base + "-Mod.jar");

			if(!Utils.isValidJar(optiFineInstaller)) {
				URL url = version.getDownloadURL();
				Utils.download(url, optiFineInstaller);
			}

			try {
				optiFine = new JarFile(mod);
			}
			catch(IOException error) {
				URLClassLoader classLoader = new URLClassLoader(new URL[] { optiFineInstaller.toURI().toURL() }, null);
				Class<?> patcher = Class.forName("optifine.Patcher", false, classLoader);
				Method processMethod = patcher.getMethod("process", File.class, File.class, File.class);
				processMethod.invoke(processMethod, Utils.getMinecraftJar(), optiFineInstaller, mod);
				optiFine = new JarFile(mod);
			}

			if(optiFine.getJarEntry("notch/net/optifine/Config.class") != null) {
				File notchMod = new File(optiFineFolder, base + "-Mod-Notch.jar");
				try {
					optiFine = new JarFile(notchMod);
				}
				catch(IOException error) {
					try(FileOutputStream out = new FileOutputStream(notchMod);
							ZipOutputStream zipOut = new ZipOutputStream(out)) {
						Enumeration<JarEntry> entries = optiFine.entries();

						while(entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							String name = entry.getName();

							if(name.startsWith("notch/")) {
								name = name.substring(6);
							}
							else if(name.startsWith("srg/")) {
								continue;
							}

							InputStream entryInput = optiFine.getInputStream(entry);

							zipOut.putNextEntry(new ZipEntry(name));
							IOUtils.copy(entryInput, zipOut);

							entryInput.close();
						}
					}
					optiFine = new JarFile(notchMod);
				}
				mod = notchMod;
			}

			((GlassLoaderImpl) GlassLoader.getInstance()).addURL(mod);
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
