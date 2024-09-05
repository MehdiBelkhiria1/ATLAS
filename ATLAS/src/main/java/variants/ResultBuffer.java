package variants;

public class ResultBuffer {
	
	int id;
	int totalReads=0;
	int totalSnps=0;
	StringBuilder result=new StringBuilder();
	
	public ResultBuffer() {
	}
	
	public ResultBuffer(int id, StringBuilder result, int totalReads, int totalSnps) {
		this.id = id;
		this.result = result;
	}
}
