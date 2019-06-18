/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.installer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Utils {

	public static final DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("lang/installer", Locale.getDefault(), new ResourceBundle.Control() {
		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
			final String bundleName = toBundleName(baseName, locale);
			final String resourceName = toResourceName(bundleName, "properties");
			try (InputStream stream = loader.getResourceAsStream(resourceName)) {
				if (stream != null) {
					try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
						return new PropertyResourceBundle(reader);
					}
				}
			}
			return super.newBundle(baseName, locale, format, loader, reload);
		}
	});

	public static File findDefaultUserDir() {
		String home = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		File dir;
		File homeDir = new File(home);

		if (os.contains("win") && System.getenv("APPDATA") != null) {
			dir = new File(System.getenv("APPDATA"));
		} else if (os.contains("mac")) {
			dir = new File(homeDir, "Library" + File.separator + "Application Support");
		} else {
			dir = homeDir;
		}
		return dir;
	}

	public static File findDefaultInstallDir() {
		String home = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		File dir;
		File homeDir = new File(home);

		if (os.contains("win") && System.getenv("APPDATA") != null) {
			dir = new File(System.getenv("APPDATA"), ".minecraft");
		} else if (os.contains("mac")) {
			dir = new File(homeDir, "Library" + File.separator + "Application Support" + File.separator + "minecraft");
		} else {
			dir = new File(homeDir, ".minecraft");
		}
		return dir;
	}

	public static String readTextFile(URL url) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

	public static void writeToFile(File file, String string) throws FileNotFoundException {
		try (PrintStream printStream = new PrintStream(new FileOutputStream(file))) {
			printStream.print(string);
		}
	}

	public static String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
	}

	/**
	 * 
	 * @param url
	 * @param file if a directory, filename is generated from url
	 * @throws IOException
	 */
	public static void downloadFile(URL url, File file) throws IOException {
		if (!file.getParentFile().isDirectory()) {
			if (!file.mkdirs()) {
				throw new IOException("Could not create directory for " + file.getAbsolutePath() + "!");
			}
		}

		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setInstanceFollowRedirects(true);
		con.addRequestProperty("User-Agent", "Mozilla/4.0");
		con.connect();

		try (InputStream in = con.getInputStream()) {
			if(file.isDirectory())
				file = new File(file, FilenameUtils.getName(con.getURL().getFile()));
			System.out.println(file.getName());
			Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static MinecraftLaunchJson getLaunchMeta(String loaderVersion) throws IOException {
		String url = String.format("%s/%s/%s/%s/%3$s-%4$s.json", Reference.MAVEN_SERVER_URL, Reference.PACKAGE, Reference.LOADER_NAME, loaderVersion);
		String fabricInstallMeta = Utils.readTextFile(new URL(url));
		JsonObject installMeta = Utils.GSON.fromJson(fabricInstallMeta, JsonObject.class);
		return new MinecraftLaunchJson(installMeta);
	}

}
