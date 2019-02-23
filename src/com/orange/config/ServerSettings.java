package com.orange.config;

import java.util.ArrayList;
import java.util.List;

public class ServerSettings
{
  //public transient IAdminHelper adminHelper;
  public volatile List<SocketAddress> socketAddresses = new ArrayList();
  public volatile IpFilterSettings ipFilter = new IpFilterSettings();
  public volatile FlashCrossDomainPolicySettings flashCrossdomainPolicy = new FlashCrossDomainPolicySettings();
  public volatile int systemControllerThreadPoolSize = 1;
  public volatile int extensionControllerThreadPoolSize = 1;
  public volatile int systemControllerRequestQueueSize = 10000;
  public volatile int extensionControllerRequestQueueSize = 10000;
  public volatile int schedulerThreadPoolSize = 1;
  public volatile int protocolCompressionThreshold = 300;
  public String protocolMode;
  public boolean useBinaryProtocol = true;
  public RemoteAdminSettings remoteAdmin = new RemoteAdminSettings();
  public BannedUserManagerSettings bannedUserManager = new BannedUserManagerSettings();
  public MailerSettings mailer = new MailerSettings();
  public WebServerSettings webServer = new WebServerSettings();
  public WebSocketEngineSettings webSocket = new WebSocketEngineSettings();
  public volatile boolean startExtensionFileMonitor = false;
  public volatile boolean useDebugMode = false;
  public volatile boolean extensionRemoteDebug = true;
  public volatile boolean useFriendlyExceptions = true;
  public int sessionMaxIdleTime;
  public int userMaxIdleTime;
  public String licenseCode = "";
  public String licenseEmails = "";
  public volatile boolean ghostHunterEnabled = true;
  public volatile boolean statsExtraLoggingEnabled = true;
  public volatile boolean enableSmasherController = true;
  public AnalyticsSettings analytics = new AnalyticsSettings();
  
  public static final class SocketAddress
  {
    public static final String TYPE_UDP = "UDP";
    public static final String TYPE_TCP = "TCP";
    public volatile String address = "127.0.0.1";
    public volatile int port = 9339;
    public volatile String type = "TCP";
  }
  
  public static final class IpFilterSettings
  {
    public List<String> addressBlackList = new ArrayList();
    public List<String> addressWhiteList = new ArrayList();
    public volatile int maxConnectionsPerAddress = 5;
  }
  
  public static final class FlashCrossDomainPolicySettings
  {
    public volatile boolean useMasterSocketPolicy = false;
    public volatile String policyXmlFile = "crossdomain.xml";
  }
  
  public static final class RemoteAdminSettings
  {
    public List<ServerSettings.AdminUser> administrators = new ArrayList();
    public List<String> allowedRemoteAddresses = new ArrayList();
    public int adminTcpPort = 9933;
  }
  
  public static final class WebServerSettings
  {
    public volatile boolean isActive = true;
    public volatile String cfgFile = "jetty/cfg/jetty.xml";
    public volatile int blueBoxPollingTimeout = 26;
    public volatile int blueBoxMsgQueueSize = 40;
  }
  
  public static final class MailerSettings
  {
    public volatile boolean isActive = true;
    public volatile String mailHost = "";
    public volatile String mailUser = "";
    public volatile String mailPass = "";
    public volatile int smtpPort = 25;
    public volatile int workerThreads = 1;
  }
  
  public static final class BannedUserManagerSettings
  {
    public boolean isAutoRemove = true;
    public boolean isPersistent = true;
    public String customPersistenceClass = null;
  }
  
  public static final class WebSocketEngineSettings
  {
    public boolean isActive = false;
    public String bindAddress = "127.0.0.1";
    public int tcpPort = 8888;
    public boolean isSSL = false;
    public List<String> validDomains = new ArrayList();
  }
  
  public static final class AnalyticsSettings
  {
    public static final String RUN_EVERYDAY = "[everyday]";
    public boolean isActive = false;
    public String runOnDay = "[everyday]";
    public int runAtHour = 2;
    public boolean runOnStartup = false;
    public boolean rebuildDB = false;
    public boolean skipGeolocation = false;
    public String sourceFolder = "";
    public String locale = "";
  }
  
//  public ISFSObject toSFSObject()
//  {
//    ISFSObject sfsObj = SFSObject.newInstance();
//    
//    return sfsObj;
//  }
//  
//  public static ServerSettings fromSFSObject(ISFSObject sfsObj)
//  {
//    ServerSettings settings = new ServerSettings();
//    
//    return settings;
//  }
  
  public static final class AdminUser
  {
    public volatile String login;
    public volatile String password;
  }
}