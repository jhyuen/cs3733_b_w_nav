package com.manlyminotaurs.databases;

import com.manlyminotaurs.messaging.Message;
import com.manlyminotaurs.messaging.Request;
import com.manlyminotaurs.nodes.Edge;
import com.manlyminotaurs.nodes.Node;
import com.manlyminotaurs.users.User;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

public interface IDataModel {

    void startDB();

    Connection getNewConnection();
    boolean closeConnection(Connection connection);

    //
    //                        _   _    ___                 _
    //                       | \ |_)    |  ._ _|_  _  ._ _|_ _.  _  _
    //                       |_/ |_)   _|_ | | |_ (/_ |   | (_| (_ (/_
    //

    /*------------------------------------------ Nodes --------------------------------------------------------------*/
    /*-------------------------------- Add / Modify / Remove Node ---------------------------------------------------*/
    boolean modifyNode(Node newNode);
    Node addNode(String nodeID, int xCoord, int yCoord, String floor, String building, String nodeType, String longName, String shortName, int xCoord3D, int yCoord3D);
    boolean removeNode(Node badNode);
    /*------------------------- Retrieve List of Nodes / All or by Attribute ----------------------------------------*/
    List<Node> retrieveNodes();
    Node getNodeByID(String ID);
    List<Node> getNodesByFloor(String floor);
    List<Node> getNodesByType(String type);
    List<Node> getNodesByBuilding(String building);
    List<Node> getNodesByBuildingTypeFloor(String building, String type, String floor);
    List<String> getBuildingsFromList();
    List<String> getTypesFromList();
    Node getNodeByCoords(int xCoord, int yCoord);
    Node getNodeByLongName(String longName);
    boolean doesNodeExist(String type);

    /*---------------------------------- Get AdjacentNodes / Edges --------------------------------------------------*/
    List<Node> getAdjacentNodesFromNode(Node node);
    Set<Edge> getEdgeList(List<Node> nodeList);

    /*----------------------------------------- Messages -------------------------------------------------------------*/
    /*------------------------------ Add / Modify / Remove Message ---------------------------------------------------*/
    Message addMessage(String message, Boolean isRead, String senderID, String receiverID);
    boolean removeMessage(Message oldMessage);
    boolean modifyMessage(Message newMessage);
    /*--------------------- Retrieve List of Messages / All or by Attribute ------------------------------------------*/
    List<Message> retrieveMessages();
    List<Message> getMessageBySender(String senderID);
    List<Message> getMessageByReceiver(String receiverID);
    Message getMessageByID(String ID);


    /*----------------------------------------- Requests ------------------------------------------------------------*/
    /*------------------------------ Add / Modify / Remove Request --------------------------------------------------*/
    Request addRequest(String requestType, int priority,  String nodeID, String message, String senderID);
    boolean removeRequest(Request oldRequest);
    boolean modifyRequest(Request newRequest);
    /*-------------------------- Retrieve List of Requests / All or by Attribute ------------------------------------*/
    List<Request> retrieveRequests();
    List<Request> getRequestBySender(String senderID);
    List<Request> getRequestByReceiver(String receiverID);
    Request getRequestByID(String ID);


    /*------------------------------------------ Users -------------------------------------------------------------*/
    /*-------------------------------- Add / Modify / Remove User --------------------------------------------------*/
    User addUser(String userID, String firstName, String middleName, String lastName, String language, String userType);
    boolean removeUser(User oldUser);
    boolean modifyUser(User newUser);
    /*------------------------ Retrieve List of Users / All or by Attribute ----------------------------------------*/
    List<User> retrieveUsers();
    User getUserByID(String ID);
}
