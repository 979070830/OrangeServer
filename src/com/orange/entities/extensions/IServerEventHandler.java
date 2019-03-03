package com.orange.entities.extensions;

import com.orange.core.ISFSEvent;
import com.orange.exceptions.SFSException;

public abstract interface IServerEventHandler
{
  public abstract void handleServerEvent(ISFSEvent paramISFSEvent)
    throws SFSException;
  
  public abstract void setParentExtension(BaseExtension paramSFSExtension);
  
  public abstract BaseExtension getParentExtension();
}
