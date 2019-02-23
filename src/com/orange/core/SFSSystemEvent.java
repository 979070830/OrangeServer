package com.orange.core;

import java.util.Map;

public class SFSSystemEvent extends SFSEvent{
	  private final Map<ISFSEventParam, Object> sysParams;
	  
	  public SFSSystemEvent(SFSEventType type, Map<ISFSEventParam, Object> params, Map<ISFSEventParam, Object> sysParams)
	  {
	    super(type, params);
	    this.sysParams = sysParams;
	  }
	  
	  public Object getSysParameter(ISFSEventParam key)
	  {
	    return this.sysParams.get(key);
	  }
	  
	  public void setSysParameter(ISFSEventParam key, Object value)
	  {
	    if (this.sysParams != null) {
	      this.sysParams.put(key, value);
	    }
	  }
}
