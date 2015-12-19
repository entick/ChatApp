
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Observable;

public class CallListenerThread extends Observable implements Runnable {
	private CallListener callListener;
	private Caller.CallStatus callStatus;
	private Connection connection;
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

	public boolean isBusy() {
		return callListener.isBusy();
	}
	@Override
	public void run() {
		while (true) {
				try {
						connection = callListener.getConnection();
						setChanged();
						notifyObservers();
				} catch (IOException e) {
					System.out.println("SmthWrong");
				}
				
		}

	}

	public void setBusy(boolean busy) {
		callListener.setBusy(busy);
	}

	public void setLocalNick(String localNick) {
		callListener.setLocalNick(localNick);
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	};

}
