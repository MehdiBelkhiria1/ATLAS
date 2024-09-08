package exceptions;

public class AtlasException extends Exception {
	
	private static final long serialVersionUID = 1L;

    public AtlasException() {
        super();
    }
    
    public AtlasException(String message) {
        super(message);
    }
    
    public AtlasException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AtlasException(Throwable cause) {
        super(cause);
    }

}
