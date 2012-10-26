/**********************************************************************************
 * $URL: http://ims-dev.googlecode.com/svn/trunk/basiclti/java-servlet/WEB-INF/classes/org/imsglobal/basiclti/BasicLTIUtilUocWrapper.java $
 * $Id: BasicLTIUtilUocWrapper.java 64 2011-01-27 abertranb@uoc.edu $
 **********************************************************************************
 *
 * Copyright (c) 2011 Universitat Oberta de Catalunya
 * 
 * This file is part of Campus Virtual de Programari Lliure (CVPLl).  
 * CVPLl is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details, currently published 
 * at http://www.gnu.org/copyleft/gpl.html or in the gpl.txt in 
 * the root folder of this distribution.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.   
 *
 **********************************************************************************/
package edu.uoc.lti;

import static org.imsglobal.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE;

import static org.imsglobal.basiclti.BasicLTIConstants.LTI_VERSION;
import static org.imsglobal.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL;
import static org.imsglobal.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION;
import static org.imsglobal.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_GUID;
import static org.imsglobal.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_NAME;
import static org.imsglobal.basiclti.BasicLTIConstants.TOOL_CONSUMER_INSTANCE_URL;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

import org.apache.log4j.Logger;
import org.imsglobal.basiclti.*;
/*import org.json.JSONObject;
import org.json.JSONException;
*/
import java.util.Properties;

/* Leave out until we have JTidy 0.8 in the repository 
import org.w3c.tidy.Tidy;
import java.io.ByteArrayOutputStream;
*/

/**
 * Some Utility code for IMS Basic LTI
 * http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
 */
public class BasicLTIUtilUocWrapper extends BasicLTIUtil {

	
	private final static Logger log = Logger.getLogger(BasicLTIUtilUocWrapper.class);
	public static String PARAM_BASE64 = "custom_lti_message_encoded_base64";
	public static String PARAM_UTF8 = "custom_lti_message_encoded_utf8";
	public static String PARAM_ISO = "custom_lti_message_encoded_iso";
	public static String [] KEYS_NO_ENCODE = {"lti_version", "lti_message_type", "tool_consumer_instance_description", "tool_consumer_instance_guid", "oauth_consumer_key", PARAM_BASE64, "oauth_nonce", "oauth_version", "oauth_callback", "oauth_timestamp", "basiclti_submit", "oauth_signature_method", "oauth_signature", PARAM_UTF8, PARAM_ISO};

	// used to remove javascript from html
	private static final String START_JAVASCRIPT = "<script";
	private static final String END_JAVASCRIPT = "</script>";
	private static final String SOURCE_CODIFICATION_ISO = "ISO-8859-1";
	private static final String SOURCE_CODIFICATION = "UTF-8";
	//Posem configuracio antiga
	public static final String DESTINATION_CODIFICATION_UTF8 = "UTF-8";
	public static final String DESTINATION_CODIFICATION = "ISO-8859-1";

	public static final String PARAM_ERROR_URL = "error_url";

 // Create the HTML to render a POST form and then automatically submit it
    // Make sure to call cleanupProperties before signing
    public static String postLaunchHTML(Properties newMap, String endpoint, boolean debug, boolean show_pop_up, String preferredHeightString) {
        if ( endpoint == null ) return null;
        StringBuffer text = new StringBuffer();
        int preferredHeight = 0;
        try {
        	preferredHeight = Integer.parseInt(preferredHeightString);
        } catch (Exception e){
        	//Nothing
        }
        boolean isIframe = false;
        String target 		= "";
        String nameIframe 	= "iframe_ltiLauncher";
        if (show_pop_up) { //Shows in pop-up
        		target = "target=\"_blank\"";
        } else {
        	if (preferredHeight!=0) { //Shows in iframe
        		target 		= "target=\""+nameIframe+"\"";
        		isIframe 	= true;
        	} //Else shows in the same page
        }
        
        
        if (isIframe) {
        	text.append(
        				"<iframe name='"+nameIframe+"' id='"+nameIframe+"' width='100%' height='"+preferredHeight+"' src='' frameborder='0'>" +
						"<br><h3>Result of launch tool</h3>"+
        				"</iframe>" 
        	);
        	
        }

        boolean sendISOEncoded = isEncoded(PARAM_ISO, newMap) && !isEncoded(PARAM_UTF8, newMap);
	    	
		String codification_response = sendISOEncoded?BasicLTIUtilUocWrapper.DESTINATION_CODIFICATION:BasicLTIUtilUocWrapper.DESTINATION_CODIFICATION_UTF8;
		
        text.append("<div id=\"ltiLaunchFormSubmitArea\">\n");
        text.append("<form action=\""+endpoint+"\" name=\"ltiLaunchForm\" id=\"ltiLaunchForm\" " +
        		"method=\"post\" "+target+" encType=\"application/x-www-form-urlencoded\" accept-charset=\""+codification_response.toLowerCase()+"\">\n" );
        for(Object okey : newMap.keySet() )
        {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = newMap.getProperty(key);
                if ( value == null ) continue;
		// This will escape the contents pretty much - at least 
		// we will be safe and not generate dangerous HTML
                key = htmlspecialchars(key);
                value = htmlspecialchars(value);
                if ( key.equals(BASICLTI_SUBMIT) ) {
                  text.append("<input type=\"submit\" name=\"");
                } else { 
                  text.append("<input type=\"hidden\" name=\"");
                }
                text.append(key);
                text.append("\" value=\"");
                text.append(value);
                text.append("\"/>\n");
        }
        text.append("</form>\n" + 
                "</div>\n");
        if ( debug ) {
            text.append("<pre>\n");
            text.append("<b>BasicLTI Endpoint</b>\n");
            text.append(endpoint);
            text.append("\n\n");
            Properties pCloned = (Properties) newMap.clone();
            pCloned = decodePostData(pCloned);
            text.append("<b>BasicLTI Parameters:</b>\n");
            for(Object okey : pCloned.keySet() )
            {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = pCloned.getProperty(key);
                if ( value == null ) continue;
                text.append(key);
                text.append("=");
                text.append(value);
                text.append("\n");
            }
            text.append("</pre>\n");
        } else {
            text.append(
                    " <script language=\"javascript\"> \n" +
		    "    document.getElementById(\"ltiLaunchFormSubmitArea\").style.display = \"none\";\n" + 
		    "    nei = document.createElement('input');\n" +
		    "    nei.setAttribute('type', 'hidden');\n" + 
		    "    nei.setAttribute('name', '"+BASICLTI_SUBMIT+"');\n" + 
		    "    nei.setAttribute('value', '"+newMap.getProperty(BASICLTI_SUBMIT)+"');\n" + 
		    "    document.getElementById(\"ltiLaunchForm\").appendChild(nei);\n" +
                    "   document.ltiLaunchForm.submit(); \n" + 
                    " </script> \n");
        }
        
        String htmltext = text.toString();
        return htmltext;
    }

    /**
     * Returns the JSON objet for LTI call
     * @param newMap
     * @param endpoint
     * @param debug
     * @param show_pop_up
     * @param preferredHeightString
     * @return
     
    public static String postLaunchJSON(Properties newMap, String endpoint) throws JSONException {
        if ( endpoint == null ) return null;
        StringBuffer text = new StringBuffer();
        boolean sendISOEncoded = isEncoded(PARAM_ISO, newMap) && !isEncoded(PARAM_UTF8, newMap);
	    	
		String codification_response = sendISOEncoded?BasicLTIUtilUocWrapper.DESTINATION_CODIFICATION:BasicLTIUtilUocWrapper.DESTINATION_CODIFICATION_UTF8;

		JSONObject arrayObj=new JSONObject();
		arrayObj.put("endpoint",endpoint);
		for(Object okey : newMap.keySet() )
        {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = newMap.getProperty(key);
                if ( value == null ) continue;
                arrayObj.put(key,value);
        }
        
        String htmltext = arrayObj.toString();
        return htmltext;
    }
    */

    public static Properties encodeBase64(Properties postProp, String [] keysNoEncode) {
    	String value, key;
    	
    	Enumeration<Object> enumeration = postProp.keys();
    	
    	while (enumeration.hasMoreElements()) {
    	
    		key = (String) enumeration.nextElement();
    		
    		if (!contains(keysNoEncode, key)) {
    			value = encodeBase64((String)postProp.get(key));
    		
    			setProperty(postProp, key, value);
    		}
    	}
    	
    	return postProp;
    }
    public static Properties decodeBase64(Properties postProp, String [] keysNoEncode) {
    	String value, key;
    	
    	Enumeration<Object> enumeration = postProp.keys();
    	
    	while (enumeration.hasMoreElements()) {
    	
    		key = (String) enumeration.nextElement();
    		
    		if (!contains(keysNoEncode, key)) {
    			value = decodeBase64((String)postProp.get(key));
    		
    			postProp.setProperty(key, value);
    		}
    	}
    	
    	return postProp;
    }
    
    private static boolean isEncoded(String param_encoding, Properties postProp) {
    	boolean isEncoded = false;
    	Object obj = postProp.get(param_encoding);
    	if (obj!=null && ((String)obj).equals("1")) {
    		isEncoded = true;
    	}
    	return isEncoded;
    }

    public static Properties encodePostData(Properties postProp) throws UnsupportedEncodingException {

    	if (isEncoded(PARAM_BASE64, postProp)) {
    		postProp = encodeISO(postProp, KEYS_NO_ENCODE);
    		postProp = encodeBase64(postProp, KEYS_NO_ENCODE);
    	} 
    	else  {
	    	if (isEncoded(PARAM_ISO, postProp)) {
	    		postProp = encodeISO(postProp, KEYS_NO_ENCODE);
	    	} else {
		    	if (isEncoded(PARAM_UTF8, postProp)){
		    		postProp = encodeUTF8(postProp, KEYS_NO_ENCODE);
		    	}
	    	}
    	}
    	
    	return postProp;
    }

    public static Properties encodeISO(Properties postProp, String [] keysNoEncode) throws UnsupportedEncodingException {
    	String value, key;
    	
    	Enumeration<Object> enumeration = postProp.keys();
    	
    	while (enumeration.hasMoreElements()) {
    	
    		key = (String) enumeration.nextElement();
    		
    		if (!contains(keysNoEncode, key)) {
    			value = encodeISO((String)postProp.get(key));
    			setProperty(postProp, key, value);
    		}
    	}
    	
    	return postProp;
    }

    public static Properties encodeUTF8(Properties postProp, String [] keysNoEncode) throws UnsupportedEncodingException {
    	String value, key;
    	
    	Enumeration<Object> enumeration = postProp.keys();
    	
    	while (enumeration.hasMoreElements()) {
    	
    		key = (String) enumeration.nextElement();
    		
    		if (!contains(keysNoEncode, key)) {
    			value = encodeUTF8((String)postProp.get(key));
    			setProperty(postProp, key, value);
    		}
    	}
    	
    	return postProp;
    }
    
    // To make absolutely sure we never send an XSS, we clean these values
    public static void setProperty(Properties props, String key, String value)
    {
        if ( value == null )  {
        	props.remove(key);
        	return;
        }
        value = cleanHtml(value);
        if ( value.trim().length() < 1 ) {
        	props.remove(key);
        	return;
        }
        props.setProperty(key, value);
    }
    

	/**
	 ** Make sure any HTML is 'clean' (no javascript, invalid image tags)
	 **/
	public static String cleanHtml( String htmlStr )
	{
		// handle embedded images			
		htmlStr = htmlStr.replaceAll("<img ", "<img alt='' ");
			
		// remove all javascript (risk of exploit)
		// note that String.replaceAll() does not reliably handle line terminators, 
		// so javascript is removed string by string
		while ( htmlStr.indexOf(START_JAVASCRIPT) != -1 )
		{
			int badStart = htmlStr.indexOf(START_JAVASCRIPT);
			int badEnd = htmlStr.indexOf(END_JAVASCRIPT);
			String badHtml;
		
			if ( badStart > -1 && badEnd == -1)
				badHtml = htmlStr.substring( badStart );
			else
				badHtml = htmlStr.substring( badStart, badEnd+END_JAVASCRIPT.length() );
				
			// use replace( CharSequence, CharSequence) -- no regexp
			htmlStr = htmlStr.replace( new StringBuilder(badHtml), new StringBuilder() );
		}

		return htmlStr;
	}
	
	/**
	   * Add the necessary fields and sign.
	   * 
	   * @param postProp
	   * @param url
	   * @param method
	   * @param oauth_consumer_key
	   * @param oauth_consumer_secret
	   * @param tool_consumer_instance_guid
	   *          See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_GUID}
	   * @param tool_consumer_instance_description
	   *          See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_DESCRIPTION}
	   * @param tool_consumer_instance_url
	   *          See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_URL}
	   * @param tool_consumer_instance_name
	   *          See: {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_NAME}
	   * @param tool_consumer_instance_contact_email
	   *          See:
	   *          {@link BasicLTIConstants#TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL}
	   * @return
	   */
	  public static Map<String, String> signProperties(
	      Map<String, String> postProp, String url, String method,
	      String oauth_consumer_key, String oauth_consumer_secret,
	      String tool_consumer_instance_guid,
	      String tool_consumer_instance_description,
	      String tool_consumer_instance_url, String tool_consumer_instance_name,
	      String tool_consumer_instance_contact_email) {
	    postProp = BasicLTIUtilUocWrapper.cleanupProperties(postProp);
	    postProp.put(LTI_VERSION, "LTI-1p0");
	    postProp.put(LTI_MESSAGE_TYPE, "basic-lti-launch-request");
	    // Allow caller to internationalize this for us...
	    if (postProp.get(BASICLTI_SUBMIT) == null) {
	      postProp.put(BASICLTI_SUBMIT, "Launch Endpoint with BasicLTI Data");
	    }
	    if (tool_consumer_instance_guid != null)
	      postProp.put(TOOL_CONSUMER_INSTANCE_GUID, tool_consumer_instance_guid);
	    if (tool_consumer_instance_description != null)
	      postProp.put(TOOL_CONSUMER_INSTANCE_DESCRIPTION,
	          tool_consumer_instance_description);
	    if (tool_consumer_instance_url != null)
	      postProp.put(TOOL_CONSUMER_INSTANCE_URL, tool_consumer_instance_url);
	    if (tool_consumer_instance_name != null)
	      postProp.put(TOOL_CONSUMER_INSTANCE_NAME, tool_consumer_instance_name);
	    if (tool_consumer_instance_contact_email != null)
	      postProp.put(TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL,
	          tool_consumer_instance_contact_email);

	    if (postProp.get("oauth_callback") == null)
	      postProp.put("oauth_callback", "about:blank");

	    if (oauth_consumer_key == null || oauth_consumer_secret == null) {
	      dPrint("No signature generated in signProperties");
	      return postProp;
	    }

	    OAuthMessage oam = new OAuthMessage(method, url, postProp.entrySet());
	    OAuthConsumer cons = new OAuthConsumer("about:blank", oauth_consumer_key,
	        oauth_consumer_secret, null);
	    OAuthAccessor acc = new OAuthAccessor(cons);
	    try {
	      oam.addRequiredParameters(acc);
	      //System.out.println("Base Message String\n"+net.oauth.signature.OAuthSignatureMethod.getBaseString(oam)+"\n");
	      
	      List<Map.Entry<String, String>> params = oam.getParameters();

	      Map<String, String> nextProp = new HashMap<String, String>();
	      // Convert to Map<String, String>
	      for (final Map.Entry<String, String> entry : params) {
	        nextProp.put(entry.getKey(), entry.getValue());
	      }
	      return nextProp;
	    } catch (net.oauth.OAuthException e) {
	      log.warn("BasicLTIUtilUocWrapper.signProperties OAuth Exception "
	          + e.getMessage());
	      throw new Error(e);
	    } catch (java.io.IOException e) {
	      log.warn("BasicLTIUtilUocWrapper.signProperties IO Exception "
	          + e.getMessage());
	      throw new Error(e);
	    } catch (java.net.URISyntaxException e) {
	      log.warn("BasicLTIUtilUocWrapper.signProperties URI Syntax Exception "
	          + e.getMessage());
	      throw new Error(e);
	    }

	  }
	
	/**
	   * Any properties which are not well known (i.e. in
	   * {@link BasicLTIConstants#validPropertyNames}) will be mapped to custom
	   * properties per the specified semantics. NOTE: no blacklisting of keys is
	   * performed.
	   * 
	   * @param rawProperties
	   *          A set of properties that will be cleaned.
	   * @return A cleansed version of rawProperties.
	   */
	  public static Map<String, String> cleanupProperties(
	      final Map<String, String> rawProperties) {
	    return cleanupProperties(rawProperties, null);
	  }

	  /**
	   * Any properties which are not well known (i.e. in
	   * {@link BasicLTIConstants#validPropertyNames}) will be mapped to custom
	   * properties per the specified semantics.
	   * 
	   * @param rawProperties
	   *          A set of properties that will be cleaned.
	   * @param blackList
	   *          An array of {@link String}s which are considered unsafe to be
	   *          included in launch data. Any matches will be removed from the
	   *          return.
	   * @return A cleansed version of rawProperties.
	   */
	  public static Map<String, String> cleanupProperties(
	      final Map<String, String> rawProperties, final String[] blackList) {
	    final Map<String, String> newProp = new HashMap<String, String>(
	        rawProperties.size()); // roughly the same size
	    for (String okey : rawProperties.keySet()) {
	      final String key = okey.trim();
	      if (blackList != null) {
	        boolean blackListed = false;
	        for (String blackKey : blackList) {
	          if (blackKey.equals(key)) {
	            blackListed = true;
	            break;
	          }
	        }
	        if (blackListed) {
	          continue;
	        }
	      }
	      final String value = rawProperties.get(key);
	      if (value == null || "".equals(value)) {
	        // remove null or empty values
	        continue;
	      }
	      if (isSpecifiedPropertyName(key)) {
	        // a well known property name
	        newProp.put(key, value);
	      } else {
	        // convert to a custom property name
	        newProp.put(adaptToCustomPropertyName(key), value);
	      }
	    }
	    return newProp;
	  }
	/**
	   * Checks to see if the passed propertyName is equal to one of the Strings
	   * contained in {@link BasicLTIConstantsUocWrapper#validPropertyNames}. String matching
	   * is case sensitive.
	   * 
	   * @param propertyName
	   * @return true if propertyName is equal to one of the Strings contained in
	   *         {@link BasicLTIConstantsUocWrapper#validPropertyNames} 
	   *         or is a custom parameter oe extension parameter ;
	   *         else return false.
	   */
	  public static boolean isSpecifiedPropertyName(final String propertyName) {
	    boolean found = false;
	    if ( propertyName.startsWith(BasicLTIConstantsUocWrapper.CUSTOM_PREFIX) ) return true;
	    if ( propertyName.startsWith(BasicLTIConstantsUocWrapper.EXTENSION_PREFIX) ) return true;
	    if ( propertyName.startsWith(BasicLTIConstantsUocWrapper.OAUTH_PREFIX) ) return true;
	    for (String key : BasicLTIConstantsUocWrapper.validPropertyNames) {
	      if (key.equals(propertyName)) {
	        found = true;
	        break;
	      }
	    }
	    return found;
	  }

    
    public static Properties decodePostData(Properties postProp) {
    	
    	if (isEncoded(PARAM_BASE64, postProp)) {
    		postProp = decodeBase64(postProp, KEYS_NO_ENCODE);
    	}
    	else {
//    		TODO nomes fer-ho si es necessari
//        	if (isEncoded(PARAM_UTF8, postProp)) {
//        		postProp = decodeUTF8(postProp, KEYS_NO_ENCODE);
//        	}
    	}
    	
    	return postProp;
    }
    
    private static boolean contains(String [] keysNoEncode, String key) {
    	boolean b = false;
    	for (int i=0; i<keysNoEncode.length; i++) {
    		b = keysNoEncode[i].equals(key);
    		if (b) 
    			break;
    	}
    	return b;
    }

    private static String encodeBase64(String s){
		 byte[] binaryData = s.getBytes();
		 s = new String (Base64.encode(binaryData));
		 return s;
	 }

    private static String encodeISO(String s) throws UnsupportedEncodingException{
		 byte[] arrByte = s.getBytes(SOURCE_CODIFICATION);
         s = new String(arrByte, DESTINATION_CODIFICATION);
		 return s;
	 }

    private static String encodeUTF8(String s) throws UnsupportedEncodingException{
		 byte[] arrByte = s.getBytes(SOURCE_CODIFICATION_ISO);
         s = new String(arrByte, DESTINATION_CODIFICATION_UTF8);
		 return s;
	 }
    
    public static String decodeBase64(String s){
		 byte[] binaryData = s.getBytes();
		 s = new String (Base64.decode(binaryData));
		 return s;
	 }
    public static String decodeISO(String s) {
    	try {
		 byte[] arrByte = s.getBytes(DESTINATION_CODIFICATION);
         s = new String(arrByte, SOURCE_CODIFICATION);
    	} catch (UnsupportedEncodingException e) {
    	}
         return s;
		 
	 }

    public static String decodeUTF8(String s){
    	try {
		 byte[] arrByte = s.getBytes(DESTINATION_CODIFICATION_UTF8);
         s = new String(arrByte, SOURCE_CODIFICATION_ISO);
    	} catch (UnsupportedEncodingException e) {
    	}
		return s;
	 }
    
	/**
	 * get user's ip from a custom HTTP request header
	 * @param request user HTTP request
	 * @return value of "rlnclientipaddr" HTTP header 
	 */
	public String getClientIpAddressFromBalanceador(javax.servlet.http.HttpServletRequest request)
	{
		String result = request.getHeader("rlnclientipaddr");
		
		if (result == null) {
			result = request.getHeader("x-forwarded-for");
		}

		if (result == null) {
			result = request.getRemoteAddr();
		}
		return result;
	}
	
	/**
	 * Detecta si la salida tiene que ser JSON o HTML
	 * @param request
	 * @return boolean
	 */
	public static boolean is_json_output(javax.servlet.http.HttpServletRequest request)
	{
		boolean is_json = false;
		try {
			is_json = "1".equals(request.getParameter(BasicLTIConstantsUocWrapper.JSON_OUTPUT));
		} catch (Exception e) {
		}
		return is_json;
	}
 }