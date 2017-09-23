import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Collections;


public class ID3 {

	private HashMap<Integer, String> header = new HashMap<Integer,String>();
	private HashMap<String, Integer> reverseHeader = new HashMap<String,Integer>();
	private static HashMap<Integer, ArrayList<Integer>> depthMap = new HashMap<Integer,  ArrayList<Integer>>();
	private int[][] data;
	static int noOfInstances=0;
	static int noOfAttributes=0;
	static int leafNodes=0;
	static int nodes=0;
	static ArrayList<Integer> labels;
	static int globalRemoveCount = 0, localRemoveCount = 0;
	static int treeDepth = -1;
	static int labelCounter = 0;
	static double postAccuracy = 0;
	static int iteration = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ID3 id3Algorithm = new ID3();
		String trainingDataSet = args[0];
		String validationDataSet = args[1];
		String testDataSet = args[2];
		float pruningFactor = Float.parseFloat(args[3]);
		
		
		int[][] data = id3Algorithm.readFile(trainingDataSet);
		
		int[] visited = new int[id3Algorithm.data[0].length];
		Arrays.fill(visited,  -1);
		labels = new ArrayList<>();
		
		Node root = id3Algorithm.createDecisionTree(data, 1, visited);
		System.out.println("Decision Tree: \n");
		id3Algorithm.printDecisionTree(root, 0);
		//Node root = id3Algorithm.createRandomDecisionTree(data, 1, visited);
		
		id3Algorithm.getNoOfNodes(root);
		double accuracy = id3Algorithm.calculateAccuracy(root, data);
		
		System.out.println("Pre-pruned accuracy: \n");
		System.out.println("Number of training instances: " + ID3.noOfInstances+ "\n");
		System.out.println("Number of training attributes: " + ID3.noOfAttributes+ "\n");
		System.out.println("Total number of nodes in the tree: " + ID3.nodes+ "\n");
		System.out.println("Number of leaf nodes in the tree: " + ID3.leafNodes+ "\n");
		System.out.println("Accuracy of the model on the training dataset: " + accuracy*100 + "\n");
		
		data = id3Algorithm.readFile(validationDataSet);
		visited = new int[id3Algorithm.data[0].length];
		Arrays.fill(visited,  -1);
		accuracy = id3Algorithm.calculateAccuracy(root, data);
		
		System.out.println("Number of training instances: " + ID3.noOfInstances+ "\n");
		System.out.println("Number of training attributes: " + ID3.noOfAttributes+ "\n");
		System.out.println("Accuracy of the model on the validation dataset: " + accuracy*100 + "\n");
		
		while(postAccuracy <= accuracy) {
			globalRemoveCount = 0;
			Node clone = id3Algorithm.clone(root);
			labels = id3Algorithm.calculateRandomLabels(pruningFactor);
			Node prunedRoot = id3Algorithm.pruneTree(clone);
			postAccuracy = id3Algorithm.calculateAccuracy(prunedRoot, data);
			System.out.println(++iteration + ". Original Validation Set Accuracy: "+accuracy*100+" Post Pruning Validation Set Accuracy: "+postAccuracy*100);
		}
		
		data = id3Algorithm.readFile(testDataSet);
		visited = new int[id3Algorithm.data[0].length];
		Arrays.fill(visited,  -1);
		accuracy = id3Algorithm.calculateAccuracy(root, data);
		
		System.out.println("Number of training instances: " + ID3.noOfInstances+ "\n");
		System.out.println("Number of training attributes: " + ID3.noOfAttributes+ "\n");
		System.out.println("Accuracy of the model on the test dataset: " + accuracy*100 + "\n");
		
		postAccuracy = 0;iteration= 0;
		while(postAccuracy <= accuracy) {
			globalRemoveCount = 0;
			Node clone = id3Algorithm.clone(root);
			labels = id3Algorithm.calculateRandomLabels(pruningFactor);
			Node prunedRoot = id3Algorithm.pruneTree(clone);
			postAccuracy = id3Algorithm.calculateAccuracy(prunedRoot, data);
			System.out.println(++iteration + ". Original Test Set Accuracy: "+accuracy*100+" Post Pruning Test Set Accuracy: "+postAccuracy*100);
		}
	}

	public Node clone(Node root) {
		if(root == null)
			return root;
		Node node = new Node(0, 0);
		node.data = root.data;
		node.dataClass = root.dataClass;
		node.depth = root.depth;
		node.header = root.header;
		node.label = root.label;
		node.left = clone(root.left);
		node.right = clone(root.right);
		return node;
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
			ID3.noOfInstances = noOfLines;
			StringTokenizer tokenizer = new StringTokenizer(headerLine, ",");
			int i=0;
			while(tokenizer.hasMoreTokens())
			{
				header.put(i, tokenizer.nextToken());
				reverseHeader.put(header.get(i), i);
				i++;
			}
			data = new int[noOfLines][i];
			ID3.noOfAttributes = i-1;
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
	
	public void getNoOfNodes(Node root)
	{
		if (root == null)
			return;
		getNoOfNodes(root.left);
		if (root.left == null && root.right == null)
			ID3.leafNodes++;
		ID3.nodes++;
		getNoOfNodes(root.right);
	}
	public double getEntropy(int[][] data)
	{
		int i=0;
		int positive = 0, negative = 0;
		while(i<data.length)
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
	
	public double getEntropyFromExample(double positive, double negative, double totalSample)
	{
		System.out.println("In calculation Positive is: " + positive + " NEgative is: " + negative + " Total Samplsi: " +totalSample + "\n" );
		if (positive == 0 || negative == 0)
			return 0.0;
		double positivePart = (positive/totalSample) * (Math.log(positive/totalSample)/Math.log(2));
		double negativePart = (negative/totalSample) * (Math.log(negative/totalSample)/Math.log(2));
		System.out.println("In calculation Positive part is: " + positivePart + " NEgative part is: " + negativePart + "\n" );
		return -positivePart - negativePart;
		//return - ((double)positive/totalSample) * Math.log10((double)positive/totalSample)/Math.log10(2) - ((double)negative/totalSample) * Math.log10((double)negative/totalSample)/Math.log10(2);
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

	static int count = 0;
	public Node createRandomDecisionTree(int[][] data, int depth, int[] visited)
	{
		count++;
		System.out.println("Count is: "+count);
		Node root = null;
		int[][] leftData;
		int[][] rightData;
		if (checkIfDone(visited) == true)
			return root;
		double rootEntropy = getEntropy(data);
		if (rootEntropy == 0.0) {
			root = new Node(data[0][data[0].length-1], ++labelCounter);
			return root;
		} else {
				Random rand = new Random();
				int attribute = rand.nextInt(data[0].length-2);
				System.out.println(":Length is: " +(data[0].length-1)+" Random attribute is: " + attribute);
				while (visited[attribute] == 1)
					attribute = rand.nextInt(data[0].length-2);
				System.out.println("Random attribute is: " + attribute);
				int negativePos=0;
				int positivePos=0;
				int positiveNeg=0;
				int negativeNeg=0;
				int positive = 0;
				int negative = 0;
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
				int dataClass;
				if ((negativePos + positivePos) > (negativeNeg + positiveNeg))
					dataClass = 1;
				else
					dataClass = 0;
				System.out.println("Attribute: " + attribute + " Header: " + header.get(attribute) + " Positive pos: " + positivePos + " PositiveNeg: " + positiveNeg + "NEgativePos: " + negativePos + "negativeNeg: "+negativeNeg);
				root = new Node(header.get(attribute), dataClass, ++labelCounter);
				visited[attribute] = 1;
				leftData = new int[negative][data[0].length];
				rightData = new int[positive][data[0].length];
				int leftCounter = 0;
				int rightCounter = 0;
				for(int i=0;i<data.length-1;i++)
				{
					if (data[i][attribute] == 0) {
						for(int j=0;j<data[i].length;j++)
						{
							leftData[leftCounter][j] = data[i][j];
						}
						leftCounter++;
					} else if (data[i][attribute] == 1) {
						for(int j=0;j<data[i].length;j++)
						{
							rightData[rightCounter][j] = data[i][j];
						}
						rightCounter++;
					}
				}
		}
		System.out.println("Left data length is: " + leftData.length);
		System.out.println("Right data length is: "+rightData.length);
		if(treeDepth < depth+1)
			treeDepth = depth+1;
		if(depthMap.containsKey(depth+1)) {
			ArrayList<Integer> list = depthMap.get(depth+1);
			list.add(root.label);
			depthMap.put(depth+1, list);
		}
		else {
			ArrayList<Integer> list = new ArrayList<>();
			list.add(root.label);
			depthMap.put(depth+1, list);
		}
		if (leftData.length > 0)
			root.left = createRandomDecisionTree(leftData, depth+1, visited.clone());
		if (rightData.length > 0)
		root.right = createRandomDecisionTree(rightData, depth+1, visited.clone());
		return root;
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
		
		System.out.println("Before entropy");
		double rootEntropy = getEntropy(data);
		System.out.println("Root entropy is: " + rootEntropy);
		if (rootEntropy == 0.0)
		{
			//All examples of certain type
			root = new Node(data[0][data[0].length-1], ++labelCounter);
			return root;
		} else {
			if (checkIfDone(visited) == true)
				return root;
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
			root = new Node(header.get(maximumAttribute), dataClass, ++labelCounter);
			System.out.println("Node is: " +root.header);
			visited[maximumAttribute] = 1;
			int leftCounter = 0;
			int rightCounter = 0;
			leftData = new int[leftCount][data[0].length];
			rightData = new int[rightCount][data[0].length];
			for(int i=0;i<data.length;i++)
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
		if(treeDepth < depth+1)
			treeDepth = depth+1;
		if(depthMap.containsKey(depth+1)) {
			ArrayList<Integer> list = depthMap.get(depth+1);
			list.add(root.label);
			depthMap.put(depth+1, list);
		}
		else {
			ArrayList<Integer> list = new ArrayList<>();
			list.add(root.label);
			depthMap.put(depth+1, list);
		}
		root.left = createDecisionTree(leftData, depth+1, visited.clone());
		root.right = createDecisionTree(rightData, depth+1, visited.clone());
		return root;
	}
	
	public boolean checkIfDone(int[] visited)
	{
		/*System.out.println("Visited ");
		for(int i=0;i<visited.length-1;i++)
		{
			System.out.print(visited[i] + ",");
		}*/
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
			if (value == 0)
			{
				if (root.left == null) {
					if (root.data == data[data.length-1])
						return true;
					else
						return false;
				} else {
					count++;
					root = root.left;
				}
			} else {
				if (root.right == null) {
					if (root.data == data[data.length-1])
						return true;
					else
						return false;
				} else {
					count++;
					root = root.right;
				}
			}
		}
		return false;
	}
	public Node pruneTree(Node root) {
		if(labels.contains((int)root.label) && globalRemoveCount < labels.size()) {
			localRemoveCount = 0;
			int rCount = removeSubtree(root);
			globalRemoveCount += rCount;
		}
		else {
			if(root.left!=null)
				pruneTree(root.left);
			if(root.right!=null)
				pruneTree(root.right);
		}
		return root;
	}

	public int removeSubtree(Node root) {
		if(root.left!=null)
			removeSubtree(root.left);
		if(root.right!=null)
			removeSubtree(root.right);
		root.left = null;
		root.right = null;
		root = null;
		localRemoveCount++;
		return localRemoveCount;
	}
	
	public ArrayList<Integer> calculateRandomLabels(float pruningFactor) {
		int noOfNodesToPrune = (int)(pruningFactor * labelCounter);
		ArrayList<Integer> listOfLabels = new ArrayList<>();
		for(int i=treeDepth;i>treeDepth-5;i--) {
			ArrayList<Integer> list = depthMap.get(i);
			listOfLabels.addAll(list);
		}

		Collections.shuffle(listOfLabels);
		ArrayList<Integer> randomLabels = new ArrayList<>();
		for(int i=0;i<noOfNodesToPrune;i++) {
			randomLabels.add(listOfLabels.get(i));
		}
		return randomLabels;
	}
}
