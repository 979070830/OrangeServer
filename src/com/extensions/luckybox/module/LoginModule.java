package com.extensions.luckybox.module;

import java.util.ArrayList;

import com.google.protobuf.TestMsgPB.TestMsg;
import com.orange.core.ISFSEvent;
import com.orange.core.SFSEventParam;
import com.orange.core.SFSEventType;
import com.orange.entities.User;
import com.orange.entities.extensions.BaseGameExtension;
import com.orange.entities.extensions.BaseServerEventHandler;
import com.orange.exceptions.SFSException;
import com.orange.module.BaseModule;

public class LoginModule extends BaseModule{

	public LoginModule(String $moduleType,BaseGameExtension parentExtension) {
		super($moduleType,parentExtension);
	}

	@Override
	protected void initEventListener() {
		
		curExtension.addEventHandler(SFSEventType.USER_LOGIN, new LoginEventHandler());//局部类不能用类引用 ，因为其他包访问不到，只能用其的实例化
		
		curExtension.addEventHandler(SFSEventType.USER_JOIN_ZONE , new LoinZoneEventListener());//登陆成功才触发这个事件-->打开选择创建角色界面

		curExtension.addEventHandler(SFSEventType.USER_DISCONNECT , new UserDisconnectEventListener());
	}
	
	@Override
	protected void initClientRequestListener() {
		super.initClientRequestListener();
	}
	
	class LoginEventHandler extends BaseServerEventHandler{
		@Override
		public void handleServerEvent(ISFSEvent event) throws SFSException {
			
		}
	}
	
	class LoinZoneEventListener extends BaseServerEventHandler{
		@Override
		public void handleServerEvent(ISFSEvent event) throws SFSException {
			User user = (User)event.getParameter(SFSEventParam.USER);
			
			TestMsg.Builder testBuilder = TestMsg.newBuilder();
			testBuilder.setNickName("测试成功啦");
			testBuilder.setNanos(222);
			ArrayList arr = new ArrayList();
			arr.add(2222);
			arr.add(343535);
			testBuilder.addAllArray(arr);
			
			ArrayList list = new ArrayList();
			list.add(testBuilder.build());
			list.add(testBuilder.build());
			
			curExtension.send("", list, user);
			System.out.println("发送数据");
		}
	}
	
	class UserDisconnectEventListener extends BaseServerEventHandler{
		@Override
		public void handleServerEvent(ISFSEvent event) throws SFSException {
			
		}
	}
}
