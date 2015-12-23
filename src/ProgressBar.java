import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressBar {
	private volatile JFrame frame;
	private volatile JProgressBar progressBar;

	public ProgressBar(int min, int max, String title) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				frame.setBounds(100, 70, 300, 70);
				frame.getContentPane().setLayout(new BorderLayout());
				frame.setVisible(true);
				frame.setTitle(title);
				progressBar = new JProgressBar(min, max);
				progressBar.setValue(0);
				progressBar.setStringPainted(true);
				frame.getContentPane().add(progressBar, BorderLayout.CENTER);
			}
		});
	}

	public int getValue() {
		return progressBar.getValue();
	}

	public void setValue(int n) {
		progressBar.setValue(n);
	}

	public void dispose() {
		frame.dispose();
	}
}
