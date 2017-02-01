package com.snowy;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappdevApplication {
        public static ComboPooledDataSource cpds;
	public static void main(String[] args) throws PropertyVetoException {
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass("com.mysql.cj.jdbc.Driver");
            cpds.setJdbcUrl("jdbc:mysql://localhost:3306/webappdev?useSSL=false");
            cpds.setUser("root");
            //cpds.setUser("webappdev");
            cpds.setPassword("nIpQ1LbTR3xod8MkxAfO");
            //cpds.setPassword("password");
            SpringApplication.run(WebappdevApplication.class, args);
	}
        public static Connection getConnection() throws SQLException{
            return cpds.getConnection();
        }
}
