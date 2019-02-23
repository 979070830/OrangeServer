package com.orange.entities.extensions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.orange.api.SFSApi;
import com.orange.core.ISFSEvent;
import com.orange.core.SFSEventType;
import com.orange.entities.Room;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.exceptions.OSException;
import com.orange.exceptions.OSRuntimeException;
import com.orange.server.OrangeServerEngine;

public class BaseExtension implements ISFSEventListener{
	private String name;
	  private String fileName;
	  private String configFileName;
	  private ExtensionLevel level;
	  private ExtensionType type;
	  private Room parentRoom = null;
	  private Zone parentZone = null;
	  private volatile boolean active;
	  private final OrangeServerEngine sfs;
	  private Properties configProperties;
	  private ExtensionReloadMode reloadMode;
	  private String currentPath;
	  protected volatile int lagSimulationMillis = 0;
	  protected volatile int lagOscillation = 0;
	  private final Logger logger;
	  private Random rnd;
	  protected final SFSApi sfsApi;
	  
	  public BaseExtension()
	  {
	    this.logger = LoggerFactory.getLogger("Extensions");
	    this.active = true;
	    
	    this.sfs = OrangeServerEngine.getInstance();
	    this.sfsApi = this.sfs.getAPIManager().getSFSApi();
	  }
	  
	  public String getCurrentFolder()
	  {
	    return this.currentPath;
	  }
	  
	  public String getName()
	  {
	    return this.name;
	  }
	  
	  public void setName(String name)
	  {
	    if (this.name != null) {
	      throw new OSException("Cannot redefine name of extension: " + toString());
	    }
	    this.name = name;
	    

	    this.currentPath = ("extensions/" + name + "/");
	  }
	  
	  public String getExtensionFileName()
	  {
	    return this.fileName;
	  }
	  
	  public Properties getConfigProperties()
	  {
	    return this.configProperties;
	  }
	  
	  public String getPropertiesFileName()
	  {
	    return this.configFileName;
	  }
	  
	  public void setPropertiesFileName(String fileName)
	    throws IOException
	  {
	    if (this.configFileName != null) {
	      throw new OSRuntimeException("Cannot redefine properties file name of an extension: " + toString());
	    }
	    boolean isDefault = false;
	    if ((fileName == null) || (fileName.length() == 0) || (fileName.equals("config.properties")))
	    {
	      isDefault = true;
	      this.configFileName = "config.properties";
	    }
	    else
	    {
	      this.configFileName = fileName;
	    }
	    String fileToLoad = "extensions/" + this.name + "/" + this.configFileName;
	    if (isDefault) {
	      loadDefaultConfigFile(fileToLoad);
	    } else {
	      loadCustomConfigFile(fileToLoad);
	    }
	  }
	  
	  public SFSApi getApi()
	  {
	    return this.sfsApi;
	  }
	  
	  public void handleServerEvent(ISFSEvent event)
	    throws Exception
	  {}
	  
	  public Object handleInternalMessage(String cmdName, Object params)
	  {
	    return null;
	  }
	  
	  private void loadDefaultConfigFile(String fileName)
	  {
	    this.configProperties = new Properties();
	    try
	    {
	      this.configProperties.load(new FileInputStream(fileName));
	    }
	    catch (IOException localIOException) {}
	  }
	  
	  private void loadCustomConfigFile(String fileName)
	    throws IOException
	  {
	    this.configProperties = new Properties();
	    this.configProperties.load(new FileInputStream(fileName));
	  }
	  
	  public void setExtensionFileName(String fileName)
	  {
	    if (this.fileName != null) {
	      throw new OSException("Cannot redefine file name of an extension: " + toString());
	    }
	    this.fileName = fileName;
	  }
	  
	  public Room getParentRoom()
	  {
	    return this.parentRoom;
	  }
	  
	  public void setParentRoom(Room room)
	  {
	    if (this.parentRoom != null) {
	      throw new OSException("Cannot redefine parent room of extension: " + toString());
	    }
	    this.parentRoom = room;
	  }
	  
	  public Zone getParentZone()
	  {
	    return this.parentZone;
	  }
	  
	  public void setParentZone(Zone zone)
	  {
	    if (this.parentZone != null) {
	      throw new OSException("Cannot redefine parent zone of extension: " + toString());
	    }
	    this.parentZone = zone;
	  }
	  
	  public void addEventListener(SFSEventType eventType, ISFSEventListener listener)
	  {
	    if (this.level == ExtensionLevel.ZONE) {
	      this.sfs.getExtensionManager().addZoneEventListener(eventType, listener, this.parentZone);
	    } else if (this.level == ExtensionLevel.ROOM) {
	      this.sfs.getExtensionManager().addRoomEventListener(eventType, listener, this.parentRoom);
	    }
	  }
	  
	  public void removeEventListener(SFSEventType eventType, ISFSEventListener listener)
	  {
	    if (this.level == ExtensionLevel.ZONE) {
	      this.sfs.getExtensionManager().removeZoneEventListener(eventType, listener, this.parentZone);
	    } else if (this.level == ExtensionLevel.ROOM) {
	      this.sfs.getExtensionManager().removeRoomEventListener(eventType, listener, this.parentRoom);
	    }
	  }
	  
	  public boolean isActive()
	  {
	    return this.active;
	  }
	  
	  public void setActive(boolean flag)
	  {
	    this.active = flag;
	  }
	  
	  public ExtensionLevel getLevel()
	  {
	    return this.level;
	  }
	  
	  public void setLevel(ExtensionLevel level)
	  {
	    if (this.level != null) {
	      throw new OSException("Cannot change level for extension: " + toString());
	    }
	    this.level = level;
	  }
	  
	  public ExtensionType getType()
	  {
	    return this.type;
	  }
	  
	  public void setType(ExtensionType type)
	  {
	    if (this.type != null) {
	      throw new OSException("Cannot change type for extension: " + toString());
	    }
	    this.type = type;
	  }
	  
	  public ExtensionReloadMode getReloadMode()
	  {
	    return this.reloadMode;
	  }
	  
	  public void setReloadMode(ExtensionReloadMode mode)
	  {
	    if (this.reloadMode != null) {
	      throw new OSException("Cannot change reloadMode for extension: " + toString());
	    }
	    this.reloadMode = mode;
	  }
	  
	  public void send(String cmdName, Message params, List<User> recipients)
	  {
	    send(cmdName, params, recipients, false);
	  }
	  
	  public void send(String cmdName, Message params, User recipient)
	  {
	    send(cmdName, params, recipient, false);
	  }
	  
	  public void send(String cmdName, Message params, List<User> recipients, boolean useUDP)
	  {
//	    if (useUDP) {
//	      params.removeElement("$FS_REQUEST_UDP_TIMESTAMP");
//	    }
	    checkLagSimulation();
	    Room room = this.level == ExtensionLevel.ROOM ? this.parentRoom : null;
	    this.sfsApi.sendExtensionResponse(cmdName, params, recipients, room, useUDP);
	  }
	  
	  public void send(String cmdName, Message params, User recipient, boolean useUDP)
	  {
//	    if (useUDP) {
//	      params.removeElement("$FS_REQUEST_UDP_TIMESTAMP");
//	    }
	    checkLagSimulation();
	    Room room = this.level == ExtensionLevel.ROOM ? this.parentRoom : null;
	    this.sfsApi.sendExtensionResponse(cmdName, params, recipient, room, useUDP);
	  }
	  
//	  public String toString()
//	  {
//	    return String.format("{ Ext: %s, Type: %s, Lev: %s, %s, %s }", new Object[] { this.name, this.type, this.level, this.parentZone, this.parentRoom == null ? "{}" : this.parentRoom });
//	  }
	  
	  public Logger getLogger()
	  {
	    return this.logger;
	  }
	  
//	  public void trace(Object... args)
//	  {
//	    trace(ExtensionLogLevel.INFO, args);
//	  }
//	  
//	  public void trace(ExtensionLogLevel level, Object... args)
//	  {
//	    String traceMsg = getTraceMessage(args);
//	    if (level == ExtensionLogLevel.DEBUG) {
//	      this.logger.debug(traceMsg);
//	    } else if (level == ExtensionLogLevel.INFO) {
//	      this.logger.info(traceMsg);
//	    } else if (level == ExtensionLogLevel.WARN) {
//	      this.logger.warn(traceMsg);
//	    } else if (level == ExtensionLogLevel.ERROR) {
//	      this.logger.error(traceMsg);
//	    }
//	    this.sfs.getTraceMonitor().handleTraceMessage(new TraceMessage(this.parentZone, this.parentRoom, level, traceMsg));
//	  }
	  
	  private String getTraceMessage(Object[] args)
	  {
	    StringBuilder traceMsg = new StringBuilder().append("{").append(this.name).append("}: ");
	    for (Object o : args) {
	      traceMsg.append(o.toString()).append(" ");
	    }
	    return traceMsg.toString();
	  }
	  
	  protected void removeEventsForListener(ISFSEventListener listener)
	  {
	    if (this.level == ExtensionLevel.ZONE) {
	      this.sfs.getExtensionManager().removeListenerFromZone(listener, this.parentZone);
	    } else if (this.level == ExtensionLevel.ROOM) {
	      this.sfs.getExtensionManager().removeListenerFromRoom(listener, this.parentRoom);
	    }
	  }
	  
	  private void checkLagSimulation()
	  {
	    if (this.lagSimulationMillis > 0) {
	      try
	      {
	        long lagValue = this.lagSimulationMillis;
	        if (this.lagOscillation > 0)
	        {
	          if (this.rnd == null) {
	            this.rnd = new Random();
	          }
	          int sign = this.rnd.nextInt(100) > 49 ? 1 : -1;
	          

	          lagValue += sign * this.rnd.nextInt(this.lagOscillation);
	        }
	        if (this.logger.isDebugEnabled()) {
	          this.logger.debug("Lag simulation, sleeping for: " + lagValue + "ms.");
	        }
	        System.out.println("Lag: " + lagValue);
	        
	        Thread.sleep(lagValue);
	      }
	      catch (InterruptedException e)
	      {
	        this.logger.warn("Interruption during lag simulation: " + e);
	      }
	    }
	  }
	  
	  public void destroy()
	  {
		  
	  }
	  
	  public void init()
	  {
		  
	  }
}
