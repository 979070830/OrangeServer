package com.orange.entities;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.server.OrangeServerEngine;
import com.orange.util.IDisconnectionReason;

public class User{
	private static AtomicInteger autoID = new AtomicInteger(0);
	  private int id;
	  private ChannelHandlerContext session = null;
	  private String name;
	  private short privilegeId = 0;
	  private volatile long lastLoginTime = 0L;
	  private final Set<String> registeredGroups;
	  private final LinkedList<Room> joinedRooms;
	  private final Set<Room> createdRooms;
	  private final ConcurrentMap<Integer, Integer> playerIdByRoomId;
	  private final ConcurrentMap<Object, Object> properties;
	  //private final ConcurrentMap<String, UserVariable> variables;
	  private volatile int ownedRoomsCount = 0;
	  private volatile int badWordsWarnings = 0;
	  private volatile int floodWarnings = 0;
	  private volatile boolean beingKicked = false;//是否被踢了
	  private volatile boolean connected = false;
	  private boolean joining = false;
	  private int maxVariablesAllowed = 0;
	  private Zone currentZone;
	  private Logger logger;
	  private volatile List<User> proxyList;

	  private static int getNewID()
	  {
	    return autoID.getAndIncrement();
	  }
	  public User(ChannelHandlerContext session)
	  {
		  this("", session);  
	  }
	  public User(String name, ChannelHandlerContext session)
	  {
	    this.id = getNewID();
	    this.name = name;
	    this.session = session;
	    this.beingKicked = false;
	    
	    this.joinedRooms = new LinkedList();
	    this.properties = new ConcurrentHashMap();
	    this.playerIdByRoomId = new ConcurrentHashMap();
	    //this.variables = new ConcurrentHashMap();
	    this.registeredGroups = new HashSet();
	    this.createdRooms = new HashSet();
	    
	    updateLastRequestTime();
	    this.logger = LoggerFactory.getLogger(getClass());
	    
	    if(session != null)
	    {
	    	OrangeServerEngine.getInstance().getSessionManager().addUser(session, this);
	    }
	  }
	  

	  
//	  public BuddyProperties getBuddyProperties()
//	  {
//	    return this.buddyProperties;
//	  }
	  
	  public int getId()
	  {
	    return this.id;
	  }
	  
	  public short getPrivilegeId()
	  {
	    return this.privilegeId;
	  }
	  
	  public void setPrivilegeId(short id)
	  {
	    this.privilegeId = id;
	  }
	  
//	  public boolean isSuperUser()
//	  {
//	    return this.currentZone.getPrivilegeManager().isFlagSet(this, SystemPermission.SuperUser);
//	  }
	  
	  public boolean isConnected()
	  {
	    return this.connected;
	  }
	  
//	  public boolean isLocal()
//	  {
//	    return this.session.isLocal();
//	  }
	  
	  public synchronized void setConnected(boolean flag)
	  {
	    this.connected = flag;
	  }
	  
	  public synchronized boolean isJoining()
	  {
	    return this.joining;
	  }
	  
	  public synchronized void setJoining(boolean flag)
	  {
	    this.joining = flag;
	  }
	  
//	  public String getIpAddress()
//	  {
//	    return this.session.getAddress();
//	  }
	  
	  public int getMaxAllowedVariables()
	  {
	    return this.maxVariablesAllowed;
	  }
	  
	  public synchronized void setMaxAllowedVariables(int max)
	  {
	    this.maxVariablesAllowed = max;
	  }
	  
	  public void addCreatedRoom(Room room)
	  {
	    synchronized (this.createdRooms)
	    {
	      this.createdRooms.add(room);
	    }
	  }
	  
	  public List<Room> getCreatedRooms()
	  {
	    List<Room> rooms = null;
	    synchronized (this.createdRooms)
	    {
	      rooms = new ArrayList(this.createdRooms);
	    }
	    return rooms;
	  }
	  
	  public void removeCreatedRoom(Room room)
	  {
	    synchronized (this.createdRooms)
	    {
	      this.createdRooms.remove(room);
	    }
	  }
	  
	  public void addJoinedRoom(Room room)
	  {
	    synchronized (this.joinedRooms)
	    {
	      if (!this.joinedRooms.contains(room)) {
	        this.joinedRooms.add(room);
	      }
	    }
	  }
	  
	  public void removeJoinedRoom(Room room)
	  {
	    synchronized (this.joinedRooms)
	    {
	      this.joinedRooms.remove(room);
	    }
	    this.playerIdByRoomId.remove(Integer.valueOf(room.getId()));
	  }
	  
	  public int getOwnedRoomsCount()
	  {
	    return this.ownedRoomsCount;
	  }
	  
	  public void subscribeGroup(String id)
	  {
	    synchronized (this.registeredGroups)
	    {
	      this.registeredGroups.add(id);
	    }
	  }
	  
	  public void unsubscribeGroup(String id)
	  {
	    synchronized (this.registeredGroups)
	    {
	      this.registeredGroups.remove(id);
	    }
	  }
	  
	  public List<String> getSubscribedGroups()
	  {
	    List<String> theGroups = null;
	    synchronized (this.registeredGroups)
	    {
	      theGroups = new ArrayList(this.registeredGroups);
	    }
	    return theGroups;
	  }
	  
	  public boolean isSubscribedToGroup(String id)
	  {
	    boolean found = false;
	    synchronized (this.registeredGroups)
	    {
	      found = this.registeredGroups.contains(id);
	    }
	    return found;
	  }
	  
	  public void disconnect(IDisconnectionReason reason)
	  {
	    //SmartFoxServer.getInstance().getAPIManager().getSFSApi().disconnectUser(this, reason);
	  }
	  
	  public boolean isNpc()
	  {
	    return this.session == null;
	  }
	  
	  public List<Room> getJoinedRooms()
	  {
	    List<Room> rooms;
	    synchronized (this.joinedRooms)
	    {
	      rooms = new ArrayList(this.joinedRooms);
	    }
	    return rooms;
	  }
	  
	  public Zone getZone()
	  {
	    return this.currentZone;
	  }
	  
	  public void setZone(Zone currentZone)
	  {
	    if (this.currentZone != null) {
	      throw new IllegalStateException("The User Zone is already set. It cannot be modified at Runtime. " + this);
	    }
	    this.currentZone = currentZone;
//	    if (currentZone.getBuddyListManager().isActive()) {
//	      this.buddyProperties = new SFSBuddyProperties();
//	    }
	  }
	  
	  public Room getLastJoinedRoom()
	  {
		  Room lastRoom = null;
	    synchronized (this.joinedRooms)
	    {
	      if (this.joinedRooms.size() > 0) {
	        lastRoom = (Room)this.joinedRooms.getLast();
	      }
	    }
	    return lastRoom;
	  }
	  
	  public boolean isJoinedInRoom(Room room)
	  {
	    boolean found = false;
	    synchronized (this.joinedRooms)
	    {
	      found = this.joinedRooms.contains(room);
	    }
	    return found;
	  }
	  
	  public long getLoginTime()
	  {
	    return this.lastLoginTime;
	  }
	  
	  public void setLastLoginTime(long lastLoginTime)
	  {
	    this.lastLoginTime = lastLoginTime;
	  }
	  
	  public String getName()
	  {
	    return this.name;
	  }
	  
	  public void setName(String name)
	  {
	    this.name = name;
	  }
	  
	  public int getPlayerId()
	  {
	    Room theRoom = getLastJoinedRoom();
	    if (theRoom == null) {
	      return 0;
	    }
	    return ((Integer)this.playerIdByRoomId.get(Integer.valueOf(theRoom.getId()))).intValue();
	  }
	  
	  public int getPlayerId(Room room)
	  {
	    if (room == null) {
	      return 0;
	    }
	    Integer playerId = (Integer)this.playerIdByRoomId.get(Integer.valueOf(room.getId()));
	    if (playerId == null)
	    {
	      this.logger.info("Can't find playerID -- User: " + this.name + " is not joined in the requested Room: " + room);
	      playerId = Integer.valueOf(0);
	    }
	    return playerId.intValue();
	  }
	  
	  public Map<Room, Integer> getPlayerIds()
	  {
	    Map<Room, Integer> allPlayerIds = new HashMap();
	    synchronized (this.joinedRooms)
	    {
	      for (Room room : this.joinedRooms) {
	        allPlayerIds.put(room, Integer.valueOf(getPlayerId(room)));
	      }
	    }
	    return allPlayerIds;
	  }
	  
	  public void setPlayerId(int id, Room room)
	  {
	    this.playerIdByRoomId.put(Integer.valueOf(room.getId()), Integer.valueOf(id));
	  }
	  
	  public boolean isPlayer()
	  {
	    return isPlayer(getLastJoinedRoom());
	  }
	  
	  public boolean isSpectator()
	  {
	    return isSpectator(getLastJoinedRoom());
	  }
	  
	  public boolean isPlayer(Room room)
	  {
	    return getPlayerId(room) > 0;
	  }
	  
	  public boolean isSpectator(Room room)
	  {
	    return getPlayerId(room) < 0;
	  }
	  
	  public Object getProperty(Object key)
	  {
	    return this.properties.get(key);
	  }
	  
	  public void setProperty(Object key, Object val)
	  {
	    this.properties.put(key, val);
	  }
	  
	  public boolean containsProperty(Object key)
	  {
	    return this.properties.containsKey(key);
	  }
	  
	  public void removeProperty(Object key)
	  {
	    this.properties.remove(key);
	  }
	  
	  public ChannelHandlerContext getSession()
	  {
	    return this.session;
	  }
	  
//	  public int getVariablesCount()
//	  {
//	    return this.variables.size();
//	  }
	  
//	  public UserVariable getVariable(String varName)
//	  {
//	    return (UserVariable)this.variables.get(varName);
//	  }
//	  
//	  public void setVariable(UserVariable var)
//	    throws SFSVariableException
//	  {
//	    String varName = var.getName();
//	    if (var.getType() == VariableType.NULL)
//	    {
//	      removeVariable(varName);
//	    }
//	    else
//	    {
//	      if (!containsVariable(varName)) {
//	        if (this.variables.size() >= this.maxVariablesAllowed) {
//	          throw new SFSVariableException("The max number of variables (" + this.maxVariablesAllowed + ") for this User: " + this.name + " was reached. Discarding variable: " + varName);
//	        }
//	      }
//	      this.variables.put(varName, var);
//	      if (this.logger.isDebugEnabled()) {
//	        this.logger.debug(String.format("UserVar set: %s, %s ", new Object[] { var, this }));
//	      }
//	    }
//	  }
//	  
//	  public void setVariables(List<UserVariable> userVariables)
//	    throws SFSVariableException
//	  {
//	    for (UserVariable uVar : userVariables) {
//	      setVariable(uVar);
//	    }
//	  }
//	  
//	  public boolean containsVariable(String varName)
//	  {
//	    return this.variables.containsKey(varName);
//	  }
//	  
//	  public List<UserVariable> getVariables()
//	  {
//	    return new ArrayList(this.variables.values());
//	  }
//	  
//	  public void removeVariable(String varName)
//	  {
//	    this.variables.remove(varName);
//	    if (this.logger.isDebugEnabled()) {
//	      this.logger.debug(String.format("UserVar removed: %s, %s", new Object[] { varName, this }));
//	    }
//	  }
	  
//	  public String toString()
//	  {
//	    return String.format("( User Name: %s, Id: %s, Priv: %s, Sess: %s ) ", new Object[] { this.name, Integer.valueOf(this.id), Short.valueOf(this.privilegeId), this.session.getFullIpAddress() });
//	  }
	  
//	  public long getLastRequestTime()
//	  {
//	    return this.session.getLastLoggedInActivityTime();
//	  }
//	  
	  public synchronized void updateLastRequestTime()
	  {
	    setLastRequestTime(System.currentTimeMillis());
	  }
	  
	  public void setLastRequestTime(long lastRequestTime)
	  {
	    //this.session.setLastLoggedInActivityTime(lastRequestTime);
	  }
	  
	  public int getBadWordsWarnings()
	  {
	    return this.badWordsWarnings;
	  }
	  
	  public void setBadWordsWarnings(int badWordsWarnings)
	  {
	    this.badWordsWarnings = badWordsWarnings;
	  }
	  
	  public int getFloodWarnings()
	  {
	    return this.floodWarnings;
	  }
	  
	  public void setFloodWarnings(int floodWarnings)
	  {
	    this.floodWarnings = floodWarnings;
	  }
	  
	  public long getLastLoginTime()
	  {
	    return this.lastLoginTime;
	  }
	  
	  public boolean isBeingKicked()
	  {
	    return this.beingKicked;
	  }
	  
	  public void setBeingKicked(boolean flag)
	  {
	    this.beingKicked = flag;
	  }
	  
//	  public String getDump()
//	  {
//	    StringBuilder sb = new StringBuilder("/////////////// User Dump ////////////////").append("\n");
//	    
//	    sb.append("\tName: ").append(this.name).append("\n")
//	      .append("\tId: ").append(this.id).append("\n")
//	      .append("\tHash: ").append(this.session.getHashId()).append("\n")
//	      .append("\tZone: ").append(getZone()).append("\n")
//	      .append("\tIP Address: ").append(getIpAddress()).append("\n")
//	      .append("\tPrivilegeId: ").append(getPrivilegeId()).append("\n")
//	      .append("\tisSubscribed Groups: ").append(getSubscribedGroups()).append("\n")
//	      .append("\tLast Joined Room: ").append(getLastJoinedRoom()).append("\n")
//	      .append("\tJoined Rooms: ").append(getJoinedRooms()).append("\n");
//	    if (this.variables.size() > 0)
//	    {
//	      sb.append("\tUserVariables: ").append("\n");
//	      for (UserVariable var : this.variables.values()) {
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
//	    sb.append("/////////////// End Dump /////////////////").append("\n");
//	    
//	    return sb.toString();
//	  }

	  public void setReconnectionSeconds(int i)
	  {
		  System.out.println("需要实现该方法 setReconnectionSeconds");
	  }
	  
	  public void setLoggedIn(boolean bool)
	  {
		  System.out.println("需要实现该方法 setLoggedIn");
	  }
}
