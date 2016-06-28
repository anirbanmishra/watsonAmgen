package databaseConnectivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Statement;
public class database
{
	Connection conn;
	ArrayList<String> category;
	ArrayList<String> subCategory;
	public database() throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = null;
		conn = DriverManager.getConnection("jdbc:mysql://localhost/issues","root", "");
		category=new ArrayList<String>();
		subCategory=new ArrayList<String>();
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
		}
		catch(Exception e){
			System.out.print("Do not connect to DB - Error:"+e);
		}
		return category;
	}
	public ArrayList<String> getSubCategory(String sub_Category){
		try{		
			subCategory.clear();
			String query = "SELECT * FROM "+sub_Category;
			Statement st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next())
			{
				subCategory.add(rs.getString("description"));
			}
		}
		catch(Exception e){
			System.out.print("Do not connect to DB - Error:"+e);
		}
		return subCategory;
	}
}