import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author: Brandon Quirarte, Shane Pruett, Stephanie Potter
 * @class: CS 342
 * @assignment: BTree
 * 
 * @description:  This class is used to search for a sequence of a specified length
 */


//Searches BTree for sequences of a specified length
//Root node must be kept in memory
//Need cache class?


//Arguments for cache, BTree file, Query file, cache size, and debug level
//Debug level is optional
	//Default is 0
//Debug levels:
	//0: The output of the queries should be printed on the standard output 
	//stream. Any diagnostic messages, help and status messages must be be 
	//printed on standard error stream


public class GeneBankSearch {
	
	static int debugLevel; //Debug level
	static int cacheSize; //Size of the cache
	static int useCache; //Used to determine if a cache is used
	static BufferedReader queryFile; //Reader for reading the query file
	
	
	
	/**
	 * Run the Search program
	 * @param args
	 */
	public static void main(String[] args){
		
		//Check if the arguments are valid.  Exit if not.
		if(!checkArguments(args)){
			printProperUsage();
			System.exit(0);
		}	
	}
	
	
	/**
	 * Check if the arguments are valid and initialize the corresponding variables
	 * @param: arguments The array of command line arguments
	 * @return: true if all arguments are valid
	 */
	public static boolean checkArguments(String[] arguments){
		
		//Check for the correct number of arguments
		if(arguments.length < 4 || arguments.length > 5){
			System.err.println("Incorrect number of arguments!\n");
			return false;
		}
		
		//Check whether to use cache or not
		try{
			
			//Check if the argument is a 0 or 1
			//Will cause error if not an integer
			if(Integer.parseInt(arguments[0]) == 0 || Integer.parseInt(arguments[0]) == 1){
				useCache = Integer.parseInt(arguments[0]);
			}
			//If not, print error
			else{
				System.err.println("Cache usage must be a 0 (not cache) or 1 (with cache)");
				return false;
			}
		}
		catch(Exception e){
			System.err.println("Cache usage must be a 0 (not cache) or 1 (with cache)");
			return false;
		}
		
		//Try opening the BTree file
//		try{
//			
//		}
//		catch(Exception e){
//			
//		}
		
		//Try opening the query file
		try{
			FileInputStream qFile = new FileInputStream(arguments[2]);
			queryFile = new BufferedReader(new InputStreamReader(qFile, Charset.forName("UTF-8")));
		}
		catch(Exception e){
			System.err.println("Query file not found");
			return false;
		}
		
		//Check if valid cache size argument is an integer and valid
		try{
			
			//Check if the argument is and integer greater than or equal to 0
			//Will cause error if not an integer
			if(Integer.parseInt(arguments[3]) >= 0){
				
				//Set cache size
				cacheSize = Integer.parseInt(arguments[3]);
			}
			//Print error if less than zero and return false
			else{
				System.err.println("Cache size must be an integer greater than or equal to 0");
				return false;
			}
		}
		catch(Exception e){
			System.err.println("Cache size must be an integer greater than or equal to 0");
			return false;
		}
		
		
		//Check if debug level is an integer and valid
		try{
			
			//Set debugLevel to 0 if no argument
			if(arguments.length == 4){
				debugLevel = 0;
			}
			//Check if the argument is an integer greater than or equal to 0
			//Will cause error if not an integer
			else if(Integer.parseInt(arguments[4])  >= 0){
				
				//Set debugLevel
				debugLevel = Integer.parseInt(arguments[4]);
			}
			//Print error if less than 0 and return false
			else{
				System.err.println("Debug level must be an integer greater than or equal to 0");
				return false;
			}	
		}
		catch(Exception e){
			System.err.println("Debug level must be an integer greater than or equal to 0");
			return false;
		}
		
		
		//If all tests pass, return true
		return true;
	}
	
	
	/**
	 * Read the BTree file.  Not sure if needed.
	 */
	public static void readBTree(){
		
	}
	
	
	/**
	 * Print the proper way to run the program via command line
	 */
	public static void printProperUsage(){
		System.out.println();
		System.err.println("Proper usage:\n");
		System.err.println("java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> "
				+ "[<cache size>] [<debug level>]");
	}
}
