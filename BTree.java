
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
 * @author Shane Pruett
 *
 */
public class BTree{

	private int nodeSize; // All Nodes have the same size based on degree of BTree
	private final int headerSize = 128; // size of the metadata used for BTree, Always the same
	private int sequenceLength; // the k value from handout, 3.2 Problem pg. 3, 1 - 31 
	//TODO getSequenceLength
	private int degree; // the degree of BTree
	//TODO getDegree
	private int rootLocation; //location of root, TODO not necessary
	private int numberOfNodes; 
	private String filename; // filename of binary file
	private FileChannel fc;

	//	private boolean usingCache; 
	//	private int cacheSize;

	//	private int debugLevel;

	// This is the root Node
	public BTreeNode root;



	/**
	 * This is the constructor of the BTree which will be used when its a new BTree
	 * 
	 * @param sequenceLength how long are the sequences
	 * @param degree the degree of the BTree
	 */
	public BTree(int sequenceLength, int degree){ //TODO parameter string filename
		this.sequenceLength = sequenceLength;
		this.degree = degree;

		// This is always the same regardless of the degree
		// Node size (32) +  SequenceLength (32) + degree (32) + number of nodes (32)
		//this.headerSize = 128; // number of bits of header

		// Metadata size (32) + Pointer size (32) * (Parent loc + Degree * 2) + (Degree *2 - 1)* Object Size        
		this.nodeSize = 32 + 32 * (degree * 2 + 1) + 96 * (degree * 2 - 1);

		this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = 0;

		byte[] header = concat(asByteArray(nodeSize),
				concat(asByteArray(sequenceLength),
						concat(asByteArray(degree),asByteArray(numberOfNodes))));

		//		System.out.println("NodeSize = " + nodeSize + "\nHeaderSize = " + headerSize + "\nsequenceLength = " + sequenceLength);
		//		System.out.println("|--nodeSize--------------------||--sequenceLength--------------||--degree----------------------||--numberofnodes---------------|");
		//		System.out.println(header.length);
		//		for (int i = 0; i < header.length; i++){
		//			System.out.print(header[i]);
		//		}

		this.filename = "filename.gbk.btree.data." + sequenceLength + "." + degree;


		RandomAccessFile aFile;
		try{
			aFile = new RandomAccessFile(filename, "rw");
			fc = aFile.getChannel();
			fc.position(0);
			fc.write(ByteBuffer.wrap(header));
		} catch (Exception e){
			System.err.println("There was an error with the RandomAccessFile: " + filename);
		}

		this.root = new BTreeNode(-1);
		this.root.leaf = true;

		//		try {
		//			OutputStream output = null;
		//			try {
		//				output = new BufferedOutputStream(new FileOutputStream(filename));
		//				output.write(header);
		//			}
		//			finally {
		//				output.close();
		//			}
		//		}
		//		catch(FileNotFoundException ex){
		//			System.err.println(ex);
		//		}
		//		catch(IOException ex){
		//			System.err.println(ex);
		//		}


	}

	/**
	 * Constructor of BTree given the name of a file. This is used when there is an existing binary file.
	 * 
	 * @param fileName
	 */
	public BTree(String fileName){

		RandomAccessFile aFile;
		ByteBuffer buf;
		int bytesRead = 0;

		try{
			aFile = new RandomAccessFile(fileName, "rw");
			fc = aFile.getChannel();
			buf = ByteBuffer.allocate(headerSize);

			bytesRead = fc.read(buf, 0);
			buf.flip();

			byte[] dst = new byte[headerSize];


			buf.get(dst, 0, headerSize);

			this.nodeSize = byteArrayAsInt(dst,0);
			//System.out.println(this.nodeSize);

			this.sequenceLength = byteArrayAsInt(dst,32);
			//			System.out.println(this.sequenceLength);

			this.degree = byteArrayAsInt(dst,64);
			//			System.out.println(this.degree);

			this.numberOfNodes = byteArrayAsInt(dst,96);
			//			System.out.println(this.numberOfNodes);

			//			System.out.println("bytesRead: " + bytesRead);
		} catch (FileNotFoundException e){
			System.err.println("There was an error with the RandomAccessFile: " + fileName);
		} catch (Exception e){
			System.err.println(e);
		}


		this.rootLocation = headerSize; 

		this.filename = fileName;

		if (numberOfNodes > 0){
			this.root = getRoot();
		}

		System.out.print("seqL = " + this.sequenceLength + "\n" +
				"degree = " + this.degree + "\n" +
				//"headerSize = " + this.headerSize + "\n" +
				"nodeSize = " + this.nodeSize + "\n" +
				//"rootLocation = " + this.rootLocation + "\n" +
				"numberOfNodes = " + this.numberOfNodes);


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
	public BTreeNode getRoot(){
		return getNode(0);
	}


	/**
	 * This method returns a Node given its location in the bin file
	 * 
	 * @param location
	 * @return
	 */
	public BTreeNode getNode(int location){



		int bitLocation = nodeLocation(location);
		//		System.out.println("IN getNode()\nLocation: " + location + "\nBitLocation: " + bitLocation);


		byte[] result = new byte[nodeSize];


		ByteBuffer buf;
		int bytesRead = 0;

		try{
			buf = ByteBuffer.allocate(nodeSize);
			bytesRead = fc.read(buf, bitLocation);
			buf.flip();
			//			System.out.println("Bytes Read: " + bytesRead);
			//			System.out.println("Buffer remaining: " +buf.remaining());
			buf.get(result, 0, nodeSize);

		} catch (FileNotFoundException e){
			System.err.println("There was an error with the RandomAccessFile: " + filename);
		} catch (Exception e){
			e.printStackTrace(System.err);
		}

		//		for (int i = 0; i < result.length; i++){
		//			System.out.print(result[i]);
		//		}


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
			//			System.out.println(numObjects);
			if (array.size() <= numObjects)
				currArray = concat(currArray, asByteArray(-1));
			else{
				currArray = concat(currArray, asByteArray(array.get(numObjects)));
			}
			numObjects++;
			
			//			System.out.println(numObjects);
		}


		//		if (array.isEmpty())
		//			return new byte[0];
		//
		//		//		int[] end = new int[array.length -1];
		//		//
		//		//		for (int i = 0; i < end.length; i++)
		//		//			end[i] = array[i+1];

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

		
//		System.out.println("Starting from " + start + ": " + val);
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

		//		for (int i = start; i < start + 64; i++)
		//			System.out.print(arr[i]);
		//		System.out.println();

		int val = 0;

		for (int i = (start + 63); i >= start + 1; i--){
			val += arr[i] * (int)(Math.pow(2, start + 63 - i));
		}

		if (arr[start] == 1)
			val *= -1;

		//		System.out.println("Inside byteArrayAsLong: " + val);

		return val;
	}

	public void insertNode(long kSeq){


		BTreeNode theRoot = root;
//		System.out.println("Compare Root: " + theRoot.objects.size() + " ? " + theRoot.currentObjects);
		if (theRoot.currentObjects == (degree * 2 - 1)){

			BTreeNode s = new BTreeNode(-1);
			this.root = s;
			s.leaf = false;
			s.currentObjects = 0;
			s.childNodeLocations.add(theRoot.selfNodeLocation);
//			System.out.println("Inside insertNode :" + s.childNodeLocations.size());
			splitChild(s,1);
			insertNonFull(s,kSeq);
//			System.out.println("Inside insertNode :" + s.childNodeLocations.size());
		}
		else {
			insertNonFull(theRoot,kSeq);
		}


	}

	public void splitChild(BTreeNode xParent, int iXChild){
//		System.out.println("Inside splitChild: " + iXChild);
//		System.out.println("x's count: " + xParent.currentObjects);

		
		
		// To be the Right Child after Split
		BTreeNode zRightChild = new BTreeNode(xParent.selfNodeLocation);
		// To be the Left Child after Split
		BTreeNode yLeftChild = getNode(xParent.childNodeLocations.get(iXChild-1));
		yLeftChild.parentNodeLocation = xParent.selfNodeLocation;

		
		zRightChild.leaf = yLeftChild.leaf;
		
//		System.out.println(xParent);
//		System.out.println(yLeftChild);
//		System.out.println(zRightChild);
		
//		System.out.println("Is right child a leaf? " + zRightChild.leaf);
		zRightChild.currentObjects = degree - 1;

		for (int i = 1; i < degree ; i++){
//			System.out.println("There are " + yLeftChild.objects.size() + " objects in left Before");
			zRightChild.objects.add(yLeftChild.objects.remove(degree));
//			System.out.println("There are " + yLeftChild.objects.size() + " objects in left After");
		}

		if (!yLeftChild.leaf){
			for (int i = 1; i <= degree; i++){
				zRightChild.childNodeLocations.add(yLeftChild.childNodeLocations.remove(degree));
//				System.out.println("z#child: " + zRightChild.childNodeLocations.size() + "  y#child: " + yLeftChild.childNodeLocations.size());
			}
		}

		yLeftChild.currentObjects = degree - 1;

		//This below is part of the pseudo code i can split because I'm using an arrayList
		//		for (int i = xNodeToSplit.currentObjects + 1; i > iXChild + 1){
		//			
		//		}

		xParent.childNodeLocations.add(iXChild, zRightChild.selfNodeLocation );
//		System.out.println("Inside splitChild #children: " + xParent.childNodeLocations.size() + " atIndex: " + iXChild);

		//Skipping part of pseudo code because of ArrayList implementation
		xParent.objects.add(iXChild - 1, yLeftChild.objects.remove(degree-1));

		xParent.currentObjects++;

		yLeftChild.writeNode();
		zRightChild.writeNode();
		xParent.writeNode();


	}

	private void insertNonFull(BTreeNode xNode, long kSeq){
		int i = xNode.currentObjects;
		//		System.out.println("Inside insertNonFull objects size: " + xNode.objects.size());

		if (xNode.leaf){

			while (i >= 1 && kSeq <= xNode.objects.get(i-1).sequence){
				if (kSeq == xNode.objects.get(i-1).sequence){
					System.out.println("A value was equal");
					xNode.objects.get(i-1).frequency++;
					break;
				}
				//				System.out.println("inside loop: " + i);
				i--;
			}
			//			if (xNode.objects.size() != 0) System.out.println("compare values kSeq:" + kSeq + "  seq:" + xNode.objects.get(0).sequence);


			//			System.out.println(kSeq);
			//xNode.addTreeObject(i, kSeq);
			xNode.objects.add(i,xNode.getTreeObject(kSeq));
			xNode.currentObjects++;
			System.out.println("right after add objectsSize: " + xNode.objects.size() + "  currObj: " + xNode.currentObjects);
			
			
			xNode.writeNode();

		}
		else {
			while(i >= 1 && kSeq <= xNode.objects.get(i-1).sequence){
				if (kSeq == xNode.objects.get(i-1).sequence){
					System.out.println("A value was equal");
					xNode.objects.get(i-1).frequency++;
					break;
				}
				i--;
			}
			i++;

//			System.out.println("in insertNonFull size of childLocations: " + xNode.childNodeLocations.size());
//			System.out.println(xNode);
			
			// TODO HERE LIES A PROBLEM
			BTreeNode child = getNode(xNode.childNodeLocations.get(i-1));
			if (child.currentObjects == (degree * 2 - 1)){
				splitChild(xNode,i);
				if (kSeq > xNode.objects.get(i-1).sequence)
					i++;
			}
			insertNonFull(getNode(xNode.childNodeLocations.get(i-1)), kSeq);

		}

	}

	public void printBTree(){
		
		System.out.println("ROOT: " + root);
		printChildNodes(root.childNodeLocations, 1);
		
	}
	
	private void printChildNodes(ArrayList<Integer> children, int tabs){
		
		for (int i : children){
			BTreeNode b = getNode(i);
			System.out.println("[DEPTH=" + (tabs)+ "] " + b);
			printChildNodes(b.childNodeLocations, tabs + 1);
		}
		
		
		
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
		BTree theTree = new BTree (3 ,3);
		System.out.println();

		//		theTree.insertNode(23L);
		//				System.out.println(theTree.root.objects.get(0));
		//		theTree.insertNode(23L);
		//		theTree.insertNode(65L);
		for (int i = 1; i < 128; i++){
//			System.out.println("Adding " + i  + "L");
			theTree.insertNode((long)(i % 64));
		}
		theTree.printBTree();
		
		
		//		BTreeNode thRoot = theTree.insertNode(63L);
		//		theTree.root.writeNode();
		//						byte[] bq = asByteArray(63L);
		//						for (int i = 0; i < bq.length; i++){
		//							System.out.print(bq[i]);
		//						}
		//						System.out.println();





		//		for (BTreeNode.TreeObject t : theTree.root.objects){
		//			System.out.println(t);
		//		}



		//		
		//theTree;

		// Testing of filename Constructor
		//		BTree aTree = new BTree ("filename.gbk.btree.data.3.3");
		//		System.out.println();
		//		ArrayList<BTreeNode.TreeObject> obj2 = aTree.root.objects;
		//		for (BTreeNode.TreeObject t : obj2){
		//			System.out.println(t);
		//		}

	}








	public class BTreeNode{

		public int currentObjects; //number of objects in Node
		public int parentNodeLocation; // location of parent Node
		public boolean leaf;

		public ArrayList<Integer> childNodeLocations;
		public ArrayList<TreeObject> objects;

		//		public int[] childNodeLocations; // array of child node locations
		//		public TreeObject[] objects; // objects in node
		public int selfNodeLocation;



//		/**
//		 * Constructor for new Node given the sequence as a long, only for first head? 
//		 * This might not get used.
//		 * 
//		 * @param parentLoc 
//		 * @param seq long representing a sequence
//		 */
//		public BTreeNode(long seq){
//
//			//TODO only for new Heads
//
//			childNodeLocations = new ArrayList<Integer>();
//			objects = new ArrayList<TreeObject>();
//
//			//parent location, always the head
//			this.parentNodeLocation = -1;
//
//			// new node, so only one object
//			this.currentObjects = 1;
//			objects.add(new TreeObject(seq));
//
//			// this nodes location, used for writing
//			this.selfNodeLocation = numberOfNodes++;
//
//			for (TreeObject t : objects){
//				System.out.println(t);
//			}
//
//		}

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
			System.out.println("currentObjects: " + currentObjects);

			this.parentNodeLocation = byteArrayAsInt(array,32);
			//			System.out.println("parentNodeLocation: " + parentNodeLocation);

			int index = 0;


			// Read the child Locations
			for (int i = 0; i < (degree * 2); i++){
				int val = byteArrayAsInt(array,32 * index++ + 64);
				if (val != -1){
					childNodeLocations.add(val);
//					System.out.println("Inside BTreeNode found child: " + val);
				}
			}

//			System.out.println("Inside BTreeNode leaf: " + childNodeLocations.size());
			this.leaf = childNodeLocations.size() == 0;



			int start = 32 * index + 64;

			// Read the objects
			int verifyObjectCount = 0;
			for (int i = 0; i < ((degree * 2) -1); i++){
				int intval = byteArrayAsInt(array, start + i * 96);
				long longval = byteArrayAsLong(array, start + i * 96 + 32);
//				System.out.println("Obj#" + i + ": " +intval + " : " + longval);
				if (intval != -1){
					verifyObjectCount++;
					objects.add(new TreeObject(intval,longval));
				}
			}

			this.selfNodeLocation = selfLocation;

//			System.out.println("During getNode there is " + objects.size());
			
//			System.out.println("CONSTRUCT: " + this);
			


			if (currentObjects != verifyObjectCount){
				System.err.println("The current number of Objects saved in the bin is different than the number found");
				System.out.println(this);
				System.exit(1);
				
			}



		}

		/**
		 * Recursively get byte array representation of objects
		 * 
		 * @param objs
		 * @return
		 */
		private byte[] getObjectArrayBytes(ArrayList<TreeObject> objs){



			int numObjects = 1;
			byte[] currArray = objs.get(0).getByteCode();

			while (numObjects < (degree * 2 - 1)){
				if (numObjects >= currentObjects)
					currArray = concat(currArray, getNullByteCode());
				else{
					currArray = concat(currArray, objs.get(numObjects).getByteCode());
				}
				numObjects++;
			}
			
//			for (int i = 0; i < currArray.length; i++)
//				System.out.print(currArray[i]);
//			System.out.println();
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
		 * 
		 * This method writes the Node to the bin file
		 * 
		 */
		public void writeNode(){

			int bitLocation = selfNodeLocation * nodeSize + headerSize;
			byte[] result = new byte[nodeSize];

			//			System.out.println("Inside writeNode()");
			//			System.out.println("Selfnodelocation: " + selfNodeLocation);

//			System.out.println("WRITE: " + this);
			
			System.out.println("\tcurrentObjects: " + currentObjects);
			result = concat(asByteArray(currentObjects),
					concat(asByteArray(parentNodeLocation),
							concat(concatLocations(childNodeLocations),
									getObjectArrayBytes(objects))));
			//			System.out.println("inside writeNode\nresult length: " + result.length);
//			System.out.println("During write there are " + objects.size() + " objects");
			try {
				fc.position(96);
				fc.write(ByteBuffer.wrap(asByteArray(numberOfNodes)));
				fc.position(bitLocation);
				fc.write(ByteBuffer.wrap(result));
			} catch (IOException e) {

				e.printStackTrace();
			}

			
			

		}

		@Override
		public String toString() {
			
			String s = "<Node> <selfLocation: " + selfNodeLocation + ">  <leaf?: " + leaf + ">  <currentObjects: " + currentObjects + ">   <parentNodeLocation: " + parentNodeLocation + ">\n";
			
			for (TreeObject t : objects)
				s+= "Obj<" + t + ">  ";
			s += "\n";
			for (int i : childNodeLocations)
				s+= "Child:<" + i + ">  ";
//			s += "\n";
			
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
			//			System.out.println("Inside addTreeObject index: "+ index + "  object:" + objects.get(index));
		}

		/**
		 * Tree Object. Has a frequency and a sequence which is also its key value
		 * 
		 * @author Shane
		 *
		 */
		public class TreeObject implements Comparable{



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
			private long convertSeq(String seq){

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
			 * increment the frequency
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
				return "freq:" + frequency + " seq: " + getSequenceString();
			}

			private BTreeNode getOuterType() {
				return BTreeNode.this;
			}

			@Override
			public int compareTo(Object o) {

				// allow comparing of longs to the TreeObjects
				// always return this - o.sequence (i think)



				// TODO Returns a negative integer, zero, or a positive integer as this object 
				// TODO is less than, equal to, or greater than the specified object.
				return 0;
			}


		}

	}


}