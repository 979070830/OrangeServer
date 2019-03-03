package com.extensions.luckybox.module;

public interface ModuleType {
	//接口
	public String getValue();
	public String getDescribe();

	
	enum LOGIN implements ModuleType{
		S2CPushLoginResult("s2clogin1","推送登陆结果"),
		GetOpenServerListData("login2","获取打开服务器列表界面"),
		CreateRole("login3","创建角色"),
		DeleteRole("login4","删除角色"),
		SeleteRoleEnterGame("login5","选择角色进入游戏"),
		UpdateRoleCidInfo("login6","更新角色个推CID信息"),
		OnHook("login7","在线挂机状态")

		;public static String type = "login";
		private LOGIN(String typeValue , String describe){	this.typeValue = typeValue;	this.describe = describe;	}
		private String typeValue,describe;
		public String getValue(){	return this.typeValue;	}
		public String getDescribe(){	return this.describe;	}
	}
}


