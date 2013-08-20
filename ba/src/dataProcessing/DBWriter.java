package dataProcessing;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class DBWriter {
	
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String PSW = "ultraclipse";
	private static final String USER = "root";
	

	//Define URL of database server for
	// database named similarity on the localhost
	// with the default port number 3306.
	private static final String URL = "jdbc:mysql://localhost:3306/similarity";
	private static final String PATH = "D:/Bachelorarbeit/Similarity/DBInput/";
	private	Statement stmt; 
	private Connection con;
	
//	private String composerName;
//	private List<Result<Long>> list;
//	private List<Result<LongContainer>> list2;
//
//	private ArrayList<String> compositions;
	
//	public DBWriter(List<Result<Long>> list, String composer, ArrayList<String> compositions) {
//		composerName = composer;
//		this.list = list;
//		this.compositions = compositions;
//
//	}
	
//	public DBWriter(List<Result<LongContainer>> list2, String currentComposer, 	ArrayList<String> compositions) {
//		composerName = currentComposer;
//		this.list2 = list2;
//		this.compositions = compositions;
//
//	}
	

	
	public void loadData() {
		connectToDB();
		try {
			
			stmt.executeUpdate(
				"LOAD DATA INFILE '" + PATH + "ngrams.csv' REPLACE INTO TABLE ngrams" + 
				" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
			
			stmt.executeUpdate(
					"LOAD DATA INFILE '" + PATH + "composer.csv' INTO TABLE composers" + 
					" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
			
			stmt.executeUpdate(
					"LOAD DATA INFILE '" + PATH + "compositions.csv' INTO TABLE compositions" + 
					" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
			
			stmt.executeUpdate(
					"LOAD DATA INFILE '" + PATH + "compcomp.csv' INTO TABLE compcomp" + 
					" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
			
			stmt.executeUpdate(
					"LOAD DATA INFILE '" + PATH + "results.csv' INTO TABLE results" + 
					" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnection();
	}

	private void connectToDB() {
		try {	
			 //Register the JDBC driver for MySQL.
			 Class.forName(DRIVER);
	
			//Get a connection to the database for a
			// user named root with the password.
			con = DriverManager.getConnection(URL,USER, PSW);
	
			//Display URL and connection information
			System.out.println("URL: " + URL);
			System.out.println("Connection: " + con);
	
			//Get a Statement object
			stmt = con.createStatement();
			 
		}catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private static final int LOWER_BOUND = 7;
//	private static final int UPPER_BOUND = 9;
//	public void loadData() {
//	connectToDB();
//	try {
//		stmt.executeUpdate(
//			"LOAD DATA INFILE 'E:/Daten/Bachelorarbeit/Similarity/DescPhrases/comps.csv' INTO TABLE composers" + 
//			" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
//		
//		stmt.executeUpdate(
//			"LOAD DATA INFILE 'E:/Daten/Bachelorarbeit/Similarity/DescPhrases/" + composerName + "descPhrases"+ LOWER_BOUND +"-" + UPPER_BOUND +".csv' INTO TABLE compositions" + 
//			" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'");
//		
//	} catch (SQLException e) {
//		e.printStackTrace();
//	}
//	
//	closeConnection();
//}
//	
//	public void writeDesPhrases() {
//		connectToDB();
//		updateTables(list);
//		closeConnection();
//	}
//
//	
//	private void updateTables(List<Result<Long>> list) {
//		try {
//
//	    	
//			for (Result<Long> nextNgram: list) {
//				String keys = "";
//				for (short key: NgramCoder.unpack(nextNgram.getNgram())) {
//					keys += " " + key;
//				}
//					
//				stmt.executeUpdate(
//					 "INSERT IGNORE INTO Compositions(song, ngram, " +
//					 "weight) VALUES('"+ compositions.get(nextNgram.getCompositionId()) +"','"+ keys +"',"+ nextNgram.getWeight() +")");
//				
//				
//				stmt.executeUpdate(
//					 "INSERT IGNORE INTO Composers(composer, song) " +
//					 "VALUES('"+ composerName + "','" + compositions.get(nextNgram.getCompositionId()) +"')");
//			}
//			
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

}
