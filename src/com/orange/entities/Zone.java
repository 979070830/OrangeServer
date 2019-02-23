package com.orange.entities;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.config.CreateRoomSettings;
import com.orange.entities.extensions.BaseExtension;
import com.orange.entities.managers.RoomManager;
import com.orange.entities.managers.UserManager;
import com.orange.entities.managers.ZoneManager;
import com.orange.exceptions.OSException;
import com.orange.server.OrangeServerEngine;

public final class Zone{
	  private List<String> disabledSystemEvents;
	  private List<String> publicGroups;
	  private List<String> defaultGroups;
	  private ZoneManager zoneManager;
	  private final RoomManager roomManager;
	  private final UserManager userManager;
	  //private IFloodFilter floodFilter;
	  //private IWordFilter wordFilter;
	  //private PrivilegeManager privilegeManager;
	  //private BuddyListManager buddyListManager;
	  //private IDBManager dbManager;
	  //private IResponseThrottler uCountResponseThrottler;
	  private volatile BaseExtension extension;
	  private volatile boolean isActive = false;
	  private boolean customLogin = false;
	  private boolean forceLogout = false;
	  private boolean clientAllowedToOverridRoomEvents = false;
	  private boolean guestUserAllowed = false;
	  private volatile boolean filterUserNames = false;
	  private volatile boolean filterRoomNames = false;
	  private volatile boolean filterPrivateMessages = false;
	  private volatile boolean filterBuddyMessages = false;
	  //private volatile Map<SystemRequest, ISystemFilterChain> filterChainByRequestId;
	  //private IAdminHelper adminHelper;
	  private volatile int maxAllowedRooms;
	  private volatile int maxAllowedUsers;
	  private volatile int maxAllowedUserVariables;
	  private volatile int maxAllowedRoomVariables;
	  private volatile int maxRoomsCreatedPerUser;
	  private volatile int userCountChangeUpdateInterval = 0;
	  private volatile int minRoomNameChars;
	  private volatile int maxRoomNameChars;
	  private volatile int userReconnectionSeconds = 0;
	  private int maxUserIdleTime = 0;
	  private final String name;
	  private String guestUserNamePrefix;
	  private String defaultPlayerIdGeneratorClass;
	  private ConcurrentMap<Object, Object> properties;
	  //private volatile Map<String, Set<SFSRoomEvents>> groupEvents;
	  private Logger logger;
	  private OrangeServerEngine sfs;
	  private int id = -1;
	  //private IRoomStorage roomStorage;
	  
	  public Zone(String name)
	  {
	    this.name = name;
	    this.logger = LoggerFactory.getLogger(getClass());
	    this.sfs = OrangeServerEngine.getInstance();
	    
	    this.roomManager = new RoomManager();
	    this.roomManager.setOwnerZone(this);
	    
	    this.userManager = new UserManager();
	    this.userManager.setOwnerZone(this);
	    
	    this.disabledSystemEvents = new ArrayList();
	    this.publicGroups = new ArrayList();
	    this.properties = new ConcurrentHashMap();
	    

	    //this.roomManager.addGroup("default");
	    this.publicGroups.add("default");
	    

//	    this.privilegeManager = new SFSPrivilegeManager();
//	    
//
//	    this.groupEvents = new ConcurrentHashMap();
//	    
//
//	    this.floodFilter = new SFSFloodFilter(this.sfs.getBannedUserManager());
//	    
//
//	    this.wordFilter = new SFSWordFilter(this.sfs.getBannedUserManager());
	  }
	  
	  public UserManager getUserManager()
	  {
	    return this.userManager;
	  }
	  
	  public boolean containsGroup(String groupId)
	  {
	    return this.roomManager.containsGroup(groupId);
	  }
	  
	  public boolean containsPublicGroup(String groupId)
	  {
	    boolean flag = false;
	    synchronized (this.publicGroups)
	    {
	      flag = this.publicGroups.contains(groupId);
	    }
	    return flag;
	  }
	  
	  public Room createRoom(CreateRoomSettings params, User user)
	    throws OSException
	  {
	    return this.roomManager.createRoom(params, user);
	  }
	  
	  public Room createRoom(CreateRoomSettings params)
	    throws OSException
	  {
	    return this.roomManager.createRoom(params);
	  }
	  
	  public void changeRoomName(Room room, String newName)
	    throws OSException
	  {
	    this.roomManager.changeRoomName(room, newName);
	  }
	  
	  public void changeRoomPasswordState(Room room, String password)
	  {
	    this.roomManager.changeRoomPasswordState(room, password);
	  }
	  
	  public void changeRoomCapacity(Room room, int newMaxUsers, int newMaxSpect)
	  {
	    this.roomManager.changeRoomCapacity(room, newMaxUsers, newMaxSpect);
	  }
	  
	  public void addDisabledSystemEvent(String eventID)
	  {
	    synchronized (this.disabledSystemEvents)
	    {
	      this.disabledSystemEvents.add(eventID);
	    }
	  }
	  
	  public void addRoom(Room room)
	    throws OSException
	  {
	    this.roomManager.addRoom(room);
	  }
	  
	  public String getGuestUserNamePrefix()
	  {
	    return this.guestUserNamePrefix;
	  }
	  
	  public int getUserCount()
	  {
	    return this.userManager.getUserCount();
	  }
	  
	  public int getTotalRoomCount()
	  {
	    return this.roomManager.getTotalRoomCount();
	  }
	  
	  public int getGameRoomCount()
	  {
	    return this.roomManager.getGameRoomCount();
	  }
	  
	  public int getMaxAllowedRooms()
	  {
	    return this.maxAllowedRooms;
	  }
	  
	  public int getMaxUserVariablesAllowed()
	  {
	    return this.maxAllowedUserVariables;
	  }
	  
	  public int getMaxAllowedUsers()
	  {
	    return this.maxAllowedUsers;
	  }
	  
	  public int getMaxRoomsCreatedPerUserLimit()
	  {
	    return this.maxRoomsCreatedPerUser;
	  }
	  
	  public String getName()
	  {
	    return this.name;
	  }
	  
	  public int getId()
	  {
	    return this.id;
	  }
	  
	  public Object getProperty(Object key)
	  {
	    return this.properties.get(key);
	  }
	  
	  public void removeProperty(Object key)
	  {
	    this.properties.remove(key);
	  }
	  
	  public List<String> getPublicGroups()
	  {
	    List<String> newList = null;
	    synchronized (this.publicGroups)
	    {
	      newList = new ArrayList(this.publicGroups);
	    }
	    return newList;
	  }
	  
	  public List<String> getGroups()
	  {
	    return this.roomManager.getGroups();
	  }
	  
	  public List<String> getDefaultGroups()
	  {
	    return new ArrayList(this.defaultGroups);
	  }
	  
	  public Room getRoomById(int id)
	  {
	    return this.roomManager.getRoomById(id);
	  }
	  
	  public Room getRoomByName(String name)
	  {
	    return this.roomManager.getRoomByName(name);
	  }
	  
	  public List<Room> getRoomList()
	  {
	    return this.roomManager.getRoomList();
	  }
	  
	  public List<Room> getRoomListFromGroup(String groupId)
	  {
	    return this.roomManager.getRoomListFromGroup(groupId);
	  }
	  
	  public User getUserById(int id)
	  {
	    return this.userManager.getUserById(id);
	  }
	  
	  public User getUserByName(String name)
	  {
	    return this.userManager.getUserByName(name);
	  }
	  
	  public User getUserBySession(ChannelHandlerContext session)
	  {
	    return this.userManager.getUserBySession(session);
	  }
	  
	  public int getUserCountChangeUpdateInterval()
	  {
	    return this.userCountChangeUpdateInterval;
	  }
	  
//	  public IResponseThrottler getUCountThrottler()
//	  {
//	    return this.uCountResponseThrottler;
//	  }
//	  
//	  public String getDefaultPlayerIdGeneratorClassName()
//	  {
//	    return this.defaultPlayerIdGeneratorClass;
//	  }
//	  
//	  public void setDefaultPlayerIdGeneratorClassName(String className)
//	  {
//	    if ((className == null) || (className.length() == 0)) {
//	      className = "com.smartfoxserver.v2.util.DefaultPlayerIdGenerator";
//	    }
//	    this.defaultPlayerIdGeneratorClass = className;
//	    
//	    Class<? extends IPlayerIdGenerator> playerGeneratorClass = DefaultPlayerIdGenerator.class;
//	    try
//	    {
//	      playerGeneratorClass = Class.forName(className);
//	    }
//	    catch (ClassNotFoundException e)
//	    {
//	      this.logger.warn("Was not able to instantiate PlayerIdGenerator Class: " + className + ", class is not found. Reverting to default implementation: " + playerGeneratorClass);
//	    }
//	    this.roomManager.setDefaultRoomPlayerIdGeneratorClass(playerGeneratorClass);
//	  }
//	  
//	  public boolean isFilterChainInited()
//	  {
//	    return this.filterChainByRequestId != null;
//	  }
//	  
//	  public void resetSystemFilterChain()
//	  {
//	    if (isFilterChainInited())
//	    {
//	      this.filterChainByRequestId.clear();
//	      this.filterChainByRequestId = null;
//	    }
//	  }
//	  
//	  public ISystemFilterChain getFilterChain(SystemRequest requestId)
//	  {
//	    if (this.filterChainByRequestId == null) {
//	      return null;
//	    }
//	    return (ISystemFilterChain)this.filterChainByRequestId.get(requestId);
//	  }
//	  
//	  public void setFilterChain(SystemRequest requestId, ISystemFilterChain chain)
//	  {
//	    if (this.filterChainByRequestId == null) {
//	      this.filterChainByRequestId = new ConcurrentHashMap();
//	    }
//	    this.filterChainByRequestId.put(requestId, chain);
//	  }
//	  
	  public BaseExtension getExtension()
	  {
	    return this.extension;
	  }
	  
	  public void setExtension(BaseExtension extension)
	  {
	    this.extension = extension;
	  }
	  
	  public int getUserReconnectionSeconds()
	  {
	    return this.userReconnectionSeconds;
	  }
	  
	  public void setUserReconnectionSeconds(int seconds)
	  {
	    this.userReconnectionSeconds = seconds;
	  }
	  
	  public int getMaxUserIdleTime()
	  {
	    return this.maxUserIdleTime;
	  }
	  
	  public void setMaxUserIdleTime(int seconds)
	  {
	    this.maxUserIdleTime = seconds;
	  }
	  
	  public Collection<User> getUsersInGroup(String groupId)
	  {
	    Set<User> userList = new HashSet();
	    for (Room room : this.roomManager.getRoomListFromGroup(groupId)) {
	      userList.addAll(room.getUserList());
	    }
	    return userList;
	  }
	  
	  public Collection<ChannelHandlerContext> getSessionsInGroup(String groupId)
	  {
	    Set<ChannelHandlerContext> sessionList = new HashSet();
	    for (Room room : this.roomManager.getRoomListFromGroup(groupId)) {
	      sessionList.addAll(room.getSessionList());
	    }
	    return sessionList;
	  }
	  
	  public Collection<ChannelHandlerContext> getSessionsListeningToGroup(String groupId)
	  {
	    Set<ChannelHandlerContext> sessionList = new HashSet();
	    List<User> allUsers = this.userManager.getAllUsers();
	    for (User user : allUsers) {
	      if (user.isSubscribedToGroup(groupId)) {
	        sessionList.add(user.getSession());
	      }
	    }
	    return sessionList;
	  }
	  
	  public Collection<ChannelHandlerContext> getSessionList()
	  {
	    return this.userManager.getAllSessions();
	  }
	  
	  public Collection<User> getUserList()
	  {
	    return this.userManager.getAllUsers();
	  }
	  
	  public ZoneManager getZoneManager()
	  {
	    return this.zoneManager;
	  }
	  
	  public boolean isActive()
	  {
	    return this.isActive;
	  }
	  
	  /**
	   * @deprecated
	   */
	  public boolean isClientAllowedToOverridRoomEvents()
	  {
	    return this.clientAllowedToOverridRoomEvents;
	  }
	  
	  public boolean isCustomLogin()
	  {
	    return this.customLogin;
	  }
	  
	  public boolean isForceLogout()
	  {
	    return this.forceLogout;
	  }
	  
	  public boolean isGuestUserAllowed()
	  {
	    return this.guestUserAllowed;
	  }
	  
	  public boolean isSystemEventAllowed(String eventID)
	  {
	    boolean flag = false;
	    synchronized (this.disabledSystemEvents)
	    {
	      flag = !this.disabledSystemEvents.contains(eventID);
	    }
	    return flag;
	  }
	  
	  public boolean isFilterUserNames()
	  {
	    return this.filterUserNames;
	  }
	  
	  public boolean isFilterRoomNames()
	  {
	    return this.filterRoomNames;
	  }
	  
	  public void setFilterRoomNames(boolean flag)
	  {
	    this.filterRoomNames = flag;
	  }
	  
	  public boolean isFilterBuddyMessages()
	  {
	    return this.filterBuddyMessages;
	  }
	  
	  public void setFilterBuddyMessages(boolean flag)
	  {
	    this.filterBuddyMessages = flag;
	  }
	  
	  public boolean containsProperty(Object key)
	  {
	    return this.properties.containsKey(key);
	  }
	  
//	  public void registerEventsForRoomGroup(String groupId, Set<SFSRoomEvents> flags)
//	  {
//	    if (this.roomManager.containsGroup(groupId)) {
//	      this.groupEvents.put(groupId, flags);
//	    } else {
//	      this.logger.warn("Cannot register events for room group: " + groupId + ". Group doesn't exists");
//	    }
//	  }
//	  
//	  public boolean isGroupEventSet(String groupId, SFSRoomEvents eventToCheck)
//	  {
//	    boolean res = false;
//	    Set<SFSRoomEvents> events = (Set)this.groupEvents.get(groupId);
//	    if (events != null) {
//	      res = events.contains(eventToCheck);
//	    }
//	    return res;
//	  }
//	  
//	  public Set<SFSRoomEvents> getGroupEvents(String groupId)
//	  {
//	    return Collections.unmodifiableSet((Set)this.groupEvents.get(groupId));
//	  }
	  
	  public void removeDisabledSystemEvent(String eventID)
	  {
	    synchronized (this.disabledSystemEvents)
	    {
	      this.disabledSystemEvents.remove(eventID);
	    }
	  }
	  
	  public void removeRoom(int roomId)
	  {
	    this.roomManager.removeRoom(roomId);
	  }
	  
	  public void removeRoom(String name)
	  {
	    this.roomManager.removeRoom(name);
	  }
	  
	  public void removeRoom(Room room)
	  {
	    this.roomManager.removeRoom(room);
	  }
	  
	  public void checkAndRemove(Room room)
	  {
	    this.roomManager.checkAndRemove(room);
	  }
	  
	  public void removeUserFromRoom(User user, Room room)
	  {
	    this.roomManager.removeUser(user, room);
	  }
	  
	  public int getMinRoomNameChars()
	  {
	    return this.minRoomNameChars;
	  }
	  
	  public void setMinRoomNameChars(int minRoomNameChars)
	  {
	    this.minRoomNameChars = minRoomNameChars;
	  }
	  
	  public int getMaxRoomNameChars()
	  {
	    return this.maxRoomNameChars;
	  }
	  
	  public void setMaxRoomNameChars(int maxRoomNameChars)
	  {
	    this.maxRoomNameChars = maxRoomNameChars;
	  }
	  
	  public void setActive(boolean flag)
	  {
	    if ((!flag) && (this.isActive)) {
	      removeAllUsers();
	    }
	    this.isActive = flag;
	  }
	  
	  public void setId(int id)
	  {
	    if (this.id != -1) {
	      throw new IllegalStateException("ID is already assigned = " + this.id);
	    }
	    this.id = id;
	  }
	  
	  /**
	   * @deprecated
	   */
	  public void setClientAllowedToOverridRoomEvents(boolean flag)
	  {
	    this.clientAllowedToOverridRoomEvents = flag;
	  }
	  
	  public void setCustomLogin(boolean flag)
	  {
	    this.customLogin = flag;
	  }
	  
	  public void setForceLogout(boolean flag)
	  {
	    this.forceLogout = flag;
	  }
	  
	  public void setGuestUserAllowed(boolean flag)
	  {
	    this.guestUserAllowed = flag;
	  }
	  
	  public void setFilterUserNames(boolean flag)
	  {
	    this.filterUserNames = flag;
	  }
	  
	  public void setGuestUserNamePrefix(String prefix)
	  {
	    this.guestUserNamePrefix = prefix;
	  }
	  
	  public void setMaxAllowedRooms(int max)
	  {
	    if (max < 0) {
	      throw new OSException("Negative values are not acceptable for Zone.maxAllowedRooms: " + max);
	    }
	    this.maxAllowedRooms = max;
	  }
	  
	  public void setMaxAllowedUsers(int max)
	  {
	    if (max < 0) {
	      throw new OSException("Negative values are not acceptable for Zone.maxAllowedUsers: " + max);
	    }
	    this.maxAllowedUsers = max;
	  }
	  
	  public void setMaxUserVariablesAllowed(int max)
	  {
	    if (max < 0) {
	      throw new OSException("Negative values are not acceptable for Zone.maxAllowedUserVariables: " + max);
	    }
	    this.maxAllowedUserVariables = max;
	  }
	  
	  public void setMaxRoomsCreatedPerUserLimit(int max)
	  {
	    if (max < 0) {
	      throw new OSException("Negative values are not acceptable for Zone.maxRoomsCreatedPerUser: " + max);
	    }
	    this.maxRoomsCreatedPerUser = max;
	  }
	  
	  public void setProperty(Object key, Object value)
	  {
	    this.properties.put(key, value);
	  }
	  
	  public void setPublicGroups(List<String> groupIDs)
	  {
	    this.publicGroups = groupIDs;
	  }
	  
	  public void setDefaultGroups(List<String> groupIDs)
	  {
	    this.defaultGroups = groupIDs;
	  }
	  
//	  public void setUserCountChangeUpdateInterval(int interval)
//	  {
//	    if (interval < 0) {
//	      throw new OSException("Negative values are not acceptable for Zone.userCountChangeUpdateInterval: " + interval);
//	    }
//	    synchronized (this)
//	    {
//	      this.userCountChangeUpdateInterval = interval;
//	    }
//	    if (this.uCountResponseThrottler == null) {
//	      this.uCountResponseThrottler = new UserCountChangeResponseThrottler(this.userCountChangeUpdateInterval, this.name);
//	    } else {
//	      this.uCountResponseThrottler.setInterval(this.userCountChangeUpdateInterval);
//	    }
//	  }
	  
	  public void setZoneManager(ZoneManager manager)
	  {
	    this.zoneManager = manager;
	  }
	  
	  public void validateUserName(String name)
	    throws OSException
	  {
	    if (this.userManager.containsName(name)) {
	      throw new OSException("User name is already taken: " + name);
	    }
	  }
	  
//	  public IFloodFilter getFloodFilter()
//	  {
//	    return this.floodFilter;
//	  }
//	  
//	  public IWordFilter getWordFilter()
//	  {
//	    return this.wordFilter;
//	  }
	  
	  public void removeAllUsers()
	  {
	    for (User user : this.userManager.getAllUsers()) {
	      this.sfs.getAPIManager().getSFSApi().disconnectUser(user);
	    }
	  }
	  
	  public void removeUser(int userId)
	  {
	    User user = this.userManager.getUserById(userId);
	    if (user == null) {
	      this.logger.info("Can't remove user with Id: " + userId + ". User doesn't exist in Zone: " + this.name);
	    } else {
	      removeUser(user);
	    }
	  }
	  
	  public void removeUser(ChannelHandlerContext session)
	  {
	    User user = this.userManager.getUserBySession(session);
	    if (user == null) {
	      this.logger.info("Can't remove user with Session: " + session + ". User doesn't exist in Zone: " + this.name);
	    } else {
	      removeUser(user);
	    }
	  }
	  
	  public void removeUser(String userName)
	  {
	    User user = this.userManager.getUserByName(userName);
	    if (user == null) {
	      this.logger.info("Can't remove user with Name: " + userName + ". User doesn't exist in Zone: " + this.name);
	    } else {
	      removeUser(user);
	    }
	  }
	  
	  public void removeUser(User user)
	  {
	    this.userManager.disconnectUser(user);
	    

	    this.roomManager.removeUser(user);
	    
	    this.logger.info("User: " + user.getName() + " was disconnected.");
	  }
	  
	  public int getMaxRoomVariablesAllowed()
	  {
	    return this.maxAllowedRoomVariables;
	  }
	  
	  public void setMaxRoomVariablesAllowed(int max)
	  {
	    this.maxAllowedRoomVariables = max;
	  }
	  
//	  public PrivilegeManager getPrivilegeManager()
//	  {
//	    return this.privilegeManager;
//	  }
//	  
//	  public void setPrivilegeManager(PrivilegeManager privilegeManager)
//	  {
//	    if (this.privilegeManager != null) {
//	      throw new SFSRuntimeException("Cannot re-assign the PrivilegeManager in this Zone: " + this.name);
//	    }
//	    this.privilegeManager = privilegeManager;
//	  }
//	  
//	  public BuddyListManager getBuddyListManager()
//	  {
//	    return this.buddyListManager;
//	  }
//	  
//	  public void setBuddyListManager(BuddyListManager buddyListManager)
//	  {
//	    if (this.buddyListManager != null) {
//	      throw new SFSRuntimeException("Cannot re-assign the BuddListManager in this Zone: " + this.name);
//	    }
//	    this.buddyListManager = buddyListManager;
//	  }
//	  
//	  public IDBManager getDBManager()
//	  {
//	    return this.dbManager;
//	  }
//	  
//	  public void setDBManager(IDBManager manager)
//	  {
//	    if (this.dbManager != null) {
//	      throw new SFSRuntimeException("Cannot re-assign the DBManager in this Zone: " + this.name);
//	    }
//	    this.dbManager = manager;
//	  }
	  
	  public boolean isFilterPrivateMessages()
	  {
	    return this.filterPrivateMessages;
	  }
	  
	  public void setFilterPrivateMessages(boolean flag)
	  {
	    this.filterPrivateMessages = flag;
	  }
	  
	  public User login(ChannelHandlerContext session, String userName, String password)
	    throws OSException
	  {
	    return login(session, userName, password, false);
	  }
	  
	  public User login(ChannelHandlerContext session, String userName, String password, boolean forceLogout)
	    throws OSException
	  {
	    boolean isEmptyName = userName.length() == 0;
	    if (!isActive())
	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
//	      errorData.addParameter(getName());
	      
	      throw new OSException("Zone: " + getName() + " is not active!"/*, errorData*/);
	    }
	    if ((forceLogout) && (isForceLogout())) {
	      applyForceLogin(userName);
	    }
	    if (getUserCount() >= this.maxAllowedUsers)
	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_ZONE_FULL);
//	      errorData.addParameter(getName());
	      
	      throw new OSException("The Zone is full, can't login user: " + userName/*, errorData*/);
	    }
	    
//	    try
//	    {
//	      this.sfs.getLSManager().execute("join", session);
//	    }
//	    catch (SFSException se)
//	    {
//	      throw ((SFSLoginException)se);
//	    }
	    
	    if ((!isGuestUserAllowed()) && (isEmptyName))
	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_GUEST_NOT_ALLOWED);
//	      errorData.addParameter(getName());
	      
	      throw new OSException("Guest users are not allowed in Zone: " + getName()/*, errorData*/);
	    }
	    if ((!isEmptyName) && (getUserByName(userName) != null))
	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
//	      errorData.setParams(Arrays.asList(new String[] { userName, getName() }));
	      
	      throw new OSException("Another user is already logged with the same name: " + userName/*, errorData*/);
	    }
	    
//	    if ((!isEmptyName) && (this.sfs.getBannedUserManager().isNameBanned(userName, getName())))
//	    {
////	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BANNED_USER);
////	      errorData.addParameter(userName);
//	      
//	      throw new OSException("This user name is banned: " + userName, errorData);
//	    }
//	    if (this.sfs.getBannedUserManager().isIpBanned(session.getAddress()))
//	    {
////	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BANNED_IP);
////	      errorData.addParameter(session.getAddress());
//	      
//	      throw new OSException("This IP address is banned: " + userName, errorData);
//	    }
	    
//	    boolean applyWordFilter = getWordFilter().isActive();
//	    applyWordFilter &= isFilterUserNames();
//	    applyWordFilter &= getWordFilter().getFilterMode() == WordsFilterMode.BLACK_LIST;
//	    applyWordFilter &= !isEmptyName;
//	    if (applyWordFilter)
//	    {
//	      FilteredMessage filteredName = getWordFilter().apply(userName);
//	      if (filteredName.getOccurrences() > 0)
//	      {
//	        SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_NAME_CONTAINS_BAD_WORDS);
//	        errorData.setParams(Arrays.asList(new String[] { userName, filteredName.getMessage() }));
//	        
//	        throw new OSException("User name: " + userName + " contains bad words.", errorData);
//	      }
//	    }
	    
	    User user = defaultLogin(session, userName, password);
	    

	    user.setMaxAllowedVariables(this.maxAllowedUserVariables);
	    user.setLastLoginTime(System.currentTimeMillis());
	    
//	    user.setReconnectionSeconds(this.userReconnectionSeconds);
//	    if (this.maxUserIdleTime > 0) {
//	      user.getSession().setMaxLoggedInIdleTime(this.maxUserIdleTime);
//	    }
	    
	    manageNewUser(user);
	    

	    registerUserInterests(user);
	    

	    //setupBuddyProperties(user);
	    
	    return user;
	  }
	  
//	  public ISFSArray getRoomListData()
//	  {
//	    return getRoomListData(this.defaultGroups);
//	  }
//	  
//	  public ISFSArray getRoomListData(List<String> groupIds)
//	  {
//	    ISFSArray roomList = SFSArray.newInstance();
//	    if (groupIds.size() > 0) {
//	      for (String groupId : groupIds)
//	      {
//	        List<Room> roomsInGroup = getRoomListFromGroup(groupId);
//	        if (roomsInGroup != null) {
//	          for (Room room : roomsInGroup) {
//	            roomList.addSFSArray(room.toSFSArray(true));
//	          }
//	        }
//	      }
//	    }
//	    return roomList;
//	  }
	  
	  public String toString()
	  {
	    return "{ Zone: " + this.name + " }";
	  }
	  
	  public String getDump()
	  {
	    throw new UnsupportedOperationException("Sorry, not implemented yet!");
	  }
	  
//	  public IAdminHelper getAdminHelper()
//	  {
//	    return this.adminHelper;
//	  }
//	  
//	  public void setAdminHelper(IAdminHelper adminHelper)
//	  {
//	    this.adminHelper = adminHelper;
//	  }
//	  
//	  public void initRoomPersistence(RoomStorageMode mode, BaseStorageConfig config)
//	  {
//	    if (this.roomStorage == null) {
//	      this.roomStorage = RoomStorageFactory.getStorage(this, mode, config);
//	    }
//	  }
//	  
//	  public IRoomStorage getRoomPersistenceApi()
//	  {
//	    return this.roomStorage;
//	  }
	  
	  private User defaultLogin(ChannelHandlerContext session, String userName, String password)
	  {
	    User user = new User(session);
	    if (userName.length() == 0) {
	      userName = getGuestUserNamePrefix() + user.getId();
	    }
	    user.setName(userName);
	    

	    user.setZone(this);
	    
	    return user;
	  }
	  
	  private void applyForceLogin(String userName)
	  {
	    User oldUser = getUserByName(userName);
	    if (oldUser == null) {
	      return;
	    }
	    
	    oldUser.setReconnectionSeconds(0);//oldUser.getSession().setReconnectionSeconds(0);
	    
	    this.logger.info("User already logged in. Disconnecting previous instance : " + oldUser);
	    this.sfs.getAPIManager().getSFSApi().disconnectUser(oldUser);
	    

	    throw new OSException("SFSLoginInterruptedException");
	  }
	  
	  private synchronized void manageNewUser(User user)
	    throws OSException
	  {
	    boolean duplicateCheck = this.sfs.getUserManager().getUserBySession(user.getSession()) != null;
	    if (duplicateCheck)
	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
//	      errorData.setParams(Arrays.asList(new String[] { user.getName(), getName() }));
	      
	      throw new OSException("Duplicate login: " + user/*, errorData*/);
	    }
	    this.userManager.addUser(user);
	    

	    this.sfs.getUserManager().addUser(user);
	  }
	  
	  private void registerUserInterests(User user)
	  {
	    if (this.defaultGroups.size() > 0) {
	      for (String groupId : this.defaultGroups) {
	        user.subscribeGroup(groupId);
	      }
	    }
	  }
	  
//	  private void setupBuddyProperties(User user)
//	  {
//	    if (this.buddyListManager.isActive())
//	    {
//	      List<BuddyVariable> bvars = this.buddyListManager.getOfflineBuddyVariables(user.getName(), true);
//	      if (bvars != null) {
//	        user.getBuddyProperties().setVariables(bvars);
//	      }
//	    }
//	  }
//	  
//	  private void populateTransientFields()
//	  {
//	    this.sfs = SmartFoxServer.getInstance();
//	    this.logger = LoggerFactory.getLogger(getClass());
//	    this.wordFilter = new SFSWordFilter(this.sfs.getBannedUserManager());
//	    
//
//	    this.floodFilter = null;
//	    
//	    this.uCountResponseThrottler = new UserCountChangeResponseThrottler(this.userCountChangeUpdateInterval, this.name);
//	  }
}
