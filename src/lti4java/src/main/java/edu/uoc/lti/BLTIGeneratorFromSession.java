package edu.uoc.lti;

import java.util.Enumeration;
import java.util.Properties;


import org.apache.log4j.Logger;

import edu.uoc.lti.utils.Utils;

public class BLTIGeneratorFromSession {
	
	private final static Logger log = Logger.getLogger(BLTIGeneratorFromSession.class);
	protected final static String CUSTOM = "custom_";
	private final static String LAUNCH_PRESENTATION_WINDOW = "window";
	private final static String LAUNCH_PRESENTATION_IFRAME = "iframe";
	private final static String LAUNCH_PRESENTATION_FRAME = "frame";
	private final static String EXT_LMS = "videochat";
	

	public BLTIGeneratorFromSession() {
//		PropertyConfigurator.configure("log4j.properties");
	}
	
	private String transformKey(String key){
		key = key.toLowerCase();
		if (key.equals("usergender")) {
      	  key = "user_gender";
        } else if (key.equals("usercity")) {
      	  key = "user_city";
        } else if (key.equals("userbirthdate")) {
      	  key = "user_birthdate";
        }
		key=key.replaceAll("\\.","_").replaceAll(":","_");
		
		return key;
	}
	
	/**
	 * To solve the " problem
	 * @param value
	 * @return
	 */
	private String transformCustomValue(String value){
		value=value.replaceAll("\"","\\\"");
		
		return value;
	}

	// Setup some fake data from the LMS
	 public Properties getLMSData(LTIApplication lti, LTIEnvironment LTIEnvironment , Properties propertiesRequestParams, boolean sendBase64Encoded, boolean sendUTF8Encoded, boolean sendISOEncoded,
			 String return_url) throws Exception {
		 Properties postProp  = new Properties();
		 try {
			 boolean show_pop_up	= lti.getLaunchinpopup()==1;
			 String preferredHeight	= lti.getPreferheight();
			 String org_id 			= lti.getOrganizationid();
			
			 String ext_lms			= EXT_LMS;

			 boolean debug = lti.isDebugmode();
			
			 /**Els dos de les propietats no calen*/
			 String username				= LTIEnvironment.getUserName();
			 if (username.startsWith(lti.getResourcekey()+":")) {
				 username = username.substring((lti.getResourcekey()+":").length());
			 }
			 String full_name				= LTIEnvironment.getFullName();
			 String email				= LTIEnvironment.getEmail();
				
			String id	 			= LTIEnvironment.getCourseKey();
			String context_title 	= LTIEnvironment.getCourseKey();
			String context_label	= LTIEnvironment.getCourseName();
			/*String nameRemoteTool	= lti.getName();
			String org_id_provider	= org_id;
			String org_desc_provider= org_id;*/
			String user_image = LTIEnvironment.getUser_image();
			
		     //Adaptat a http://www.rfc-editor.org/rfc/bcp/bcp47.txt
			//postProp.setProperty(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_LOCALE, ((String)agent.getPropertiesByType(agc.getUserPropertiesType()).getProperty("lang")).replaceAll("_", "-"));
			postProp.setProperty("user_image", user_image);
			postProp.setProperty(BasicLTIConstantsUocWrapper.RESOURCE_LINK_ID, LTIEnvironment.getIdRoom());
			postProp.setProperty(BasicLTIConstantsUocWrapper.RESOURCE_LINK_TITLE, LTIEnvironment.getTopicRoom());
			postProp.setProperty(BasicLTIConstantsUocWrapper.RESOURCE_LINK_DESCRIPTION,  LTIEnvironment.getDescriptionRoom());
		    postProp.setProperty(BasicLTIConstantsUocWrapper.USER_ID, username);
		     
			//Search the role in course
		    boolean isAdmin = false;
		    boolean isTeacher = LTIEnvironment.isInstructor();
		    boolean isStudent = LTIEnvironment.isDomainAuthorized();
		    boolean isGuest = false;
			//TODO: Mentor/Learner values??
			//'urn:lti:sysrole:ims/lis/Administrator'
		    postProp.setProperty("roles",isAdmin?BasicLTIConstantsUocWrapper.ADMINISTRATOR_ROLE:(isTeacher ?BasicLTIConstantsUocWrapper.INSTRUCTOR_ROLE:(isStudent?BasicLTIConstantsUocWrapper.STUDENT_ROLE:(isGuest?BasicLTIConstantsUocWrapper.GUEST_ROLE:BasicLTIConstantsUocWrapper.OTHER_ROLE))));
			
		    //Sending user details
		    boolean sendEmailDetails 	= lti.getSendemailaddr()==1;
			boolean sendNameDetails  	= lti.getSendname()==1;
		    if (sendNameDetails) {
		    	postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_PERSON_NAME_FULL, full_name); 
		    	postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_PERSON_NAME_FAMILY, full_name); 
		    	//postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_PERSON_NAME_GIVEN, username); 
		    }
		    if (sendEmailDetails) {
		    	postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_PERSON_CONTACT_EMAIL_PRIMARY,email);
		    }
		    
		    postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_PERSON_SOURCEDID, lti.getResourcekey()+":"+username);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.CUSTOM_USERNAME, username);
		    
		    postProp.setProperty(BasicLTIConstantsUocWrapper.CONTEXT_ID,id);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.CONTEXT_TITLE,context_title);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.CONTEXT_LABEL,context_label);//TODO possible to get code  
		    
		    //Adding custom values
		    String[][] customParams = Utils.parseCustomParameters(lti.getCustomparameters());
		    for (int i=0; i<customParams.length; i++) {
		    	postProp.setProperty(CUSTOM+customParams[i][0].toLowerCase(), transformCustomValue(customParams[i][1]));
		    }
		    if (sendUTF8Encoded) 
		    	postProp.setProperty(BasicLTIUtilUocWrapper.PARAM_UTF8, "1");
		    if (sendISOEncoded)
		    	postProp.setProperty(BasicLTIUtilUocWrapper.PARAM_ISO, "1");
		    if (sendBase64Encoded)
		    	postProp.setProperty(BasicLTIUtilUocWrapper.PARAM_BASE64, "1");
		    
		    //Adding parameters from request
		    String parameterName="";
		    for (Enumeration<?> e=propertiesRequestParams.keys(); e.hasMoreElements();) {
		    	parameterName = (String)e.nextElement();
		    	postProp.setProperty(CUSTOM+parameterName.toLowerCase(), propertiesRequestParams.getProperty(parameterName));
		    }
		    //Afegim camps opcionals
		    postProp.setProperty(BasicLTIConstantsUocWrapper.CONTEXT_TYPE, BasicLTIConstantsUocWrapper.CONTEXT_TYPE_COURSE_SECTION);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_DOCUMENT_TARGET, show_pop_up?LAUNCH_PRESENTATION_WINDOW:LAUNCH_PRESENTATION_IFRAME);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_HEIGHT, preferredHeight);
//		    postProp.setProperty(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_WIDTH, arg1);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.LAUNCH_PRESENTATION_RETURN_URL, return_url);
		    //postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_RESULT_SOURCEDID, idInstance);
		    postProp.setProperty(BasicLTIConstantsUocWrapper.EXT_LMS, ext_lms);
//		    postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_COURSE_SECTION_SOURCEDID, org_id+":"+idInstance);
//		    postProp.setProperty(BasicLTIConstantsUocWrapper.LIS_COURSE_OFFERING_SOURCEDID, org_id+":"+idInstance);
		    
		 }catch (Exception e) {
			 log.error("Exception getting data from OKI "+e.getMessage(), e);
			 Exception exc = new Exception(e.getMessage());
			 exc.initCause(e);
			 throw exc;
		 }
	    
	    return postProp;
	  }
	 
	 

}