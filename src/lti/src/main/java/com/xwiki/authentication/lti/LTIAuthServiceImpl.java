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
                if (usernameLTI.contains(":")) {
                    usernameLTI = usernameLTI.split(":")[1];
                }
                // get the group to assigna to the user.
                String group = LTIEnvironment.getParameter("custom_groups");
                
                return syncUser(usernameLTI, group, context, LTIEnvironment.isInstructor());
            } else {
                Exception lastException = LTIEnvironment.getLastException();
                log.info("Error LTI authentication "+(lastException!=null?lastException.getMessage():""));
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
    protected Principal syncUser(String user, String groupName, XWikiContext context, boolean isInstructor) throws XWikiException {
        String xwikiUser = super.findUser(user, context);
        String wikiNameShow = context.getWiki().getName();
        log.info("["+wikiNameShow+"] usernameLTI="+user+", GroupsLTI:"+groupName);
                
        if (xwikiUser == null) {
            log.info("["+wikiNameShow+"] LTI Create user: User " + user + " does not exist");
            String wikiname = context.getWiki().clearName(user, true, true, context);
            context.getWiki().createEmptyUser(wikiname, "edit", context);
            log.info("["+wikiNameShow+"] LTI Create user: User " + user + " has been created");
            xwikiUser = "XWiki."+user;
            //TODO: save email, name, ... parameters
            
            // if the user is "instructor", assign admin rights except in speakapps wiki
            if (isInstructor && !context.getWiki().getName().equalsIgnoreCase("speakapps")) {
                try {
                    this.addUserGroup(xwikiUser, user, "XWikiAdminGroup", context);
                    log.info("["+wikiNameShow+"] add admin member " + xwikiUser);
                }catch(Exception ex) {
                    log.warn("["+wikiNameShow+"] Execption adding admin user "+ex);
                }
            }
        }

        // És un group però no és l'administrador
        if (groupName != null && !groupName.equalsIgnoreCase("XWikiAdminGroup")) {
            try {
                this.addUserGroup(xwikiUser, user, groupName, context);
                log.info("["+wikiNameShow+"] add a \""+groupName+"\" member " + xwikiUser);
            }catch(Exception ex) {
                log.warn("["+wikiNameShow+"] Execption adding user a \""+groupName+"\" group "+ex);
            }
        }
        
        return new SimplePrincipal(context.getDatabase() + ":" + xwikiUser);
    }
    
    private boolean addUserGroup(String xwikiUser, String user, String groupName, XWikiContext context) throws XWikiException {
        boolean isAdded = false;
        Group group = Group.getGroup("XWiki", groupName, context);
        if (group != null && !group.isMember(xwikiUser, context)) {
            if (group.addUser(user, context)) {
                group.save(context);
                isAdded = true;
            }
        }
        
        return isAdded;
    }
}