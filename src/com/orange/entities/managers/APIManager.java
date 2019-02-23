package com.orange.entities.managers;

import com.orange.api.SFSApi;
import com.orange.server.OrangeServerEngine;

public class APIManager{
	
	private final String serviceName = "APIManager";
	  private OrangeServerEngine sfs;
	  private SFSApi sfsApi;
//	  private ISFSBuddyApi buddyApi;
//	  private ISFSGameApi gameApi;
//	  private ISFSMMOApi mmoApi;
	  
	  public void init(Object o)
	  {
	    this.sfs = OrangeServerEngine.getInstance();
	    this.sfsApi = new SFSApi(this.sfs);
//	    this.buddyApi = new SFSBuddyApi(this.sfs);
//	    this.gameApi = new SFSGameApi(this.sfs);
//	    this.mmoApi = new SFSMMOApi(this.sfs);
	  }
	  
	  public SFSApi getSFSApi()
	  {
	    return this.sfsApi;
	  }
	  
//	  public ISFSBuddyApi getBuddyApi()
//	  {
//	    return this.buddyApi;
//	  }
//	  
//	  public ISFSGameApi getGameApi()
//	  {
//	    return this.gameApi;
//	  }
//	  
//	  public ISFSMMOApi getMMOApi()
//	  {
//	    return this.mmoApi;
//	  }
	  
	  public void destroy(Object arg0) {}
	  
	  public String getName()
	  {
	    return "APIManager";
	  }
	  
	  public void handleMessage(Object msg)
	  {
	    throw new UnsupportedOperationException("Not supported");
	  }
	  
	  public void setName(String arg0)
	  {
	    throw new UnsupportedOperationException("Not supported");
	  }
}
