package com.manlyminotaurs.databases;

import java.sql.*;

public class UserSecurity {

    void addUserPassword(String userName, String password, String userID){
        Connection connection = DataModelI.getInstance().getNewConnection();
        try {
            String str = "INSERT INTO UserPassword(userName, password, userID) VALUES (?,?,?)";

            // Create the prepared statement
            PreparedStatement statement = connection.prepareStatement(str);
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, userID);
            System.out.println("Prepared statement created...");
            statement.executeUpdate();
            System.out.println("UserPassowrd added to database");
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }
    }

    String getIDByUserPassword(String userName, String password){
        Connection connection = DataModelI.getInstance().getNewConnection();
        String userID ="";
        try {
            String str = "SELECT * FROM UserPassword WHERE userName = ? AND password = ?";

            // Create the prepared statement
            PreparedStatement statement = connection.prepareStatement(str);
            statement.setString(1, userName);
            statement.setString(2, password);

            ResultSet rset = statement.executeQuery( );
            if (rset.next()) {
                userID = rset.getString("userID");
            }
            rset.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }
        return userID;
    }

    boolean doesUserPasswordExist(String userName, String password) {
        Connection connection = DataModelI.getInstance().getNewConnection();
        String userID ="";
        try {
            String str = "SELECT * FROM UserPassword WHERE userName = ? AND password = ?";

            // Create the prepared statement
            PreparedStatement statement = connection.prepareStatement(str);
            statement.setString(1, userName);
            statement.setString(2, password);

            ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                return true;
            }
            rset.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }
        return false;
    }


}
