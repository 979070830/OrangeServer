package com.orange.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.entities.Room;

public class DefaultPlayerIdGenerator {
	private Room parentRoom;
	  private volatile Boolean[] playerSlots;
	  private final Logger logger;
	  
	  public DefaultPlayerIdGenerator()
	  {
	    this.logger = LoggerFactory.getLogger(getClass());
	  }
	  
	  public void init()
	  {
	    this.playerSlots = new Boolean[this.parentRoom.getMaxUsers() + 1];
	    

	    Arrays.fill(this.playerSlots, Boolean.FALSE);
	  }
	  
	  public int getPlayerSlot()
	  {
	    int playerId = 0;
	    synchronized (this.playerSlots)
	    {
	      for (int ii = 1; ii < this.playerSlots.length; ii++) {
	        if (!this.playerSlots[ii].booleanValue())
	        {
	          playerId = ii;
	          this.playerSlots[ii] = Boolean.TRUE;
	          break;
	        }
	      }
	    }
	    if (playerId < 1) {
	      this.logger.warn("No player slot found in " + this.parentRoom);
	    }
	    return playerId;
	  }
	  
	  public void freePlayerSlot(int playerId)
	  {
	    if (playerId == -1) {
	      return;
	    }
	    if (playerId >= this.playerSlots.length) {
	      return;
	    }
	    synchronized (this.playerSlots)
	    {
	      this.playerSlots[playerId] = Boolean.FALSE;
	    }
	  }
	  
	  public void onRoomResize()
	  {
	    Boolean[] newPlayerSlots = new Boolean[this.parentRoom.getMaxUsers() + 1];
	    synchronized (this.playerSlots)
	    {
	      for (int i = 1; i < newPlayerSlots.length; i++) {
	        if (i < this.playerSlots.length) {
	          newPlayerSlots[i] = this.playerSlots[i];
	        } else {
	          newPlayerSlots[i] = Boolean.FALSE;
	        }
	      }
	    }
	    this.playerSlots = newPlayerSlots;
	  }
	  
	  public Room getParentRoom()
	  {
	    return this.parentRoom;
	  }
	  
	  public void setParentRoom(Room room)
	  {
	    this.parentRoom = room;
	  }
}
