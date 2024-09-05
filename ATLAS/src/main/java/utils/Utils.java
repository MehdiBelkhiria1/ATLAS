package utils;

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
		//c 4
		else if(regionStart>=recordStart && regionEnd<=recordEnd) {
			SCENARIO=4;
			startPos=regionStart-recordStart;
			endPos=startPos+(regionEnd-regionStart);
		}
		System.out.println("CASE: "+SCENARIO);
		
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
