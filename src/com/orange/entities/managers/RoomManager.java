package com.orange.entities.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.config.CreateRoomSettings;
import com.orange.config.ZoneSettings;
import com.orange.core.BaseCoreService;
import com.orange.core.ICoreService;
import com.orange.entities.Room;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.entities.extensions.ExtensionLevel;
import com.orange.entities.extensions.ExtensionType;
import com.orange.exceptions.OSException;
import com.orange.exceptions.OSRuntimeException;
import com.orange.server.OrangeServerEngine;
import com.orange.util.DefaultPlayerIdGenerator;

public class RoomManager extends BaseCoreService implements ICoreService{
	private final Map<Integer, Room> roomsById;
	  private final Map<String, Room> roomsByName;
	  private final Map<String, List<Room>> roomsByGroup;
	  private final List<String> groups;
	  private final AtomicInteger gameRoomCounter;
	  private Logger logger;
	  private OrangeServerEngine sfs;
	  private Zone ownerZone;
	  private Class<? extends DefaultPlayerIdGenerator> playerIdGeneratorClass = DefaultPlayerIdGenerator.class;
	  
	  public RoomManager()
	  {
	    this.sfs = OrangeServerEngine.getInstance();
	    this.logger = LoggerFactory.getLogger(getClass());
	    
	    this.roomsById = new ConcurrentHashMap();
	    this.roomsByName = new ConcurrentHashMap();
	    this.roomsByGroup = new ConcurrentHashMap();
	    this.groups = new ArrayList();
	    this.gameRoomCounter = new AtomicInteger();
	  }
	  
	  public Room createRoom(CreateRoomSettings params)
	    throws OSException
	  {
	    return createRoom(params, null);
	  }
	  
	  public Room createRoom(CreateRoomSettings params, User owner)
	    throws OSException
	  {
	    String roomName = params.getName();
	    try
	    {
	      validateRoomName(roomName);
	    }
	    catch (OSException roomExc)
	    {
	      throw new OSException(roomExc.getMessage()/*, roomExc.getErrorData()*/);
	    }
	    Room newRoom;
//	    if ((params instanceof CreateSFSGameSettings))
//	    {
//	      Room newRoom = new SFSGame(roomName);
//	      
//
//	      params.setGame(true);
//	    }
//	    else
//	    {
//	      Room newRoom;
//	      if ((params instanceof CreateMMORoomSettings)) {
//	        newRoom = new MMORoom(roomName, ((CreateMMORoomSettings)params).getDefaultAOI(), ((CreateMMORoomSettings)params).getProximityListUpdateMillis());
//	      } else {
	        newRoom = new Room(roomName);
//	      }
//	    }
	    newRoom.setZone(this.ownerZone);
	    newRoom.setGroupId(params.getGroupId());
	    newRoom.setPassword(params.getPassword());
	    newRoom.setDynamic(params.isDynamic());
	    newRoom.setHidden(params.isHidden());
	    newRoom.setMaxUsers(params.getMaxUsers());
	    if (params.isGame()) {
	      newRoom.setMaxSpectators(params.getMaxSpectators());
	    } else {
	      newRoom.setMaxSpectators(0);
	    }
	    newRoom.setGame(
	    
	      params.isGame(), 
	      params.getCustomPlayerIdGeneratorClass() == null ? this.playerIdGeneratorClass : params.getCustomPlayerIdGeneratorClass());
	    

	    newRoom.setMaxRoomVariablesAllowed(params.getMaxVariablesAllowed());
//	    if (params.getRoomVariables() != null) {
//	      newRoom.setVariables(params.getRoomVariables());
//	    }
//	    Set<SFSRoomSettings> roomSettings = params.getRoomSettings();
//	    if (roomSettings == null) {
//	      if ((params instanceof CreateMMORoomSettings)) {
//	        roomSettings = EnumSet.of(
//	        
//	          SFSRoomSettings.USER_COUNT_CHANGE_EVENT, 
//	          SFSRoomSettings.USER_VARIABLES_UPDATE_EVENT, 
//	          SFSRoomSettings.PUBLIC_MESSAGES);
//	      } else {
//	        roomSettings = EnumSet.of(
//	        
//	          SFSRoomSettings.USER_ENTER_EVENT, 
//	          SFSRoomSettings.USER_EXIT_EVENT, 
//	          SFSRoomSettings.USER_COUNT_CHANGE_EVENT, 
//	          SFSRoomSettings.USER_VARIABLES_UPDATE_EVENT, 
//	          SFSRoomSettings.PUBLIC_MESSAGES);
//	      }
//	    }
//	    if ((newRoom instanceof MMORoom))
//	    {
//	      if (roomSettings.contains(SFSRoomSettings.USER_ENTER_EVENT)) {
//	        roomSettings.remove(SFSRoomSettings.USER_ENTER_EVENT);
//	      }
//	      if (roomSettings.contains(SFSRoomSettings.USER_EXIT_EVENT)) {
//	        roomSettings.remove(SFSRoomSettings.USER_EXIT_EVENT);
//	      }
//	    }
	    newRoom.setUseWordsFilter(params.isUseWordsFilter());
	    //newRoom.setFlags(roomSettings);
	    newRoom.setOwner(owner);
	    newRoom.setAutoRemoveMode(params.getAutoRemoveMode());
//	    if (this.roomsById.size() >= this.ownerZone.getMaxAllowedRooms())
//	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.CREATE_ROOM_ZONE_FULL);
//	      throw new SFSCreateRoomException("Zone is full. Can't add any more rooms.", errorData);
//	    }
//	    if (params.getRoomProperties() != null) {
//	      ((SFSRoom)newRoom).setProperties(params.getRoomProperties());
//	    }
	    addRoom(newRoom);
	    

	    newRoom.setActive(true);
	    if ((params.getExtension() != null) && (params.getExtension().getId() != null) && (params.getExtension().getId().length() > 0)) {
	      try
	      {
	        createRoomExtension(newRoom, params.getExtension());
	      }
	      catch (OSException e)
	      {
//	        ExceptionMessageComposer message = new ExceptionMessageComposer(e);
//	        message.setDescription("Failure while creating room extension.");
//	        message.setPossibleCauses("If the CreateRoom request was sent from client make sure that the extension name matches the name of an existing extension");
	        this.logger.warn(/*message.toString()*/e.getMessage());
	      }
	    }
	    if (newRoom.isGame()) {
	      this.gameRoomCounter.incrementAndGet();
	    }
	    this.logger.info(
	    
	      String.format(
	      
	      "Room created: %s, %s", new Object[] {
	      newRoom.getZone().toString(), 
	      newRoom.toString() }));
	    


	    return newRoom;
	  }
	  
	  private void createRoomExtension(Room room, CreateRoomSettings.RoomExtensionSettings params)
	    throws OSException
	  {
	    if (params == null) {
	      return;
	    }
	    ExtensionType extType = ExtensionType.JAVA;
	    ExtensionLevel extLevel = ExtensionLevel.ROOM;
	    
	    String className = params.getClassName();
//	    if (StringUtils.endsWithIgnoreCase(className, ".py")) {
//	      extType = ExtensionType.PYTHON;
//	    }
	    ZoneSettings.ExtensionSettings extSettings = new ZoneSettings.ExtensionSettings();
	    extSettings.name = params.getId();
	    extSettings.file = className;
	    extSettings.propertiesFile = params.getPropertiesFile();
	    extSettings.reloadMode = "AUTO";
	    extSettings.type = extType.toString();
	    
	    this.sfs.getExtensionManager().createExtension(extSettings, extLevel, room.getZone(), room);
	  }
	  
	  public Class<? extends DefaultPlayerIdGenerator> getDefaultRoomPlayerIdGenerator()
	  {
	    return this.playerIdGeneratorClass;
	  }
	  
	  public void setDefaultRoomPlayerIdGeneratorClass(Class<? extends DefaultPlayerIdGenerator> customIdGeneratorClass)
	  {
	    this.playerIdGeneratorClass = customIdGeneratorClass;
	  }
	  
	  public void addGroup(String groupId)
	  {
	    synchronized (this.groups)
	    {
	      this.groups.add(groupId);
	    }
	  }
	  
	  public void addRoom(Room room)
	  {
	    this.roomsById.put(Integer.valueOf(room.getId()), room);
	    this.roomsByName.put(room.getName(), room);
	    synchronized (this.groups)
	    {
	      if (!this.groups.contains(room.getGroupId())) {
	        this.groups.add(room.getGroupId());
	      }
	    }
	    addRoomToGroup(room);
	  }
	  
	  public boolean containsGroup(String groupId)
	  {
	    boolean flag = false;
	    synchronized (this.groups)
	    {
	      flag = this.groups.contains(groupId);
	    }
	    return flag;
	  }
	  
	  public List<String> getGroups()
	  {
	    List<String> groupsCopy = null;
	    synchronized (this.groups)
	    {
	      groupsCopy = new ArrayList(this.groups);
	    }
	    return groupsCopy;
	  }
	  
	  public Room getRoomById(int id)
	  {
	    return (Room)this.roomsById.get(Integer.valueOf(id));
	  }
	  
	  public Room getRoomByName(String name)
	  {
	    return (Room)this.roomsByName.get(name);
	  }
	  
	  public List<Room> getRoomList()
	  {
	    return new ArrayList(this.roomsById.values());
	  }
	  
	  public List<Room> getRoomListFromGroup(String groupId)
	  {
	    List<Room> roomList = (List)this.roomsByGroup.get(groupId);
	    List<Room> copyOfRoomList = null;
	    if (roomList != null) {
	      synchronized (roomList)
	      {
	        copyOfRoomList = new ArrayList(roomList);
	      }
	    }
	    copyOfRoomList = new ArrayList();
	    
	    return copyOfRoomList;
	  }
	  
	  public int getGameRoomCount()
	  {
	    return this.gameRoomCounter.get();
	  }
	  
	  public int getTotalRoomCount()
	  {
	    return this.roomsById.size();
	  }
	  
	  public void removeGroup(String groupId)
	  {
	    synchronized (this.groups)
	    {
	      this.groups.remove(groupId);
	    }
	  }
	  
	  public void removeRoom(int roomId)
	  {
	    Room room = (Room)this.roomsById.get(Integer.valueOf(roomId));
	    if (room == null) {
	      this.logger.warn("Can't remove requested room. ID = " + roomId + ". Room was not found.");
	    } else {
	      removeRoom(room);
	    }
	  }
	  
	  public void removeRoom(String name)
	  {
	    Room room = (Room)this.roomsByName.get(name);
	    if (room == null) {
	      this.logger.warn("Can't remove requested room. Name = " + name + ". Room was not found.");
	    } else {
	      removeRoom(room);
	    }
	  }
	  
	  public void removeRoom(Room room)
	  {
//	    boolean wasRemoved;
	    try
	    {
//	      ISFSExtension roomExtension = room.getExtension();
//	      if (roomExtension != null) {
//	        this.sfs.getExtensionManager().destroyExtension(roomExtension);
//	      }
	    }
	    finally
	    {
	      room.destroy();
	      room.setActive(false);
	      
	      boolean wasRemoved = this.roomsById.remove(Integer.valueOf(room.getId())) != null;
	      this.roomsByName.remove(room.getName());
	      removeRoomFromGroup(room);
	      if ((wasRemoved) && (room.isGame())) {
	        this.gameRoomCounter.decrementAndGet();
	      }
	      this.logger.info(
	      
	        String.format(
	        
	        "Room removed: %s, %s, Duration: %s", new Object[] {
	        room.getZone().toString(), 
	        room.toString(), 
	        Long.valueOf(room.getLifeTime()) }));
	    }
	  }
	  
	  public boolean containsRoom(int id, String groupId)
	  {
	    Room room = (Room)this.roomsById.get(Integer.valueOf(id));
	    return isRoomContainedInGroup(room, groupId);
	  }
	  
	  public boolean containsRoom(int id)
	  {
	    return this.roomsById.containsKey(Integer.valueOf(id));
	  }
	  
	  public boolean containsRoom(Room room, String groupId)
	  {
	    return isRoomContainedInGroup(room, groupId);
	  }
	  
	  public boolean containsRoom(Room room)
	  {
	    return this.roomsById.containsValue(room);
	  }
	  
	  public boolean containsRoom(String name, String groupId)
	  {
	    Room room = (Room)this.roomsByName.get(name);
	    return isRoomContainedInGroup(room, groupId);
	  }
	  
	  public boolean containsRoom(String name)
	  {
	    return this.roomsByName.containsKey(name);
	  }
	  
	  public Zone getOwnerZone()
	  {
	    return this.ownerZone;
	  }
	  
	  public void setOwnerZone(Zone zone)
	  {
	    this.ownerZone = zone;
	  }
	  
	  public void removeUser(User user)
	  {
	    for (Room room : user.getJoinedRooms()) {
	      removeUser(user, room);
	    }
	  }
	  
	  public void removeUser(User user, Room room)
	  {
	    try
	    {
	      if (room.containsUser(user))
	      {
	        room.removeUser(user);
	        this.logger.debug("User: " + user.getName() + " removed from Room: " + room.getName());
	      }
	      else
	      {
	        throw new OSRuntimeException("Can't remove user: " + user + ", from: " + room);
	      }
	    }
	    finally
	    {
	      handleAutoRemove(room);
	    }
	    handleAutoRemove(room);
	  }
	  
	  public void checkAndRemove(Room room)
	  {
	    handleAutoRemove(room);
	  }
	  
	  public void changeRoomName(Room room, String newName)
	    throws OSException
	  {
	    if (room == null) {
	      throw new IllegalArgumentException("Can't change name. Room is Null!");
	    }
	    if (!containsRoom(room)) {
	      throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
	    }
	    validateRoomName(newName);
	    
	    String oldName = room.getName();
	    

	    room.setName(newName);
	    





	    this.roomsByName.put(newName, room);
	    this.roomsByName.remove(oldName);
	  }
	  
	  public void changeRoomPasswordState(Room room, String password)
	  {
	    if (room == null) {
	      throw new IllegalArgumentException("Can't change password. Room is Null!");
	    }
	    if (!containsRoom(room)) {
	      throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
	    }
	    room.setPassword(password);
	  }
	  
	  public void changeRoomCapacity(Room room, int newMaxUsers, int newMaxSpect)
	  {
	    if (room == null) {
	      throw new IllegalArgumentException("Can't change password. Room is Null!");
	    }
	    if (!containsRoom(room)) {
	      throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
	    }
	    if (newMaxUsers > 0) {
	      room.setMaxUsers(newMaxUsers);
	    }
	    if (newMaxSpect >= 0) {
	      room.setMaxSpectators(newMaxSpect);
	    }
	  }
	  
	  private void handleAutoRemove(Room room)
	  {
//	    if ((room.isEmpty()) && (room.isDynamic())) {
//	      switch (room.getAutoRemoveMode())
//	      {
//	      case DEFAULT: 
//	        if (room.isGame()) {
//	          removeWhenEmpty(room);
//	        } else {
//	          removeWhenEmptyAndCreatorIsGone(room);
//	        }
//	        break;
//	      case NEVER_REMOVE: 
//	        removeWhenEmpty(room);
//	        break;
//	      case WHEN_EMPTY: 
//	        removeWhenEmptyAndCreatorIsGone(room);
//	      }
//	    }
	  }
	  
//	  private void removeWhenEmpty(Room room)
//	  {
//	    if (room.isEmpty()) {
//	      this.sfs.getAPIManager().getSFSApi().removeRoom(room);
//	    }
//	  }
//	  
//	  private void removeWhenEmptyAndCreatorIsGone(Room room)
//	  {
//	    User owner = room.getOwner();
//	    if ((owner != null) && (!owner.isConnected())) {
//	      this.sfs.getAPIManager().getSFSApi().removeRoom(room);
//	    }
//	  }
	  
	  private boolean isRoomContainedInGroup(Room room, String groupId)
	  {
	    boolean flag = false;
	    if ((room != null) && (room.getGroupId().equals(groupId)) && (containsGroup(groupId))) {
	      flag = true;
	    }
	    return flag;
	  }
	  
	  private void addRoomToGroup(Room room)
	  {
	    String groupId = room.getGroupId();
	    

	    List<Room> roomList = (List)this.roomsByGroup.get(groupId);
	    if (roomList == null)
	    {
	      roomList = new ArrayList();
	      this.roomsByGroup.put(groupId, roomList);
	    }
	    synchronized (roomList)
	    {
	      roomList.add(room);
	    }
	  }
	  
	  private void removeRoomFromGroup(Room room)
	  {
	    List<Room> roomList = (List)this.roomsByGroup.get(room.getGroupId());
	    if (roomList != null) {
	      synchronized (roomList)
	      {
	        roomList.remove(room);
	      }
	    }
	    this.logger.info("Cannot remove room: " + room.getName() + " from it's group: " + room.getGroupId() + ". The group was not found.");
	  }
	  
	  private void validateRoomName(String roomName)
	    throws OSException
	  {
//	    if (containsRoom(roomName))
//	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_DUPLICATE_NAME);
//	      errorData.addParameter(roomName);
//	      
//	      String message = String.format("A room with the same name already exists: %s", new Object[] { roomName });
//	      throw new SFSRoomException(message, errorData);
//	    }
//	    int nameLen = roomName.length();
//	    int minLen = this.ownerZone.getMinRoomNameChars();
//	    int maxLen = this.ownerZone.getMaxRoomNameChars();
//	    if ((nameLen < minLen) || (nameLen > maxLen))
//	    {
//	      SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_NAME_BAD_SIZE);
//	      errorData.addParameter(String.valueOf(minLen));
//	      errorData.addParameter(String.valueOf(maxLen));
//	      errorData.addParameter(String.valueOf(nameLen));
//	      
//	      String message = String.format("Room name length is out of valid range. Min: %s, Max: %s, Found: %s (%s)", new Object[] { Integer.valueOf(minLen), Integer.valueOf(maxLen), Integer.valueOf(nameLen), roomName });
//	      throw new SFSRoomException(message, errorData);
//	    }
//	    IWordFilter wordFilter = this.ownerZone.getWordFilter();
//	    if (this.ownerZone.isFilterRoomNames()) {
//	      if ((wordFilter.isActive()) && (wordFilter.getFilterMode() == WordsFilterMode.BLACK_LIST))
//	      {
//	        FilteredMessage filteredName = wordFilter.apply(roomName);
//	        if (filteredName.getOccurrences() > 0)
//	        {
//	          SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_NAME_CONTAINS_BADWORDS);
//	          errorData.addParameter(roomName);
//	          
//	          String message = String.format("Room name contains bad words: %s", new Object[] { roomName });
//	          throw new SFSRoomException(message, errorData);
//	        }
//	      }
//	    }
	  }
	  
	  private void populateTransientFields()
	  {
	    this.sfs = OrangeServerEngine.getInstance();
	    this.logger = LoggerFactory.getLogger(getClass());
	  }
}
