package com.orange.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;


public class ZoneSettings
{
  private static final AtomicInteger idGenerator = new AtomicInteger();
  private transient Integer id;
  public String name = "";
  public boolean isCustomLogin = false;
  public boolean isForceLogout = true;
  public boolean isFilterUserNames = true;
  public boolean isFilterRoomNames = true;
  public boolean isFilterPrivateMessages = true;
  public boolean isFilterBuddyMessages = true;
  public int maxUsers = 1000;
  public int maxUserVariablesAllowed = 5;
  public int maxRoomVariablesAllowed = 5;
  public int minRoomNameChars = 3;
  public int maxRoomNameChars = 10;
  public int maxRooms = 500;
  public int maxRoomsCreatedPerUser = 3;
  public int userCountChangeUpdateInterval = 1000;
  public int userReconnectionSeconds = 0;
  public int overrideMaxUserIdleTime = 120;
  public boolean allowGuestUsers = true;
  public String guestUserNamePrefix = "Guest#";
  public String publicRoomGroups = "default";
  public String defaultRoomGroups = "default";
  public String defaultPlayerIdGeneratorClass = "";
  public WordFilterSettings wordsFilter;
  public FloodFilterSettings floodFilter;
  public List<RoomSettings> rooms;
  public List<String> disabledSystemEvents;
  public PrivilegeManagerSettings privilegeManager;
  public ExtensionSettings extension;
  public BuddyListSettings buddyList;
  //public DBConfig databaseManager;
  public static final String DENIABLE_REQUESTS = "JoinRoom,CreateRoom,ChangeRoomName,ChangeRoomPassword,ObjectMessage,SetRoomVariables,SetUserVariables,LeaveRoom,SubscribeRoomGroup,UnsubscribeRoomGroup,SpectatorToPlayer,PlayerToSpectator,ChangeRoomCapacity,PublicMessage,PrivateMessage,FindRooms,FindUsers,InitBuddyList,AddBuddy,BlockBuddy,RemoveBuddy,SetBuddyVariables,GoOnline,BuddyMessage,InviteUser,InvitationReply,CreateSFSGame,QuickJoinGame";
  public static final String DB_EXHAUSTED_POOL_MODES = String.format("%s,%s,%s", new Object[] { "BLOCK", "FAIL", "GROW" });
  
  public ZoneSettings()
  {
    getId();
  }
  
  public ZoneSettings(String name)
  {
    this();
    this.name = name;
    
    this.wordsFilter = new WordFilterSettings();
    this.floodFilter = new FloodFilterSettings();
    
    this.rooms = new ArrayList();
    this.disabledSystemEvents = new ArrayList();
    this.buddyList = new BuddyListSettings();
    

    this.privilegeManager = new PrivilegeManagerSettings();
    this.privilegeManager.profiles = new ArrayList();
    setupDefaultPrivileges();
    

    this.extension = new ExtensionSettings();
    //this.databaseManager = new DBConfig();
  }
  
  private void setupDefaultPrivileges()
  {
    PermissionProfile guestProfile = new PermissionProfile();
    guestProfile.id = 0;
    guestProfile.name = "Guest";
    guestProfile.permissionFlags = new ArrayList();
    guestProfile.deniedRequests = Arrays.asList(new String[] {
    
      "CreateRoom", 
      "PrivateMessage", 
      "SetRoomVariables", 
      "SetUserVariables", 
      "ChangeRoomName", 
      "ChangeRoomPassword", 
      "ChangeRoomCapacity", 
      "InitBuddyList", 
      "AddBuddy", 
      "BlockBuddy", 
      "RemoveBuddy", 
      "SetBuddyVariables", 
      "GoOnline", 
      "BuddyMessage", 
      "ModeratorMessage", 
      "AdminMessage", 
      "KickUser", 
      "BanUser" });
    


    PermissionProfile regularProfile = new PermissionProfile();
    regularProfile.id = 1;
    regularProfile.name = "Standard";
    regularProfile.permissionFlags = new ArrayList();
    regularProfile.deniedRequests = Arrays.asList(new String[] {
    
      "ModeratorMessage", 
      "AdminMessage", 
      "KickUser", 
      "BanUser" });
    
    regularProfile.permissionFlags = Arrays.asList(new String[] { "ExtensionCalls" });
    

    PermissionProfile modProfile = new PermissionProfile();
    modProfile.id = 2;
    modProfile.name = "Moderator";
    modProfile.deniedRequests = Arrays.asList(new String[] { "AdminMessage" });
    modProfile.permissionFlags = Arrays.asList(new String[] { "ExtensionCalls", "SuperUser" });
    

    PermissionProfile adminProfile = new PermissionProfile();
    adminProfile.id = 3;
    adminProfile.name = "Administrator";
    adminProfile.deniedRequests = new ArrayList();
    adminProfile.permissionFlags = Arrays.asList(new String[] { "ExtensionCalls", "SuperUser" });
    
    this.privilegeManager.profiles = Arrays.asList(new PermissionProfile[] { guestProfile, regularProfile, modProfile, adminProfile });
  }
  
  public String getDeniableRequests()
  {
    return "JoinRoom,CreateRoom,ChangeRoomName,ChangeRoomPassword,ObjectMessage,SetRoomVariables,SetUserVariables,LeaveRoom,SubscribeRoomGroup,UnsubscribeRoomGroup,SpectatorToPlayer,PlayerToSpectator,ChangeRoomCapacity,PublicMessage,PrivateMessage,FindRooms,FindUsers,InitBuddyList,AddBuddy,BlockBuddy,RemoveBuddy,SetBuddyVariables,GoOnline,BuddyMessage,InviteUser,InvitationReply,CreateSFSGame,QuickJoinGame";
  }
  
  public synchronized int getId()
  {
    if (this.id == null) {
      this.id = Integer.valueOf(setUniqueId());
    }
    return this.id.intValue();
  }
  
  public synchronized RoomSettings getRoomSettings(int id)
  {
    if (this.rooms == null) {
      throw new IllegalStateException("No Room configuration has been loaded yet!");
    }
    RoomSettings settings = null;
    for (RoomSettings item : this.rooms) {
      if (item.getId() == id)
      {
        settings = item;
        break;
      }
    }
    return settings;
  }
  
  public synchronized RoomSettings getRoomSettings(String name)
  {
    if (this.rooms == null) {
      throw new IllegalStateException("No Room configuration has been loaded yet!");
    }
    RoomSettings settings = null;
    for (RoomSettings item : this.rooms) {
      if (item.name.equals(name))
      {
        settings = item;
        break;
      }
    }
    return settings;
  }
  
  public synchronized void addRoomSettings(RoomSettings settings)
  {
    if (getRoomSettings(settings.name) != null) {
      throw new IllegalArgumentException("A room with the same name already exists: " + settings.name);
    }
    this.rooms.add(settings);
  }
  
  public synchronized void removeRoomSetting(RoomSettings settings)
  {
    this.rooms.remove(settings);
  }
  
  public static final class WordFilterSettings
  {
    public boolean isActive = false;
    public boolean useWarnings = false;
    public int warningsBeforeKick = 3;
    public int kicksBeforeBan = 2;
    public int banDuration = 1440;
    public int maxBadWordsPerMessage = 0;
    public int kicksBeforeBanMinutes = 3;
    public int secondsBeforeBanOrKick = 5;
    public String warningMessage = "Stop swearing or you will be banned";
    public String kickMessage = "Swearing not allowed: you are being kicked";
    public String banMessage = "Too much swearing: you are banned";
    public String wordsFile = "config/wordsFile.txt";
    public String filterMode = "WHITELIST";
    public String banMode = "NAME";
    public String hideBadWordWithCharacter = "*";
    public String customWordFilterClass = null;
  }
  
  public static final class FloodFilterSettings
  {
    private static String REQUEST_LIST = null;
    public boolean isActive = false;
    public int banDurationMinutes = 1440;
    public int maxFloodingAttempts = 5;
    public int secondsBeforeBan = 5;
    public String banMode = "NAME";
    public boolean logFloodingAttempts = true;
    public String banMessage = "Too much flooding, you are banned";
    public List<ZoneSettings.RequestFilterSettings> requestFilters;
    
//    public String getRequestsList()
//    {
//      if (REQUEST_LIST == null) {
//        createRequestList();
//      }
//      return REQUEST_LIST;
//    }
//    
//    private void createRequestList()
//    {
//      List<String> names = new ArrayList();
//      for (SystemRequest item : SystemRequest.values())
//      {
//        Short reqId = (Short)item.getId();
//        if ((reqId.shortValue() < 1000) && (reqId.shortValue() != 13)) {
//          names.add(item.toString());
//        }
//      }
//      Collections.sort(names);
//      
//      REQUEST_LIST = "";
//      for (String name : names) {
//        REQUEST_LIST = REQUEST_LIST + name + ",";
//      }
//      REQUEST_LIST = REQUEST_LIST.substring(0, REQUEST_LIST.length() - 1);
//    }
    
  }
  
  public static final class RoomEventsSettings
  {
    public boolean isClientOverrideAllowed = true;
    public String registeredEvents = "ROOM_NAME_CHANGE,PASSWORD_STATE_CHANGE,USER_COUNT_CHANGE,ROOM_VARIABLES_UPDATE";
  }
  
  public static final class RegisteredRoomEvents
  {
    public String groupId = "";
    public boolean roomNameChange = false;
    public boolean roomCapacityChange = false;
    public boolean userCountChange = true;
    public boolean roomVariablesUpdate = true;
    public boolean passwordStatusChange = false;
  }
  
  public static final class RoomSettings
  {
    public static final String EVENTS = "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";
    private static final AtomicInteger idGenerator = new AtomicInteger();
    private transient Integer id;
    public String name = null;
    public String groupId = "default";
    public String password = null;
    public int maxUsers = 20;
    public int maxSpectators = 0;
    public boolean isDynamic = false;
    public boolean isGame = false;
    public boolean isHidden = false;
    public String autoRemoveMode = "DEFAULT";
    public ZoneSettings.RoomPermissions permissions = new ZoneSettings.RoomPermissions();
    public String events = "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";
    public ZoneSettings.BadWordsFilterSettings badWordsFilter = new ZoneSettings.BadWordsFilterSettings();
    public List<ZoneSettings.RoomVariableDefinition> roomVariables;
    public ZoneSettings.ExtensionSettings extension;
    public ZoneSettings.MMOSettings mmoSettings = new ZoneSettings.MMOSettings();
    
    public RoomSettings()
    {
      getId();
    }
    
    public RoomSettings(String name)
    {
      this();
      this.name = name;
      this.password = "";
      this.roomVariables = new ArrayList();
      this.extension = new ZoneSettings.ExtensionSettings();
    }
    
    public int getId()
    {
      if (this.id == null) {
        this.id = Integer.valueOf(getUniqueId());
      }
      return this.id.intValue();
    }
    
    private static int getUniqueId()
    {
      return idGenerator.getAndIncrement();
    }
    
    public String getAvailableEvents()
    {
      return "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";
    }
  }
  
  public static final class MMOSettings
  {
    public boolean isActive = false;
    public String defaultAOI = "100,100,0";
    public String lowerMapLimit = "";
    public String higherMapLimit = "";
    public int userMaxLimboSeconds = 50;
    public int proximityListUpdateMillis = 500;
    public boolean sendAOIEntryPoint = true;
  }
  
  public static final class RoomPermissions
  {
    public static final String FLAGS = "ROOM_NAME_CHANGE,PASSWORD_STATE_CHANGE,PUBLIC_MESSAGES,CAPACITY_CHANGE";
    public String flags = "PASSWORD_STATE_CHANGE,PUBLIC_MESSAGES";
    public int maxRoomVariablesAllowed = 10;
    
    public String getAvailableFlags()
    {
      return "ROOM_NAME_CHANGE,PASSWORD_STATE_CHANGE,PUBLIC_MESSAGES,CAPACITY_CHANGE";
    }
  }
  
  public static final class BadWordsFilterSettings
  {
    public boolean isActive = false;
  }
  
  public static final class RoomVariableDefinition
  {
    public String name = "";
    public String value = "";
    public String type = "";
    public boolean isPrivate = false;
    public boolean isPersistent = false;
    public boolean isGlobal = false;
    public boolean isHidden = false;
  }
  
  public static final class PrivilegeManagerSettings
  {
    public boolean active = false;
    public List<ZoneSettings.PermissionProfile> profiles;
  }
  
  public static final class PermissionProfile
  {
    public short id = -1;
    public String name = "";
    public List<String> deniedRequests;
    public List<String> permissionFlags;
    
    public String getDeniedRequestsString()
    {
      return StringUtils.join(this.deniedRequests, ",");
    }
    
    public void setDeniedRequestsString(Object stringList)
    {
      String list = stringList.toString();
      if (list.length() > 0) {
        this.deniedRequests = Arrays.asList(list.toString().split("\\,"));
      } else {
        this.deniedRequests = new ArrayList();
      }
    }
    
    public String getPermissionFlagsString()
    {
      return StringUtils.join(this.permissionFlags, ",");
    }
    
    public void setPermissionFlagsString(Object stringList)
    {
      String list = stringList.toString();
      if (list.length() > 0) {
        this.permissionFlags = Arrays.asList(list.split("\\,"));
      } else {
        this.permissionFlags = new ArrayList();
      }
    }
    
    public int getIntId()
    {
      return this.id;
    }
    
    public void setIntId(Object id)
    {
      this.id = ((Integer)id).shortValue();
    }
  }
  
  public static final class ExtensionSettings
  {
    public String name = "";
    public String type = "JAVA";
    public String file = "";
    public String propertiesFile = "";
    public String reloadMode = "AUTO";
  }
  
  public static final class BuddyListSettings
  {
    public boolean active = false;
    public boolean allowOfflineBuddyVariables = true;
    public int maxItemsPerList = 100;
    public int maxBuddyVariables = 15;
    public int offlineBuddyVariablesCacheSize = 500;
    public String customStorageClass = "";
    public boolean useTempBuddies = true;
    public List<String> buddyStates = Arrays.asList(new String[] { "Available", "Away", "Occupied" });
    public ZoneSettings.BadWordsFilterSettings badWordsFilter = new ZoneSettings.BadWordsFilterSettings();
  }
  
//  public ISFSObject toSFSObject()
//  {
//    ISFSObject sfsObj = SFSObject.newInstance();
//    
//    return sfsObj;
//  }
//  
//  public static ZoneSettings fromSFSObject(ISFSObject sfsObj)
//  {
//    ZoneSettings settings = new ZoneSettings();
//    
//    return settings;
//  }
  
  private static int setUniqueId()
  {
    return idGenerator.getAndIncrement();
  }
  
  public static final class RequestFilterSettings
  {
    public String reqName;
    public int maxRequestsPerSecond;
  }
}