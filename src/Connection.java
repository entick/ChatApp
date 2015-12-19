import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

public class Connection {

	private Socket socket;
	private Socket voiceSocket;
	private Socket fileSocket;
	public static final int PORT = 28420;
	public static final String ENCODING = "UTF-8";
	public static final char EOL = '\n';
	private PrintStream outStream;
	private Scanner inStream;
	private Scanner inSFile;
	private BufferedOutputStream outFile;
	private BufferedInputStream inFile;
	private PrintStream outPSFile;
	private String nickname;
	private DataInputStream inVoice;
	private DataOutputStream outVoice;
	private File file;

	public Connection(Socket s, Socket fileSocket, Socket voiceSocket, String nickname)
			throws IOException, SocketException {
		this.socket = s;
		this.fileSocket = fileSocket;
		this.voiceSocket = voiceSocket;
		outStream = new PrintStream(this.socket.getOutputStream(), true, ENCODING);
		inStream = new Scanner(this.socket.getInputStream(), "UTF-8");
		inSFile = new Scanner(this.fileSocket.getInputStream(), "UTF-8");
		inFile = new BufferedInputStream(this.fileSocket.getInputStream());
		outFile = new BufferedOutputStream(this.fileSocket.getOutputStream());
		outPSFile = new PrintStream(this.fileSocket.getOutputStream(), true, ENCODING);
		inVoice = new DataInputStream(this.voiceSocket.getInputStream());
		outVoice = new DataOutputStream(this.voiceSocket.getOutputStream());
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

	public void applyFile() throws IOException {
		outStream.println("ApplyFile");
	}

	public void rejectFile() throws IOException {
		outStream.println("RejectFile");
	}

	public void sendCommandFile(File file) {
		this.file = file;
		long fileLength = file.length();
		outStream.println("File " + file.getName() + " Size " + file.length());
	}

	public void sendSpeakCommand() {
		outStream.println("SpeakStart");
		sendVoice();
	}

	public void sendSpeakStopCommand() {
		outStream.println("SpeakStop");
	}

	public void sendFile() {
		Runnable r = new Runnable() {
			public void run() {
				try {
					System.out.println("Sending " + file.getName() + "...");
					System.out.println("Start");
					try {
						byte[] byteArray = new byte[1024];
						FileInputStream fis = new FileInputStream(file.getPath());
						long s;
						s = file.length();
						outPSFile.println(s);
						int sp = (int) (s / 1024);
						if (s % 1024 != 0)
							sp++;
						Thread.sleep(500);
						while (s > 0) {
							int i = fis.read(byteArray);
							outFile.write(byteArray, 0, i);
							System.out.println("sending...");
							s -= i;
						}
						outFile.flush();
						fis.close();
						file = null;
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (Exception e) {

				}
			}
		};
		new Thread(r).start();
	}

	public void sendVoice() {
		Runnable r = new Runnable() {
			public void run() {
				byte buffer[] = new byte[(int) MainForm.FORMAT.getSampleRate() * MainForm.FORMAT.getFrameSize()];
				try {
					while (MainForm.micro) {
						int cnt = MainForm.microphoneLine.read(buffer, 0, buffer.length);
						System.out.println(cnt);
						if (cnt > 0) {
							outVoice.write(buffer, 0, cnt);
							outVoice.flush();
						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		};
		new Thread(r).start();
	}

	public void recieveVoice() {
		Runnable r = new Runnable() {
			public void run() {
				byte buffer[] = new byte[(int) MainForm.FORMAT.getSampleRate() * MainForm.FORMAT.getFrameSize()];
				try {
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, MainForm.FORMAT);
					SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
					line.open(MainForm.FORMAT);
					line.start();
					int count;
					while (((count = inVoice.read(buffer, 0, buffer.length)) != -1)) {
						System.out.println(count);
						if (count > 0) {
							line.write(buffer, 0, count);
						}
					}
					line.drain();
					line.close();
				} catch (IOException | LineUnavailableException | IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(r).start();
	}

	public void recieveFile(String filename) {
		Runnable r = new Runnable() {
			public void run() {
				try {
					long s;
					s = Long.parseLong(inSFile.nextLine());
					System.out.println(s);
					System.out.println("File size: " + s);
					byte[] byteArray = new byte[1024];
					new File("Recieved").mkdir();
					File f = new File("./Recieved/" + filename);
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					int sp = (int) (s / 1024);
					if (s % 1024 != 0)
						sp++;
					while (s > 0) {
						int i = inFile.read(byteArray);
						fos.write(byteArray, 0, i);
						s -= i;
					}
					fos.close();
				} catch (IOException e) {
					System.err.println("Recieve IO Error");
				}
				new JOptionPane().showMessageDialog(null, "Recieved " + filename);
			}
		};
		new Thread(r).start();
	}

	public Command receive() throws IOException {
		String str;
		try {
			str = inStream.nextLine();
			if (str.toUpperCase().startsWith("CHATAPP 2015 USER")) {
				Scanner in = new Scanner(str);
				in.next();
				return new NickCommand(in.next(), in.skip(" [a-z,A-Z]{4} ").nextLine().replaceAll(" BUSY", ""),
						str.toUpperCase().endsWith(" BUSY"));
			} else if (str.toUpperCase().equals("APPLYFILE")) {
				return new Command(Command.CommandType.APPLYFILE);
			} else if (str.toUpperCase().equals("REJECTFILE")) {
				return new Command(Command.CommandType.REJECTFILE);
			} else if (str.toUpperCase().equals("SPEAKSTOP")) {
				return new Command(Command.CommandType.SPEAKSTOP);
			} else if (str.toUpperCase().equals("MESSAGE")) {
				str = inStream.nextLine();
				return new MessageCommand(str);
			} else if (str.toUpperCase().startsWith("FILE")) {
				str = str.substring(5, str.length() - 1);
				return new FileCommand(str);
			} else {
				str = str.toUpperCase().replaceAll("[\r\n]", "");
				for (Command.CommandType cc : Command.CommandType.values())
					if (cc.toString().equals(str))
						return new Command(Command.CommandType.valueOf(str.replaceAll("ED", "")));
			}
		} catch (NoSuchElementException | IndexOutOfBoundsException e) {
			System.out.println("recieve Exception");
		}
		return null;
	}

}
