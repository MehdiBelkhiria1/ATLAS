package ReadingUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Utils.Utils;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class VariantsCaller {
	
	private static final Logger logger = LogManager.getLogger(VariantsCaller.class);
	
	private int chunkSize=100000;
	
	private int numThreads = 1;
	private String bamFilePath;
	private String fastaFilePath; 
	private String outputFile; 
	private String region;
	private AtomicInteger idleThreadsCounter=null;
	private ExecutorService executor = null;
	
	public VariantsCaller() {
		
	}
	public VariantsCaller(String bamFilePath, String fastaFilePath, String outputFile, String region, int numThreads, int chunkSize) {
		this.numThreads=numThreads;
		this.chunkSize=chunkSize;
		this.bamFilePath=bamFilePath;
		this.fastaFilePath=fastaFilePath; 
		this.outputFile=outputFile; 
		this.region=region;
		
		this.idleThreadsCounter=new AtomicInteger(numThreads);
		this.executor = Executors.newFixedThreadPool(numThreads);
	}
	
	static final TreeMap<Integer,Future<ResultBuffer>> futures = new TreeMap<>();
	
	public final void  process() throws Exception {
		
		final String[] regionParts = region.split(":");
		final String chromosome = regionParts[0];
		int regionStart = Integer.parseInt(regionParts[1].split("-")[0]);
		int regionEnd = Integer.parseInt(regionParts[1].split("-")[1]);
		final int numberOfAnalysedBases=regionEnd-regionStart;
		
		final TreeMap<Integer,Integer> intervals= Utils.prepareThreadIntervals(numThreads, regionStart, regionEnd);
		final long startTime = System.nanoTime();
		try (ReferenceSequenceFile referenceSequenceFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(new File(fastaFilePath));) {
		    final SamReaderFactory samReaderFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
	        samReaderFactory.setOption(SamReaderFactory.Option.CACHE_FILE_BASED_INDEXES, true);
	        samReaderFactory.setOption(SamReaderFactory.Option.EAGERLY_DECODE, true);
	        
			System.out.println("Reference file opened succefully");
			System.out.println(fastaFilePath);
			// **********
			final ReferenceSequence referenceSequence = referenceSequenceFile.getSequence(chromosome);
			final byte[] referenceBases = referenceSequence.getBases();
			for (Map.Entry<Integer, Integer> entry : intervals.entrySet()) {
				processThreadWithExecutor(bamFilePath, fastaFilePath, referenceBases, samReaderFactory, chromosome, entry.getKey(), entry.getValue(), entry.getKey());
			}
		}catch(Exception e) {
			logger.error(e.getMessage());
			throw e;
		}finally {
			executor.shutdown();
		}
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
			final ResultBuffer result=new ResultBuffer();
			for (Map.Entry<Integer, Future<ResultBuffer>> entry : futures.entrySet()) {
				ResultBuffer r=entry.getValue().get();
				result.result.append(r.result);
				result.totalReads+=r.totalReads;
				result.totalSnps+=r.totalSnps;
		    }
			
			String averageReadDepth=String.format("%.2f",((double)result.totalReads/(double)numberOfAnalysedBases));
			String summaryString="*****************SUMMARY******************:\n"+
			                     "Analysed bases:" + (numberOfAnalysedBases)+" Region:"+region+"\n"+
			                     "Avg. Read Depth= X" + averageReadDepth+"\n"+
					             "SNPs found=" +result.totalSnps+"\n"+
					             "******************************************:\n";
			writer.write(summaryString);
			writer.write(result.result.toString());
		}

		long endTime = System.nanoTime();
        double executionTimeSeconds = (double) (endTime - startTime) / 1_000_000_000; // Convert nanoseconds to seconds
        System.out.println("Execution time: " + executionTimeSeconds + " seconds");
        System.out.println("Idle Threads: " + idleThreadsCounter.longValue());
		
	}
	
	
    private final void processThreadWithExecutor( final String bamFilePath,final String fastaFilePath,final byte[] referenceBases,final SamReaderFactory samReaderFactory,final String chromosome,final int regionStart,final int regionEnd,final int processId) throws Exception {
    	Future<ResultBuffer> future = executor.submit(() ->{
			ResultBuffer resultBuffer=null;
			try {
				resultBuffer= processThread( bamFilePath,  fastaFilePath, referenceBases, samReaderFactory, chromosome, regionStart, regionEnd, processId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return resultBuffer;
		});
		futures.put(regionStart,future);
	}
    

	private final ResultBuffer processThread(final String bamFilePath,final String fastaFilePath,final byte[] referenceBases,final SamReaderFactory samReaderFactory,final String chromosome,final int regionStart,final int regionEnd,final int processId) throws Exception {
		idleThreadsCounter.decrementAndGet();
		final ResultBuffer result=new ResultBuffer();
		try (SamReader samReader = samReaderFactory.open(new File(bamFilePath))) {
			
			System.out.println("BAM file opened succefully");
			System.out.println(samReader.getResourceDescription());
			
			// ***preparing chunks of fixed chunkSize to avoid high memory footprint
			List<Chunk>	chunks=prepareChunks(regionStart,regionEnd ,chromosome);
			
			int loopSize=chunks.size();
			for(int i=0;i<loopSize;i++) {
				Chunk chunk=chunks.get(i);
				chunk.process(samReader, referenceBases);
				result.totalReads+=chunk.readCount;
				result.totalSnps+=chunk.snps.size();
				for(Position p:chunk.snps){
					result.result.append(p.toStringBuilder());
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			idleThreadsCounter.incrementAndGet();
			System.out.println("Thread finished processing");
		}
		return result;
	}

	
	private final List<Chunk> prepareChunks(final int regionStart,final int regionEnd ,final String chromosome) {
		final int numberOfAnalysedBases=regionEnd-regionStart;
		List<Chunk> chunks=new ArrayList<Chunk>();
		int nchunks=numberOfAnalysedBases/chunkSize;
		int lastChunkSize=numberOfAnalysedBases%chunkSize;
		if(nchunks>1) {
			int lastProcessedRegionEnd=0;
			for(int i=0;i<nchunks;i++) {
				int processRegionStart=regionStart+i*chunkSize;
				int processRegionEnd=processRegionStart+chunkSize;
				lastProcessedRegionEnd=processRegionEnd;
				chunks.add(new Chunk(processRegionStart,processRegionEnd,chromosome));
			}
			chunks.add(new Chunk(lastProcessedRegionEnd,lastProcessedRegionEnd+lastChunkSize,chromosome));
		}else {
			chunks.add(new Chunk(regionStart,regionEnd,chromosome));
		}
		return chunks;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	public int getNumThreads() {
		return numThreads;
	}
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	public String getBamFilePath() {
		return bamFilePath;
	}
	public void setBamFilePath(String bamFilePath) {
		this.bamFilePath = bamFilePath;
	}
	public String getFastaFilePath() {
		return fastaFilePath;
	}
	public void setFastaFilePath(String fastaFilePath) {
		this.fastaFilePath = fastaFilePath;
	}
	public String getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
	

}
