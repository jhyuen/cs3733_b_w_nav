package com.manlyminotaurs.databases;

import com.manlyminotaurs.core.KioskInfo;
import com.manlyminotaurs.log.Log;
import com.manlyminotaurs.log.Pathfinder;
import com.manlyminotaurs.messaging.Message;
import com.manlyminotaurs.messaging.Request;
import com.manlyminotaurs.nodes.*;
import com.manlyminotaurs.users.StaffFields;
import com.manlyminotaurs.users.User;
import com.manlyminotaurs.users.UserPassword;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//
//  '||''|.             .              '||    ||'              '||          '||
//   ||   ||   ....   .||.   ....       |||  |||    ...      .. ||    ....   ||
//   ||    || '' .||   ||   '' .||      |'|..'||  .|  '|.  .'  '||  .|...||  ||
//   ||    || .|' ||   ||   .|' ||      | '|' ||  ||   ||  |.   ||  ||       ||
//  .||...|'  '|..'|'  '|.' '|..'|'    .|. | .||.  '|..|'  '|..'||.  '|...' .||.
//

public class DataModelI implements IDataModel{

    /*---------------------------------------------- Variables -------------------------------------------------------*/

    // all the utils
	private MessagesDBUtil messagesDBUtil;
	private NodesDBUtil nodesDBUtil;
	private RequestsDBUtil requestsDBUtil;
	private UserDBUtil userDBUtil;
	private TableInitializer tableInitializer;
	private UserSecurity userSecurity;
	private LogDBUtil logDBUtil;
	private PathfinderDBUtil pathfinderDBUtil;

    // list of all objects

    private static DataModelI dataModelI = null;
    private Connection connection = null;

    /*------------------------------------------------ Methods -------------------------------------------------------*/

    public static void main(String[] args){
         DataModelI.getInstance().startDB();
         DataModelI.getInstance().updateAllCSVFiles();

        TableInitializer tableInitializer = new TableInitializer();
    //    System.out.println(tableInitializer.convertStringToDate("2018-04-06"));
     //   System.out.println(tableInitializer.convertStringToTimestamp("2018-04-06 07:43:10:2").toLocalDateTime().toString().replace("T"," "));
    }

    private DataModelI() {
        messagesDBUtil = new MessagesDBUtil();
        nodesDBUtil = new NodesDBUtil();
        requestsDBUtil = new RequestsDBUtil();
        userDBUtil = new UserDBUtil();
        tableInitializer = new TableInitializer();
        userSecurity = new UserSecurity();
        logDBUtil = new LogDBUtil();
        pathfinderDBUtil = new PathfinderDBUtil();
    }

    public static DataModelI getInstance(){
       if(dataModelI == null) {
           dataModelI = new DataModelI();
       }
       return dataModelI;
    }

    @Override
    public void startDB() {
        tableInitializer.setupDatabase();
      // System.out.println(Timestamp.valueOf("0000-00-00 00:00:00").toLocalDateTime());
        //System.out.println(tableInitializer.convertStringToDate("12-04-2017"));
    }

    @Override
    public Connection getNewConnection() {
        try {
            if(DataModelI.getInstance().connection == null || DataModelI.getInstance().connection.isClosed()) {
                DataModelI.getInstance().connection = DriverManager.getConnection("jdbc:derby:nodesDB;create=true");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DataModelI.getInstance().connection;
    }

    @Override
    public boolean closeConnection() {
        try {
            if(DataModelI.getInstance().connection != null) {
                DataModelI.getInstance().connection.close();
                DataModelI.getInstance().connection = null;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException en){
            en.printStackTrace();
            return false;
        }
    }

	/*------------------------------------------------ Nodes -------------------------------------------------------*/
    @Override
    @Deprecated
    public List<Node> retrieveNodes() {
        return nodesDBUtil.getNodeList();
    }

    public Map<String, Node> getNodeMap(){
        return nodesDBUtil.getNodeMap(false);
    }

    @Override
    public List<Node> getNodeList() {
        return nodesDBUtil.getNodeList();
    }

    @Override
    public boolean modifyNode(Node newNode) {
        boolean tempBool =  nodesDBUtil.modifyNode(newNode);
        addLog("Modified "+ newNode.getNodeID()+" Node",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newNode.getNodeID(),"node");
        return tempBool;
    }

    @Override
    public Node addNode(String nodeID, int xCoord, int yCoord, String floor, String building, String nodeType, String longName, String shortName, int status, int xCoord3D, int yCoord3D) {
        Node tempNode =  nodesDBUtil.addNode(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, yCoord3D, xCoord3D);
        addLog("Added "+ tempNode.getNodeID()+" Node",LocalDateTime.now(), KioskInfo.getCurrentUserID(), tempNode.getNodeID(),"node");
        return tempNode;
    }

    @Override
    /**
     * Remove Node from database
     * @param Node the Node to be deleted
     */
    public boolean removeNode(Node badNode) {
        boolean tempBool = nodesDBUtil.removeNode(badNode.getNodeID());
        addLog("Removed "+ badNode.getNodeID()+" Node",LocalDateTime.now(), KioskInfo.getCurrentUserID(),badNode.getNodeID(),"node");
        return tempBool;
    }

    @Override
    @Deprecated
    /**
     * Retrieve Nodes from database that match a given type
     * @param type the Node type to match
     */
    public List<Node> getNodesByType(String type) {
        return nodesDBUtil.getNodesByType(type);
    }

    @Override
    /**
     * Query Node existence in database
     * @param nodeID the ID of the node to check
     */
    public boolean doesNodeExist(String nodeID) {
        return nodesDBUtil.doesNodeExist(nodeID);
    }

    @Override
    /**
     * Retrieve Nodes from database that match a given building, floor, and type
     * @param building the building to filter by
     * @param floor the floor to filter by
     * @param type the type to filter by
     */
    public List<String> getNamesByBuildingFloorType(String building, String floor, String type) {
        return nodesDBUtil.getNamesByBuildingFloorType(building, floor, type);
    }

    @Override
    /**
     * Find node in database by nodeID
     * @param nodeID the ID of the node to search for
     */
    public Node getNodeByID(String nodeID) {
        return nodesDBUtil.getNodeByID(nodeID);
    }

    @Override
    @Deprecated
	public Node getNodeByIDFromList(String nodeID, List<Node> nodeList) {
    	return nodesDBUtil.getNodeByIDFromList(nodeID, nodeList);
	}

    @Override
    @Deprecated
    public List<Node> getNodesByFloor(String floor) {
        return nodesDBUtil.getNodesByFloor(floor);
    }

    @Override
    @Deprecated
	public List<Node> getNodesByBuilding(String building) { return nodesDBUtil.getNodesByBuilding(building); }

    @Override
    /**
     * Retrieve list of buildings in database
     */
    public List<String> getBuildingsFromList() {
        return nodesDBUtil.getBuildingsFromList();
    }

    @Override
    /**
     * Retrieve list of types in database
     */
    public List<String> getTypesFromList() {
        return nodesDBUtil.getTypesFromList();
    }

    @Override
    /**
     * Find nearest Node to given X and Y coordinates
     * @param xCoord the X coordinate
     * @param yCoord the Y coordinate
     */
    public Node getNodeByCoords(int xCoord, int yCoord) {
        return nodesDBUtil.getNodeByCoords(xCoord, yCoord);
    }

    @Override
    /**
     * Retrieve Node by longName
     * @param longName the longName of a Node
     */
    public Node getNodeByLongName(String longName) {
        return nodesDBUtil.getNodeByLongName(longName);
    }

    @Override
    @Deprecated
    public Node getNodeByLongNameFromList(String longName, List<Node> nodeList) {
        return nodesDBUtil.getNodeByLongNameFromList(longName, nodeList);
    }

    @Override
    /**
     * Retrieve list of longNames in database
     */
    public List<String> getLongNames() {
        return nodesDBUtil.getLongNames();
    }

    @Override
    @Deprecated
    public List<Node> getNodesByBuildingTypeFloor (String building, String type, String floor) {
        return nodesDBUtil.getNodesByBuildingTypeFloor(building, type, floor);
    }

    @Override
    @Deprecated
    public List<String> getLongNameByBuildingTypeFloor(String building, String type, String floor) {
        return nodesDBUtil.getLongNameByBuildingTypeFloor(building,type,floor);
    }

    @Override
    /**
     * Retrieve list of Nodes adjacent to given Node
     * @param node the Node for which to check adjacent Nodes
     */
    public List<String> getAdjacentNodes(Node node) {
        return nodesDBUtil.getAdjacentNodes(node);
    }

    //-------------------------------------------Edges---------------------------------------------------

    @Override
    /**
     * Retrieve list of Edges in database
     */
    public List<Edge> getEdgeList() {
        return nodesDBUtil.getEdgeList(false);
    }

    @Override
    /**
     * Retrieve Edge by edgeID from database
     * @param edgeID the edgeID to search for
     */
    public Edge getEdgeByID(String edgeID) {
        return nodesDBUtil.getEdgeByID(edgeID);
    }

    @Override
    /**
     * Add Edge - a connected pair of Nodes - to database
     * @param startNode one of the Nodes in the Edge
     * @param endNode the other Node in the Edge
     */
    public Edge addEdge(Node startNode, Node endNode) {
        Edge tempEdge = nodesDBUtil.addEdge(startNode, endNode);
        addLog("Added "+ tempEdge.getEdgeID()+" Edge",LocalDateTime.now(), KioskInfo.getCurrentUserID(),tempEdge.getEdgeID(),"edge");
        return tempEdge;
    }

    @Override
    /**
     * Remove Edge from database
     * @param startNode one of the Nodes in the Edge
     * @param endNode the other Node in the Edge
     */
    public void removeEdge(Node startNode, Node endNode) {
        String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();
        nodesDBUtil.removeEdge(startNode, endNode);
        addLog("Removed "+ edgeID+" Edge",LocalDateTime.now(), KioskInfo.getCurrentUserID(), edgeID,"edge");
    }

    @Override
    /**
     * Modify Edge in database
     * @param startNode one of the Nodes in the Edge
     * @param endNode the other Node in the Edge
     * @param status the active status of the Edge
     */
    public void modifyEdge(Node startNode, Node endNode, int status) {
        String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();
        nodesDBUtil.modifyEdge(startNode, endNode, status);
        addLog("Modified "+ edgeID+" Edge",LocalDateTime.now(), KioskInfo.getCurrentUserID(),edgeID,"edge");
    }

    /*------------------------------------------------ Messages -------------------------------------------------------*/

    @Override
    /**
     * Add message to the database
     * @param messageObject the Message object to add to the database
     */
    public Message addMessage(Message messageObject) {
        Message tempMessage = messagesDBUtil.addMessage(messageObject);
        addLog("Added "+ messageObject.getMessageID()+" Message",LocalDateTime.now(), KioskInfo.getCurrentUserID(),messageObject.getMessageID(),"message");
        return tempMessage;
    }

    @Override
    /**
     * Remove message from the database
     * @param messageID the messageID of the Message object to be removed from the database
     */
    public boolean removeMessage(String messageID) {
        boolean tempBool = messagesDBUtil.removeMessage(messageID);
        addLog("Removed "+ messageID+" Message",LocalDateTime.now(), KioskInfo.getCurrentUserID(), messageID,"message");
        return tempBool;
    }

    @Override
    /**
     * Modify message in the database
     * @param newMessage the Message object to modify in the database
     */
    public boolean modifyMessage(Message newMessage) {
        boolean tempBool = messagesDBUtil.modifyMessage(newMessage);
        addLog("Modified "+ newMessage.getMessageID()+" Message",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newMessage.getMessageID(),"message");
        return tempBool;
    }

    @Override
    /**
     * Generate new messageID
     */
    public String getNextMessageID() {
        return messagesDBUtil.generateMessageID();
    }

    @Override
    /**
     * Retrieve list of all Message objects in database
     */
    public List<Message> retrieveMessages() {
        return messagesDBUtil.retrieveMessages(false);
    }

    @Override
    /**
     * Retrieve list of all Message objects in database filtered by senderID
     * @param senderID the String identifying the sender of a message
     */
    public List<Message> getMessageBySender(String senderID) {
        return messagesDBUtil.searchMessageBySender(senderID);
    }

    @Override
    /**
     * Retrieve list of all Message objects in database filtered by receiverID
     * @param recieverID the String identifying the receiver of a message
     */
    public List<Message> getMessageByReceiver(String receiverID) {
        return messagesDBUtil.searchMessageByReceiver(receiverID);
    }

    @Override
    /**
     * Retrieve Message object from database matching a given messageID
     * @param messageID the String identifying the unique ID of a message
     */
    public Message getMessageByID(String ID) {
        return messagesDBUtil.getMessageByID(ID);
    }


    /*------------------------------------------------ Requests -------------------------------------------------------*/
    @Override
    /**
     * Add Request object to database with Message object
     * @param requestObject the Request to add
     * @param messageObject the Message to add
     */
    public Request addRequest(Request requestObject, Message messageObject) {
        Request newRequest = requestsDBUtil.addRequest(requestObject, messageObject);
        addLog("Added "+ newRequest.getRequestID()+" Request",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newRequest.getRequestID(),"request");
        return newRequest;
    }

    @Override
    public Request addRequest(Request requestObject) {
        return requestsDBUtil.addRequest(requestObject);
    }

    @Override
    /**
     * Remove Request object from database by requestID
     * @param requestID the unique string identifier for Requests
     */
    public boolean removeRequest(String requestID) {
        boolean tempBool = requestsDBUtil.removeRequest(requestID);
        addLog("Removed "+ requestID +" Request",LocalDateTime.now(), KioskInfo.getCurrentUserID(),requestID,"request");
        return tempBool;
    }

    @Override
    /**
     * Modify Request object in database
     * @param newRequest the updated Request object
     */
    public boolean modifyRequest(Request newRequest) {
        boolean tempBool = requestsDBUtil.modifyRequest(newRequest);
        addLog("Modified "+ newRequest.getRequestID()+" Request",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newRequest.getRequestID(),"request");
        return tempBool;
    }

    @Override
    /**
     * Request new unique string identifier for Requests
     */
    public String getNextRequestID() {
        return requestsDBUtil.generateRequestID();
    }

    @Override
    /**
     * Retrieve list of all Request objects in database
     */
    public List<Request> retrieveRequests() {
        return requestsDBUtil.retrieveRequests(false);
    }

    @Override
    /**
     * Retrieve list of all Request objects in database filtered by senderID
     * @param senderID the unique string identifier for sender
     */
    public List<Request> getRequestBySender(String senderID) {
        return requestsDBUtil.searchRequestsBySender(senderID);
    }

    @Override
    /**
     * Retrieve list of all Request objects in database filtered by receiverID
     * @param receiverID the unique string identifier for receiver
     */
    public List<Request> getRequestByReceiver(String receiverID) {
        return requestsDBUtil.searchRequestsByReceiver(receiverID);
    }

    @Override
    /**
     * Retrieve Request object from database by requestID
     * @param requestID the unique string identifier for Request
     */
    public Request getRequestByID(String requestID) {
        return requestsDBUtil.getRequestByID(requestID);
    }

	/*------------------------------------------------ Users -------------------------------------------------------*/

    @Override
    public User addUser(String userID, String firstName, String middleName, String lastName, List<String> languages, String userType, String userName, String password) {
        User newUser = userDBUtil.addUser(userID, firstName, middleName, lastName, languages, userType, userName, password);
        addLog("Added "+ newUser.getUserID()+" User",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newUser.getUserID(),"user");
        return newUser;
    }

    @Override
    public void addUser(User userObject) {
        userDBUtil.addUser(userObject);
    }

    @Override
    public boolean removeUser(String userID) {
        boolean tempBool = userDBUtil.removeUser(userID);
        addLog("Removed "+ userID +" User",LocalDateTime.now(), KioskInfo.getCurrentUserID(), userID,"user");
        return tempBool;
    }

    @Override
    public boolean modifyUser(User newUser) {
        boolean tempBool = userDBUtil.modifyUser(newUser);
        addLog("Modified "+ newUser.getUserID()+" User",LocalDateTime.now(), KioskInfo.getCurrentUserID(),newUser.getUserID(),"user");
        return tempBool;
    }

    @Override
    public List<User> retrieveUsers() {
        return userDBUtil.retrieveUsers(false);
    }

    @Override
    public List<StaffFields> retrieveStaffs() {
        return userDBUtil.retrieveStaffs();
    }

    @Override
    public User getUserByID(String userID) {
        return userDBUtil.getUserByID(userID);
    }

    @Override
    public String getLanguageString(List<String> languages) {
        return userDBUtil.getLanguageString(languages);
    }

    @Override
    public List<String> getLanguageList(String languagesConcat) {
        return userDBUtil.getLanguageList(languagesConcat);
    }

    //-----------------------------------------------User Password---------------------------------------------------

    @Override
    public String getIDByUserPassword(String userName, String password) {
        UserSecurity userSecurity = new UserSecurity();
        return userSecurity.getIDByUserPassword(userName, password);
    }

    @Override
    public List<UserPassword> retrieveUserPasswords() {
        return userSecurity.retrieveUserPasswords(false);
    }

    @Override
    public void addUserPassword(String userName, String password, String userID) {
        userSecurity.addUserPassword(userName, password, userID);
        addLog("Added username and password for UserID: "+ userID +" ",LocalDateTime.now(), KioskInfo.getCurrentUserID(),userID,"userpassword");
    }


    @Override
    public boolean removeUserPassword(String userID) {
        boolean tempBool = userSecurity.removeUserPassword(userID);
        addLog("Removed username and password for UserID: "+ userID +" ",LocalDateTime.now(), KioskInfo.getCurrentUserID(),userID,"userpassword");
        return tempBool;
    }

    @Override
    public boolean doesUserPasswordExist(String userName, String password) {
        return userSecurity.doesUserPasswordExist(userName, password);
    }

    //---------------------------------------------------------------------------------------------------

    @Override
    public List<Log> retrieveLogData() {
        return logDBUtil.retrieveLogData();
    }

    @Override
    public Log addLog(String description, LocalDateTime logTime, String userID, String associatedID, String associatedType) {
        return logDBUtil.addLog(description, logTime, userID, associatedID, associatedType);
    }

    @Override
    public void addLog(Log newLog) {
        logDBUtil.addLog(newLog);
    }

    @Override
    public boolean removeLog(String logID) {
        return logDBUtil.removeLog(logID);
    }

    @Override
    public Log getLogByLogID(String logID) {
        return logDBUtil.getLogByLogID(logID);
    }

    @Override
    public List<Log> getLogsByUserID(String userID) {
        return logDBUtil.getLogsByUserID(userID);
    }

    @Override
    public List<Log> getLogsByAssociatedType(String associatedType) {
        return logDBUtil.getLogsByAssociatedType(associatedType);
    }

    @Override
    public List<Log> getLogsByLogTime(LocalDateTime startTime, LocalDateTime endTime) {
        return logDBUtil.getLogsByLogTime(startTime,endTime);
    }

    @Override
    public List<Log> getLogsByLogTimeChoice(String timeChoice) {
        return logDBUtil.getLogsByLogTimeChoice(timeChoice);
    }


    //----------------------------------Pathfinding Log-------------------------------------------

    public List<Pathfinder> retrievePathfinderData(){
        return pathfinderDBUtil.retrievePathfinderData();
    }
    public Pathfinder addPath(String startNodeID, String endNodeID){
        Pathfinder tempPath = pathfinderDBUtil.addPath(startNodeID, endNodeID);
        addLog("Pathfind from" + tempPath.getStartNodeID() +" to " + tempPath.getEndNodeID() + " is done",LocalDateTime.now(), KioskInfo.getCurrentUserID(), tempPath.getPathfinderID(),"pathfind");
        return tempPath;
    }
    public boolean removePath(Pathfinder pathfinder){
        boolean tempBool = pathfinderDBUtil.removePath(pathfinder);
        addLog("Pathfind from" + pathfinder.getStartNodeID() +" to " + pathfinder.getEndNodeID() + " is removed",LocalDateTime.now(), KioskInfo.getCurrentUserID(), pathfinder.getPathfinderID(),"pathfind");
        return tempBool;
    }
    public Pathfinder getPathByPathfinderID(String pathfinderID){
        return pathfinderDBUtil.getPathByPathfinderID(pathfinderID);
    }
    public List<Pathfinder> getPathByStartNodeID(String startNodeID){
        return pathfinderDBUtil.getPathByStartNodeID(startNodeID);
    }
    public List<Pathfinder> getPathByEndNodeID(String endNodeID){
        return pathfinderDBUtil.getPathByEndNodeID(endNodeID);
    }

    @Override
    public Timestamp convertStringToTimestamp(String timeString) {
        return tableInitializer.convertStringToTimestamp(timeString);
    }

    @Override
    public Date convertStringToDate(String timeString) {
        return tableInitializer.convertStringToDate(timeString);
    }



    //----------------------------------------Backup--------------------------------------------

    @Override
    public boolean permanentlyRemoveNode(Node badNode) {
        boolean tempBool = nodesDBUtil.permanentlyRemoveNode(badNode);
        addLog("Permanently Removed "+ badNode.getNodeID()+" Node",LocalDateTime.now(), KioskInfo.getCurrentUserID(),badNode.getNodeID(),"node");
        return tempBool;
    }

    @Override
    public boolean permanentlyRemoveEdge(Node startNode, Node endNode) {
        String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();
        addLog("Permanently Removed "+ edgeID+" Edge",LocalDateTime.now(), KioskInfo.getCurrentUserID(),edgeID,"edge");
        return nodesDBUtil.permanentlyRemoveEdge(startNode, endNode);
    }

    @Override
    public boolean permanentlyRemoveMessage(String messageID) {
        addLog("Permanently Removed "+ messageID+" Message",LocalDateTime.now(), KioskInfo.getCurrentUserID(),messageID,"message");
        return messagesDBUtil.permanentlyRemoveMessage(messageID);
    }

    @Override
    public boolean permanentlyRemoveRequest(Request oldRequest) {
        addLog("Permanently Removed "+ oldRequest.getRequestID()+" Request",LocalDateTime.now(), KioskInfo.getCurrentUserID(),oldRequest.getRequestID(),"request");
        return requestsDBUtil.permanentlyRemoveRequest(oldRequest);
    }

    @Override
    public boolean permanentlyRemoveUser(User oldUser) {
        addLog("Permanently Removed "+ oldUser.getUserID()+" User",LocalDateTime.now(), KioskInfo.getCurrentUserID(),oldUser.getUserID(),"user");
        return userDBUtil.permanentlyRemoveUser(oldUser);
    }

    @Override
    public boolean permanentlyRemoveUserPassword(String userID) {
        addLog("Permanently Removed "+ userID+" username and password",LocalDateTime.now(), KioskInfo.getCurrentUserID(),userID,"userpassword");
        return userSecurity.permanentlyRemoveUserPassword(userID);
    }

    @Override
    public boolean restoreNode(String nodeID) {
        addLog("Restored "+ nodeID+" Node",LocalDateTime.now(), KioskInfo.getCurrentUserID(), nodeID,"node");
        return nodesDBUtil.restoreNode(nodeID);
    }

    @Override
    public boolean restoreEdge(String startNodeID, String endNodeID) {
        String edgeID = startNodeID + "_"+ endNodeID;
        addLog("Restored "+ edgeID+" Edge",LocalDateTime.now(), KioskInfo.getCurrentUserID(), edgeID,"edge");
        return nodesDBUtil.restoreEdge(startNodeID, endNodeID);
    }

    @Override
    public boolean restoreMessage(String messageID) {
        addLog("Restored "+ messageID+" Message",LocalDateTime.now(), KioskInfo.getCurrentUserID(), messageID,"message");
        return messagesDBUtil.restoreMessage(messageID);
    }

    @Override
    public boolean restoreRequest(String requestID) {
        addLog("Restored "+ requestID+" Request",LocalDateTime.now(), KioskInfo.getCurrentUserID(), requestID,"request");
        return requestsDBUtil.restoreRequest(requestID);
    }

    @Override
    public boolean restoreUser(String userID) {
        addLog("Restored "+ userID+" User",LocalDateTime.now(), KioskInfo.getCurrentUserID(), userID,"user");
        return userDBUtil.restoreUser(userID);
    }

    @Override
    public boolean restoreUserPassword(String userID) {
        addLog("Restored "+ userID+" username and password",LocalDateTime.now(), KioskInfo.getCurrentUserID(), userID,"userpassword");
        return userSecurity.restoreUserPassword(userID);
    }

    //--------------------------------------CSV stuffs------------------------------------------
    @Override
    public void updateAllCSVFiles() {
        new CsvFileController().updateAllCSVFiles();
    }

}
