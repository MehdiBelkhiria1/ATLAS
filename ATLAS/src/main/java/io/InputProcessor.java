package io;

import exceptions.InvalidArgumentsException;
import variants.VariantsCallerFactory;

public class InputProcessor {
	
	private final static String ref="-f";
	private final static String input="-i";
	private final static String output="-o";
	private final static String threads="-t";
	private final static String regions="-r";
	private final static String chunk="-chunk";
	private String[] args;
	
	public InputProcessor(String[] args){
		this.setArgs(args);
	}
	
	public void process() throws Exception{
		
		String refArg=null;
		String inputArg=null;
		String outputArg=null;
		Integer threadsArg=null;
		String regionsArg=null;
		Integer chunkSize=null;
		
		if(args.length==0) {
			throw new InvalidArgumentsException();
		}
		try {
			for(int i=0;i<args.length;i++) {
				String arg=args[i];
				switch(arg) {
				  case ref:
					  refArg=args[i+1];
				    break;
				  case input:
					  inputArg=args[i+1];
				    break;
				  case output:
					  outputArg=args[i+1];
				    break;
				  case threads:
					  threadsArg=Integer.valueOf(args[i+1]);
				    break;
				  case regions:
					  regionsArg=args[i+1];
				    break;
				  case chunk:
					  chunkSize=Integer.valueOf(args[i+1]);
				    break;
				}
			}
			if(refArg==null || inputArg==null || outputArg==null || regionsArg==null) {
				throw new InvalidArgumentsException();
			}
		}catch(Exception e) {
			throw new InvalidArgumentsException();
		}
		
		//******
		VariantsCallerFactory.createVariantssCaller(inputArg,refArg,outputArg,regionsArg,threadsArg,chunkSize).process();
		
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	

}
