package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
      System.out.println("Created DBExchanger");
   }

   public void connect() throws SQLException {
      conn = DriverManager.getConnection(protocol + dbLocation + ";create=true");
      conn.setAutoCommit(false);
      if (conn != null) {
         System.out.println("Successfully created Connection to: " + protocol + dbLocation + ";create=true");
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
            System.out.println("Database did not shut down normally");
         } else {
            System.out.println("Database shut down normally");
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
            + "PROJ_ID_FK INT CONSTRAINT PROJ_ID_FK1 REFERENCES PROJECTS(PROJ_ID_PK), " + "STACK INT NOT NULL,"
            + "QUESTION VARCHAR (32672), " + "ANSWER VARCHAR(32672), " + "CUSTOM_WIDTH_Q INT, " + "CUSTOM_WIDTH_A INT)");
      conn.commit();
      st.execute("CREATE TABLE LABELS (LABEL_ID_PK INT PRIMARY KEY, "
            + "PROJ_ID_FK INT CONSTRAINT PROJ_ID_FK2 REFERENCES PROJECTS(PROJ_ID_PK), " + "LABEL_NAME VARCHAR (100))");
      conn.commit();
      st.execute("CREATE TABLE LABELS_FLASHCARDS (LABELS_FLASHCARDS_ID_PK INT PRIMARY KEY,"
            + "LABEL_ID_FK INT CONSTRAINT LABEL_ID_FK REFERENCES LABELS(LABEL_ID_PK), "
            + "CARD_ID_FK INT CONSTRAINT CARD_ID_FK REFERENCES FLASHCARDS(CARD_ID_PK), " + "UNIQUE(LABEL_ID_FK, CARD_ID_FK))");
      conn.commit();
      st.execute("CREATE TABLE MEDIAMAPPING_TMP (PROJ_ID_FK INT CONSTRAINT PROJ_ID_FKMEDIA REFERENCES PROJECTS(PROJ_ID_PK), "
            + "CARD_ID_FK INT CONSTRAINT CARD_ID_FK REFERENCES FLASHCARDS(CARD_ID_PK), OLD_CARD_ID INT, UNIQUE(PROJ_ID_FK, CARD_ID_FK))");
      conn.commit();
      st.execute("CREATE TABLE MEDIAS (MEDIA_ID INT PK, CARD_ID_FK INT CONSTRAINT CARD_ID_FK REFERENCES FLASHCARDS(CARD_ID_PK), PATH_TO_MEDIA VARCHAR(100))");
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
         targetSt.execute("INSERT INTO PROJECTS (PROJ_ID_PK, PROJ_TITLE, NO_OF_STACKS)" + " VALUES (" + srcRes.getInt(1) + ", '"
               + srcRes.getString(2) + "', " + srcRes.getInt(3) + ")");
         conn.commit();
      }
      srcSt.close();
      targetSt.close();
      System.out.println("done!");
   }

   public void transferFlashcards(DatabaseConnector source) throws SQLException {
      System.out.print("Transferring cards......");
      Statement srcSt = source.conn.createStatement();
      Statement targetSt = conn.createStatement();

      srcSt.execute("SELECT ID FROM PROJECTS");
      source.conn.commit();
      ResultSet srcRes = srcSt.getResultSet();
      ArrayList<Integer> projIds = new ArrayList<Integer>();
      while (srcRes.next()) {
         projIds.add(srcRes.getInt(1));
      }

      Iterator<Integer> it = projIds.iterator();
      int nextCardId = 1;
      while (it.hasNext()) {
         int projId = it.next();
         srcSt.execute("SELECT STACK, QUESTION, ANSWER FROM PROJEKT_" + projId);
         source.conn.commit();
         ResultSet srcResCards = srcSt.getResultSet();
         while (srcResCards.next()) {
            targetSt.execute("INSERT INTO FLASHCARDS(CARD_ID_PK, PROJ_ID_FK, STACK, QUESTION, ANSWER, "
                  + "CUSTOM_WIDTH_Q, CUSTOM_WIDTH_A) VALUES (" + nextCardId + ", " + projId + ", " + srcResCards.getInt(1)
                  + ", '" + srcResCards.getString(2) + "', '" + srcResCards.getString(3) +  "',0 ,0 )");
            conn.commit();
            nextCardId++;
         }
      }
      System.out.println("done!");
   }
   
   
   public void blobToPng(int projId, int oldCardId, int newCardId, String pathToMediaFolder) throws SQLException {
      Statement st = conn.createStatement();
      st.executeQuery("SELECT QUESTIONPIC FROM PROJEKT_" + projId + " WHERE ID = "  );
   }

}
