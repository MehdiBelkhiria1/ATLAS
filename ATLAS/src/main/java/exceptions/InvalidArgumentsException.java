package exceptions;

public class InvalidArgumentsException extends AtlasException{

	private static final long serialVersionUID = 1L;
	
	public InvalidArgumentsException() {
        super("Invalid arguments !");
    }

}
