package edu.uoc.lti;

public class BasicLTIConstantsUocWrapper extends org.imsglobal.basiclti.BasicLTIConstants {

	/**
	   * user_id=0ae836b9-7fc9-4060-006f-27b2066ac545
	   * <p>
	   * This attribute specifies the URI for an image of the user who launched this request.  
	   * This image is suitable for use as a "profile picture" or an avatar representing the user.  
	   * This parameter is optional.
	   */
	  public static final String USER_IMAGE = "user_image";

	/**
	   * moodle-1		For Moodle 1.x
	   * sakai-2			For Sakai 2.8
	   * olat-7.1			For OLAT 7.
	   * <p>
	   * In order to better assist tools in using extensions and also making their user interface fit into the LMS that they are being called from, each LMS is encouraged to include the ext_lms parameter.  This is a simple vocabulary that is the name of the learning management system followed by version information.  Possible example values for this field might be:
	   */
	  public static final String EXT_LMS = "ext_lms";

	  /**
	   * Utility array useful for validating property names when building launch
	   * data.
	   */
	  public static final String[] validPropertyNames = { CONTEXT_ID,
	      CONTEXT_LABEL, CONTEXT_TITLE, CONTEXT_TYPE,
	      LAUNCH_PRESENTATION_DOCUMENT_TARGET, LAUNCH_PRESENTATION_HEIGHT,
	      LAUNCH_PRESENTATION_LOCALE, LAUNCH_PRESENTATION_RETURN_URL,
	      LAUNCH_PRESENTATION_WIDTH, LIS_PERSON_CONTACT_EMAIL_PRIMARY,
	      LAUNCH_PRESENTATION_CSS_URL,
	      LIS_PERSON_NAME_FAMILY, LIS_PERSON_NAME_FULL, LIS_PERSON_NAME_GIVEN,
	      LIS_PERSON_SOURCEDID, LIS_COURSE_OFFERING_SOURCEDID, 
	      LIS_COURSE_SECTION_SOURCEDID, LIS_RESULT_SOURCEDID,
	      LTI_MESSAGE_TYPE, LTI_VERSION, RESOURCE_LINK_ID, 
	      RESOURCE_LINK_TITLE, RESOURCE_LINK_DESCRIPTION, ROLES,
	      TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL, TOOL_CONSUMER_INSTANCE_DESCRIPTION,
	      TOOL_CONSUMER_INSTANCE_GUID, TOOL_CONSUMER_INSTANCE_NAME,
	      TOOL_CONSUMER_INSTANCE_URL, USER_ID, USER_IMAGE };
	  
	  /**
	   * Indica si el resultat ha de ser en JSON
	   */
	  public static final String JSON_OUTPUT = "json_output";
	  
	  /**
	   * Output para HTML
	   */
	  public static final String CONTENT_TYPE_OUTPUT_HTML = "text/html";

	  /**
	   * Output para JSON
	   */
	  public static final String CONTENT_TYPE_OUTPUT_JSON = "application/json";
	  
	  public static final String ADMINISTRATOR_ROLE = "Administrator";
	  public static final String STUDENT_ROLE = "Learner";
	  public static final String INSTRUCTOR_ROLE = "Instructor";
	  public static final String GUEST_ROLE = "urn:lti:instrole:ims/lis/Guest";
	  public static final String OTHER_ROLE = "urn:lti:instrole:ims/lis/Other";

	  /**
	   * CustomUSername
	   */
	  public static final String CUSTOM_USERNAME = "custom_username";
	  
}
