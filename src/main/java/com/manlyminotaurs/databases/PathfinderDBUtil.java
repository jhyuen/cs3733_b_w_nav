package com.manlyminotaurs.databases;

import com.manlyminotaurs.log.Pathfinder;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PathfinderDBUtil {

    private static int pathFinderIDCounter = 0;

    /**
     * sets pathfinder id counter to 0
     * @param pathFinderIDCounter global var
     */
    public static void setPathFinderIDCounter(int pathFinderIDCounter) {
        PathfinderDBUtil.pathFinderIDCounter = pathFinderIDCounter;
    }

    /**
     * generates a pathfinder id
     * @return incremented pathfinder id
     */
    private String generatePathfinderID(){
        pathFinderIDCounter++;
        return Integer.toString(pathFinderIDCounter);
    }


    /**
     * retrieves pathfinding
     * @return list of pathfinder (edges)
     */
    public List<Pathfinder> retrievePathfinderData(){
        List<Pathfinder> listOfPath = new ArrayList<>();
        Connection connection = DataModelI.getInstance().getNewConnection();

        String pathfinderID = "";
        String startNodeID = "";
        String endNodeID = "";
        Pathfinder pathfinder = null;

        try {
            Statement stmt = connection.createStatement();
            String str = "SELECT * FROM PATHFINDER";
            ResultSet rset = stmt.executeQuery(str);

            while (rset.next()) {
                pathfinderID = rset.getString("pathfinderID");
                startNodeID = rset.getString("startNodeID");
                endNodeID = rset.getString("endNodeID");

                pathfinder = new Pathfinder(pathfinderID, startNodeID, endNodeID);
                listOfPath.add(pathfinder);

                System.out.println("path added to the list: " + pathfinderID);
            }
            rset.close();
            stmt.close();
            System.out.println("Done adding users");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }

        return listOfPath;
    }

    /**
     * adds a path to pathfind to
     * @param startNodeID id of start
     * @param endNodeID id of end
     * @return pathfinder (edge)
     */
    public Pathfinder addPath(String startNodeID, String endNodeID){

        String pathfinderID = generatePathfinderID();
        Pathfinder pathfinder = new Pathfinder(pathfinderID, startNodeID, endNodeID);

        Connection connection = DataModelI.getInstance().getNewConnection();
        try {
            String str = "INSERT INTO PATHFINDER(pathfinderID, startNodeID, endNodeID) VALUES (?,?,?)";

            // Create the prepared statement
            PreparedStatement statement = connection.prepareStatement(str);
            statement.setString(1, pathfinderID);
            statement.setString(2, startNodeID);
            statement.setString(3, endNodeID);
            System.out.println("Prepared statement created...");
            statement.executeUpdate();
            System.out.println("Log added to database");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return pathfinder;
    }

    /**
     * removes path from db
     * @param pathfinder path to remove
     * @return true if success
     */
    boolean removePath(Pathfinder pathfinder){
        boolean isSuccess = false;
        Connection connection = DataModelI.getInstance().getNewConnection();
        try {
            Statement stmt = connection.createStatement();
            String str = "DELETE FROM PATHFINDER WHERE pathfinderID = '" + pathfinder.getPathfinderID() + "'";
            stmt.executeUpdate(str);
            stmt.close();
            isSuccess = true;
            System.out.println("Pathfinder removed from database");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }
        return isSuccess;
    }

    /**
     * gets path by path id
     * @param pathfinderID id of path
     * @return path (edge)
     */
    Pathfinder getPathByPathfinderID(String pathfinderID){
        // Connection
        Connection connection = DataModelI.getInstance().getNewConnection();

        // Variables
        String startNodeID = "";
        String endNodeID = "";
        Pathfinder pathfinder = null;

        try {
            Statement stmt = connection.createStatement();
            String str = "SELECT * FROM PATHFINDER WHERE PATHFINDERID = '" + pathfinderID + "'";
            ResultSet rset = stmt.executeQuery(str);

            if(rset.next()) {
                startNodeID = rset.getString("startNodeID");
                endNodeID = rset.getString("endNodeID");

                pathfinder = new Pathfinder(pathfinderID, startNodeID, endNodeID);

                System.out.println("get path to the list: " + pathfinderID);
            }
            rset.close();
            stmt.close();
            System.out.println("Done adding users");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }

        return pathfinder;
    }

    /**
     * gets path connected to start node
     * @param startNodeID id of node
     * @return list of paths
     */
    List<Pathfinder> getPathByStartNodeID(String startNodeID){
        // Connection
        Connection connection = DataModelI.getInstance().getNewConnection();

        // Variables
        String pathfinderID = "";
        String endNodeID = "";
        Pathfinder pathfinder = null;
        List<Pathfinder> listOfPath = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            String str = "SELECT * FROM PATHFINDER WHERE STARTNODEID = '" + startNodeID + "'";
            ResultSet rset = stmt.executeQuery(str);

            while(rset.next()) {
                pathfinderID = rset.getString("pathfinderID");
                endNodeID = rset.getString("endNodeID");

                pathfinder = new Pathfinder(pathfinderID, startNodeID, endNodeID);
                listOfPath.add(pathfinder);

                System.out.println("get path to the list: " + pathfinderID);
            }
            rset.close();
            stmt.close();
            System.out.println("Done adding users");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }

        return listOfPath;
    }

    /**
     * gets path based on end node id
     * @param endNodeID id of end node
     * @return list of paths (edges)
     */
    List<Pathfinder> getPathByEndNodeID(String endNodeID){
        // Connection
        Connection connection = DataModelI.getInstance().getNewConnection();

        // Variables
        String pathfinderID = "";
        String startNodeID = "";
        Pathfinder pathfinder = null;
        List<Pathfinder> listOfPath = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            String str = "SELECT * FROM PATHFINDER WHERE ENDNODEID = '" + endNodeID + "'";
            ResultSet rset = stmt.executeQuery(str);

            while(rset.next()) {
                pathfinderID = rset.getString("pathfinderID");
                startNodeID = rset.getString("startNodeID");

                pathfinder = new Pathfinder(pathfinderID, startNodeID, endNodeID);
                listOfPath.add(pathfinder);

                System.out.println("get path to the list: " + pathfinderID);
            }
            rset.close();
            stmt.close();
            System.out.println("Done adding users");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataModelI.getInstance().closeConnection();
        }

        return listOfPath;
    }
}
