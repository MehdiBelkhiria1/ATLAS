package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import variants.Position;

public class Utils {
	
	private static final byte UPPER_CASE_OFFSET = 'A' - 'a';

    /**
     * @param b ASCII character
     * @return uppercase version of arg if it was lowercase, otherwise returns arg
     */
    public static final byte toUpperCase(final byte b) {
        if (b < 'a' || b > 'z') {
            return b;
        }
        return (byte) (b + UPPER_CASE_OFFSET);
    }
    
    public static final String charArrayToString(final char[] chars) {
    	StringBuilder sb = new StringBuilder(chars.length);
    	for (char c : chars) {
    		 sb.append(c);
    	}
    	return sb.toString();
    }
    
    
    public static final void processRecord(final SAMRecord record, final char[] chromosome, int regionStart, int regionEnd,final Map<Integer, Position> positions,final byte[] referenceBases) {

        int mappingQuality=record.getMappingQuality();
        int recordStart = record.getAlignmentStart();
        int recordEnd = record.getAlignmentEnd();
        
		if ((!record.getReadUnmappedFlag() && record.getCigar().containsOperator(CigarOperator.N))) {
			return;
		}

		for (int i = 0; i < record.getReadLength(); i++) {
			
			int realPosition=record.getReferencePositionAtReadPosition(i+1);
			int currentReferenceBasePosition=realPosition-1;
			
			if(realPosition==0 || realPosition<regionStart || realPosition>regionEnd) {
				continue;
			}
			
			Position p = positions.get(realPosition);
			if (p == null) {
				p = new Position(chromosome, realPosition, Utils.toUpperCase(referenceBases[currentReferenceBasePosition]));
				positions.put(realPosition, p);
			}
			
			p.addRead(Utils.toUpperCase(record.getReadBases()[i]),mappingQuality);
		}
		
		//TODO: add insertions and deletions detection
//		int pos=0;
//		for(CigarElement cigarElement:record.getCigar().getCigarElements()) {
//			CigarOperator op = cigarElement.getOperator();
//            int length = cigarElement.getLength();
//            if(op==CigarOperator.D) {
//            	
//            }
//            else if(op==CigarOperator.I){
//            	
//            }
//            pos+=length;
//		}
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

}
