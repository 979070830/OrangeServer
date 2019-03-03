package com.orange.entities.extensions;

public abstract interface IHandlerFactory
{
  public abstract void addHandler(String paramString, Class<?> paramClass);
  
  public abstract void addHandler(String paramString, Object paramObject);
  
  public abstract void removeHandler(String paramString);
  
  public abstract Object findHandler(String paramString)
    throws InstantiationException, IllegalAccessException;
  
  public abstract void clearAll();
}
