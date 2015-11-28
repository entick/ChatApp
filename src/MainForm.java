import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.UnexpectedException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.BorderLayout;
import javax.swing.JFormattedTextField;
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

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.omg.CORBA.portable.UnknownException;

import com.sun.glass.events.MouseEvent;
import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

import java.awt.Color;

@SuppressWarnings("unused")
public class MainForm {

	private JFrame frame;

	private JTextField textField;
	private JTextField nickField;
	private JTextField remoteLogiField;
	private JTextField remoteAddrField;
	private HistoryView textArea;
	private JTextArea messageArea;
	private JButton send;
	private JButton discButton;
	private CallListener callListener;
	private JButton connectButt;
	private Caller caller;
	private Connection connection;
	private CallListenerThread callLT;
	private HistoryModel model;
	private CommandListenerThread commandLT;
	private int isPressed;
	private ServerConnection server;
	private ContactsView friends;
	private JList list=new JList();

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainForm window = new MainForm();
					window.frame.setVisible(true);
					Class.forName("com.mysql.jdbc.Driver");
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

		JButton nickApplyButton = new JButton("Apply");
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

		JPanel contactsPanel = new JPanel();
		frame.getContentPane().add(contactsPanel);
		contactsPanel.setPreferredSize(new Dimension(200,400));
		contactsPanel.setLayout(new BorderLayout());
		JPanel forLabel=new JPanel();
		JLabel name = new JLabel("List of person on server");
		forLabel.add(name);
		contactsPanel.add(forLabel, BorderLayout.NORTH);
		JPanel forButton = new JPanel();
		JButton update = new JButton("Update");
		contactsPanel.setBorder(BorderFactory.createEtchedBorder());
		forButton.add(update);
		contactsPanel.add(forButton, BorderLayout.WEST);
		JPanel forButton1 = new JPanel();
		JButton save = new JButton("Save to..");
		update.setEnabled(false);
		contactsPanel.add(forButton1, BorderLayout.EAST);
		forButton1.add(save);
		save.setEnabled(false);
		frame.getContentPane().add(mainPanel);
		update.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (server != null) {
					friends = new ContactsView(server);
					list = new JList(friends);
					JLabel l=new JLabel();
					l.add(list);
					contactsPanel.add(l,BorderLayout.CENTER);
				}
			}

		});
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.TXT","*.*");
				
				JFileChooser fileOpen = new JFileChooser();
				fileOpen.setFileFilter(filter);
				if ( fileOpen.showSaveDialog(null) == JFileChooser.APPROVE_OPTION ) {
		            try ( FileWriter fw = new FileWriter(fileOpen.getSelectedFile()) ) {
		            	for(int i=0;i<friends.getStr().length;i++)
		                fw.write(friends.getStr()[i]+"\n");
		            }
		            catch ( IOException e1 ) {
		                
		            }
		        }
			}
			});

		discButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				try {
					if (connection != null) {

						connection.disconnect();
						forDisconnect();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

		});

		connectButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (remoteAddrField.getText() != "") {
					String login;
					login = nickField.getText();
					caller = new Caller(login, remoteAddrField.getText());
					try {
						connection = caller.call();
						if (connection != null) {
							commandLT = new CommandListenerThread(connection);
							commandLT.start();
							connection.sendNickHello(nickField.getText());
							forConnect();

						}
					} catch (InterruptedException e1) {

						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {

						e1.printStackTrace();
					} catch (IOException e1) {

						e1.printStackTrace();
					}

				} else {
					JOptionPane.showMessageDialog(null, "You must write remote address");
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
		nickApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String login;
				if (nickField.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "You must write your login");
				} else {
					login = nickField.getText();
					try {
						//I'm not sure
						InetAddress addr = InetAddress.getLocalHost();
						server = new ServerConnection(addr.toString(), login);
						server.connect();
						server.goOnline();
					} catch (UnknownHostException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					nickField.setEnabled(false);
					friends = new ContactsView(server);
					list = new JList(friends);
					JLabel l=new JLabel();
					l.add(list);
					contactsPanel.add(l,BorderLayout.CENTER);
					list.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							if (connection == null) {
								remoteLogiField.setText(list.getSelectedValue().toString());
								remoteAddrField.setText(server.getIpForNick(list.getSelectedValue().toString()));
							} else {
								JOptionPane.showMessageDialog(null, "You must disconnect to choose");
							}
						}
					});
					nickApplyButton.setEnabled(false);
					save.setEnabled(true);
					update.setEnabled(true);
					try {
						callLT = new CallListenerThread();
						callLT.start();
						commandLT = new CommandListenerThread();
						ThreadOfCall();
						// ThreadOfCommand();

					} catch (IOException e1) {
						e1.printStackTrace();
					}

					callLT.setLocalNick(login);
				}
			}
		});
	}

	public void ThreadOfCall() throws IOException {
		callLT.addObserver(new Observer() {
			public void update(Observable arg0, Object arg1) {
				connection = callLT.getConnection();
				commandLT.setConnection(connection);
				commandLT.start();
				long t1 = System.currentTimeMillis();
				long t2 = System.currentTimeMillis();
				boolean b = false;
				while (((t2 - t1) <= 100000) && !b) {
					Command command = commandLT.getLastCommand();
					if (command instanceof NickCommand) {
						int reply = JOptionPane.showConfirmDialog(null,
								"Do you want to accept incoming connection from user ".concat(command.toString()), "",
								JOptionPane.YES_NO_OPTION);

						try {
							if (reply == JOptionPane.YES_OPTION) {
								connection.accept();
								connection.sendNickHello(nickField.getText());
								remoteAddrField.setText(callLT.getRemoteAddress().toString());
								remoteLogiField.setText(command.toString());
								forConnect();
								ThreadOfCommand();
							} else {
								connection.reject();
								commandLT.stop();
								forDisconnect();
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						t2 = System.currentTimeMillis();
					}
				}
				if (!b) {
					try {
						connection.disconnect();
						commandLT.stop();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						forConnect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

	}

	public void ThreadOfCommand() {
		commandLT.addObserver(new Observer() {
			@SuppressWarnings("incomplete-switch")
			public void update(Observable arg0, Object arg1) {
				Command lastCommand = commandLT.getLastCommand();
				if (lastCommand instanceof MessageCommand) {
					model.addMessage(remoteLogiField.getText(), new Date(), commandLT.getLastCommand().toString());
					textArea.update(model, new Object());
				} else if (lastCommand instanceof NickCommand) {

					// remoteLogiField.setText(lastCommand.toString());
				} else if (lastCommand != null) {
					switch (lastCommand.type) {
					case ACCEPT: {
						model.addMessage(remoteLogiField.getText(), new Date(), commandLT.getLastCommand().toString());
						textArea.update(model, new Object());
						break;
					}
					case REJECT: {
						model.addMessage(remoteLogiField.getText(), new Date(), commandLT.getLastCommand().toString());
						textArea.update(model, new Object());
						commandLT.stop();
						forDisconnect();
						break;
					}
					case DISCONNECT: {
						model.addMessage(remoteLogiField.getText(), new Date(), commandLT.getLastCommand().toString());
						textArea.update(model, new Object());
						commandLT.stop();
						forDisconnect();
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
	}

	void forConnect() throws IOException {
		send.setEnabled(true);
		connectButt.setEnabled(false);
		messageArea.setEnabled(true);
		discButton.setEnabled(true);
		remoteAddrField.setEnabled(false);
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
		JFrame f = new JFrame();
		Container cp = f.getContentPane();
		f.setSize(400, 175);
		f.setLocation(200, 200);
		f.setTitle("         ��������� ���������� ����������");
		f.setVisible(b);
		JPanel panel = new JPanel();
		cp.setLayout(null);
		JButton v = new JButton("�������� ");
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

		JLabel text = new JLabel("�������� ������������� �������..");
		text.setSize(230, 60);
		text.setLocation(95, 20);
		v.setSize(150, 25);
		v.setLocation(125, 95);
		cp.add(text);
		cp.add(v);
		f.setContentPane(cp);
	}

}
