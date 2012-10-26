package edu.uoc.lti;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import net.oauth.OAuthMessage;

public class OAuthMessageUocUtil {
	
	private Properties properties = null;
	/*More info in http://en.gravatar.com/*/
	private final static String URL_GRAVATAR = "http://www.gravatar.com/avatar.php?gravatar_id=";
	public OAuthMessageUocUtil(OAuthMessage oam) throws IOException{
		try {
			initialize(oam);
		} catch (IOException e) {
			properties = null;
			throw e;
		}
	 } 
	 
	 private void initialize(OAuthMessage oam) throws IOException {

		 Properties postProperties = new Properties();
		 Entry <String, String> e = null;
		 for(Iterator<Entry<String, String>> params = oam.getParameters().iterator(); params.hasNext();) {

			 e=params.next();
			 postProperties.put(e.getKey(), e.getValue());
			 
		 }

		 properties = BasicLTIUtilUocWrapper.decodePostData(postProperties) ;
	 }
	 
	 public  Properties getParameterMap() {
		 return properties;
	 }
	 
	 public String getParameter(String name)  throws IOException {
		 return properties.getProperty(name);
	 }

	public String getCustomParameter(String property) throws IOException {
		String ret = this.getParameter(BasicLTIConstantsUocWrapper.CUSTOM_PREFIX+property);
		return ret;
	}


	 public boolean isInstructor()  throws IOException {
	        String roles = getParameter("roles");
	        roles = roles.toLowerCase();
	        if ( ! ( roles.indexOf("instructor") == -1 ) ) return true;
	        if ( ! ( roles.indexOf("administrator") == -1 ) ) return true;
	        return false;
	    }
	 
	 public boolean isLearner()  throws IOException {
	        String roles = getParameter("roles");
	        roles = roles.toLowerCase();
	        if ( ! ( roles.indexOf("learner") == -1 ) ) return true;
	        return false;
	    }

	 public String getUserEmail()  throws IOException {
	        String email = getParameter("lis_person_contact_email_primary");
	        if ( !isNullorVoid(email) ) return email;
	        //# Sakai Hack
	        email = getParameter("lis_person_contact_emailprimary");
	        if ( !isNullorVoid(email) ) return email;
	        return null;
	    }

	 public String getUserShortName()  throws IOException {
	        String email = this.getUserEmail();
	        String givenname = getParameter("lis_person_name_given");
	        String familyname = getParameter("lis_person_name_family");
	        String fullname = getParameter("lis_person_name_full");
	        if ( !isNullorVoid(email) ) return email;
	        if ( !isNullorVoid(givenname) ) return givenname;
	        if ( !isNullorVoid(familyname) ) return familyname;
	        if ( !isNullorVoid(fullname) ) return fullname;
	        return this.getUserName();
	    }
	  
	 public String getUserName()  throws IOException {
	        String givenname = getParameter("lis_person_name_given");
	        String familyname = getParameter("lis_person_name_family");
	        String fullname = getParameter("lis_person_name_full");
	        if ( !isNullorVoid(fullname) ) return fullname;
	        if ( !isNullorVoid(familyname) && !isNullorVoid(givenname) ) return givenname + familyname;
	        if ( !isNullorVoid(givenname)) return givenname;
	        if ( !isNullorVoid(familyname) ) return familyname;
	        return this.getUserEmail();
	    }

	 public String getUserKey()  throws IOException {
	        String lis_person_sourcedid = getParameter("lis_person_sourcedid");
	        String oauth = getParameter("oauth_consumer_key");
	        if (!isNullorVoid(oauth) && !isNullorVoid(lis_person_sourcedid) ) {
		        int pos = lis_person_sourcedid.indexOf(oauth+":");
		        if (pos != -1 && !isNullorVoid(lis_person_sourcedid))
		          return lis_person_sourcedid;
	        }
	        String id = getParameter("user_id");
	        if ( !isNullorVoid(id) && !isNullorVoid(oauth) ) return oauth + ':' + id;
	        return null;
	    }

	 public String getUserImage()  throws IOException {
	        String image = getParameter("user_image");
	        if ( !isNullorVoid(image) ) return image;
	        String email = this.getUserEmail();
	        if ( "".equals(email)) return null;
	        String grav_url =  URL_GRAVATAR+ 
	        MD5Util.md5Hex(email.toLowerCase());
	        return grav_url;
	    }

	 public String getResourceKey()  throws IOException {
	        String oauth = getParameter("oauth_consumer_key");
	        String id = getParameter("resource_link_id");
	        if ( !isNullorVoid(id) && !isNullorVoid(oauth) ) return oauth + ':' + id;
	        return null;
	    }

	 public String getResourceDescription()  throws IOException {
	        String title = getParameter("resource_link_description");
	        if ( !isNullorVoid(title) ) return title;
	        return getResourceTitle();
	    }

	 public String getResourceTitle()  throws IOException {
	        String title = getParameter("resource_link_title");
	        if ( !isNullorVoid(title) ) return title;
	        return null;
	    }

	 public String getConsumerKey()  throws IOException {
	        String oauth = getParameter("oauth_consumer_key");
	        return oauth;
	    }

	 public String getCourseKey()  throws IOException {
	        String oauth = getParameter("oauth_consumer_key");
	        String id = getParameter("context_id");
	        if ( !isNullorVoid(id) && !isNullorVoid(oauth) ) return oauth + ":" + id;
	        return null;
	    }

	 public String getCourseName() throws IOException {
	        String label = getParameter("context_label");
	        String title = getParameter("context_title");
	        String id = getParameter("context_id");
	        if ( !isNullorVoid(label) ) return label;
	        if ( !isNullorVoid(title) ) return title;
	        if ( !isNullorVoid(id) ) return id;
	        return null;
	    }
	 
	 public String getPresentationLocale() throws IOException {
		 return getParameter(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_LOCALE);
	 }

	 public String dump() throws IOException {
		 return dump(false);
	 }
	 public String dump(boolean is_html_output) throws IOException { 
	        String newLine = "\n";
	        if (is_html_output)
	        	newLine = "<br>";
	        
	        String ret = newLine;
	        
	        if ( this.isInstructor() ) {
	            ret += "isInstructor() = true"+newLine;
	        } else {
	            ret += "isInstructor() = false"+newLine;
	        }
	        ret += "getUserKey() = "+this.getUserKey()+""+newLine;
	        ret += "getUserEmail() = "+this.getUserEmail()+""+newLine;
	        ret += "getUserShortName() = "+this.getUserShortName()+""+newLine;
	        ret += "getUserName() = "+this.getUserName()+""+newLine;
	        ret += "getUserImage() = "+this.getUserImage()+""+newLine;
	        ret += "getResourceKey() = "+this.getResourceKey()+""+newLine;
	        ret += "getResourceTitle() = "+this.getResourceTitle()+""+newLine;
	        ret += "getCourseName() = "+this.getCourseName()+""+newLine;
	        ret += "getCourseKey() = "+this.getCourseKey()+""+newLine;
	        ret += "getConsumerKey() = "+this.getConsumerKey()+""+newLine;
	        ret += "\nAll params : "+newLine;
	        String key = "";
	        String value = "";
	        for (Enumeration<Object> keys = getParameterMap().keys(); keys.hasMoreElements();) {
	        	key = (String) keys.nextElement();
	        	value = getParameter(key);
	        	ret += key+" = "+value+""+newLine;
	        }
	        return ret;
	    }



	 private static class MD5Util {
	  public static String hex(byte[] array) {
	      StringBuffer sb = new StringBuffer();
	      for (int i = 0; i < array.length; ++i) {
	      sb.append(Integer.toHexString((array[i]
	          & 0xFF) | 0x100).substring(1,3));       
	      }
	      return sb.toString();
	  }
	  public  static String md5Hex (String message) {
	      try {
	      MessageDigest md =
	          MessageDigest.getInstance("MD5");
	      return hex (md.digest(message.getBytes("CP1252")));
	      } catch (NoSuchAlgorithmException e) {
	      } catch (UnsupportedEncodingException e) {
	      }
	      return null;
	  }
	}
	 
	 /**
	  * Check if is null
	  * @param s
	  * @return boolean
	  */
	 private boolean isNullorVoid(String s){
		 return s==null || s.length()==0; 
	 }

}