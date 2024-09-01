package Utils;

public class Utils {
	
	private static final byte UPPER_CASE_OFFSET = 'A' - 'a';

    /**
     * @param b ASCII character
     * @return uppercase version of arg if it was lowercase, otherwise returns arg
     */
    public static final byte toUpperCase(final byte b) {
        if (b < 'a' || b > 'z') {
            return b;
        }
        return (byte) (b + UPPER_CASE_OFFSET);
    }
    
    public static final String charArrayToString(final char[] chars) {
    	StringBuilder sb = new StringBuilder(chars.length);
    	for (char c : chars) {
    		 sb.append(c);
    	}
    	return sb.toString();
    }

}
