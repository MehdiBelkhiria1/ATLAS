package examples;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ReadingUtils.VariantsCallerFactory;


public class Atlas {
	
	private static final Logger logger = LogManager.getLogger(Atlas.class);

	public static void main(String[] args) {
		
		try {
			
			final String region="chrY:61911140-61911150";
			final String bamPath="F:\\Y_chr.bam";
			final String referencePath= "F:\\chm13v2.0.fa";
			final String outputFile= "F:\\results.txt";
			
			VariantsCallerFactory.createVariantssCaller(bamPath,referencePath,outputFile,region,8,100000).process();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

}
