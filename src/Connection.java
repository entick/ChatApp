import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Connection {

	private Socket socket;
	public static final int PORT = 28411;
	public static final String ENCODING = "UTF-8";
	public static final char EOL = '\n';
	private PrintStream outStream;
	private Scanner inStream;
	private String nickname;
	private File file;

	public Connection(Socket s, String nickname) throws IOException, SocketException {
		this.socket = s;
		outStream = new PrintStream(s.getOutputStream(),true, ENCODING);
		inStream = new Scanner(s.getInputStream());
		this.nickname = nickname;

	}

	public boolean isOpen() {
		return !socket.isClosed();
	}

	public void sendNickHello(String nick) throws UnsupportedEncodingException, IOException {
		outStream.println("ChatApp 2015 user " + nick);
	}

	public void sendNickBusy(String nick) throws UnsupportedEncodingException, IOException {
		outStream.println("ChatApp 2015 user " + nick + " busy");
	}

	public void accept() throws IOException {
		outStream.println("Accepted");
	}

	public void reject() throws IOException {
		outStream.println("Rejected");
	}

	public void sendMessage(final String message) throws UnsupportedEncodingException, IOException {
		outStream.println("Message");
		outStream.println(message);
	}

	public void disconnect() throws IOException {
		outStream.println("Disconnect");
		outStream.close();
		socket.close();
	}
	
	public void applyFile() throws IOException{
		outStream.println("ApplyFile");
	}
	
	public void rejectFile() throws IOException{
		outStream.println("RegectFile");
	}
	
	public void sendCommandFile(File file){
		this.file=file;
		outStream.println("File "+file.getName()+" "+file.length());
	}
	public void sendFile(){
		Runnable r = new Runnable() {
			public void run() {
				try {
					Socket timeS = new Socket(socket.getInetAddress(),28411);
					PrintStream ps = new PrintStream(timeS.getOutputStream(), true, "UTF-8");
					System.out.println("Sending " + file.getName() + "...");
					System.out.println("Start");
					try {
						byte[] byteArray = new byte[1024];
						FileInputStream fis = new FileInputStream(file.getPath());
						long s;
						s = file.length();
						ps.println(s);
						int sp = (int) (s / 1024);
						if (s % 1024 != 0)
							sp++;
						BufferedOutputStream bos = new BufferedOutputStream(timeS.getOutputStream());
						Thread.sleep(500);
						while (s > 0) {
							int i = fis.read(byteArray);
							bos.write(byteArray, 0, i);
							s -= i;
						}
						bos.flush();
						fis.close();
						file=null;
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					System.err.println("File not found!");
				} catch (IOException e) {
					System.err.println("IOException");
				} catch (Exception e) {

				}
			}
		};
		new Thread(r).start();
	}


	public Command receive() throws IOException {
		String str;
		try{
		str=inStream.nextLine();
		if (str.toUpperCase().startsWith("CHATAPP 2015 USER")) {
			Scanner in = new Scanner(str);
			in.next();
			return new NickCommand(in.next(), in.skip(" [a-z,A-Z]{4} ").nextLine().replaceAll(" BUSY",""), str.toUpperCase().endsWith(" BUSY"));
		} else if (str.toUpperCase().equals("APPLYFILE")){
			return new Command(Command.CommandType.APPLYFILE);
		} else if (str.toUpperCase().equals("REJECTFILE")){
			return new Command(Command.CommandType.REJECTFILE);
		} else if (str.toUpperCase().equals("MESSAGE")) {
				str=inStream.nextLine();
			return new MessageCommand(str);
		} else if (str.toUpperCase().startsWith("FILE")){
			str.replaceFirst("FILE ", "");
			return new FileCommand(str);
		} else {
			str = str.toUpperCase().replaceAll("[\r\n]","");
			for (Command.CommandType cc : Command.CommandType.values())
				if (cc.toString().equals(str))
					return new Command(Command.CommandType.valueOf(str.replaceAll("ED", "")));
		}
		}catch(NoSuchElementException e){
			System.out.println("recieve NoSuchElementException");
		}
		return new Command(Command.CommandType.NULL);
	}

}
