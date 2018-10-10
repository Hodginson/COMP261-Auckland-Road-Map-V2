package Assignment_2;

public class AStarSearch implements Comparable<AStarSearch> {

	Create_Nodes nodes;
	AStarSearch StartNode;
	Create_Segments segments;
	double CurrentCost;
	double EstimateCost;

	public AStarSearch(Create_Nodes node, AStarSearch fromNode, double costSoFar, double estCostToGoal) {
		this.nodes = node;
		this.StartNode = fromNode;
		this.CurrentCost = costSoFar;
		this.EstimateCost = estCostToGoal;
	}

	public Create_Nodes getNodes() {
		return nodes;
	}

	public Create_Segments getSegments() {
		return segments;
	}

	public AStarSearch getFromNode() {
		return StartNode;
	}
	
	public double getCurrentCost() {
		return CurrentCost;
	}
	
	public double getEstimateCost() {
		return EstimateCost;
	}
	
	public void setNode(Create_Nodes node) {
		this.nodes = node;
	}

	public void setSegment(Create_Segments segment) {
		this.segments = segment;
	}

	public void setFromNode(AStarSearch fromNode) {
		this.StartNode = fromNode;
	}

	public void setCurrentCost(double costSoFar) {
		this.CurrentCost = costSoFar;
	}

	public void setEstimateCost(double estCostSoFar) {
		this.EstimateCost = estCostSoFar;
	}

	@Override
	public int compareTo(AStarSearch node) {
		if (EstimateCost < node.getEstimateCost()) {
			return -1;
		} else if (EstimateCost > node.getEstimateCost()) {
			return 1;
		}
		return 0;
	}
}
