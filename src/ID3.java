import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ID3 {

	private HashMap<Integer, String> header = new HashMap<Integer,String>();
	private HashMap<String, Integer> reverseHeader = new HashMap<String,Integer>();
	private int[][] data;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ID3 id3Algorithm = new ID3();
		int[][] data = id3Algorithm.readFile(args[0]);
		System.out.println("In main" + data[0][data[0].length-1]);
		int[] visited = new int[id3Algorithm.data[0].length];
		Arrays.fill(visited,  -1);
		Node root = id3Algorithm.createDecisionTree(data, 1, visited);
		id3Algorithm.printDecisionTree(root, 0);
		double accuracy = id3Algorithm.calculateAccuracy(root, data);
		System.out.println("Accuracy is: " + accuracy);
	}

	public int[][] readFile(String filename)
	{
		try {
			int noOfLines = 0;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String headerLine = br.readLine();
			while(br.readLine() != null)
				noOfLines++;
			System.out.println(headerLine);
			System.out.println(noOfLines);
			StringTokenizer tokenizer = new StringTokenizer(headerLine, ",");
			int i=0;
			while(tokenizer.hasMoreTokens())
			{
				header.put(i, tokenizer.nextToken());
				reverseHeader.put(header.get(i), i);
				i++;
			}
			data = new int[noOfLines][i];
			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			int lineNo = 0;
			while(true)
			{
				String line = br.readLine();
				if (line == null)
					break;
				tokenizer = new StringTokenizer(line, ",");
				int colNo = 0;
				while(tokenizer.hasMoreTokens())
				{
					data[lineNo][colNo] = Integer.parseInt(tokenizer.nextToken());
					colNo++;
				}
				lineNo++;
			}
			System.out.println(header.get(0));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("In readFile" + data[0][data[0].length-1]);
		return data;
	}
	
	public double getEntropy(int[][] data)
	{
		int i=0;
		int positive = 0, negative = 0;
		while(i<data.length-1)
		{
			if (data[i][data[0].length-1] == 1)
				positive++;
			else
				negative++;
			i++;
		}
		System.out.println("Positive is: " + positive);
		System.out.println("NEgative is: " + negative);
		if (negative == 0 || positive == 0)
			return 0.0;
		double positivePart = ((double)positive/data.length) * (Math.log10((double)positive/data.length)/Math.log10(2));
		double negativePart = ((double)negative/data.length) * (Math.log10((double)negative/data.length)/Math.log10(2));
		return -negativePart - positivePart;
	}
	
	public double getEntropyFromExample(int positive, int negative, int totalSample)
	{
		if (positive == 0 || negative == 0)
			return 0.0;
		return - ((double)positive/totalSample) * Math.log10((double)positive/totalSample)/Math.log10(2) - ((double)negative/totalSample) * Math.log10((double)negative/totalSample)/Math.log10(2);
	}
	
	public void printDecisionTree(Node root, int count)
	{
		if (root == null)
			return;
		if (root.header != null)
		{ 
			System.out.print("\n");
		}  else {
			System.out.print(root.data + "\n");
			return;
		}

		for(int i=0;i<count;i++)
		{
			System.out.print("| ");
		}
		
		if (root.left != null) {
			System.out.print(root.header + " =" + " 0 : ");
			printDecisionTree(root.left, count+1);
		} else {
			System.out.print(root.header + " =" + " 0 : " + root.data + "\n");
		}
		for(int i=0;i<count;i++)
		{
			System.out.print("| ");
		}
		if (root.right != null) {
			System.out.print(root.header + " =" + " 1 : ");
			printDecisionTree(root.right, count+1);
		} else {
			System.out.print(root.header + " =" + " 1 : " + root.data + "\n");
		}
	}

	public Node createDecisionTree(int[][] data, int depth, int[] visited)
	{
		Node root = null;
		int positivePos=0, negativePos=0, positiveNeg=0, negativeNeg=0, positive=0, negative = 0, maximumAttribute=0;
		double maxGain = -Double.MAX_VALUE;
		int leftCount = 0, rightCount = 0;
		int[][] leftData = null; 
		int[][] rightData = null;
		int dataClass = 0;
		if (checkIfDone(visited) == true)
			return root;
		System.out.println("Before entropy");
		double rootEntropy = getEntropy(data);
		System.out.println("Root entropy is: " + rootEntropy);
		if (rootEntropy == 0.0)
		{
			//All examples of certain type
			root = new Node(data[0][data[0].length-1]);
			return root;
		} else {
			for(int i=0;i<data[0].length-1;i++)
			{
				if (visited[i] == 1)
					continue;
				int attribute = i;
				negativePos=0;
				positivePos=0;
				positiveNeg=0;
				negativeNeg=0;
				positive = 0;
				negative = 0;
				for(int j=0;j<data.length;j++)
				{
					if (data[j][attribute] == 0)
					{
						negative++;
						if (data[j][data[j].length-1] == 1) {
							negativePos++;
						} else {
							negativeNeg++;
						}
					} else if (data[j][attribute] == 1) {
						positive++;
						if (data[j][data[j].length-1] == 1) {
							positivePos++;
						} else {
							positiveNeg++;
						}
					}
				}
				System.out.println("Attribute: " + attribute + " Header: " + header.get(attribute) + " Positive pos: " + positivePos + " PositiveNeg: " + positiveNeg + "NEgativePos: " + negativePos + "negativeNeg: "+negativeNeg);
				double positiveEntropy = getEntropyFromExample(positivePos, positiveNeg, positive);
				double negativeEntropy = getEntropyFromExample(negativePos, negativeNeg, negative);
				double entropy = ((double) negative / (negative + positive)) * negativeEntropy + ((double) positive / (negative + positive)) * positiveEntropy;
				double infoGain = rootEntropy - entropy;
				System.out.println("Attribute: " + attribute + " Header: " + header.get(attribute) + " Entropy: " + entropy + " Gain: " + infoGain);
				if (infoGain > maxGain)
				{
					maxGain = infoGain;
					maximumAttribute = attribute;
					leftCount = negative;
					rightCount = positive;
					if ((negativePos + positivePos) > (negativeNeg + positiveNeg))
						dataClass = 1;
					else
						dataClass = 0;
				}
			}
			root = new Node(header.get(maximumAttribute), dataClass);
			System.out.println("Node is: " +root.header);
			visited[maximumAttribute] = 1;
			int leftCounter = 0;
			int rightCounter = 0;
			leftData = new int[leftCount][data[0].length];
			rightData = new int[rightCount][data[0].length];
			for(int i=0;i<data.length-1;i++)
			{
				if (data[i][maximumAttribute] == 0) {
					for(int j=0;j<data[i].length;j++)
					{
						leftData[leftCounter][j] = data[i][j];
					}
					leftCounter++;
				} else if (data[i][maximumAttribute] == 1) {
					for(int j=0;j<data[i].length;j++)
					{
						rightData[rightCounter][j] = data[i][j];
					}
					rightCounter++;
				}
			}
		}
		/*if (checkIfDone(visited) == true)
			return root;*/
		root.left = createDecisionTree(leftData, depth+1, visited);
		root.right = createDecisionTree(rightData, depth+1, visited);
		return root;
	}
	
	public boolean checkIfDone(int[] visited)
	{
		System.out.println("Visited ");
		for(int i=0;i<visited.length-1;i++)
		{
			System.out.print(visited[i] + ",");
		}
		System.out.print("\n");
		for(int i=0;i<visited.length-1;i++)
		{
			if (visited[i] == -1)
				return false;
		}
		return true;
	}
	
	public double calculateAccuracy(Node root, int[][] data)
	{
		int right=0, wrong=0;
		boolean isCorrect;
		for(int i=0;i<data.length;i++)
		{
			isCorrect = checkRow(root, data[i]);
			if (isCorrect == true)
				right++;
			else
				wrong++;
		}
		return (double)right/(right+wrong);
	}
	
	public boolean checkRow(Node root, int[] data)
	{
		int count = 0;
		while(count != data.length-1)
		{
			System.out.println(root.header);
			if (root.header == null)
			{
				if (root.data == data[data.length-1])
					return true;
				else
					return false;
			}
			if (root.left == null && root.right == null)
			{
				if (root.data == data[data.length-1])
					return true;
				else
					return false;
			}
			int value = data[reverseHeader.get(root.header)];
			System.out.println(root.left + " " + root.right + " " + value);
			if (value == 0)
			{
				count++;
				root = root.left;
			} else {
				count++;
				root = root.right;
			}
		}
		return false;
	}
}
