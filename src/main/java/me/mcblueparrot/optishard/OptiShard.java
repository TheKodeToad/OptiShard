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
import java.util.Arrays;
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
	private File optiFineConfig = new File(optiFineFolder, "config.toml");
	private String optiFineVersion;
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
		try {
			loadConfig();
		}
		catch(Throwable error) {
			System.out.println("Could not read config");
			error.printStackTrace();
		}

		try {
			Optional<OptiFineVersion> optionalVersion = optiFineVersion == null
					? OptiFineVersion.getNewestForMinecraftVersion(Utils.VERSION)
					: OptiFineVersion.getVersion(optiFineVersion);

			File mod;
			File optiFineInstaller = null;
			String base = null;

			if(!optionalVersion.isPresent()) {
				if(optiFineVersion == null) {
					System.err.println("Could not find OptiFine version for " + Utils.VERSION);
					return;
				}

				mod = new File(optiFineFolder, optiFineVersion);

				if(!mod.exists()) {
					System.err.println("Could not not find OptiFine file: " + mod.getPath());
					return;
				}
			}
			else {
				OptiFineVersion version = optionalVersion.get();

				base = version.getFilename().substring(0, version.getFilename().lastIndexOf("."));
				optiFineInstaller = new File(optiFineFolder, version.getFilename());
				mod = new File(optiFineFolder, base + "-Mod.jar");

				if(!Utils.isValidJar(optiFineInstaller)) {
					URL url = version.getDownloadURL();
					Utils.download(url, optiFineInstaller);
				}
			}

			try {
				optiFine = new JarFile(mod);
			}
			catch(IOException error) {
				if(optiFineInstaller.exists()) {
					URLClassLoader classLoader = new URLClassLoader(new URL[] { optiFineInstaller.toURI().toURL() }, null);
					Class<?> patcher = Class.forName("optifine.Patcher", false, classLoader);
					Method processMethod = patcher.getMethod("process", File.class, File.class, File.class);
					processMethod.invoke(processMethod, Utils.getMinecraftJar(), optiFineInstaller, mod);
					optiFine = new JarFile(mod);
				}
				else {
					System.err.println("Could not load OptiFine JAR");
					error.printStackTrace();
					return;
				}
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
			System.err.println("Could not load OptiFine");
			error.printStackTrace();
		}
	}
//
	private void loadConfig() throws FileNotFoundException, IOException {
		if(!optiFineConfig.exists()) {
			try(InputStream in = getClass().getResourceAsStream("/config.toml")) {
				FileUtils.copyInputStreamToFile(in, optiFineConfig);
			}
		}

		TomlTable toml = Toml.from(new FileInputStream(optiFineConfig));

		if(toml.get("version") != null) {
			optiFineVersion = toml.get("version").toString();
		}

		if("latest".equalsIgnoreCase(optiFineVersion)) {
			optiFineVersion = null;
		}
	}

}
