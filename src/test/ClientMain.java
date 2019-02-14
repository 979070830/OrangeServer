package test;

public class ClientMain {
	public static void main(String[] args) throws Exception {
		int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8009;
        }

        new TestClient().connect(port, "localhost");
	}
}
