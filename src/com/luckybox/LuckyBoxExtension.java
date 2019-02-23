package com.luckybox;

import com.orange.entities.Room;
import com.orange.entities.Zone;
import com.orange.entities.extensions.BaseExtension;

public class LuckyBoxExtension extends BaseExtension
{
	public LuckyBoxExtension()
	{
		super();
		//记录主游戏区实例
		//MyGame.getInstance().mainExtension = this;
	}
	@Override
	public void init() 
	{
		System.out.println("--------游戏主要扩展区(MainExtension) 初始化完成--------");
		//MyGame.getInstance().initCompleteZone(this.getParentZone().getName());
	}
	
	public Room getGameRoom()
	{
		return this.getParentRoom();
	}
	
	public Zone getZone()
	{
		return this.getParentZone();
	}
}
