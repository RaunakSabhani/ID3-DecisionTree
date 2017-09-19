
public class Node {
	String header;
	Node left;
	Node right;
	int data;
	int depth;
	int dataClass;
	
	public Node(String header, int value)
	{
		this.header = header;
		this.data = value;
		left = null;
		right = null;
	}
	
	public Node(int value)
	{
		this.header = null;
		this.data = value;
	}
}
