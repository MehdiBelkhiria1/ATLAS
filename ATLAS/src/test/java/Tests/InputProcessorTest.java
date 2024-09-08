package Tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import exceptions.InvalidArgumentsException;
import io.InputProcessor;

public class InputProcessorTest {
	
	@Test
    void testEmptyArgs() {
		String[] args= {};
		
        assertThrows(InvalidArgumentsException.class, () -> {
        	InputProcessor inputProcessor=new InputProcessor(args);
			inputProcessor.process();
        });
    }
	
	@Test
    void testInvalidArgs() {
		String[] args= {"text"};
		
        assertThrows(InvalidArgumentsException.class, () -> {
        	InputProcessor inputProcessor=new InputProcessor(args);
			inputProcessor.process();
        });
    }

}
