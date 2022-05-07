package me.mcblueparrot.optishard.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.glassmc.loader.api.GlassLoader;

public class Utils {

	public static final String VERSION;
	public static final String VERSION_PREFIX;
	private static final String USER_AGENT = "Mozilla/5.0";

	static {
		String mcVersion = GlassLoader.getInstance().getShardVersion("client");
		mcVersion = mcVersion.substring(mcVersion.indexOf("-") + 1);
		VERSION = mcVersion;
		VERSION_PREFIX = "v" + VERSION.replace(".", "_") + ".";
	}

	private static InputStream getInputStream(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", USER_AGENT);
		return connection.getInputStream();
	}

	public static void download(URL url, File output) throws IOException {
		InputStream in = getInputStream(url);
		FileUtils.copyInputStreamToFile(in, output);
		in.close();
	}

	public static String fetch(URL url) throws IOException {
		InputStream in = getInputStream(url);
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
		in.close();
		return result;
	}

	public static boolean isValidJar(File out) {
		try {
			new JarFile(out).close();
		}
		catch(IOException error) {
			return false;
		}

		return true;
	}

	public static File getMinecraftJar() throws ClassNotFoundException, URISyntaxException {
		Class<?> clazz = firstClass(VERSION_PREFIX + "net.minecraft.client.main.Main",
				VERSION_PREFIX + "net.minecraft.client.Main", VERSION_PREFIX + "net.minecraft.server.MinecraftServer",
				VERSION_PREFIX + "net.minecraft.server.Main", "net.minecraft.client.main.Main",
				"net.minecraft.client.Main", "net.minecraft.server.MinecraftServer", "net.minecraft.server.Main", "a");
		String path = clazz.getResource("/" + clazz.getName().replace(".", "/") + ".class").getPath();
		path = path.substring(0, path.lastIndexOf("!"));
		path = path.substring(path.indexOf(":") + 1);
		return new File(path);
	}

	private static Class<?> firstClass(String... names) throws ClassNotFoundException {
		for(String name : names) {
			try {
				return Class.forName(name);
			}
			catch(ClassNotFoundException error) {
				System.out.println(error.getMessage());
			}
		}

		throw new ClassNotFoundException("Cannot find any of the following classes: " + String.join(", ", names));
	}

}
