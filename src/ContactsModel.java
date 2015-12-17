import java.io.IOException;

import java.io.RandomAccessFile;

public class ContactsModel  {
	private String nick, ip;
	private boolean isOnline;
	private RandomAccessFile f;

	ContactsModel(String nick, String ip) {
		this.ip = ip;
		this.nick = nick;
	}
	ContactsModel(String nick, boolean online) {
		this.isOnline = online;
		this.nick = nick;
	}
	ContactsModel() {

	}



	public String getNick() {
		return nick;
	}

	public boolean isOnline() {
		return isOnline;
	}
	public String toString() {
				if (isOnline)
					return nick + " online";
				return nick;
			}
	public String toString1() {
		return nick + "|" + ip + "\n";
	}
	public String getIp() {
		return ip;

	}

	public void addLocalNick() throws IOException {
		f = new RandomAccessFile("LocalContacts.txt", "rw");
		f.seek(f.length());
		System.out.println(nick+ip);
		String str = nick + "|" + ip + "\n";
		f.write(str.getBytes());
		f.close();
	
	}

}