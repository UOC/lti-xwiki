package com.xwiki.authentication.lti;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.userdirectory.Group;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiServletRequest;
import edu.uoc.lti.LTIEnvironment;
import java.security.Principal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;
import org.xwiki.component.annotation.Component;

/**
 *
 * @author jdurancal
 */
@Component
public class LTIAuthServiceImpl extends XWikiAuthServiceImpl {

    private static final Log log = LogFactory.getLog(LTIAuthServiceImpl.class);
        
    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException {
        XWikiServletRequest request = (XWikiServletRequest) context.getRequest();

        try {
            LTIEnvironment LTIEnvironment = new LTIEnvironment(request);
            if (LTIEnvironment.isAuthenticated()) {
                String usernameLTI = LTIEnvironment.getUserName();
                log.debug("usernameLTI="+usernameLTI);
                if (usernameLTI.contains(":")) {
                    usernameLTI = usernameLTI.split(":")[1];
                }
                // get the group to assigna to the user.
                String group = LTIEnvironment.getParameter("custom_groups");
                System.out.println("GroupsLTI:"+group);
                
                return syncUser(usernameLTI, group, context);
            } else {
                Exception lastException = LTIEnvironment.getLastException();
                log.debug("Error LTI authentication "+(lastException!=null?lastException.getMessage():""));
            }
            
        }catch(Exception ex) {
            log.warn("Execption authentication "+ex);
        }
        
        // Fallback on standard XWiki authentication
        return super.authenticate(username, password, context);
    }
    

    /**
     * Creates the user if he doesn't exist in the XWiki repository. User is assigned
     * to the default XWikiAllGroup
     * @param user
     * @param context
     * @throws com.xpn.xwiki.XWikiException
     */
    protected Principal syncUser(String user, String groupName, XWikiContext context) throws XWikiException {
        String xwikiUser = super.findUser(user, context);
        if (xwikiUser == null) {
            log.debug("LTI Create user: User " + user + " does not exist");
            String wikiname = context.getWiki().clearName(user, true, true, context);
            context.getWiki().createEmptyUser(wikiname, "edit", context);
            log.debug("LTI Create user: User " + user + " has been created");
            xwikiUser = "XWiki."+user;
            //TODO: passar-hi els paràmetres d'email, nom, etc..
        }

        // És un group però no és l'administrador
        if (groupName != null && !groupName.equalsIgnoreCase("XWikiAdminGroup")) {
            System.out.println("add member " + xwikiUser);
            log.info("add member " + xwikiUser);
            Group group = Group.getGroup("XWiki", groupName, context);
            if (group != null && !group.isMember(xwikiUser, context)) {
                System.out.println("exist group " + groupName);
                log.info("exist group " + groupName);
                if (group.addUser(user, context)) {
                    System.out.println("is add");
                    try {
                        group.save(context);
                    } catch (Exception e) {
                        log.warn(e);
                    }
                }
            } else {
                System.out.println("no exist group " + groupName);
                log.info("no exist group " + groupName);
            }
        }
        
        return new SimplePrincipal(context.getDatabase() + ":" + xwikiUser);
    }
    
    
    //TODO: assignar usuaris a grups, útil per assignar estudiants a "aules"
//2. Create group
//                XWikiDocument xwikiDocument = new XWikiDocument(course_key, course_label);
//                xwikiDocument.setLanguage(locale);
//
//                Group course = new Group(xwikiDocument, context); if
//                (!course.isMember(user, context)) { course.addUser(user,
//                context); }   
}