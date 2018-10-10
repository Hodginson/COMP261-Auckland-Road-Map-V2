package Assignment_2;

public class Create_Restrictions {

	int StartNodeID;
	int EndNodeID;
	int nodeID;
	int StartRoadID;
	int EndRoadID;
	
	public Create_Restrictions(int fromNodeID, int toNodeID, int nodeID, int fromRoadID, int toRoadID) {
		this.StartNodeID = fromNodeID;
		this.EndNodeID = toNodeID;
		this.nodeID = nodeID;
		this.StartRoadID = fromRoadID;
		this.EndRoadID = toRoadID;
	}
	
	public Create_Restrictions(String line) {
		String[] l = line.split("\t");
		this.StartNodeID = Integer.parseInt(l[0]);
		this.StartRoadID = Integer.parseInt(l[1]);
		this.nodeID = Integer.parseInt(l[2]);
		this.EndRoadID = Integer.parseInt(l[3]);
		this.EndNodeID = Integer.parseInt(l[4]);
	}

	public int getStartNodeID() {
		return StartNodeID;
	}

	public int getEndNodeID() {
		return EndNodeID;
	}

	public int getNodeID() {
		return nodeID;
	}

	public int getStartRoadID() {
		return StartRoadID;
	}

	public int getEndRoadID() {
		return EndRoadID;
	}

	public void setStartNodeID(int fromNodeID) {
		this.StartNodeID = fromNodeID;
	}

	public void setEndNodeID(int toNodeID) {
		this.EndNodeID = toNodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void setStartRoadID(int fromRoadID) {
		this.StartRoadID = fromRoadID;
	}

	public void setEndRoadID(int toRoadID) {
		this.EndRoadID = toRoadID;
	}
}
