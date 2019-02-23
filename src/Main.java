import com.orange.server.OrangeServerEngine;


public class Main {

	/**
	 * 主入口类
	 * @param args
	 */
	public static void main(String[] args)
	{
		boolean clusterMode = false;
	    boolean useConsole = false;
	    if (args.length > 0)
	    {
	      clusterMode = args[0].equalsIgnoreCase("cluster");
	      useConsole = (args.length > 1) && (args[1].equalsIgnoreCase("console"));
	    }
	    
	    OrangeServerEngine orangeServer = OrangeServerEngine.getInstance();
	    orangeServer.setClustered(clusterMode);
	    if (useConsole) {
	    	orangeServer.startDebugConsole();
	    }
	    orangeServer.start();
	}

}
