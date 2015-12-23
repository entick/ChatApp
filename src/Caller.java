import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Caller {
	private String localNick;
	private String ip;
	private SocketAddress remoteAddress;
	private String remoteNick;
	private int key;
	// private final static int REMOTE_PORT = 28411; nused

	public static enum CallStatus {
		BUSY, NO_SERVICE, NOT_ACCESSIBLE, OK, REJECTED
	}

	// TODO:why so many constructors?
	public Caller() {
		this("NickName", "127.0.0.1");
	}

	public Caller(String localNick) {
		this(localNick, "127.0.0.1");
	}

	public Caller(String localNick, SocketAddress remoteAddress) {
		this.localNick = localNick;
		ip = "127.0.0.1";
		this.remoteAddress = remoteAddress;
	}

	public Caller(String localNick, String ip) {
		this.localNick = localNick;
		this.ip = ip;
		remoteAddress = new InetSocketAddress(ip, Connection.PORT);
	}

	public Connection call() throws InterruptedException {
		String ip = remoteAddress.toString(); // "/ip:port"
		try {
			Socket s = new Socket();
			s.connect(remoteAddress, 30000);
			key=(int)(10000*Math.random());
			System.out.println(key);
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println(key);
			pw.flush();
			Socket files= new Socket();
			files.connect(remoteAddress, 30000);
			pw = new PrintWriter(files.getOutputStream());
			pw.println(key);
			pw.flush();
			Socket voice = new Socket();
			voice.connect(remoteAddress, 0);
			pw = new PrintWriter(voice.getOutputStream());
			pw.println(key);
			pw.flush();
			return  new Connection(s,files,voice, localNick);
		} catch (IOException e) {
			System.out.println("Not connected");
			return null;
		}
	}

	public String getLocalNick() {
		return localNick;
	}

	public SocketAddress getRemoteAddess() {
		return remoteAddress;
	}

	// TODO:Where are you getting remoteNick?
	public String getRemoteNick() {
		return remoteNick;
	}

	public void setLocalNick(String localNick) {
		this.localNick = localNick;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	// for test
	@Override
	public String toString() {
		return "Caller [localNick=" + localNick + ", ip=" + ip + ", remoteAddress=" + remoteAddress + ", remoteNick="
				+ remoteNick + "]";
	}

	// TODO:What Status?
	public CallStatus getStatus() {
		return null;
	}

}
