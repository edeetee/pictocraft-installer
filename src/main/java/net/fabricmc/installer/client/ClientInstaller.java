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

package net.fabricmc.installer.client;

import net.fabricmc.installer.util.*;
import net.fabricmc.installer.util.PictocraftUtil.Release;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ClientInstaller {
	static void installMod(String deleteOld, File modsDir, URL download){

		installMod(deleteOld, modsDir, download, "");
	}

	static void installMod(String deleteOld, File modsDir, URL download, String filename){
		System.out.println("Installing " + deleteOld + " from " + download);
		
		for (File file : modsDir.listFiles()) {
			if(file.getName().toLowerCase().startsWith(deleteOld))
				file.delete();
		}
		try{
			Utils.downloadFile(download, new File(modsDir, filename));
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public static String install(File mcDir, String gameVersion, String loaderVersion, InstallerProgress progress) throws IOException {
		System.out.println("Installing " + gameVersion + " with fabric " + loaderVersion);

		String profileName = String.format("%s-%s-%s", Reference.LOADER_NAME, loaderVersion, gameVersion);

		MinecraftLaunchJson launchJson = Utils.getLaunchMeta(loaderVersion);
		launchJson.id = profileName;
		launchJson.inheritsFrom = gameVersion;

		//Adds loader and the mappings
		launchJson.libraries.add(new MinecraftLaunchJson.Library(Reference.PACKAGE.replaceAll("/", ".") + ":" + Reference.MAPPINGS_NAME + ":" + gameVersion, Reference.MAVEN_SERVER_URL));
		launchJson.libraries.add(new MinecraftLaunchJson.Library(Reference.PACKAGE.replaceAll("/", ".") + ":" + Reference.LOADER_NAME + ":" + loaderVersion, Reference.MAVEN_SERVER_URL));

		File versionsDir = new File(mcDir, "versions");
		File profileDir = new File(versionsDir, profileName);
		File profileJson = new File(profileDir, profileName + ".json");

		if (!profileDir.exists()) {
			profileDir.mkdirs();
		}
		
		File modsDir = new File(mcDir, "mods");

		if(PictocraftUtil.isSelected){
			Release release = PictocraftUtil.latestRelease;
			progress.updateProgress("Installing Pictocraft");
			installMod("pictocraft", modsDir, release.getJarUrl(), "pictocraft-"+release.tag_name+".jar");
		}
		if(PictocraftUtil.installModMenu){
			progress.updateProgress("Installing ModMenu");
			installMod("modmenu", modsDir, new URL("https://minecraft.curseforge.com/projects/modmenu/files/latest"));
		}

		/*

		This is a fun meme

		The vanilla launcher assumes the profile name is the same name as a maven artifact, how ever our profile name is a combination of 2
		(mappings and loader). The launcher will also accept any jar with the same name as the profile, it doesnt care if its empty

		 */
		File dummyJar = new File(profileDir, profileName + ".jar");
		dummyJar.createNewFile();

		Utils.writeToFile(profileJson, Utils.GSON.toJson(launchJson));

		progress.updateProgress(Utils.BUNDLE.getString("progress.done"));

		return profileName;
	}
}
