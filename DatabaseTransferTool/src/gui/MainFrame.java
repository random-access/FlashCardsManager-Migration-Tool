package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements IView {
	private JButton btnClose;
	private JScrollPane scp;
	private JTextArea txt;
	private JPanel pnlControls, pnlCenter;
	
	public MainFrame (JButton startButton) {
		setTitle("Database Transfer Tool");
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
		btnClose = new JButton("schlie\u00dfen");
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
				
			}
		});
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

	@Override
	public void showSuccessMessage(String text) {
		JOptionPane.showMessageDialog(this, text, "Fertig", JOptionPane.INFORMATION_MESSAGE);
		pnlControls.removeAll();
		pnlControls.add(btnClose);
		MainFrame.this.revalidate();
		
	}
}
