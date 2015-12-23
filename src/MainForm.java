import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.Socket;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;

public class MainForm<JForm> {

	private JFrame frame;

	private JTextField textField;
	private JTextField nickField;
	private JTextField remoteLogiField;
	private JTextField remoteAddrField;
	private HistoryView textArea;
	private JTextArea messageArea;
	private JButton send;
	private JButton sendFile;
	private JButton discButton;
	private CallListener callListener;
	private JButton connectButt;
	private Caller caller;
	private Connection connection;
	private CallListenerThread callLT;
	private HistoryModel model;
	private CommandListenerThread commandLT;
	public volatile static boolean micro;
	public volatile static boolean voice;
	private ServerConnection server;
	private ContactsView friends;
	private LocalContactsView local;
	private JList list, list1;
	private String login;
	private Socket fileSocket;
	private String filename;
	private boolean isConnected;
	public static TargetDataLine microphoneLine;

	public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private class MyDispatcher implements KeyEventDispatcher {
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (connection != null) {
				if ((!nickField.isFocusOwner()) && (!messageArea.isFocusOwner()) && (!remoteAddrField.isFocusOwner())) {
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						if (e.getKeyCode() == KeyEvent.VK_Q) {
							if (!micro) {
								connection.sendSpeakCommand();
								System.out.println("sendSpeakCommand");
							}
							micro = true;
						}
					} else if (e.getID() == KeyEvent.KEY_RELEASED) {
						if (e.getKeyCode() == KeyEvent.VK_Q) {
							connection.sendSpeakStopCommand();
							micro = false;
						}
					}
				}
			}
			return false;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainForm window = new MainForm();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public MainForm() throws IOException {
		frame = new JFrame();
		frame.setBounds(100, 100, 850, 400);
		frame.setTitle("ChatApp 2015");
		isConnected = false;
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());
		initMicro();
		frame.addWindowListener(new WindowListener() {

			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}

			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				try {
					commandLT.stop();
					server.goOffline();
				} catch (NullPointerException e) {
					System.out.println("CLT");
				}
				if (connection != null)
					try {
						connection.disconnect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				System.exit(0);
			}

			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel top_panel = new JPanel();
		top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.X_AXIS));
		JPanel panel_login = new JPanel();
		panel_login.setLayout(new BoxLayout(panel_login, BoxLayout.Y_AXIS));
		JPanel panel_nick = new JPanel();
		panel_nick.setLayout(new BoxLayout(panel_nick, BoxLayout.X_AXIS));
		panel_login.add(panel_nick);
		JLabel loginLabel = new JLabel("local login");
		panel_nick.add(loginLabel);

		nickField = new JTextField();
		nickField.setMaximumSize(new Dimension(150, 20));
		nickField.setToolTipText("You must write your nick for applying");
		panel_nick.add(nickField);
		nickField.setColumns(10);
		JPanel panel_connection = new JPanel();
		panel_connection.setMaximumSize(new Dimension(32767, 100));
		panel_connection.setLayout(new GridLayout(2, 3));
		mainPanel.add(top_panel);
		top_panel.add(panel_login);

		final JButton nickApplyButton = new JButton("Apply");
		panel_login.add(nickApplyButton);

		top_panel.add(panel_connection);

		JLabel remoteNickLabel = new JLabel("Remote login");
		remoteNickLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_connection.add(remoteNickLabel);

		remoteLogiField = new JTextField();
		remoteLogiField.setMaximumSize(new Dimension(150, 20));
		remoteLogiField.setEnabled(false);
		panel_connection.add(remoteLogiField);
		remoteLogiField.setColumns(10);

		discButton = new JButton("Disconnect");
		discButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_connection.add(discButton);
		discButton.setEnabled(false);
		JLabel remoteAddrLabel = new JLabel("Remote addr");
		remoteAddrLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_connection.add(remoteAddrLabel);
		remoteAddrField = new JTextField();
		remoteAddrField.setMaximumSize(new Dimension(150, 20));
		panel_connection.add(remoteAddrField);
		remoteAddrField.setColumns(10);
		remoteAddrField.setToolTipText("You must press Enter to continue");
		connectButt = new JButton("Connect");
		connectButt.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_connection.add(connectButt);
		connectButt.setEnabled(false);
		JPanel main_panel = new JPanel();
		main_panel.setLayout(new GridLayout(1, 1));
		JPanel bot_panel = new JPanel();
		bot_panel.setLayout(new BoxLayout(bot_panel, BoxLayout.X_AXIS));
		mainPanel.add(main_panel);
		model = new HistoryModel();
		textArea = new HistoryView(model);
		textArea.setBackground(new Color(255, 255, 204));
		textArea.setBorder(new LineBorder(Color.CYAN, 3));
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setRows(10);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		main_panel.add(scroll);
		mainPanel.add(bot_panel);
		messageArea = new JTextArea();
		messageArea.setMinimumSize(new Dimension(16, 4));
		messageArea.setMaximumSize(new Dimension(800, 100));
		messageArea.setLineWrap(true);
		messageArea.setEnabled(false);
		bot_panel.add(messageArea);
		send = new JButton("Send");
		send.setMinimumSize(new Dimension(60, 25));
		send.setMaximumSize(new Dimension(100, 50));
		send.setAlignmentX(Component.CENTER_ALIGNMENT);
		send.setEnabled(false);
		bot_panel.add(send);
		sendFile = new JButton("SendFile");
		sendFile.setMinimumSize(new Dimension(60, 25));
		sendFile.setMaximumSize(new Dimension(100, 50));
		sendFile.setAlignmentX(Component.CENTER_ALIGNMENT);
		sendFile.setEnabled(false);
		bot_panel.add(sendFile);

		final JPanel contactsPanel = new JPanel();
		frame.getContentPane().add(contactsPanel);
		contactsPanel.setPreferredSize(new Dimension(230, 400));
		// contactsPanel.setMinimumSize(new Dimension(50, 100));
		contactsPanel.setMaximumSize(new Dimension(800, 800));
		contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.Y_AXIS));
		JLabel name = new JLabel("List of person on server");
		name.setHorizontalAlignment(JLabel.CENTER);
		contactsPanel.add(name);

		contactsPanel.setBorder(BorderFactory.createEtchedBorder());
		JPanel forButton1 = new JPanel();

		frame.getContentPane().add(mainPanel);
		discButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				try {
					if (connection != null) {

						connection.disconnect();
						forDisconnect();
						commandLT.stop();
						connection = null;
						isConnected = false;
						if (!local.findNick(remoteLogiField.getText(), remoteAddrField.getText())) {
							int reply = JOptionPane.showConfirmDialog(null,
									"Do you want to save this person to your contact list", "",
									JOptionPane.YES_NO_OPTION);
							if (reply == 0) {
								ContactsModel modelForCont = new ContactsModel(remoteLogiField.getText(),
										remoteAddrField.getText());
								modelForCont.addLocalNick();
								local.addElement(modelForCont.toString());
								list1.setModel(local);
								frame.validate();
							}
						}

					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

		});

		connectButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!remoteAddrField.getText().equals("")) {
					String login;
					login = nickField.getText();
					caller = new Caller(login, remoteAddrField.getText());
					try {
						connection = caller.call();
						if (connection != null) {
							commandLT.setConnection(connection);
							commandLT.start();
							// ThreadOfCommand();
							forConnect();
							isConnected = true;
							connection.sendNickHello(nickField.getText());
						} else {
							JOptionPane.showMessageDialog(null, "Couldn't connect this ip ");
						}
					} catch (InterruptedException e1) {

						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {

						e1.printStackTrace();
					} catch (IOException e1) {

						e1.printStackTrace();
					}

				} else {
					JOptionPane.showMessageDialog(null, "You must write remote address ");
				}
			}
		});
		messageArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						connection.sendMessage(messageArea.getText());
						model.addMessage(nickField.getText(), new Date(), messageArea.getText());
						textArea.update(model, new Object());
						messageArea.setText("");

					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!messageArea.getText().equals("")) {
						connection.sendMessage(messageArea.getText());
						model.addMessage(nickField.getText(), new Date(), messageArea.getText());

						textArea.update(model, new Object());
						messageArea.setText("");

					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		});
		sendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();
				int ret = fileopen.showDialog(null, "������� ����");
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fileopen.getSelectedFile();
					connection.sendCommandFile(file);
				}
			}
		});
		nickApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (nickField.getText().equals("")) {
					login = "unnamed";
				} else
					login = nickField.getText();
				boolean isCorrectLogin = false;
				for (int i = 0; i < login.toCharArray().length; i++)
					if (login.toCharArray()[i] != ' ') {
						isCorrectLogin = true;
						break;
					}
				if (!isCorrectLogin) {
					login = "unnamed";
				}
				while (login.charAt(0) == ' ')
					login = login.substring(1);
				nickField.setText(login);
				nickField.setEnabled(false);
				try {
					callLT = new CallListenerThread();
					callLT.start();
					commandLT = new CommandListenerThread();
					ThreadOfCall();
					ThreadOfCommand();
					connectButt.setEnabled(true);
					nickApplyButton.setEnabled(false);
					callLT.setLocalNick(login);
					Runnable r = new Runnable() {
						public void run() {
							server = new ServerConnection(login);
							server.connect();
							server.goOnline();
							try {
								friends = new ContactsView(server);
								list = new JList();
								list.setModel(friends);
								local = new LocalContactsView();
								local.writeLocalNicks();
								list1 = new JList();
								list1.setModel(local);
								JScrollPane scroll1 = new JScrollPane(list);
								scroll1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
								JScrollPane scroll2 = new JScrollPane(list1);
								scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
								contactsPanel.add(scroll1);
								JLabel name = new JLabel("List of local contacts");
								name.setHorizontalAlignment(JLabel.CENTER);
								contactsPanel.add(name);
								contactsPanel.add(scroll2);
								frame.validate();
								list.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										if (connection == null) {
											String[] str = list.getSelectedValue().toString().split(" ");
											remoteLogiField.setText(str[0]);
											remoteAddrField.setText(server.getIpForNick(str[0]));
										} else {
											JOptionPane.showMessageDialog(null, "You must disconnect to choose");
										}
									}
								});
								list1.addListSelectionListener(new ListSelectionListener() {
									public void valueChanged(ListSelectionEvent e) {
										if (connection == null) {
											String[] str = list1.getSelectedValue().toString().split("\\|");
											remoteLogiField.setText(str[0]);
											remoteAddrField.setText(str[1]);
										} else {
											JOptionPane.showMessageDialog(null, "You must disconnect to choose");
										}
									}

								});
							} catch (IOException e1) {
								System.out.println("Server connection error");
							}
						}
					};
					new Thread(r).start();
				} catch (BindException e2) {
					e2.printStackTrace();
					JOptionPane.showMessageDialog(null, "You can't open two examples of one program");
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		});
	}

	public void ThreadOfCall() throws IOException {
		callLT.addObserver(new Observer() {
			public void update(Observable arg0, Object arg1) {
				// TODO:Thread, TimeOut
				System.out.println("update listener");
				connection = callLT.getConnection();
				try {
					connection.sendNickHello(nickField.getText());
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				commandLT.setConnection(connection);
				commandLT.start();
				System.out.println("CLT started");
				try {
				} catch (NullPointerException e) {
					System.out.println("null");
				}
			}
		});
	}

	public void ThreadOfCommand() {
		System.out.println("testif");
		commandLT.addObserver(new Observer() {
			public void update(Observable arg0, Object arg1) {
				System.out.println("testobs");
				Command lastCommand = commandLT.getLastCommand();
				System.out.println(lastCommand.getClass() + " " + lastCommand.toString());
				if (lastCommand instanceof MessageCommand) {
					model.addMessage(remoteLogiField.getText(), new Date(), commandLT.getLastCommand().toString());
					textArea.update(model, new Object());
				} else if (lastCommand instanceof NickCommand) {
					if (!isConnected) {
						int reply = JOptionPane.showConfirmDialog(null,
								"Do you want to accept incoming connection from user ".concat(lastCommand.toString()),
								"", JOptionPane.YES_NO_OPTION);
						System.out.println(reply);

						try {
							if (reply == 0) {
								connection.accept();
								remoteAddrField.setText(callLT.getRemoteAddress().toString());
								remoteLogiField.setText(lastCommand.toString());
								forConnect();
							} else {
								forDisconnect();
								connection.reject();
								commandLT.stop();
								connection = null;
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Connection getted");
						isConnected = true;
					} else {
						remoteLogiField.setText(lastCommand.toString());
					}
				} else if (lastCommand instanceof FileCommand) {
					int reply = JOptionPane.showConfirmDialog(null,
							"Do you want to save file " + lastCommand.toString() + "?", "", JOptionPane.YES_NO_OPTION);
					if (reply == 0) {
						try {
							filename = ((FileCommand) lastCommand).getFileName();
							connection.applyFile();
							connection.recieveFile(filename);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							connection.rejectFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else if (lastCommand != null) {
					switch (lastCommand.type) {
					case ACCEPT: {
						model.addMessage(remoteLogiField.getText(), new Date(), "User was accepted");
						textArea.update(model, new Object());
						if (!local.findNick(remoteLogiField.getText(), remoteAddrField.getText())) {
							int reply = JOptionPane.showConfirmDialog(null,
									"Do you want to save this person to your contactlist", "",
									JOptionPane.YES_NO_OPTION);
							if (reply == 0) {
								ContactsModel modelForCont = new ContactsModel(remoteLogiField.getText(),
										remoteAddrField.getText());
								try {
									modelForCont.addLocalNick();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								local.addElement(modelForCont.toString());
								list1.setModel(local);
								frame.validate();
							}
						}
						break;
					}
					case REJECT: {
						model.addMessage(remoteLogiField.getText(), new Date(), "User was rejected");
						textArea.update(model, new Object());
						commandLT.stop();
						isConnected = false;
						forDisconnect();
						break;
					}
					case DISCONNECT: {
						model.addMessage(remoteLogiField.getText(), new Date(), "User was disconnected");
						textArea.update(model, new Object());
						commandLT.stop();
						forDisconnect();
						isConnected = false;
						break;
					}
					case APPLYFILE: {
						connection.sendFile();
						break;
					}
					case SPEAKSTART: {
						connection.recieveVoice();
						break;
					}

					case SPEAKSTOP: {
						break;
					}
					}
				}

			}

		});
	}

	void forDisconnect() {
		send.setEnabled(false);
		remoteLogiField.setText("");
		messageArea.setEnabled(false);
		discButton.setEnabled(false);
		connectButt.setEnabled(true);
		remoteAddrField.setText("");
		remoteAddrField.setEnabled(true);
		sendFile.setEnabled(false);
	}

	void forConnect() throws IOException {
		send.setEnabled(true);
		sendFile.setEnabled(true);
		connectButt.setEnabled(false);
		messageArea.setEnabled(true);
		discButton.setEnabled(true);
		remoteAddrField.setEnabled(false);
	}

	boolean formForConnect(boolean b, final String nick) {
		boolean isconnect = false;
		final JFrame f = new JFrame();
		Container cp = f.getContentPane();
		f.setSize(400, 175);
		f.setLocation(200, 200);
		f.setVisible(b);
		JPanel panel = new JPanel();
		cp.setLayout(null);
		JLabel text = new JLabel("Do you want to accept incoming connection from user ".concat(nick));
		JButton yes = new JButton("Yes");
		JButton no = new JButton("No");
		text.setSize(500, 60);
		text.setLocation(20, 20);
		yes.setSize(90, 25);
		yes.setLocation(70, 95);
		no.setSize(90, 25);
		no.setLocation(220, 95);
		f.setContentPane(cp);
		yes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					connection.accept();
					connection.sendNickHello(nickField.getText());
					remoteAddrField.setText(callListener.getRemoteAddress().toString());
					remoteLogiField.setText(nick);
					forConnect();
					f.setVisible(false);
					f.dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		no.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					connection.reject();
					forDisconnect();
					f.setVisible(false);
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					f.dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		text.setSize(500, 60);
		text.setLocation(20, 20);
		yes.setSize(90, 25);
		yes.setLocation(70, 95);
		no.setSize(90, 25);
		no.setLocation(220, 95);
		cp.add(text);
		cp.add(yes);
		cp.add(no);
		f.setContentPane(cp);
		return isconnect;

	}

	public void formForNewTalk(boolean b, String nick) {
		JFrame f = new JFrame();
		Container cp = f.getContentPane();
		f.setSize(400, 175);
		f.setLocation(200, 200);
		f.setVisible(b);
		JPanel panel = new JPanel();
		cp.setLayout(null);
		JLabel text = new JLabel("New user " + nick + " want to speak with you." + "\n");
		JLabel text1 = new JLabel("Do you want to reject current connection?");
		JButton yes = new JButton("Yes");
		JButton no = new JButton("No");

		text.setSize(500, 60);
		text.setLocation(100, 20);
		text1.setSize(500, 60);
		text1.setLocation(85, 40);
		yes.setSize(90, 25);
		yes.setLocation(70, 95);
		no.setSize(90, 25);
		no.setLocation(220, 95);
		cp.add(text);
		cp.add(text1);
		cp.add(yes);
		cp.add(no);
		f.setContentPane(cp);
	}

	void formWait(boolean b) {
		final JFrame f = new JFrame();
		Container cp = f.getContentPane();
		f.setSize(400, 175);
		f.setLocation(200, 200);
		f.setTitle("         Установка исходящего соединения");
		f.setVisible(b);
		JPanel panel = new JPanel();
		cp.setLayout(null);
		JButton v = new JButton("Прервать ");
		v.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (connection != null) {
						connection.disconnect();

					}
					f.dispose();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JLabel text = new JLabel("Ожидание подтверждения запроса..");
		text.setSize(230, 60);
		text.setLocation(95, 20);
		v.setSize(150, 25);
		v.setLocation(125, 95);
		cp.add(text);
		cp.add(v);
		f.setContentPane(cp);
	}

	private void recieveFile() {
		Runnable r = new Runnable() {
			public void run() {
				try {
					long s;
					Scanner in = new Scanner(fileSocket.getInputStream(), "UTF-8");
					s = Long.parseLong(in.nextLine());
					System.out.println("File size: " + s);
					byte[] byteArray = new byte[1024];
					new File("Recieved").mkdir();
					File f = new File("./Recieved/" + filename);
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					int sp = (int) (s / 1024);
					if (s % 1024 != 0)
						sp++;
					BufferedInputStream bis = new BufferedInputStream(fileSocket.getInputStream());
					while (s > 0) {
						int i = bis.read(byteArray);
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

	private void initMicro() {
		DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, MainForm.FORMAT);
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println("----------------Mixers Available----------------");
		for (int i = 0; i < mixerInfos.length; i++) {
			try {
				System.out.println(new String(mixerInfos[i].toString().getBytes("Windows-1252"), "Windows-1251"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("----------------------Mixers Supporting Line----------------------");
		for (int i = 0; i < mixerInfos.length; i++) {
			Mixer m = AudioSystem.getMixer(mixerInfos[i]);
			if (m.isLineSupported(lineInfo)) {
				try {
					System.out.println(
							i + " " + new String(mixerInfos[i].toString().getBytes("Windows-1252"), "Windows-1251"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			Mixer mixerS = AudioSystem.getMixer(mixerInfos[3]);
			microphoneLine = (TargetDataLine) mixerS.getLine(lineInfo);
			microphoneLine.open(MainForm.FORMAT);
			microphoneLine.start();
			// Thread captureThread = new Thread(new CaptureThread());
			// captureThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
