package com.orange.entities.extensions;

import com.google.protobuf.Message;
import com.orange.annotations.MultiHandler;
import com.orange.core.ISFSEvent;
import com.orange.core.SFSEventType;
import com.orange.entities.Room;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.exceptions.OSRuntimeException;

public class BaseGameExtension extends BaseExtension{

	@Override
	public void init() 
	{
		super.init();
		this.initModules();
		System.out.println("--------初始化扩展区"+this.getParentZone().getName()+"完成--------");
	}
	
	public Room getGameRoom()
	{
		return this.getParentRoom();
	}
	
	public Zone getZone()
	{
		return this.getParentZone();
	}
	
	public void initModules()
	{
		
	}
	
	public static final String MULTIHANDLER_REQUEST_ID = "__[[REQUEST_ID]]__";
	  private final IHandlerFactory handlerFactory;
	  //private final IFilterChain filterChain;
	  
	  public BaseGameExtension()
	  {
	    this.handlerFactory = new SFSHandlerFactory(this);
	    //this.filterChain = new SFSExtensionFilterChain(this);
	  }
	  
	  public void destroy()
	  {
	    this.handlerFactory.clearAll();
	    //this.filterChain.destroy();
	    removeEventsForListener(this);
	  }
	  
	  protected void addRequestHandler(String requestId, Class<?> theClass)
	  {
	    if (!IClientRequestHandler.class.isAssignableFrom(theClass)) {
	      throw new OSRuntimeException(
	      
	        String.format(
	        
	        "Provided Request Handler does not implement IClientRequestHandler: %s, Cmd: %s", new Object[] {
	        theClass, 
	        requestId }));
	    }
	    this.handlerFactory.addHandler(requestId, theClass);
	  }
	  
	  protected void addRequestHandler(String requestId, IClientRequestHandler requestHandler)
	  {
	    this.handlerFactory.addHandler(requestId, requestHandler);
	  }
	  
	  protected void addEventHandler(SFSEventType eventType, Class<?> theClass)
	  {
	    if (!IServerEventHandler.class.isAssignableFrom(theClass)) {
	      throw new OSRuntimeException(
	      
	        String.format(
	        
	        "Provided Event Handler does not implement IServerEventHandler: %s, Cmd: %s", new Object[] {
	        theClass, 
	        eventType.toString() }));
	    }
	    addEventListener(eventType, this);
	    

	    this.handlerFactory.addHandler(eventType.toString(), theClass);
	  }
	  
	  public void addEventHandler(SFSEventType eventType, IServerEventHandler handler)
	  {
	    addEventListener(eventType, this);
	    

	    this.handlerFactory.addHandler(eventType.toString(), handler);
	  }
	  
	  protected void removeRequestHandler(String requestId)
	  {
	    this.handlerFactory.removeHandler(requestId);
	  }
	  
	  protected void removeEventHandler(SFSEventType eventType)
	  {
	    removeEventListener(eventType, this);
	    this.handlerFactory.removeHandler(eventType.toString());
	  }
	  
	  protected void clearAllHandlers()
	  {
	    this.handlerFactory.clearAll();
	  }
	  
	  public void handleClientRequest(String requestId, User sender, Message params)
	  {
		  System.out.println("handleClientRequest"+requestId+sender+params);
//	    if (this.filterChain.size() > 0) {
//	      if (this.filterChain.runRequestInChain(requestId, sender, params) == FilterAction.HALT) {
//	        return;
//	      }
//	    }
//	    try
//	    {
//	      IClientRequestHandler handler = (IClientRequestHandler)this.handlerFactory.findHandler(requestId);
//	      if (handler == null) {
//	        throw new OSRuntimeException("Request handler not found: '" + requestId + "'. Make sure the handler is registered in your extension using addRequestHandler()");
//	      }
//	      if (handler.getClass().isAnnotationPresent(MultiHandler.class))
//	      {
//	        String[] requestNameTokens = requestId.split("\\.");
//	        params.putUtfString("__[[REQUEST_ID]]__", requestNameTokens[(requestNameTokens.length - 1)]);
//	      }
//	      handler.handleClientRequest(sender, params);
//	    }
//	    catch (InstantiationException err)
//	    {
//	      trace(ExtensionLogLevel.WARN, new Object[] { "Cannot instantiate handler class: " + err });
//	    }
//	    catch (IllegalAccessException err)
//	    {
//	      trace(ExtensionLogLevel.WARN, new Object[] { "Illegal access for handler class: " + err });
//	    }
	  }
	  
	  public void handleServerEvent(ISFSEvent event)
	    throws Exception
	  {
	    String handlerId = event.getType().toString();
//	    if (this.filterChain.size() > 0) {
//	      if (this.filterChain.runEventInChain(event) == FilterAction.HALT) {
//	        return;
//	      }
//	    }
	    try
	    {
	      IServerEventHandler handler = (IServerEventHandler)this.handlerFactory.findHandler(handlerId);
	      if (handler == null)
	      {
	        if (getLevel() == ExtensionLevel.ROOM) {
	          if (getParentZone().getRoomById(getParentRoom().getId()) == null) {
	            return;
	          }
	        }
	        throw new OSRuntimeException("Event handler not found: '" + handlerId + "'. Make sure the handler is registered in your extension using addEventHandler()");
	      }
	      handler.handleServerEvent(event);
	    }
	    catch (InstantiationException err)
	    {
	      trace(ExtensionLogLevel.WARN, new Object[] { "Cannot instantiate handler class: " + err });
	    }
	    catch (IllegalAccessException err)
	    {
	      trace(ExtensionLogLevel.WARN, new Object[] { "Illegal access for handler class: " + err });
	    }
	  }
	  
//	  public final void addFilter(String filterName, SFSExtensionFilter filter)
//	  {
//	    this.filterChain.addFilter(filterName, filter);
//	  }
//	  
//	  public void removeFilter(String filterName)
//	  {
//	    this.filterChain.remove(filterName);
//	  }
//	  
//	  public void clearFilters()
//	  {
//	    this.filterChain.destroy();
//	  }
}
