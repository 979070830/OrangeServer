package com.orange.api;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.orange.api.response.SFSResponseApi;
import com.orange.config.CreateRoomSettings;
import com.orange.core.ISFSEventParam;
import com.orange.core.SFSEvent;
import com.orange.core.SFSEventParam;
import com.orange.core.SFSEventType;
import com.orange.entities.Room;
import com.orange.entities.SFSRoomSettings;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.entities.managers.UserManager;
import com.orange.exceptions.OSException;
import com.orange.server.OrangeServerEngine;
import com.orange.util.ClientDisconnectionReason;
import com.orange.util.IDisconnectionReason;

public class SFSApi {
	protected final OrangeServerEngine sfs;
	  protected final Logger log;
	  protected UserManager globalUserManager;
//	  private final LoginErrorHandler loginErrorHandler;
//	  private final MatchingUtils matcher;
	  protected final SFSResponseApi responseAPI;
	  
	  public SFSApi(OrangeServerEngine sfs)
	  {
	    this.log = LoggerFactory.getLogger(getClass());
	    this.sfs = sfs;
	    this.globalUserManager = sfs.getUserManager();
//	    this.loginErrorHandler = new LoginErrorHandler();
//	    this.matcher = MatchingUtils.getInstance();
	    this.responseAPI = new SFSResponseApi();
	  }
	  
	  public SFSResponseApi getResponseAPI()
	  {
	    return this.responseAPI;
	  }
	  
	  public User getUserById(int userId)
	  {
	    return this.globalUserManager.getUserById(userId);
	  }
	  
	  public User getUserByName(String name)
	  {
	    return this.globalUserManager.getUserByName(name);
	  }
	  
	  public User getUserBySession(ChannelHandlerContext session)
	  {
	    return this.globalUserManager.getUserBySession(session);
	  }
	  
//	  public void kickUser(User userToKick, User modUser, String kickMessage, int delaySeconds)
//	  {
//	    this.log.info("Kicking user: " + userToKick);
//	    this.sfs.getBannedUserManager().kickUser(userToKick, modUser, kickMessage, delaySeconds);
//	  }
//	  
//	  public void banUser(User userToBan, User modUser, String banMessage, BanMode mode, int durationMinutes, int delaySeconds)
//	  {
//	    this.sfs.getBannedUserManager().banUser(
//	    
//	      userToBan, 
//	      modUser, 
//	      durationMinutes, 
//	      mode, 
//	      "", 
//	      banMessage, 
//	      delaySeconds);
//	  }
	  
//	  public List<Room> findRooms(Collection<Room> roomList, MatchExpression expression, int limit)
//	  {
//	    return this.matcher.matchRooms(roomList, expression, limit);
//	  }
//	  
//	  public List<User> findUsers(Collection<User> userList, MatchExpression expression, int limit)
//	  {
//	    return this.matcher.matchUsers(userList, expression, limit);
//	  }
	  
	  public void disconnect(ChannelHandlerContext session)
	  {
	    if (session == null) {
	      throw new OSException("Unexpected, cannot disconnect session. Session object is null.");
	    }
	    User lostUser = this.globalUserManager.getUserBySession(session);
	    if (lostUser != null) {
	      disconnectUser(lostUser);
	    } 
	    else if (!session.isRemoved()/*session.isConnected()*/) {
	      try
	      {
	        session.close();
	      }
	      catch (Exception err)
	      {
	        throw new OSException(err.getMessage());
	      }
	    }
	  }
	  
	  public void disconnectUser(User user, IDisconnectionReason reason)
	  {
	    //user.getSession().setSystemProperty("disconnectionReason", reason);
	    this.responseAPI.notifyClientSideDisconnection(user, reason);
	  }
	  
	  public void disconnectUser(User user)
	  {
	    if (user == null) {
	      throw new OSException("Cannot disconnect user, User object is null.");
	    }
	    ChannelHandlerContext session = user.getSession();
	    Zone zone = user.getZone();

	    List<Room> joinedRooms = user.getJoinedRooms();
	    

	    Map<Room, Integer> playerIds = user.getPlayerIds();
	    boolean goodToGo;
	    IDisconnectionReason disconnectionReason = null;
	    try
	    {
//	      if (session.isConnected()) {
//	        session.close();
//	      }
	      //TODO 修改
	      if(!session.isRemoved())
	      {
	    	  session.disconnect();
	      }
	      user.setConnected(false);
	    }
	    catch (OSException err)
	    {
	      throw new OSException(err.getMessage());
	    }
	    finally
	    {
	      if (zone != null) {
	        zone.removeUser(user);
	      }
	      this.globalUserManager.removeUser(user);
	      

	      this.responseAPI.notifyUserLost(user, joinedRooms);
	      

	      user.setConnected(false);
	      for (Room r : joinedRooms)
	      {
	    	goodToGo = (r != null) && (r.isActive());
	        if (goodToGo)
	        {
	          this.responseAPI.notifyUserCountChange(zone, r);
	          

	          //this.responseAPI.notifyRoomVariablesUpdate(r, r.removeVariablesCreatedByUser(user));
	        }
	      }
	      Map<ISFSEventParam, Object> evtParams = new HashMap();
	      evtParams.put(SFSEventParam.ZONE, zone);
	      evtParams.put(SFSEventParam.USER, user);
	      evtParams.put(SFSEventParam.JOINED_ROOMS, joinedRooms);
	      evtParams.put(SFSEventParam.PLAYER_IDS_BY_ROOM, playerIds);
	      
	      //disconnectionReason = (IDisconnectionReason)user.getSession().getSystemProperty("disconnectionReason");
	      evtParams.put(SFSEventParam.DISCONNECTION_REASON, disconnectionReason == null ? ClientDisconnectionReason.UNKNOWN : disconnectionReason);
	      

	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_DISCONNECT, evtParams));
	      for (Room r : user.getCreatedRooms()) {
	        if ((r != null) && (!joinedRooms.contains(r))) {
	          zone.checkAndRemove(r);
	        }
	      }
	      this.log.info(
	      
	        String.format(
	        
	        "User disconnected: %s, %s, SessionLen: %s, Type: %s", new Object[] {
	        user.getZone().toString(), 
	        user.toString(), 
	        Long.valueOf(System.currentTimeMillis() - user.getLoginTime())
	        //,user.getSession().getSystemProperty("ClientType") 
	        }));
	    }
	  }
	  
	  public void removeRoom(Room room)
	  {
	    removeRoom(room, true, true);
	  }
	  
	  public void removeRoom(Room room, boolean fireClientEvent, boolean fireServerEvent)
	  {
	    room.getZone().removeRoom(room);
	    if (room.getOwner() != null) {
	      room.getOwner().removeCreatedRoom(room);
	    }
	    if (fireClientEvent) {
	      this.responseAPI.notifyRoomRemoved(room);
	    }
	    if (fireServerEvent)
	    {
	      Map<ISFSEventParam, Object> evtParams = new HashMap();
	      evtParams.put(SFSEventParam.ZONE, room.getZone());
	      evtParams.put(SFSEventParam.ROOM, room);
	      
	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_REMOVED, evtParams));
	    }
	  }
	  
//	  public boolean checkSecurePassword(ChannelHandlerContext session, String originalPass, String encryptedPass)
//	  {
//	    if ((originalPass == null) || (originalPass.length() < 1)) {
//	      return false;
//	    }
//	    if ((encryptedPass == null) || (encryptedPass.length() < 1)) {
//	      return false;
//	    }
//	    return encryptedPass.equalsIgnoreCase(CryptoUtils.getClientPassword(session, originalPass));
//	  }
	  
	  public User login(ChannelHandlerContext sender, String name, String pass, String zoneName, Message paramsOut)
	  {
	    return login(sender, name, pass, zoneName, paramsOut, false);
	  }
	  
	  public User login(final ChannelHandlerContext sender, final String name, final String pass, final String zoneName, final Message paramsOut, boolean forceLogout)
	  {
	    if (!this.sfs.getSessionManager().containsSession(sender))
	    {
	      this.log.warn("Login failed: " + name + " , session is already expired!");
	      return null;
	    }
//	    
//	    Message resObj = SFSObject.newInstance();
//	    User user = null;
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.Login.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(sender);
//	    
//	    Zone zone = this.sfs.getZoneManager().getZoneByName(zoneName);
//	    if (zone == null)
//	    {
//	      resObj.putShort("ec", SFSErrorCode.LOGIN_BAD_ZONENAME.getId());
//	      resObj.putUtfStringArray("ep", Arrays.asList(new String[] { zoneName }));
//	      
//
//	      response.write();
//	      
//
//	      this.log.info("Bad login request, Zone: " + zoneName + " does not exist. Requested by: " + sender);
//	      
//
//	      return null;
//	    }
//	    try
//	    {
//	      user = zone.login(sender, name, pass, forceLogout);
//	      user.setConnected(true);
//	      
//
//	      sender.setLoggedIn(true);
//	      
//
//	      IPermissionProfile profile = (IPermissionProfile)sender.getProperty("$permission");
//	      if (profile != null) {
//	        user.setPrivilegeId(profile.getId());
//	      }
//	      this.log.info(
//	      
//	        String.format(
//	        
//	        "User login: %s, %s, Type: %s", new Object[] {
//	        zone.toString(), 
//	        user.toString(), 
//	        user.getSession().getSystemProperty("ClientType") }));
//	      if (user.isNpc()) {
//	        return user;
//	      }
//	      user.updateLastRequestTime();
//	      
//
//	      resObj.putInt("id", user.getId());
//	      
//
//	      resObj.putUtfString("zn", zone.getName());
//	      
//
//	      resObj.putUtfString("un", user.getName());
//	      
//
//	      resObj.putShort("rs", (short)zone.getUserReconnectionSeconds());
//	      
//
//	      resObj.putShort("pi", user.getPrivilegeId());
//	      
//
//	      resObj.putSFSArray("rl", zone.getRoomListData());
//	      if ((paramsOut != null) && (paramsOut.size() > 0)) {
//	        resObj.putSFSObject("p", paramsOut);
//	      }
//	      response.write();
//	      
//
//
//
//
//
//
//
//	      Map<ISFSEventParam, Object> evtParams = new HashMap();
//	      evtParams.put(SFSEventParam.ZONE, zone);
//	      evtParams.put(SFSEventParam.USER, user);
//	      
//	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_JOIN_ZONE, evtParams));
//	    }
//	    catch (SFSLoginInterruptedException e)
//	    {
//	      this.sfs.getTaskScheduler().schedule(
//	      
//	        new Runnable()
//	        {
//	          public void run()
//	          {
//	            SFSApi.this.login(
//	            
//	              sender, name, pass, zoneName, paramsOut);
//	          }
//	        }, 2000, 
//	        TimeUnit.MILLISECONDS);
//	    }
//	    catch (SFSLoginException err)
//	    {
//	      this.log.info("Login error: " + err.getMessage() + ". Requested by: " + sender);
//	      this.loginErrorHandler.execute(sender, err);
//	    }  
//	    return user;
	    
	    return null;
	  }
	  
	  public void logout(User user)
	  {
	    if (user == null) {
	      throw new OSException("Cannot logout null user.");
	    }
	    Zone zone = user.getZone();
	    

	    List<Room> joinedRooms = user.getJoinedRooms();
	    

	    Map<Room, Integer> playerIds = user.getPlayerIds();
	    

	    user.setConnected(false);
	    

	    zone.removeUser(user);
	    

	    this.globalUserManager.removeUser(user);
	    

	    user.setLoggedIn(false);//user.getSession().setLoggedIn(false);


	    this.responseAPI.notifyUserLost(user, joinedRooms);
	    for (Room r : joinedRooms)
	    {
	      boolean goodToGo = (r != null) && (r.isActive());
	      if (goodToGo)
	      {
	        this.responseAPI.notifyUserCountChange(zone, r);
	        

	        //this.responseAPI.notifyRoomVariablesUpdate(r, r.removeVariablesCreatedByUser(user));
	      }
	    }
	    for (Room r : user.getCreatedRooms()) {
	      if ((r != null) && (!joinedRooms.contains(r))) {
	        zone.checkAndRemove(r);
	      }
	    }
	    this.responseAPI.notifyLogout(user.getSession(), zone.getName());
	    

	    Map<ISFSEventParam, Object> evtParams = new HashMap();
	    evtParams.put(SFSEventParam.ZONE, zone);
	    evtParams.put(SFSEventParam.USER, user);
	    evtParams.put(SFSEventParam.JOINED_ROOMS, joinedRooms);
	    evtParams.put(SFSEventParam.PLAYER_IDS_BY_ROOM, playerIds);
	    
	    this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_LOGOUT, evtParams));
	    

	    this.log.info(
	    
	      String.format(
	      
	      "User logout: %s, %s, SessionLen: %s, Type: %s", new Object[] {
	      user.getZone().toString(), 
	      user.toString(), 
	      Long.valueOf(System.currentTimeMillis() - user.getLoginTime())
	      //, user.getSession().getSystemProperty("ClientType")
	      }));
	  }
	  
//	  public User createNPC(String userName, Zone zone, boolean forceLogin)
//	    throws OSException
//	  {
//		  ChannelHandlerContext socketLessSession = this.sfs.getSessionManager().createConnectionlessSession();
//	    
//
//	    User npcUser = zone.login(socketLessSession, userName, null);
//	    
//	    npcUser.setConnected(true);
//	    socketLessSession.setLoggedIn(true);
//	    
//	    return npcUser;
//	  }
	  
	  public Room createRoom(Zone zone, CreateRoomSettings params, User owner)
	    throws OSException
	  {
	    return createRoom(zone, params, owner, false, null, true, true);
	  }
	  
	  public Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave)
	    throws OSException
	  {
	    return createRoom(zone, params, owner, joinIt, roomToLeave, true, true);
	  }
	  
	  public Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave, boolean fireClientEvent, boolean fireServerEvent)
	    throws OSException
	  {
	    Room theRoom = null;
	    try
	    {
	      String groupId = params.getGroupId();
	      if ((groupId == null) || (groupId.length() == 0)) {
	        params.setGroupId("default");
	      }
	      theRoom = zone.createRoom(params, owner);
	      if (owner != null)
	      {
	        owner.addCreatedRoom(theRoom);
	        

	        owner.updateLastRequestTime();
	      }
//	      if ((theRoom instanceof MMORoom)) {
//	        configureMMORoom((MMORoom)theRoom, (CreateMMORoomSettings)params);
//	      }
	      if (fireClientEvent) {
	        this.responseAPI.notifyRoomAdded(theRoom);
	      }
	      if (fireServerEvent)
	      {
	        Map<ISFSEventParam, Object> eventParams = new HashMap();
	        eventParams.put(SFSEventParam.ZONE, zone);
	        eventParams.put(SFSEventParam.ROOM, theRoom);
	        
	        this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_ADDED, eventParams));
	      }
	    }
	    catch (OSException err)
	    {
//	      if (fireClientEvent) {
//	        this.responseAPI.notifyRequestError(err, owner, SystemRequest.CreateRoom);
//	      }
//	      String message = String.format("Room creation error. %s, %s, %s", new Object[] { err.getMessage(), zone, owner });
	      throw new OSException(err.toString());
	    }
	    if ((theRoom != null) && (owner != null) && (joinIt)) {
	      try
	      {
	        joinRoom(owner, theRoom, theRoom.getPassword(), false, roomToLeave, true, true);
	      }
	      catch (OSException e)
	      {
	        this.log.warn("Unable to join the just created Room: " + theRoom + ", reason: " + e.getMessage());
	      }
	    }
	    return theRoom;
	  }
	  
	  public void joinRoom(User user, Room room)
	    throws OSException
	  {
	    joinRoom(user, room, "", false, user.getLastJoinedRoom());
	  }
	  
	  public void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave)
	    throws OSException
	  {
	    joinRoom(user, roomToJoin, password, asSpectator, roomToLeave, true, true);
	  }
	  
	  public void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave, boolean fireClientEvent, boolean fireServerEvent)
	    throws OSException
	  {
	    try
	    {
	      if (user.isJoining()) {
	        throw new OSException("Join request discarded. User is already in a join transaction: " + user);
	      }
	      user.setJoining(true);
//	      if (roomToJoin == null) {
//	        throw new SFSJoinRoomException("Requested room doesn't exist", new SFSErrorData(SFSErrorCode.JOIN_BAD_ROOM));
//	      }
//	      if (!roomToJoin.isActive())
//	      {
//	        String message = String.format("Room is currently locked, %s", new Object[] { roomToJoin });
//	        SFSErrorData errData = new SFSErrorData(SFSErrorCode.JOIN_ROOM_LOCKED);
//	        errData.addParameter(roomToJoin.getName());
//	        
//	        throw new SFSJoinRoomException(message, errData);
//	      }
//	      boolean isSFSGame = roomToJoin instanceof SFSGame;
//	      boolean isMMO = roomToJoin instanceof MMORoom;
//	      if (isMMO) {
//	        user.setProperty("_uJoinTime", Long.valueOf(System.currentTimeMillis()));
//	      }
//	      if (isSFSGame) {
//	        try
//	        {
//	          checkSFSGameAccess((SFSGame)roomToJoin, user, asSpectator);
//	        }
//	        catch (OSException e)
//	        {
//	          throw new SFSJoinRoomException(e.getMessage(), e.getErrorData());
//	        }
//	      }
//	      boolean doorIsOpen = true;
//	      if (roomToJoin.isPasswordProtected()) {
//	        doorIsOpen = roomToJoin.getPassword().equals(password);
//	      }
//	      if (!doorIsOpen)
//	      {
//	        String message = String.format("Room password is wrong, %s", new Object[] { roomToJoin });
//	        SFSErrorData data = new SFSErrorData(SFSErrorCode.JOIN_BAD_PASSWORD);
//	        data.addParameter(roomToJoin.getName());
//	        
//	        throw new SFSJoinRoomException(message, data);
//	      }
	      roomToJoin.addUser(user, asSpectator);
	      if (this.sfs.getConfigurator().getServerSettings().statsExtraLoggingEnabled) {
	        this.log.info(
	        
	          String.format(
	          
	          "Room joined: %s, %s, %s, asSpect: %s", new Object[] {
	          roomToJoin, 
	          roomToJoin.getZone(), 
	          user, 
	          Boolean.valueOf(asSpectator) }));
	      }
	      user.updateLastRequestTime();
//	      if (fireClientEvent)
//	      {
//	        this.responseAPI.notifyJoinRoomSuccess(user, roomToJoin);
//	        if (!isMMO) {
//	          this.responseAPI.notifyUserEnterRoom(user, roomToJoin);
//	        }
//	        this.responseAPI.notifyUserCountChange(user.getZone(), roomToJoin);
//	      }
	      if (fireServerEvent)
	      {
	        Map<ISFSEventParam, Object> evtParams = new HashMap();
	        evtParams.put(SFSEventParam.ZONE, user.getZone());
	        evtParams.put(SFSEventParam.ROOM, roomToJoin);
	        evtParams.put(SFSEventParam.USER, user);
	        
	        this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_JOIN_ROOM, evtParams));
	      }
	      if (roomToLeave != null) {
	        leaveRoom(user, roomToLeave);
	      }
	    }
	    catch (OSException err)
	    {
	    	System.out.println(err.toString());
//	      if (fireClientEvent) {
//	        this.responseAPI.notifyRequestError(err, user, SystemRequest.JoinRoom);
//	      }
//	      String message = String.format("Join Error - %s", new Object[] { err.getMessage() });
//	      throw new SFSJoinRoomException(message);
	    }
	    finally
	    {
	      user.setJoining(false);
	    }
	  }
	  
	  public void leaveRoom(User user, Room room)
	  {
	    leaveRoom(user, room, true, true);
	  }
	  
	  public void leaveRoom(User user, Room room, boolean fireClientEvent, boolean fireServerEvent)
	  {
	    if (room == null)
	    {
	      room = user.getLastJoinedRoom();
	      if (room == null) {
	        throw new OSException("LeaveRoom failed: user is not joined in any room. " + user);
	      }
	    }
	    if (!room.containsUser(user)) {
	      return;
	    }
	    Zone zone = user.getZone();
	    

	    int playerId = user.getPlayerId(room);
	    

	    user.updateLastRequestTime();
	    if (fireClientEvent) {
	      this.responseAPI.notifyUserExitRoom(user, room, room.isFlagSet(SFSRoomSettings.USER_EXIT_EVENT));
	    }
	    zone.removeUserFromRoom(user, room);
	    






	    boolean roomWasRemoved = zone.getRoomById(room.getId()) == null;
	    if ((!roomWasRemoved) && (room.isActive()))
	    {
	      this.responseAPI.notifyUserCountChange(user.getZone(), room);
	      

	      //this.responseAPI.notifyRoomVariablesUpdate(room, room.removeVariablesCreatedByUser(user));
	    }
	    if (fireServerEvent)
	    {
	      Map<ISFSEventParam, Object> evtParams = new HashMap();
	      evtParams.put(SFSEventParam.ZONE, user.getZone());
	      evtParams.put(SFSEventParam.ROOM, room);
	      evtParams.put(SFSEventParam.USER, user);
	      evtParams.put(SFSEventParam.PLAYER_ID, Integer.valueOf(playerId));
	      
	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_LEAVE_ROOM, evtParams));
	    }
	  }
	  
//	  void sendPublicMessage(Room targetRoom, User sender, String message, Message params, Vec3D aoi)
//	  {
//	    if (targetRoom == null) {
//	      throw new IllegalArgumentException("The target Room is null");
//	    }
//	    if (!sender.isJoinedInRoom(targetRoom)) {
//	      throw new IllegalStateException("Sender " + sender + " is not joined the target room " + targetRoom);
//	    }
//	    if (!targetRoom.isFlagSet(SFSRoomSettings.PUBLIC_MESSAGES)) {
//	      throw new IllegalArgumentException("Room does not support public messages: " + targetRoom + ", Requested by: " + sender);
//	    }
//	    if (message.length() == 0)
//	    {
//	      this.log.warn("Empty public message request (len == 0) discarded, sender: " + sender);
//	      return;
//	    }
//	    Map<ISFSEventParam, Object> evtParams = new HashMap();
//	    evtParams.put(SFSEventParam.ZONE, sender.getZone());
//	    evtParams.put(SFSEventParam.ROOM, targetRoom);
//	    evtParams.put(SFSEventParam.USER, sender);
//	    evtParams.put(SFSEventParam.MESSAGE, message);
//	    evtParams.put(SFSEventParam.OBJECT, params);
//	    
//	    this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.PUBLIC_MESSAGE, evtParams));
//	    
//	    Zone zone = sender.getZone();
//	    if ((zone.getWordFilter().isActive()) && (targetRoom.isUseWordsFilter()))
//	    {
//	      FilteredMessage filtered = targetRoom.getZone().getWordFilter().apply(message, sender);
//	      if (filtered == null) {
//	        message = "";
//	      } else {
//	        message = filtered.getMessage();
//	      }
//	    }
//	    List<ChannelHandlerContext> recipients = getPublicMessageRecipientList(sender, targetRoom, aoi);
//	    if (recipients != null) {
//	      sendGenericMessage(GenericMessageType.PUBLIC_MSG, sender, targetRoom.getId(), message, params, recipients);
//	    }
//	  }
//	  
//	  public void sendPublicMessage(Room targetRoom, User sender, String message, Message params)
//	  {
//	    sendPublicMessage(targetRoom, sender, message, params, null);
//	  }
//	  
//	  public void sendPrivateMessage(User sender, User recipient, String message, Message params)
//	  {
//	    if (sender == null) {
//	      throw new IllegalArgumentException("PM sender is null.");
//	    }
//	    if (recipient == null) {
//	      throw new IllegalArgumentException("PM recipient is null");
//	    }
//	    if (sender == recipient) {
//	      throw new IllegalStateException("PM sender and receiver are the same. Why?");
//	    }
//	    if (message.length() == 0)
//	    {
//	      this.log.info("Empty private message request (len == 0) discarded");
//	      return;
//	    }
//	    Zone zone = sender.getZone();
//	    
//
//
//
//
//	    Map<ISFSEventParam, Object> evtParams = new HashMap();
//	    evtParams.put(SFSEventParam.ZONE, zone);
//	    evtParams.put(SFSEventParam.USER, sender);
//	    evtParams.put(SFSEventParam.RECIPIENT, recipient);
//	    evtParams.put(SFSEventParam.MESSAGE, message);
//	    evtParams.put(SFSEventParam.OBJECT, params);
//	    
//	    this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.PRIVATE_MESSAGE, evtParams));
//	    if ((zone.getWordFilter().isActive()) && (zone.isFilterPrivateMessages()))
//	    {
//	      FilteredMessage filtered = zone.getWordFilter().apply(message, sender);
//	      if (filtered == null) {
//	        message = "";
//	      } else {
//	        message = filtered.getMessage();
//	      }
//	    }
//	    ISFSArray senderData = null;
//	    if (!UsersUtil.usersSeeEachOthers(sender, recipient)) {
//	      senderData = sender.toSFSArray(sender.getLastJoinedRoom());
//	    }
//	    List<ChannelHandlerContext> messageRecipients = new ArrayList();
//	    messageRecipients.add(recipient.getSession());
//	    messageRecipients.add(sender.getSession());
//	    
//
//	    sendGenericMessage(
//	    
//	      GenericMessageType.PRIVATE_MSG, 
//	      sender, 
//	      -1, 
//	      message, 
//	      params, 
//	      messageRecipients, 
//	      senderData);
//	  }
//	  
//	  public void sendBuddyMessage(User sender, User recipient, String message, Message params)
//	    throws SFSBuddyListException
//	  {
//	    if (sender == null) {
//	      throw new IllegalArgumentException("BuddyMessage sender is null.");
//	    }
//	    if (recipient == null) {
//	      throw new IllegalArgumentException("BuddyMessage recipient is null");
//	    }
//	    if (sender == recipient) {
//	      throw new IllegalStateException("BuddyMessage sender and receiver are the same. Why?");
//	    }
//	    String senderName = sender.getName();
//	    String recipientName = recipient.getName();
//	    
//	    BuddyListManager manager = sender.getZone().getBuddyListManager();
//	    
//	    BuddyList senderBuddyList = manager.getBuddyList(senderName);
//	    Buddy recipientBuddy = senderBuddyList.getBuddy(recipientName);
//	    BuddyList recipientBuddyList = manager.getBuddyList(recipientName);
//	    Buddy senderBuddy = recipientBuddyList != null ? recipientBuddyList.getBuddy(senderName) : null;
//	    
//
//	    boolean recipientIsInSenderList = recipientBuddy != null;
//	    
//
//	    boolean recipientIsNotBlockedInSenderList = !recipientBuddy.isBlocked();
//	    
//
//	    boolean senderIsNotBlockedInRecipientList = !senderBuddy.isBlocked();
//	    
//
//	    boolean recipientIsOnline = recipient.getBuddyProperties().isOnline();
//	    
//	    Zone zone = sender.getZone();
//	    
//
//
//
//
//	    Map<ISFSEventParam, Object> evtParams = new HashMap();
//	    evtParams.put(SFSEventParam.ZONE, zone);
//	    evtParams.put(SFSEventParam.USER, sender);
//	    evtParams.put(SFSEventParam.RECIPIENT, recipient);
//	    evtParams.put(SFSEventParam.MESSAGE, message);
//	    evtParams.put(SFSEventParam.OBJECT, params);
//	    
//	    this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.BUDDY_MESSAGE, evtParams));
//	    if ((zone.getWordFilter().isActive()) && (zone.isFilterBuddyMessages()))
//	    {
//	      FilteredMessage filtered = zone.getWordFilter().apply(message, sender);
//	      if (filtered == null) {
//	        message = "";
//	      } else {
//	        message = filtered.getMessage();
//	      }
//	    }
//	    boolean goodToGo = (recipientIsInSenderList) && 
//	      (recipientIsNotBlockedInSenderList) && 
//	      (senderIsNotBlockedInRecipientList) && 
//	      (recipientIsOnline);
//	    if (goodToGo)
//	    {
//	      if (manager.getUseTempBuddies()) {
//	        if (senderBuddy == null) {
//	          if (recipientBuddyList != null) {
//	            this.sfs.getAPIManager().getBuddyApi().addBuddy(recipient, senderName, true, true, false);
//	          }
//	        }
//	      }
//	      List<ChannelHandlerContext> msgRecipients = new ArrayList();
//	      msgRecipients.add(recipient.getSession());
//	      msgRecipients.add(sender.getSession());
//	      
//
//	      sendGenericMessage(
//	      
//	        GenericMessageType.BUDDY_MSG, 
//	        sender, 
//	        -1, 
//	        message, 
//	        params, 
//	        msgRecipients);
//	    }
//	    else
//	    {
//	      String errorMessage = null;
//	      if (!recipientIsInSenderList) {
//	        errorMessage = String.format("Recipient %s is not found in sender's list: %s", new Object[] { recipientName, senderName });
//	      } else if (!recipientIsNotBlockedInSenderList) {
//	        errorMessage = String.format("Recipient %s is blocked in sender's list: %s", new Object[] { recipientName, senderName });
//	      } else if (!senderIsNotBlockedInRecipientList) {
//	        errorMessage = String.format("Sender %s is blocked in recipient's list: %s", new Object[] { senderName, recipientName });
//	      } else if (!recipientIsOnline) {
//	        errorMessage = String.format("Recipient %s is not online", new Object[] { recipientName });
//	      }
//	      if (errorMessage == null) {
//	        errorMessage = "Unexpected error";
//	      }
//	      throw new SFSBuddyListException(errorMessage);
//	    }
//	  }
//	  
//	  public void sendModeratorMessage(User sender, String message, Message params, Collection<ChannelHandlerContext> recipients)
//	  {
//	    sendSuperUserMessage(GenericMessageType.MODERATOR_MSG, sender, message, params, recipients);
//	  }
//	  
//	  public void sendAdminMessage(User sender, String message, Message params, Collection<ChannelHandlerContext> recipients)
//	  {
//	    sendSuperUserMessage(GenericMessageType.ADMING_MSG, sender, message, params, recipients);
//	  }
//	  
//	  private void sendSuperUserMessage(GenericMessageType type, User sender, String message, Message params, Collection<ChannelHandlerContext> recipients)
//	  {
//	    if (recipients.size() == 0) {
//	      throw new IllegalStateException("Mod Message discarded. No recipients");
//	    }
//	    if (message.length() == 0) {
//	      throw new IllegalStateException("Mod Message discarded. Empty message");
//	    }
//	    if (sender == null) {
//	      switch (type)
//	      {
//	      case OBJECT_MSG: 
//	        sender = UsersUtil.getServerAdmin();
//	        break;
//	      default: 
//	        sender = UsersUtil.getServerModerator();
//	      }
//	    }
//	    sendGenericMessage(
//	    
//	      type, 
//	      sender, 
//	      -1, 
//	      message, 
//	      params, 
//	      recipients, 
//	      sender.toSFSArray(null));
//	  }
//	  
//	  void sendObjectMessage(Room targetRoom, User sender, Message message, Vec3D aoi)
//	  {
//	    if (targetRoom == null) {
//	      throw new IllegalArgumentException("The target Room is null");
//	    }
//	    if (!sender.isJoinedInRoom(targetRoom)) {
//	      throw new IllegalStateException("Sender " + sender + " is not joined the target room " + targetRoom);
//	    }
//	    if (!(targetRoom instanceof MMORoom)) {
//	      throw new IllegalArgumentException("The target Room is not an MMORoom");
//	    }
//	    List<ChannelHandlerContext> recipientList = null;
//	    if (aoi != null) {
//	      recipientList = MMOHelper.getProximitySessionList((MMORoom)targetRoom, sender, aoi);
//	    } else {
//	      recipientList = MMOHelper.getProximitySessionList(sender);
//	    }
//	    sendGenericMessage(GenericMessageType.OBJECT_MSG, sender, targetRoom.getId(), null, message, recipientList);
//	  }
//	  
//	  public void sendObjectMessage(Room targetRoom, User sender, Message message, Collection<User> recipients)
//	  {
//	    if (targetRoom == null) {
//	      throw new IllegalArgumentException("The target Room is null");
//	    }
//	    if (!sender.isJoinedInRoom(targetRoom)) {
//	      throw new IllegalStateException("Sender " + sender + " is not joined the target room " + targetRoom);
//	    }
//	    List<ChannelHandlerContext> recipientList = null;
//	    if (recipients == null)
//	    {
//	      recipientList = targetRoom.getSessionList();
//	      recipientList.remove(sender.getSession());
//	    }
//	    else
//	    {
//	      recipientList = new LinkedList();
//	      for (User user : recipients) {
//	        if (targetRoom.containsUser(user)) {
//	          recipientList.add(user.getSession());
//	        }
//	      }
//	    }
//	    sendGenericMessage(GenericMessageType.OBJECT_MSG, sender, targetRoom.getId(), null, message, recipientList);
//	  }
	  
	  public void sendExtensionResponse(String cmdName, Message params, List<User> recipients, Room room, boolean useUDP)
	  {
	    List<ChannelHandlerContext> sessions = new LinkedList();
	    for (User user : recipients) {
	      sessions.add(user.getSession());
	    }
	    this.responseAPI.sendExtResponse(cmdName, params, sessions, room, useUDP);
	  }
	  
	  public void sendExtensionResponse(String cmdName, Message params, User recipient, Room room, boolean useUDP)
	  {
	    List<ChannelHandlerContext> msgRecipients = new LinkedList();
	    msgRecipients.add(recipient.getSession());
	    
	    this.responseAPI.sendExtResponse(cmdName, params, msgRecipients, room, useUDP);
	  }
//	  
//	  public void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables)
//	  {
//	    setRoomVariables(user, targetRoom, variables, true, true, false);
//	  }
//	  
//	  public void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables, boolean fireClientEvent, boolean fireServerEvent, boolean overrideOwnership)
//	  {
//	    if (targetRoom == null) {
//	      throw new OSException("The target Room is null!");
//	    }
//	    if (variables == null) {
//	      throw new OSException("Missing variables list!");
//	    }
//	    List<RoomVariable> listOfChanges = new ArrayList();
//	    for (RoomVariable var : variables) {
//	      try
//	      {
//	        targetRoom.setVariable(var, overrideOwnership);
//	        listOfChanges.add(var);
//	      }
//	      catch (SFSVariableException e)
//	      {
//	        this.log.warn(e.getMessage());
//	      }
//	    }
//	    if (user != null) {
//	      user.updateLastRequestTime();
//	    }
//	    if ((listOfChanges.size() > 0) && (fireClientEvent)) {
//	      this.responseAPI.notifyRoomVariablesUpdate(targetRoom, listOfChanges);
//	    }
//	    if ((listOfChanges.size() > 0) && (fireServerEvent))
//	    {
//	      Map<ISFSEventParam, Object> evtParams = new HashMap();
//	      evtParams.put(SFSEventParam.ZONE, targetRoom.getZone());
//	      evtParams.put(SFSEventParam.ROOM, targetRoom);
//	      evtParams.put(SFSEventParam.USER, user);
//	      evtParams.put(SFSEventParam.VARIABLES, listOfChanges);
//	      
//	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_VARIABLES_UPDATE, evtParams));
//	    }
//	  }
//	  
//	  public void setUserVariables(User owner, List<UserVariable> variables)
//	  {
//	    setUserVariables(owner, variables, true, true);
//	  }
//	  
//	  public void setUserVariables(User owner, List<UserVariable> variables, boolean fireClientEvent, boolean fireServerEvent)
//	  {
//	    List<UserVariable> listOfChanges = executeSetUserVariables(owner, variables);
//	    
//
//	    fireUserVariablesEvent(owner, listOfChanges, null, fireClientEvent, fireServerEvent);
//	  }
//	  
//	  void setUserVariables(User owner, List<UserVariable> variables, Vec3D aoi, boolean fireClientEvent, boolean fireServerEvent)
//	  {
//	    List<UserVariable> listOfChanges = executeSetUserVariables(owner, variables);
//	    
//
//	    fireUserVariablesEvent(owner, listOfChanges, aoi, fireClientEvent, fireServerEvent);
//	  }
//	  
//	  private List<UserVariable> executeSetUserVariables(User owner, List<UserVariable> variables)
//	  {
//	    if (owner == null) {
//	      throw new OSException("The User is null!");
//	    }
//	    if (variables == null) {
//	      throw new OSException("Missing variables list!");
//	    }
//	    List<UserVariable> listOfChanges = new ArrayList();
//	    for (UserVariable var : variables) {
//	      try
//	      {
//	        owner.setVariable(var);
//	        if (!var.isHidden()) {
//	          listOfChanges.add(var);
//	        }
//	      }
//	      catch (SFSVariableException e)
//	      {
//	        this.log.warn(e.getMessage());
//	      }
//	    }
//	    owner.updateLastRequestTime();
//	    
//	    return listOfChanges;
//	  }
//	  
//	  private void fireUserVariablesEvent(User owner, List<UserVariable> listOfChanges, Vec3D aoi, boolean fireClientEvent, boolean fireServerEvent)
//	  {
//	    if ((listOfChanges.size() > 0) && (fireClientEvent)) {
//	      this.responseAPI.notifyUserVariablesUpdate(owner, listOfChanges, aoi);
//	    }
//	    if ((listOfChanges.size() > 0) && (fireServerEvent))
//	    {
//	      Map<ISFSEventParam, Object> evtParams = new HashMap();
//	      evtParams.put(SFSEventParam.ZONE, owner.getZone());
//	      evtParams.put(SFSEventParam.USER, owner);
//	      evtParams.put(SFSEventParam.VARIABLES, listOfChanges);
//	      
//	      this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_VARIABLES_UPDATE, evtParams));
//	    }
//	  }
//	  
//	  public void changeRoomName(User owner, Room targetRoom, String newName)
//	    throws OSException
//	  {
//	    if (targetRoom.isFlagSet(SFSRoomSettings.ROOM_NAME_CHANGE)) {
//	      try
//	      {
//	        if (canUserChangeAttributesInThisRoom(owner, targetRoom))
//	        {
//	          targetRoom.getZone().changeRoomName(targetRoom, newName);
//	          
//
//	          this.responseAPI.notifyRoomNameChange(targetRoom);
//	          if (owner == null) {
//	            return;
//	          }
//	          owner.updateLastRequestTime();
//	          
//
//	          return;
//	        }
//	        SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_NAME_CHANGE_PERMISSION_ERR);
//	        errorData.addParameter(targetRoom.getName());
//	        
//	        String message = String.format("Room name change not permitted. Room: %s, User: %s", new Object[] { targetRoom, owner });
//	        throw new OSException(message, errorData);
//	      }
//	      catch (OSException err)
//	      {
//	        if (owner != null) {
//	          this.responseAPI.notifyRequestError(err, owner, SystemRequest.ChangeRoomName);
//	        }
//	        throw err;
//	      }
//	    } else {
//	      throw new OSException(
//	      
//	        String.format(
//	        
//	        "Attempt to change name to a Room that doesn't support it. %s, %s", new Object[] {
//	        targetRoom, 
//	        owner }));
//	    }
//	  }
//	  
//	  public void changeRoomPassword(User owner, Room targetRoom, String newPassword)
//	    throws OSException
//	  {
//	    if (targetRoom.isFlagSet(SFSRoomSettings.PASSWORD_STATE_CHANGE)) {
//	      try
//	      {
//	        if (canUserChangeAttributesInThisRoom(owner, targetRoom))
//	        {
//	          boolean previousState = targetRoom.isPasswordProtected();
//	          
//
//	          targetRoom.getZone().changeRoomPasswordState(targetRoom, newPassword);
//	          
//	          boolean newState = targetRoom.isPasswordProtected();
//	          
//
//	          this.responseAPI.notifyRoomPasswordChange(targetRoom, owner, previousState ^ newState);
//	          if (owner == null) {
//	            return;
//	          }
//	          owner.updateLastRequestTime();
//	          
//	          return;
//	        }
//	        SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_PASS_CHANGE_PERMISSION_ERR);
//	        errorData.addParameter(targetRoom.getName());
//	        
//	        String message = String.format("Room password change not permitted. Room: %s, User: %s", new Object[] { targetRoom, owner });
//	        throw new OSException(message, errorData);
//	      }
//	      catch (OSException err)
//	      {
//	        if (owner != null) {
//	          this.responseAPI.notifyRequestError(err, owner, SystemRequest.ChangeRoomPassword);
//	        }
//	        throw err;
//	      }
//	    } else {
//	      throw new OSException(
//	      
//	        String.format(
//	        
//	        "Attempt to change password to a Room that doesn't support it. %s, %s", new Object[] {
//	        targetRoom, 
//	        owner }));
//	    }
//	  }
//	  
//	  public void changeRoomCapacity(User owner, Room targetRoom, int maxUsers, int maxSpectators)
//	    throws OSException
//	  {
//	    if (targetRoom.isFlagSet(SFSRoomSettings.CAPACITY_CHANGE)) {
//	      try
//	      {
//	        if (canUserChangeAttributesInThisRoom(owner, targetRoom))
//	        {
//	          Zone zone = targetRoom.getZone();
//	          if (maxUsers > 0) {
//	            zone.changeRoomCapacity(targetRoom, maxUsers, maxSpectators);
//	          }
//	          this.responseAPI.notifyRoomCapacityChange(targetRoom);
//	          if (owner == null) {
//	            return;
//	          }
//	          owner.updateLastRequestTime();
//	          
//	          return;
//	        }
//	        SFSErrorData errorData = new SFSErrorData(SFSErrorCode.ROOM_CAPACITY_CHANGE_PERMISSION_ERR);
//	        errorData.addParameter(targetRoom.getName());
//	        
//	        String message = String.format("Room capacity change not allowed. Room: %s, User: %s", new Object[] { targetRoom, owner });
//	        throw new OSException(message, errorData);
//	      }
//	      catch (OSException err)
//	      {
//	        if (owner != null) {
//	          this.responseAPI.notifyRequestError(err, owner, SystemRequest.ChangeRoomCapacity);
//	        }
//	        throw err;
//	      }
//	    } else {
//	      throw new OSException(
//	      
//	        String.format(
//	        
//	        "Attempt to change capacity in a Room that doesn't support it. %s, %s", new Object[] {
//	        targetRoom, 
//	        owner }));
//	    }
//	  }
//	  
//	  private boolean canUserChangeAttributesInThisRoom(User user, Room targetRoom)
//	  {
//	    if (user == null) {
//	      return true;
//	    }
//	    if (user == targetRoom.getOwner()) {
//	      return true;
//	    }
//	    if (user.isSuperUser()) {
//	      return true;
//	    }
//	    return false;
//	  }
//	  
//	  public void subscribeRoomGroup(User user, String groupId)
//	  {
//	    Zone zone = user.getZone();
//	    try
//	    {
//	      if ((zone.containsGroup(groupId)) || (zone.containsPublicGroup(groupId)))
//	      {
//	        if (!user.isSubscribedToGroup(groupId))
//	        {
//	          user.subscribeGroup(groupId);
//	          this.responseAPI.notifyGroupSubscribeSuccess(user, groupId);
//	        }
//	        else
//	        {
//	          SFSErrorData errData = new SFSErrorData(SFSErrorCode.SUBSCRIBE_GROUP_ALREADY_SUBSCRIBED);
//	          errData.addParameter(groupId);
//	          
//	          throw new SFSException(
//	          
//	            String.format("User: %s is already subscribed to group: %s", new Object[] { user.getName(), groupId }), 
//	            errData);
//	        }
//	      }
//	      else
//	      {
//	        SFSErrorData errData = new SFSErrorData(SFSErrorCode.SUBSCRIBE_GROUP_NOT_FOUND);
//	        errData.addParameter(groupId);
//	        
//	        throw new SFSException(
//	        
//	          String.format("User: %s is request subscription to non-existing group: %s", new Object[] { user.getName(), groupId }), 
//	          errData);
//	      }
//	    }
//	    catch (SFSException err)
//	    {
//	      this.responseAPI.notifyRequestError(err, user, SystemRequest.SubscribeRoomGroup);
//	    }
//	  }
//	  
//	  public void unsubscribeRoomGroup(User user, String groupId)
//	  {
//	    Zone zone = user.getZone();
//	    try
//	    {
//	      if ((zone.containsGroup(groupId)) || (zone.containsPublicGroup(groupId)))
//	      {
//	        if (user.isSubscribedToGroup(groupId))
//	        {
//	          user.unsubscribeGroup(groupId);
//	          this.responseAPI.notifyGroupUnsubscribeSuccess(user, groupId);
//	        }
//	        else
//	        {
//	          SFSErrorData errData = new SFSErrorData(SFSErrorCode.UNSUBSCRIBE_GROUP_NOT_SUBSCRIBED);
//	          errData.addParameter(groupId);
//	          
//	          throw new SFSException(
//	          
//	            String.format("Can't unsubscribe user: %s from group: %s. Group is not subscribed.", new Object[] { user.getName(), groupId }), 
//	            errData);
//	        }
//	      }
//	      else
//	      {
//	        SFSErrorData errData = new SFSErrorData(SFSErrorCode.SUBSCRIBE_GROUP_NOT_FOUND);
//	        errData.addParameter(groupId);
//	        
//	        throw new SFSException(
//	        
//	          String.format("Can't unsubscribe user: %s from group: %s. Group doesn't exist", new Object[] { user.getName(), groupId }), 
//	          errData);
//	      }
//	    }
//	    catch (SFSException err)
//	    {
//	      this.responseAPI.notifyRequestError(err, user, SystemRequest.UnsubscribeRoomGroup);
//	    }
//	  }
//	  
//	  private void sendGenericMessage(GenericMessageType type, User sender, int targetRoomId, String message, Message params, Collection<ChannelHandlerContext> recipients, ISFSArray senderData)
//	  {
//	    if (sender != null) {
//	      sender.updateLastRequestTime();
//	    }
//	    Message resObj = SFSObject.newInstance();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.GenericMessage.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(recipients);
//	    
//	    resObj.putByte("t", (byte)type.getId());
//	    resObj.putInt("r", targetRoomId);
//	    
//	    resObj.putInt("u", sender.getId());
//	    if (message != null) {
//	      resObj.putUtfString("m", message);
//	    }
//	    if (params != null) {
//	      resObj.putSFSObject("p", params);
//	    }
//	    if (senderData != null) {
//	      resObj.putSFSArray("sd", senderData);
//	    }
//	    response.write();
//	  }
//	  
//	  private void sendGenericMessage(GenericMessageType type, User sender, int targetRoomId, String message, Message params, Collection<ChannelHandlerContext> recipients)
//	  {
//	    sendGenericMessage(type, sender, targetRoomId, message, params, recipients, null);
//	  }
//	  
//	  public void spectatorToPlayer(User user, Room targetRoom, boolean fireClientEvent, boolean fireServerEvent)
//	    throws OSException
//	  {
//	    if (targetRoom == null) {
//	      throw new IllegalArgumentException("A target room was not specified (null)");
//	    }
//	    if (user == null) {
//	      throw new IllegalArgumentException("A user was not specified (null)");
//	    }
//	    user.updateLastRequestTime();
//	    try
//	    {
//	      if ((targetRoom instanceof SFSGame)) {
//	        checkSFSGameAccess((SFSGame)targetRoom, user, false);
//	      }
//	      targetRoom.switchSpectatorToPlayer(user);
//	      if (fireClientEvent)
//	      {
//	        this.responseAPI.notifySpectatorToPlayer(user.getSession(), targetRoom, user.getId(), user.getPlayerId(targetRoom));
//	        
//
//	        this.responseAPI.notifyUserCountChange(user.getZone(), targetRoom);
//	      }
//	      if (fireServerEvent)
//	      {
//	        Map<ISFSEventParam, Object> evtParams = new HashMap();
//	        evtParams.put(SFSEventParam.ZONE, user.getZone());
//	        evtParams.put(SFSEventParam.ROOM, targetRoom);
//	        evtParams.put(SFSEventParam.USER, user);
//	        evtParams.put(SFSEventParam.PLAYER_ID, Integer.valueOf(user.getPlayerId(targetRoom)));
//	        
//	        this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.SPECTATOR_TO_PLAYER, evtParams));
//	      }
//	    }
//	    catch (OSException err)
//	    {
//	      if (fireClientEvent) {
//	        this.responseAPI.notifyRequestError(err, user, SystemRequest.SpectatorToPlayer);
//	      }
//	      String message = String.format("SpectatorToPlayer Error - %s", new Object[] { err.getMessage() });
//	      throw new OSException(message, err.getErrorData());
//	    }
//	  }
//	  
//	  public void playerToSpectator(User user, Room targetRoom, boolean fireClientEvent, boolean fireServerEvent)
//	    throws OSException
//	  {
//	    if (targetRoom == null) {
//	      throw new IllegalArgumentException("A target room was not specified (null)");
//	    }
//	    if (user == null) {
//	      throw new IllegalArgumentException("A user was not specified (null)");
//	    }
//	    user.updateLastRequestTime();
//	    try
//	    {
//	      if ((targetRoom instanceof SFSGame)) {
//	        checkSFSGameAccess((SFSGame)targetRoom, user, true);
//	      }
//	      targetRoom.switchPlayerToSpectator(user);
//	      if (fireClientEvent)
//	      {
//	        this.responseAPI.notifyPlayerToSpectator(user.getSession(), targetRoom, user.getId());
//	        
//
//	        this.responseAPI.notifyUserCountChange(user.getZone(), targetRoom);
//	      }
//	      if (fireServerEvent)
//	      {
//	        Map<ISFSEventParam, Object> evtParams = new HashMap();
//	        evtParams.put(SFSEventParam.ZONE, user.getZone());
//	        evtParams.put(SFSEventParam.ROOM, targetRoom);
//	        evtParams.put(SFSEventParam.USER, user);
//	        
//	        this.sfs.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.PLAYER_TO_SPECTATOR, evtParams));
//	      }
//	    }
//	    catch (OSException err)
//	    {
//	      if (fireClientEvent) {
//	        this.responseAPI.notifyRequestError(err, user, SystemRequest.PlayerToSpectator);
//	      }
//	      String message = String.format("PlayerToSpectator Error - %s", new Object[] { err.getMessage() });
//	      throw new OSException(message, err.getErrorData());
//	    }
//	  }
//	  
//	  private void checkSFSGameAccess(SFSGame gameRoom, User user, boolean asSpectator)
//	    throws OSException
//	  {
//	    MatchExpression expression = null;
//	    if (asSpectator) {
//	      expression = gameRoom.getSpectatorMatchExpression();
//	    } else if (gameRoom.isPublic()) {
//	      expression = gameRoom.getPlayerMatchExpression();
//	    }
//	    if (expression == null) {
//	      return;
//	    }
//	    if (!this.matcher.matchUser(user, expression))
//	    {
//	      String message = String.format("User does not match the MatchExpression of the Game Room: %s", new Object[] { expression });
//	      SFSErrorData errData = new SFSErrorData(SFSErrorCode.JOIN_GAME_ACCESS_DENIED);
//	      errData.addParameter(gameRoom.getName());
//	      
//	      throw new OSException(message, errData);
//	    }
//	  }
//	  
//	  private void configureMMORoom(MMORoom room, CreateMMORoomSettings settings)
//	  {
//	    if (settings.getMapLimits() != null) {
//	      room.setMapLimits(settings.getMapLimits().getLowerLimit(), settings.getMapLimits().getHigherLimit());
//	    }
//	    room.setUserLimboMaxSeconds(settings.getUserMaxLimboSeconds());
//	    room.setSendAOIEntryPoint(settings.isSendAOIEntryPoint());
//	  }
//	  
//	  private List<ChannelHandlerContext> getPublicMessageRecipientList(User sender, Room targetRoom, Vec3D aoi)
//	  {
//	    List<ChannelHandlerContext> recipients = null;
//	    if ((targetRoom instanceof MMORoom))
//	    {
//	      if (aoi != null) {
//	        recipients = MMOHelper.getProximitySessionList((MMORoom)targetRoom, sender, aoi);
//	      } else {
//	        recipients = MMOHelper.getProximitySessionList(sender);
//	      }
//	      recipients.add(sender.getSession());
//	    }
//	    else
//	    {
//	      recipients = targetRoom.getSessionList();
//	    }
//	    return recipients;
//	  }
}
