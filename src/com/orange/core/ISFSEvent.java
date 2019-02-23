package com.orange.core;

public interface ISFSEvent {
	  public abstract SFSEventType getType();
	  
	  public abstract Object getParameter(ISFSEventParam paramISFSEventParam);
}
