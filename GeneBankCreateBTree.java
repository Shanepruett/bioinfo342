import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class GeneBankCreateBTree {

	public static void main(String[] args) {
		int debugLevel = 0, cacheSize, degree = 0, seqLength;
		boolean useCache;
		String gbk;
		
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
		
		
		
		

		String line = "", sequence = "", range;
		long value, start = System.currentTimeMillis();
		char x;
		boolean readIn = false;
		File input;
		
		input = new File(gbk);
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(input));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			line = read.readLine();
			while(line != null) {
				line = line.toLowerCase();
			line = line.replaceAll("[^a-zA-Z/]","");
				if(line.contains("origin")){
					System.out.println("inside contains origin");
					sequence = "";
					line = read.readLine();
					line = line.replaceAll("[^a-zA-Z/]","");
					readIn = true;
					//continue;
				}
				if(line.equals("//")) {
					System.out.println(sequence);
					readIn = false;
					int i = 0;
					System.out.println("Sequence length: " + seqLength);
					System.out.println("sequence.length():" + sequence.length());
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
					//continue;
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
		System.out.println("Closing");
			
	
		
		if(debugLevel == 2) {
			long time = System.currentTimeMillis()-start;
			System.out.println("Total time: " + time);
		}
	}
}
