package variants;

public class VariantsCallerFactory {
	
	public static int default_chunkSize=100000;
	public static int default_numThreads=(Runtime.getRuntime().availableProcessors()/2)+1;
	
	public static VariantsCaller createVariantssCaller(String bamFilePath, String fastaFilePath, String outputFile, String region, Integer numThreads, Integer chunkSize) {
		if(numThreads==null) {
			numThreads=default_numThreads;
		}
		if(chunkSize==null) {
			chunkSize=default_chunkSize;
		}
		return new VariantsCaller( bamFilePath, fastaFilePath, outputFile, region, numThreads, chunkSize);
	}
	
	public static VariantsCaller createVariantssCaller(String bamFilePath, String fastaFilePath, String outputFile, String region) {
		return new VariantsCaller( bamFilePath, fastaFilePath, outputFile, region, default_numThreads, default_chunkSize);
	}

}
