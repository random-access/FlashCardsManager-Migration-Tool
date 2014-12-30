package core;

import gui.MainFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class StartApp {
	public static void main(String[] args) {
		JButton startButton = new JButton("Transfer starten");
		MainFrame frame = new MainFrame(startButton);
		TransferController ctl = new TransferController(frame);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ctl.start();
				startButton.setEnabled(false);
			}
		});
   }
}
