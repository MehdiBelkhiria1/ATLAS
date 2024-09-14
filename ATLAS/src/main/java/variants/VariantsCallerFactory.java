package variants;

public class VariantsCallerFactory {
	
	public static int default_chunkSize=100000;
	public static int default_numThreads=(Runtime.getRuntime().availableProcessors()/2)+1;
	
	public static VariantsCaller createVariantsCaller(String bamFilePath, String fastaFilePath, String outputFile, String region, Integer numThreads, Integer chunkSize, boolean viewAll) {
		if(numThreads==null) {
			numThreads=default_numThreads;
		}
		if(chunkSize==null) {
			chunkSize=default_chunkSize;
		}
		return new VariantsCaller( bamFilePath, fastaFilePath, outputFile, region, numThreads, chunkSize, viewAll);
	}
	
	public static VariantsCaller createVariantsCaller(String bamFilePath, String fastaFilePath, String outputFile, String region, boolean viewAll) {
		return new VariantsCaller( bamFilePath, fastaFilePath, outputFile, region, default_numThreads, default_chunkSize, viewAll);
	}

}
