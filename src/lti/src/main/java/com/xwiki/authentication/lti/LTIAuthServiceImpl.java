package com.xwiki.authentication.lti;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiServletRequest;
import edu.uoc.lti.LTIEnvironment;
import java.util.Enumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;

/**
 *
 * @author jdurancal
 */
@Component
public class LTIAuthServiceImpl extends XWikiAuthServiceImpl {

    private static final Log log = LogFactory.getLog(LTIAuthServiceImpl.class);

    public LTIAuthServiceImpl() throws XWikiException { 
        super();
    }
    
    @Override
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException {
        XWikiServletRequest request = (XWikiServletRequest) context.getRequest();
        
        try {
            String userR = request.getRemoteUser();
         Enumeration paramNames = request.getParameterNames();
    while(paramNames.hasMoreElements()) {
      String paramName = (String)paramNames.nextElement();
      String[] paramValues = request.getParameterValues(paramName);
      if (paramValues.length == 1) {
        String paramValue = paramValues[0];
        if (paramValue.length() == 0)
          System.out.print("Param:"+paramName+":No Value");
        else
          System.out.print("Param:"+paramName+":"+paramValue);
      } else {
        System.out.println("Param:"+paramName+"[m]:");
        for(int i=0; i<paramValues.length; i++) {
          System.out.println("-->" + paramValues[i]);
        }
      }
    }
            LTIEnvironment LTIEnvironment = new LTIEnvironment(request);                       
            if (LTIEnvironment.isAuthenticated()) {
                String username = LTIEnvironment.getUserName();
                if (username.contains(":")) {
                    username = username.split(":")[1];
                }
                /*
                 * if (username.startsWith(LTIEnvironment.getResourcekey()+":"))
                 * { username =
                 * username.substring((LTIEnvironment.getResourcekey()+":").length());
                }
                 */

//                String full_name = LTIEnvironment.getFullName();
//                String email = LTIEnvironment.getEmail();
//                String user_image = LTIEnvironment.getUser_image();
//
//                String course_key = LTIEnvironment.getCourseKey();
//                String course_label = LTIEnvironment.getCourseName();
//
//                String locale = LTIEnvironment.getLocale();

                //1. Create user if its needded
                String user = createUser(username, context);
                context.setUser(user);
                //context.
                
                //2. Create group
                //XWikiDocument xwikiDocument = new XWikiDocument(course_key, course_label);
                //xwikiDocument.setLanguage(locale);

                /*
                 * Group course = new Group(xwikiDocument, context); if
                 * (!course.isMember(user, context)) { course.addUser(user,
                 * context); }
                 */
                
                return new XWikiUser(user);
            
            } else {
                Exception lastException = LTIEnvironment.getLastException();
                log.warn("Error LTI authentication "+(lastException!=null?lastException.getMessage():""));
                return super.checkAuth(context);
                
            }
            
        }catch(Exception ex) {
            log.warn("Execption authentication "+ex);
            return super.checkAuth(context);
        }
    }
    
    /**
     * Creates the user if he doesn't exist in the XWiki repository. User is assigned
     * to the default XWikiAllGroup
     * @param user
     * @param context
     * @throws com.xpn.xwiki.XWikiException
     */
    @Override
    protected String createUser(String user, XWikiContext context) throws XWikiException {
        String xwikiUser = super.findUser(user, context);
        if (xwikiUser == null) {
            log.debug("LTI Create user: User " + xwikiUser + " does not exist");
            String wikiname = context.getWiki().clearName(user, true, true, context);
            context.getWiki().createEmptyUser(wikiname, "edit", context);
            log.debug("LTI Create user: User " + xwikiUser + " has been created");
        } 
        return xwikiUser;
    }
}