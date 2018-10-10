package Assignment_2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Map_Main extends GUI {
	
	
	Location origin;
	double scale = 30;
	double north = Double.NEGATIVE_INFINITY;
	double south = Double.POSITIVE_INFINITY;
	double east = Double.NEGATIVE_INFINITY;
	double west = Double.POSITIVE_INFINITY;
	
	Map<Integer, Create_Nodes> nodesMap = new HashMap<Integer, Create_Nodes>(); //a map to store the nodes
	Map<Integer, Create_Roads> roadsMap = new HashMap<Integer, Create_Roads>(); // a map to store the roads
	Set<Create_Polygons> polygonSet = new HashSet<Create_Polygons>();
	Map<Integer, PolygonColor> colours = new TreeMap<Integer,PolygonColor>();//the colour set for the polygons
	Map<Integer, Create_Restrictions> Restrictions = new HashMap<Integer, Create_Restrictions>();
	Set<String> BothRoadNameSet = new HashSet<String>();
	
	// for the trie tree method
	Trie_tree trieRoot = new Trie_tree();
	List<Create_Roads> SelectedRoads = new ArrayList<Create_Roads>();
	
	//for the quad tree
	Create_Nodes currentNode; 
	Quad_Tree quadRoot = null;
	
	//for A* search
	boolean find_Route = false;
	Create_Nodes StartingPoint;
	Create_Nodes FinishingPoint;
	int currentNodeSize = 10;
	ArrayList<Create_Nodes> currentNodes;
	List<Create_Segments> PathSegments;
	
	//for the Articulation Points
	Set<Create_Nodes> articulationPoints = new HashSet<Create_Nodes>();
	List<Create_Nodes> notVisited = new ArrayList<Create_Nodes>();
	Stack<Articulation_Points> articulationStack = new Stack<Articulation_Points>();
	
	public Map_Main() {	// defines the Color set for polygons
		colours.put(2, new PolygonColor(142, 145, 142)); //city - Light Grey
		colours.put(5, new PolygonColor(174, 40, 58)); //Car park - Reddish purple
		colours.put(7, new PolygonColor(66, 66, 66)); //Airport - Dark grey
		colours.put(8, new PolygonColor(226, 126, 4)); //Shopping Center - Orange
		colours.put(10, new PolygonColor(250, 255, 0)); //University - Yellow
		colours.put(11, new PolygonColor(173, 24, 24)); //Hospital - Red
		colours.put(14, new PolygonColor(206, 206, 202)); //Airport Runway - Grey
		colours.put(19, new PolygonColor(163, 108, 26)); //Man made area - Brown/Orange
		colours.put(22, new PolygonColor(79, 188, 78)); //National park - Green
		colours.put(23, new PolygonColor(9, 232, 42)); //city park - Green
		colours.put(24, new PolygonColor(0, 255, 4)); //golf course - Light Green
		colours.put(25, new PolygonColor(12, 209, 137)); //sport - turquoise 
		colours.put(26, new PolygonColor(125, 61, 165)); //Cemetery - Purple
		colours.put(30, new PolygonColor(0, 178, 38)); //state park - Green
		colours.put(40, new PolygonColor(65, 103, 226)); // OCEAN - Blue
		colours.put(60, new PolygonColor(7, 244, 189)); //lake baby blue
		colours.put(62, new PolygonColor(7, 244, 189)); //lake
		colours.put(64, new PolygonColor(65, 172, 226)); // RIVERS light blue
		colours.put(65, new PolygonColor(65, 172, 226)); // RIVERS
		colours.put(69, new PolygonColor(65, 172, 226)); // RIVERS
		colours.put(71, new PolygonColor(65, 172, 226)); // RIVERS
		colours.put(72, new PolygonColor(65, 172, 226)); // RIVERS
	    colours.put(80, new PolygonColor(0, 68, 1)); // Woods - dark green
	}
	
	protected void find_RoutePressed() {
		if (find_Route) {
			find_Route = false;
			currentNode = null;
			getTextOutputArea().setText("");
		} else {
			find_Route = true;
			currentNode = null;
			StartingPoint = null;
			FinishingPoint = null;
			getTextOutputArea().setText("Select the two nodes you want to find the route to");
		}
	}
	
	private AStarSearch findShortestRoute(Create_Nodes startNode, Create_Nodes endNode) {
		
		Set<AStarSearch> visitedSearch = new HashSet<AStarSearch>();
		Set<Create_Nodes> NodesVisited = new HashSet<Create_Nodes>();
		Queue<AStarSearch> fringe = new PriorityQueue<AStarSearch>();
		
		AStarSearch start = new AStarSearch(startNode, null, 0, startNode.DistanceToNode(endNode));
		fringe.add(start);

		while (fringe.size() > 0) { 
			AStarSearch searchNode = fringe.poll(); // dequeue the highest node, and process it if is not been visited
			if (searchNode.getNodes().equals(endNode)) { // if the node equals goal return the node
				return searchNode;
			}
			double costToHere = searchNode.getCurrentCost(); // calculates the current cost
			if (!visitedSearch.contains(searchNode)) { 
				visitedSearch.add(searchNode); // add the node to the visited set to indicate it has been visited 
			}
			if (searchNode.getNodes() != null) { 
				NodesVisited.add(searchNode.getNodes());
			}
			Map<Create_Nodes, Create_Segments> neighbours = searchNode.getNodes().getAdjacentNodes(); // add its neighbors to the fringe
			if (neighbours.size() > 0) {				
				for (Create_Nodes entryPoint : neighbours.keySet()) {// Check the restrictions
					Create_Restrictions restriction = Restrictions.get(entryPoint.getId());
					//check if the road is one way
					for (Create_Segments Segments : entryPoint.getSegments()) {						
						if (restriction != null) { // check if there are any restrictions that will cause doubling back
							if (restriction.getNodeID() == entryPoint.getId() 
									&& restriction.getStartNodeID() == searchNode.getNodes().getId() 
									&& restriction.getEndRoadID() == Segments.getRoad().getId()) { 
								NodesVisited.add(entryPoint);
								break;
							} 
						}
					}
				}
				// Iterate though nodes while taking into account all of the restrictions
				for (Create_Nodes entryPoint : neighbours.keySet()) {
					for (Create_Segments Segments : entryPoint.getSegments()) {
						if (Segments.getRoad().isOneway()) { 
							if (Segments.getNodeEnd().equals(entryPoint)) { 
								Double segmentLength = neighbours.get(entryPoint).getLength();
								Double estimateDistance = entryPoint.DistanceToNode(endNode);
								if (!NodesVisited.contains(entryPoint)) { 
									AStarSearch addNeighbour = new AStarSearch(entryPoint, searchNode, costToHere + segmentLength, costToHere + estimateDistance);
									fringe.add(addNeighbour);
								}
							}
						} else { // the road is not one way
							Double segmentLength = neighbours.get(entryPoint).getLength();
							Double estimateDistance = entryPoint.DistanceToNode(endNode);
							if (!NodesVisited.contains(entryPoint)) { 
								AStarSearch addNeighbour = new AStarSearch(entryPoint, searchNode, costToHere + segmentLength, costToHere + estimateDistance);
								fringe.add(addNeighbour);
							}
						}
					}
				}
			}
		}
		return null; 
	}

	protected void setOrigin() { //finds the centre point of the map
		double[] Map_Limits = this.setMap_Limits();
		scale = Math.min(400 / (Map_Limits[1] - Map_Limits[0]),
				400 / (Map_Limits[2] - Map_Limits[3]));
		origin = new Location(Map_Limits[0], Map_Limits[2]);
	}
	
	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons, File restrictions) {	
		this.loadData(nodes, roads, segments, polygons, restrictions);
		this.setOrigin();
		this.findArticulationPoints();
	}

	 //loads the data from the files, checks if polygons is null
	protected void loadData(File nodes, File roads, File segments, File polygons, File restrictions) {
		this.loadRoads(roads);
		this.loadNodes(nodes);
		this.loadSegments(segments);
		if (polygons != null) { 
		this.loadPolygons(polygons);
		}
		if (restrictions != null) { 
			this.loadRestrictions(polygons);
		}
	}

	 //load the nodes from the Node file, adds them to the Map
	protected void loadNodes(File nodes) {
			try {
				BufferedReader data = new BufferedReader(new FileReader(nodes));
				while (data.ready()) {
					String line = data.readLine(); 	// Read the data and creates a node
					if (line.length() > 0) {
						Create_Nodes node = new Create_Nodes(line); // Add the node to the node map
						nodesMap.put(node.getId(), node);
					}
				}
				// Generates a quad-tree structure for the nodes 
				double[] bounds = this.setMap_Limits();
				quadRoot = new Quad_Tree(new Location(bounds[0], bounds[2]), new Location(bounds[1], bounds[3]));
				// Add all the nodes to the quad-tree 
				for(Create_Nodes node : nodesMap.values()) {
					quadRoot.addNode(node);
				}
				data.close();
			} catch (IOException e) {
				System.out.printf("Failed to open file: %s, %s", e.getMessage(),
						e.getStackTrace());
			}
		}
		
	//loads the roads from text file, adds them to the Map
	protected void loadRoads(File roads) {
		try {
			BufferedReader data = new BufferedReader(new FileReader(roads));
			data.readLine();// skips the first line as it contains headers
			while (data.ready()) {
				String line = data.readLine(); // Read the data and creates a road
				Create_Roads road = new Create_Roads(line); //adds the road to the map
				roadsMap.put(road.getId(), road);
				trieRoot.addRoad(road);
			}
			data.close();
		} catch (IOException e) {
			System.out.printf("Failed to load: %s, %s", e.getMessage(),e.getStackTrace());//prints the path if it fails
		}
	}
	
	 //load all segments from text file and adds them to "nodeMap" & "roadMap"
	protected void loadSegments(File segments) {
		try {
			BufferedReader data = new BufferedReader(new FileReader(segments));
			data.readLine(); // skips the first line as it contains headers
			while (data.ready()) { 
				String line = data.readLine();
				Create_Segments segment = new Create_Segments(line, roadsMap, nodesMap);
				Create_Roads road = segment.getRoad();// If a road exists add a new segment
				if (road != null) {
					road.addSegment(segment);
				}
				Create_Nodes nodeStart = segment.getNodeStart(); // If a node exists then add a new segment
				if (nodeStart != null) {
					nodeStart.addSegment(segment);
				}
				Create_Nodes nodeEnd = segment.getNodeEnd();
				if (nodeEnd != null) {
					nodeEnd.addSegment(segment);
				}
			}
			data.close();
		} catch (IOException e) {
			System.out.printf("Failed to load: %s, %s", e.getMessage(),e.getStackTrace());
		}
	}
	
	 //load polygon file into memory
	protected void loadPolygons(File polygons){
		Integer polygonType =0;
		String label = "";
		Integer endLevel = 0;
		Integer cityIndex = 0;
		List<Location> coordinates = new ArrayList<Location>();
		Set<Integer> type = new TreeSet<Integer>();

		try {
			BufferedReader data = new BufferedReader(new FileReader(polygons));
			while (data.ready()){
				String line = data.readLine();
					if (line.startsWith("Type=")){
						polygonType = Integer.parseInt(line.substring(7),16);
						// Add polyType to a temp set, to check how many colors to use
						type.add(polygonType);
					} else if (line.startsWith("Label=")) {
						label = line.substring(6);
					} else if (line.startsWith("EndLevel=")) {
						endLevel = Integer.parseInt(line.substring(9));
					} else if (line.startsWith("CityIdx=")) {
						cityIndex = Integer.parseInt(line.substring(8));
					} else if (line.startsWith("Data0=")) {
						String strCoords = line.substring(6);
						coordinates.clear();
			
						String[] coordArray = strCoords.substring(1,strCoords.length()-2).split("\\),\\(",-1);// Splits the coordList String and separates them into X and Y
						for (int i=0;i<coordArray.length;i++){
							Double X = Double.parseDouble(coordArray[i].split(",")[0]);
							Double Y = Double.parseDouble(coordArray[i].split(",")[1]);
							coordinates.add(Location.newFromLatLon(X, Y));
						}
						Create_Polygons polyShape = new Create_Polygons(polygonType, endLevel, label, cityIndex, coordinates, colours.get(polygonType));
						polygonSet.add(polyShape);
					}
				}
			data.close();
		}
		catch (IOException e){
			System.out.printf("Failed to load %s, %s", e.getMessage(), e.getStackTrace());
		}
	}
	
	private void loadRestrictions(File restrictions) {
		try {
			BufferedReader data = new BufferedReader(new FileReader(restrictions));
			data.readLine();
			while (data.ready()) {
				String line = data.readLine();
				String[] l = line.split("\t");
				int nodeID = Integer.parseInt(l[2]);
				Create_Restrictions restriction = new Create_Restrictions(line);
				Restrictions.put(nodeID, restriction);
			}
			data.close();
		} catch (IOException e) {
			System.out.printf("filed to open file %s, %s", e.getMessage(), e.getStackTrace());
		}
	}
	
	// set the max and min location
	protected double[] setMap_Limits() {
		for (Create_Nodes nodes : nodesMap.values()) {
			if (nodes.getLocation().y > north)
				north = nodes.getLocation().y;
			if (nodes.getLocation().x > east)
				east = nodes.getLocation().x;
			if (nodes.getLocation().y < south)
				south = nodes.getLocation().y;
			if (nodes.getLocation().x < west)
				west = nodes.getLocation().x;			
		}
		return new double[] { west, east, north, south };
	}
	
	@Override
	protected void onMove(Move m) { //called when a button on the gui is pressed
		double zoomFactor = 1.5;
		double factor = 100/scale;
		switch (m) {
		case NORTH: {
			origin = origin.moveBy(0, factor);
			this.redraw();
			break;
		}
		case SOUTH: {
			origin = origin.moveBy(0, -factor);
			this.redraw();
			break;
		}
		case EAST: {
			origin = origin.moveBy(factor, 0);
			this.redraw();
			break;
		}
		case WEST: {
			origin = origin.moveBy(-factor, 0);
			this.redraw();
			break;
		}
		case ZOOM_IN: {
			double NewOrigin = super.getDrawingAreaDimension().getHeight()
					/ scale * (zoomFactor - 1) / zoomFactor / 2;
			double a = scale * (zoomFactor - 1) / zoomFactor / 2;
			System.out.printf("Failed to load %s, %s", NewOrigin,a);
			origin = new Location(origin.x + NewOrigin, origin.y - NewOrigin);
			scale = scale * zoomFactor;
			this.redraw();
			break;
		}
		case ZOOM_OUT: {
			scale = scale / zoomFactor;
			double newOrigin = super.getDrawingAreaDimension().getHeight()
					/ scale * (zoomFactor - 1) / zoomFactor / 2;
			origin = new Location(origin.x - newOrigin, origin.y + newOrigin);
			this.redraw();
			break;
		}
		}
		return;
	}
	
	@Override
	protected void redraw(Graphics g) {
		this.draw(g, origin, scale,
				super.getDrawingAreaDimension().getHeight(), super
						.getDrawingAreaDimension().getWidth());
	}
	
	private void setStartingPoint(Create_Nodes node) {
		this.StartingPoint = node;
	}

	private void setFinishingPoint(Create_Nodes node) {
		this.FinishingPoint = node;
	}	 
	
	@Override
	protected void onClick(MouseEvent click) {
		// When the user clicks the coordinates are found
		if (click.getButton() == 1) {
			int x = click.getPoint().x;
			int y = click.getPoint().y;
			Location clickedCoordinates = Location.newFromPoint(new Point(x, y), this.origin, this.scale);
			// Find quad tree in area then query the children for intersections
			Quad_Tree childNode = quadRoot.getCoordinates(clickedCoordinates.x, clickedCoordinates.y);
			for (Create_Nodes node : childNode.getNodeList()) {
				if (node.getLocation().isClose(clickedCoordinates, 0.08)) {//the margin of error for a click if the quad node will find it. initially had it too high so selecting nodes was tough as it would always auto select
					List<Create_Segments> pathsegmentList = node.getSegments();
					
					for (Create_Segments seg : pathsegmentList) {
						BothRoadNameSet.add(seg.getRoad().getName());
					}
					if (!find_Route) { 
					int nodeID = node.getId();
					String selectedNode = String.format("Intersection For: ",nodeID);
					getTextOutputArea().setText(selectedNode);
					List<Create_Segments> segmentList = node.getSegments();
					Set<String> RoadNameSet = new HashSet<String>();
					for (Create_Segments seg : segmentList) {
						RoadNameSet.add(seg.getRoad().getName());
					}
					for (String road : RoadNameSet) {
						getTextOutputArea().append(road + "  ");
						break;
					}
					currentNode = node;
				} else if (find_Route) { // find path mode is active
					if(!(StartingPoint == null) && !(FinishingPoint == null)){
						StartingPoint = null;
						FinishingPoint = null;
						}
					if (StartingPoint == null) {
						setStartingPoint(node);
						int startID = StartingPoint.getId();
						String startNode = String.format("From: ", startID);
						getTextOutputArea().setText(startNode);
						List<Create_Segments> segmentList = node.getSegments();
						Set<String> FirstRoadNameSet = new HashSet<String>();
						for (Create_Segments seg : segmentList) {
							FirstRoadNameSet.add(seg.getRoad().getName());
						}
						for (String road : FirstRoadNameSet) {
							getTextOutputArea().append(road + "  ");
							break;
						}
					} else if (StartingPoint != null && FinishingPoint == null) {
						setFinishingPoint(node);
						List<Create_Segments> segmentList = node.getSegments();
						Set<String> SecondRoadNameSet = new HashSet<String>();
						for (Create_Segments seg : segmentList) {
							SecondRoadNameSet.add(seg.getRoad().getName());
						}
						for (String road : SecondRoadNameSet) {
							getTextOutputArea().append("To: " + road + " \n");
						}
					} 
					if (StartingPoint != null && FinishingPoint != null) { // if nodes are active look up for shortest path
						currentNodes = new ArrayList<Create_Nodes>();
						currentNodes.add(StartingPoint);
						PathSegments = new ArrayList<Create_Segments>();
						AStarSearch endNode = findShortestRoute(StartingPoint, FinishingPoint);
						if (endNode != null) { // check if there is possible route
							currentNodes.add(StartingPoint);
							currentNodes.add(FinishingPoint);
							while (endNode.getFromNode() != null) {
								for (Create_Segments seg : endNode.getNodes().getSegments()) { // selects only the segments which are on the path
									if ((seg.getNodeEnd().getId() == endNode.getNodes().getId() || seg.getNodeStart().getId() == endNode.getNodes().getId())
											&& ((seg.getNodeStart().getId() == endNode.getFromNode().getNodes().getId()) || seg.getNodeEnd().getId() == endNode.getFromNode().getNodes()
													.getId())) {
										PathSegments.add(seg);
									}
								}
								currentNodes.add(endNode.getNodes());
								endNode = endNode.getFromNode();
								
							}
							Collections.reverse(currentNodes); // Reverse the node list ;-)
						}
					
					}				
				}
				}
			}
		}
	}

	@Override
	protected void onSearch() { //called when the search box has something entered or removed from it
		String RequiredRoad = getSearchBox().getText();
		String SearchString = "";
		Set<String> roadNames = new HashSet<String>();
		if (RequiredRoad.length() > 0) {
			Trie_tree node = trieRoot.FindRoad(RequiredRoad);
			if (node != null) {
				SelectedRoads = node.getRoads();
				for (Create_Roads road : SelectedRoads) {
					if (!roadNames.contains(road.getName())) {
						SearchString = SearchString + "\n" + road.getName();
						roadNames.add(road.getName());
					}
				}
				getTextOutputArea().setText(SearchString);
			} else
				SelectedRoads.clear();
		}
	}
	
	private void findArticulationPoints() {

		// copy all the unvisited nodes to a set
		for (Create_Nodes node : nodesMap.values()) {
			notVisited.add(node);
		}

		// creates a new empty set for the articulation points
		while (notVisited.size() > 0) {		
			int numberOfSubTrees = 0; //number of subtrees
			Create_Nodes startNode = null; //root node
			int count = 0; 			
			for (int i=0; i<notVisited.size(); i++ ) {
				notVisited.get(i).resetNodeDepth();
				if (count == 0) {
					startNode = notVisited.get(i);
				}
				count++;
			}

			// root case
			for (Create_Nodes node : startNode.getAllAdjacent().keySet()) {
				notVisited.remove(startNode); // removes the startNode from the unVisited list
				if (node.getNodeDepth() == Integer.MAX_VALUE) {
					ArticulationPoints(node, startNode);
					numberOfSubTrees++;
				}
			}
			if (numberOfSubTrees > 1) {
				articulationPoints.add(startNode);
			}
		}
		
	}

	private void ArticulationPoints(Create_Nodes firstNode, Create_Nodes root) {//the iteration method
		
		articulationStack.push(new Articulation_Points(firstNode, 1, new Articulation_Points(root, 0, null)));
		
		while (!articulationStack.isEmpty()) {
			Articulation_Points ArtPoints = articulationStack.peek(); // looks at previous node
			Create_Nodes node = ArtPoints.getNode();

			if (ArtPoints.children() == null) { 
				
				ArrayDeque<Create_Nodes> children = new ArrayDeque<Create_Nodes>();
				ArtPoints.setChildren(children);
				ArtPoints.setReachBack(ArtPoints.getPointdepth());
				node.setNodeDepth(ArtPoints.getPointdepth()); 			
				
				for (Create_Nodes neighbour : node.getAllAdjacent().keySet()) {
					if (neighbour != ArtPoints.getParent().getNode()) {
						children.offer(neighbour);
					}
				}
			
			} else if (!ArtPoints.children().isEmpty()) {
				Create_Nodes child = ArtPoints.children().poll();				
				if (child.getNodeDepth() < Integer.MAX_VALUE) {
					ArtPoints.setReachBack(Math.min(ArtPoints.getReachBack(), child.getNodeDepth()));
				} else {
					articulationStack.push(new Articulation_Points(child, node.getNodeDepth() + 1, ArtPoints));
				}
			} else {
				if (node != firstNode) {
					if (ArtPoints.getReachBack() >= ArtPoints.getParent().getPointdepth()) {
						articulationPoints.add(ArtPoints.getParent().getNode());
					}
					ArtPoints.getParent().setReachBack(Math.min(ArtPoints.getParent().getReachBack(), ArtPoints.getReachBack()));
				}
				notVisited.remove(articulationStack.pop().getNode());
			}
		}
	}

	protected void draw(Graphics g, Location origin, double scale, double screenH, double screenW) {
		
		
		this.origin = origin;
		this.scale = scale;
		int nodeSize = (int) Math.min(.2 * (scale), 7);
		if (nodesMap.size() > 0) {

			// Draw the polygons
			for (Create_Polygons polygons : polygonSet) {
				for (Integer entry : colours.keySet()) {
					polygons.draw(g, origin, scale, entry);
				}
			}
			// Draw the segments
			for (Create_Roads roads : roadsMap.values()) {
				if (SelectedRoads.contains(roads)) {
					g.setColor(Color.RED);
					for (Create_Segments segments : roads.getSegments()) {
						segments.draw(g, origin, scale, 1);
					}
				} else {
					g.setColor(Color.BLACK);
					for (Create_Segments segments : roads.getSegments()) {
						segments.draw(g, origin, scale, 1);
					}
				}
			}
			
			// Draw the nodes
			for (Create_Nodes nodes : nodesMap.values()) {
				if (!articulationPoints.contains(nodes)) { // check if node belongs to articulation points
					if (nodes == currentNode) {
						g.setColor(Color.RED);
					} else {
						g.setColor(Color.BLUE);
					}
					nodes.draw(g, origin, scale, nodeSize);
				}
			}
				

					if (find_Route && StartingPoint != null && FinishingPoint != null && currentNodes.size() > 0 && PathSegments.size() > 0) { // Draw start node if
						for (Create_Segments Segments : PathSegments) { // draw segments
							g.setColor(Color.green);
							Segments.draw(g, origin, scale, 1);
							}
						Set<String> roadNames = new HashSet<String>();
						Double totalDistance = 0.0;
						for (int i = 0; i < currentNodes.size() - 1; i++) {
							for (Create_Segments Segments : currentNodes.get(i).getSegments()) {
								if (Segments.getNodeEnd().equals(currentNodes.get(i + 1))) { // if it's an end node
									String roadName = Segments.getRoad().getName();
									double Distance = Segments.getLength();
									if (!roadNames.contains(roadName)) {
										getTextOutputArea().append(String.format("%s: %.2fkm %n", roadName, Distance));
										roadNames.add(roadName);
										totalDistance += Distance;
									}
								} else if (Segments.getNodeStart().equals(currentNodes.get(i + 1))) { // if it's a start node
									String roadName = Segments.getRoad().getName();
									double Distance = Segments.getLength();
									if (!roadNames.contains(roadName)) {
										getTextOutputArea().append(String.format("%s: %.2fkm %n", roadName, Distance));
										roadNames.add(roadName);
										totalDistance += Distance;
									}
								}
							}
						}
					getTextOutputArea().append(String.format("Total Distance: %.2fkm %n", totalDistance));
					//drawing nodes
					for (Create_Nodes node : currentNodes) { // draw nodes
						g.setColor(Color.yellow);
						node.draw(g, origin, scale, currentNodeSize);
					}
					}
				if (find_Route && StartingPoint != null) { // Draw start node if findPath is active
					StartingPoint.draw(g, origin, scale, currentNodeSize);
				}
				if (find_Route && FinishingPoint != null) { // Draw arrival node if findPath is active
					FinishingPoint.draw(g, origin, scale, currentNodeSize);	
				}
				//Draw articulation points
				for (Create_Nodes ArticulationNodes : articulationPoints) {
					if (ArticulationNodes == currentNode) {
						g.setColor(Color.RED);
					} else if(ArticulationNodes == StartingPoint || ArticulationNodes == FinishingPoint ) {
						g.setColor(Color.orange);
					}else{
						g.setColor(Color.cyan);
						}
					ArticulationNodes.draw(g, origin, scale, nodeSize + 3);
				}
				
		}
		
	}
	
	public static void main(String[] args) {
		new Map_Main();
	}
}

