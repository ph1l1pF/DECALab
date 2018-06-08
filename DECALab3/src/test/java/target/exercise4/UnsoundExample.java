package target.exercise4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import target.exercise1.HttpServletRequest;

public class UnsoundExample{
	public void doGet(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		/*	Put more code here and generate a data-flow between userId and alias.
		 *  Only use statements of the form.
		 *  - Container1 x = new Container1();
		 *  - Container2 x = new Container2();
		 *  - x.taint = y;
		 *  - x.container = y;
		 *  - String y = x.taint;
		 *  - Container1 y = x.container;
		 *  (x and y are variables names that you may and must change)
		 * 	*/
		String alias = null; //Change null here to some variable that contains the content of userId.
		try {
			Connection conn = DriverManager.getConnection("url", "userName", "password");
			Statement st = conn.createStatement();
			String query = "SELECT * FROM  User where userId='" + alias + "'";
			st.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}
	
	public static class Container1{
		public String taint;
	}
	
	public static class Container2{
		public Container1 container;
	}
}
