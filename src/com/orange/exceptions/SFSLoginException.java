package com.orange.exceptions;

public class SFSLoginException
extends SFSException
{
public SFSLoginException() {}

public SFSLoginException(String message)
{
  super(message);
}

public SFSLoginException(String message, SFSErrorData data)
{
  super(message, data);
}
}

