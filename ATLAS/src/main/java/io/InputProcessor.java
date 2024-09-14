package io;

import exceptions.InvalidArgumentsException;
import variants.VariantsCallerFactory;

public class InputProcessor {
	
	private final static String REF="-f";
	private final static String INPUT="-i";
	private final static String OUTPUT="-o";
	private final static String THREADS="-t";
	private final static String REGIONS="-r";
	private final static String CHUNK="-chunk";
	private final static String VIEW="view";
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
		boolean isView=false;
		
		if(args.length==0) {
			throw new InvalidArgumentsException();
		}
		try {
			for(int i=0;i<args.length;i++) {
				String arg=args[i];
				switch(arg) {
				  case REF:
					  refArg=args[i+1];
				    break;
				  case INPUT:
					  inputArg=args[i+1];
				    break;
				  case OUTPUT:
					  outputArg=args[i+1];
				    break;
				  case THREADS:
					  threadsArg=Integer.valueOf(args[i+1]);
				    break;
				  case REGIONS:
					  regionsArg=args[i+1];
				    break;
				  case CHUNK:
					  chunkSize=Integer.valueOf(args[i+1]);
				  case VIEW:
					  isView=true;
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
		VariantsCallerFactory.createVariantsCaller(inputArg,refArg,outputArg,regionsArg,threadsArg,chunkSize,isView).process();
		
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	

}
