package target.exercise2and3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import target.exercise1.HttpServletRequest;

public class FieldSQLInjection1 {
	public void doGet(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		ObjectWithTaint o = new ObjectWithTaint();
		o.userInput = userId;
		String loaded = o.userInput;
		try {
			Connection conn = DriverManager.getConnection("url", "userName", "password");
			Statement st = conn.createStatement();
			String query = "SELECT * FROM  User where userId='" + loaded + "'";
			st.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}

	private static class ObjectWithTaint{
		String userInput = "noUserInput";
	}
}
