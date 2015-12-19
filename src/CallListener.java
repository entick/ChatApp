import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

public class CallListener {
	private String localNick;
	private String localIp;
	private ServerSocket sSocket;
	private Socket socket;
	private boolean isBusy;
	private int key;

	public CallListener(String localNick, String localIp) throws IOException {
		this.localNick = localNick;
		this.localIp = localIp;
		this.sSocket = new ServerSocket(Connection.PORT);
		this.isBusy=false;
	}

	public CallListener(String localNick) throws IOException {
		this(localNick, "127.0.0.1");
	}

	public CallListener() throws IOException {
		this.localNick = "NickName";
		this.localIp = "127.0.0.1";
		this.sSocket = new ServerSocket(28411);
		this.isBusy=false;
	}

	// TODO: make function
	public Connection getConnection() throws IOException {
		socket = sSocket.accept();
		Scanner in = new Scanner(socket.getInputStream());
		key=in.nextInt();
		System.out.println(key);
		Socket files = sSocket.accept();
		in = new Scanner(files.getInputStream());
		PrintStream pw;
		while (in.nextInt()!=key){
			pw = new PrintStream(files.getOutputStream(),true, "UTF-8");
			pw.println("ChatApp 2015 user " + localNick + " busy");
			files=sSocket.accept();
			in = new Scanner(socket.getInputStream());
		};
		System.out.println("files getted");
		Socket voice = sSocket.accept();
		in = new Scanner(voice.getInputStream());
		while(in.nextInt()!=key){
			pw = new PrintStream(voice.getOutputStream(),true, "UTF-8");
			pw.println("ChatApp 2015 user " + localNick + " busy");
			voice = sSocket.accept();
			in = new Scanner(socket.getInputStream());
		}
		System.out.println("voice getted");
		return new Connection(socket,files,voice, localNick);
	}
	
	public Socket getSocket(){
		try {
			return sSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SocketAddress getListenAddress() throws IOException {
		return sSocket.getLocalSocketAddress();
	}

	public String getLocalNick() {
		return localNick;
	}

	public SocketAddress getRemoteAddress() throws IOException {
		return socket.getRemoteSocketAddress();
	}

	public boolean isBusy() {
		return sSocket.isBound();
	}

	// TODO: Some with it
	public void setBusy(boolean busy) {
		this.isBusy=busy;
	}

	public void setLocalNick(String localNick) {
		this.localNick = localNick;
	}
	 //for test
	@Override
	public String toString() {
		return "CallListener [localNick=" + localNick + ", localIp=" + localIp + ", sSocket=" + sSocket + "]";
	}

}
