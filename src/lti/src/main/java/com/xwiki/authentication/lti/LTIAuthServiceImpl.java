package com.xwiki.authentication.lti;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.userdirectory.Group;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiServletRequest;
import edu.uoc.lti.LTIEnvironment;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.securityfilter.realm.SimplePrincipal;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

/**
 *
 * @author jdurancal
 */
@Component
public class LTIAuthServiceImpl extends XWikiAuthServiceImpl {

    // TODO: fer que el logger funcioni
    //@Inject
    //private Logger logger;
        
    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException {
        XWikiServletRequest request = (XWikiServletRequest) context.getRequest();

        if (!context.getAction().equalsIgnoreCase("logout")) {
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
                    System.out.println("Error LTI authentication " + (lastException != null ? lastException.getMessage() : ""));
                }
                
            } catch (Exception ex) {
                System.out.println("Execption authentication " + ex);
            }
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
        String wikiNameShow = context.getDatabase();
        System.out.println("["+wikiNameShow+"] usernameLTI="+user+", GroupsLTI:"+groupName);
                
        if (xwikiUser == null) {
            System.out.println("["+wikiNameShow+"] LTI Create user: User " + user + " does not exist");
            String wikiname = context.getWiki().clearName(user, true, true, context);
            context.getWiki().createEmptyUser(wikiname, "edit", context);
            System.out.println("["+wikiNameShow+"] LTI Create user: User " + user + " has been created");
            xwikiUser = "XWiki."+user;
            //TODO: save email, name, ... parameters
            
            // if the user is "instructor", assign admin rights except in speakapps wiki
            if (isInstructor && !context.getWiki().getName().equalsIgnoreCase("speakapps")) {
                try {
                    this.addUserGroup(xwikiUser, "XWikiAdminGroup", context);                   
                }catch(Exception ex) {
                    System.out.println("["+wikiNameShow+"] Execption adding admin user "+ex);
                }
            }
        }

        // És un group però no és l'administrador
        if (groupName != null && !groupName.equalsIgnoreCase("XWikiAdminGroup")) {
            try {
                this.addUserGroup(xwikiUser, groupName, context);
            }catch(Exception ex) {
                System.out.println("["+wikiNameShow+"] Execption adding user a \""+groupName+"\" group "+ex);
            }
        }
        
        return new SimplePrincipal(context.getDatabase() + ":" + xwikiUser);
    }
    
    private boolean addUserGroup(String xwikiUserName, String groupName, XWikiContext context) throws XWikiException {
        try {
            BaseClass groupClass = context.getWiki().getGroupClass(context);

            // Get document representing group
            DocumentReference groupDocumentReference = new DocumentReference(context.getDatabase(), XWiki.SYSTEM_SPACE, groupName);
            XWikiDocument groupDoc = context.getWiki().getDocument(groupDocumentReference, context);

            Group group = Group.getGroup("XWiki", groupName, context);
            synchronized (groupDoc) {
                if (group != null && !group.isMember(xwikiUserName, context)) {

                    // Add a member object to document
                    BaseObject memberObj = groupDoc.newXObject(groupClass.getDocumentReference(), context);
//                    Map<String, String> map = new HashMap<String, String>();
//                    map.put("member", xwikiUserName);
//                    groupClass.fromMap(map, memberObj);
                    memberObj.setStringValue("member", xwikiUserName);

                    // Save modifications
                    context.getWiki().saveDocument(groupDoc, context);
                    
                    System.out.println("["+context.getDatabase()+"] add a \""+groupName+"\" member " + xwikiUserName);
                }
            }

        } catch (Exception e) {
            System.out.println(MessageFormat.format("Failed to add a user [{0}] to a group [{1}]", xwikiUserName, groupName));
            return false;
        }
        
        return true;
    }   
}