package com.orange.entities.extensions;

import com.google.protobuf.Message;
import com.orange.entities.User;

public abstract interface IClientRequestHandler
{
  public abstract void handleClientRequest(User paramUser, Message paramISFSObject);
  
  public abstract void setParentExtension(BaseExtension paramSFSExtension);
  
  public abstract BaseExtension getParentExtension();
}
