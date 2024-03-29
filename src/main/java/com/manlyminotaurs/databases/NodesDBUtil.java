package com.manlyminotaurs.databases;

import com.manlyminotaurs.nodes.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//   __   __          ___          ___
//  |  \ |__)    |  |  |  | |    |  |  \ /
//  |__/ |__)    \__/  |  | |___ |  |   |
//
//              __   __   ___  __
//     __ |\ | /  \ |  \ |__  /__`
//        | \| \__/ |__/ |___ .__/
//

//update CSV file from room, exit, hallway, transport nodes.
//finish erd diagram and create request table
class NodesDBUtil {

	private int nodeIDGeneratorCount = 200;
	private int elevatorCounter = 0;
	private List<Node> nodes;
	private static Map<String, Node> nodeMap;

	/**
	 * Retrieve Map of Nodes using updateNodeMap
	 * @param allEntriesExist True to include deleted Nodes
	 * @return Map of nodeID's and Nodes
	 */
	Map<String, Node> getNodeMap(boolean allEntriesExist) {
		updateNodeMap(allEntriesExist);
		return nodeMap;
	}

	/**
	 * Get List of Nodes from local Map of Nodes
	 * @return List of Nodes
	 */
	List<Node> getNodeList(){
		updateNodeMap(false);
		List<Node> listOfNodes = new ArrayList(nodeMap.values());
		return listOfNodes;
	}
	/*---------------------------------------- Create java objects ---------------------------------------------------*/

	/**
	 * Initialize ArrayList of Nodes and HashMap of nodeID's and Nodes
	 */
	public NodesDBUtil() {
		nodes = new ArrayList<>();
		nodeMap  = new HashMap<>();
	}

	/**
	 * Close connection to database using jdbc
	 * @param connection the connection to terminate
	 */
	static void closeConnection(Connection connection) {
		try {
			if(connection != null) {
				connection.commit();
				connection.close();
			}
			else {
				System.out.println("Connection not established");
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	/**
	 * Creates a list of objects and stores them in the global variable nodeList
	 */
	@Deprecated
	List<Node> retrieveNodes() {
		// Connection
		nodes.clear();

		// Variables
		Node node = null;
		String ID = "";
		String nodeType = "";
		String longName = "";
		String shortName = "";
		int xCoord = 0;
		int yCoord = 0;
		int xCoord3D = 0;
		int yCoord3D = 0;
		String floor = "";
		String building = "";
		int status = 0;
		PreparedStatement stmt = null;
		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			String str = "SELECT * FROM MAP_NODES WHERE status = 1";
			stmt = connection.prepareStatement(str);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
 			while (rset.next()) {
				ID = rset.getString("nodeID");
				nodeType = rset.getString("nodeType");
				floor = rset.getString("floor");
				building = rset.getString("building");
				xCoord = rset.getInt("xCoord");
				yCoord = rset.getInt("yCoord");
				longName = rset.getString("longName");
				shortName = rset.getString("shortName");
				status = rset.getInt("status");
				xCoord3D = rset.getInt("xCoord3D");
				yCoord3D = rset.getInt("yCoord3D");

				// Create the java objects based on the node type
				node = buildNode(ID,xCoord, yCoord, floor, building, nodeType, longName, shortName, status, xCoord3D, yCoord3D, null);
				// Add the new node to the list
				nodes.add(node);
			}
			rset.close();
 			addAllEdges();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return nodes;
	} // retrieveNodes() ends

	/**
	 * Update local Node Map with Nodes from the database
	 * @param allEntriesExist True to include deleted Nodes
	 * @return Map of nodeID's and Nodes
	 */
	Map<String, Node> updateNodeMap(boolean allEntriesExist){
		// Variables
		Node node = null;
		String ID = "";
		String nodeType = "";
		String longName = "";
		String shortName = "";
		int xCoord = 0;
		int yCoord = 0;
		int xCoord3D = 0;
		int yCoord3D = 0;
		String floor = "";
		String building = "";
		int status = 0;
		LocalDateTime deleteTime = null;
		PreparedStatement stmt = null;
		Connection connection = null;
        nodeMap.clear();
		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			String str;
			if(allEntriesExist) {
				str = "SELECT * FROM MAP_NODES";
			}else{
				str = "SELECT * FROM MAP_NODES WHERE deleteTime IS NULL";
			}
			stmt = connection.prepareStatement(str);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
			while (rset.next()) {
				ID = rset.getString("nodeID");
				nodeType = rset.getString("nodeType");
				floor = rset.getString("floor");
				building = rset.getString("building");
				xCoord = rset.getInt("xCoord");
				yCoord = rset.getInt("yCoord");
				longName = rset.getString("longName");
				shortName = rset.getString("shortName");
				status = rset.getInt("status");
				xCoord3D = rset.getInt("xCoord3D");
				yCoord3D = rset.getInt("yCoord3D");
				if(rset.getTimestamp("deleteTime") != null) {
					deleteTime = rset.getTimestamp("deleteTime").toLocalDateTime();
				}else{
					deleteTime = null;
				}
				// Create the java objects based on the node type
				node = buildNode(ID,xCoord, yCoord, floor, building, nodeType, longName, shortName, status, xCoord3D, yCoord3D, deleteTime);
				// Add the new node to the list
				nodeMap.put(ID,node);
			}
			rset.close();
			connectNodes();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return nodeMap;
	} // retrieveNodeMap() ends

	@Deprecated
	 private void addAllEdges() {
		for(Node x: nodes) {
			List<String> nodeIDs = getAdjacentNodes(x);
			for(Node y: nodes) {
				if(nodeIDs.contains(y.getNodeID())) {
					if(!areNeighbors(x,y))
						x.addAdjacentNode(y);
					if(!areNeighbors(y,x))
						y.addAdjacentNode(x);
				}
			}
		}
	}

    /**
     * Connect Nodes in database
     */
	private void connectNodes() {
		for (Node xNode : nodeMap.values()){
			List<String> nodeIDs = getAdjacentNodes(xNode);
			for (Node yNode : nodeMap.values()){
				if(nodeIDs.contains(yNode.getNodeID())) {
					if(!areNeighbors(xNode,yNode))
						xNode.addAdjacentNode(yNode);
					if(!areNeighbors(yNode,xNode))
						yNode.addAdjacentNode(xNode);
				}
			}
		}
	}

    /**
     * Check if two Nodes are neighbors using getAdjacentNodes
     * @param start one of the two Nodes
     * @param end the other Node
     * @return True if end Node ID is in start Nodes List of adjacent Nodes
     */
	private boolean areNeighbors(Node start, Node end) {
		for(Node x: start.getAdjacentNodes()) {
			if(x.getNodeID().equals(end.getNodeID())) {
				return true;
			}
		}
		return false;
	}

	/*---------------------------------------- Add/edit/delete nodes -------------------------------------------------*/

	/**
	 * Adds the java object and the corresponding entry in the database table
     * @param nodeID unique ID
     * @param xCoord    xcoord
     * @param yCoord    ycoord
     * @param nodeType  node type
     * @param longName  long name of the node
     * @param shortName short name of the node
     * @param status active status of Node
     * @param yCoord3D  yCoord3D
     * @param xCoord3D  xCoord3D
     */
	Node addNode(String nodeID, int xCoord, int yCoord, String floor, String building, String nodeType, String longName, String shortName, int status, int yCoord3D, int xCoord3D) {

		Node aNode = buildNode(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status , xCoord3D, yCoord3D, null);

		Connection connection;
		connection = DataModelI.getInstance().getNewConnection();
		PreparedStatement statement = null;

		System.out.println("Node added to object list...");
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String str = "INSERT INTO map_nodes(nodeID,xCoord,yCoord,floor,building,nodeType,longName,shortName, status, xCoord3D, yCoord3D) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

			// Create the prepared statement
			statement = connection.prepareStatement(str);
			statement.setString(1, aNode.getNodeID());
			statement.setInt(2, aNode.getXCoord());
			statement.setInt(3, aNode.getYCoord());
			statement.setString(4, aNode.getFloor());
			statement.setString(5, aNode.getBuilding());
			statement.setString(6, aNode.getNodeType());
			statement.setString(7, aNode.getLongName());
			statement.setString(8, aNode.getShortName());
			statement.setInt(9,aNode.getStatus());
			statement.setInt(10, aNode.getXCoord3D());
			statement.setInt(11, aNode.getYCoord3D());
			statement.executeUpdate();
			nodeMap.put(aNode.getNodeID(),aNode);
		} catch (SQLException e) {
			System.out.println("Node already in the database");
		} finally {
            System.out.println("Node added to database");
			try {
				statement.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return aNode;
	} // end addNode()

	/**
	 * Add the Node given by the unique nodeID to the backup
	 * @param nodeID the ID of the Node to save
	 * @return Node that was backed up
	 */
	Node addNodeToBackup(String nodeID) {
		Node aNode = getNodeByID(nodeID);
		int xCoord = aNode.getXCoord();
		int yCoord = aNode.getYCoord();
		String floor = aNode.getFloor();
		String building = aNode.getBuilding();
		String nodeType = aNode.getNodeType();
		String longName = aNode.getLongName();
		String shortName = aNode.getShortName();
		int status = aNode.getStatus();
		int xCoord3D = aNode.getXCoord3D();
		int yCoord3D = aNode.getYCoord3D();

		Connection connection;
		connection = DataModelI.getInstance().getNewConnection();
		PreparedStatement statement = null;

		System.out.println("Node added to object list...");
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String str = "INSERT INTO BACKUP(nodeID,xCoord,yCoord,floor,building,nodeType,longName,shortName, status, xCoord3D, yCoord3D) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

			// Create the prepared statement
			statement = connection.prepareStatement(str);
			statement.setString(1, aNode.getNodeID());
			statement.setInt(2, aNode.getXCoord());
			statement.setInt(3, aNode.getYCoord());
			statement.setString(4, aNode.getFloor());
			statement.setString(5, aNode.getBuilding());
			statement.setString(6, aNode.getNodeType());
			statement.setString(7, aNode.getLongName());
			statement.setString(8, aNode.getShortName());
			statement.setInt(9,aNode.getStatus());
			statement.setInt(10, aNode.getXCoord3D());
			statement.setInt(11, aNode.getYCoord3D());
			statement.executeUpdate();
			nodeMap.put(aNode.getNodeID(),aNode);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Node added to database");
			try {
				statement.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return aNode;
	} // end addNode()


	/**
	 * Update the Node in the database with the matching nodeID
	 * @param node the updated Node
	 * @return True if successful
	 */
	boolean modifyNode(Node node) {
		boolean isSucessful = false;
		Connection connection = DataModelI.getInstance().getNewConnection();
		PreparedStatement statement = null;
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			connection = DataModelI.getInstance().getNewConnection();
			String str = "UPDATE map_nodes SET xCoord = ?,yCoord = ?,floor = ?,building = ?,nodeType = ?,longName = ?, shortName =?, status = ?, xCoord3D = ?, yCoord3D = ? WHERE nodeID = '" + node.getNodeID() +"'";

			// Create the prepared statement
			statement = connection.prepareStatement(str);
			statement.setInt(1, node.getXCoord());
			statement.setInt(2, node.getYCoord());
			statement.setString(3, node.getFloor());
			statement.setString(4, node.getBuilding());
			statement.setString(5, node.getNodeType());
			statement.setString(6, node.getLongName());
			statement.setString(7, node.getShortName());
            statement.setInt(8, node.getStatus());
			statement.setInt(9, node.getXCoord3D());
			statement.setInt(10, node.getYCoord3D());
			statement.executeUpdate();
			isSucessful = true;
			nodeMap.replace(node.getNodeID(),node);
		} catch (SQLException e) {
			System.out.println("Node already in the database");
		} finally {
            System.out.println("Node modified in database");
			try {
				statement.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return isSucessful;
		}
	}

	/**
	 * Soft remove Node from database
	 * @param nodeID the unique nodeID for the Node to be deleted
	 */
	boolean removeNode(String nodeID){
		boolean isSucessful = false;
		Connection connection = DataModelI.getInstance().getNewConnection();
		PreparedStatement statement = null;
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String str = "UPDATE map_nodes SET deleteTime = ? WHERE nodeID = ?";

			// Create the prepared statement
			statement = connection.prepareStatement(str);
			statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			statement.setString(2, nodeID);
			statement.executeUpdate();
			nodeMap.remove(nodeID);

			List<Edge> listOfEdges = getAdjacentEdges(nodeID);
			for(Edge aEdge:listOfEdges){
				DataModelI.getInstance().removeEdge(getNodeByID(aEdge.getStartNodeID()), getNodeByID(aEdge.getEndNodeID()));
				System.out.println("removing edges~~~~~~~~~~: "+aEdge.getStartNodeID() + "_" + aEdge.getEndNodeID());
			}
			System.out.println("remove Node~~~~~~~~~~~~:"+nodeID);
			isSucessful = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Node marked deleted");
			try {
				statement.close();
				DataModelI.getInstance().closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return isSucessful;
		}
	}

	/**
	 * Restore Node from the database
	 * @param nodeID the unique nodeID for the soft-deleted Node
     * @return True if successful
	 */
	boolean restoreNode(String nodeID){
		boolean isSucessful = false;
		Connection connection = DataModelI.getInstance().getNewConnection();
		PreparedStatement statement = null;
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String str = "UPDATE map_nodes SET deleteTime = NULL WHERE nodeID = ?";

			// Create the prepared statement
			statement = connection.prepareStatement(str);
			statement.setString(1, nodeID);
			statement.executeUpdate();
			isSucessful = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Node delete is unmarked");
			try {
				statement.close();
				DataModelI.getInstance().closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return isSucessful;
		}
	}


	/**
	 * Removes a node from the list of objects as well as the database
	 * @param node the Node to be removed
	 * @return True if successfully removed - False if it didn't exist
	 */
	boolean permanentlyRemoveNode(Node node) {
		boolean isSucessful = false;
		nodes.remove(node);
		// Remove from the database
		Connection connection = DataModelI.getInstance().getNewConnection();
		try {
			// Get connection to database and delete the node from the database
			Statement stmt = connection.createStatement();
			String str = "DELETE FROM MAP_NODES WHERE nodeID = '" + node.getNodeID() + "'";
			stmt.executeUpdate(str);
			stmt.close();
			System.out.println("Node removed from database");
			if(nodeMap.remove(node.getNodeID(),node)){
				isSucessful = true;
			}
			else{
				isSucessful = false;
				System.out.println("node not removed correctly!!!!");
				nodeMap.remove(node.getNodeID());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(connection);
		}
		return isSucessful;
	}

	/*---------------------------------------- Add/delete/edit "edges" -------------------------------------------------*/

	 /**
	 * Add Edge - a connected pair of Nodes - to database
	 * @param startNode one of the Nodes in the Edge
	 * @param endNode the other Node in the Edge
	 * @return	Edge object
	 */
	Edge addEdge(Node startNode, Node endNode) {
		Connection connection = DataModelI.getInstance().getNewConnection();
		nodeMap.get(startNode.getNodeID()).getAdjacentNodes().add(endNode);
		nodeMap.get(endNode.getNodeID()).getAdjacentNodes().add(startNode);
		Edge a_edge = new Edge(startNode.getNodeID(),endNode.getNodeID(),startNode.getNodeID() + "_" + endNode.getNodeID());
		System.out.println("Node added to adjacent node...");
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String str = "INSERT INTO MAP_EDGES(edgeID, startNodeID, endNodeID, status) VALUES (?,?,?,?)";

			// Create the prepared statement
			PreparedStatement statement = connection.prepareStatement(str);
			statement.setString(1, startNode.getNodeID() + "_" + endNode.getNodeID());
			statement.setString(2, startNode.getNodeID());
			statement.setString(3, endNode.getNodeID());
			statement.setInt(4, 1);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
            System.out.println("Edge added to database");
			closeConnection(connection);
		}
		return a_edge;
	} // end addEdge()

	/**
	 * Makes an Edge between nodes - the two Nodes are adjacent
	 * @param startNodeID one Node in the Edge
	 * @param endNodeID the other Node in the Edge
	 * @return Edge object
	 */
	private Edge makeEdge(String startNodeID, String endNodeID){
		Edge edge = null;
		int caseInt = startNodeID.compareTo(endNodeID);
		if(caseInt < 0){
			String edgeID = startNodeID + "_" + endNodeID;
			edge = new Edge(startNodeID, endNodeID, edgeID);
		}
		else if(caseInt == 0){
			System.out.println("you messed up");
			return null;
		}
		else if(caseInt > 0){
			String edgeID = endNodeID + "_" + startNodeID;
			edge = new Edge(endNodeID, startNodeID, edgeID);
			return null;
		}
		return edge;
	}

	/**
	 * Retrieve Edge by edgeID from database
	 * @param edgeID the edgeID to search for
	 * @return Edge object
	 */
	Edge getEdgeByID(String edgeID){

		Edge edge = null;
		int status = 0;
		String startNodeID = "";
		String endNodeID = "";
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			stmt = connection.createStatement();
			String str= "SELECT * FROM MAP_EDGES WHERE edgeID = '" + edgeID + "'";

			ResultSet rset = stmt.executeQuery(str);

			if(rset.next()) {
				edgeID = rset.getString("edgeID");
				startNodeID = rset.getString("startNodeID");
				endNodeID = rset.getString("endNodeID");
				status = rset.getInt("status");

				// Add the new edge to the list
				edge = new Edge(startNodeID, endNodeID, edgeID);
				edge.setStatus(status);
				//	System.out.println("Edge added to the list: " + edgeID);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return edge;
	}

	/**
	 * Retrieve list of Edges in database
	 * @param allEntriesExist True to include deleted Nodes
	 * @return List of Edges
	 */
	List<Edge> getEdgeList(boolean allEntriesExist){
		List<Edge> listOfEdges = new ArrayList<Edge>();
		/*
		for(Node a_node : nodeMap.values()) {
			for(Node b_node : a_node.getAdjacentNodes()) {
				//bug is that nodeID1_nodeID2 is getting stored twice in csv
				Edge a_edge = makeEdge(b_node.getNodeID(), a_node.getNodeID());
				if(a_edge != null) {
					edgeList.add(a_edge);
				}
			}
		}
		return edgeList;*/

		int status = 0;
		String startNodeID = "";
		String endNodeID = "";
		String edgeID = "";
		Connection connection = null;
		Statement stmt = null;
		LocalDateTime deleteTime = null;
		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			stmt = connection.createStatement();
			String str;
			if(allEntriesExist) {
				str = "SELECT * FROM MAP_EDGES";
			}else{
				str = "SELECT * FROM MAP_EDGES WHERE deleteTime IS NULL";
			}

			ResultSet rset = stmt.executeQuery(str);

			while(rset.next()) {
				edgeID = rset.getString("edgeID");
				startNodeID = rset.getString("startNodeID");
				endNodeID = rset.getString("endNodeID");
				status = rset.getInt("status");
				if(rset.getTimestamp("deleteTime") != null) {
					deleteTime = rset.getTimestamp("deleteTime").toLocalDateTime();
				}else{
					deleteTime = null;
				}
				// Add the new edge to the list
				Edge edge = new Edge(startNodeID, endNodeID, edgeID);
				edge.setStatus(status);
				edge.setDeleteTime(deleteTime);
				listOfEdges.add(edge);
			//	System.out.println("Edge added to the list: " + edgeID);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return listOfEdges;
	}

	/**
	 * Mark Edge as removed in database
	 * @param startNode one of the Nodes in the Edge
	 * @param endNode the other Node in the Edge
	 */
	void removeEdge(Node startNode, Node endNode){
		Connection connection = DataModelI.getInstance().getNewConnection();

		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();
			String str = "UPDATE MAP_EDGES SET deleteTime = ? WHERE edgeID = '"+ edgeID + "'";

			// Create the prepared statement
			PreparedStatement statement = connection.prepareStatement(str);
			statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			System.out.println("Node already in the database");
		} finally {
			System.out.println("Node modified");
			DataModelI.getInstance().closeConnection();
		}
	}
	/**
	 * Mark Edge as not-removed in database
	 * @param startNodeID nodeID of one of the Nodes in the Edge
	 * @param endNodeID nodeID the other Node in the Edge
	 * @return boolean corresponding to success
	 */
	boolean restoreEdge(String startNodeID, String endNodeID){
		Connection connection = DataModelI.getInstance().getNewConnection();

		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String edgeID = startNodeID + "_" + endNodeID;
			String str = "UPDATE MAP_EDGES SET deleteTime = NULL WHERE edgeID = ? ";

			// Create the prepared statement
			PreparedStatement statement = connection.prepareStatement(str);
			statement.setString(1, edgeID);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			System.out.println("Node already in the database");
		} finally {
			System.out.println("Node modified");
			DataModelI.getInstance().closeConnection();
		}
		return true;
	}

	/**
	 * Remove Edge from database
	 * @param startNode one of the Nodes in the Edge
	 * @param endNode the other Node in the Edge
	 */
	boolean permanentlyRemoveEdge(Node startNode, Node endNode) {
		// Find the node to remove from the edgeList
		nodeMap.get(startNode.getNodeID()).getAdjacentNodes().remove(endNode);
		nodeMap.get(endNode.getNodeID()).getAdjacentNodes().remove(startNode);
		Connection connection = DataModelI.getInstance().getNewConnection();
		try {
			// Get connection to database and delete the edge from the database
			Statement stmt = connection.createStatement();
			String str = "DELETE FROM MAP_EDGES WHERE edgeID = '" + startNode.getNodeID() + "_" + endNode.getNodeID() + "'";
			stmt.executeUpdate(str);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(connection);
		}
		return true;
	} // removeEdge

	/**
	 * Modify Edge in database
	 * @param startNode one of the Nodes in the Edge
	 * @param endNode the other Node in the Edge
	 * @param status the active status of the Edge
	 */
	void modifyEdge(Node startNode, Node endNode, int status){
		Connection connection = DataModelI.getInstance().getNewConnection();
		try {
			// Connect to the database
			System.out.println("Getting connection to database...");
			String edgeID = startNode.getNodeID() + "_" + endNode.getNodeID();
			String str = "UPDATE MAP_EDGES SET status = ? WHERE edgeID = '"+ edgeID + "'";

			// Create the prepared statement
			PreparedStatement statement = connection.prepareStatement(str);
			statement.setInt(1, status);

			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			System.out.println("Node already in the database");
		} finally {
            System.out.println("Node modified");
			DataModelI.getInstance().closeConnection();
		}
	}

	/*----------------------------------------- Helper functions -----------------------------------------------------*/

	/**
	 * Retrieve list of Nodes adjacent to given Node
	 * @param node the Node for which to check adjacent Nodes
	 * @return List of Strings
	 */
	List<String> getAdjacentNodes(Node node) {
		List<String> adjacentNodes = new ArrayList<>();
        // Connection
        Connection connection = null;
        // Variables
        int status = 0;
        Statement stmt = null;
        try {
                connection = DriverManager.getConnection("jdbc:derby:nodesDB");
                stmt = connection.createStatement();
                String str = "SELECT * FROM MAP_EDGES WHERE startNodeID = '"+ node.getNodeID()+ "' OR endNodeID = '" + node.getNodeID() +"'";
                ResultSet rset = stmt.executeQuery(str);

                while(rset.next()) {
                    String newNodeID;
                    String startNodeID = rset.getString("startNodeID");
                    String endNodeID = rset.getString("endNodeID");

                    if (node.getNodeID().equals(startNodeID)) {
                        newNodeID = endNodeID;
                    } else if(node.getNodeID().equals(startNodeID)){
                        newNodeID = startNodeID;
                    } else
					{
						newNodeID = null;
					}

					adjacentNodes.add(newNodeID);
                }
                rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
                closeConnection(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
		return adjacentNodes;
	}


	/**
	 * find all adjacent edges from the node object using sql query
	 * @param nodeID unique ID of Node object
	 * @return List of Edge objects
	 */
	private List<Edge> getAdjacentEdges(String nodeID) {
		List<Edge> listOfEdges = new ArrayList<Edge>();
		Connection connection = DataModelI.getInstance().getNewConnection();
		Edge edge;
		String edgeID;
		String startNodeID;
		String endNodeID;
		Statement stmt = null;

		try {
			stmt = connection.createStatement();
			String str = "SELECT * FROM MAP_EDGES WHERE STARTNODEID = '" + nodeID + "'" + "OR ENDNODEID = '" + nodeID + "'";
			ResultSet rset = stmt.executeQuery(str);

			// For every edge, get the information
			while (rset.next()) {
				edgeID = rset.getString("edgeID");
				startNodeID = rset.getString("startNodeID");
				endNodeID = rset.getString("endNodeID");

				// Add the new edge to the list
				edge = new Edge(startNodeID, endNodeID, edgeID);
				listOfEdges.add(edge);
				}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return listOfEdges;
	}

	//---------------------------------------Getter functions---------------------------------

	/**
	 * Retrieve list of buildings in database
	 * @return List of Nodes
	 */
	List<String> getBuildingsFromList() {
		List<String> buildings = new ArrayList<>();
		String building;

		Connection connection = DataModelI.getInstance().getNewConnection();
		try {
			Statement stmt = connection.createStatement();
			String str = "SELECT DISTINCT building FROM MAP_NODES";
			ResultSet rset = stmt.executeQuery(str);

			// For every node, get the information
			while (rset.next()) {
				building = rset.getString("building");
				buildings.add(building);
			}
			rset.close();
			stmt.close();
			System.out.println("Done adding buildings");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(connection);
		}
		return buildings;
	}

	/**
	 * Retrieve list of types in database
	 * @return List of Strings
	 */
	List<String> getTypesFromList() {
		List<String> types = new ArrayList<>();
		String type;

		Connection connection = DataModelI.getInstance().getNewConnection();
		try {
			Statement stmt = connection.createStatement();
			String str = "SELECT DISTINCT nodeType FROM MAP_NODES";
			ResultSet rset = stmt.executeQuery(str);

			// For every node, get the information
			while (rset.next()) {
				type = rset.getString("nodeType");
				types.add(type);
			}
			rset.close();
			stmt.close();
			System.out.println("Done adding types");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(connection);
		}
		return types;
	}

	@Deprecated
	List<Node> getNodesByBuildingTypeFloor (String nodeBuilding, String nodeType, String nodeFloor) {
		List<Node> selectedNodes = new ArrayList<>();
		// Variables
		Node node = null;
		String ID = "";
		String type = "";
		String longName = "";
		String shortName = "";
		int xCoord = 0;
		int yCoord = 0;
		int xCoord3D = 0;
		int yCoord3D = 0;
		String floor = "";
		String building = "";
		int status = 0;
		PreparedStatement stmt = null;
		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			String str = "SELECT * FROM MAP_NODES WHERE building = ? AND nodeType = ? AND floor = ?";
			stmt = connection.prepareStatement(str);
			stmt.setString(1, nodeBuilding);
			stmt.setString(2, nodeType);
			stmt.setString(3, nodeFloor);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
			while (rset.next()) {
				ID = rset.getString("nodeID");
				type = rset.getString("nodeType");
				floor = rset.getString("floor");
				building = rset.getString("building");
				xCoord = rset.getInt("xCoord");
				yCoord = rset.getInt("yCoord");
				longName = rset.getString("longName");
				shortName = rset.getString("shortName");
				status = rset.getInt("status");
				xCoord3D = rset.getInt("xCoord3D");
				yCoord3D = rset.getInt("yCoord3D");

				// Create the java objects based on the node type
				node = buildNode(ID,xCoord, yCoord, floor, building, type, longName, shortName, status, xCoord3D, yCoord3D, null);
				// Add the new node to the list
				selectedNodes.add(node);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return selectedNodes;
	}

	/**
	 * Retrieve list of longNames in database
	 * @return List of Strings
	 */
	List<String> getLongNames(){
		List<String> listOfLongNames = new ArrayList<>();

		PreparedStatement stmt = null;
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			String str = "SELECT longName FROM MAP_NODES";
			stmt = connection.prepareStatement(str);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
			while (rset.next()) {
				String longName = rset.getString("longName");
				listOfLongNames.add(longName);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return listOfLongNames;
	}

	/**
	 * Retrieve longNames of Nodes from database that match a given building, type, and floor
	 * @param nodeBuilding the Node building to match
	 * @param nodeType the Node type to match
	 * @param nodeFloor the Node floor to match
	 * @return List of Nodes with matching parameters
	 */
	List<String> getLongNameByBuildingTypeFloor (String nodeBuilding, String nodeType, String nodeFloor) {
		List<String> selectedNames = new ArrayList<>();
		PreparedStatement stmt = null;
		Connection connection = null;
		String longName;

		if(nodeBuilding==null){
			nodeBuilding = "";
		}
		if(nodeType == null){
			nodeType = "";
		}
		if(nodeFloor == null){
			nodeFloor = "";
		}

		//connection = DataModelI.getInstance().getNewConnection();
		try {
			connection = DriverManager.getConnection("jdbc:derby:nodesDB");
			String str = "SELECT longName FROM MAP_NODES WHERE building LIKE ? AND nodeType LIKE ? AND floor LIKE ?";
			stmt = connection.prepareStatement(str);
			stmt.setString(1, nodeBuilding);
			stmt.setString(2, nodeType);
			stmt.setString(3, nodeFloor);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
			while (rset.next()) {
				longName = rset.getString("longName");
				selectedNames.add(longName);
			}
			rset.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				//DataModelI.getInstance().closeConnection();
				closeConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return selectedNames;
	}

	@Deprecated
	public List<Node> getNodesByType(String type) {
		List<Node> selectedNodes = new ArrayList<>();
		List<Node> allNodes = retrieveNodes();

		for(Node a_node : allNodes){
			if(a_node.getNodeType().equals(type)){
				selectedNodes.add(a_node);
			}
		}
		return selectedNodes;
	}

	/**
	 * Retrieve Nodes from database that match a given floor
	 * @param floor the Node type to match
	 * @return List of Nodes with matching floor
	 */
	public List<Node> getNodesByFloor(String floor) {
		List<Node> selectedNodes = new ArrayList<>();

		for(Node a_node : nodeMap.values()){
			if(a_node.getFloor().equals(floor)){
				selectedNodes.add(a_node);
			}
		}
		return selectedNodes;
	}

	@Deprecated
	public List<Node> getNodesByBuilding(String building) {
		List<Node> selectedNodes = new ArrayList<>();
		List<Node> allNodes = retrieveNodes();

		for(Node a_node : allNodes){
			if(a_node.getBuilding().equals(building)){
				selectedNodes.add(a_node);
			}
		}
		return selectedNodes;
	}

	/**
	 * Query Node existence in database
	 * @param nodeID the ID of the node to check
	 * @return True if Node is found
	 */
    public boolean doesNodeExist(String nodeID) {
        return nodeMap.containsKey(nodeID);
    }


	/**
	 * builds and returns a node with given attributes
	 * @param nodeID the unique ID of the Node
	 * @param xCoord the x-coordinate on the 2D map
	 * @param yCoord the y-coordinate on the 2D map
	 * @param floor the floor level of the Node
	 * @param building the building of the Node
	 * @param nodeType the type of the Node
	 * @param longName the descriptive long name
	 * @param shortName the less-descriptive short name
	 * @param status the active status of the Node
	 * @param xCoord3D the x-coordinate on the 3D map
	 * @param yCoord3D the y-coordinate on the 3D map
	 * @param deleteTime the time the Node was marked as deleted
	 * @return Node object
	 */
    public Node buildNode(String nodeID, int xCoord, int yCoord, String floor, String building, String nodeType, String longName, String shortName, int status, int xCoord3D, int yCoord3D, LocalDateTime deleteTime){
        Node aNode;

	    if (nodeID.equals("") || nodeID.isEmpty()) {
	        nodeID = generateNodeID(nodeType, floor, "A");
	    }
        switch (nodeType){
            case "HALL":
                aNode = new Hallway(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, yCoord3D, xCoord3D);
            case "ELEV":
                aNode = new Transport(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, xCoord3D, yCoord3D);
            case "STAI":
                aNode = new Transport(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, xCoord3D, yCoord3D);
            case "EXIT":
                aNode = new Exit(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, yCoord3D, xCoord3D);
            default:
                aNode = new Room(nodeID, xCoord, yCoord, floor, building, nodeType, longName, shortName, status, yCoord3D, xCoord3D);
        }
        aNode.setDeleteTime(deleteTime);

	    return aNode;
    }


	/**
	 * return the node object that has the matching nodeID with the ID provided in the argument
	 * return null if it can't  find any
	 * @param nodeID the ID of the node to search for
	 * @return Node object
	 */
	public Node getNodeByID(String nodeID) {
		return nodeMap.get(nodeID);
	}

	@Deprecated
	public Node getNodeByIDFromList(String nodeID, List<Node> nodeList) {
		for(Node x: nodeList) {
			if (x.getNodeID().equals(nodeID)) {
				return x;
			}
		}
		return null;
	}

	/**
	 * Find nearest Node to given X and Y coordinates
	 * @param xCoord the X coordinate
	 * @param yCoord the Y coordinate
	 * @return Node object
	 */
	Node getNodeByCoords(int xCoord, int yCoord) {
		// Connection
		Connection connection = DataModelI.getInstance().getNewConnection();

		// Variables
		Node node = null;
		String nodeID = "";
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String str = "SELECT nodeID FROM MAP_NODES WHERE xCoord= " + xCoord + " yCoord = " +  yCoord;
			ResultSet rset = stmt.executeQuery(str);

			// For every node, get the information
			if (rset.next()) {
				nodeID = rset.getString("nodeID");
				node = nodeMap.get(nodeID);
			}
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataModelI.getInstance().closeConnection();
		}
		return node;
	}

	/**
	 * get the node that has non-unique long name which describes the node
	 * @param longName the longName of a Node
	 * @return Node object
	 */
	Node getNodeByLongName(String longName){
		// Connection
		Connection connection = DataModelI.getInstance().getNewConnection();

		// Variables
		Node node = null;
		String nodeID = "";
		PreparedStatement stmt = null;
		try {
			String str = "SELECT * FROM MAP_NODES WHERE longName = ?";

			stmt = connection.prepareStatement(str);
			stmt.setString(1, longName);
			ResultSet rset = stmt.executeQuery();

			// For every node, get the information
			if (rset.next()) {
				nodeID = rset.getString("nodeID");
				node = nodeMap.get(nodeID);
			}
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataModelI.getInstance().closeConnection();
		}
		return node;
	}

	/**
	 * Retrieve Nodes from database that match a given building, floor, and type
	 * @param building the building to filter by
	 * @param floor the floor to filter by
	 * @param type the type to filter by
	 * @return List of Strings
	 */
	public List<String> getNamesByBuildingFloorType(String building, String floor, String type){

		if(building == null || building == "None"){
			building = "";
		}
		if(floor == null || floor == "None"){
			floor = "";
		}
		if(type == null || type == "None"){
			type = "";
		}

		Node node = null;
		String aName;
		Statement stmt = null;
		List<String> listOfNames = new ArrayList<>();
		Connection connection = DataModelI.getInstance().getNewConnection();

		try {
			stmt = connection.createStatement();
			String str = "SELECT * FROM MAP_NODES WHERE building LIKE '" + building + "%' AND floor LIKE '"+ floor +"%' AND nodeType LIKE '" + type +"%' AND nodeType NOT LIKE 'HALL%'";
			ResultSet rset = stmt.executeQuery(str);

			// For every node, get the information
			while (rset.next()) {
				aName = rset.getString("longName");
				listOfNames.add(aName);
			}
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DataModelI.getInstance().closeConnection();
		}
		return listOfNames;
	}

	@Deprecated
	Node getNodeByLongNameFromList(String longName, List<Node> nodeList) {
	    for(Node x : nodeList) {
	        if(x.getLongName().equals(longName)) {
	            return x;
            }
        }
        return null;
    }

	/**
	 * Generate unique nodeID when adding a new node on the map
	 *
	 * @param nodeType the type of Node
	 * @param floor the floor of the Node
	 * @param elevatorLetter must not be null or empty string
	 * @return unique nodeID
	 */
	public String generateNodeID(String nodeType, String floor, String elevatorLetter) {
		String nodeID = "X" + nodeType;

		ArrayList<String> elevatorLetters = new ArrayList<>();
		elevatorLetters.add("A");
		elevatorLetters.add("B");
		elevatorLetters.add("C");
		elevatorLetters.add("D");
		elevatorLetters.add("E");
		elevatorLetters.add("F");
		elevatorLetters.add("G");
		elevatorLetters.add("H");

		if (nodeType.equals("ELEV")) {
			if (elevatorLetter == null || elevatorLetter.equals("")) {
				System.out.println("elevator exception happened!!!!!");
				return "ERROR";
			} else {
				nodeID = nodeID + "00" + elevatorLetters.get(elevatorCounter);
				elevatorCounter++;
			}
		} else {
			nodeID += Integer.toString(nodeIDGeneratorCount);
			nodeIDGeneratorCount++;
		}

		switch (floor){
            case "1":
                nodeID += "01";
                break;
            case "2":
                nodeID += "02";
                break;
            case "3":
                nodeID += "03";
                break;
            default:
                nodeID += floor;
                break;
        }
		return nodeID;
	}

} // end NodesDBUtil class