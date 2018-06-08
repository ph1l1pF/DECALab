package target.exercise1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AssignmentSQLInjection{
	public void doGet(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		String alias = userId;
		try {
			Connection conn = DriverManager.getConnection("url", "userName", "password");
			Statement st = conn.createStatement();
			String query = "SELECT * FROM  User where userId='" + alias + "'";
			st.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}
}
