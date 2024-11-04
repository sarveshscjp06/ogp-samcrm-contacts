/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sam.raagatech.ogp.samcrm.contacts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author sarve
 */
abstract class DatabaseConnection {

    private final String DB_USER = "ADMIN";
    private final String DB_PASSWORD = "Samriddhi12345";
    private final String DB_URL = "jdbc:oracle:thin:@(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.ap-mumbai-1.oraclecloud.com))(connect_data=(service_name=ge9bf8133738252_raagatechdb_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))";

    public final String dbName = "raagatechdb";
    public final String hostName = "localhost:3306";
    public final String dbUsername = "sarvesh";
    public final String dbPassword = "arjunarya";
    public final String dbUrl = "jdbc:mysql://" + hostName + "/" + dbName;

    protected final Connection createConnection() throws Exception {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        var ods = new OracleDataSource();
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);
        Connection connection = ods.getConnection();

        return connection;
    }
    //mysql
//    public Connection createConnection() throws Exception {
//        Connection connection = null;
//        try {
//            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//        } catch (SQLException e) {
//        }
//
//        return connection;
//    }

    protected final int generateNextPrimaryKey(String tableName, String columnName) throws Exception {

        int nextInt = 0;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String selectQuery = "select COALESCE(max(" + columnName + "), 0) + 1 as primary_key from " + tableName;
            ResultSet rs = statement.executeQuery(selectQuery);
            while (rs.next()) {
                nextInt = rs.getInt("primary_key");
            }
            rs.close();
            connection.close();
        } catch (SQLException sqle) {
            System.out.println(sqle);
        }
        return nextInt;
    }
}
