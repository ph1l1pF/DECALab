package target.exercise1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class NoSQLInjection {
	public void doGet(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		try {
			Connection conn = DriverManager.getConnection("url", "userName", "password");
			PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM  User where userId=?");
			preparedStatement.setString(1, userId);
			preparedStatement.execute();
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}
	}
}
