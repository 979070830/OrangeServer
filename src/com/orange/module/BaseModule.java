package com.orange.module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Message;
import com.orange.entities.User;
import com.orange.entities.Zone;
import com.orange.entities.extensions.BaseGameExtension;

public class BaseModule implements IModule{
	
	private static Map<String,BaseModule> moduleMap;
	
	protected String _moduleType;
	protected BaseGameExtension curExtension;
	protected Zone curZone;
	
	public BaseModule(String $moduleType,BaseGameExtension parentExtension)
	{
		constructor($moduleType,parentExtension);
	}
	
	private void constructor(String $moduleType,BaseGameExtension parentExtension)
	{
		_moduleType = $moduleType;
		curExtension = parentExtension;
		curZone = curExtension.getParentZone();
		
		if(moduleMap==null)
		{
			moduleMap = new ConcurrentHashMap<String,BaseModule>();
		}
		moduleMap.put(_moduleType, this);
		
		initEventListener();
		initClientRequestListener();
	}
	
	/**
	 * 初始化客户端派发的sfs2x集成的事件的监听
	 */
	protected void initEventListener()
	{
		
	}
	/**
	 * 初始化客户端Request(请求)事件的监听 
	 */
	protected void initClientRequestListener()
	{
		
	}
	
	public static BaseModule getModuleByModuleType(String $moduleType)
	{
		return moduleMap.get($moduleType);
	}
	/**
	 * 获取当前模块类型
	 */
	public String getModuleType()
	{
		return _moduleType;
	}
	
	protected void send(String cmdName, String _moduleType, Message params,
			User recipient) {
		//this.curExtension.send(cmdName, _moduleType, params, recipient);
	}
}
