import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StartFormForInitMicro<JForm> {
	private JFrame frame;
	private JComboBox comboBox;
	private JButton test;
	private JButton stopTest;
	private JButton startProgramm;
	private static HashMap<String, Mixer> microphones;
	public static TargetDataLine microphoneLine;
	public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);
	private boolean isStartThread;
	private Socket s;

	public void initMicro() {
		DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, FORMAT);
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
					comboBox.addItem(new String(mixerInfos[i].toString().getBytes("Windows-1252")));
					microphones.put(new String(mixerInfos[i].toString().getBytes("Windows-1252")),
							AudioSystem.getMixer(mixerInfos[i]));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					System.out.println(
							i + " " + new String(mixerInfos[i].toString().getBytes("Windows-1252"), "Windows-1251"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		microphones = new HashMap<String, Mixer>();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				StartFormForInitMicro window = new StartFormForInitMicro();
				window.frame.setVisible(true);
				window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}

	public StartFormForInitMicro() {
		frame = new JFrame();
		frame.setBounds(100, 100, 400, 90);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		comboBox = new JComboBox<String>();
		initMicro();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		startProgramm = new JButton();
		startProgramm.setText("Start ChatApp");
		test = new JButton();
		test.setText("Test");
		stopTest = new JButton();
		stopTest.setText("stop test");
		stopTest.setEnabled(false);
		buttonPanel.add(startProgramm);
		buttonPanel.add(test);
		buttonPanel.add(stopTest);
		frame.getContentPane().add(comboBox);
		frame.getContentPane().add(buttonPanel);
		isStartThread = false;
		startProgramm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					System.out.println(microphones.get(comboBox.getSelectedItem()));
					Class.forName("com.mysql.jdbc.Driver");
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								MainForm window = new MainForm(microphones.get(comboBox.getSelectedItem()));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					frame.dispose();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		test.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stu
				test.setEnabled(false);
				startProgramm.setEnabled(false);
				if (!isStartThread) {
					new Thread(new Runnable() {
						public void run() {
							ServerSocket ss;
							try {
								ss = new ServerSocket(28420);
								Socket s = ss.accept();
								DataInputStream is = new DataInputStream(s.getInputStream());
								AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);
								AudioInputStream audioIn;
								audioIn = new AudioInputStream(is, FORMAT, 4);
								DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
								SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
								line.open(FORMAT);
								line.start();
								int bufferSize = (int) FORMAT.getSampleRate() * FORMAT.getFrameSize();
								byte buffer[] = new byte[bufferSize];
								int count;
								while (((count = is.read(buffer, 0, buffer.length)) != -1)) {
									System.out.println(count);
									if (count > 0) {
										line.write(buffer, 0, count);
									}
								}
								System.out.println("exit");
								line.drain();
								line.close();
								ss.close();

							} catch (IOException | LineUnavailableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}).start();
				}

				new Thread(new Runnable() {
					public void run() {
						byte buffer[] = new byte[(int) FORMAT.getSampleRate() * FORMAT.getFrameSize()];
						try {
							if (!isStartThread) {
								s = new Socket("127.0.0.1", 28420);
							}
							DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, MainForm.FORMAT);
							Mixer mixer = microphones.get(comboBox.getSelectedItem());
							microphoneLine = (TargetDataLine) mixer.getLine(lineInfo);
							microphoneLine.open(FORMAT);
							microphoneLine.start();
							DataOutputStream out = new DataOutputStream(s.getOutputStream());
							while (stopTest.isEnabled()) {
								int cnt = microphoneLine.read(buffer, 0, buffer.length);
								if (cnt > 0) {
									out.write(buffer, 0, cnt);
									out.flush();
								}
							}
							microphoneLine.stop();
							microphoneLine.close();
							isStartThread = true;
							test.setEnabled(true);
							startProgramm.setEnabled(true);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(0);
						}
					}
				}).start();
				stopTest.setEnabled(true);

			}

		});

		stopTest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				stopTest.setEnabled(false);

			}

		});
	}
}
