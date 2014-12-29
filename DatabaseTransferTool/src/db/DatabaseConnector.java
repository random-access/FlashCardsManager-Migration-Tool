package db;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseConnector {

	private final String driver = "org.apache.derby.jdbc.EmbeddedDriver"; // db-driver
	private final String protocol = "jdbc:derby:"; // database protocol
	private final String dbLocation; // database location

	private Connection conn; // connection

	public DatabaseConnector(String dbLocation) throws ClassNotFoundException {
		this.dbLocation = dbLocation;
		Class.forName(driver); // check if driver is reachable
	}

	public void connect() throws SQLException {
		conn = DriverManager.getConnection(protocol + dbLocation + ";create=true");
		conn.setAutoCommit(false);
		if (conn != null) {
			System.out.println("Successfully created Connection to: " + dbLocation);
		}
	}

	public void disconnect() {
		try {
			conn.commit();
			conn.close();
			boolean gotSQLExc = false;
			try {
				DriverManager.getConnection("jdbc:derby:" + dbLocation + ";shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("08006")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database at " + dbLocation + " shut down with errors");
			} else {
				System.out.println("Database at " + dbLocation + " shut down normally");
			}
			System.gc();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void createDBv2Tables() throws SQLException {
		System.out.print("Creating tables......");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE PROJECTS (PROJ_ID_PK INT PRIMARY KEY, " + "PROJ_TITLE VARCHAR (100) NOT NULL, "
				+ "NO_OF_STACKS INT NOT NULL)");
		conn.commit();
		st.execute("CREATE TABLE FLASHCARDS (CARD_ID_PK INT PRIMARY KEY, "
				+ "PROJ_ID_FK INT CONSTRAINT PROJ_ID_FK_FL REFERENCES PROJECTS(PROJ_ID_PK), " + "STACK INT NOT NULL,"
				+ "QUESTION VARCHAR (32672), " + "ANSWER VARCHAR(32672), " + "CUSTOM_WIDTH_Q INT, " + "CUSTOM_WIDTH_A INT)");
		conn.commit();
		st.execute("CREATE TABLE LABELS (LABEL_ID_PK INT PRIMARY KEY, "
				+ "PROJ_ID_FK INT CONSTRAINT PROJ_ID_FK_LA REFERENCES PROJECTS(PROJ_ID_PK), " + "LABEL_NAME VARCHAR (100))");
		conn.commit();
		st.execute("CREATE TABLE LABELS_FLASHCARDS (LABELS_FLASHCARDS_ID_PK INT PRIMARY KEY, "
				+ "LABEL_ID_FK INT CONSTRAINT LABEL_ID_FK_LF REFERENCES LABELS(LABEL_ID_PK" + "), "
				+ "CARD_ID_FK INT CONSTRAINT CARD_ID_FK_LF REFERENCES FLASHCARDS(CARD_ID_PK), "
				+ "UNIQUE(LABEL_ID_FK, CARD_ID_FK))");
		conn.commit();
		st.execute("CREATE TABLE MEDIAMAPPING_TMP (PROJ_ID_FK INT CONSTRAINT PROJ_ID_FK_MM REFERENCES PROJECTS(PROJ_ID_PK), "
				+ "CARD_ID_FK INT CONSTRAINT CARD_ID_FK_MM REFERENCES FLASHCARDS(CARD_ID_PK), OLD_CARD_ID INT, UNIQUE(PROJ_ID_FK, CARD_ID_FK))");
		conn.commit();
		st.execute("CREATE TABLE MEDIAS (MEDIA_ID INT PRIMARY KEY, CARD_ID_FK INT CONSTRAINT CARD_ID_FK_ME REFERENCES FLASHCARDS(CARD_ID_PK), PATH_TO_MEDIA VARCHAR(100))");
		conn.commit();
		st.close();
		System.out.println("done!");
	}

	public void transferProjectsTable(DatabaseConnector source) throws SQLException {
		System.out.print("Transferring projects......");
		Statement srcSt = source.conn.createStatement();
		Statement targetSt = conn.createStatement();
		srcSt.execute("SELECT * FROM PROJECTS");
		source.conn.commit();
		ResultSet srcRes = srcSt.getResultSet();
		while (srcRes.next()) {
			targetSt.execute("INSERT INTO PROJECTS (PROJ_ID_PK, PROJ_TITLE, NO_OF_STACKS)" + " VALUES (" + srcRes.getInt(1)
					+ ", '" + srcRes.getString(2) + "', " + srcRes.getInt(3) + ")");
			conn.commit();
		}
		srcSt.close();
		targetSt.close();
		System.out.println("done!");
	}

	public void transferFlashcards(DatabaseConnector sourceConnector) throws SQLException {
		System.out.print("Transferring cards......");
		Statement srcSt = sourceConnector.conn.createStatement();
		Statement targetSt = conn.createStatement();
		ArrayList<Integer> projIds = getProjectIds(sourceConnector, srcSt);
		Iterator<Integer> it = projIds.iterator();
		int nextCardId = 1;
		while (it.hasNext()) {
			int projId = it.next();
			srcSt.execute("SELECT ID, STACK, QUESTION, ANSWER FROM PROJEKT_" + projId);
			sourceConnector.conn.commit();
			ResultSet srcResCards = srcSt.getResultSet();
			while (srcResCards.next()) {
				targetSt.execute("INSERT INTO FLASHCARDS(CARD_ID_PK, PROJ_ID_FK, STACK, QUESTION, ANSWER, "
						+ "CUSTOM_WIDTH_Q, CUSTOM_WIDTH_A) VALUES (" + nextCardId + ", " + projId + ", " + srcResCards.getInt(2)
						+ ", '" + srcResCards.getString(3) + "', '" + srcResCards.getString(4) + "',0 ,0 )");
				conn.commit();
				targetSt.execute("INSERT INTO MEDIAMAPPING_TMP (PROJ_ID_FK, CARD_ID_FK, OLD_CARD_ID) VALUES " + "(" + projId
						+ ", " + nextCardId + ", " + srcResCards.getInt(1) + ")");
				conn.commit();
				nextCardId++;
			}
		}
		System.out.println("done!");
	}

	private ArrayList<Integer> getProjectIds(DatabaseConnector sourceConnector, Statement srcSt) throws SQLException {
		srcSt.execute("SELECT ID FROM PROJECTS");
		sourceConnector.conn.commit();
		ResultSet srcRes = srcSt.getResultSet();
		ArrayList<Integer> projIds = new ArrayList<Integer>();
		while (srcRes.next()) {
			projIds.add(srcRes.getInt(1));
		}
		return projIds;
	}

	public void blobToPng(DatabaseConnector sourceConnector, String pathToMediaFolder) throws SQLException, IOException {
		Statement srcSt = sourceConnector.conn.createStatement();
		Statement targetSt = conn.createStatement();
		// fetch all project id's:
		ArrayList<Integer> projIds = getProjectIds(sourceConnector, srcSt); 
		int nextMediaId = 1;
		Iterator<Integer> it = projIds.iterator();
		while (it.hasNext()) {
			int nextProjectId = it.next();
			// get blobs: 
			srcSt.execute("SELECT ID, QUESTIONPIC, ANSWERPIC FROM PROJEKT_" + nextProjectId);
			sourceConnector.conn.commit();
			ResultSet res = srcSt.getResultSet();
			while (res.next()) {
				int oldCardId = res.getInt(1);
				int newCardId = getMapping(targetSt, nextProjectId, oldCardId);
				Blob qBlob = res.getBlob(2);
				Blob aBlob = res.getBlob(3);
				String qPathName = pathToMediaFolder + "/pic" + nextProjectId + "-" + newCardId + "q.png";
				String aPathName = pathToMediaFolder + "/pic" + nextProjectId + "-" + newCardId + "a.png";
				if (qBlob != null) {
					writeBlobToFile(qBlob, qPathName);
					saveMediaInDatabase(nextMediaId, newCardId, qPathName, targetSt);
					nextMediaId++;
				}
				if (aBlob != null) {
					writeBlobToFile(aBlob, aPathName);
					saveMediaInDatabase(nextMediaId, newCardId, aPathName, targetSt);
					nextMediaId++;
				}
			}
		}

	}

	private int getMapping(Statement targetSt, int nextProjectId, int oldCardId) throws SQLException {
		int newCardId = 0;
		targetSt.execute("SELECT CARD_ID_FK FROM MEDIAMAPPING_TMP WHERE  PROJ_ID_FK = " + nextProjectId + " AND OLD_CARD_ID = "
				+ oldCardId);
		conn.commit();
		ResultSet resMapping = targetSt.getResultSet();
		if (resMapping.next()) {
			newCardId = resMapping.getInt(1); // get mapping old id -
												// new id for file title
		}
		resMapping.close();
		return newCardId;
	}

	private void saveMediaInDatabase(int nextMediaId, int cardId, String pathName, Statement st) throws SQLException {
		st.execute("INSERT INTO MEDIAS (MEDIA_ID, CARD_ID_FK, PATH_TO_MEDIA) "
				+ "VALUES (" + nextMediaId + ", " + cardId + ",'" + pathName + "')");
		conn.commit();
	}

	private void writeBlobToFile(Blob blob, String pathName) throws FileNotFoundException, SQLException, IOException {
		File qFile = new File(pathName);
		OutputStream out = new FileOutputStream(qFile);
		byte[] buff = blob.getBytes(1, (int) blob.length());
		out.write(buff);
		out.close();
		blob.free();
	}

}
