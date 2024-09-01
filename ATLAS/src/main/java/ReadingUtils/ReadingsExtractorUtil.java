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
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class ReadingsExtractorUtil {
	
	private static final Logger logger = LogManager.getLogger(ReadingsExtractorUtil.class);
	
	public static byte DEL='D';
	
	public final static int chunkSize=100000;
	
	// Define number of threads
	static int numThreads = 1;
	static AtomicInteger idleThreadsCounter=new AtomicInteger(numThreads);
	// Create a thread pool with a fixed number of threads
	static final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
	static final TreeMap<Integer,Future<ResultBuffer>> futures = new TreeMap<>();
	
	public  static final void  process(final String bamFilePath, final String fastaFilePath, final String outputFile, final String region) throws Exception {
		
		final String[] regionParts = region.split(":");
		final String chromosome = regionParts[0];
		int regionStart = Integer.parseInt(regionParts[1].split("-")[0]);
		int regionEnd = Integer.parseInt(regionParts[1].split("-")[1]);
		final int numberOfAnalysedBases=regionEnd-regionStart;
		
		final TreeMap<Integer,Integer> intervals=prepareThreadIntervals(numThreads, regionStart, regionEnd);
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
	
	
    public static final void processThreadWithExecutor( final String bamFilePath,final String fastaFilePath,final byte[] referenceBases,final SamReaderFactory samReaderFactory,final String chromosome,final int regionStart,final int regionEnd,final int processId) throws Exception {
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
    

	public static final ResultBuffer processThread(final String bamFilePath,final String fastaFilePath,final byte[] referenceBases,final SamReaderFactory samReaderFactory,final String chromosome,final int regionStart,final int regionEnd,final int processId) throws Exception {
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
	
	
	public final static TreeMap<Integer,Integer> prepareThreadIntervals(final int numberOfChunks,final int regionStart,final int regionEnd){
		final TreeMap<Integer,Integer> intervals=new TreeMap<>();
		int numberOfAnalysedBases=regionEnd-regionStart;
		int chunkSize=numberOfAnalysedBases/numberOfChunks;
		int remainingChunkSize=numberOfAnalysedBases%numberOfChunks;
		int lastIntervalEnding=regionStart+chunkSize;
		for(int i=0;i<numberOfChunks;i++) {
			lastIntervalEnding=regionStart+(i+1)*chunkSize;
			intervals.put(regionStart+i*chunkSize, lastIntervalEnding);
		}
		if(remainingChunkSize>0) {
			int remainingchunkEnding=lastIntervalEnding+remainingChunkSize;
			if(remainingChunkSize<chunkSize/10) {
				intervals.put(intervals.lastKey(), remainingchunkEnding);
			}else {
				intervals.put(lastIntervalEnding, remainingchunkEnding);
			}
		}
		return intervals;
	}
	
	public static final List<Chunk> prepareChunks(final int regionStart,final int regionEnd ,final String chromosome) {
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
	

	public static final void processRecord(final SAMRecord record, final char[] chromosome, int regionStart, int regionEnd,final Map<Integer, Position> positions,final byte[] referenceBases) {
		int recordStart = record.getAlignmentStart();
        int recordEnd = record.getAlignmentEnd();
        int mappingQuality=record.getMappingQuality();
        final byte[] readBases= record.getReadBases();
        
		if ((!record.getReadUnmappedFlag() && record.getCigar().containsOperator(CigarOperator.N))) {
			return;
		}
		
		int startPos = 0;
		int endPos = record.getReadLength()-1;
		//int currentRealPosition = recordStart;
		//for debug
		int SCENARIO=0;
		//CAS 2
		if(regionEnd>=recordEnd && regionStart<=recordStart) {
			SCENARIO=2;
			startPos = 0;
			endPos = record.getReadLength()-1;
		}
		//CAS 1
		else if(regionStart<=recordEnd && regionStart>recordStart && regionEnd>recordEnd) {
			SCENARIO=1;
			startPos=record.getReadLength()-1-(recordEnd-regionStart);
			endPos = record.getReadLength()-1;
		}
		//CAS 3
		else if(regionEnd<recordEnd && regionEnd>=recordStart && regionStart<recordStart) {
			SCENARIO=3;
			startPos=0;
			endPos=regionEnd-recordStart;
		}
		//CAS 4
		else if(regionStart>=recordStart && regionEnd<=recordEnd) {
			SCENARIO=4;
			startPos=regionStart-recordStart;
			endPos=startPos+(regionEnd-regionStart);
		}
		
		// Iterate over the CIGAR string to adjust positions
		int pos=0;
		int positionTiSkipSize=0;
		int[] thresholds=new int[record.getReadLength()];
        for (CigarElement cigarElement : record.getCigar().getCigarElements()) {
            CigarOperator op = cigarElement.getOperator();
            int length = cigarElement.getLength();
            if(op==CigarOperator.D) {
            	positionTiSkipSize+=length;
            	for(int i=pos;i<pos+length;i++) {
            		thresholds[i]=-1;
            	}
            }
            else if(op==CigarOperator.I){
            	positionTiSkipSize+=length;
            	for(int i=pos;i<pos+length;i++) {
            		thresholds[i]=1;
            	}
            }
            pos+=length;
        }
        
		for (int i = startPos; i < endPos; i++) {
			int currentRealPosition=record.getReferencePositionAtReadPosition(i);
			
			if(currentRealPosition==0 || currentRealPosition<regionStart || currentRealPosition>regionEnd) {
				continue;
			}
			
			int threshold=0;
			if(positionTiSkipSize>0) {
				for(int k=0;k<i;k++) {
					threshold+=thresholds[k];
				}
			}
			
			Position p = positions.get(currentRealPosition+1);
			if (p == null) {
				p = new Position(chromosome, currentRealPosition+1, Utils.toUpperCase(referenceBases[currentRealPosition]));
				positions.put(currentRealPosition+1, p);
			}
			if(threshold>0) {
				System.out.println(i+threshold+" "+(char)readBases[i+threshold]+" pos "+(currentRealPosition+1+threshold)+" ref base:"+(char)referenceBases[currentRealPosition+threshold]);
			}
			p.addRead(Utils.toUpperCase(readBases[i+threshold]),mappingQuality);
		}
	}

}
