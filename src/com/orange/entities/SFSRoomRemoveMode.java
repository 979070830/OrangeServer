package com.orange.entities;

public enum SFSRoomRemoveMode {
	DEFAULT,  WHEN_EMPTY,  WHEN_EMPTY_AND_CREATOR_IS_GONE,  NEVER_REMOVE;
	  
  public static SFSRoomRemoveMode fromString(String id)
  {
    return valueOf(id.toUpperCase());
  }
}
