package target.exercise1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InterproceduralSQLInjection {
	public void doGet(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		createQuery(userId);
	}

	private void createQuery(String parameter) {
		try {
			Connection conn = DriverManager.getConnection("url", "userName", "password");
			Statement st = conn.createStatement();
			String query = "SELECT * FROM  User where userId='" + parameter + "'";
			st.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}
}
