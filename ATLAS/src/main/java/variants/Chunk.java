package variants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import utils.Utils;

public class Chunk {
	
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
	
	public final void  process(final SamReader samReader, final byte[] referenceBases)throws Exception{
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
				System.out.println(e.getMessage());
			}
			long totalReadCount=0l;
			for (Map.Entry<Integer,Position> entry : positions.entrySet()) {
				Position p = entry.getValue();
				p.processVariants();
				/*
				if(p.variantDetected) {
					this.snps.add(p);
				}
				*/
				this.snps.add(p);
				totalReadCount+=p.total_readings;
			}
			this.readCount=totalReadCount;
		}catch(Exception e) {
			throw e;
		}finally {
			if(iter!=null) {
				iter.close();
			}
		}
	}

}
