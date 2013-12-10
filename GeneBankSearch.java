import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GeneBankSearch {
        @SuppressWarnings("resource")
        public static void main(String[] args) {
                int debugLevel, cacheSize = 0;
                boolean useCache;
                String BTreeFile, query;
                ArrayList<Long> queryList = new ArrayList<Long>();

                /* Cache size and debug level are ignored, debug level set to default: 0 */
                if(args.length == 3) 
                        debugLevel = 0;
                
                /* Cache size provided. Debug level ignored, debug level set to default: 0 */
                else if(args.length == 4) {
                        /* Cache size */
                        try {
                                cacheSize = Integer.parseInt(args[3]);
                        }
                        catch(NumberFormatException e) {
                                throw new IllegalArgumentException("Please enter cache size as an int value");
                        }
                        
                        debugLevel = 0;
                }
                
                /* Cache size and debug level are provided */
                else if(args.length == 5) {
                        
                        /* Cache size */
                        try {
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
                        

                        //debug
                        if(debugLevel != 0){
                        	try{
                        		debugLevel = Integer.parseInt(args[4]);
                        	} catch (NumberFormatException e){
                        		throw new IllegalArgumentException("\ndebug must be of type int.");
                        	}
                        	if(debugLevel != 1 & debugLevel != 0)
                        		throw new IllegalArgumentException("\ndebug must be 0 or 1.");
                        }
                }
                
                /* Catch error in number of arguments provided */
                else
                        throw new IllegalArgumentException("Expected arguments: \n " +
                                        "java GeneBankSearch <0/1(no/withCache)>" +
                                        "<btree file> <query file> [<cache size>]" +
                                        "[<debug level>]");
                
                System.out.println("debug level: " + debugLevel);
                System.out.println("cache size: " + cacheSize);
                
                /* args[0] determines whether or not cache is used */
                if(Integer.parseInt(args[0]) == 0) {
                        useCache = false;
                }
                else if(Integer.parseInt(args[1]) == 1)
                        useCache = true;
                else
                        throw new IllegalArgumentException("Please make first argument '0' for no cache or '1' to use cache");

                /* BTree File name */
                BTreeFile = args[1];

                /* Query file */
                query = args[2];

                System.out.println("BTree file: " + BTreeFile);
                System.out.println("Query file: " + query);

                
                
                
                
                
                String nextLine;
                char value;
                int frequency = 0;
                long sequence = 0;
                File queryFile = null;
                long start = System.currentTimeMillis();
                BufferedReader read = null;
                
                /* Creates File */
                queryFile = new File(query);

                try {
                        read = new BufferedReader(new FileReader(queryFile));
//TODO:                int stringLength = tree.sequenceLength;
                        
                        /* While there is more to be read in the query, remove all spaces, etc */
                        while((nextLine = read.readLine()) != null) {
                                nextLine = nextLine.replaceAll("[^a-zA-Z/]","");
                                nextLine = nextLine.toLowerCase();
                                sequence = 0;
                                
                    //            System.out.println(nextLine);
                                
                                /* Checks to see if line contains an n, if not continue parsing file */
                                if(!nextLine.contains("n")) {
                                        if(nextLine.matches("^[actg]+$"))
//TODO:                                        if(nextLine.length() != )
//TODO:                                                throw new IllegalArgumentException("Query and BTree must have same string lengths");
                                        
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
                                        
//                                        System.out.println("Sequence " + sequence);
//                                        System.out.println("Line" + nextLine);
//                                        System.out.println("");
                                        queryList.add(sequence);
//TODO:                                frequency = tree.getSequence......................?;
//                                        if(frequency != 0)
//                                                System.out.println(nextLine + ":" + frequency);
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
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                        }
                }
                BTree tree = new BTree(BTreeFile);
                
//                tree.printBTree();//TODO
                
                for(Long l : queryList) {
//                	System.out.println("Value" + l);
                	BTree.BTreeNode.TreeObject querySequence = BTreeSearch(tree, tree.root, l);
                	if(querySequence != null)
                		System.out.println(querySequence);
                }
                
                if(debugLevel == 1){
        			long totalTime = System.currentTimeMillis() - start;
        			System.out.println("Total Time was "+ totalTime );
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
        		BTree.BTreeNode read = tree.getNode(xnode.childNodeLocations.get(i-1));
        		return BTreeSearch(tree, read, ksequence);
        	}
        }
}
