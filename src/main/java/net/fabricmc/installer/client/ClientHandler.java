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

import net.fabricmc.installer.Handler;
import net.fabricmc.installer.InstallerGui;
import net.fabricmc.installer.util.ArgumentParser;
import net.fabricmc.installer.util.InstallerProgress;
import net.fabricmc.installer.util.PictocraftUtil;
import net.fabricmc.installer.util.Utils;
import net.fabricmc.installer.util.PictocraftUtil.Release;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;

public class ClientHandler extends Handler {

	private JCheckBox createProfile;
	private JCheckBox installPictocraft;
	private JCheckBox installModMenu;

	@Override
	public String name() {
		return "Client";
	}

	@Override
	public void install() {
		String gameVersion = (String) gameVersionComboBox.getSelectedItem();
		String loaderVersion = (String) loaderVersionComboBox.getSelectedItem();
		System.out.println("Installing");
		new Thread(() -> {
			try {
				updateProgress(new MessageFormat(Utils.BUNDLE.getString("progress.installing")).format(new Object[]{loaderVersion}));
				File mcPath = new File(installLocation.getText());
				if (!mcPath.exists()) {
					throw new RuntimeException(Utils.BUNDLE.getString("progress.exception.no.launcher.directory"));
				}
				String profileName = ClientInstaller.install(mcPath, gameVersion, loaderVersion, this);
				if (createProfile.isSelected()) {
					ProfileInstaller.setupProfile(mcPath, profileName, gameVersion);
				}
			} catch (Exception e) {
				error(e);
			}
			buttonInstall.setEnabled(true);
		}).start();
	}

	@Override
	public void installCli(ArgumentParser args) throws Exception {
		File file = new File(args.get("dir"));
		if (!file.exists()) {
			throw new FileNotFoundException("Launcher directory not found at " + file.getAbsolutePath());
		}

		String gameVersion = getGameVersion(args);
		String loaderVersion = getLoaderVersion(args);

		String profileName = ClientInstaller.install(file, gameVersion, loaderVersion, InstallerProgress.CONSOLE);
		ProfileInstaller.setupProfile(file, profileName, gameVersion);
	}

	@Override
	public String cliHelp() {
		return "-dir <install dir, required> -version <minecraft version, default latest> -loader <loader version, default latest>";
	}

	@Override
	public void setupPane1(JPanel pane, InstallerGui installerGui) {

	}

	@Override
	public void setupPane2(JPanel pane, InstallerGui installerGui) {
		addRow(pane, jPanel -> jPanel.add(createProfile = new JCheckBox(Utils.BUNDLE.getString("option.create.profile"), true)));

		addRow(pane, jPanel -> jPanel.add(installPictocraft = 
			new JCheckBox(Utils.BUNDLE.getString("option.create.pictocraft") + " (" + PictocraftUtil.latestRelease.tag_name + ")", true)));
		installPictocraft.addChangeListener((ChangeEvent e) -> PictocraftUtil.isSelected = installPictocraft.isSelected());

		addRow(pane, jPanel -> jPanel.add(installModMenu = new JCheckBox(Utils.BUNDLE.getString("option.create.modmenu"), true)));
		installModMenu.addChangeListener((ChangeEvent e) -> PictocraftUtil.installModMenu = installModMenu.isSelected());

		installLocation.setText(Utils.findDefaultInstallDir().getAbsolutePath());
	}

}
