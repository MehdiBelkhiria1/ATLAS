package variants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Utils;


public class Position{
	
	public Position(char[] chromosome, int position, byte referenceAllele){
		this.position=position;
		this.chromosome=chromosome;
		this.referenceAllele=referenceAllele;
	}
	
	char[] chromosome;
	int position=0;
	boolean variantDetected=false;
	byte referenceAllele;
	List<Variant> variants=new ArrayList<>();
	
	int total_readings=0;
	
	public final void processVariants() {
		Variant referenceVariant=getVariant(referenceAllele);
		//filter
		int referenceReadsCount=referenceVariant!=null?referenceVariant.count:0;
		
		for (Variant variant : variants) {
			if(variant.base!=referenceAllele && variant.count>=referenceReadsCount) {
				variantDetected=true;
			}
			variant.score=((float)variant.count/(float)total_readings);
		}
	}

	public final void  addRead(byte base, int mappingQuality) {
		Variant variant=getVariant(base);
		if(variant==null) {
			variant=new Variant();
			variant.base=base;
			variants.add(variant);
		}
		variant.count++;
		variant.mappingQuality+=mappingQuality;
		total_readings++;
	}
	
	public final void  addInsert(byte base, byte[] indel, int mappingQuality, boolean isInsert) {
		addIndel(base, indel, mappingQuality, true);
	}
	
	public final void  addDeletion(byte base, byte[] indel, int mappingQuality, boolean isInsert) {
		addIndel(base, indel, mappingQuality, false);
	}
	
	public final void  addIndel(byte base, byte[] indel, int mappingQuality, boolean isInsert) {
		Variant variant=null;
		if(isInsert) {
			variant=getInsert(base, indel);
		}else {
			variant=getDeletion(base, indel);
		}
		
		if(variant==null) {
			variant=new Variant();
			variant.base=base;
			variants.add(variant);
		}
		variant.count++;
		variant.mappingQuality+=mappingQuality;
	}
	
	public  final Variant getVariant(byte read) {
		for(Variant variant:variants) {
			if(variant.base==read) {
				return variant;
			}
		}
		return null;
	}
	
	public  final Variant getInsert(byte read, byte[] indel) {
		for(Variant variant:variants) {
			if(variant.base==read && variant.isInsert && Arrays.equals(indel, variant.indel)) {
				return variant;
			}
		}
		return null;
	}
	
	public  final Variant getDeletion(byte read, byte[] indel) {
		for(Variant variant:variants) {
			if(variant.base==read && variant.isDeletion && Arrays.equals(indel, variant.indel)) {
				return variant;
			}
		}
		return null;
	}
	
	// TODO
	public String toVCF_string() {
		StringBuilder result=new StringBuilder();
		return result.toString();
	}
	
	public final StringBuilder toStringBuilder() {
		StringBuilder result=new StringBuilder();
		result
		.append(Utils.charArrayToString(chromosome)).append("")
		.append(":").append(position).append(" ;")
		.append(" Total Reads:x").append(total_readings).append(" ;")
		.append("Reference Allele: ").append((char)referenceAllele).append(" ;")
		.append("Possible SNPs: ");
		for (Variant variant : variants) {
			result.append(variant.toStringBuilder());
		}
		result.append("\n");
		return result;
	}

	
}
