package core;

import java.sql.SQLException;
import db.DatabaseConnector;

public class StartApp {
   public static void main(String[] args) {
      try {
         DatabaseConnector dbex1 = new DatabaseConnector("C:\\Users\\IT-Helpline16\\Desktop\\TestDB");

         dbex1.connect();
         dbex1.createDBv2Tables();

         DatabaseConnector dbex2 = new DatabaseConnector("C:\\Users\\IT-Helpline16\\Desktop\\database_1");
         dbex2.connect();
         dbex1.transferProjectsTable(dbex2);
         dbex1.transferFlashcards(dbex2);

         dbex2.disconnect();
         dbex1.disconnect();
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
