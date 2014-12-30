package core;

import gui.IView;

import java.io.IOException;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import utils.FileUtils;
import db.DatabaseConnector;

public class TransferController extends Thread {
	private static final String APP_FOLDER = FileUtils.appDirectory("Lernkarten");
	private static final String DATABASEv1_FOLDER = APP_FOLDER + "/database_1";
	private static final String DATABASEv2_FOLDER = APP_FOLDER + "/database_2";
	private static final String MEDIA_FOLDER = APP_FOLDER + "/medias";
	private IView view;

	public TransferController(IView view) {
		this.view = view;
	}
	
	@Override
	public void run() {
		try {
			setStatus("Datenbanktransfer wird vorbereitet ...\n");
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
