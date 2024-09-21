package variants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import utils.Utils;

public class Chunk {
	
	private static final Logger LOGGER = LogManager.getLogger(Chunk.class);
	
	int regionStart;
	int regionEnd;
	long readCount;
	String chromosome;
	List<Position> snps=new ArrayList<>();
	
	public Chunk() {}

	public Chunk(int regionStart, int regionEnd, String chromosome) {
		super();
		this.regionStart = regionStart;
		this.regionEnd = regionEnd;
		this.chromosome = chromosome;
	}
	
	public final void  process(final SamReader samReader, final byte[] referenceBases, boolean viewAll)throws Exception{
		char[] chr=chromosome.toCharArray();
		LinkedHashMap<Integer, Position> positions=new LinkedHashMap<>(regionEnd-regionStart);
		SAMRecordIterator iter=null;
		try {
			try {
				iter = samReader.query(chromosome, regionStart, regionEnd, false);
				while (iter.hasNext()) {
					final SAMRecord rec = iter.next();
					Utils.processRecord(rec, chr, regionStart, regionEnd, positions, referenceBases);
				}
			}catch(Exception e) {
				LOGGER.error(e.getMessage());
				throw e;
			}
			long totalReadCount=0l;
			for (Map.Entry<Integer,Position> entry : positions.entrySet()) {
				Position p = entry.getValue();
				p.processVariants();
				
				if(viewAll) {
					this.snps.add(p);
				}else if(p.variantDetected) {
					this.snps.add(p);
				}
				
				totalReadCount+=p.total_readings;
			}
			this.readCount=totalReadCount;
		}catch(Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}finally {
			if(iter!=null) {
				iter.close();
			}
		}
	}

}
