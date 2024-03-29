package com.manlyminotaurs.communications;

//for creating JSON object
import com.manlyminotaurs.databases.DataModelI;
import com.manlyminotaurs.databases.UserDBUtil;
import com.manlyminotaurs.log.Log;
import com.manlyminotaurs.messaging.Request;
import com.manlyminotaurs.messaging.RequestFactory;
import com.manlyminotaurs.messaging.RequestType;
import com.manlyminotaurs.nodes.Node;
import com.manlyminotaurs.users.User;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//for parsing Json Object

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JsonController {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss:S");
    //------------------------------------------------------------------------------------------------------
    //--------------------------Only two public methods for JSON Communication------------------------------
    //------------------------------------------------------------------------------------------------------

    /**
     * run json operation and attempt to receieve json request
     *
     * @param requestedJson takes in requested json object
     * @return returns null if parse exception
     */
    public String receieveRequestJson(String requestedJson) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(requestedJson);
            String requestedJsonType = (String) jsonObject.get("Type");
            String requestedJsonOperation = (String) jsonObject.get("Operation");

            switch(requestedJsonType){
                case "log":
                    if(requestedJsonOperation.equals("remove")){
                        removeLogJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("retrieve")){
                        retrieveJson(requestedJsonType);
                    }
                    break;

                case "user":
                    if(requestedJsonOperation.equals("add")){
                        addUserJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("remove")){
                        removeUserJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("modify")){
                        modifyUserJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("retrieve")){
                        retrieveJson(requestedJsonType);
                    }
                    break;

                case "request":
                    if(requestedJsonOperation.equals("add")){
                        addRequestJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("remove")){
                        removeRequestJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("modify")){
                        modifyRequestJson(jsonObject);
                    }
                    else if(requestedJsonOperation.equals("retrieve")){
                        retrieveJson(requestedJsonType);
                    }
                    break;

                case "node":
                    if(requestedJsonOperation.equals("retrieve")){
                        retrieveJson(requestedJsonType);
                    }
                    break;

                case "emergency":

                    break;
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

        /**
         * returns retrieved Data in Json String from Kiosk to Web app
         * @param requestedJson json object to request
         * @return Json format string
         */
        public String retrieveJson (String requestedJson){
        JSONObject requestedJsonObject = null;
        switch(requestedJson){
            case "Log":
                requestedJsonObject = retrieveLogJson();
                break;
            case "User":
                requestedJsonObject = retrieveUserJson();
                break;
            case "Request":
                requestedJsonObject = retrieveRequestJson();
                break;
            case "Node":
                requestedJsonObject = retrieveNodesJson();
                break;
        }
        return requestedJsonObject.toJSONString();

        }

        //------------------------------------------------------------------------------------------------------
        //---------------------------------------Log Starts-----------------------------------------------------
        //------------------------------------------------------------------------------------------------------

    /**
     *
     * removes json object from log
     *
     * @param jsonObject object to remove
     * @return true if function is successful
     */

   private boolean removeLogJson(JSONObject jsonObject){
        JSONParser parser = new JSONParser();
        boolean isSuccessful = false;

        JSONArray logData = (JSONArray) jsonObject.get("Data");
        Iterator<JSONObject> iterator = logData.iterator();
        if (iterator.hasNext()) {
            JSONObject jsonRow = iterator.next();
            String logID = jsonRow.get("logID").toString();
            DataModelI.getInstance().removeLog(logID);
            isSuccessful = true;
        }

        return isSuccessful;
    }

    /**
     * finds json object in the log
     *
     * @return json log object
     */
    private JSONObject retrieveLogJson(){
        JSONObject wholeObj = new JSONObject();
        JSONObject obj;
        JSONArray jsonLogArray = new JSONArray();

        List<Log> listOfLogs = DataModelI.getInstance().retrieveLogData();
        for(Log logRow : listOfLogs) {
            obj = new JSONObject();
            obj.put("logID", logRow.getLogID());
            obj.put("description", logRow.getDescription());
            obj.put("logTime", logRow.getLogTime().format(dateTimeFormatter));
            obj.put("userID", logRow.getUserID());
            obj.put("associatedID", logRow.getAssociatedID());
            obj.put("associatedType", logRow.getAssociatedType());
            jsonLogArray.add(obj);
        }

        wholeObj.put("Type", "log");
        wholeObj.put("Operation", "retrieve");
        wholeObj.put("Data", jsonLogArray);

        return wholeObj;
    }

    //------------------------------------------------------------------------------------------------------
    //---------------------------------------------Log Ends-------------------------------------------------
    //------------------------------------------------------------------------------------------------------


    //------------------------------------------------------------------------------------------------------
    //------------------------------------------Request Starts----------------------------------------------
    //------------------------------------------------------------------------------------------------------


    /**
     *  add json object request
     *
     * @param jsonObject jsonObject
     */
    private void addRequestJson(JSONObject jsonObject){
        JSONObject jsonRow  = (JSONObject) jsonObject.get("Data");

        String requestType = jsonRow.get("requestType").toString();
        String nodeID = jsonRow.get("nodeID").toString();
        String message = jsonRow.get("message").toString();
        String senderID = jsonRow.get("senderID").toString();
        String priority = jsonRow.get("priority").toString();
        Node aNode = DataModelI.getInstance().getNodeByID(nodeID);

        RequestFactory rFactory = new RequestFactory();
        rFactory.genNewRequest(RequestType.valueOf(requestType),aNode,message,senderID,Integer.parseInt(priority));
    }

    /**
     * remove the json object from the requests
     *
     * @param jsonObject json object to remove
     */
    private void removeRequestJson(JSONObject jsonObject){
        JSONObject jsonRow = (JSONObject) jsonObject.get("Data");
        String requestID = jsonRow.get("requestID").toString();
        DataModelI.getInstance().removeRequest(requestID);
    }

    /**
     *  modify current json object
     *
     * @param jsonObject json object to modify
     */
    private void modifyRequestJson(JSONObject jsonObject){
        JSONObject jsonRow  = (JSONObject) jsonObject.get("Data");

        String requestID = jsonRow.get("requestID").toString();
        String requestType = jsonRow.get("requestType").toString();
        String isComplete = jsonRow.get("isComplete").toString();
        String adminConfirm = jsonRow.get("adminConfirm").toString();
        String nodeID = jsonRow.get("nodeID").toString();
        String messageID = jsonRow.get("messageID").toString();
        String password = jsonRow.get("password").toString();

        Request newReq = DataModelI.getInstance().getRequestByID(requestID);
        newReq.setRequestType(requestType);
        newReq.setNodeID(nodeID);
        newReq.setMessageID(messageID);
        newReq.setComplete(Boolean.valueOf(isComplete));
        newReq.setAdminConfirm(Boolean.valueOf(adminConfirm));
        newReq.setNodeID(nodeID);
        newReq.setMessageID(messageID);
        newReq.setPassword(password);

        DataModelI.getInstance().modifyRequest(newReq);
    }

    /**
     * retrieves json request
     *
     * @return the proper json request
     */
    private JSONObject retrieveRequestJson(){
        JSONObject wholeObj = new JSONObject();
        JSONObject obj;
        JSONArray jsonRequestArray = new JSONArray();

        List<Request> listOfRequests = DataModelI.getInstance().retrieveRequests();
        for(Request requestRow : listOfRequests) {
            obj = new JSONObject();
            obj.put("requestID", requestRow.getRequestID());
            obj.put("requestType", requestRow.getRequestType());
            obj.put("priority", requestRow.getPriority());
            obj.put("isComplete", requestRow.getComplete());
            obj.put("adminConfirm", requestRow.getAdminConfirm());
            obj.put("startTime", requestRow.getStartTime().format(dateTimeFormatter));
            obj.put("endTime", requestRow.getEndTime().format(dateTimeFormatter));
            obj.put("nodeID", requestRow.getNodeID());
            obj.put("messageID", requestRow.getMessageID());
            obj.put("password", requestRow.getPassword());
            jsonRequestArray.add(obj);
        }

        wholeObj.put("Type", "request");
        wholeObj.put("Operation", "retrieve");
        wholeObj.put("Data", jsonRequestArray);

        return wholeObj;
    }

    //------------------------------------------------------------------------------------------------------
    //-------------------------------------------Request Ends-----------------------------------------------
    //------------------------------------------------------------------------------------------------------


    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------User Starts-----------------------------------------------
    //------------------------------------------------------------------------------------------------------

    /**
     * adds json user
     *
     * @param jsonObject json user to add
     */

    private void addUserJson(JSONObject jsonObject){
        JSONObject jsonRow = (JSONObject) jsonObject.get("Data");

        String userID = jsonRow.get("userID").toString();
        String firstName = jsonRow.get("firstName").toString();
        String middleName = jsonRow.get("middleName").toString();
        String lastName = jsonRow.get("lastName").toString();
        String language = jsonRow.get("language").toString();
        String userType = jsonRow.get("userType").toString();
        String userName = jsonRow.get("userName").toString();
        String userPassword = jsonRow.get("userPassword").toString();
        List<String> languages = new ArrayList<>();
        languages.add(language);
        DataModelI.getInstance().addUser(userID,firstName,middleName,lastName,languages,userType,userName,userPassword);
    }

    /**
     * removes specified json object
     *
     * @param jsonObject json user to remove
     */
    private void removeUserJson(JSONObject jsonObject){
        JSONObject jsonRow = (JSONObject) jsonObject.get("Data");
        String userID = jsonRow.get("userID").toString();
        DataModelI.getInstance().removeUser(userID);
    }

    /**
     *  modifies input json user
     *
     * @param jsonObject to modify
     */
    private void modifyUserJson(JSONObject jsonObject){
        JSONObject jsonRow = (JSONObject) jsonObject.get("Data");

        String userID = jsonRow.get("userID").toString();
        String firstName = jsonRow.get("firstName").toString();
        String middleName = jsonRow.get("middleName").toString();
        String lastName = jsonRow.get("lastName").toString();
        String language = jsonRow.get("language").toString();
        String userType = jsonRow.get("userType").toString();

        UserDBUtil userDBUtil = new UserDBUtil();
        List<String> languages = userDBUtil.getLanguageList(language);
        User aUser = DataModelI.getInstance().getUserByID(userID);

        aUser.setFirstName(firstName);
        aUser.setMiddleName(middleName);
        aUser.setLastName(lastName);
        aUser.setLanguages(languages);
        aUser.setUserType(userType);
        DataModelI.getInstance().modifyUser(aUser);
    }

    /**
     * gets a json user
     *
     * @return json user
     */
    private JSONObject retrieveUserJson(){
        JSONObject wholeObj = new JSONObject();
        JSONObject obj;
        JSONArray jsonUserArray = new JSONArray();

        List<User> listOfUsers = DataModelI.getInstance().retrieveUsers();
        for(User userRow : listOfUsers) {
            obj = new JSONObject();
            UserDBUtil userDBUtil = new UserDBUtil();
            obj.put("userID", userRow.getUserID());
            obj.put("firstName", userRow.getFirstName());
            obj.put("middleName", userRow.getMiddleName());
            obj.put("lastName", userRow.getLastName());
            obj.put("language", userDBUtil.getLanguageString(userRow.getLanguages()));
            obj.put("userType", userRow.getUserID());
            jsonUserArray.add(obj);
        }

        wholeObj.put("Type", "user");
        wholeObj.put("Operation", "retrieve");
        wholeObj.put("Data", jsonUserArray);

        return wholeObj;
    }

    //------------------------------------------------------------------------------------------------------
    //---------------------------------------------User Ends------------------------------------------------
    //------------------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------Nodes Start-----------------------------------------------
    //------------------------------------------------------------------------------------------------------

    /**
     * retrieve json nodes
     *
     * @return json node
     */

    private JSONObject retrieveNodesJson(){
        JSONObject wholeObj = new JSONObject();
        JSONObject obj;
        JSONArray jsonUserArray = new JSONArray();

        List<Node> listOfNodes = DataModelI.getInstance().getNodeList();
        for(Node aNode: listOfNodes) {
            obj = new JSONObject();
            obj.put("nodeID", aNode.getNodeID());
            obj.put("xCoord", aNode.getXCoord());
            obj.put("yCoord", aNode.getYCoord());
            obj.put("floor", aNode.getFloor());
            obj.put("building", aNode.getBuilding());
            obj.put("nodeType", aNode.getNodeType());
            obj.put("longName", aNode.getLongName());
            obj.put("shortName", aNode.getShortName());
            obj.put("status", aNode.getStatus());
            obj.put("xCoord3D", aNode.getXCoord3D());
            obj.put("yCoord3D", aNode.getYCoord3D());

            jsonUserArray.add(obj);
        }

        wholeObj.put("Type", "node");
        wholeObj.put("Operation", "retrieve");
        wholeObj.put("Data", jsonUserArray);

        return wholeObj;
    }

    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------Nodes End-------------------------------------------------
    //------------------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------------
    //----------------------------------------Emergency Start-----------------------------------------------
    //------------------------------------------------------------------------------------------------------

    /**
     * creates an emergency
     *
     * @param emergencyType type of emergency
     */

    private void makeEmergencyJson(String emergencyType){

        JSONObject wholeObj = new JSONObject();
        wholeObj.put("Type","emergency");
        wholeObj.put("EmergencyType", emergencyType);
    }

    //------------------------------------------------------------------------------------------------------
    //-----------------------------------------Emergency End------------------------------------------------
    //------------------------------------------------------------------------------------------------------


    /**
     * main function
     * @param args n/a
     */
    public static void main(String[] args) {
        DataModelI.getInstance().startDB();

        JsonController jsonController = new JsonController();
        String retrievedString = jsonController.retrieveJson("Log");
        System.out.println(retrievedString);
        System.out.println("==================Sepeartor==================");
        retrievedString = jsonController.retrieveJson("Request");
        System.out.println(retrievedString);
        System.out.println("==================Sepeartor==================");
        retrievedString = jsonController.retrieveJson("User");
        System.out.println(retrievedString);
        System.out.println("==================Sepeartor==================");
        retrievedString = jsonController.retrieveJson("Node");
        System.out.println(retrievedString);
    }
}