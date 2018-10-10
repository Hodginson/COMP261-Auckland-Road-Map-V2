package Assignment_2;

import java.util.ArrayDeque;

public class Articulation_Points {
	
	private Create_Nodes nodes;
	private int reachBack;
	private Articulation_Points parent;
	private int Pointdepth;
	private ArrayDeque<Create_Nodes> children;
		
	public Articulation_Points(Create_Nodes nodes, int reachBack, Articulation_Points parent) {
		this.nodes = nodes;
		this.reachBack = reachBack;
		this.Pointdepth = reachBack;
		this.parent = parent;
	}
	
	public int getPointdepth() {
		return Pointdepth;
	}
	
	public Create_Nodes getNode() {
		return nodes;
	}
	
	public int getReachBack() {
		return reachBack;
	}
	
	public Articulation_Points getParent() {
		return parent;
	}

	public void setPointdepth(int Pointdepth) {
		this.Pointdepth = Pointdepth;
	}
	
	public void setNodes(Create_Nodes nodes) {
		this.nodes = nodes;
	}
	
	public void setChildren(ArrayDeque<Create_Nodes> children){
		this.children = children;
	}
	
	public void setReachBack(int reachBack) {
		this.reachBack = reachBack;
	}
		
	public void setParent(Articulation_Points parent) {
		this.parent = parent;
	}
	
	public ArrayDeque<Create_Nodes> children(){
		return this.children;
	}	
}
