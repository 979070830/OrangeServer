package com.orange.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.TestMsgPB;

public class AllMessageClassUtil {
	
	private static Map<String,Class<?>> classMap;
	
	public static Class<?> getClass(String classSimpleName)
	{
		if(classMap == null)
		{
			init();
		}
		return classMap.get(classSimpleName);
	}
	
	private static void init()
	{
		classMap = new HashMap<String,Class<?>>();
		//获取当前的包名
		String packageName = TestMsgPB.class.getPackage().getName();

		List<String> classNameList = null;
		try {
			classNameList = PackageUtil.getClassName(packageName);
			
			//System.out.println("包下所有类："+classNameList);
			
			Class<?> cls = null;
            for(String classPath : classNameList) {
            	cls = Class.forName(classPath);
            	if(cls != null && com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(cls))
            	{
            		if(classMap.containsKey(cls.getSimpleName()))
            		{
            			System.out.println("类名重复:"+cls.getSimpleName());
            		}
            		else
            		{
            			classMap.put(cls.getSimpleName(), cls);
            		}
            	}
//            	else
//            	{
//            		System.out.println("过滤:"+classPath);
//            	}
            }
            
            System.out.println("包下所有Message实体类："+classMap);
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
