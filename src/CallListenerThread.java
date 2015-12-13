
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

public class CallListenerThread extends Observable implements Runnable {
	private CallListener callListener;
	private Caller.CallStatus callStatus;
	private Connection connection;
	private Socket fileSocket;
	private boolean isFile;
	private volatile boolean isOpen;
	// TODO: Add lastEvent;

	public CallListenerThread() throws IOException {
		callListener = new CallListener();
	}
	
	public CallListenerThread(String localNick) throws IOException {
		callListener = new CallListener(localNick);
	}

	public CallListenerThread(String localNick, String localIp) throws IOException {
		callListener = new CallListener(localNick, localIp);
	}

	public Caller.CallStatus getCallStatus() {
		return callStatus;
	}

	public SocketAddress getListenAddress() throws IOException {
		return callListener.getListenAddress();
	}

	public String getLocalNick() {
		return callListener.getLocalNick();
	}

	public SocketAddress getRemoteAddress() throws IOException {
		return callListener.getRemoteAddress();
	}

	public Connection getConnection() {
		return connection;
	}
	
	public Socket getSocket(){
		return fileSocket;
	}
	
	public void setIsFile(boolean isFIle){
		this.isFile=isFile;
	}
	
	public boolean isFile(){
		return this.isFile;
	}

	public boolean isBusy() {
		return callListener.isBusy();
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (isFile){
					fileSocket=callListener.getSocet();
					isFile=false;
				}
				else
					connection = callListener.getConnection();
				if (connection == null) {
					callStatus = Caller.CallStatus.valueOf("BUSY");
				} else {
					callStatus = Caller.CallStatus.valueOf("OK");
				}
			} catch (IOException e) {
				System.out.println("SmthWrong");
			}
			setChanged();
			notifyObservers();
		}

	}

	public void setBusy(boolean busy) {
		callListener.setBusy(busy);
	}


	public void setLocalNick(String localNick) {
		callListener.setLocalNick(localNick);
	}

	public void start() {
		this.isOpen = true;
		this.isFile = false;
		Thread t = new Thread(this);
		t.start();
	};

	public void stop() {
		this.isOpen = false;
	}

}
