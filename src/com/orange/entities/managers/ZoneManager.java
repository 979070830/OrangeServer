package com.orange.entities.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.config.Configurator;
import com.orange.config.CreateRoomSettings;
import com.orange.config.ZoneSettings;
import com.orange.core.BaseCoreService;
import com.orange.core.ICoreService;
import com.orange.entities.Room;
import com.orange.entities.SFSRoomRemoveMode;
import com.orange.entities.Zone;
import com.orange.exceptions.OSException;
import com.orange.server.OrangeServerEngine;

public class ZoneManager extends BaseCoreService implements ICoreService {
	private Logger logger;
	private ConcurrentMap<String, Zone> zones;
	private OrangeServerEngine sfs;
	private Configurator configurator;
//	private final ConcurrentMap<String, ITrafficMeter> trafficMonitors;
//	private final TrafficMeterExecutor trafficMeterExecutor;

	private final class ShutDownHandler extends Thread {
		private ShutDownHandler() {
		}

		public void run() {
			ZoneManager.this.logger.info("BuddyList saveAll...");
//			for (Zone zone : ZoneManager.this.zones.values()) {
//				try {
//					zone.getBuddyListManager().saveAll();
//				} catch (Exception e) {
//					ZoneManager.this.logger.warn(e.toString());
//				}
//			}
		}
	}
//
//	private static class TrafficMeterExecutor implements Runnable {
//		private final Logger log;
//		private final Collection<ITrafficMeter> trafficMonitors;
//
//		public TrafficMeterExecutor(Collection<ITrafficMeter> trafficMonitors) {
//			this.trafficMonitors = trafficMonitors;
//			this.log = LoggerFactory.getLogger(getClass());
//		}
//
//		public void run() {
//			try {
//				long t1 = System.nanoTime();
//				for (ITrafficMeter monitor : this.trafficMonitors) {
//					monitor.onTick();
//				}
//				long t2 = System.nanoTime();
//				if (this.log.isDebugEnabled()) {
//					this.log.debug("Traffic Monitor update: " + (t2 - t1)
//							/ 1000000.0D + "ms.");
//				}
//			} catch (Exception e) {
//				this.log.warn("Unexpected exception: " + e
//						+ ". Task will not be interrupted.");
//			}
//		}
//	}
//
	public ZoneManager() {
		this.logger = LoggerFactory.getLogger(getClass());
		if (this.zones == null) {
			this.zones = new ConcurrentHashMap();
		}
//		this.trafficMonitors = new ConcurrentHashMap();
//		this.trafficMeterExecutor = new TrafficMeterExecutor(
//				this.trafficMonitors.values());
//		Runtime.getRuntime().addShutdownHook(new ShutDownHandler(null));
	}

	public void init(Object o) {
		super.init(o);
		this.sfs = OrangeServerEngine.getInstance();
		this.configurator = this.sfs.getConfigurator();
	}

	public void addZone(Zone zone) {
		if (this.zones.containsKey(zone.getName())) {
			throw new OSException("Zone already exists: "
					+ zone.getName()
					+ ". Can't add the same zone more than once.");
		}
		this.zones.put(zone.getName(), zone);
	}

	public Zone getZoneByName(String name) {
		return (Zone) this.zones.get(name);
	}

	public Zone getZoneById(int id) {
		Zone theZone = null;
		for (Zone zone : this.zones.values()) {
			if (zone.getId() == id) {
				theZone = zone;
				break;
			}
		}
		return theZone;
	}

	public List<Zone> getZoneList() {
		return new ArrayList(this.zones.values());
	}

	public synchronized void initializeZones() throws OSException {
		if (this.zones.size() > 0) {
			this.logger.info(this.zones.size() + " Zones found in cluster: ");
			for (Zone zone : this.zones.values()) {
				this.logger.info(zone.toString());
			}
			return;
		}
		List<ZoneSettings> zoneSettings = this.configurator
				.loadZonesConfiguration();
		for (ZoneSettings settings : zoneSettings) {
			this.logger
					.info(

					String.format(

							"%n%n%s%n >> Zone: %s %n%s%n",
							new Object[] {
									"::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::",
									settings.name,
									"::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::" }));

			addZone(createZone(settings));
		}
		//activateTrafficMonitors();
	}

	public void toggleZone(String name, boolean isActive) {
		Zone theZone = getZoneByName(name);
		theZone.setActive(isActive);
	}

//	public ITrafficMeter getZoneTrafficMeter(String zoneName) {
//		return (ITrafficMeter) this.trafficMonitors.get(zoneName);
//	}
//
//	private void activateTrafficMonitors() {
//		for (Zone zone : this.zones.values()) {
//			this.trafficMonitors
//					.put(zone.getName(), new ZoneTrafficMeter(zone));
//		}
//		this.sfs.getTaskScheduler().scheduleAtFixedRate(
//
//		this.trafficMeterExecutor, 0, 5, TimeUnit.MINUTES);
//	}

	public Zone createZone(ZoneSettings settings) throws OSException {
		Zone zone = new Zone(settings.name);
		zone.setId(settings.getId());

		zone.setCustomLogin(settings.isCustomLogin);
		zone.setForceLogout(settings.isForceLogout);
		zone.setFilterUserNames(settings.isFilterUserNames);
		zone.setFilterRoomNames(settings.isFilterRoomNames);
		zone.setFilterPrivateMessages(settings.isFilterPrivateMessages);
		zone.setFilterBuddyMessages(settings.isFilterBuddyMessages);
		zone.setGuestUserAllowed(settings.allowGuestUsers);
		zone.setGuestUserNamePrefix(settings.guestUserNamePrefix);
		zone.setMaxAllowedRooms(settings.maxRooms);

		zone.setMaxAllowedUsers(settings.maxUsers);
		zone.setMaxUserVariablesAllowed(settings.maxUserVariablesAllowed);
		zone.setMaxRoomVariablesAllowed(settings.maxRoomVariablesAllowed);
		zone.setMinRoomNameChars(settings.minRoomNameChars);
		zone.setMaxRoomNameChars(settings.maxRoomNameChars);
		zone.setMaxRoomsCreatedPerUserLimit(settings.maxRoomsCreatedPerUser);
		//zone.setDefaultPlayerIdGeneratorClassName(settings.defaultPlayerIdGeneratorClass);
		//zone.setUserCountChangeUpdateInterval(settings.userCountChangeUpdateInterval);
		zone.setUserReconnectionSeconds(settings.userReconnectionSeconds);

		int theZoneIdleTime = this.sfs.getConfigurator().getServerSettings().userMaxIdleTime;
		if (settings.overrideMaxUserIdleTime > 0) {
			if (settings.overrideMaxUserIdleTime >= this.sfs.getConfigurator()
					.getServerSettings().sessionMaxIdleTime) {
				theZoneIdleTime = settings.overrideMaxUserIdleTime;
			} else {
				this.logger
						.warn(

						String.format(

								"%s - Could not override maxUserIdleTime. The provided value (%s sec) is < sessionMaxIdleTime (%s sec). You must provide a value > sessionMaxIdleTime. Please double check your configuration.",
								new Object[] {
										zone,
										Integer.valueOf(settings.overrideMaxUserIdleTime),
										Integer.valueOf(this.sfs
												.getConfigurator()
												.getServerSettings().sessionMaxIdleTime) }));
			}
		}
		zone.setMaxUserIdleTime(theZoneIdleTime);

		List<String> defaultRoomGroups = null;
		if (settings.defaultRoomGroups != null) {
			String[] defaultGroups = settings.defaultRoomGroups.split("\\,");
			defaultRoomGroups = Arrays.asList(defaultGroups);
		} else {
			defaultRoomGroups = new ArrayList();
		}
		if (defaultRoomGroups.size() == 0) {
			defaultRoomGroups.add("default");
		}
		zone.setDefaultGroups(defaultRoomGroups);

		List<String> publicRoomGroups = null;
		if (settings.publicRoomGroups != null) {
			String[] publicGroups = settings.publicRoomGroups.split("\\,");
			publicRoomGroups = Arrays.asList(publicGroups);
		} else {
			publicRoomGroups = new ArrayList();
		}
		if (publicRoomGroups.size() == 0) {
			publicRoomGroups.add("default");
		}
		zone.setPublicGroups(publicRoomGroups);

		zone.setZoneManager(this);
		for (String eventName : settings.disabledSystemEvents) {
			zone.addDisabledSystemEvent(eventName);
		}
		
//		configureWordsFilter(zone, settings.wordsFilter);
//
//		configureFloodFilter(zone, settings.floodFilter);
//
//		configureZonePermissions(zone, settings.privilegeManager);
//		if (settings.databaseManager != null) {
//			configureDBManager(zone, settings.databaseManager);
//		}
//		configureBuddyListManager(zone, settings.buddyList);
		
		for (ZoneSettings.RoomSettings roomSettings : settings.rooms) {
			try {
				createRoom(zone, roomSettings);
			} catch (OSException e) {
				this.logger.warn("Error while creating Room: "
						+ roomSettings.name + " -> " + e.getMessage());
			}
		}
//		if ((settings.extension != null) && (settings.extension.name != null)
//				&& (settings.extension.name.length() > 0)) {
//			try {
//				this.sfs.getExtensionManager().createExtension(
//						settings.extension, ExtensionLevel.ZONE, zone, null);
//			} catch (SFSExtensionException err) {
//				String extName = settings.extension.name == null ? "{Unknown}"
//						: settings.extension.name;
//
//				throw new SFSException("Extension creation failure: " + extName
//						+ " - " + err.getMessage());
//			}
//		}
		zone.setActive(true);

		return zone;
	}
//
//	private void configureWordsFilter(Zone zone,
//			ZoneSettings.WordFilterSettings settings) {
//		IWordFilter wordFilter = zone.getWordFilter();
//		wordFilter.setBanDurationMinutes(settings.banDuration);
//		wordFilter.setBanMessage(settings.banMessage);
//		wordFilter.setBanMode(BanMode.fromString(settings.banMode));
//		wordFilter.setBannedUserManager(this.sfs.getBannedUserManager());
//		wordFilter.setFilterMode(WordsFilterMode
//				.fromString(settings.filterMode));
//
//		wordFilter.setUseWarnings(settings.useWarnings);
//		wordFilter.setWarningMessage(settings.warningMessage);
//		wordFilter.setKickMessage(settings.kickMessage);
//		wordFilter.setKicksBeforeBan(settings.kicksBeforeBan);
//		wordFilter.setKicksBeforeBanMinutes(settings.kicksBeforeBanMinutes);
//		wordFilter.setMaskCharacter(settings.hideBadWordWithCharacter);
//		wordFilter.setMaxBadWordsPerMessage(settings.maxBadWordsPerMessage);
//		wordFilter.setName(zone.getName() + "-WordFilter");
//		wordFilter.setSecondsBeforeBanOrKick(settings.secondsBeforeBanOrKick);
//		wordFilter.setWarningsBeforeKick(settings.warningsBeforeKick);
//		wordFilter.setWordsFile(settings.wordsFile);
//		wordFilter.setActive(settings.isActive);
//		wordFilter.init(null);
//	}
//
//	private void configureFloodFilter(Zone zone,
//			ZoneSettings.FloodFilterSettings settings) {
//		IFloodFilter floodFilter = zone.getFloodFilter();
//
//		floodFilter.setActive(settings.isActive);
//		floodFilter.setBanDurationMinutes(settings.banDurationMinutes);
//		floodFilter.setBanMessage(settings.banMessage);
//		floodFilter.setBanMode(BanMode.fromString(settings.banMode));
//		floodFilter.setLogFloodingAttempts(settings.logFloodingAttempts);
//		floodFilter.setMaxFloodingAttempts(settings.maxFloodingAttempts);
//		floodFilter.setSecondsBeforeBan(settings.secondsBeforeBan);
//		if (settings.requestFilters != null) {
//			for (ZoneSettings.RequestFilterSettings item : settings.requestFilters) {
//				floodFilter.addRequestFilter(
//						SystemRequest.valueOf(item.reqName),
//						item.maxRequestsPerSecond);
//			}
//		}
//	}
//
//	private void configureZonePermissions(Zone zone,
//			ZoneSettings.PrivilegeManagerSettings settings) {
//		PrivilegeManager privilegeManager = zone.getPrivilegeManager();
//		privilegeManager.setActive(settings.active);
//		if (settings.active) {
//			for (ZoneSettings.PermissionProfile profileSetting : settings.profiles) {
//				List<SystemRequest> deniedReq = new ArrayList();
//				for (String reqName : profileSetting.deniedRequests) {
//					deniedReq.add(SystemRequest.valueOf(reqName));
//				}
//				List<SystemRequest> deniedSysReq = new ArrayList();
//				Object sysFlags = new ArrayList();
//				if (profileSetting.deniedRequests != null) {
//					for (String sysReqName : profileSetting.deniedRequests) {
//						deniedSysReq.add(SystemRequest.valueOf(sysReqName));
//					}
//				}
//				if (profileSetting.permissionFlags != null) {
//					for (String flagName : profileSetting.permissionFlags) {
//						((List) sysFlags).add(SystemPermission
//								.valueOf(flagName));
//					}
//				}
//				privilegeManager.setPermissionProfile(
//
//				new SFSPermissionProfile(
//
//				profileSetting.id, profileSetting.name, deniedSysReq,
//						(List) sysFlags));
//			}
//		}
//	}
//
//	private void configureBuddyListManager(Zone zone,
//			ZoneSettings.BuddyListSettings settings) {
//		BuddyListManager buddyListManager = new SFSBuddyListManager(zone,
//				settings.active);
//		if (buddyListManager.isActive()) {
//			buddyListManager.setBuddyListMaxSize(settings.maxItemsPerList);
//			buddyListManager.setMaxBuddyVariables(settings.maxBuddyVariables);
//			buddyListManager
//					.setOfflineBuddyVariablesCacheSize(settings.offlineBuddyVariablesCacheSize);
//			buddyListManager
//					.setAllowOfflineBuddyVariables(settings.allowOfflineBuddyVariables);
//			buddyListManager.setBuddyStates(settings.buddyStates);
//			buddyListManager.setUseTempBuddies(settings.useTempBuddies);
//			buddyListManager
//					.setApplyBadWordsFilter(settings.badWordsFilter.isActive);
//
//			String customClass = null;
//			if ((settings.customStorageClass != null)
//					&& (settings.customStorageClass.length() > 0)) {
//				customClass = settings.customStorageClass;
//			} else {
//				customClass = "com.smartfoxserver.v2.buddylist.storage.FSBuddyStorage";
//			}
//			try {
//				Class<?> storageClass = Class.forName(customClass);
//				if (!BuddyStorage.class.isAssignableFrom(storageClass)) {
//					throw new SFSRuntimeException(
//							"Specified BuddyList Storage class: "
//									+ customClass
//									+ " does not implement the BuddyStorage interface");
//				}
//				buddyListManager.setStorageHandler((BuddyStorage) storageClass
//						.newInstance());
//
//				buddyListManager.init(null);
//			} catch (ClassNotFoundException e) {
//				throw new SFSRuntimeException("BuddyList storage class: "
//						+ customClass + " was not found.");
//			} catch (InstantiationException e) {
//				throw new SFSRuntimeException("BuddyList storage class: "
//						+ customClass + " could not be instantiated.");
//			} catch (IllegalAccessException e) {
//				throw new SFSRuntimeException(
//						"Illegal access for BuddyList storage class: "
//								+ customClass);
//			}
//		}
//		zone.setBuddyListManager(buddyListManager);
//	}
//
//	private void configureDBManager(Zone zone, DBConfig settings) {
//		IDBManager dbManager = new SFSDBManager(settings);
//		zone.setDBManager(dbManager);
//		dbManager.init(zone);
//	}
//
	public Room createRoom(Zone zone, ZoneSettings.RoomSettings roomSettings)
			throws OSException {
		boolean isMMO = (roomSettings.mmoSettings != null)
				&& (roomSettings.mmoSettings.isActive);
		CreateRoomSettings params;
		//CreateRoomSettings params;
		//if (!isMMO) {
			params = new CreateRoomSettings();
		//} else {
		//	params = new CreateMMORoomSettings();
		//}
		params.setName(roomSettings.name);
		params.setGroupId(roomSettings.groupId);
		params.setPassword(roomSettings.password);
		params.setAutoRemoveMode(SFSRoomRemoveMode.fromString(roomSettings.autoRemoveMode));
		params.setMaxUsers(roomSettings.maxUsers);
		params.setMaxSpectators(roomSettings.maxSpectators);
		params.setMaxVariablesAllowed(roomSettings.permissions.maxRoomVariablesAllowed);
		params.setDynamic(roomSettings.isDynamic);
		params.setGame(roomSettings.isGame);
		params.setHidden(roomSettings.isHidden);
		params.setUseWordsFilter(roomSettings.badWordsFilter.isActive);
//
//		Set<SFSRoomSettings> sfsRoomSettings = new HashSet();
//
//		String[] settings = (String[]) ArrayUtils.addAll(
//				roomSettings.permissions.flags.split("\\,"),
//				roomSettings.events.split("\\,"));
//		for (String item : settings) {
//			try {
//				sfsRoomSettings
//						.add(SFSRoomSettings.valueOf(item.toUpperCase()));
//			} catch (IllegalArgumentException argError) {
//				this.logger.warn("RoomSetting literal not found: " + item);
//			}
//		}
//		params.setRoomSettings(sfsRoomSettings);
//
//		List<RoomVariable> variables = new ArrayList();
//		for (ZoneSettings.RoomVariableDefinition varDef : roomSettings.roomVariables) {
//			Object sfsRoomVar = SFSRoomVariable.newFromStringLiteral(
//					varDef.name, varDef.type, varDef.value);
//			((RoomVariable) sfsRoomVar).setPrivate(varDef.isPrivate);
//			((RoomVariable) sfsRoomVar).setPersistent(varDef.isPersistent);
//			((RoomVariable) sfsRoomVar).setGlobal(varDef.isGlobal);
//			((RoomVariable) sfsRoomVar).setHidden(varDef.isHidden);
//
//			variables.add(sfsRoomVar);
//		}
//		params.setRoomVariables(variables);
//		if (isMMO) {
//			Vec3D defaultAOI = MMOHelper
//					.stringToVec3D(roomSettings.mmoSettings.defaultAOI);
//			Vec3D lowerMapLimit = MMOHelper
//					.stringToVec3D(roomSettings.mmoSettings.lowerMapLimit);
//			Vec3D higherMapLimit = MMOHelper
//					.stringToVec3D(roomSettings.mmoSettings.higherMapLimit);
//
//			CreateMMORoomSettings cmrs = (CreateMMORoomSettings) params;
//
//			cmrs.setDefaultAOI(defaultAOI);
//			cmrs.setUserMaxLimboSeconds(roomSettings.mmoSettings.userMaxLimboSeconds);
//			cmrs.setProximityListUpdateMillis(roomSettings.mmoSettings.proximityListUpdateMillis);
//			cmrs.setSendAOIEntryPoint(roomSettings.mmoSettings.sendAOIEntryPoint);
//			if ((lowerMapLimit != null) && (higherMapLimit != null)) {
//				cmrs.setMapLimits(new CreateMMORoomSettings.MapLimits(
//						lowerMapLimit, higherMapLimit));
//			}
//		}
		
//		Room room = this.sfs.getAPIManager().getSFSApi()
//				.createRoom(zone, params, null, false, null, false, false);
//		if ((roomSettings.extension != null)
//				&& (roomSettings.extension.name != null)
//				&& (roomSettings.extension.name.length() > 0)) {
//			try {
//				this.sfs.getExtensionManager()
//						.createExtension(roomSettings.extension,
//								ExtensionLevel.ROOM, zone, room);
//			} catch (SFSExtensionException err) {
//				String extName = roomSettings.extension.name == null ? "{Unknown}"
//						: roomSettings.extension.name;
//				throw new SFSException("Room Extension creation failure: "
//						+ extName + " - " + err.getMessage() + " - Room: "
//						+ room);
//			}
//		}
//		return room;
		
		return new Room("");
	}
//
//	private void populateTransientFields() {
//		this.logger = LoggerFactory.getLogger(getClass());
//		this.sfs = SmartFoxServer.getInstance();
//		this.configurator = this.sfs.getConfigurator();
//	}
}
