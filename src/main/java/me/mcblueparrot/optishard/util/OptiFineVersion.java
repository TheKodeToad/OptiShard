package me.mcblueparrot.optishard.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class OptiFineVersion {

	private static final URL DOWNLOADS_URL;

	private String mcVersion;
	private String name;
	private URL adloadx;
	private String filename;

	static {
		try {
			DOWNLOADS_URL = new URL("https://www.optifine.net/downloads");
		}
		catch(MalformedURLException error) {
			throw new Error(error);
		}
	}

	public static List<OptiFineVersion> getVersions() throws IOException {
		return getVersions(DOWNLOADS_URL);
	}

	public static List<OptiFineVersion> getVersions(URL url) throws IOException {
		List<OptiFineVersion> result = new ArrayList<>();

		String html = Utils.fetch(url);

		String[] lines = html.split("(\n|\r\n)");

		String mcVersion = null;

		Pattern versionPattern = Pattern.compile(" *<h2>Minecraft ([0-9.]*)<\\/h2>.*");
		Pattern columnPattern = Pattern.compile(" *<td class='([A-z]*)'>(.*)</td>");

		OptiFineVersion version = null;

		for(String line : lines) {
			Matcher versionMatcher = versionPattern.matcher(line);
			Matcher columnMatcher = columnPattern.matcher(line);

			if(!columnMatcher.matches()) {
				if(versionMatcher.matches()) {
					mcVersion = versionMatcher.group(1);
				}

				continue;
			}

			String elementClass = columnMatcher.group(1);
			String value = columnMatcher.group(2);

			if(elementClass.equals("colFile")) {
				if(value.contains(" pre")) {
					version = null;
					continue;
				}

				version = new OptiFineVersion();
				version.mcVersion = mcVersion;
				version.name = value;
			}
			else if(elementClass.equals("colMirror") && version != null) {
				version.adloadx = new URL(value.substring(9, value.indexOf("\">")).replace("http", "https"));
				version.filename = version.adloadx.getQuery().substring(2);
				result.add(version);
			}
		}

		return result;
	}

	public static Optional<OptiFineVersion> getNewestForMinecraftVersion(String id) throws IOException {
		List<OptiFineVersion> versions = getVersions();

		for(OptiFineVersion version : versions) {
			if(version.mcVersion.equals(id)) {
				return Optional.of(version);
			}
		}

		return Optional.empty();
	}

	public URL getDownloadURL() throws IOException {
		String html = Utils.fetch(adloadx);
		String link = html.substring(html.indexOf("downloadx"));
		link = link.substring(0, link.indexOf("'"));
		return new URL("https://optifine.net/" + link);
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public String getMcVersion() {
		return mcVersion;
	}

}
