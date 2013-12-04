import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;




/**
 * BTree is a store class, Binary Tree with a degree set at creation. There are
 * zero of more BTreeNodes which contain at least one BTreeObject. BTree creates a 
 * large binary file it stores the node data in. The Nodes may be retrieved from the
 * Binary Tree using normal methods
 * 
 * @author Shane
 *
 */
public class BTree{
	
	private int nodeSize; // All Nodes have the same size based on degree of BTree
	private final int headerSize = 128; // size of the metadata used for BTree, Always the same
	private int sequenceLength; // the k value from handout, 3.2 Problem pg. 3, 1 - 31 
	private int degree; // the degree of BTree
	private int rootLocation; //location of root
	private int numberOfNodes; 
	private String filename; // filename of binary file
	
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
	public BTree(int sequenceLength, int degree){
		this.sequenceLength = sequenceLength;
		this.degree = degree;
		
		// This is always the same regardless of the degree
		// Node size (32) +  SequenceLength (32) + degree (32) + number of nodes (32)
		//this.headerSize = 128; // number of bits of header
		
		// Metadata size (32) + Pointer size (32) * (Parent loc + Degree + 1) + Degree * Object Size        
		this.nodeSize = 32 + 32 * (degree + 2) + 96 * degree;
		
		this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = 0;
		
		byte[] header = concat(asByteArray(nodeSize),
								concat(asByteArray(sequenceLength),
									concat(asByteArray(degree),asByteArray(numberOfNodes))));
		
		System.out.println("NodeSize = " + nodeSize + "\nHeaderSize = " + headerSize + "\nsequenceLength = " + sequenceLength);
		System.out.println("|--nodeSize--------------------||--sequenceLength--------------||--degree----------------------||--numberofnodes---------------|");
		System.out.println(header.length);
		for (int i = 0; i < header.length; i++){
		System.out.print(header[i]);
		}
		
		this.filename = "filename.gbk.btree.data." + sequenceLength + "." + degree;
		
		//TODO
		FileChannel fc = (FileChannel.open(new File(filename), READ, WRITE));
		
		try {
		      OutputStream output = null;
		      try {
		        output = new BufferedOutputStream(new FileOutputStream(filename));
		        output.write(header);
		      }
		      finally {
		        output.close();
		      }
		    }
		    catch(FileNotFoundException ex){
		      System.err.println(ex);
		    }
		    catch(IOException ex){
		    	System.err.println(ex);
		    }
		
		
	}
	
	/**
	 * Constructor of BTree given the name of a file. This is used when there is an existing binary file.
	 * 
	 * @param fileName
	 */
	public BTree(String fileName){
		
		File file = new File(fileName);
		
	    byte[] result = new byte[headerSize];
	    try {
	      InputStream input = null;
	      try {
	        int totalBytesRead = 0;
	        input = new BufferedInputStream(new FileInputStream(file));
	        
	        while(totalBytesRead < result.length){
	          int bytesRemaining = result.length - totalBytesRead;
	          //input.read() returns -1, 0, or more :
	          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
	          if (bytesRead > 0){
	            totalBytesRead = totalBytesRead + bytesRead;
	          }
	        }
	        
//	         the above style is a bit tricky: it places bytes into the 'result' array; 
//	         'result' is an output parameter;
//	         the while loop usually has a single iteration only.
	        
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (FileNotFoundException ex) {
	      System.err.println("File not found.");
	    }
	    catch (IOException ex) {
	    	System.err.println(ex);
	    }
		
		this.nodeSize = byteArrayAsInt(result,0);
		//this.headerSize = byteArrayAsInt(result,32);
		
		this.sequenceLength = byteArrayAsInt(result,32);
	     
		this.degree = byteArrayAsInt(result,64);
		
		this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = byteArrayAsInt(result,96);
		this.filename = fileName;
	    
		if (numberOfNodes > 0){
			this.root = getRoot();
		}
		
		System.out.print("seqL = " + this.sequenceLength + "\n" +
						  "degree = " + this.degree + "\n" +
						  "headerSize = " + this.headerSize + "\n" +
						  "nodeSize = " + this.nodeSize + "\n" +
						  "rootLocation = " + this.rootLocation + "\n" +
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
		return getNode(rootLocation);
	}
	
	
	/**
	 * This method returns a Node given its location in the bin file
	 * 
	 * @param location
	 * @return
	 */
	public BTreeNode getNode(int location){
		
		int bitLocation = nodeLocation(location);
		
		byte[] result = new byte[nodeSize];
		
		
		
	    try {
	      InputStream input = null;
	      try {
	        int totalBytesRead = bitLocation;
	        input = new BufferedInputStream(new FileInputStream(filename));
	        
	        while(totalBytesRead < result.length + bitLocation){
	          int bytesRemaining = result.length - totalBytesRead;
	          //input.read() returns -1, 0, or more :
	          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
	          if (bytesRead > 0){
	            totalBytesRead = totalBytesRead + bytesRead;
	          }
	        }
	        
//	         the above style is a bit tricky: it places bytes into the 'result' array; 
//	         'result' is an output parameter;
//	         the while loop usually has a single iteration only.
	       
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (FileNotFoundException ex) {
	      System.err.println("File not found.");
	    }
	    catch (IOException ex) {
	    	System.err.println(ex);
	    }
		
		//TODO: interpret byte array
	    
	    
		
		return null;
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
	 * This method concatenates an int array as a byte array, recursively
	 * 
	 * @param array
	 * @return
	 */
	private static byte[] concat(int[] array){
		
		if (array.length == 1)
			return asByteArray(array[0]);
		
		int[] end = new int[array.length -1];
		
		for (int i = 0; i < end.length; i++)
			end[i] = array[i+1];
		
		return concat(asByteArray(array[0]), concat(end));
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
		
		for (int i = start + 31; i >= start; i--){
			val += arr[i] * (int)(Math.pow(2, start + 31 - i));
		}
		
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
	private static int byteArrayAsLong(byte[] arr, long start){
		
		int val = 0;
		
		for (int i = (int) (start + 31); i >= start; i--){
			val += arr[i] * (int)(Math.pow(2, start + 31 - i));
		}
		
		return val;
	}
	
	public BTreeNode insertNode(int parent, long seq){
		

		
		BTreeNode newNode = new BTreeNode(parent,seq);
		newNode.writeNode();
		
		if (parent == -1)
			this.root = newNode;
		
		return newNode;
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
		theTree.insertNode(-1, 65L);
//		
		//theTree;
		
		// Testing of filename Constructor
//		BTree aTree = new BTree ("filename.gbk.btree.data.3.3");
		
	}
	
	
	
	

	
	
	
	public class BTreeNode{
		
		private int currentObjects; //number of objects in Node
		private int parentNodeLocation; // location of parent Node
		private int[] childNodeLocations; // array of child node locations
		private TreeObject[] objects; // objects in node
		private int selfNodeLocation;
		
		
		
		/**
		 * Constructor for new Node given the sequence as a long
		 * 
		 * @param parentLoc 
		 * @param seq long representing a sequence
		 */
		public BTreeNode(int parentLoc, long seq){
			
			//initialize arrays
			this.childNodeLocations = new int[degree + 1];
			this.objects = new TreeObject[degree];
			
			//parent location
			this.parentNodeLocation = parentLoc;
			
			
			// new node, so only one object
			this.currentObjects = 1;
			this.objects[0] = new TreeObject(seq);
			
			// this nodes location, used for writing
			this.selfNodeLocation = numberOfNodes++;
			
			for (int i = 0; i < childNodeLocations.length; i++){
				childNodeLocations[i] = -1;
			}
			
		}
		
		
		
		/**
		 * Constructor given a byte array representation of Node as read by binary file
		 * 
		 * @param array 
		 */
		public BTreeNode(byte[] array){
			
			this.childNodeLocations = new int[degree + 1];
			this.objects = new TreeObject[degree];
//			this.parentNodeLocation = parentLoc;
			
			//TODO fix
			this.currentObjects = 1;//FIX
//			this.objects[0] = obj;
			
			for (int i = 0; i < childNodeLocations.length; i++){
				childNodeLocations[i] = -1;
			}
			
		}
		
		/**
		 * Recursively get byte array representation of objects
		 * 
		 * @param objs
		 * @return
		 */
		private byte[] getObjectArrayBytes(TreeObject[] objs){
			
			if (objs.length == 1)
				return objs[0] == null? getNullByteCode() : objs[0].getByteCode();
			
			TreeObject[] end = new TreeObject[objs.length -1];
			
			for (int i = 0; i < end.length; i++)
				end[i] = objs[i+1];
			
			return concat(objs[0] == null? getNullByteCode() : objs[0].getByteCode(), getObjectArrayBytes(end));
		}
		
		public byte[] getNullByteCode(){
			return concat(asByteArray(-1),asByteArray(-1L));
		}
		
		public void writeNode(){
			
			int bitLocation = selfNodeLocation * nodeSize + headerSize;
			byte[] result = new byte[nodeSize];
			
			System.out.println("Selfnodelocation: " + selfNodeLocation);
			
			result = concat(asByteArray(currentObjects),
								concat(asByteArray(parentNodeLocation),
										concat(concat(childNodeLocations),
												getObjectArrayBytes(objects))));
			
			try {
			      OutputStream output = null;
			      try {
			    	
			        output = new BufferedOutputStream(new FileOutputStream(filename));
			        //output.
			        System.out.println("result size: " + result.length + " bitLocation: " + bitLocation + " nodeSize: " + nodeSize);
			        output.write(result,0,nodeSize);
			      }
			      finally {
			        output.close();
			      }
			    }
			    catch(FileNotFoundException ex){
			      System.err.println(ex);
			    }
			    catch(IOException ex){
			    	System.err.println(ex);
			    }
			
			for (int i = 0; i < result.length; i++){
				System.out.print(result[i]);
				}
			
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
			public TreeObject(String seq, int freq){
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
			public TreeObject(long seq, int freq){
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
				String result = "";
				
				for (int i = arr.length - 2 * sequenceLength; i < arr.length; i = i + 2){
					
					if (arr[i-1] == 0){
						if (arr[i] == 0)
							result += "A";
					    else 
							result += "C";
					}
					else {
						if (arr[i] == 0)
							result += "G";
						else
							result += "T";
					}
				}
				
				return result;
			}

			@Override
			public String toString() {
				return "" + frequency + " " + getSequenceString();
			}

			private BTreeNode getOuterType() {
				return BTreeNode.this;
			}
			
			
		}
		
	}
	
	
}