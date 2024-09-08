package examples;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.InputProcessor;


public class Atlas {
	
	private static final Logger logger = LogManager.getLogger(Atlas.class);

	public static void main(String[] args) {
		try {
			InputProcessor inputProcessor=new InputProcessor(args);
			inputProcessor.process();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
}
