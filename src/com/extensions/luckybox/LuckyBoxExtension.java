package com.extensions.luckybox;

import com.extensions.luckybox.module.LoginModule;
import com.extensions.luckybox.module.ModuleType;
import com.orange.entities.extensions.BaseGameExtension;

public class LuckyBoxExtension extends BaseGameExtension
{
	public LuckyBoxExtension()
	{
		super();
		
		//String [] springCofing = new String[] {"globalSpringConfig.xml","globalSpringDaoService.xml","globalSpringServiceEx.xml"};
		//this.sfs.getSpringManager().addExtensionSpringConfig(springCofing);
		
		//String [] springCofing2 = new String[] {"springConfig.xml","springDaoService.xml","springServiceEx.xml"};
		//this.sfs.getSpringManager().addExtensionSpringConfig(springCofing2);
		
		
	}
	
	@Override
	public void initModules() 
	{
		super.initModules();
		
		new LoginModule(ModuleType.LOGIN.type,this);
	}
	
	
}
