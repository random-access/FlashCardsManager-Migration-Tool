package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements IView {
	private JScrollPane scp;
	private JTextArea txt;
	private JPanel pnlControls, pnlCenter;
	
	public MainFrame (JButton startButton) {
		setTitle("JComponent Testklasse");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		pnlControls = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlCenter = new JPanel(new BorderLayout());
		pnlCenter.setBorder(new EmptyBorder(20, 20, 20, 20));
		txt = new JTextArea(15,50);
		txt.setBorder(new EmptyBorder(5,10,5,10));
		txt.setEditable(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		txt.setBackground(Color.BLACK);
		txt.setForeground(Color.WHITE);
		scp = new JScrollPane(txt);
		add(pnlCenter, BorderLayout.CENTER);
		pnlCenter.add(scp);
		add(pnlControls, BorderLayout.SOUTH);
		pnlControls.add(startButton);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void updateStatus(String text) {
		txt.append(text);
		txt.setCaretPosition(txt.getText().length());
	}
	
	public void setErrorMessage(String text) {
		JOptionPane.showMessageDialog(this, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}
}
