package databaseConnectivity;

import java.util.ArrayList;
import java.sql.*;
import com.mysql.jdbc.Statement;
public class database
{
	Connection conn;
	ArrayList<String> category;
	public database() throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = null;
		conn = DriverManager.getConnection("jdbc:mysql://localhost/issues","root", "");
		category=new ArrayList<String>();
	}
	
	//Get categories from database.
	
	public ArrayList<String> getCategory(){
		try{			
			String query = "SELECT * FROM category";
			Statement st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next())
			{
				category.add(rs.getString("name"));
			}
			st.close();
			conn.close();
		}
		catch(Exception e){
			System.out.print("Do not connect to DB - Error:"+e);
		}
		return category;
	}
}