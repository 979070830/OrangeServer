package com.orange.entities.managers;

import it.gotoandplay.util.launcher.BootException;
import it.gotoandplay.util.launcher.IClassLoader;
import it.gotoandplay.util.launcher.JarLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.config.ZoneSettings;
import com.orange.core.ISFSEvent;
import com.orange.core.SFSEventParam;
import com.orange.core.SFSEventType;
import com.orange.core.SFSSystemEvent;
import com.orange.entities.Room;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.entities.extensions.BaseExtension;
import com.orange.entities.extensions.ExtensionLevel;
import com.orange.entities.extensions.ExtensionReloadMode;
import com.orange.entities.extensions.ExtensionType;
import com.orange.entities.extensions.ISFSEventListener;
import com.orange.exceptions.ExceptionMessageComposer;
import com.orange.exceptions.OSException;
import com.orange.server.OrangeServerEngine;

public class ExtensionManager implements ISFSEventListener{
	private static final String JAR_EXTENSION = "jar";
	private static final String JS_EXTENSION = "js";
	private static final String PY_EXTENSION = "py";
	private final ConcurrentMap<Zone, BaseExtension> extensionsByZone;
	private final ConcurrentMap<Room, BaseExtension> extensionsByRoom;
	private final Map<Room, Map<SFSEventType, Set<ISFSEventListener>>> listenersByRoom;
	private final Map<Zone, Map<SFSEventType, Set<ISFSEventListener>>> listenersByZone;
	private final Logger logger;
	private OrangeServerEngine sfs;
	private final IClassLoader jarLoader;
	//private final LoginErrorHandler loginErrorHandler;
	private FileSystemManager fsManager;
	private DefaultFileMonitor extensionFileMonitor;
	public FileListener extensionFileListener;
	private EventManager eventManager;
	private boolean vfsFailed = false;
	private volatile boolean extMonitorActive = false;

	private final class ExtensionFileChangeListener
	implements FileListener
	{
		private ExtensionFileChangeListener() {}

		public void fileChanged(FileChangeEvent fileEvent)
				throws Exception
				{
					String changedFileExtension = fileEvent.getFile().getName().getExtension();
					if ((changedFileExtension.equalsIgnoreCase("jar")) || (changedFileExtension.equalsIgnoreCase("py")))
					{
						String extName = fileEvent.getFile().getName().getParent().getBaseName();
						List<BaseExtension> reloadableExtensions = ExtensionManager.this.findZoneExtensionByName(extName);
						if (reloadableExtensions.size() > 0) {
							for (BaseExtension theExtension : reloadableExtensions) {
								if ((theExtension != null) && (theExtension.getReloadMode() == ExtensionReloadMode.AUTO)) {
									ExtensionManager.this.reloadExtension(theExtension);
								}
							}
						}
					}
				}

		public void fileCreated(FileChangeEvent fileEvent)
				throws Exception
				{

				}

		public void fileDeleted(FileChangeEvent fileEvent)
				throws Exception
				{
			
				}
	}

	public ExtensionManager()
	{
		this.logger = LoggerFactory.getLogger(getClass());
		this.jarLoader = new JarLoader();

		this.extensionsByZone = new ConcurrentHashMap();
		this.extensionsByRoom = new ConcurrentHashMap();

		this.listenersByRoom = new ConcurrentHashMap();
		this.listenersByZone = new ConcurrentHashMap();

		//this.loginErrorHandler = new LoginErrorHandler();
	}

	public boolean isExtensionMonitorActive()
	{
		return this.extMonitorActive;
	}

	public void setExtensionMonitorActive(boolean flag)
	{
		if ((this.vfsFailed) && (flag))
		{
			this.logger.warn("Cannot activate Extension files monitoring services. Initialization failed at server boot. Check your logs.");
			return;
		}
		this.extMonitorActive = flag;
		if (this.extMonitorActive)
		{
			this.extensionFileMonitor.start();
			this.logger.debug("Extension File Monitor started");
		}
		else
		{
			this.extensionFileMonitor.stop();
			this.logger.debug("Extension File Monitor stopped");
		}
	}

	public synchronized void activateAllExtensions()
	{
		for (BaseExtension extension : this.extensionsByRoom.values()) {
			extension.setActive(true);
		}
		for (BaseExtension extension : this.extensionsByZone.values()) {
			extension.setActive(true);
		}
	}

	public void addExtension(BaseExtension extension)
	{
		if (extension.getLevel() == ExtensionLevel.ZONE) {
			this.extensionsByZone.put(extension.getParentZone(), extension);
		} else if (extension.getLevel() == ExtensionLevel.ROOM) {
			this.extensionsByRoom.put(extension.getParentRoom(), extension);
		}
	}

	public BaseExtension getRoomExtension(Room room)
	{
		return (BaseExtension)this.extensionsByRoom.get(room);
	}

	public BaseExtension getZoneExtension(Zone zone)
	{
		return (BaseExtension)this.extensionsByZone.get(zone);
	}

	private List<BaseExtension> findZoneExtensionByName(String extName)
	{
		List<BaseExtension> extensions = new ArrayList();
		for (BaseExtension ext : this.extensionsByZone.values()) {
			if (extName.equals(ext.getName())) {
				extensions.add(ext);
			}
		}
		return extensions;
	}

	public void createExtension(ZoneSettings.ExtensionSettings settings, ExtensionLevel level, Zone parentZone, Room parentRoom)
			throws OSException
			{
		if ((settings.file == null) || (settings.file.length() == 0)) {
			throw new OSException("Extension file parameter is missing!");
		}
		if ((settings.name == null) || (settings.name.length() == 0)) {
			throw new OSException("Extension name parameter is missing!");
		}
		if (settings.type == null) {
			throw new OSException("Extension type was not specified: " + settings.name);
		}
		if (settings.reloadMode == null) {
			settings.reloadMode = "";
		}
		ExtensionReloadMode reloadMode = ExtensionReloadMode.valueOf(settings.reloadMode.toUpperCase());
		if (reloadMode == null) {
			reloadMode = ExtensionReloadMode.MANUAL;
		}
		ExtensionType extensionType = ExtensionType.valueOf(settings.type.toUpperCase());
		BaseExtension extension = null;
		if (extensionType == ExtensionType.JAVA)
		{
			extension = createJavaExtension(settings);
		}
		else
		{
			//	      if (extensionType == ExtensionType.JAVASCRIPT)
			//	      {
			//	        extension = createJSExtension(settings);
			//	      }
			//	      else
			//	      {
			//	        BaseExtension extension;
			//	        if (extensionType == ExtensionType.PYTHON) {
			//	          extension = createPYExtension(settings);
			//	        } else {
			//	          throw new OSException("Extension type not supported: " + extensionType);
			//	        }
			//	      }
		}
		extension.setLevel(level);
		extension.setName(settings.name);
		extension.setExtensionFileName(settings.file);
		extension.setReloadMode(reloadMode);
		extension.setParentZone(parentZone);
		extension.setParentRoom(parentRoom);
		try
		{
			if (settings.propertiesFile != null) {
				if ((settings.propertiesFile.startsWith("../")) || (settings.propertiesFile.startsWith("/"))) {
					throw new OSException("Illegal path for Extension property file. File path outside the extensions/ folder is not valid: " + settings.propertiesFile);
				}
			}
			extension.setPropertiesFileName(settings.propertiesFile);
		}
		catch (IOException e)
		{
			throw new OSException("Unable to load extension properties file: " + settings.propertiesFile);
		}
		try
		{
			extension.init();


			addExtension(extension);
			if (parentRoom != null) {
				parentRoom.setExtension(extension);
			} else {
				parentZone.setExtension(extension);
			}
		}
		catch (Exception err)
		{
			ExceptionMessageComposer msg = new ExceptionMessageComposer(err);
			msg.setDescription("Extension initialization failed.");
			this.logger.error(msg.toString());
		}
			}

	private BaseExtension createJavaExtension(ZoneSettings.ExtensionSettings settings)
			throws OSException
			{
		BaseExtension extension;
		try
		{
			String extensionPath = "extensions/" + settings.name;


			ClassLoader extensionClassLoader = this.jarLoader.loadClasses(new String[] { extensionPath }, getClass().getClassLoader());


			Class<?> extensionClass = extensionClassLoader.loadClass(settings.file);
			if (!BaseExtension.class.isAssignableFrom(extensionClass)) {
				throw new OSException("Extension does not implement BaseExtension interface: " + settings.name);
			}
			extension = (BaseExtension)extensionClass.newInstance();
			extension.setType(ExtensionType.JAVA);
		}
		catch (BootException e)
		{
			throw new OSException("Extension boot error. " + e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			throw new OSException("Illegal access while instantiating class: " + settings.file);
		}
		catch (InstantiationException e)
		{
			throw new OSException("Cannot instantiate class: " + settings.file);
		}
		catch (ClassNotFoundException e)
		{
			throw new OSException("Class not found: " + settings.file);
		}
		return extension;
			}

	//	  private BaseExtension createJSExtension(ZoneSettings.ExtensionSettings settings)
	//	  {
	//	    BaseExtension extension = new JavascriptExtension();
	//	    extension.setType(ExtensionType.JAVASCRIPT);
	//	    
	//	    return extension;
	//	  }
	//	  
	//	  private BaseExtension createPYExtension(ZoneSettings.ExtensionSettings settings)
	//	  {
	//	    BaseExtension extension = new PythonExtension();
	//	    extension.setType(ExtensionType.PYTHON);
	//	    
	//	    return extension;
	//	  }

	public synchronized void deactivateAllExtensions()
	{
		for (BaseExtension extension : this.extensionsByRoom.values()) {
			extension.setActive(false);
		}
		for (BaseExtension extension : this.extensionsByZone.values()) {
			extension.setActive(false);
		}
	}

	public void destroyExtension(BaseExtension extension)
	{
		try
		{
			extension.destroy();
		}
		finally
		{
			if (extension.getLevel() == ExtensionLevel.ROOM) {
				this.extensionsByRoom.remove(extension.getParentRoom());
			} else {
				this.extensionsByZone.remove(extension.getParentZone());
			}
			this.logger.debug("Removed: " + extension);
		}
	}

	public List<BaseExtension> getExtensions()
	{
		List<BaseExtension> allOfThem = new ArrayList(this.extensionsByRoom.values());
		allOfThem.addAll(this.extensionsByZone.values());

		return allOfThem;
	}

	public int getExtensionsCount()
	{
		return this.extensionsByRoom.size() + this.extensionsByZone.size();
	}

	public void init()
	{
		this.sfs = OrangeServerEngine.getInstance();
		this.eventManager = this.sfs.getEventManager();

		initializeExtensionFileMonitoring();
		for (SFSEventType type : SFSEventType.values()) {
			this.eventManager.addEventListener(type, this);
		}
		this.logger.debug("Extension Manager started.");
	}

	public void destroy()
	{
		for (SFSEventType type : SFSEventType.values()) {
			this.eventManager.removeEventListener(type, this);
		}
		this.listenersByRoom.clear();
		this.listenersByZone.clear();
		for (BaseExtension extension : this.extensionsByRoom.values()) {
			extension.destroy();
		}
		for (BaseExtension extension : this.extensionsByZone.values()) {
			extension.destroy();
		}
		this.extensionsByRoom.clear();
		this.extensionsByZone.clear();

		this.logger.debug("Extension Manager stopped.");
	}

	private void initializeExtensionFileMonitoring()
	{
		try
		{
//			System.out.println("设置路径");
//			System.setProperty("java.io.tmpdir","logs/");

			this.fsManager = VFS.getManager();


			File extFolder = new File("extensions/");
			FileObject directoryToWatch = this.fsManager.resolveFile(extFolder.getAbsolutePath());


			this.extensionFileListener = new ExtensionFileChangeListener();


			this.extensionFileMonitor = new DefaultFileMonitor(this.extensionFileListener);
			this.extensionFileMonitor.setRecursive(true);
			this.extensionFileMonitor.addFile(directoryToWatch);
		}
		catch (FileSystemException e)
		{
			e.printStackTrace();
			this.vfsFailed = true;

			ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
			composer.setDescription("Failed activating extension file monitoring services.");
			composer.setPossibleCauses("You might need to adjust the permissions for the extensions folder");

			this.logger.warn(composer.toString());
		}
	}

	public void reloadExtension(BaseExtension extension)
	{
		this.logger.info("Reloading extension: " + extension);

		ZoneSettings.ExtensionSettings newSettings = new ZoneSettings.ExtensionSettings();
		newSettings.file = extension.getExtensionFileName();
		newSettings.name = extension.getName();
		newSettings.propertiesFile = extension.getPropertiesFileName();
		newSettings.reloadMode = extension.getReloadMode().toString();
		newSettings.type = extension.getType().toString();
		try
		{
			createExtension(

					newSettings, 
					ExtensionLevel.ZONE, 
					extension.getParentZone(), 
					extension.getParentRoom());







			extension.destroy();
		}
		catch (Throwable t)
		{
			ExceptionMessageComposer composer = new ExceptionMessageComposer(t);
			composer.setDescription("An error occurred while reloading extension: " + extension.getName() + " in " + extension.getParentZone());
			composer.addInfo("The new extension might not function properly.");

			this.logger.error(composer.toString());
		}
	}

	public void reloadRoomExtension(String extName, Room room)
	{
		throw new UnsupportedOperationException("Sorry, this feature is not implemented yet.");
	}

	public void reloadZoneExtension(String extName, Zone zone)
	{
		BaseExtension extension = (BaseExtension)this.extensionsByZone.get(zone);
		if (extension != null) {
			reloadExtension(extension);
		} else {
			this.logger.warn(

					String.format("Could not find extension to reload: %s, %s", new Object[] { extName, zone }));
		}
	}

	public synchronized void addZoneEventListener(SFSEventType type, ISFSEventListener listener, Zone zone)
	{
		Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByZone.get(zone);
		if (listenersByType == null)
		{
			listenersByType = new ConcurrentHashMap();
			this.listenersByZone.put(zone, listenersByType);
		}
		Set<ISFSEventListener> listeners = (Set)listenersByType.get(type);
		if (listeners == null)
		{
			listeners = new CopyOnWriteArraySet();
			listenersByType.put(type, listeners);
		}
		listeners.add(listener);
	}

	public synchronized void addRoomEventListener(SFSEventType type, ISFSEventListener listener, Room room)
	{
		Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByRoom.get(room);
		if (listenersByType == null)
		{
			listenersByType = new ConcurrentHashMap();
			this.listenersByRoom.put(room, listenersByType);
		}
		Set<ISFSEventListener> listeners = (Set)listenersByType.get(type);
		if (listeners == null)
		{
			listeners = new CopyOnWriteArraySet();
			listenersByType.put(type, listeners);
		}
		listeners.add(listener);
	}

	public void dispatchEvent(ISFSEvent event, ExtensionLevel level)
	{
		if (level == ExtensionLevel.GLOBAL) {
			dispatchGlobalEvent(event);
		} else if (level == ExtensionLevel.ZONE) {
			dispatchZoneLevelEvent(event);
		} else if (level == ExtensionLevel.ROOM) {
			dispatchRoomLevelEvent(event);
		}
	}

	private void dispatchGlobalEvent(ISFSEvent event)
	{
		List<ISFSEventListener> allListeners = new ArrayList();
		SFSEventType type = event.getType();
		for (Map<SFSEventType, Set<ISFSEventListener>> zoneListeners : this.listenersByZone.values())
		{
			Set<ISFSEventListener> listeners = (Set)zoneListeners.get(type);
			if (listeners != null) {
				allListeners.addAll(listeners);
			}
		}
		for (Map<SFSEventType, Set<ISFSEventListener>> roomListeners : this.listenersByRoom.values())
		{
			Set<ISFSEventListener> listeners = (Set)roomListeners.get(type);
			if (listeners != null) {
				allListeners.addAll(listeners);
			}
		}
		dispatchEvent(event, allListeners);
	}

	private void dispatchZoneLevelEvent(ISFSEvent event)
	{
		Zone zone = (Zone)event.getParameter(SFSEventParam.ZONE);
		if (zone != null)
		{
			Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByZone.get(zone);
			if (listenersByType != null)
			{
				Set<ISFSEventListener> listeners = (Set)listenersByType.get(event.getType());
				dispatchEvent(event, listeners);
			}
		}
		else
		{
			this.logger.info("Zone Event was not dispatched. ZONE param is null: " + event);
		}
	}

	private void dispatchRoomLevelEvent(ISFSEvent event, Room room)
	{
		if (room != null)
		{
			Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByRoom.get(room);
			if (listenersByType != null)
			{
				Set<ISFSEventListener> listeners = (Set)listenersByType.get(event.getType());
				dispatchEvent(event, listeners);
			}
		}
		else
		{
			this.logger.info("Room Event was not dispatched. ROOM param is null: " + event);
		}
	}

	private void dispatchRoomLevelEvent(ISFSEvent event)
	{
		Room room = (Room)event.getParameter(SFSEventParam.ROOM);
		dispatchRoomLevelEvent(event, room);
	}

	private void dispatchRoomLevelEvent(ISFSEvent event, List<Room> roomList)
	{
		if (roomList != null) {
			for (Room room : roomList)
			{
				Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByRoom.get(room);
				if (listenersByType != null)
				{
					Set<ISFSEventListener> listeners = (Set)listenersByType.get(event.getType());
					dispatchEvent(event, listeners);
				}
			}
		} else {
			this.logger.info("Multi Room Event was not dispatched. RoomList param is null: " + event);
		}
	}

	private void dispatchEvent(ISFSEvent event, Collection<ISFSEventListener> listeners)
	{
		if ((listeners != null) && (listeners.size() > 0)) {
			for (ISFSEventListener listener : listeners) {
				try
				{
					listener.handleServerEvent(event);
					if ((event instanceof SFSSystemEvent)) {
						executeEventCommand((SFSSystemEvent)event);
					}
				}
				//	        catch (WrappedException jsWrappedException)
				//	        {
				//	          Throwable t = jsWrappedException.getWrappedException();
				//	          if ((t instanceof SFSLoginException)) {
				//	            handleLoginException((SFSSystemEvent)event, (SFSLoginException)t);
				//	          } else {
				//	            throw jsWrappedException;
				//	          }
				//	        }
				//	        catch (SFSLoginException logErr)
				//	        {
				//	          handleLoginException((SFSSystemEvent)event, logErr);
				//	        }
				catch (Exception e)
				{
					ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
					composer.setDescription("Error during event handling: " + e + ", Listener: " + listener);

					this.logger.warn(composer.toString());
				}
			}
		}
	}

	public void removeListenerFromZone(ISFSEventListener listener, Zone zone)
	{
		Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByZone.get(zone);
		if (listenersByType != null) {
			for (Set<ISFSEventListener> listenersByEvent : listenersByType.values()) {
				listenersByEvent.remove(listener);
			}
		}
	}

	public void removeListenerFromRoom(ISFSEventListener listener, Room room)
	{
		Map<SFSEventType, Set<ISFSEventListener>> listenersByType = (Map)this.listenersByRoom.get(room);
		if (listenersByType != null)
		{
			this.listenersByRoom.remove(room);
			for (Set<ISFSEventListener> listenersByEvent : listenersByType.values()) {
				listenersByEvent.remove(listener);
			}
		}
	}

	public void removeZoneEventListener(SFSEventType type, ISFSEventListener listener, Zone zone)
	{
		removeEventListener((Map)this.listenersByZone.get(zone), type, listener);
	}

	public void removeRoomEventListener(SFSEventType type, ISFSEventListener listener, Room room)
	{
		removeEventListener((Map)this.listenersByRoom.get(room), type, listener);
	}

	private void removeEventListener(Map<SFSEventType, Set<ISFSEventListener>> listenersByType, SFSEventType type, ISFSEventListener listener)
	{
		if (listenersByType != null)
		{
			Set<ISFSEventListener> listeners = (Set)listenersByType.get(type);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

	public void handleServerEvent(ISFSEvent event)
	{
		SFSEventType type = event.getType();
		if (type == SFSEventType.SERVER_READY)
		{
			dispatchEvent(event, ExtensionLevel.GLOBAL);
		}
		else if (type == SFSEventType.USER_LOGIN)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.USER_JOIN_ZONE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.USER_LOGOUT)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.USER_JOIN_ROOM)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.USER_LEAVE_ROOM)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.ROOM_ADDED)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.ROOM_REMOVED)
		{
			Room theRoom = (Room)event.getParameter(SFSEventParam.ROOM);
			this.extensionsByRoom.remove(theRoom);

			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.USER_DISCONNECT)
		{
			dispatchZoneLevelEvent(event);


			dispatchRoomLevelEvent(event, (List)event.getParameter(SFSEventParam.JOINED_ROOMS));
		}
		else if (type == SFSEventType.USER_RECONNECTION_TRY)
		{
			dispatchZoneLevelEvent(event);


			User user = (User)event.getParameter(SFSEventParam.USER);
			dispatchRoomLevelEvent(event, user.getJoinedRooms());
		}
		else if (type == SFSEventType.USER_RECONNECTION_SUCCESS)
		{
			dispatchZoneLevelEvent(event);


			User user = (User)event.getParameter(SFSEventParam.USER);
			dispatchRoomLevelEvent(event, user.getJoinedRooms());
		}
		else if (type == SFSEventType.PUBLIC_MESSAGE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.PRIVATE_MESSAGE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.ROOM_VARIABLES_UPDATE)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.USER_VARIABLES_UPDATE)
		{
			dispatchZoneLevelEvent(event);

			User user = (User)event.getParameter(SFSEventParam.USER);
			if (user.getJoinedRooms().size() > 0) {
				for (Room joinedRoom : user.getJoinedRooms()) {
					dispatchRoomLevelEvent(event, joinedRoom);
				}
			}
		}
		else if (type == SFSEventType.SPECTATOR_TO_PLAYER)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.PLAYER_TO_SPECTATOR)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_ADD)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_BLOCK)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_LIST_INIT)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_MESSAGE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_ONLINE_STATE_UPDATE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_REMOVE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.BUDDY_VARIABLES_UPDATE)
		{
			dispatchZoneLevelEvent(event);
		}
		else if (type == SFSEventType.GAME_INVITATION_SUCCESS)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.GAME_INVITATION_FAILURE)
		{
			dispatchZoneLevelEvent(event);
			dispatchRoomLevelEvent(event);
		}
		else if (type == SFSEventType.__TRACE_MESSAGE)
		{
			dispatchZoneLevelEvent(event);
		}
	}

	private void executeEventCommand(SFSSystemEvent sysEvent)
			throws Exception
			{
		System.out.println("executeEventCommand"+sysEvent);
		//	    Class<?> commandClass = (Class)sysEvent.getSysParameter(SFSEventSysParam.NEXT_COMMAND);
		//	    IRequest request = (IRequest)sysEvent.getSysParameter(SFSEventSysParam.REQUEST_OBJ);
		//	    if ((commandClass != null) && (request != null))
			//	    {
			//	      IControllerCommand command = (IControllerCommand)commandClass.newInstance();
			//	      command.execute(request);
			//	    }
			}

	//	  private void handleLoginException(SFSSystemEvent event, SFSLoginException err)
	//	  {
		//	    this.logger.warn(err.toString());
	//	    ISession sender = ((IRequest)event.getSysParameter(SFSEventSysParam.REQUEST_OBJ)).getSender();
	//	    this.loginErrorHandler.execute(sender, err);
	//	  }
}
