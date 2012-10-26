package edu.uoc.lti;

//import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;


import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import edu.uoc.lti.utils.LTIConfigurationProperties;
import edu.uoc.lti.utils.Utils;

import org.osid.utils.PropertyNotFoundException;
import org.osid.utils.UtilsProperties;

public class LTIEnvironment {

    private boolean authenticated = false;
    private String userName = "";
    private String fullName = "";  
    private String email = "";
	private boolean isAuthorized = false;
	private boolean isInstructor = false;
	private String courseKey = null;
	private String courseName = null;
	private String idRoom = null;
	private Properties parameterMap = null;
	private String topicRoom = null;
	private String descriptionRoom = null;
	private String locale = null;
	private String user_image = null;
	private String field_session_id = null;
	private String session_id = null; 
	private final static Logger log = Logger.getLogger(LTIEnvironment.class);
	private Exception lastException = null;
	private UtilsProperties ltiConfigurationProperties;
	
	
	public LTIEnvironment() {
		loadProperties();
	}

	private void loadProperties() {
		try {
			ltiConfigurationProperties = LTIConfigurationProperties.getSingleton();
		} catch (IOException ioe) {
			log.error("Error LTI Environment loading properties "+ioe.getLocalizedMessage(), ioe);
		}
	}
	
	public LTIEnvironment(HttpServletRequest request) {

		loadProperties();
		lastException = null;
		boolean is_lti = false;
		  String oauth_consumer_key = request.getParameter("oauth_consumer_key");
		  String resource_link_id = request.getParameter("resource_link_id");
		  if ( "basic-lti-launch-request".equals(request.getParameter("lti_message_type")) &&
				    "LTI-1p0".equals(request.getParameter("lti_version")) ||
				    oauth_consumer_key != null || resource_link_id != null ) {
			  is_lti = true;
		  }
		  
		  if (is_lti) {
		  
			  try {
			  OAuthMessage oam = OAuthServlet.getMessage(request, null);
			  OAuthValidator oav = new SimpleOAuthValidator();
			  OAuthConsumer cons = getValidConsumerKey(oauth_consumer_key);
			  
			  OAuthAccessor acc = new OAuthAccessor(cons);

			  oav.validateMessage(oam,acc);
			  this.setAuthenticated(true);
			    
			  //Afegir el consumer
			  String name = null;//request.getParameter("name");
			  //UOC Wrapper to decode
			  OAuthMessageUocUtil wrapperUOC = new  OAuthMessageUocUtil(oam);
			  
			  String courseKey = wrapperUOC.getCourseKey();
			  courseKey = courseKey.replaceAll(":", "_");
			  String courseName = wrapperUOC.getCourseName();
			  String resourceTitle = wrapperUOC.getResourceTitle();
			  if (resourceTitle==null)
				  resourceTitle = courseName;
			  String resourceDescription =wrapperUOC.getResourceDescription();
			  if (resourceDescription == null)
				  resourceDescription = resourceTitle;
			  String resourceKey = wrapperUOC.getResourceKey();
				  
			  //TODO treure aixo
			  if ("uoc.edu".equals(wrapperUOC.getConsumerKey())) {
				  resourceKey = wrapperUOC.getResourceKey()+"-"+this.getCourseKey();
				  resourceTitle = wrapperUOC.getCourseName();
				  resourceDescription =wrapperUOC.getCourseName();
			  }

			  String sessionId = null;
			  if (this.field_session_id!=null) {
				  sessionId = wrapperUOC.getParameter(this.field_session_id);
			  }	  				
			  initSetParams(true, courseKey, courseName, resourceTitle, resourceDescription,
						resourceKey, wrapperUOC.getUserEmail(), wrapperUOC.getUserKey(), wrapperUOC.getUserName(), wrapperUOC.getParameterMap(), 
						wrapperUOC.isInstructor(), wrapperUOC.isInstructor() || wrapperUOC.isLearner(),
						sessionId, wrapperUOC.getUserImage(),  wrapperUOC.getPresentationLocale());
			  request.getSession().setAttribute("LTI", this);
			
		  } catch(Exception e) {
			  lastException = e;  
			log.error("Error LTI Environment "+e.getLocalizedMessage(), e);
			this.setAuthenticated(false);  
			
		  }
		} else {
			if (request.getSession().getAttribute("LTI")!=null) {
				LTIEnvironment lti = (LTIEnvironment) request.getSession().getAttribute("LTI");
				
				 initSetParams(lti.isAuthenticated(), lti.getCourseKey(), lti.getCourseName(), lti.getTopicRoom(), lti.getDescriptionRoom(),
							lti.getIdRoom(), lti.getEmail(), lti.getUser_image(), lti.getFullName(), lti.getParameterMap(), 
							lti.isInstructor(), lti.isDomainAuthorized(),
							/*lti.getLtiApplicationList(), lti.getRemoteApplicationList(), */lti.getSessionId(), lti.getUser_image(),  lti.getLocale());
				  
			} else {
				lastException = new Exception ("Is not a valid LTI Call");
			}
		}

	}
	
	private void initSetParams(boolean isAuthenticated, String courseKey, String courseName, String resourceTitle, String resourceDescription,
			String resourceKey, String userEmail, String userKey, String userName, Properties parameters, boolean isInstructor, boolean isAuthorized,
			/*List<LTIApplication> ltiApplicationList, List<RemoteApplication> remoteApplicationList, */String sessionId, String userImage, String presentationLocale) {
		  this.setAuthenticated(isAuthenticated);
		    
		  this.setEmail(userEmail);
		  this.setUserName(userKey);
		  this.setFullName(userName);
		  this.setParameterMap(parameters);
		  this.setInstructor(isInstructor);
		  this.setAuthorized(isAuthorized);
		  
		    
		  this.setCourseKey(courseKey);
		  this.setCourseName(courseName);
		  this.setTopicRoom(resourceTitle);
		  this.setDescriptionRoom(resourceDescription);
		  this.setIdRoom(resourceKey);
		  
/*		  this.setLtiApplicationList(ltiApplicationList);
		  this.setRemoteApplicationList(remoteApplicationList);
	*/		
		  if (sessionId!=null) {
			  this.setSessionId(sessionId);
		  }

		  this.setUser_image(userImage);
		  this.setLocale(presentationLocale);
		  
	}
	
	/**
	 * Get if consumer key is valid
	 * @param oauth_consumer_key
	 * @return
	 * @throws Exception
	 */
	private OAuthConsumer getValidConsumerKey(String oauth_consumer_key) throws Exception {
			OAuthConsumer cons = null;

			try {
				
				//20121025 - @abertranb - using Utils property class
				String consumer_enabled = ltiConfigurationProperties.getProperty("consumer_key."+oauth_consumer_key+".enabled");
				String consumer_secret = ltiConfigurationProperties.getProperty("consumer_key."+oauth_consumer_key+".secret");
				String consumer_callBackUrl = ltiConfigurationProperties.getProperty("consumer_key."+oauth_consumer_key+".callBackUrl");
				this.field_session_id = ltiConfigurationProperties.getProperty("consumer_key."+oauth_consumer_key+".fieldSessionId");
				//******** ORIGINAL
				/*Properties prop = new Properties();
			InputStream is = this.getClass().getResourceAsStream(PROPERTIES_PATH); 
			try {
				prop.load(is);
			} catch (IOException io) {
				Exception e = new Exception ("Error loading file authorization consumer key from "+PROPERTIES_PATH);
				e.initCause(io);
				throw e;
			}
			String consumer_enabled = prop.getProperty("consumer_key."+oauth_consumer_key+".enabled");
			String consumer_secret = prop.getProperty("consumer_key."+oauth_consumer_key+".secret");
			String consumer_callBackUrl = prop.getProperty("consumer_key."+oauth_consumer_key+".callBackUrl");
			this.field_session_id = prop.getProperty("consumer_key."+oauth_consumer_key+".fieldSessionId");
			*/
				//********* END
				if (consumer_enabled!=null && consumer_secret!=null && consumer_callBackUrl!=null) {
					if ("1".equals(consumer_enabled)) {
						cons = new OAuthConsumer(consumer_callBackUrl, oauth_consumer_key, consumer_secret, null);
					} else {
						throw new Exception("Error consumer LTI "+oauth_consumer_key+" is not enabled");
					}
		
				} else {
					throw new Exception("Error unknown consumer LTI "+oauth_consumer_key);
				}
			} catch (PropertyNotFoundException pne) {
				throw new Exception("Error Property Not found exception consumer LTI "+oauth_consumer_key+" "+pne.getMessage());
			}
		  return cons;
		
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	public void setUserName(String username) {
		this.userName = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
    public String getEmail() {
		return email;
	}

	public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getFullName() {
        return this.fullName;
    }

   public boolean isDomainAuthorized() {

	   return this.isAuthorized;
   }
    
	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public void setInstructor(boolean isInstructor) {
		this.isInstructor = isInstructor;
	}
	
	public boolean isInstructor() {
		return this.isInstructor;
	}

	public String getCourseKey() {
		return courseKey;
	}

	public void setCourseKey(String courseKey) {
		if (courseKey!=null)
			courseKey = courseKey.replaceAll(":", "_");
		
		this.courseKey = courseKey;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Properties getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Properties parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public String getParameter(String property) {
		String ret = null;
		if (this.parameterMap!=null)
			ret = (String) this.parameterMap.getProperty(property);
		return ret;
	}

	public String getCustomParameter(String property, HttpServletRequest request) {
		
		String ret = request.getParameter(property) ;
		if (ret == null)
				ret = this.getParameter(BasicLTIConstantsUocWrapper.CUSTOM_PREFIX+property);
		return ret;
	}

	public void setTopicRoom(String topicRoom) {
		this.topicRoom = topicRoom;
	}

	public String getTopicRoom() {
		return topicRoom;
	}

	public void setDescriptionRoom(String descriptionRoom) {
		this.descriptionRoom = descriptionRoom;
	}

	public String getDescriptionRoom() {
		return descriptionRoom;
	}

	public String getIdRoom() {
		return idRoom;
	}

	public void setIdRoom(String idRoom) {
		this.idRoom = idRoom;
	}

	public void setSessionId(String session_id) {
		this.session_id = session_id;
	}
	
	public String getSessionId() {
		return this.session_id;
	}

	/*public List<LTIApplication> getLtiApplicationList() {
		return ltiApplicationList;
	}

	public void setLtiApplicationList(List<LTIApplication> ltiApplicationList) {
		this.ltiApplicationList = ltiApplicationList;
	}*/

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getUser_image() {
		return user_image;
	}

	public void setUser_image(String user_image) {
		this.user_image = user_image;
	}
/*
	public List<RemoteApplication> getRemoteApplicationList() {
		return remoteApplicationList;
	}

	public void setRemoteApplicationList(List<RemoteApplication> remoteApplicationList) {
		this.remoteApplicationList = remoteApplicationList;
	}
	*/
	
	public Exception getLastException() {
		return lastException;
	}
}
