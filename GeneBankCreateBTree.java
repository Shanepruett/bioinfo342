import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class GeneBankCreateBTree {

	public static void main(String[] args) throws IOException {
		int debugLevel = 0, cacheSize = 0, degree = 0, seqLength;
		boolean useCache =false;
		//Cache<BTree.BTreeNode> cache = null;
		
		String gbk;

		/* No cache size or debug level provided */
		if(args.length == 4) {
			debugLevel = 0;
			if(Integer.parseInt(args[0]) == 1)
				throw new IllegalArgumentException("If using a cache, please provide cache size");
		}
		/* 5 arguments provided, using cache which means that cache size is provided */
		else if(args.length == 5 && Integer.parseInt(args[0]) == 1) {
			try{
				cacheSize = Integer.parseInt(args[4]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}
			debugLevel = 0;
		}
		/* 5 arguments provided, not using cache which means debug level provided */
		else if(args.length == 5 && Integer.parseInt(args[0]) == 0) {
			try{
				if(Integer.parseInt(args[4]) == 0 || Integer.parseInt(args[4]) == 1)
					debugLevel = Integer.parseInt(args[4]);
				else
					throw new IllegalArgumentException("Please enter value 0 or 1 for debugLevel");
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter debugLevel as an int value");
			}
		}

		else if(args.length == 6) {
			try {
				cacheSize = Integer.parseInt(args[4]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}
			try{
				debugLevel = Integer.parseInt(args[5]);
			} catch (NumberFormatException e){
				throw new IllegalArgumentException("\ndebug must be of type int.");
			}
			if(debugLevel != 1 && debugLevel != 0)
				throw new IllegalArgumentException("\ndebug must be 0 or 1.");
		}
		else
			throw new IllegalArgumentException("Expected arguments: \n " +
					"java GeneBankSearch <0/1(no/withCache)>" +
					"<degree> <gbk file> <sequence length> [<cache size>]" +
					"[<debug level>]");

		if(Integer.parseInt(args[0]) == 0)
			useCache = false;
		else if(Integer.parseInt(args[0]) == 1)
			useCache = true;
		else
			throw new IllegalArgumentException("Please make first argument '0' for no cache or '1' to use cache");

		seqLength = Integer.parseInt(args[3]);
		if(seqLength < 1 || seqLength > 31)
			throw new IllegalArgumentException("seqLength must be between 1 and 31, inclusive");

		try {
			degree = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("Please enter degree as an int value");
		}
		
		if(Integer.parseInt(args[1]) == 0) {
			degree = 16;
//			System.out.println("Degree: " + degree);
		}
		
		gbk = args[2];

		boolean found;



		String line = "", sequence = "", range;
		long value, start = System.currentTimeMillis();
		char x;
		boolean readIn = false;
		File input;
		//                BTree tree = new BTree(seqLength, degree, gbk);
		//                System.out.println("Sequence length: " + seqLength);
		//                System.out.println("Degree: " + degree);
		//                System.out.println("File: " + gbk);

		input = new File(gbk);

		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(input));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String> sequenceList = new ArrayList<String>();
		try {
			line = read.readLine();
			while(line != null) {
				line = line.toLowerCase();
				line = line.replaceAll("[^a-zA-Z/]","");
				if(line.contains("origin")){
//					System.out.println("inside contains origin");
					sequence = "";//BIGCHANGE
					line = read.readLine();
					line = line.replaceAll("[^a-zA-Z/]","");
					readIn = true;
					//continue;
				}
				if(line.equals("//")) {
//					System.out.println(sequence);
					readIn = false;
					sequenceList.add(sequence);
//					System.out.println("Sequence length: " + seqLength);
//					System.out.println("sequence.length():" + sequence.length());
				}
				if(readIn) {
					line = line.replaceAll("[0-9] ", "");
					sequence = sequence + line;
				}
				line = read.readLine();
			}
		}        
		catch(IOException e) {
			try {
				throw new IOException(gbk + " not accessible or not formatted properly");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		try {

			read.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("Closing");

		BTree tree = new BTree(seqLength, degree, gbk);
		
		if(useCache){
			tree.useCache(cacheSize);
//			cache = new Cache<BTree.BTreeNode>(cacheSize);
			System.out.println("using cache!!");
		}
		
//		System.out.println("Sequence length: " + seqLength);
//		System.out.println("Degree: " + degree);
//		System.out.println("File: " + gbk);

		for (String s : sequenceList){
			int i = 0;
			while(i+seqLength <= s.length()) {
				value = 0;
				range = s.substring(i, i+seqLength);
				if(!range.contains("n")) {
					if(range.matches("^[actg]+$")) {
						for(int j = 0; j < range.length(); j++){
							x = range.charAt(j) ;
							if(x == 'a')
								value = value * 4 + 0b00;
							else if(x == 'c')
								value = value * 4 + 0b01;
							else if(x == 'g')
								value = value * 4 + 0b10;
							else if(x == 't')
								value = value * 4 + 0b11;
						}
						tree.insertNode(value);
						/* If we want to use cache and the cache is created (size > 0) */
//						if (useCache && cache.cache.size() > 0){
//							found = false;
//							/* For all spaces in cache */
//							for (int fIndex = 0; fIndex < cache.cache.size(); fIndex++){
//								
//								/* bt node is created from cache at fIndex */
//								BTree.BTreeNode bt = (BTree.BTreeNode) cache.cache.get(fIndex);
//								
//								/* For all bt objects */
//								for (int sIndex = 0; sIndex < bt.objects.size(); sIndex++){
//									BTree.BTreeNode.TreeObject t = bt.objects.get(sIndex);
//									if (value == t.getSequence()){
//	//								System.out.println("A VALUE IS EQUAL: " + t.getSequence() + "=" + value + " f:" + t.getFreq());
//										t.incrementFreq();
//										//                                                                        System.out.println(" FA: " + t.getFreq());
//										cache.getObject(bt);
//										found = true;
//										break;
//										//bt.writeNode();
//									}
//									else {
//										BTree.BTreeNode obj = (BTree.BTreeNode) cache.addObject(tree.insertNode(value));
//										//                                                                        System.out.println("CacheSize: " + cache.cache.size());
//										obj.writeNode();
//									}
//								}
//								if (found){
////									System.out.println("Out of loops");
//									break;
//								}
//							}
//						}
//						else{
//							if (useCache){
//								cache.addObject(tree.insertNode(value));
////								System.out.println("CacheSize: " + cache.cache.size());
//							}
//							else{
//								
//							}
//						}

					}
					else
						throw new IOException(gbk + "is not properly formatted");
				}
				i++;
			}
		}

		if (useCache) tree.finishBTree();
		
//		tree.printBTree();
		System.out.println(debugLevel);
		if(debugLevel == 1){
//			System.out.println("RECURSIVE RESULTS");
////			tree.printBTree();
//			System.out.println(tree.printBTree());
			BufferedWriter writer = null;
			try{
				File dump = new File("dump");
				writer = new BufferedWriter(new FileWriter(dump));
				tree.printBTree(writer);
				System.out.println("DEBUG DONE");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					/* Close the writer regardless of what happens... */
					writer.close();
				} catch (Exception e) {
				}
			}
		}
//		System.out.println("Number of Nodes: " + tree.numberOfNodes);
//		tree.writeBTree();
	}
}
