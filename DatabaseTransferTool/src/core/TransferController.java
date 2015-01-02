package core;

import gui.IView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import utils.FileUtils;
import db.DatabaseConnector;

public class TransferController extends Thread {
	private final String APP_FOLDER;
	private final String DATABASEv1_FOLDER;
	private final String DATABASEv2_FOLDER;
	private final String MEDIA_FOLDER;
	private IView view;

	public TransferController(IView view) {
		this.view = view;
		APP_FOLDER = FileUtils.appDirectory("Lernkarten", this);
		DATABASEv1_FOLDER = APP_FOLDER + "/database_1";
		DATABASEv2_FOLDER = APP_FOLDER + "/database_2";
		MEDIA_FOLDER = APP_FOLDER + "/media";
	}
	
	@Override
	public void run() {
		try {
			setStatus("Datenbanktransfer wird vorbereitet ...\n");
			Properties p = System.getProperties();
			p.setProperty("derby.system.home", APP_FOLDER);
			DatabaseConnector targetDbex = new DatabaseConnector(DATABASEv2_FOLDER, this);
			targetDbex.connect();
			DatabaseConnector srcDbex = new DatabaseConnector(DATABASEv1_FOLDER, this);
			srcDbex.connect();
			targetDbex.createDBv2Tables();
			targetDbex.transferProjectsTable(srcDbex);
			targetDbex.transferFlashcards(srcDbex);
			targetDbex.transferPics(srcDbex, MEDIA_FOLDER);
			targetDbex.deleteTmpMediaTable();
			srcDbex.disconnect();
			targetDbex.disconnect();
			view.showSuccessMessage("Neue Datenbank wurde erfolgreich erstellt!");
			// TODO: Datenbank v1 loeschen?
			setStatus("Datenbanktranfer war erfolgreich!\n");
		} catch (SQLException e) {
			if (e.getSQLState().equals("X0Y32")) {
				view.setErrorMessage("Es existiert bereits eine Datenbank in der aktuellsten Version!");
			} else {
				view.setErrorMessage("SQL Fehler");
			}
			setStatus("\n" + e.getMessage() + "\nSQL-Status: " + e.getSQLState() + "\nFehlercode: " + e.getErrorCode());
		} catch (IOException e) {
			view.setErrorMessage("IO Fehler");
			setStatus(e.getMessage());
		} catch (ClassNotFoundException e) {
			view.setErrorMessage("Treiberproblem");
			setStatus(e.getMessage());
		}
	}

	public void setStatus(String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				view.updateStatus(message);
		 }
	 });
		
	}
	
}
