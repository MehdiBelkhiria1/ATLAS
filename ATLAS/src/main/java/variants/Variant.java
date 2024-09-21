package variants;

public class Variant {
	
    public Variant() {}
    
    public Variant(byte base,int count) {
    	this.base=base;
    	this.count=count;
    }
    
    public Variant(byte base,int count,byte[] indel, boolean isDeletion, boolean isInsert) {
    	this.base=base;
    	this.count=count;
    	this.indel=indel;
    	this.isDeletion=isDeletion;
    	this.isInsert=isInsert;
    }
    
	byte base;
	boolean isDeletion=false;
	boolean isInsert=false;
	byte[] indel=null;
	int count=0;
	int mappingQuality=0;
	float score=0f;
	
	public final StringBuilder toStringBuilder() {
		StringBuilder result=new StringBuilder();
		result.append("x").append(count).append((char)base).append(" P:").append(score).append(" Q:").append(mappingQuality).append(" ;");
		return result;
	}
	
}
