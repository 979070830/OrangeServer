import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import com.google.protobuf.TestMsgPB.TestMsg;
import com.orange.server.WSServer;


public class Main {

	/**
	 * 主入口类
	 * @param args
	 */
	public static void main(String[] args) {
		int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8009;
        }
        System.out.println("java版本号：" + System.getProperty("java.version")); // java版本号
        
        Class dd = TestMsg.class;
        System.out.println(dd);
        //telnet 192.168.1.125 8081
        //telnet localhost 8081
        WSServer server = new WSServer("localhost",8009,false);
		try {
			server.start();
		} catch (SSLException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

}
