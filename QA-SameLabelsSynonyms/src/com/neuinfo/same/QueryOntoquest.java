package com.neuinfo.same;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class QueryOntoquest {
	private static final Logger LOG = Logger.getLogger(QueryOntoquest.class.getSimpleName());
	// Example from http://www.mkyong.com/jdbc/jdbc-transaction-example/
	private static final String DB_DRIVER = "org.postgresql.Driver";
	private static final String DB_CONNECTION = "jdbc:postgresql://postgres-stage.neuinfo.org:5432/ontoquest";
	private static final String DB_USER = "ont2_ro";
	private static final String DB_PASSWORD = "nefR7xut";

	public static HashMap<String, List<String>> queryOQ() throws SQLException { 
		Connection dbConnection = null;
		Statement statementSelect = null;
		HashMap<String,List<String>> termsToReview = new HashMap<String,List<String>>();

		// Use to check for DUP Syns ONLY 
		String selectTableSQL = "SELECT nt.term||'---'||rtid||'---'||rid||'---'||tid as pk, nt.term, nt.tid, nt.synonyms " +
				"FROM nif_term as nt " +
				"WHERE nt.synonyms is not null ";  //+
				//"and nt.synonyms like '%SERPING%'";
				//"WHERE nt.synonyms like '%hippocampus%' " +
				//"LIMIT 50 ";	
		try {
			dbConnection = getDBConnection();
			dbConnection.setAutoCommit(false);
			statementSelect = dbConnection.createStatement();
			ResultSet rs = statementSelect.executeQuery(selectTableSQL);
			
			while (rs.next())
			{
				String pk = rs.getString(1); //get value by index position
				String term = rs.getString("term"); //get value by column name
				String tid = rs.getString("tid");
				String synonyms = rs.getString("synonyms"); //status_date is the name of a column in the table
				//LOG.info(tId+"\t"+rs.getString(2)+"\t"+statusDate);
				
				//Add values to List and then rows to HashMap
				List<String> values = new ArrayList<String>();
				values.add(term);
				values.add(tid);
				values.add(synonyms);
				//TODO Add another item to split on later when processing values
				values.add("XYZ");
				
				// Add to HashMap
				termsToReview.put(pk, values);
			} rs.close();
			statementSelect.close();
			//System.out.println("Done!");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			dbConnection.rollback();
		} finally {
			if (statementSelect != null) {
				statementSelect.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return termsToReview;
	}


	private static Connection getDBConnection() {
		Connection dbConnection = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}

		try {
			dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
					DB_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return dbConnection;
	}
	
}	

