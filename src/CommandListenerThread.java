import java.io.IOException;
import java.util.Observable;

public class CommandListenerThread extends Observable implements Runnable {
	private boolean stopFlag;
	private boolean disconnected;
	private Connection con;
	private Command lastCommand = new Command();

	public CommandListenerThread() {
	}

	public CommandListenerThread(Connection connection) {
		this.disconnected = false;
		this.con = connection;
		this.lastCommand = new Command();
	}

	void setConnection(Connection con) {
		disconnected = false;
		this.con = con;
		lastCommand = new Command();
	}

	Command getLastCommand() {
		assert lastCommand == null;
		return lastCommand;
	}

	boolean isDisconnected() {
		return disconnected;
	}

	@Override
	public void run() {
			while (!disconnected) {
				try {
					synchronized (this) {
							try {
								this.lastCommand = con.receive();
								System.out.printf("%s %s\n", lastCommand.getClass(), lastCommand.toString());
							} catch (NullPointerException e) {
							}
						}


						if (lastCommand != null)
							if ((lastCommand.type == (Command.CommandType.DISCONNECT)
									|| (lastCommand.type.toString().equals("REJECTED")))) {
								disconnected = true;
								System.out.println("test");

							}

					}
				 catch (IOException e) {

					e.printStackTrace();
				}

				this.setChanged();
				this.notifyObservers();

			}
		}

	void start() {
		Thread t = new Thread(this);
		t.start();
	}

	void stop() {
		disconnected = true;
	}

}
