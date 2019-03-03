package com.orange.server;

import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.config.Configurator;
import com.orange.config.ServerSettings;
import com.orange.core.OSShutdownHook;
import com.orange.entities.managers.APIManager;
import com.orange.entities.managers.EventManager;
import com.orange.entities.managers.ExtensionManager;
import com.orange.entities.managers.SessionManager;
import com.orange.entities.managers.SpringManager;
import com.orange.entities.managers.UserManager;
import com.orange.entities.managers.ZoneManager;
import com.orange.exceptions.ExceptionMessageComposer;
import com.orange.exceptions.OSException;
import com.orange.type.ServerState;
import com.orange.util.StringHelper;
import com.orange.util.TaskScheduler;

public class OrangeServerEngine {
	private final String version = "1.0.0";
	private volatile ServerState state = ServerState.STARTING;
	private volatile boolean initialized = false;
	private volatile boolean started = false;
	private volatile Configurator configurator;

	private TaskScheduler taskScheduler;
	
	private EventManager eventManager;
	private ZoneManager zoneManager;
	private UserManager userManager;
	private SessionManager sessionManager;
	private APIManager apiManager;
	private ExtensionManager extensionManager;
	private SpringManager springManager;

	public EventManager getEventManager() {
		return eventManager;
	}

	public ZoneManager getZoneManager() {
		return zoneManager;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public SessionManager getSessionManager() {
		return sessionManager;
	}
	
	public APIManager getAPIManager() {
		return apiManager;
	}
	
	public ExtensionManager getExtensionManager() {
		return extensionManager;
	}
	
	public SpringManager getSpringManager() {
		return springManager;
	}

	private final Logger log;

	private boolean clustered = false;
	private boolean useConsole = false;
	
	
	///////////////////////////////////////////
	private boolean debug;//程序是否是以debug模式运行的
	private boolean isTest;//是否测试模式（或者是开发模式）
	private boolean isDebugDeploy = false;//是否采取部署配置进行debug
	
	///////////////////////////////////////////
	
	

	public OrangeServerEngine() {
		super();

		//		this.bitSwarmEngine = BitSwarmEngine.getInstance();
			    this.configurator = new Configurator();

			    this.log = LoggerFactory.getLogger(getClass());


		//	    this.networkEvtListener = new NetworkEvtListener(null);
		//	    
			    this.springManager = new SpringManager();
			    this.eventManager = new EventManager();
			    this.zoneManager = new ZoneManager();
			    if (this.userManager == null) {
			      this.userManager = new UserManager();
			    }
			    this.extensionManager = new ExtensionManager();
		//	    this.bannedUserManger = new SFSBannedUserManager();
		//	    this.statsManager = new SFSStatsManager();
		//
			    this.taskScheduler = new TaskScheduler(1);
			    
			    
			    String ipAddress="";
				try {
					ipAddress = InetAddress.getLocalHost().toString();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("ipAddress:"+ipAddress);
			    isDebugDeploy =  (ipAddress.indexOf("192.168.") == -1 && ipAddress.indexOf("127.0.0.1") == -1 && ipAddress.indexOf("localhost") == -1) ? true : false;
				System.out.println("isDebugDeploy:"+isDebugDeploy);
				debug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
				
				if((isTest = debug && !isDebugDeploy))
				{
					System.out.println("当前采取Debug配置进行初始化!");
				}
				else
				{
					System.out.println("当前采取部署配置进行初始化!");
				}
	}

	private static OrangeServerEngine _instance;// = new OrangeServerEngine();
	public static OrangeServerEngine getInstance()
	{
		if (_instance == null) {
			_instance = new OrangeServerEngine();
		}
		return _instance;
	}
	
	public Configurator getConfigurator() {
		return configurator;
	}

	public static void initConfig()
	{
		initConfig("config/server.config");
	}

	public static void initConfig(String configName)
	{
		if(configName == null)
		{

		}
	}

	public void start()
	{
		if (!this.initialized) {
		      initialize();
		    }
		    try
		    {
		      this.configurator.loadConfiguration();
//		      try
//		      {
//		        this.lsManager.execute("boot", null);
//		        this.lsManager.execute("dump", null);
//		      }
//		      catch (SFSException e)
//		      {
//		        this.log.warn(e.toString());
//		      }
		      
		      configureServer();
		      

		      //configureBitSwarm();
		      

	//		      this.traceMonitor = new TraceMessageMonitor();
			      try
			      {
			    	  this.zoneManager.init(null);
			        this.zoneManager.initializeZones();
			        initializedZones();
			      }
			      catch (OSException err)
			      {
			        //this.zoneInitError = err;
			    	  System.out.println(err);
			      }
	//		      this.adminToolService = new AdminToolService();
	//		      this.adminToolService.init(null);
	//		      if (this.sfsConfigurator.getServerSettings().webServer.isActive)
	//		      {
	//		        this.httpServer = new SFSHttpServer();
	//		        this.httpServer.init(this.sfsConfigurator.getServerSettings().webServer.cfgFile);
	//		        this.httpServer.start();
	//		      }
	//		      if (this.sfsConfigurator.getServerSettings().mailer.isActive)
	//		      {
	//		        this.mailService = new SFSPostOffice();
	//		        this.mailService.init(this.sfsConfigurator.getServerSettings().mailer);
	//		      }
	//		      this.bitSwarmEngine.start("SmartFoxServer 2X");
	//		    }
	//		    catch (FileNotFoundException e)
	//		    {
	//		      ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
	//		      msg.setDescription("There has been a problem loading the server configuration. The server cannot start.");
	//		      msg.setPossibleCauses("Make sure that core.xml and server.xml files exist in your config/ folder.");
	//		      
	//		      this.log.error(msg.toString());
	//		    }
	//		    catch (BindException e)
	//		    {
	//		      ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
	//		      msg.setDescription("The specified TCP port cannot be bound to the configured IP address.");
	//		      msg.setPossibleCauses("Probably you are running another instance of SFS2X. Please double check using the AdminTool.");
	//		      msg.addInfo("Start a new browser page at http://<your-sfs-domain>/admin/");
	//		      msg.addInfo("If the problem persists, email us the content of this error message to support[at]smartfoxserver.com");
	//		      this.log.error(msg.toString());
	//		    }
	//		    catch (SFSException e)
	//		    {
	//		      ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
	//		      msg.setDescription("An error occurred during the Server boot, preventing it to start.");
	//		      
	//		      this.log.error(msg.toString());
			      
			      ServerSettings settings = this.configurator.getServerSettings();
			      startWSServer(settings);
			
		    }
		    catch (Exception e)
		    {
		    	ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
		    	msg.setDescription("Unexpected error during Server boot. The server cannot start.");
		    	msg.addInfo("Solution: Please email us the content of this error message, including the stack trace to support[at]smartfoxserver.com");
		      
		    	this.log.error(e.getMessage());
		    }
	}

	public String getVersion()
	{
		return version;
	}

	public void setClustered(boolean clusterMode) {
		this.clustered = clusterMode;
	}

	public boolean isClustered()
	{
		return this.clustered;
	}

	public void startDebugConsole() {

	}

	private void initialize()
	{
		if (this.initialized) {
			throw new IllegalStateException("OrangeServerEngine already initialized!");
		}
		PropertyConfigurator.configure("config/log4j.properties");


		this.log.info("Boot sequence starts...");


		String bootMessage = StringHelper.getAsciiMessage("boot");
		if (bootMessage != null) {
			this.log.info(bootMessage);
		}

		//initLMService();
		//this.lsManager.init(this.log);


		Runtime.getRuntime().addShutdownHook(new OSShutdownHook());


		this.apiManager = new APIManager();
		this.apiManager.init(null);
		
		this.extensionManager.init();
		// 
		//
		//this.ghostUserHunter = new GhostUserHunter();
		//
		//this.bitSwarmEngine.addEventListener("serverStarted", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionAdded", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionLost", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionIdle", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionIdleCheckComplete", this.networkEvtListener);
		//
		//
		//this.bitSwarmEngine.addEventListener("packetDropped", this.networkEvtListener);
		//  
		//this.bitSwarmEngine.addEventListener("sessionReconnectionTry", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionReconnectionSuccess", this.networkEvtListener);
		//this.bitSwarmEngine.addEventListener("sessionReconnectionFailure", this.networkEvtListener);


		this.initialized = true;
	}
	
	  private void configureServer()
	  {
	    ServerSettings settings = this.configurator.getServerSettings();
	    

	    this.taskScheduler.resizeThreadPool(settings.schedulerThreadPoolSize);
	    
//	    
//
//	    this.bannedUserManger.setAutoRemoveBan(settings.bannedUserManager.isAutoRemove);
//	    this.bannedUserManger.setName("BannedUserManager");
//	    this.bannedUserManger.setPersistent(settings.bannedUserManager.isPersistent);
//	    this.bannedUserManger.setPersistenceClass(settings.bannedUserManager.customPersistenceClass);
//	    this.bannedUserManger.init(null);
//	    
//
	    this.extensionManager.setExtensionMonitorActive(settings.startExtensionFileMonitor);
//	    
//
	    ExceptionMessageComposer.globalPrintStackTrace = settings.useDebugMode;
	    ExceptionMessageComposer.useExtendedMessages = settings.useFriendlyExceptions;
//	    
//
//	    this.invitationManager = new SFSInvitationManager();
//	    ((IService)this.invitationManager).init(null);
	  }
	  
	  private void initializedZones()
	  {
		  System.out.println("初始化所有Zone完成");
		  this.springManager.initContext();
	  }
	  
	  private void startWSServer(ServerSettings settings)
	  {
		  	//telnet 192.168.1.125 8081
	        //telnet localhost 8081
	        //WSServer server = new WSServer("localhost",8009,false);
		  	WSServer server = new WSServer(settings.webSocket.bindAddress,settings.webSocket.tcpPort,settings.webSocket.isSSL);
			try {
				server.start();
			} catch (SSLException e) {
				e.printStackTrace();
			}
			catch (CertificateException e) {
				e.printStackTrace();
			}
			
	  }
}
