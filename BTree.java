
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;


/**
 * BTree is a store class, Binary Tree with a degree set at creation. There are
 * zero of more BTreeNodes which contain at least one BTreeObject. BTree creates a 
 * large binary file it stores the node data in. The Nodes may be retrieved from the
 * Binary Tree using normal methods
 * 
 * @author: Brandon Quirarte, Shane Pruett, Stephanie Potter
 * @class: CS 342
 * @assignment: BTree
 *
 */
public class BTree{

	private int nodeSize; // All Nodes have the same size based on degree of BTree
	// Headersize is always the same regardless of the degree
	// Node size (32) +  SequenceLength (32) + degree (32) + number of nodes (32)
	private final int headerSize = 128; // size of the metadata used for BTree, Always the same
	private int sequenceLength; // the k value from handout, 3.2 Problem pg. 3, 1 - 31 
	private int degree; // the degree of BTree
	public int numberOfNodes; 
	private String filename; // filename of binary file
	private FileChannel fc;
	private RandomAccessFile aFile;
	private boolean useCache = false;
	private Cache cache;
//	private ByteBuffer buf;

	// This is the root Node
	public BTreeNode root;

	public int getSequenceLength() {
		return this.sequenceLength;
	}

	/**
	 * This is the constructor of the BTree which will be used when its a new BTree
	 * 
	 * @param sequenceLength how long are the sequences
	 * @param degree the degree of the BTree
	 */
	public BTree(int sequenceLength, int degree, String filename){
		this.sequenceLength = sequenceLength;
		this.degree = degree;
		
		
		
		ByteBuffer buf;

		if (this.sequenceLength > 31){
			System.err.println("Sequence length is too large, you entered: " + this.sequenceLength);
			System.exit(1);
		}

		// Metadata size (32) + Pointer size (32) * (Parent loc + Degree * 2) + (Degree *2 - 1)* Object Size        
		this.nodeSize = 32 + 32 * (degree * 2 + 1) + 96 * (degree * 2 - 1);

		//this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = 0;

		byte[] header = concat(asByteArray(nodeSize),
						concat(asByteArray(sequenceLength),
						concat(asByteArray(degree),
							   asByteArray(numberOfNodes))));

		this.filename = filename + ".btree.data." + sequenceLength + "." + degree;
		
//		for (int i = 0; i < header.length; i++){
//			System.out.print(header[i]);
//		}
//		System.out.println("\nInside BTree Construct\nnodesSize: " + nodeSize + 
//							"\nsequenceL: "+ sequenceLength + 
//							"\ndegree: " + degree +
//							"\nnumberOfNodes: " + numberOfNodes);
		 
		try{
			aFile = new RandomAccessFile(this.filename, "rw");
			fc = aFile.getChannel();
			fc.position(0);
			buf = ByteBuffer.wrap(header);
//			System.out.println("Buff size:" + buf.remaining());
			fc.write(buf);
//			System.out.println("Buff size:" + buf.remaining());
			buf.clear();
			fc.close();
		} catch (Exception e){
			System.err.println("There was an error with the RandomAccessFile: " + filename);
		}

		this.root = new BTreeNode(-1);
		this.root.leaf = true;


	}

	/**
	 * Constructor of BTree given the name of a file. This is used when there is an existing binary file.
	 * 
	 * @param fileName
	 */
	public BTree(String fileName){
		
		this.filename = fileName;
		ByteBuffer buf;
		//int bytesRead = 0;

		try{
			aFile = new RandomAccessFile(this.filename, "rw");
			fc = aFile.getChannel();
			buf = ByteBuffer.allocate(this.headerSize);
			
			//bytesRead = 
//			System.out.println("Buff size:" + buf.remaining());
			fc.read(buf, 0);
			buf.flip();
 
			
			byte[] dst = new byte[this.headerSize];

//			System.out.println("Buff size:" + buf.remaining());
			buf.get(dst, 0, this.headerSize);
//			System.out.println("Buff size:" + buf.remaining());
//			for (int i = 0; i < headerSize; i++){
//				System.out.print(dst[i]);
//			}
//			System.out.println();
			
			this.nodeSize = byteArrayAsInt(dst,0);
			this.sequenceLength = byteArrayAsInt(dst,32);
			this.degree = byteArrayAsInt(dst,64);
			this.numberOfNodes = byteArrayAsInt(dst,96);
			fc.close();
			
		} catch (FileNotFoundException e){
			System.err.println("There was an error with the RandomAccessFile: " + fileName);
		} catch (Exception e){
			System.err.println(e);
		}

		
		//TODO Left off here
//		System.out.print("seqL = " + this.sequenceLength + "\n" +
//		"degree = " + this.degree + "\n" +
//		//"headerSize = " + this.headerSize + "\n" +
//		"nodeSize = " + this.nodeSize + "\n" +
//		//"rootLocation = " + this.rootLocation + "\n" +
//		"numberOfNodes = " + this.numberOfNodes);
		
		if (numberOfNodes > 0){
			this.root = getRoot(0);
		}
		

//		System.out.print("seqL = " + this.sequenceLength + "\n" +
//				"degree = " + this.degree + "\n" +
//				//"headerSize = " + this.headerSize + "\n" +
//				"nodeSize = " + this.nodeSize + "\n" +
//				//"rootLocation = " + this.rootLocation + "\n" +
//				"numberOfNodes = " + this.numberOfNodes);


	}

	/**
	 * @param size
	 * @return
	 */
	public boolean useCache(int size){
		
		if (size > 0){
			this.cache = new Cache(size);
			this.useCache = true;
		}
		
		return useCache;
	}
	
	
	/**
	 * This method takes the simple location value of the Node and returns the location of which it can be
	 * found in the bin file.
	 * 
	 * @param location the integer value used to represent the location of a Node, range from value 1 - number of nodes
	 * @return return the integer location in the binary file in which the Node begins, bitlocation
	 * 
	 */
	private int nodeLocation(int location){
		// root location has a value of 0 for its location plus the headerSize
		return location * nodeSize + headerSize;
	}

	/**
	 * This method retrieves the root Node. This does not need to be used. As root node is public.
	 * 
	 * @return root node
	 */
	private BTreeNode getRoot(int location){
		
	
		
		BTreeNode node = getNode(location);
		
		if (node.parentNodeLocation == -1)
			return node;
		
		return getRoot(node.parentNodeLocation);
	}

	public BTreeNode retrieveNode(int location){
		// TODO
		if (useCache){
			BTreeNode node = cache.getObject(location);
			if (node == null){
				BTreeNode removed = cache.addObject(getNode(location));
				if (removed != null)
					removed.writeNode();
				return cache.getObject(location);
			} else {
				return node;
			}
			
		} else {
			return getNode(location);
		}
	}
	
	
	public boolean finishBTree(){
		//TODO
		
		while (cache.cache.size() > 0){
			BTreeNode removed = cache.cache.remove(0);
			removed.writeNode();
		}
		
		
		return cache.cache.size() == 0;
	}

	/**
	 * This method returns a Node given its location in the bin file
	 * 
	 * @param location
	 * @return
	 */
	public BTreeNode getNode(int location){

		int bitLocation = nodeLocation(location);
//		System.out.println("IN getNode()\nLocation: " + location + "\nBitLocation: " + bitLocation + " nodeSize:" + nodeSize);
		
		
		try {
			aFile = new RandomAccessFile(filename, "rw");
			fc = aFile.getChannel();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//aFile = new RandomAccessFile(fileName, "rw");
		//ByteBuffer buf = ByteBuffer.allocate(headerSize);

		byte[] result = new byte[nodeSize];
		ByteBuffer buf;
		
		//int bytesRead = 0;

		try{
			buf = ByteBuffer.allocate(nodeSize);
			fc.read(buf, bitLocation);
			buf.flip();
			buf.get(result, 0, nodeSize);
			fc.close();
		} catch (FileNotFoundException e){
			System.err.println("There was an error with the RandomAccessFile: " + filename);
		} catch (Exception e){
			e.printStackTrace(System.err);
		}

//		for (int i = 0; i < result.length; i++){
//			System.out.print((i % 32 == 0) ? "\n" : "" + result[i]);
//		}
//		System.out.println();
		
		return new BTreeNode(result, location);
	}


	/**
	 * This method concatenates byte arrays
	 * 
	 * @param A byte array number 1
	 * @param B byte array number 2
	 * @return returns a concatenated array of A and B
	 */
	private static byte[] concat(byte[] A, byte[] B) {
		int aLength = A.length;
		int bLength = B.length;
		byte[] C= new byte[aLength + bLength];
		System.arraycopy(A, 0, C, 0, aLength);
		System.arraycopy(B, 0, C, aLength, bLength);
		return C;
	}

	/**
	 * This method concatenates an int array of locations
	 * 
	 * @param array
	 * @return
	 */
	private byte[] concatLocations(ArrayList<Integer> array){

		int numObjects = 0;
		byte[] currArray = new byte[0];

//		System.out.println("inside concatLocations, array size:" + array.size());
		while (numObjects < (degree * 2)){
			if (array.size() <= numObjects)
				currArray = concat(currArray, asByteArray(-1));
			else{
				currArray = concat(currArray, asByteArray(array.get(numObjects)));
			}
			numObjects++;
		}

		return currArray;
	}

	/**
	 * This method converts an integer to a byte array binary representation
	 * 
	 * @param value integer to convert to byte array
	 * @return byte array representation
	 */
	private static byte[] asByteArray (int value){

		int val = value;
		byte[] arr = new byte[32];

		if (val < 0) {
			arr[0] = 1;
			val *= -1;
		}

		for (int i = 31; i > 0; i--){
			arr[i] = (byte) (val % 2);
			val /= 2;
		}
		
//		System.out.print("value: " + value);
//		for (int i = 0; i < arr.length; i++){
//			System.out.print(arr[i]);
//		}
//		System.out.println();
		
		return arr;
	}

	/**
	 * This method converts a long to a byte array binary representation
	 * 
	 * @param value long to convert to byte array
	 * @return byte array representation
	 */
	private static byte[] asByteArray (long value){

		long val = value;
		byte[] arr = new byte[64];

		if (val < 0) {
			arr[0] = 1;
			val *= -1;
		}

		for (int i = 63; i > 0; i--){
			arr[i] = (byte) (val % 2);
			val /= 2;
		}
		
//		System.out.print("value: " + value + " ");
//		for (int i = 0; i < arr.length; i++){
//			System.out.print(arr[i]);
//		}
//		System.out.println();
		
		return arr;
	}

	/**
	 * This method takes a binary representation of an integer and converts
	 * it to an int.
	 * 
	 * @param arr byte array to interpret as an int
	 * @param start the start position of the int
	 * @return and int from the binary of the array
	 */
	private static int byteArrayAsInt(byte[] arr, int start){

		int val = 0;

		for (int i = start + 31; i >= start + 1; i--){
			val += arr[i] * (int)(Math.pow(2, start + 31 - i));
		}

		if (arr[start] == 1)
			val *= -1;

//		System.out.print("value: " + val + " ");
//		for (int i = start; i < start + 32; i++){
//			System.out.print(arr[i]);
//		}
//		System.out.println();
		
		
		return val;
	}

	/**
	 * This method takes a binary representation of an integer and converts
	 * it to an long.
	 * 
	 * @param arr byte array to interpret as an long
	 * @param start the start position of the long
	 * @return and long from the binary of the array
	 */
	private static int byteArrayAsLong(byte[] arr, int start){

		int val = 0;

		for (int i = (start + 63); i >= start + 1; i--){
			val += arr[i] * (int)(Math.pow(2, start + 63 - i));
		}

		if (arr[start] == 1)
			val *= -1;

//		System.out.print("value: " + val + " ");
//		for (int i = start; i < start + 64; i++){
//			System.out.print(arr[i]);
//		}
//		System.out.println();
		
		return val;
	}

	/**
	 * This is the main insert method of BTree, it will split the root if it needs split and use
	 * the insertNonFull() method to insert the sequence
	 * 
	 * @param kSeq long representation of the sequence
	 */
	public BTreeNode insertNode(long kSeq){

		BTreeNode theRoot = root;
		BTreeNode returned = theRoot;
		//		System.out.println("Compare Root: " + theRoot.objects.size() + " ? " + theRoot.currentObjects);
		
		// Just in case the root needs split
		if (theRoot.currentObjects == (degree * 2 - 1)){

			BTreeNode s = new BTreeNode(-1);
			this.root = s;
			s.leaf = false;
			s.currentObjects = 0;
			s.childNodeLocations.add(theRoot.selfNodeLocation);
			splitChild(s,1);
			returned = insertNonFull(s,kSeq);
		}
		else {
			returned = insertNonFull(theRoot,kSeq);
		}

		return returned;
	}

	/**
	 * This method splits the child node if it is full
	 * 
	 * @param xParent
	 * @param iXChild
	 */
	private void splitChild(BTreeNode xParent, int iXChild){

		// To be the Right Child after Split
		BTreeNode zRightChild = new BTreeNode(xParent.selfNodeLocation);
		// To be the Left Child after Split
		BTreeNode yLeftChild = retrieveNode(xParent.childNodeLocations.get(iXChild-1)); //getNode replaced
		yLeftChild.parentNodeLocation = xParent.selfNodeLocation;


		zRightChild.leaf = yLeftChild.leaf;
		zRightChild.currentObjects = degree - 1;

		for (int i = 1; i < degree ; i++){
			zRightChild.objects.add(yLeftChild.objects.remove(degree));
		}

		if (!yLeftChild.leaf){
			for (int i = 1; i <= degree; i++){
				zRightChild.childNodeLocations.add(yLeftChild.childNodeLocations.remove(degree));
				//				System.out.println("z#child: " + zRightChild.childNodeLocations.size() + "  y#child: " + yLeftChild.childNodeLocations.size());
			}
		}

		yLeftChild.currentObjects = degree - 1;

		// Skipping part of the pseudo code i can split because I'm using an arrayList


		xParent.childNodeLocations.add(iXChild, zRightChild.selfNodeLocation );
		// Skipping part of pseudo code because of ArrayList implementation
		xParent.objects.add(iXChild - 1, yLeftChild.objects.remove(degree-1));

		xParent.currentObjects++;

		yLeftChild.writeNode();
		zRightChild.writeNode();
		xParent.writeNode();


	}
	
//	public void writeBTree(){
//		byte[] header = concat(asByteArray(nodeSize),
//				concat(asByteArray(sequenceLength),
//						concat(asByteArray(degree),asByteArray(numberOfNodes))));
//		System.out.println("BTree Write():\nnodeSize: " + nodeSize + "\nsequenceL: " + sequenceLength + "\nDegree: " + degree);
//		
//		this.filename = filename + ".gbk.btree.data." + sequenceLength + "." + degree;
//		ByteBuffer buf;
//
//		try{
//			aFile = new RandomAccessFile(filename, "rw");
//			fc = aFile.getChannel();
//			
//			fc.position(0);
//			buf = ByteBuffer.wrap(header);
//			System.out.println("Buff size:" + buf.remaining());
//			fc.write(buf);
//			System.out.println("Buff size:" + buf.remaining());
//			buf.clear();
//			fc.close();
//		} catch (Exception e){
//			System.err.println("There was an error with the RandomAccessFile: " + filename);
//		}
//	}

	/**
	 * This method inserts a sequence in to a nonfull node
	 * 
	 * @param xNode
	 * @param kSeq
	 */
	private BTreeNode insertNonFull(BTreeNode xNode, long kSeq){
		int i = xNode.currentObjects;

		if (xNode.leaf){

			while (i >= 1 && kSeq <= xNode.objects.get(i-1).sequence){
				if (kSeq == xNode.objects.get(i-1).sequence){
					xNode.objects.get(i-1).frequency++;
					xNode.writeNode();
					return xNode;
				}
				i--;
			}
			//if ( i >= 1 && kSeq == xNode.objects.get(i-1).sequence) System.err.println("ERROR INSIDE INSERTNONFULL");

			//			System.out.println(kSeq);
			//xNode.addTreeObject(i, kSeq);
			xNode.objects.add(i,xNode.getTreeObject(kSeq));
			xNode.currentObjects++;
//			System.out.println("right after add objectsSize: " + xNode.objects.size() + "  currObj: " + xNode.currentObjects);


			xNode.writeNode();
			return xNode;

		}
		else {
			while(i >= 1 && kSeq <= xNode.objects.get(i-1).sequence){
				if (kSeq == xNode.objects.get(i-1).sequence){
					
					xNode.objects.get(i-1).frequency++;
//					System.out.println("A value was equal <" + xNode.objects.get(i-1));
					xNode.writeNode();
					return xNode;
				}
				i--;
			}
			i++;

			//			System.out.println("in insertNonFull size of childLocations: " + xNode.childNodeLocations.size());
			//			System.out.println(xNode);

			BTreeNode child = retrieveNode(xNode.childNodeLocations.get(i-1));// getNode replaced
			if (child.currentObjects == (degree * 2 - 1)){
				splitChild(xNode,i);
				if (kSeq >= xNode.objects.get(i-1).sequence){
					if (kSeq == xNode.objects.get(i-1).sequence){
						xNode.objects.get(i-1).frequency++;
//						System.out.println("A value was equal <" + xNode.objects.get(i-1));
						xNode.writeNode();
						return xNode;
					
					}
					i++;
				}
				
			}
			return insertNonFull(retrieveNode(xNode.childNodeLocations.get(i-1)), kSeq); // getNode replaced

		}
		
	}

	/**
	 * This method prints some debugging info to the konsole
	 */
	public void printBTree(BufferedWriter b){

//		String s ="";
//		for (BTreeNode.TreeObject b : root.objects){
//			s+= b.getSequenceString() + " v:" + b.sequence + " ";
//		}
//
//
//		System.out.println("ROOT: " + s);
		printNode(root, b);
		
		
//		for (BTreeNode.TreeObject b : root.objects){
//			
//			System.out.println(b);
//			
//			printChildNodes(root.childNodeLocations, 1);
//			
//		}

	}

	/**
	 * recursively prints the child nodes
	 * 
	 * @param children
	 * @param tabs
	 */
	private void printNode(BTreeNode node, BufferedWriter b){

//		String t = s;
//		String s = "";
//
//		for (int i : children){
//			if (i != -1){
//			BTreeNode b = getNode(i);
//
//			for (BTreeNode.TreeObject t : b.objects){
////				System.out.println(t);
//				s += " [<" + t.getSequenceString() + "=:=" + t.sequence + ">#" + t.frequency +"] ";
//			}
//			printChildNodes(b.childNodeLocations, tabs + 1);
//			}
//		}
//
//		if (s.length() != 0)
//			System.out.println("[DEPTH=" + tabs + "] " + s);
		
		if (node.childNodeLocations.size() ==0){
			for (int i = 0; i < node.currentObjects; i++){
//				t = t + node.objects.get(i) + "\n";
				try {
					b.write("" + node.objects.get(i) + " \n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for (int i = 0; i < node.childNodeLocations.size(); i++){
			printNode(getNode(node.childNodeLocations.get(i)), b);
			if (i < node.currentObjects){
//				t = t + node.objects.get(i) + "\n";
				try {
					b.write("" + node.objects.get(i) + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
//		return t;


		
	}


	@Override
	public String toString() {
		return "BTree [root=" + root + "]" ;
	}

	public static void main(String[] args) throws IOException {

		// Testing of asByteArray (int)
		//		byte[] x = asByteArray(-1);
		//		System.out.println();
		//		for (int i = 0; i < x.length; i++){
		//		System.out.print(x[i]);
		//		}
		//		System.out.println();

		// Testing of byteArrayAsInt(byte[], start)
		//		int val = byteArrayAsInt(x, 0);
		//		System.out.println(val);

		// Testing of asByteArray (long)
		//		byte[] x = asByteArray(234L);
		//		System.out.println();
		//		for (int i = 0; i < x.length; i++){
		//		System.out.print(x[i]);
		//		}

		// Testing of concat(byte[], byte[])
		//		byte[] x = asByteArray(5);
		//		byte[] y = asByteArray(234);
		//		byte[] z = concat(x , y);
		//		for (int i = 0; i < z.length; i++){
		//		System.out.print(z[i]);
		//		}
		//		System.out.println();

		// Testing of multiple ints in array
		//		int val1 = byteArrayAsInt(z, 0);
		//		int val2 = byteArrayAsInt(z, 32);
		//		System.out.println("val1 = " + val1 + "\nval2 = " + val2);

		// Testing of constructor
//		BTree theTree = new BTree (5, 4, "filename.gbk");
////		System.out.println();
//		for (int i = 1; i < 512; i++){
//			//			System.out.println("Adding " + i  + "L");
//			theTree.insertNode((long)(i % 256));
//		}
////		theTree.printBTree();
//		System.out.println("numberOfNode: " +theTree.numberOfNodes); 


		// TODO HERE!
		// Testing of filename Constructor
//				BTree aTree = new BTree ("filename.gbk.btree.data.5.4"/*"filename.gbk.btree.data.3.3"*/);
//				System.out.println("numberOfNode: " +aTree.numberOfNodes);

	}

	/**
	 * This inner class is the Nodes used to store sequences
	 * 
	 * @author spruett
	 *
//	 */
	public class BTreeNode{

		public int currentObjects; //number of objects in Node
		public int parentNodeLocation; // location of parent Node
		public boolean leaf;

		public ArrayList<Integer> childNodeLocations;
		public ArrayList<TreeObject> objects;

		private int selfNodeLocation;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			int other = (Integer) obj;
			if (selfNodeLocation != other)
				return false;
			return true;
		}

		/**
		 * Create an empty Node
		 * 
		 * @param parentNode
		 */
		public BTreeNode(int parentNode){

			childNodeLocations = new ArrayList<Integer>();
			objects = new ArrayList<TreeObject>();

			this.currentObjects = 0;
			this.parentNodeLocation = parentNode;

			// This new node has a location equal to the number of nodes now in the tree
			this.selfNodeLocation = numberOfNodes++;


		}

		/**
		 * Constructor given a byte array representation of Node as read by binary file
		 * 
		 * @param array 
		 */
		public BTreeNode(byte[] array, int selfLocation){

			childNodeLocations = new ArrayList<Integer>();
			objects = new ArrayList<TreeObject>();

			this.currentObjects = byteArrayAsInt(array,0);
			this.parentNodeLocation = byteArrayAsInt(array,32);

			int index = 0;

			// Read the child Locations
			int verifyChildrenCount = 0;
			for (int i = 0; i < (degree * 2); i++){
				int val = byteArrayAsInt(array,32 * index++ + 64);
//				System.out.println("child loc: " + val);
				if (val != -1){
					verifyChildrenCount++;
					childNodeLocations.add(val);
				}
			}
			
			this.leaf = childNodeLocations.size() == 0;
//			System.out.println("is a leaf? " + leaf + "  #children=" + childNodeLocations.size());
			
			if (currentObjects + 1 != verifyChildrenCount && !leaf){
				System.err.println("The current number of Children saved in the bin is different than the number found " + 
									verifyChildrenCount + " vs "+ currentObjects);
				System.out.println(this);
				System.exit(1);

			}

			



			int start = 32 * index + 64;

			// Read the objects
			int verifyObjectCount = 0;
			for (int i = 0; i < ((degree * 2) -1); i++){
				int intval = byteArrayAsInt(array, start + i * 96);
				long longval = byteArrayAsLong(array, start + i * 96 + 32);
				if (intval != -1){
					verifyObjectCount++;
//					System.out.println("Inside BTreeNode Construct: f: " + intval + " v:" + longval);
					objects.add(new TreeObject(intval,longval));
				}
			}

			this.selfNodeLocation = selfLocation;

			if (currentObjects != verifyObjectCount){
				System.err.println("The current number of Objects saved in the bin is different than the number found " + 
									verifyObjectCount + " vs "+ currentObjects);
				System.out.println(this);
				System.exit(1);

			}
		}

		/**
		 * Get byte array representation of objects
		 * 
		 * @param objs
		 * @return
		 */
		private byte[] getObjectArrayBytes(ArrayList<TreeObject> objs){

//			int numObjects = 1;
//			byte[] currArray = objs.get(0).getByteCode();
			
			int numObjects = 0;
			byte[] currArray = new byte[0];

			while (numObjects < (degree * 2 - 1)){
				if (numObjects >= currentObjects)
					currArray = concat(currArray, getNullByteCode());
				else{
					currArray = concat(currArray, objs.get(numObjects).getByteCode());
				}
				numObjects++;
			}

			return currArray;
		}

		/**
		 * Use a null ByteCode for Objects that don't exist yet
		 * 
		 * @return
		 */
		public byte[] getNullByteCode(){
			return concat(asByteArray(-1),asByteArray(-1L));
		}


		/**
		 * This method writes the Node to the bin file
		 */
		public void writeNode(){
			try {
				aFile = new RandomAccessFile(filename, "rw");
				fc = aFile.getChannel();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			ByteBuffer buf;
			// TODO HERE
			int bitLocation = nodeLocation(selfNodeLocation);//selfNodeLocation * nodeSize + headerSize;
			
//			System.out.println("NodeSize inside writeNode: " + nodeSize);
			
			byte[] result = new byte[nodeSize];

//			System.out.println("NODE:\ncurrObj: " + currentObjects +
//								" parentLoc: "+ parentNodeLocation +
//								" childLocSize: " + childNodeLocations.size() +
//								" objectsSize: " + objects.size());
			
			
			
			result = concat(asByteArray(currentObjects),
					 concat(asByteArray(parentNodeLocation),
					 concat(concatLocations(childNodeLocations),
							getObjectArrayBytes(objects))));
			
//			System.out.println("result size: " + result.length + " nodeSize: " + nodeSize);
//			System.out.println("objects size: " + getObjectArrayBytes(objects).length);
			
			try {
				// Update number of nodes in BTree
				fc.position(96);
//				System.out.println("bus size: " + buf.remaining());
				buf = ByteBuffer.wrap(asByteArray(numberOfNodes));
//				System.out.println("bus size: " + buf.remaining());
				fc.write(buf);
				buf.clear();
				// Update Node info in bin
				fc.position(bitLocation);
//				System.out.println("bus size: " + buf.remaining());
				buf = ByteBuffer.wrap(result);
//				System.out.println("bus size: " + buf.remaining());
				fc.write(buf);
//				System.out.println("bus size: " + buf.remaining());
				buf.clear();
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {

			String s = "<Node> location: <" + selfNodeLocation + ">  leaf?: <" + leaf + ">  currentObjects: <" + currentObjects + ">   parentLocation: <" + parentNodeLocation + ">\n";
			s+= "TreeObjects: ";
			for (TreeObject t : objects)
				s+= "Obj<" + t + ">  ";
			s += "\nChild Locations: ";
			for (int i : childNodeLocations)
				s+= " <" + i + ">  ";

			return s;
		}

		/**
		 * Add a TreeObject to the Node at a specific index
		 * 
		 * @param index
		 * @param seq
		 */
		public TreeObject getTreeObject(long seq){
			TreeObject obj = new TreeObject(seq);
			return obj;
		}

		/**
		 * Tree Object. Has a frequency and a sequence which is also its key value
		 * 
		 * @author Shane
		 *
		 */
		public class TreeObject{

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				TreeObject other = (TreeObject) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (sequence != other.sequence)
					return false;
				return true;
			}

			private int frequency;
			private long sequence;

			/**
			 * Construct a Tree object from a string sequence. Use this constructor 
			 * if this is the first appearance of the sequence.
			 * 
			 * @param seq
			 */
			public TreeObject(String seq){
				this.frequency = 1;
				this.sequence = convertSeq(seq);

			}

			/**
			 * Construct a Tree object from a sequence in long form. Use this constructor 
			 * if this is the first appearance of the sequence.
			 * 
			 * @param seq
			 */
			public TreeObject(long seq){
				this.frequency = 1;
				this.sequence = seq;

			}

			/**
			 * Construct a Tree object from a String sequence and a frequency. Use this
			 * constructor when the BTreeNode is being created from the bin file
			 * 
			 * @param seq
			 * @param freq
			 */
			public TreeObject(int freq,String seq){
				this.frequency = freq;
				this.sequence = convertSeq(seq);

			}

			/**
			 * Construct a Tree object from a sequence in long form and a frequency. Use this
			 * constructor when the BTreeNode is being created from the bin file
			 * 
			 * @param seq
			 * @param freq
			 */
			public TreeObject(int freq,long seq){
				this.frequency = freq;
				this.sequence = seq;

			}

			/**
			 * This method returns a byte array representation of the TreeObject
			 * 
			 * @return
			 */
			public byte[] getByteCode(){

				return concat(asByteArray(frequency),asByteArray(sequence));
			}

			/**
			 * returns the long representation of the sequence
			 * 
			 * @param seq string representation of the sequence
			 * @return long representation of sequence
			 */
			public long convertSeq(String seq){

				seq.toUpperCase();

				if (seq.length() != sequenceLength)
					System.err.println("A provided sequence (" + seq + ") is the improper length");
				byte[] arr = new byte[64];

				int arrIndex = arr.length - 1;

				for (int i = seq.length(); i > 0; i--){

					switch (seq.charAt(i)){
					case 'A':
						arr[i-1] = 0; arr[i] = 0;
						break;
					case 'T':
						arr[i-1] = 1; arr[i] = 1;
						break;
					case 'C':
						arr[i-1] = 0; arr[i] = 1;
						break;
					case 'G':
						arr[i-1] = 1; arr[i] = 0;
						break;
					}

					arrIndex = arrIndex - 2;

				}

				long result = byteArrayAsLong(arr, 0);

				return result;
			}

			/**
			 * increment the frequency, not really needed
			 */
			public void incrementFreq(){
				this.frequency++;
			}

			/**
			 * returns the frequency of this sequence
			 * 
			 * @return int
			 */
			public int getFreq(){
				return this.frequency;
			}

			/**
			 * returns a long representation of the sequence
			 * 
			 * @return long
			 */
			public long getSequence(){
				return sequence;
			}

			/**
			 * returns a string representation of the sequence
			 * 
			 * @return string
			 */
			public String getSequenceString(){
				byte[] arr = asByteArray(sequence);

				if (arr[0] == 1){
					return "nil";
				}

				String result = "";

				for (int i = arr.length - 2 * sequenceLength; i < arr.length; i = i + 2){

					if (arr[i] == 0){
						if (arr[i+1] == 0)
							result += "A";
						else 
							result += "C";
					}
					else {
						if (arr[i+1] == 0)
							result += "G";
						else
							result += "T";
					}
				}

				return result;
			}

			@Override
			public String toString() {
				return getSequenceString().toLowerCase() + ": " + frequency;
			}

			private BTreeNode getOuterType() {
				return BTreeNode.this;
			}
		}

		private BTree getOuterType() {
			return BTree.this;
		}
	}
}