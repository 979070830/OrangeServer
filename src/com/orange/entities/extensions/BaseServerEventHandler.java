package com.orange.entities.extensions;

import com.orange.core.ISFSEvent;
import com.orange.exceptions.SFSException;

public class BaseServerEventHandler implements IServerEventHandler{

	@Override
	public void handleServerEvent(ISFSEvent paramISFSEvent) throws SFSException {
		// TODO Auto-generated method stub
		System.out.println("handleServerEvent"+paramISFSEvent);
	}

	@Override
	public void setParentExtension(BaseExtension paramSFSExtension) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BaseExtension getParentExtension() {
		// TODO Auto-generated method stub
		return null;
	}

}
