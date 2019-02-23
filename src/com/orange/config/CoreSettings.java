package com.orange.config;

public final class CoreSettings
{
  public String systemControllerClass = "com.smartfoxserver.v2.controllers.SystemController";
  public String extensionControllerClass = "com.smartfoxserver.v2.controllers.ExtensionController";
  public String ioHandlerClass = "com.smartfoxserver.v2.protocol.SFSIoHandler";
  public String sessionManagerClass = "com.smartfoxserver.bitswarm.sessions.DefaultSessionManager";
  public String packetQueuePolicyClass = "com.smartfoxserver.bitswarm.sessions.DefaultPacketQueuePolicy";
  public String readBufferType = "HEAP";
  public String writeBufferType = "HEAP";
  public int maxIncomingRequestSize = 4096;
  public int maxReadBufferSize = 1024;
  public int maxWriteBufferSize = 32768;
  public int socketAcceptorThreadPoolSize = 1;
  public int socketReaderThreadPoolSize = 1;
  public int socketWriterThreadPoolSize = 1;
  public int sessionPacketQueueSize = 120;
  public boolean tcpNoDelay = false;
  public boolean packetDebug = false;
  public boolean lagDebug = false;
  public int bbMaxLogFiles = 10;
  public int bbMaxLogFileSize = 1000000;
  public boolean bbDebugMode = false;
}

