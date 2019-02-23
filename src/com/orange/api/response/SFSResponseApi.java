package com.orange.api.response;

import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.orange.entities.Room;
import com.orange.entities.SFSRoomSettings;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.util.IDisconnectionReason;

public class SFSResponseApi {
	private final Logger log;
	  private final int SEARCH_RESULT_LIMIT = 50;
	  
	  public SFSResponseApi()
	  {
	    this.log = LoggerFactory.getLogger(getClass());
	  }
	  
	  public void sendExtResponse(String cmdName, Message params, List<ChannelHandlerContext> recipients, Room room, boolean sendUDP)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    resObj.putUtfString("c", cmdName);
//	    resObj.putSFSObject("p", params != null ? params : new SFSObject());
//	    if (room != null) {
//	      resObj.putInt("r", room.getId());
//	    }
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.CallExtension.getId());
//	    response.setTargetController(DefaultConstants.CORE_EXTENSIONS_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(recipients);
//	    if (sendUDP) {
//	      response.setTransportType(TransportType.UDP);
//	    }
//	    response.write();
	  }
	  
	  public void sendPingPongResponse(ChannelHandlerContext recipient)
	  {
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.PingPong.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(new SFSObject());
//	    response.setRecipients(recipient);
//	    
//	    response.write();
	  }
	  
	  public void notifyRoomAdded(Room newRoom)
	  {
	    Collection<ChannelHandlerContext> recipients = newRoom.getZone().getSessionsListeningToGroup(newRoom.getGroupId());
	    

//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.CreateRoom.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(recipients);
//	    
//	    resObj.putSFSArray("r", newRoom.toSFSArray(true));
//	    
//
//	    response.write();
	  }
	  
	  public void notifyRoomRemoved(Room room)
	  {
	    Collection<ChannelHandlerContext> recipients = room.getZone().getSessionsListeningToGroup(room.getGroupId());
	    if (recipients.size() > 0)
	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      IResponse response = new Response();
//	      response.setId(SystemRequest.OnRoomLost.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(resObj);
//	      response.setRecipients(recipients);
//	      
//	      resObj.putInt("r", room.getId());
//	      
//
//	      response.write();
	    }
	  }
	  
	  public void notifyJoinRoomSuccess(User recipient, Room joinedRoom)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.JoinRoom.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(recipient.getSession());
//	    
//
//	    resObj.putSFSArray("r", joinedRoom.toSFSArray(false));
//	    
//
//
//
//
//
//	    resObj.putSFSArray("ul", (joinedRoom instanceof MMORoom) ? new SFSArray() : joinedRoom.getUserListData());
//	    
//
//	    response.write();
	  }
	  
	  public void notifyUserExitRoom(User user, Room room, boolean sendToEveryOne)
	  {
//	    List<ISession> recipients = new ArrayList();
//	    if (sendToEveryOne) {
//	      recipients.addAll(room.getSessionList());
//	    }
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.OnUserExitRoom.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(recipients);
//	    
//
//	    resObj.putInt("u", user.getId());
//	    resObj.putInt("r", room.getId());
//	    
//
//	    response.write();
	  }
	  
	  public void notifyUserLost(User user, List<Room> joinedRooms)
	  {
	    Set<ChannelHandlerContext> recipients = new HashSet();
	    for (Room room : joinedRooms) {
	      if (room.isFlagSet(SFSRoomSettings.USER_EXIT_EVENT)) {
	        recipients.addAll(room.getSessionList());
	      }
	    }
	    if (recipients.size() > 0)
	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      Object response = new Response();
//	      ((IResponse)response).setId(SystemRequest.OnUserLost.getId());
//	      ((IResponse)response).setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      ((IResponse)response).setContent(resObj);
//	      ((IResponse)response).setRecipients(recipients);
//	      
//
//	      resObj.putInt("u", user.getId());
//	      
//
//	      ((IResponse)response).write();
	    }
	  }
	  
	  public void notifyUserCountChange(Zone zone, Room room)
	  {
	    if (!room.isFlagSet(SFSRoomSettings.USER_COUNT_CHANGE_EVENT)) {
	      return;
	    }
	    String groupId = room.getGroupId();
	    






	    Collection<ChannelHandlerContext> recipients = zone.getSessionsListeningToGroup(groupId);
	    if (recipients.size() > 0)
	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      RoomSize roomSize = room.getSize();
//	      resObj.putInt("r", room.getId());
//	      
//
//	      resObj.putShort("uc", (short)roomSize.getUserCount());
//	      if (room.isGame()) {
//	        resObj.putShort("sc", (short)roomSize.getSpectatorCount());
//	      }
//	      IResponse response = new Response();
//	      response.setId(SystemRequest.OnRoomCountChange.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(resObj);
//	      response.setRecipients(recipients);
//	      
//
//	      zone.getUCountThrottler().enqueueResponse(response);
	    }
	  }
	  
	  public void notifyUserEnterRoom(User user, Room room)
	  {
	    if (!room.isFlagSet(SFSRoomSettings.USER_ENTER_EVENT)) {
	      return;
	    }
	    List<ChannelHandlerContext> recipients = room.getSessionList();
	    recipients.remove(user.getSession());
	    if (recipients.size() > 0)
	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      resObj.putInt("r", room.getId());
//	      resObj.putSFSArray("u", user.toSFSArray(room));
//	      
//
//	      IResponse response = new Response();
//	      response.setId(SystemRequest.OnEnterRoom.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(resObj);
//	      response.setRecipients(recipients);
//	      
//	      response.write();
	    }
	  }
	  
//	  public void notifyRoomVariablesUpdate(Room targetRoom, List<RoomVariable> listOfChanges)
//	  {
//	    List<ISession> roomRecipients = targetRoom.getSessionList();
//	    IResponse response = null;
//	    
//
//	    ISFSObject roomResponseObj = SFSObject.newInstance();
//	    
//	    ISFSArray roomVariablesData = SFSArray.newInstance();
//	    ISFSArray zoneVariablesData = SFSArray.newInstance();
//	    for (RoomVariable var : listOfChanges) {
//	      if (!var.isHidden())
//	      {
//	        ISFSArray varData = var.toSFSArray();
//	        if (var.isGlobal()) {
//	          zoneVariablesData.addSFSArray(varData);
//	        }
//	        roomVariablesData.addSFSArray(varData);
//	      }
//	    }
//	    if (roomVariablesData.size() > 0)
//	    {
//	      roomResponseObj.putInt("r", targetRoom.getId());
//	      
//	      roomResponseObj.putSFSArray("vl", roomVariablesData);
//	      
//
//	      response = new Response();
//	      response.setId(SystemRequest.SetRoomVariables.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(roomResponseObj);
//	      response.setRecipients(roomRecipients);
//	      if (this.log.isDebugEnabled()) {
//	        this.log.debug("Room Recipients: " + roomRecipients);
//	      }
//	      response.write();
//	    }
//	    if (zoneVariablesData.size() > 0)
//	    {
//	      Zone zone = targetRoom.getZone();
//	      
//
//	      Object globalRecipients = zone.getSessionsListeningToGroup(targetRoom.getGroupId());
//	      
//
//	      ((Collection)globalRecipients).removeAll(roomRecipients);
//	      
//	      ISFSObject zoneResponseObj = SFSObject.newInstance();
//	      zoneResponseObj.putInt("r", targetRoom.getId());
//	      zoneResponseObj.putSFSArray("vl", zoneVariablesData);
//	      
//
//	      response = new Response();
//	      response.setId(SystemRequest.SetRoomVariables.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(zoneResponseObj);
//	      response.setRecipients((Collection)globalRecipients);
//	      if (this.log.isDebugEnabled()) {
//	        this.log.debug("RoomVars Global Recipients: " + globalRecipients);
//	      }
//	      response.write();
//	    }
//	  }
	  
//	  public void notifyUserVariablesUpdate(User user, List<UserVariable> varList)
//	  {
//	    notifyUserVariablesUpdate(user, varList, null);
//	  }
//	  
//	  public void notifyUserVariablesUpdate(User user, List<UserVariable> varList, Vec3D aoi)
//	  {
//	    List<Room> allRooms = user.getJoinedRooms();
//	    Collection<ISession> recipients = null;
//	    List<ISession> allOfThem;
//	    if (allRooms.size() == 0)
//	    {
//	      recipients = Arrays.asList(new ISession[] { user.getSession() });
//	    }
//	    else if (allRooms.size() == 1)
//	    {
//	      if (user.getLastJoinedRoom().isFlagSet(SFSRoomSettings.USER_VARIABLES_UPDATE_EVENT)) {
//	        recipients = getRoomRecipients(user.getLastJoinedRoom(), user, aoi);
//	      }
//	    }
//	    else
//	    {
//	      Set<ISession> sessionSet = new HashSet();
//	      for (Room room : allRooms) {
//	        if (room.isFlagSet(SFSRoomSettings.USER_VARIABLES_UPDATE_EVENT))
//	        {
//	          allOfThem = getRoomRecipients(room, user, aoi);
//	          if (allOfThem != null) {
//	            sessionSet.addAll(allOfThem);
//	          }
//	        }
//	      }
//	      recipients = sessionSet;
//	    }
//	    if ((recipients != null) && (recipients.size() > 0))
//	    {
//	      ISFSObject responseObj = SFSObject.newInstance();
//	      
//
//	      ISFSArray userVariablesData = SFSArray.newInstance();
//	      for (UserVariable var : varList) {
//	        userVariablesData.addSFSArray(var.toSFSArray());
//	      }
//	      responseObj.putSFSArray("vl", userVariablesData);
//	      responseObj.putInt("u", user.getId());
//	      
//
//	      Object response = new Response();
//	      ((IResponse)response).setId(SystemRequest.SetUserVariables.getId());
//	      ((IResponse)response).setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      ((IResponse)response).setContent(responseObj);
//	      ((IResponse)response).setRecipients(recipients);
//	      
//
//	      ((IResponse)response).write();
//	    }
//	  }
//	  
//	  public void notifyGroupSubscribeSuccess(User user, String groupId)
//	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    resObj.putUtfString("g", groupId);
//	    
//
//	    resObj.putSFSArray("rl", user.getZone().getRoomListData(Arrays.asList(new String[] { groupId })));
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.SubscribeRoomGroup.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(user.getSession());
//	    
//
//	    response.write();
//	  }
//	  
//	  public void notifyGroupUnsubscribeSuccess(User user, String groupId)
//	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    resObj.putUtfString("g", groupId);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.UnsubscribeRoomGroup.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(user.getSession());
//	    
//
//	    response.write();
//	  }
//	  
	  public void notifyClientSideDisconnection(User user, IDisconnectionReason reason)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    resObj.putByte("dr", reason.getByteValue());
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.OnClientDisconnection.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    response.setRecipients(user.getSession());
//	    
//
//	    response.write();
	  }
	  
	  public void notifyRoomNameChange(Room room)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    resObj.putInt("r", room.getId());
//	    resObj.putUtfString("n", room.getName());
//	    
//	    Zone zone = room.getZone();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.ChangeRoomName.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(zone.getSessionsListeningToGroup(room.getGroupId()));
//	    
//
//	    response.write();
	  }
	  
	  public void notifyRoomPasswordChange(Room room, User sender, boolean isStateChanged)
	  {
	    Zone zone = room.getZone();
	    

	    Collection<ChannelHandlerContext> recipients = null;
	    if (isStateChanged) {
	      recipients = zone.getSessionsListeningToGroup(room.getGroupId());
	    } else if (sender != null) {
	      recipients = Arrays.asList(new ChannelHandlerContext[] { sender.getSession() });
	    }
	    if (recipients != null)
	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      resObj.putInt("r", room.getId());
//	      resObj.putBool("p", room.isPasswordProtected());
//	      
//
//	      IResponse response = new Response();
//	      response.setId(SystemRequest.ChangeRoomPassword.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(resObj);
//	      
//
//	      response.setRecipients(recipients);
//	      
//
//	      response.write();
	    }
	  }
	  
	  public void notifyRoomCapacityChange(Room room)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//
//	    resObj.putInt("r", room.getId());
//	    resObj.putInt("u", room.getMaxUsers());
//	    resObj.putInt("s", room.getMaxSpectators());
//	    
//	    Zone zone = room.getZone();
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.ChangeRoomCapacity.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(zone.getSessionsListeningToGroup(room.getGroupId()));
//	    
//
//	    response.write();
	  }
	  
	  public void notifyLogout(ChannelHandlerContext recipient, String zoneName)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    resObj.putUtfString("zn", zoneName);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.Logout.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(recipient);
//	    
//
//	    response.write();
	  }
	  
	  public void notifySpectatorToPlayer(ChannelHandlerContext recipient, Room room, int userId, int playerId)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    resObj.putInt("r", room.getId());
//	    resObj.putInt("u", userId);
//	    resObj.putShort("p", (short)playerId);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.SpectatorToPlayer.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(room.getSessionList());
//	    
//
//	    response.write();
	  }
	  
	  public void notifyPlayerToSpectator(ChannelHandlerContext recipient, Room room, int userId)
	  {
//	    ISFSObject resObj = SFSObject.newInstance();
//	    resObj.putInt("r", room.getId());
//	    resObj.putInt("u", userId);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.PlayerToSpectator.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(room.getSessionList());
//	    
//
//	    response.write();
	  }
	  
	  public void notifyFilteredRoomList(ChannelHandlerContext recipient, Collection<Room> roomList)
	  {
//	    int itemCount = roomList.size();
//	    if (itemCount > 50)
//	    {
//	      itemCount = 50;
//	      this.log.info(String.format("FindRooms request returned a too large result set: %s, the limit for a client request is: %s", new Object[] { Integer.valueOf(roomList.size()), Integer.valueOf(50) }));
//	    }
//	    ISFSObject resObj = SFSObject.newInstance();
//	    
//	    ISFSArray roomListData = SFSArray.newInstance();
//	    
//	    int cnt = 0;
//	    for (Room room : roomList)
//	    {
//	      if (cnt >= itemCount) {
//	        break;
//	      }
//	      roomListData.addSFSArray(room.toSFSArray(true));
//	      cnt++;
//	    }
//	    resObj.putSFSArray("fr", roomListData);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.FindRooms.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(recipient);
//	    
//
//	    response.write();
	  }
	  
	  public void notifyFilteredUserList(ChannelHandlerContext recipient, Collection<User> userList)
	  {
//	    int itemCount = userList.size();
//	    if (itemCount > 50)
//	    {
//	      itemCount = 50;
//	      this.log.info(String.format("FindRooms request returned a too large result set: %s, the limit for a client request is: %s", new Object[] { Integer.valueOf(userList.size()), Integer.valueOf(50) }));
//	    }
//	    ISFSObject resObj = SFSObject.newInstance();
//	    ISFSArray userListData = SFSArray.newInstance();
//	    
//	    int cnt = 0;
//	    for (User user : userList)
//	    {
//	      if (cnt >= itemCount) {
//	        break;
//	      }
//	      userListData.addSFSArray(user.toSFSArray());
//	      cnt++;
//	    }
//	    resObj.putSFSArray("fu", userListData);
//	    
//
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.FindUsers.getId());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    response.setContent(resObj);
//	    
//
//	    response.setRecipients(recipient);
//	    
//
//	    response.write();
	  }
	  
	  public void notifyReconnectionFailure(ChannelHandlerContext recipient)
	  {
//	    IResponse response = new Response();
//	    response.setId(SystemRequest.OnReconnectionFailure.getId());
//	    response.setRecipients(recipient);
//	    response.setContent(new SFSObject());
//	    response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	    
//	    response.write();
//	    System.out.println("SENDING TO -------> " + recipient);
	  }
	  
//	  public void notifyRequestError(SFSException err, User recipient, SystemRequest requestType)
//	  {
//	    notifyRequestError(err.getErrorData(), recipient, requestType);
//	  }
//	  
//	  public void notifyRequestError(SFSErrorData errData, User recipient, SystemRequest requestType)
//	  {
//	    if (recipient != null)
//	    {
//	      ISFSObject resObj = SFSObject.newInstance();
//	      
//
//	      IResponse response = new Response();
//	      response.setId(requestType.getId());
//	      response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
//	      response.setContent(resObj);
//	      response.setRecipients(recipient.getSession());
//	      
//	      resObj.putShort("ec", errData.getCode().getId());
//	      resObj.putUtfStringArray("ep", errData.getParams());
//	      
//	      response.write();
//	    }
//	    else
//	    {
//	      ExceptionMessageComposer composer = new ExceptionMessageComposer(new NullPointerException("Can't send error notification to client."));
//	      composer.setDescription("Attempting to send: " + errData.getCode() + " in response to: " + requestType);
//	      composer.setPossibleCauses("Recipient is NULL!");
//	      this.log.warn(composer.toString());
//	    }
//	  }
//	  
//	  private List<ChannelHandlerContext> getRoomRecipients(Room targetRoom, User sender, Vec3D aoi)
//	  {
//	    List<ChannelHandlerContext> sessions = null;
//	    if ((targetRoom instanceof MMORoom))
//	    {
//	      if (aoi != null) {
//	        sessions = MMOHelper.getProximitySessionList((MMORoom)targetRoom, sender, aoi);
//	      } else {
//	        sessions = MMOHelper.getProximitySessionList(sender);
//	      }
//	      if (sessions == null) {
//	        sessions = new LinkedList();
//	      }
//	      sessions.add(sender.getSession());
//	    }
//	    else
//	    {
//	      sessions = targetRoom.getSessionList();
//	    }
//	    return sessions;
//	  }
}
