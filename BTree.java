import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;




public class BTree{
	
	private int nodeSize;
	private int headerSize;
	private int sequenceLength; // the k value from handout, 3.2 Problem pg. 3, 1 - 31 
	private int degree; // the degree of BTree
	private int rootLocation;
	private int numberOfNodes;
	
	private boolean usingCache; 
	private int cacheSize;
	
//	private int debugLevel;
	
	private BTreeNode root;
	
	
	public BTree(int sequenceLength, int degree, boolean usingCache){
		this.sequenceLength = sequenceLength;
		this.degree = degree;
		
		this.headerSize = 192; // number of bits of header
		// Metadata size (32) + Pointer size (32) * (Parent loc + Degree + 1) + Degree * Object Size        
		this.nodeSize = 32 + 32 * (degree + 2) + 96 * degree;
		
		this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = 0;
		
		byte[] header = concat(asByteArray(nodeSize),
							concat(asByteArray(headerSize),
								concat(asByteArray(sequenceLength),
									concat(asByteArray(degree),asByteArray(numberOfNodes)))));
		
		System.out.println("NodeSize = " + nodeSize + "\nHeaderSize = " + headerSize + "\nsequenceLength = " + sequenceLength);
		System.out.println("|--nodeSize--------------------||--headerSize------------------||--sequenceLength--------------||--degree----------------------||--numberofnodes---------------|");
		for (int i = 0; i < header.length; i++){
		System.out.print(header[i]);
		}
		
		try {
		      OutputStream output = null;
		      try {
		        output = new BufferedOutputStream(new FileOutputStream("filename.gbk.btree.data." + sequenceLength + "." + degree));
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
	
	public BTree(String fileName){
		
		
		
		File file = new File(fileName);
		
	    byte[] result = new byte[(int)file.length()];
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
	        /*
	         the above style is a bit tricky: it places bytes into the 'result' array; 
	         'result' is an output parameter;
	         the while loop usually has a single iteration only.
	        */
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
		this.headerSize = byteArrayAsInt(result,32);
		
		this.sequenceLength = byteArrayAsInt(result,64);
	     
		this.degree = byteArrayAsInt(result,96);
		
		this.rootLocation = headerSize; // root is first Node
		this.numberOfNodes = byteArrayAsInt(result,128);
	    
		System.out.print("seqL = " + this.sequenceLength + "\n" +
						  "degree = " + this.degree + "\n" +
						  "headerSize = " + this.headerSize + "\n" +
						  "nodeSize = " + this.nodeSize + "\n" +
						  "rootLocation = " + this.rootLocation + "\n" +
						  "numberOfNodes = " + this.numberOfNodes);
		
		
//		for (int i = 0; i < result.length; i++){
//		System.out.print(result[i]);
//		}
		
	}
	
	
	/**
	 * This method takes the simple location value of the Node and returns the location of which it can be
	 * found in the bin file.
	 * 
	 * @param location the integer value used to represent the location of a Node, range from value 1 - number of nodes
	 * @return return the integer location in the binary file in which the Node begins, bitlocation
	 * 
	 */
	private int NodeLocation(int location){
		// root location has a value of 0 for its location plus the headerSize
		return location * nodeSize + headerSize;
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
	 * This method converts an integer to a byte array binary representation
	 * 
	 * @param value integer to convert to byte array
	 * @return byte array representation
	 */
	private static byte[] asByteArray (int value){
		
		int val = value;
		byte[] arr = new byte[32];
		
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
		
		for (int i = 63; i > 0; i--){
			arr[i] = (byte) (val % 2);
			val /= 2;
		}
		return arr;
	}
	
	private static int byteArrayAsInt(byte[] arr, int start){
		
		int val = 0;
		
		for (int i = start + 31; i >= start; i--){
			val += arr[i] * (int)(Math.pow(2, start + 31 - i));
		}
		
		
		return val;
	}
	
	
	public static void main(String[] args) throws IOException {
		
		// Testing of asByteArray (int)
//		byte[] x = asByteArray(234);
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
		BTree theTree = new BTree (3 ,3 , false);
		System.out.println();
		
		// Testing of filename Constructor
		BTree aTree = new BTree ("filename.gbk.btree.data.3.3");
		
	}
	
	
	
	
	private BTreeNode GetNode(int bitLocation){
		
		
		
		
		
		return null;
	}
	
	
	
	public class BTreeNode{
		
		public BTreeNode(){
			
			
		}
		
		
	}
	
	
}