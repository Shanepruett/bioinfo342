import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GeneBankSearch {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		int debugLevel, cacheSize = 0;
		boolean useCache = false;
		String BTreeFile, query;
		ArrayList<Long> queryList = new ArrayList<Long>();

		/* Cache size and debug level are ignored, debug level set to default: 0 */
		if(args.length == 3) {
			debugLevel = 0;

			if(Integer.parseInt(args[0]) == 1)
				throw new IllegalArgumentException("If using a cache, please provide cache size");
		}

		else if(args.length == 4 && Integer.parseInt(args[0]) == 1) {
			try{
				useCache = true;
				cacheSize = Integer.parseInt(args[3]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}
			debugLevel = 0;
		}

		/* Cache size provided. Debug level ignored, debug level set to default: 0 */
		else if(args.length == 4 && Integer.parseInt(args[0]) == 0) {
			/* Cache size */
			try {
				if(Integer.parseInt(args[3]) == 0 || Integer.parseInt(args[3]) == 1)
					debugLevel = Integer.parseInt(args[3]);

				else
					throw new IllegalArgumentException("Please enter value 0 or 1 for debugLevel");
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter debugLevel as an int value");
			}
		}

		/* Cache size and debug level are provided */
		else if(args.length == 5 && Integer.parseInt(args[0]) == 1) {

			/* Cache size */
			try {
				useCache = true;
				cacheSize = Integer.parseInt(args[3]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}

			/* Debug level */
			try{
				debugLevel = Integer.parseInt(args[4]);
			}
			catch (NumberFormatException e){
				throw new IllegalArgumentException("Please enter debug level as an int value: 0 or 1");
			}

			if(debugLevel != 1 || debugLevel != 0)
				throw new IllegalArgumentException("\ndebug must be 0 or 1.");
		}

		/* Catch error in number of arguments provided */
		else
			throw new IllegalArgumentException("Expected arguments: \n " +
					"java GeneBankSearch <0/1(no/withCache)>" +
					"<btree file> <query file> [<cache size>]" +
					"[<debug level>]");

		/* BTree File name */
		BTreeFile = args[1];

		/* Query file */
		query = args[2];                

		String nextLine = "";
		char value;
		long sequence = 0;
		File queryFile = null;
		long start = System.currentTimeMillis();
		BufferedReader read = null;

		/* Creates File */
		queryFile = new File(query);
		int seqL = 0;
		
		try {
			read = new BufferedReader(new FileReader(queryFile));

			/* While there is more to be read in the query, remove all spaces, etc */
			while((nextLine = read.readLine()) != null) {
				nextLine = nextLine.replaceAll("[^a-zA-Z/]","");
				nextLine = nextLine.toLowerCase();
				if (seqL == 0) seqL = nextLine.length();
				if (nextLine.length() != seqL){
					System.err.println("Not all query lengths are the same in the query file.");
					System.exit(1);
				}
				sequence = 0;

				/* Checks to see if line contains an n, if not continue parsing file */
				if(!nextLine.contains("n")) {
					if(nextLine.matches("^[actg]+$"))
						for(int i = 0; i < nextLine.length(); i++) {

							value = nextLine.charAt(i);

							if(value == 'a')
								sequence = sequence * 4 + 0b00;
							else if(value == 'c')
								sequence = sequence * 4 + 0b01;
							else if(value == 't')
								sequence = sequence * 4 + 0b11;
							else if(value == 'g')
								sequence = sequence * 4 + 0b10;
						}
					queryList.add(sequence);
				}
				else throw new IOException("File provided is formatted incorrectly.");
			}
		}

		catch(IOException e) {
			try {
				read.close();
			}
			catch(IOException f) {
				f.printStackTrace();
			}
			
			try {
				throw new IOException("File is not working properly. Please check that file" +
						"exists and is properly formatted.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		BTree tree = new BTree(BTreeFile);

		if (tree.getSequenceLength() != seqL) {
			System.err.println("Sequence length is not equal to query object length");
			System.exit(0);
		}
			
		if(useCache)
			tree.useCache(cacheSize);

		for(Long l : queryList) {
			BTree.BTreeNode.TreeObject querySequence = BTreeSearch(tree, tree.root, l);
			if(querySequence != null)
				System.out.println(querySequence);
		}
	} 

	public static BTree.BTreeNode.TreeObject BTreeSearch(BTree tree, BTree.BTreeNode xnode, Long ksequence) {
		int i = 1;

		while(i <= xnode.currentObjects && ksequence > xnode.objects.get(i-1).getSequence()) {
			i++;
		}
		
		if(i <= xnode.currentObjects && ksequence == xnode.objects.get(i-1).getSequence()) {
			return xnode.objects.get(i-1);
		}
		else if(xnode.leaf) {
			return null;
		}
		else {
			BTree.BTreeNode read = tree.retrieveNode(xnode.childNodeLocations.get(i-1));
			return BTreeSearch(tree, read, ksequence);
		}
	}
}
