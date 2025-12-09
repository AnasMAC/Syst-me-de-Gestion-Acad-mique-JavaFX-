package com.example.gestionacademique.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    private static DBconnection instance;
    private Connection connection;
    private final String url="jdbc:postgresql://localhost:5432/testjava";
    private final String user="postgres";
    private final String password="anas946835";

    private DBconnection() throws SQLException {
        try {
            connection= DriverManager.getConnection(url,user,password);
            System.out.println("Connected to database successfully");
        }catch (SQLException e){
            System.out.println("Connection Failed");
            System.out.println(e.getMessage());
        }
    }

    public static DBconnection getInstance() throws SQLException {
        if(instance==null){
            instance= new DBconnection();
            return instance;
        }
        return instance;
    }
    public Connection getConnection(){
        return connection;
    }
}
