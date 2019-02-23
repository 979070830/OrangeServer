package com.orange.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSShutdownHook
  extends Thread
{
  private final Logger log;
  
  public OSShutdownHook()
  {
    super("OrangeServerEngine ShutdownHook");
    this.log = LoggerFactory.getLogger(getClass());
  }
  
  public void run()
  {
    this.log.warn("OrangeServerEngine is shutting down. The process may take a few seconds...");
  }
}