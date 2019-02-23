package com.orange.entities.managers;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.core.BaseCoreService;
import com.orange.core.ICoreService;
import com.orange.entities.Room;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.exceptions.OSRuntimeException;
import com.orange.server.OrangeServerEngine;
import com.orange.util.ClientDisconnectionReason;

public class UserManager extends BaseCoreService implements ICoreService{
	private final ConcurrentMap<String, User> usersByName;
	  private final ConcurrentMap<ChannelHandlerContext, User> usersBySession;
	  private final ConcurrentMap<Integer, User> usersById;
	  private Room ownerRoom;
	  private Zone ownerZone;
	  private Logger logger;
	  private int highestCCU = 0;
	  
	  public UserManager()
	  {
	    this.logger = LoggerFactory.getLogger(getClass());
	    
	    this.usersBySession = new ConcurrentHashMap();
	    this.usersByName = new ConcurrentHashMap();
	    this.usersById = new ConcurrentHashMap();
	    
	    this.name = "UserManagerService";
	    this.active = true;
	  }
	  
	  public void addUser(User user)
	  {
	    if (containsId(user.getId())) {
	      throw new OSRuntimeException("Can't add User: " + user.getName() + " - Already exists in Room: " + this.ownerRoom + ", Zone: " + this.ownerZone);
	    }
	    this.usersById.put(Integer.valueOf(user.getId()), user);
	    this.usersByName.put(user.getName(), user);
	    this.usersBySession.put(user.getSession(), user);
	    if (this.usersById.size() > this.highestCCU) {
	      this.highestCCU = this.usersById.size();
	    }
	  }
	  
	  public User getUserById(int id)
	  {
	    return (User)this.usersById.get(Integer.valueOf(id));
	  }
	  
	  public User getUserByName(String name)
	  {
	    return (User)this.usersByName.get(name);
	  }
	  
	  public User getUserBySession(ChannelHandlerContext session)
	  {
	    return (User)this.usersBySession.get(session);
	  }
	  
	  public void removeUser(int userId)
	  {
	    User user = (User)this.usersById.get(Integer.valueOf(userId));
	    if (user == null) {
	      this.logger.warn("Can't remove user with ID: " + userId + ". User was not found.");
	    } else {
	      removeUser(user);
	    }
	  }
	  
	  public void removeUser(String name)
	  {
	    User user = (User)this.usersByName.get(name);
	    if (user == null) {
	      this.logger.warn("Can't remove user with name: " + name + ". User was not found.");
	    } else {
	      removeUser(user);
	    }
	  }
	  
	  public void removeUser(ChannelHandlerContext session)
	  {
	    User user = (User)this.usersBySession.get(session);
	    if (user == null) {
	      throw new OSRuntimeException("Can't remove user with session: " + session + ". User was not found.");
	    }
	    removeUser(user);
	  }
	  
	  public void removeUser(User user)
	  {
	    this.usersById.remove(Integer.valueOf(user.getId()));
	    this.usersByName.remove(user.getName());
	    this.usersBySession.remove(user.getSession());
	  }
	  
	  public boolean containsId(int userId)
	  {
	    return this.usersById.containsKey(Integer.valueOf(userId));
	  }
	  
	  public boolean containsName(String name)
	  {
	    return this.usersByName.containsKey(name);
	  }
	  
	  public boolean containsSessions(ChannelHandlerContext session)
	  {
	    return this.usersBySession.containsKey(session);
	  }
	  
	  public boolean containsUser(User user)
	  {
	    return this.usersById.containsValue(user);
	  }
	  
	  public Room getOwnerRoom()
	  {
	    return this.ownerRoom;
	  }
	  
	  public void setOwnerRoom(Room ownerRoom)
	  {
	    this.ownerRoom = ownerRoom;
	  }
	  
	  public Zone getOwnerZone()
	  {
	    return this.ownerZone;
	  }
	  
	  public void setOwnerZone(Zone ownerZone)
	  {
	    this.ownerZone = ownerZone;
	  }
	  
	  public List<User> getAllUsers()
	  {
	    return new ArrayList(this.usersById.values());
	  }
	  
	  public List<ChannelHandlerContext> getAllSessions()
	  {
	    return new ArrayList(this.usersBySession.keySet());
	  }
	  
	  public Collection<User> getDirectUserList()
	  {
	    return Collections.unmodifiableCollection(this.usersById.values());
	  }
	  
	  public Collection<ChannelHandlerContext> getDirectSessionList()
	  {
	    return Collections.unmodifiableCollection(this.usersBySession.keySet());
	  }
	  
	  public int getUserCount()
	  {
	    return this.usersById.values().size();
	  }
	  
	  public int getNPCCount()
	  {
	    int npcCount = 0;
	    for (User user : this.usersById.values()) {
	      if (user.isNpc()) {
	        npcCount++;
	      }
	    }
	    return npcCount;
	  }
	  
	  public void disconnectUser(int userId)
	  {
	    User user = (User)this.usersById.get(Integer.valueOf(userId));
	    if (user == null) {
	      this.logger.warn("Can't disconnect user with id: " + userId + ". User was not found.");
	    } else {
	      disconnectUser(user);
	    }
	  }
	  
	  public void disconnectUser(ChannelHandlerContext session)
	  {
	    User user = (User)this.usersBySession.get(session);
	    if (user == null) {
	      this.logger.warn("Can't disconnect user with session: " + session + ". User was not found.");
	    } else {
	      disconnectUser(user);
	    }
	  }
	  
	  public void disconnectUser(String name)
	  {
	    User user = (User)this.usersByName.get(name);
	    if (user == null) {
	      this.logger.warn("Can't disconnect user with name: " + name + ". User was not found.");
	    } else {
	      disconnectUser(user);
	    }
	  }
	  
	  public void disconnectUser(User user)
	  {
	    removeUser(user);
	  }
	  
	  public int getHighestCCU()
	  {
	    return this.highestCCU;
	  }
	  
//	  public void purgeOrphanedUsers()
//	  {
//		SessionManager mgr = OrangeServerEngine.getInstance().getSessionManager();
//	    ISFSApi api = SmartFoxServer.getInstance().getAPIManager().getSFSApi();
//	    int tot = 0;
//	    for (ISession session : this.usersBySession.keySet()) {
//	      if (!mgr.containsSession(session))
//	      {
//	        User evictable = (User)this.usersBySession.get(session);
//	        api.disconnectUser(evictable, ClientDisconnectionReason.KICK);
//	        tot++;
//	      }
//	    }
//	    this.logger.info("Evicted " + tot + " users.");
//	  }
	  
	  private String getOwnerDetails()
	  {
	    StringBuilder sb = new StringBuilder();
	    if (this.ownerZone != null) {
	      sb.append("Zone: ").append(this.ownerZone.getName());
	    } else if (this.ownerRoom != null) {
	      sb.append("Zone: ").append(this.ownerRoom.getZone().getName()).append("Room: ").append(this.ownerRoom.getName()).append(", Room Id: ").append(this.ownerRoom.getId());
	    }
	    return sb.toString();
	  }
	  
	  private void populateTransientFields()
	  {
	    this.logger = LoggerFactory.getLogger(getClass());
	  }
}
