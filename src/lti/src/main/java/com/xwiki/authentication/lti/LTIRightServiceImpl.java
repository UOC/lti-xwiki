/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xwiki.authentication.lti;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

/**
 *
 * @author jdurancal
 */
public class LTIRightServiceImpl extends XWikiRightServiceImpl {
    
    @Override
    public boolean checkRight(String name, XWikiDocument doc, String accessLevel, boolean user, boolean allow, boolean global, XWikiContext context) throws XWikiRightNotFoundException, XWikiException {
        return super.checkRight(name, doc, accessLevel, user, allow, global, context);
    }
    
}
