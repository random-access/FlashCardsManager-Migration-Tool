package core;

import java.io.IOException;
import java.sql.SQLException;

import db.DatabaseConnector;

public class StartApp {
   public static void main(String[] args) {
      try {
         DatabaseConnector targetDbex = new DatabaseConnector("/home/moni/Desktop/TestDB");
         targetDbex.connect();
         DatabaseConnector srcDbex = new DatabaseConnector("/home/moni/Desktop/database_1");
         srcDbex.connect();
         
         targetDbex.createDBv2Tables();
         targetDbex.transferProjectsTable(srcDbex);
         targetDbex.transferFlashcards(srcDbex);
         targetDbex.blobToPng(srcDbex, "/home/moni/Desktop/TestDB-medias");
         srcDbex.disconnect();
         targetDbex.disconnect();
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
}
