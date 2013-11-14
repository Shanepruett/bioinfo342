import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GeneBankSearch {
	public static int debugLevel, cacheSize;
	public static boolean useCache;
	public static String file, query;

	@SuppressWarnings("unused")
	private static void readArgs(String[] args) throws IllegalArgumentException {
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
			if(debugLevel != 0 && debugLevel != 1)
				throw new IllegalArgumentException("Debug level must be either 0 or 1");
		}
		/* Catch error in number of arguments provided */
		else
			throw new IllegalArgumentException("Expected arguments: \n " +
					"java GeneBankSearch <0/1(no/withCache)>" +
					"<btree file> <query file> [<cache size>]" +
					"[<debug level>]");
		/* args[0] determines whether or not cache is used */
		if(args[0] == "0")
			useCache = false;
		else if(args[0] == "1")
			useCache = true;
		else
			throw new IllegalArgumentException("Please make first argument '0' for no cache or '1' to use cache");
		/* File name */
		file = args[1];
		/* Query file */
		query = args[2];
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String nextLine;
		char value;
		long sequence = 0;
		FileInputStream queryFile;
		
		try {
			queryFile = new FileInputStream(query);
		}
		catch(FileNotFoundException e) {
			throw new FileNotFoundException("File provided not found");
		}
		BufferedReader queryReader = new BufferedReader(new InputStreamReader(queryFile, Charset.forName("UTF-8")));
		
		try {
			while((nextLine = queryReader.readLine()) != null) {
				nextLine = nextLine.replaceAll("\\s", "");
				nextLine = nextLine.toLowerCase();
				
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
				}
				else throw new IOException("File provided is formatted incorrectly.");
			}
		}
		catch(IOException e) {
			try {
				queryFile.close();
			}
			catch(IOException f) {
				f.printStackTrace();
			}
			throw new IOException("File is not working properly. Please check that file" +
					"exists and is properly formatted.");
		}
	} 
}
