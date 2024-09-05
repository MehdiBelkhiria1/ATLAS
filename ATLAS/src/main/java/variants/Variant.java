package variants;

public class Variant {
	
    public Variant() {}
    
    public Variant(byte base,int count) {
    	this.base=base;
    	this.count=count;
    }
    
	byte base;
	boolean isStartIndel=false;
	boolean isDel=false;
	byte[] ref=null;
	byte[] alt=null;
	int count=0;
	int mappingQuality=0;
	float score=0f;
	
	public final StringBuilder toStringBuilder() {
		StringBuilder result=new StringBuilder();
		result.append("x").append(count).append((char)base).append(" P:").append(score).append(" Q:").append(mappingQuality).append(" ;");
		return result;
	}
	
}
