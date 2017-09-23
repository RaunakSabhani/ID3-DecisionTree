
public class Node {
	String header;
	Node left;
	Node right;
	int data;
	int depth;
	int dataClass;
	int label;
	
	public Node(String header, int value, int label)
	{
		this.header = header;
		this.data = value;
		this.label = label;
		left = null;
		right = null;
	}
	
	public Node(int value, int label)
	{
		this.header = null;
		this.data = value;
		this.label = label;
	}
}
