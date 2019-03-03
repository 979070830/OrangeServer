package com.orange.entities.extensions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.orange.annotations.Instantiation;
import com.orange.annotations.MultiHandler;

@Instantiation(Instantiation.InstantiationMode.NEW_INSTANCE)
public class SFSHandlerFactory
  implements IHandlerFactory
{
  private static final String DOT_SEPARATOR = ".";
  private final Map<String, Class<?>> handlers;
  private final Map<String, Object> cachedHandlers;
  private final BaseExtension parentExtension;
  
  public SFSHandlerFactory(BaseExtension parentExtension)
  {
    this.handlers = new ConcurrentHashMap();
    this.cachedHandlers = new ConcurrentHashMap();
    this.parentExtension = parentExtension;
  }
  
  public void addHandler(String handlerKey, Class<?> handlerClass)
  {
    this.handlers.put(handlerKey, handlerClass);
  }
  
  public void addHandler(String handlerKey, Object requestHandler)
  {
    setHandlerParentExtension(requestHandler);
    this.cachedHandlers.put(handlerKey, requestHandler);
  }
  
  public synchronized void clearAll()
  {
    this.handlers.clear();
    this.cachedHandlers.clear();
  }
  
  public synchronized void removeHandler(String handlerKey)
  {
    this.handlers.remove(handlerKey);
    if (this.cachedHandlers.containsKey(handlerKey)) {
      this.cachedHandlers.remove(handlerKey);
    }
  }
  
  public Object findHandler(String key)
    throws InstantiationException, IllegalAccessException
  {
    Object handler = getHandlerInstance(key);
    if (handler == null)
    {
      int lastDotPos = key.lastIndexOf(".");
      if (lastDotPos > 0) {
        key = key.substring(0, lastDotPos);
      }
      handler = getHandlerInstance(key);
      if ((handler != null) && (!handler.getClass().isAnnotationPresent(MultiHandler.class))) {
        handler = null;
      }
    }
    return handler;
  }
  
  private Object getHandlerInstance(String key)
    throws InstantiationException, IllegalAccessException
  {
    Object handler = this.cachedHandlers.get(key);
    if (handler != null) {
      return handler;
    }
    Class<?> handlerClass = (Class)this.handlers.get(key);
    if (handlerClass == null) {
      return null;
    }
    handler = handlerClass.newInstance();
    
    setHandlerParentExtension(handler);
    if (handlerClass.isAnnotationPresent(Instantiation.class))
    {
      Instantiation instAnnotation = (Instantiation)handlerClass.getAnnotation(Instantiation.class);
      if (instAnnotation.value() == Instantiation.InstantiationMode.SINGLE_INSTANCE) {
        this.cachedHandlers.put(key, handler);
      }
    }
    return handler;
  }
  
  private void setHandlerParentExtension(Object handler)
  {
    if ((handler instanceof IClientRequestHandler)) {
      ((IClientRequestHandler)handler).setParentExtension(this.parentExtension);
    } else if ((handler instanceof IServerEventHandler)) {
      ((IServerEventHandler)handler).setParentExtension(this.parentExtension);
    }
  }
}
