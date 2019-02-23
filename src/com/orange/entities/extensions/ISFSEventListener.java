package com.orange.entities.extensions;

import com.orange.core.ISFSEvent;

public abstract interface ISFSEventListener
{
  public abstract void handleServerEvent(ISFSEvent paramISFSEvent)
    throws Exception;
}

