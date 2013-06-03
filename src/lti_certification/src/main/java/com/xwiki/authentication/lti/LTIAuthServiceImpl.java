/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.xwiki.authentication.lti;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.LargeStringProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiServletRequest;
import edu.uoc.lti.LTIEnvironment;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.securityfilter.realm.SimplePrincipal;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 *
 * @author jdurancal
 */
@Component
public class LTIAuthServiceImpl extends XWikiAuthServiceImpl {

    private static final int ADMINISTRATOR = 0;
    private static final int INSTRUCTOR = 1;
    private static final int STUDENT = 2;
    private static final int GUEST = 3;
    
    private static final String [] ROLES_STR = {"XWikiAdminGroup", "Instructor", "Student" , "Guest"};
    private static final String [] ROLES_RIGHTS = {"admin,edit,view,comment,delete,undelete,register,programming", "admin,edit,view,comment,delete,undelete", "edit,view,comment" , "view"};

    private static final Logger logger = Logger.getLogger(LTIAuthServiceImpl.class.getName());
        
    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException {

        XWikiServletRequest request = (XWikiServletRequest) context.getRequest();

        if (!context.getAction().equalsIgnoreCase("logout")) {
            String wikiNameShow = context.getDatabase();

            try {
                LTIEnvironment LTIEnvironment = new LTIEnvironment(request);

                if (LTIEnvironment.isAuthenticated()) {
                    // get role
                    int userRole = GUEST;
                    if (LTIEnvironment.isCourseAuthorized()) {
                        userRole = STUDENT;
                        if (LTIEnvironment.isInstructor()) {
                            String rolesParam = LTIEnvironment.getParameter("roles");
                            if (rolesParam != null && rolesParam.toLowerCase().contains("administration")) {
                                userRole = ADMINISTRATOR;
                            } else {
                                userRole = INSTRUCTOR;
                            }
                        }
                    }

                    // get user name
                    String userName = LTIEnvironment.getUserName();
                    if (userName.contains(":")) {
                        userName = userName.split(":")[1];
                    }
                    userName = userName.replaceAll("[\\.|:]", "_");

                    // get unique group name
                    String groupName = LTIEnvironment.getResourceKey();
                    if (userRole != ADMINISTRATOR) {
                        groupName += "_" + ROLES_STR[userRole];
                    } else {
                        groupName = "XWikiAdminGroup";
                    }
                    groupName = groupName.replaceAll("[\\.|:]", "_");

                    // get space name
                    String spaceName = "";
                    if (LTIEnvironment.getResourceTitle() != null) {
                        spaceName = LTIEnvironment.getCourseName();
                    }
                    if (LTIEnvironment.getCourseName() != null) {
                        spaceName = LTIEnvironment.getCourseName() + (("".equals(spaceName)) ? "" : "-" + spaceName);
                    }
                    if ("".equals(spaceName)) {
                        spaceName = LTIEnvironment.getResourceKey();
                    }

                    logger.finest("[" + wikiNameShow + "] user: " + userName
                            + ", group: " + groupName
                            + ", space: " + spaceName
                            + ", role: " + userRole);

                    // synchronize user
                    String xwikiUser = syncUser(userName, context, LTIEnvironment);

                    // synchronize group
                    String xwikiGroup = syncGroup(xwikiUser, groupName, context, LTIEnvironment);

                    // synchronize space
                    syncSpace(spaceName, xwikiGroup, ROLES_RIGHTS[userRole], context);

                    context.setURL(new URL("http://www.google.com"));
                    return new SimplePrincipal(context.getDatabase() + ":" + xwikiUser);

                } else {
                    Exception lastException = LTIEnvironment.getLastException();
                    if (lastException != null && lastException.getMessage() != null) {
                        logger.warning("[" + wikiNameShow + "] Error LTI authentication " + lastException.getMessage());
                    }
                }

            } catch (MalformedURLException ex) {
                logger.warning("URL authentication fail " + ex);
            } catch (Exception ex) {
                logger.warning("[" + wikiNameShow + "] Execption authentication " + ex);
            }
        }
        
        // Fallback on standard XWiki authentication
        return super.authenticate(username, password, context);
    }
    

    /**
     * Creates the user if he/she doesn't exist in the XWiki repository. User is assigned
     * to the default XWikiAllGroup
     * @param user
     * @param context
     * @throws com.xpn.xwiki.XWikiException
     */
    private synchronized String syncUser(String userName, XWikiContext context, LTIEnvironment lti) throws XWikiException {
        String xwikiUser = super.findUser(userName, context);
        String wikiNameShow = context.getDatabase();
        
        if (xwikiUser == null) {
            HashMap<String, Object> userDetails = new HashMap<String, Object>();
            if (lti.getEmail() != null) {
                userDetails.put("first_name", lti.getFullName());
            }
            if (lti.getEmail() != null) {
                userDetails.put("email", lti.getEmail());
            }
            
            if (context.getWiki().createUser(userName, userDetails, context) == 1) {
                logger.info("["+wikiNameShow+"] Created XWiki-User '" + userName + "'");
                xwikiUser = "XWiki."+userName;
            } else {
                logger.warning("["+wikiNameShow+"] Creation of user '" + userName + "' in XWiki failed!");
                throw new XWikiException();
            }
        }
        
        return xwikiUser;
    }
    
    
    private synchronized String syncGroup(String xwikiUser, String groupName, XWikiContext context, LTIEnvironment LTIEnvironment) throws XWikiException {
        String xwikiGroup = "";
        
        try {
            if (groupName != null && !"".equals(groupName)) {
                BaseClass groupClass = context.getWiki().getGroupClass(context);

                // Get document representing group
                DocumentReference groupDocumentReference = new DocumentReference(context.getDatabase(), XWiki.SYSTEM_SPACE, groupName);
                XWikiDocument groupDoc = context.getWiki().getDocument(groupDocumentReference, context);
                synchronized (groupDoc) {
                    boolean memberFound = false;
                    if (groupDoc.isNew()) {
                        groupDoc.setSyntax(Syntax.XWIKI_2_0);
                        groupDoc.setContent("{{include document='XWiki.XWikiGroupSheet' /}}");
                    } else {
                        try {
                            Iterator<BaseObject> iMembers = groupDoc.getXObjects(groupClass.getDocumentReference()).iterator();
                            while (iMembers.hasNext() && !memberFound) {
                                StringProperty propertyMember = (StringProperty)iMembers.next().getField("member");
                                if (propertyMember.getValue().equals(xwikiUser)) {
                                    memberFound = true;
                                }
                            }
                        } catch (NullPointerException ex) {
                        }
                    }

                    if (!memberFound) {
                        // Add a member object to document
                        BaseObject memberObj = groupDoc.newXObject(groupClass.getDocumentReference(), context);
                        memberObj.setStringValue("member", xwikiUser);
                        context.getWiki().saveDocument(groupDoc, context);
                        logger.info("[" + context.getDatabase() + "] add a \"" + groupName + "\" member " + xwikiUser);
                    }
                    
                    xwikiGroup = "XWiki."+groupName;
                }
            } else {
                throw new XWikiException();
            }

        } catch (Exception e) {
           logger.warning(MessageFormat.format("Failed to add a user [{0}] to a group [{1}] :"+e, xwikiUser, groupName));
           throw new XWikiException();
        }
        
        return xwikiGroup;
    }

    
    private synchronized void syncSpace(String spaceName, String groupName, String rightLevel, XWikiContext context) throws XWikiException {
        DocumentReference spaceDocumentReference = new DocumentReference(context.getDatabase(), spaceName, "WebHome");
        XWikiDocument spaceDoc = context.getWiki().getDocument(spaceDocumentReference, context);
        synchronized (spaceDoc) {
            // Get the space preferences page, where space level rights are saved.
            BaseClass globalRightsClass = context.getWiki().getGlobalRightsClass(context);
            DocumentReference spacePrefsRef = new DocumentReference(context.getDatabase(), spaceName, "WebPreferences");
            XWikiDocument spacePrefs = context.getWiki().getDocument(spacePrefsRef, context);
            synchronized (spacePrefs) {
                if (spaceDoc.isNew()) {
                    context.getWiki().saveDocument(spaceDoc, context);
                    logger.info("Create space: " + spaceName);
                }
            
                // Get the XWikiGlobalRights from the space preferences page.
                boolean groupRightsFound = false;
                List<BaseObject> spaceRights = spacePrefs.getXObjects(globalRightsClass.getDocumentReference());
                if (spaceRights != null && !spaceRights.isEmpty()) {
                    Iterator<BaseObject> itSpaceRights = spaceRights.iterator();
                    while (!groupRightsFound && itSpaceRights.hasNext()) {
                        BaseObject spaceRight = itSpaceRights.next();
                        LargeStringProperty groupProperty = (LargeStringProperty) spaceRight.getField("groups");
                        if (groupProperty.getValue().equalsIgnoreCase(groupName)) {
                            groupRightsFound = true;
                        }
                    }
                }

                if (!groupRightsFound) {
                    // Modify space rights.
                    BaseObject newRights = spacePrefs.newXObject(globalRightsClass.getDocumentReference(), context);
                    newRights.set("groups", groupName, context);
                    newRights.set("levels", rightLevel, context);
                    newRights.set("allow", 1, context);
                    /*if (rightLevel.equalsIgnoreCase("edit") || rightLevel.equalsIgnoreCase("admin") ) {
                        BaseObject newViewRights = spacePrefs.newXObject(globalRightsClass.getDocumentReference(), context);
                        newRights.set("groups", groupName, context);
                        newRights.set("levels", rightLevel, context);
                        newRights.set("allow", 1, context);
                    }*/
                    context.getWiki().saveDocument(spacePrefs, context);
                    logger.info("Space group righs udate: " + spaceName + "[" + groupName + ", " + rightLevel + "]");
                }
            }
        }
    }
}