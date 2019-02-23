package com.orange.entities;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.entities.extensions.BaseExtension;
import com.orange.entities.managers.UserManager;
import com.orange.entities.managers.UserManager;
import com.orange.exceptions.OSException;
import com.orange.util.DefaultPlayerIdGenerator;

public class Room{
	private static AtomicInteger autoID = new AtomicInteger(0);
	  private int id;
	  private String groupId;
	  private String name;
	  private String password;
	  private boolean passwordProtected;
	  private int maxUsers;
	  private int maxSpectators;
	  private int maxRoomVariablesAllowed;
	  private User owner;
	  private UserManager userManager;
	  private Zone zone;
	  private volatile BaseExtension extension;
	  private boolean dynamic;
	  private boolean game;
	  private boolean hidden;
	  private volatile boolean active;
	  private SFSRoomRemoveMode autoRemoveMode;
	  private DefaultPlayerIdGenerator playerIdGenerator;
	  private final long lifeTime;
	  //private final Lock switchUserLock;
	  private final Map<Object, Object> properties;
	  //private final Map<String, RoomVariable> variables;
	  private Set<SFSRoomSettings> flags;
	  private volatile boolean userWordsFilter;
	  protected Logger logger;
	  private boolean isGameFlagInited = false;
	  //private IAdminHelper adminHelper;
	  
	  private static int getNewID()
	  {
	    return autoID.getAndIncrement();
	  }
	  
	  public Room(String name)
	  {
	    this(name, null);
	  }
	  
	  public Room(String name, Class<?> customPlayerIdGeneratorClass)
	  {
	    this.id = getNewID();
	    this.name = name;
	    this.active = false;
	    
	    this.logger = LoggerFactory.getLogger(getClass());
	    this.properties = new ConcurrentHashMap();
	    //this.variables = new ConcurrentHashMap();
	    this.userManager = new UserManager();
	    
	    //this.switchUserLock = new ReentrantLock();
	    this.lifeTime = System.currentTimeMillis();
	  }
	  
	  public int getId()
	  {
	    return this.id;
	  }
	  
	  public String getGroupId()
	  {
	    if ((this.groupId != null) && (this.groupId.length() > 0)) {
	      return this.groupId;
	    }
	    return "default";
	  }
	  
	  public void setGroupId(String groupId)
	  {
	    this.groupId = groupId;
	  }
	  
	  public String getName()
	  {
	    return this.name;
	  }
	  
	  public void setName(String name)
	  {
	    this.name = name;
	  }
	  
	  public String getPassword()
	  {
	    return this.password;
	  }
	  
	  public void setPassword(String password)
	  {
	    this.password = password;
	    if ((this.password != null) && (this.password.length() > 0)) {
	      this.passwordProtected = true;
	    } else {
	      this.passwordProtected = false;
	    }
	  }
	  
	  public boolean isPasswordProtected()
	  {
	    return this.passwordProtected;
	  }
	  
	  public boolean isPublic()
	  {
	    return !this.passwordProtected;
	  }
	  
	  public int getMaxUsers()
	  {
	    return this.maxUsers;
	  }
	  
	  public void setMaxUsers(int maxUsers)
	  {
	    this.maxUsers = maxUsers;
	    if ((isGame()) && (this.playerIdGenerator != null)) {
	      this.playerIdGenerator.onRoomResize();
	    }
	  }
	  
	  public int getMaxSpectators()
	  {
	    return this.maxSpectators;
	  }
	  
	  public void setMaxSpectators(int maxSpectators)
	  {
	    this.maxSpectators = maxSpectators;
	  }
	  
	  public User getOwner()
	  {
	    return this.owner;
	  }
	  
	  public void setOwner(User owner)
	  {
	    this.owner = owner;
	  }
	  
	  public UserManager getUserManager()
	  {
	    return this.userManager;
	  }
	  
	  public void setUserManager(UserManager userManager)
	  {
	    this.userManager = userManager;
	  }
	  
	  public Zone getZone()
	  {
	    return this.zone;
	  }
	  
	  public void setZone(Zone zone)
	  {
	    this.zone = zone;
	    
	    instantiateRoomIdGenerator();
	  }
	  
	  public boolean isDynamic()
	  {
	    return this.dynamic;
	  }
	  
	  public void setDynamic(boolean dynamic)
	  {
	    this.dynamic = dynamic;
	  }
	  
	  public boolean isGame()
	  {
	    return this.game;
	  }
	  
	  public void setGame(boolean game, Class<? extends DefaultPlayerIdGenerator> customPlayerIdGeneratorClass)
	  {
	    if (this.isGameFlagInited) {
	      throw new IllegalStateException(toString() + ", isGame flag cannot be reset");
	    }
	    this.game = game;
	    this.isGameFlagInited = true;
	    if (this.game) {
	      try
	      {
	        this.playerIdGenerator = ((DefaultPlayerIdGenerator)customPlayerIdGeneratorClass.newInstance());
	        

	        this.playerIdGenerator.setParentRoom(this);
	        this.playerIdGenerator.init();
	      }
	      catch (InstantiationException err)
	      {
	        this.logger.warn(
	        
	          String.format(
	          
	          "Cannot instantiate Player ID Generator: %s, Reason: %s -- Room might not function correctly.", new Object[] {
	          customPlayerIdGeneratorClass, 
	          err }));
	      }
	      catch (IllegalAccessException err)
	      {
	        this.logger.warn(
	        
	          String.format(
	          
	          "Illegal Access to Player ID Generator Class: %s, Reason: %s -- Room might not function correctly.", new Object[] {
	          customPlayerIdGeneratorClass, 
	          err }));
	      }
	    }
	  }
	  
	  public void setGame(boolean game)
	  {
	    setGame(game, null);
	  }
	  
	  public boolean isHidden()
	  {
	    return this.hidden;
	  }
	  
	  public void setHidden(boolean hidden)
	  {
	    this.hidden = hidden;
	  }
	  
	  public boolean isActive()
	  {
	    return this.active;
	  }
	  
	  public void setActive(boolean flag)
	  {
	    this.active = flag;
	  }
	  
	  public SFSRoomRemoveMode getAutoRemoveMode()
	  {
	    return this.autoRemoveMode;
	  }
	  
	  public void setAutoRemoveMode(SFSRoomRemoveMode autoRemoveMode)
	  {
	    this.autoRemoveMode = autoRemoveMode;
	  }
	  
	  public List<User> getPlayersList()
	  {
	    List<User> playerList = new ArrayList();
	    for (User user : this.userManager.getAllUsers()) {
	      if (user.isPlayer(this)) {
	        playerList.add(user);
	      }
	    }
	    return playerList;
	  }
	  
	  public Object getProperty(Object key)
	  {
	    return this.properties.get(key);
	  }
	  
	  public RoomSize getSize()
	  {
	    int uCount = 0;
	    int sCount = 0;
	    if (this.game) {
	      for (User user : this.userManager.getAllUsers()) {
	        if (user.isSpectator(this)) {
	          sCount++;
	        } else {
	          uCount++;
	        }
	      }
	    } else {
	      uCount = this.userManager.getUserCount();
	    }
	    return new RoomSize(uCount, sCount);
	  }
	  
	  public void removeProperty(Object key)
	  {
	    this.properties.remove(key);
	  }
	  
	  public List<User> getSpectatorsList()
	  {
	    List<User> specList = new ArrayList();
	    for (User user : this.userManager.getAllUsers()) {
	      if (user.isSpectator(this)) {
	        specList.add(user);
	      }
	    }
	    return specList;
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
	  
	  public User getUserByPlayerId(int playerId)
	  {
	    User user = null;
	    for (User u : this.userManager.getAllUsers()) {
	      if (u.getPlayerId(this) == playerId)
	      {
	        user = u;
	        break;
	      }
	    }
	    return user;
	  }
	  
	  public List<User> getUserList()
	  {
	    return this.userManager.getAllUsers();
	  }
	  
	  public List<ChannelHandlerContext> getSessionList()
	  {
	    return this.userManager.getAllSessions();
	  }
	  
//	  public int getVariablesCount()
//	  {
//	    return this.variables.size();
//	  }
//	  
//	  public RoomVariable getVariable(String varName)
//	  {
//	    return (RoomVariable)this.variables.get(varName);
//	  }
//	  
//	  public List<RoomVariable> getVariables()
//	  {
//	    return new ArrayList(this.variables.values());
//	  }
//	  
//	  public List<RoomVariable> getVariablesCreatedByUser(User user)
//	  {
//	    List<RoomVariable> varList = new ArrayList();
//	    for (RoomVariable rVar : this.variables.values()) {
//	      if (rVar.getOwner() == user) {
//	        varList.add(rVar);
//	      }
//	    }
//	    return varList;
//	  }
	  
	  public boolean containsProperty(Object key)
	  {
	    return this.properties.containsKey(key);
	  }
	  
//	  public void removeVariable(String varName)
//	  {
//	    this.variables.remove(varName);
//	    if (this.logger.isDebugEnabled()) {
//	      this.logger.debug("RoomVar deleted: " + varName + " in " + this);
//	    }
//	  }
//	  
//	  public List<RoomVariable> removeVariablesCreatedByUser(User user)
//	  {
//	    List<RoomVariable> varList = getVariablesCreatedByUser(user);
//	    for (RoomVariable rVar : varList)
//	    {
//	      removeVariable(rVar.getName());
//	      
//
//
//
//
//
//	      rVar.setNull();
//	    }
//	    return varList;
//	  }
	  
	  public int getCapacity()
	  {
	    return this.maxUsers + this.maxSpectators;
	  }
	  
	  public void setCapacity(int maxUser, int maxSpectators)
	  {
	    this.maxUsers = maxUser;
	    this.maxSpectators = maxSpectators;
	  }
	  
	  public void setMaxRoomVariablesAllowed(int max)
	  {
	    this.maxRoomVariablesAllowed = max;
	  }
	  
	  public int getMaxRoomVariablesAllowed()
	  {
	    return this.maxRoomVariablesAllowed;
	  }
	  
	  public void setFlags(Set<SFSRoomSettings> settings)
	  {
	    this.flags = settings;
	  }
	  
	  public boolean isFlagSet(SFSRoomSettings flag)
	  {
	    return this.flags.contains(flag);
	  }
	  
	  public void setFlag(SFSRoomSettings flag, boolean state)
	  {
	    if (state) {
	      this.flags.add(flag);
	    } else {
	      this.flags.remove(flag);
	    }
	  }
	  
	  public boolean isUseWordsFilter()
	  {
	    return this.userWordsFilter;
	  }
	  
	  public void setUseWordsFilter(boolean useWordsFilter)
	  {
	    this.userWordsFilter = useWordsFilter;
	  }
	  
	  public void setProperty(Object key, Object value)
	  {
	    this.properties.put(key, value);
	  }
	  
//	  public void setVariables(List<RoomVariable> variables)
//	  {
//	    setVariables(variables, false);
//	  }
//	  
//	  public void setVariables(List<RoomVariable> variables, boolean overrideOwnership)
//	  {
//	    for (RoomVariable var : variables) {
//	      try
//	      {
//	        setVariable(var);
//	      }
//	      catch (SFSVariableException e)
//	      {
//	        this.logger.warn(e.getMessage());
//	      }
//	    }
//	  }
//	  
//	  public void setVariable(RoomVariable roomVariable)
//	    throws SFSVariableException
//	  {
//	    setVariable(roomVariable, false);
//	  }
	  
	  public void destroy() {}
	  
//	  public void setVariable(RoomVariable roomVariable, boolean overrideOwnership)
//	    throws SFSVariableException
//	  {
//	    if (this.maxRoomVariablesAllowed < 1) {
//	      throw new SFSVariableException("Room Variables are disabled: " + toString());
//	    }
//	    String varName = roomVariable.getName();
//	    RoomVariable oldVariable = (RoomVariable)this.variables.get(varName);
//	    if (roomVariable.getType() == VariableType.NULL)
//	    {
//	      if (oldVariable == null) {
//	        throw new SFSVariableException("Cannot delete non-existent Room Variable called: " + roomVariable.getName() + ", Owner: " + roomVariable.getOwner());
//	      }
//	      deleteVariable(oldVariable, roomVariable, overrideOwnership);
//	    }
//	    else if (oldVariable != null)
//	    {
//	      modifyVariable(oldVariable, roomVariable, overrideOwnership);
//	    }
//	    else
//	    {
//	      addVariable(roomVariable, overrideOwnership);
//	    }
//	  }
//	  
//	  private void addVariable(RoomVariable var, boolean overrideOwnership)
//	    throws SFSVariableException
//	  {
//	    if (this.variables.size() >= this.maxRoomVariablesAllowed) {
//	      throw new SFSVariableException(
//	      
//	        String.format(
//	        
//	        "The max number of variables (%s) for this Room: %s was reached. Discarding variable: %s", new Object[] {
//	        Integer.valueOf(this.maxRoomVariablesAllowed), 
//	        this.name, 
//	        var.getName() }));
//	    }
//	    this.variables.put(var.getName(), var);
//	    if (this.logger.isDebugEnabled()) {
//	      this.logger.debug(String.format("RoomVar created: %s in %s ", new Object[] { var, this }));
//	    }
//	  }
//	  
//	  private void modifyVariable(RoomVariable oldVariable, RoomVariable newVariable, boolean overrideOwnership)
//	    throws SFSVariableException
//	  {
//	    if (overrideOwnership) {
//	      overwriteVariable(oldVariable, newVariable);
//	    } else if (oldVariable.isPrivate())
//	    {
//	      if (oldVariable.getOwner() == newVariable.getOwner()) {
//	        overwriteVariable(oldVariable, newVariable);
//	      } else {
//	        throw new SFSVariableException(String.format("Variable: %s cannot be changed by user: %s", new Object[] { oldVariable, newVariable.getOwner() }));
//	      }
//	    }
//	    else {
//	      overwriteVariable(oldVariable, newVariable);
//	    }
//	  }
//	  
//	  private void overwriteVariable(RoomVariable oldRv, RoomVariable newRv)
//	  {
//	    if (oldRv.getOwner() == null) {
//	      newRv.setOwner(null);
//	    }
//	    newRv.setHidden(oldRv.isHidden());
//	    newRv.setGlobal(oldRv.isGlobal());
//	    
//	    this.variables.put(newRv.getName(), newRv);
//	    if (this.logger.isDebugEnabled()) {
//	      this.logger.debug(String.format("RoomVar changed: %s in %s ", new Object[] { newRv, this }));
//	    }
//	  }
//	  
//	  private void deleteVariable(RoomVariable oldVariable, RoomVariable newVariable, boolean overrideOwnership)
//	    throws SFSVariableException
//	  {
//	    if (overrideOwnership) {
//	      removeVariable(oldVariable.getName());
//	    } else if (oldVariable.isPrivate())
//	    {
//	      if (oldVariable.getOwner() == newVariable.getOwner()) {
//	        removeVariable(oldVariable.getName());
//	      } else {
//	        throw new SFSVariableException("Variable: " + oldVariable + " cannot be deleted by user: " + newVariable.getOwner());
//	      }
//	    }
//	    else {
//	      removeVariable(oldVariable.getName());
//	    }
//	  }
//	  
//	  public boolean containsVariable(String varName)
//	  {
//	    return this.variables.containsKey(varName);
//	  }
	  
	  public boolean containsUser(String name)
	  {
	    return this.userManager.containsName(name);
	  }
	  
	  public boolean containsUser(User user)
	  {
	    return this.userManager.containsUser(user);
	  }
	  
	  public void addUser(User user)
	    throws OSException
	  {
	    addUser(user, false);
	  }
	  
	  public void addUser(User user, boolean asSpectator)
	    throws OSException
	  {
	    if (this.userManager.containsId(user.getId()))
	    {
	      String message = String.format("User already joined: %s, Room: %s, Zone: %s", new Object[] { user, this, getZone() });
//	      SFSErrorData data = new SFSErrorData(SFSErrorCode.JOIN_ALREADY_JOINED);
//	      data.addParameter(this.name);
	      
	      throw new OSException(message);
	    }
	    boolean okToAdd = false;
	    synchronized (this)
	    {
	      RoomSize roomSize = getSize();
	      if ((isGame()) && (asSpectator)) {
	        okToAdd = roomSize.getSpectatorCount() < this.maxSpectators;
	      } else {
	        okToAdd = roomSize.getUserCount() < this.maxUsers;
	      }
	      if (!okToAdd)
	      {
	        String message = String.format("Room is full: %s, Zone: %s - Can't add User: %s ", new Object[] { this.name, this.zone, user });
//	        SFSErrorData data = new SFSErrorData(SFSErrorCode.JOIN_ROOM_FULL);
//	        data.addParameter(this.name);
	        
	        throw new OSException(message);
	      }
	      this.userManager.addUser(user);
	    }
	    user.addJoinedRoom(this);
	    if (isGame())
	    {
	      if (asSpectator) {
	        user.setPlayerId(-1, this);
	      } else {
	        user.setPlayerId(this.playerIdGenerator.getPlayerSlot(), this);
	      }
	    }
	    else {
	      user.setPlayerId(0, this);
	    }
	  }
	  
	  public void removeUser(User user)
	  {
	    if (isGame()) {
	      this.playerIdGenerator.freePlayerSlot(user.getPlayerId(this));
	    }
	    this.userManager.removeUser(user);
	    user.removeJoinedRoom(this);
	  }
	  
//	  public void switchPlayerToSpectator(User user)
//	    throws OSException
//	  {
//	    if (!isGame())
//	    {
//	      SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NOT_A_GAME_ROOM);
//	      errData.addParameter(this.name);
//	      
//	      throw new SFSRoomException("Not supported in a non-game room", errData);
//	    }
//	    if (!this.userManager.containsUser(user))
//	    {
//	      SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NOT_JOINED_IN_ROOM);
//	      errData.addParameter(this.name);
//	      
//	      throw new SFSRoomException(String.format("%s is not joined in %s", new Object[] { user, this }));
//	    }
//	    if (user.isSpectator(this))
//	    {
//	      this.logger.warn(String.format("PlayerToSpectator refused, %s is already a spectator in %s", new Object[] { user, this }));
//	      return;
//	    }
//	    this.switchUserLock.lock();
//	    try
//	    {
//	      if (getSize().getSpectatorCount() < this.maxSpectators)
//	      {
//	        int currentPlayerId = user.getPlayerId(this);
//	        
//
//	        user.setPlayerId(-1, this);
//	        
//
//	        this.playerIdGenerator.freePlayerSlot(currentPlayerId);
//	      }
//	      else
//	      {
//	        SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE);
//	        errData.addParameter(this.name);
//	        
//	        throw new SFSRoomException("All Spectators slots are already occupied!", errData);
//	      }
//	    }
//	    finally
//	    {
//	      this.switchUserLock.unlock();
//	    }
//	    this.switchUserLock.unlock();
//	  }
//	  
//	  public void switchSpectatorToPlayer(User user)
//	    throws SFSRoomException
//	  {
//	    if (!isGame())
//	    {
//	      SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NOT_A_GAME_ROOM);
//	      errData.addParameter(this.name);
//	      
//	      throw new SFSRoomException("Not supported in a non-game room", errData);
//	    }
//	    if (!this.userManager.containsUser(user))
//	    {
//	      SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NOT_JOINED_IN_ROOM);
//	      errData.addParameter(this.name);
//	      
//	      throw new SFSRoomException(String.format("%s is not joined in %s", new Object[] { user, this }));
//	    }
//	    if (user.isPlayer(this))
//	    {
//	      this.logger.warn(String.format("SpectatorToPlayer refused, %s is already a player in %s", new Object[] { user, this }));
//	      return;
//	    }
//	    this.switchUserLock.lock();
//	    try
//	    {
//	      if (getSize().getUserCount() < this.maxUsers)
//	      {
//	        user.setPlayerId(this.playerIdGenerator.getPlayerSlot(), this);
//	      }
//	      else
//	      {
//	        SFSErrorData errData = new SFSErrorData(SFSErrorCode.SWITCH_NO_PLAYER_SLOTS_AVAILABLE);
//	        errData.addParameter(this.name);
//	        
//	        throw new SFSRoomException("All Player slots are already occupied!", errData);
//	      }
//	    }
//	    finally
//	    {
//	      this.switchUserLock.unlock();
//	    }
//	    this.switchUserLock.unlock();
//	  }
	  
	  public long getLifeTime()
	  {
	    return System.currentTimeMillis() - this.lifeTime;
	  }
	  
	  public boolean isEmpty()
	  {
	    return this.userManager.getUserCount() == 0;
	  }
	  
	  public boolean isFull()
	  {
	    if (isGame()) {
	      return getSize().getUserCount() == this.maxUsers;
	    }
	    return this.userManager.getUserCount() == this.maxUsers;
	  }
	  
//	  public ISFSArray getUserListData()
//	  {
//	    ISFSArray userListData = SFSArray.newInstance();
//	    for (User user : this.userManager.getAllUsers())
//	    {
//	      ISFSArray userObj = SFSArray.newInstance();
//	      
//	      userObj.addInt(user.getId());
//	      userObj.addUtfString(user.getName());
//	      userObj.addShort(user.getPrivilegeId());
//	      userObj.addShort((short)user.getPlayerId(this));
//	      userObj.addSFSArray(user.getUserVariablesData());
//	      
//	      userListData.addSFSArray(userObj);
//	    }
//	    return userListData;
//	  }
//	  
//	  public ISFSArray getRoomVariablesData(boolean globalsOnly)
//	  {
//	    ISFSArray variablesData = SFSArray.newInstance();
//	    for (RoomVariable var : this.variables.values()) {
//	      if (!var.isHidden()) {
//	        if ((!globalsOnly) || (var.isGlobal())) {
//	          variablesData.addSFSArray(var.toSFSArray());
//	        }
//	      }
//	    }
//	    return variablesData;
//	  }
	  
	  public String toString()
	  {
	    return String.format("[ Room: %s, Id: %s, Group: %s, isGame: %s ]", new Object[] { this.name, Integer.valueOf(this.id), this.groupId, Boolean.valueOf(this.game) });
	  }
	  
	  public boolean equals(Object obj)
	  {
	    if (!(obj instanceof Room)) {
	      return false;
	    }
	    Room room = (Room)obj;
	    boolean isEqual = false;
	    if (room.getId() == this.id) {
	      isEqual = true;
	    }
	    return isEqual;
	  }
	  
	  public BaseExtension getExtension()
	  {
	    return this.extension;
	  }
	  
	  public void setExtension(BaseExtension extension)
	  {
	    this.extension = extension;
	  }
	  
//	  public ISFSArray toSFSArray(boolean globalRoomVarsOnly)
//	  {
//	    RoomSize roomSize = getSize();
//	    
//	    ISFSArray roomObj = SFSArray.newInstance();
//	    roomObj.addInt(this.id);
//	    roomObj.addUtfString(this.name);
//	    roomObj.addUtfString(this.groupId);
//	    
//	    roomObj.addBool(isGame());
//	    roomObj.addBool(isHidden());
//	    roomObj.addBool(isPasswordProtected());
//	    
//	    roomObj.addShort((short)roomSize.getUserCount());
//	    roomObj.addShort((short)this.maxUsers);
//	    
//
//	    roomObj.addSFSArray(getRoomVariablesData(globalRoomVarsOnly));
//	    if (isGame())
//	    {
//	      roomObj.addShort((short)roomSize.getSpectatorCount());
//	      roomObj.addShort((short)this.maxSpectators);
//	    }
//	    return roomObj;
//	  }
	  
//	  public String getDump()
//	  {
//	    StringBuilder sb = new StringBuilder("/////////////// Room Dump ////////////////").append("\n");
//	    sb.append("\tName: ").append(this.name).append("\n")
//	      .append("\tId: ").append(this.id).append("\n")
//	      .append("\tGroupId: ").append(this.groupId).append("\n")
//	      .append("\tPassword: ").append(this.password).append("\n")
//	      .append("\tOwner: ").append(this.owner == null ? "[[ SERVER ]]" : this.owner.toString()).append("\n")
//	      .append("\tisDynamic: ").append(this.dynamic).append("\n")
//	      .append("\tisGame: ").append(this.game).append("\n")
//	      .append("\tisHidden: ").append(this.hidden).append("\n")
//	      .append("\tsize: ").append(getSize()).append("\n")
//	      .append("\tMaxUser: ").append(this.maxUsers).append("\n")
//	      .append("\tMaxSpect: ").append(this.maxSpectators).append("\n")
//	      .append("\tMaxVars: ").append(this.maxRoomVariablesAllowed).append("\n")
//	      .append("\tRemoveMode: ").append(this.autoRemoveMode).append("\n")
//	      .append("\tPlayerIdGen: ").append(this.playerIdGenerator).append("\n")
//	      .append("\tSettings: ").append("\n");
//	    for (SFSRoomSettings setting : SFSRoomSettings.values()) {
//	      sb.append("\t\t").append(setting).append(": ").append(this.flags.contains(setting)).append("\n");
//	    }
//	    if (this.variables.size() > 0)
//	    {
//	      sb.append("\tRoomVariables: ").append("\n");
//	      for (RoomVariable var : this.variables.values()) {
//	        sb.append("\t\t").append(var.toString()).append("\n");
//	      }
//	    }
//	    if (this.properties.size() > 0)
//	    {
//	      sb.append("\tProperties: ").append("\n");
//	      for (Object key : this.properties.keySet()) {
//	        sb.append("\t\t").append(key).append(": ").append(this.properties.get(key)).append("\n");
//	      }
//	    }
//	    if (this.extension != null)
//	    {
//	      sb.append("\tExtension: ").append("\n");
//	      
//	      sb.append("\t\t").append("Name: ").append(this.extension.getName()).append("\n");
//	      sb.append("\t\t").append("Class: ").append(this.extension.getExtensionFileName()).append("\n");
//	      sb.append("\t\t").append("Type: ").append(this.extension.getType()).append("\n");
//	      sb.append("\t\t").append("Props: ").append(this.extension.getPropertiesFileName()).append("\n");
//	    }
//	    sb.append("/////////////// End Dump /////////////////").append("\n");
//	    
//	    return sb.toString();
//	  }
	  
//	  public IAdminHelper getAdminHelper()
//	  {
//	    return this.adminHelper;
//	  }
//	  
//	  public void setAdminHelper(IAdminHelper helper)
//	  {
//	    this.adminHelper = helper;
//	  }
	  
	  public String getPlayerIdGeneratorClassName()
	  {
	    return this.playerIdGenerator.getClass().getName();
	  }
	  
	  public void setProperties(Map<Object, Object> props)
	  {
	    this.properties.clear();
	    this.properties.putAll(props);
	  }
	  
	  private void instantiateRoomIdGenerator()
	  {
	    String className;// = this.zone.getDefaultPlayerIdGeneratorClassName();
	    //if (className == null) {
	      className = "com.smartfoxserver.v2.util.DefaultPlayerIdGenerator";
	    //}
	    try
	    {
	      Class<?> theClass = Class.forName(className);
	      this.playerIdGenerator = ((DefaultPlayerIdGenerator)theClass.newInstance());
	    }
	    catch (Exception e)
	    {
	      this.logger.error("Could not instantiate the IPlayerIdGenerator object. Room: " + this + ", class: " + className + ", err: " + e);
	    }
	  }
	  
	  private void populateTransientFields()
	  {
	    this.logger = LoggerFactory.getLogger(getClass());
	    

	    this.extension = null;
	  }
}
