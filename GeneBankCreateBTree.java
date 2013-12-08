import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.File;


public class GeneBankCreateBTree {
	public static int debugLevel, cacheSize, degree, seqLength;
	public static boolean useCache;
	public static String gbk;
	
	private static void readArgs(String[] args) throws IllegalArgumentException {
		if(args.length == 4)
			debugLevel = 0;
		else if(args.length == 5) {
			try{
				cacheSize = Integer.parseInt(args[4]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}
			debugLevel = 0;
		}
		
		else if(args.length == 6) {
			try {
				cacheSize = Integer.parseInt(args[4]);
			}
			catch(NumberFormatException e) {
				throw new IllegalArgumentException("Please enter cache size as an int value");
			}
			if(debugLevel != 0 && debugLevel != 1)
				throw new IllegalArgumentException("Debug level must be either 0 or 1");
		}
		else
			throw new IllegalArgumentException("Expected arguments: \n " +
					"java GeneBankSearch <0/1(no/withCache)>" +
					"<degree> <gbk file> <sequence length> [<cache size>]" +
					"[<debug level>]");
		
		if(Integer.parseInt(args[0]) == 0)
			useCache = false;
		else if(Integer.parseInt(args[0]) == 1) {
			useCache = true;
		}
		else
			throw new IllegalArgumentException("Please make first argument '0' for no cache or '1' to use cache");
		if(args[1] == "0")
			//TODO : figure this out;
		try {
			degree = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("Please enter degree as an int value");
		}
		gbk = args[2];
		seqLength = Integer.parseInt(args[3]);
		if(seqLength < 1 || seqLength > 31)
			throw new IllegalArgumentException("seqLength must be between 1 and 31, inclusive");
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String line = null, sequence = null, range;
		long value, start = System.currentTimeMillis();
		char x;
		boolean readIn = false;
		
		readArgs(args);
		File input;
				
		input = new File(gbk);
		/*try {
			input = new File(gbk);
		} 
		catch (FileNotFoundException e) {
			throw new FileNotFoundException("File " + gbk + " could not be found");
		}*/
		
		BufferedReader read = new BufferedReader(new FileReader(input/*, Charset.forName("UTF-8")*/));
		
		try {
			BTree tree = new BTree(seqLength, degree, gbk);
			line = read.readLine();
			while(line != null) {
			System.out.println("inside While");
				line = line.toLowerCase();
//				line = line.replaceAll("\\s","");
				if(line.equals("origin")){
					sequence = "";
					readIn = true;
					//continue;
				}
				if(line.equals("//")) {
					System.out.println(sequence);
					readIn = false;
					int i = 0;
					while(i+seqLength <= sequence.length()) {
						
						value = 0;
						range = sequence.substring(i, i+seqLength);
						if(!range.contains("n")) {
							if(range.matches("^[actg]+$")) {
								for(int j = 0; j < range.length(); j ++){
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
							}
							else
								throw new IOException(gbk + "is not properly formatted");
						}
						i++;
					}
					continue;
				}
				if(readIn) {
					line = line.replaceAll("[0-9] ", "");
					sequence = sequence + line;
				}
				line = read.readLine();
			}
		}	
		catch(IOException e) {
			throw new IOException(gbk + " not accessible or not formatted properly");
		}
			try {
				read.close();
				System.out.println("Closing");
			}
			catch(IOException ea) {
				ea.printStackTrace();	
			}
			
	
		
		if(debugLevel == 2) {
			long time = System.currentTimeMillis()-start;
			System.out.println("Total time: " + time);
		}
	}
}
