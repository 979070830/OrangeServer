package com.orange.entities.managers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.core.BaseCoreService;
import com.orange.core.ICoreService;
import com.orange.core.ISFSEvent;
import com.orange.core.SFSEventType;
import com.orange.entities.extensions.ISFSEventListener;

public class EventManager extends BaseCoreService implements ICoreService{
	private int corePoolSize = 4;
	  private int maxPoolSize = 5;
	  private int threadKeepAliveTime = 60;
	  private final ThreadPoolExecutor threadPool;
	  private final Map<SFSEventType, Set<ISFSEventListener>> listenersByEvent;
	  private final Logger logger;
	  
	  private static final class SFSEventRunner
	    implements Runnable
	  {
	    private final ISFSEventListener listener;
	    private final ISFSEvent event;
	    
	    public SFSEventRunner(ISFSEventListener listener, ISFSEvent event)
	    {
	      this.listener = listener;
	      this.event = event;
	    }
	    
	    public void run()
	    {
	      try
	      {
	        this.listener.handleServerEvent(this.event);
	      }
	      catch (Exception e)
	      {
	        LoggerFactory.getLogger(getClass()).warn("Error in event handler: " + e + ", Event: " + this.event + " Listener: " + this.listener);
	      }
	    }
	  }
	  
	  public EventManager()
	  {
	    setName("SFSEventManager");
	    

	    this.logger = LoggerFactory.getLogger(EventManager.class);
	    

	    this.threadPool = new ThreadPoolExecutor(
	    
	      this.corePoolSize, 
	      this.maxPoolSize, 
	      this.threadKeepAliveTime, 
	      TimeUnit.SECONDS, 
	      new LinkedBlockingQueue());
	    


	    this.listenersByEvent = new ConcurrentHashMap();
	  }
	  
	  public void init(Object o)
	  {
	    super.init(o);
	    this.logger.info(this.name + " initalized");
	  }
	  
	  public void destroy(Object o)
	  {
	    super.destroy(o);
	    this.listenersByEvent.clear();
	    this.logger.info(this.name + " shut down.");
	  }
	  
	  public void setThreadPoolSize(int poolSize)
	  {
	    this.threadPool.setCorePoolSize(poolSize);
	  }
	  
	  public synchronized void addEventListener(SFSEventType type, ISFSEventListener listener)
	  {
	    Set<ISFSEventListener> listeners = (Set)this.listenersByEvent.get(type);
	    if (listeners == null)
	    {
	      listeners = new CopyOnWriteArraySet();
	      this.listenersByEvent.put(type, listeners);
	    }
	    listeners.add(listener);
	  }
	  
	  public boolean hasEventListener(SFSEventType type)
	  {
	    boolean found = false;
	    
	    Set<ISFSEventListener> listeners = (Set)this.listenersByEvent.get(type);
	    if ((listeners != null) && (listeners.size() > 0)) {
	      found = true;
	    }
	    return found;
	  }
	  
	  public synchronized void removeEventListener(SFSEventType type, ISFSEventListener listener)
	  {
	    Set<ISFSEventListener> listeners = (Set)this.listenersByEvent.get(type);
	    if (listeners != null) {
	      listeners.remove(listener);
	    }
	  }
	  
	  public void dispatchEvent(ISFSEvent event)
	  {
	    Set<ISFSEventListener> listeners = (Set)this.listenersByEvent.get(event.getType());
	    if ((listeners != null) && (listeners.size() > 0)) {
	      for (ISFSEventListener listener : listeners) {
	        this.threadPool.execute(new SFSEventRunner(listener, event));
	      }
	    }
	  }
}
