package com.orange.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.exceptions.OSException;
import com.orange.exceptions.OSRuntimeException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Configurator {
	private volatile CoreSettings coreSettings;
	private volatile ServerSettings serverSettings;
	private volatile List<ZoneSettings> zonesSettings;
	private final Logger log;

	public Configurator() {
		this.log = LoggerFactory.getLogger(getClass());
	}

	public void loadConfiguration() throws FileNotFoundException {
		this.coreSettings = loadCoreSettings();
		this.serverSettings = loadServerSettings();
		if (this.serverSettings.webSocket == null) {
			this.serverSettings.webSocket = new ServerSettings.WebSocketEngineSettings();
		}
	}

	public CoreSettings getCoreSettings() {
		return this.coreSettings;
	}

	public synchronized ServerSettings getServerSettings() {
		return this.serverSettings;
	}

	public synchronized List<ZoneSettings> getZoneSettings() {
		return this.zonesSettings;
	}

	public synchronized ZoneSettings getZoneSetting(String zoneName) {
		if (this.zonesSettings == null) {
			throw new IllegalStateException(
					"No Zone configuration has been loaded yet!");
		}
		ZoneSettings settings = null;
		for (ZoneSettings item : this.zonesSettings) {
			if (item.name.equals(zoneName)) {
				settings = item;
				break;
			}
		}
		return settings;
	}

	public synchronized ZoneSettings getZoneSetting(int id) {
		if (this.zonesSettings == null) {
			throw new IllegalStateException(
					"No Zone configuration has been loaded yet!");
		}
		ZoneSettings settings = null;
		for (ZoneSettings item : this.zonesSettings) {
			if (item.getId() == id) {
				settings = item;
				break;
			}
		}
		return settings;
	}

	public synchronized void removeZoneSetting(String name) throws IOException {
		ZoneSettings settings = getZoneSetting(name);
		if (settings != null) {
			String path = FilenameUtils.concat("zones/", settings.name
					+ ".zone.xml");

			makeBackup(path);

			FileUtils.forceDelete(new File(path));

			this.zonesSettings.remove(settings);
		}
	}

	public synchronized List<ZoneSettings> loadZonesConfiguration()
			throws OSException {
		this.zonesSettings = new ArrayList();
		List<File> zoneDefinitionFiles = getZoneDefinitionFiles("zones/");
		for (File file : zoneDefinitionFiles) {
			try {
				FileInputStream inStream = new FileInputStream(file);

				this.log.info("Loading: " + file.toString());
				this.zonesSettings
						.add((ZoneSettings) getZonesXStreamDefinitions()
								.fromXML(inStream));
			} catch (FileNotFoundException e) {
				throw new OSRuntimeException(
						"Could not locate Zone definition file: "
								+ file.getAbsolutePath());
			}
		}
		return this.zonesSettings;
	}

	public synchronized void saveServerSettings(boolean makeBackup)
			throws IOException {
		if (makeBackup) {
			makeBackup("config/server.xml");
		}
		OutputStream outStream = new FileOutputStream("config/server.xml");
		getServerXStreamDefinitions().toXML(this.serverSettings, outStream);
	}

	public synchronized void saveZoneSettings(ZoneSettings settings,
			boolean makeBackup) throws IOException {
		String filePath = FilenameUtils.concat("zones/", settings.name
				+ ".zone.xml");
		if (makeBackup) {
			makeBackup(filePath);
		}
		OutputStream outStream = new FileOutputStream(filePath);
		getZonesXStreamDefinitions().toXML(settings, outStream);
	}

	public synchronized void saveNewZoneSettings(ZoneSettings settings)
			throws IOException {
		if (getZoneSetting(settings.name) != null) {
			throw new IllegalArgumentException(
					"Save request failed. The new Zone name is already in use: "
							+ settings.name);
		}
		saveZoneSettings(settings, false);

		this.zonesSettings.add(settings);
	}

	public synchronized void saveZoneSettings(ZoneSettings zoneSettings,
			boolean makeBackup, String oldZoneName) throws IOException {
		String newFilePath = FilenameUtils.concat("zones/", zoneSettings.name
				+ ".zone.xml");
		String oldFilePath = FilenameUtils.concat("zones/", oldZoneName
				+ ".zone.xml");
		if (makeBackup) {
			makeBackup(oldFilePath);
		}
		OutputStream outStream = new FileOutputStream(newFilePath);
		getZonesXStreamDefinitions().toXML(zoneSettings, outStream);

		FileUtils.forceDelete(new File(oldFilePath));
	}

	private CoreSettings loadCoreSettings() throws FileNotFoundException {
		FileInputStream inStream = new FileInputStream("config/core.xml");

		XStream xstream = new XStream();
		xstream.alias("coreSettings", CoreSettings.class);

		return (CoreSettings) xstream.fromXML(inStream);
	}

	private ServerSettings loadServerSettings() throws FileNotFoundException {
		FileInputStream inStream = new FileInputStream("config/server.xml");

		return (ServerSettings) getServerXStreamDefinitions().fromXML(inStream);
	}

	private XStream getServerXStreamDefinitions() {
		XStream xstream = new XStream();
		xstream.alias("serverSettings", ServerSettings.class);

		xstream.alias("socket", ServerSettings.SocketAddress.class);
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "address");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "port");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "type");

		xstream.alias("ipFilter", ServerSettings.IpFilterSettings.class);
		xstream.alias("flashCrossdomainPolicy",
				ServerSettings.FlashCrossDomainPolicySettings.class);

		xstream.alias("remoteAdmin", ServerSettings.RemoteAdminSettings.class);
		xstream.alias("adminUser", ServerSettings.AdminUser.class);
		xstream.alias("mailer", ServerSettings.MailerSettings.class);
		xstream.alias("webServer", ServerSettings.WebServerSettings.class);
		xstream.alias("bannedUserManager",
				ServerSettings.BannedUserManagerSettings.class);
		xstream.alias("websocketEngine",
				ServerSettings.WebSocketEngineSettings.class);

		return xstream;
	}

	private XStream getZonesXStreamDefinitions() {
		XStream xstream = new XStream(new DomDriver());

		xstream.alias("zone", ZoneSettings.class);
		xstream.aliasField("applyWordsFilterToUserName", ZoneSettings.class,
				"isFilterUserNames");
		xstream.aliasField("applyWordsFilterToRoomName", ZoneSettings.class,
				"isFilterRoomNames");
		xstream.aliasField("applyWordsFilterToPrivateMessages",
				ZoneSettings.class, "isFilterPrivateMessages");
		xstream.alias("wordsFilter", ZoneSettings.WordFilterSettings.class);
		xstream.useAttributeFor(ZoneSettings.WordFilterSettings.class,
				"isActive");
		xstream.aliasAttribute(ZoneSettings.WordFilterSettings.class,
				"isActive", "active");
		xstream.alias("floodFilter", ZoneSettings.FloodFilterSettings.class);
		xstream.useAttributeFor(ZoneSettings.FloodFilterSettings.class,
				"isActive");
		xstream.aliasAttribute(ZoneSettings.FloodFilterSettings.class,
				"isActive", "active");
		xstream.alias("requestFilter", ZoneSettings.RequestFilterSettings.class);

		xstream.alias("roomEvents", ZoneSettings.RoomEventsSettings.class);

		xstream.alias("registerEvent", ZoneSettings.RegisteredRoomEvents.class);
		xstream.useAttributeFor(ZoneSettings.RegisteredRoomEvents.class,
				"groupId");

		xstream.alias("extension", ZoneSettings.ExtensionSettings.class);

		xstream.alias("room", ZoneSettings.RoomSettings.class);
		xstream.alias("permissions", ZoneSettings.RoomPermissions.class);
		xstream.alias("MMOSettings", ZoneSettings.MMOSettings.class);

		xstream.alias("badWordsFilter",
				ZoneSettings.BadWordsFilterSettings.class);
		xstream.useAttributeFor(ZoneSettings.BadWordsFilterSettings.class,
				"isActive");
		xstream.alias("variable", ZoneSettings.RoomVariableDefinition.class);

		xstream.alias("privilegeManager",
				ZoneSettings.PrivilegeManagerSettings.class);
		xstream.useAttributeFor(ZoneSettings.PrivilegeManagerSettings.class,
				"active");
		xstream.alias("profile", ZoneSettings.PermissionProfile.class);
		xstream.useAttributeFor(ZoneSettings.PermissionProfile.class, "id");

		xstream.alias("buddyList", ZoneSettings.BuddyListSettings.class);
		xstream.useAttributeFor(ZoneSettings.BuddyListSettings.class, "active");

		//xstream.alias("databaseManager", DBConfig.class);
		//xstream.useAttributeFor(DBConfig.class, "active");

		return xstream;
	}

	private List<File> getZoneDefinitionFiles(String path) throws OSException {
		List<File> files = new ArrayList();

		File currDir = new File(path);
		if (currDir.isDirectory()) {
			for (File f : currDir.listFiles()) {
				if (f.getName().endsWith(".zone.xml")) {
					files.add(f);
				}
			}
		} else {
			throw new OSException("Invalid zones definition folder: "
					+ currDir);
		}
		return files;
	}

	private void makeBackup(String filePath) throws IOException {
		String basePath = FilenameUtils.getPath(filePath);

		String backupBasePath = FilenameUtils.concat(basePath, "_backups");

		DateTimeFormatter fmt = DateTimeFormat
				.forPattern("yyyy-MM-dd-HH-mm-ss");
		String backupId = new DateTime().toString(fmt);

		String backupFileName = FilenameUtils.concat(

		backupBasePath, backupId + "__" + FilenameUtils.getName(filePath));

		File sourceFile = new File(filePath);
		File backupFile = new File(backupFileName);
		File backupDir = new File(backupBasePath);
		if (!backupDir.isDirectory()) {
			FileUtils.forceMkdir(backupDir);
		}
		FileUtils.copyFile(sourceFile, backupFile);
	}
}
