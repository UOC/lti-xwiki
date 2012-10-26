package edu.uoc.lti.utils;

public class Utils {

	/**
	 * Parse the customparameters
	 * @param customparameters
	 * @return
	 */
	public static String[][] parseCustomParameters(String customparameters) {
		String [][] r = null;
		String[] rtemp = new String[0];
		try {
			if (customparameters!=null && !"".equals(customparameters)) {
				customparameters = customparameters.trim();
				rtemp = customparameters.split(";");
			}
			r = new String[rtemp.length][1];
			for (int i=0; i<rtemp.length; i++){
				r[i] = rtemp[i].split("=");
			}
		} catch (Exception e){
			 r = new String[0][0];
		}
		return r;
	}
	
	
}
