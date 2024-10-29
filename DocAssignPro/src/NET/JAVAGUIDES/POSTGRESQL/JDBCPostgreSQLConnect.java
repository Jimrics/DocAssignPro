package NET.JAVAGUIDES.POSTGRESQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCPostgreSQLConnect {
//jdbc url
	// jdbc username
	// jdbc password
	private final String url = "jdbc:postgresql://localhost/test1";
	private final String user = "postgres";
	private final String password = "postgres";

private void connect() {
	try(Connection connection = DriverManager.getConnection(url, user, password)) {
	if(connection!=null) {
		System.out.println("Connected to database\n");
	}
	}

catch(SQLException e) {
	e.printStackTrace();
}
}

public static void main(String[] args) {
	JDBCPostgreSQLConnect sqlconnect = new JDBCPostgreSQLConnect();
	sqlconnect.connect();
}
}
