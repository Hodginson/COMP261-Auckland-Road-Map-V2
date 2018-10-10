package Assignment_2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.awt.*;


public class Create_Nodes {

	private int id;
	private Location location;
	private List<Create_Segments> segments = new ArrayList<Create_Segments>();
	private int NodeDepth;
	private List<Create_Segments> IncomingSegments = new ArrayList<Create_Segments>();
	private List<Create_Segments> OutgoingSegments = new ArrayList<Create_Segments>();
	
	public Create_Nodes(String line) {
		String[] l = line.split("\t");
		this.id = Integer.parseInt(l[0]);
		this.location = Location.newFromLatLon(Double.parseDouble(l[1]), Double.parseDouble(l[2]));
		}
	public void resetNodeDepth() {
		this.NodeDepth = Integer.MAX_VALUE;
	}

	public int getNodeDepth() {
		return NodeDepth;
	}

	public void setNodeDepth(int depth) {
		this.NodeDepth = depth;
	}

	public Create_Nodes(int id, Location location) {
		this.location = location;
		this.id = id;
	}

	// Gets the ID
	public int getId(){
		return id;
	}

	// Gets the Location
	public Location getLocation(){
		return this.location;
	}

	// Adds the segments
	public void addSegment(Create_Segments segment){
		segments.add(segment);
	}

	// Gets the Segments
	public List<Create_Segments> getSegments(){
		return segments;
	}
	// Add a segment in
	public void addSegmentIn(Create_Segments segment){
		IncomingSegments.add(segment);
	}
		
	// Add a segment out
	public void addSegmentOut(Create_Segments segment){
		OutgoingSegments.add(segment);
	}
	// Get Segments in
	public List<Create_Segments> getSegmentsIn(){
		return IncomingSegments;
	}
		
	// Get Segments out
	public List<Create_Segments> getSegmentsOut(){
		return OutgoingSegments;
	}
		
		
	public double DistanceToNode (Create_Nodes endNode) {
		double startX = this.location.x;
		double startY = this.location.y;
		double endX = endNode.location.x;
		double endY = endNode.location.y;
			return	Math.sqrt(Math.pow((startX-endX),2)+Math.pow((startY-endY),2));
	}
	
	public Map<Create_Nodes, Create_Segments> getAdjacentNodes(){
		Map<Create_Nodes, Create_Segments> adjacent = new HashMap<Create_Nodes, Create_Segments>();
		for (Create_Segments Segments : segments) {
			if (Segments.getRoad().isOneway()) { 
				if (Segments.getNodeEnd().getId() != this.getId()) { 
					adjacent.put(Segments.getNodeEnd(), Segments);
				}
			} else { 
				if (Segments.getNodeEnd().getId() != this.getId()) { 
					adjacent.put(Segments.getNodeEnd(), Segments);
				} else if (Segments.getNodeStart().getId() != this.getId()) { 
					adjacent.put(Segments.getNodeStart(), Segments);
				}
			}
		}
		return adjacent;
	}
	
	public Map<Create_Nodes, Create_Segments> getAllAdjacent(){
		Map<Create_Nodes, Create_Segments> adjacent = new HashMap<Create_Nodes, Create_Segments>();
		for (Create_Segments Segments : segments) {
			if (Segments.getNodeEnd().getId() != this.getId()) { 
				adjacent.put(Segments.getNodeEnd(), Segments);
			} else if (Segments.getNodeStart().getId() != this.getId()) { 
				adjacent.put(Segments.getNodeStart(), Segments);
			}
		}
		return adjacent;
	}
	
	// Draws the nodes
	public void draw(Graphics g, Location origin, double scale, int nodeSize) {
		Point p = this.location.asPoint(origin, scale);
		g.fillRect((int) p.getX()-nodeSize/2, (int) p.getY()-nodeSize/2, nodeSize, nodeSize);
			
	}
}
