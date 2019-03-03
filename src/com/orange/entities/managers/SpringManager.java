package com.orange.entities.managers;

import java.util.ArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringManager {
	
	public static final String DefaultType = "";
	public static final String DebugType = "debug.";
	public static final String DeployType = "deploy.";
	
	private String curType = DefaultType;
	public ApplicationContext context;//ApplicationContext
	
	private ArrayList<String []> allExtensionSpringConfigs = new ArrayList<String []>();
	private String [] curAllSpringConfigs;
	
	public void addExtensionSpringConfig(String [] configs)
	{
		allExtensionSpringConfigs.add(configs);
	}
	
	public void initContext()
	{
		//springCofing = new String[]{};
		ArrayList<String> configs = new ArrayList<String>();
		for (String [] extensionSpringConfigs : allExtensionSpringConfigs) {
			
			int len = extensionSpringConfigs.length;
			for (int i = 0; i < len; i++) {
				String string = extensionSpringConfigs[i];
				configs.add("classpath*:/"+curType+string);
			}
		}
		
		//添加事务配置
		if(configs.size() > 0) configs.add("classpath*:/TransactionTemplate.xml");
		
		
		curAllSpringConfigs = configs.toArray(new String[0]);
		
		if(curAllSpringConfigs.length > 0) context = new ClassPathXmlApplicationContext(curAllSpringConfigs);
		
		System.out.println("SpringManager.initContext()初始化完成");
	}
	
}
