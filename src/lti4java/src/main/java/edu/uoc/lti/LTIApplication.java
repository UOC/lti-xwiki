package edu.uoc.lti;

import java.util.Date;

public class LTIApplication {
	
	private int id;
	private String toolurl;
	private String name;
	private String description;
	private String resourcekey;
	private String password;
	private String preferheight;
	private short sendname;
	private short sendemailaddr;
	private short acceptgrades;
	private short allowroster;
	private short allowsetting;
	private String customparameters;
	private short allowinstructorcustom;
	private String organizationid;
	private String organizationurl;
	private short launchinpopup;
	private boolean debugmode;
	private Date registered;
	private Date updated;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getToolurl() {
		return toolurl;
	}
	public void setToolurl(String toolurl) {
		this.toolurl = toolurl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResourcekey() {
		return resourcekey;
	}
	public void setResourcekey(String resourcekey) {
		this.resourcekey = resourcekey;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPreferheight() {
		return preferheight;
	}
	public void setPreferheight(String preferheight) {
		this.preferheight = preferheight;
	}
	public short getSendname() {
		return sendname;
	}
	public void setSendname(short sendname) {
		this.sendname = sendname;
	}
	public short getSendemailaddr() {
		return sendemailaddr;
	}
	public void setSendemailaddr(short sendemailaddr) {
		this.sendemailaddr = sendemailaddr;
	}
	public short getAcceptgrades() {
		return acceptgrades;
	}
	public void setAcceptgrades(short acceptgrades) {
		this.acceptgrades = acceptgrades;
	}
	public short getAllowroster() {
		return allowroster;
	}
	public void setAllowroster(short allowroster) {
		this.allowroster = allowroster;
	}
	public short getAllowsetting() {
		return allowsetting;
	}
	public void setAllowsetting(short allowsetting) {
		this.allowsetting = allowsetting;
	}
	public String getCustomparameters() {
		return customparameters;
	}
	public void setCustomparameters(String customparameters) {
		this.customparameters = customparameters;
	}
	public short getAllowinstructorcustom() {
		return allowinstructorcustom;
	}
	public void setAllowinstructorcustom(short allowinstructorcustom) {
		this.allowinstructorcustom = allowinstructorcustom;
	}
	public String getOrganizationid() {
		return organizationid;
	}
	public void setOrganizationid(String organizationid) {
		this.organizationid = organizationid;
	}
	public String getOrganizationurl() {
		return organizationurl;
	}
	public void setOrganizationurl(String organizationurl) {
		this.organizationurl = organizationurl;
	}
	public short getLaunchinpopup() {
		return launchinpopup;
	}
	public void setLaunchinpopup(short launchinpopup) {
		this.launchinpopup = launchinpopup;
	}
	public boolean isDebugmode() {
		return debugmode;
	}
	public void setDebugmode(boolean debugmode) {
		this.debugmode = debugmode;
	}
	public Date getRegistered() {
		return registered;
	}
	public void setRegistered(Date registered) {
		this.registered = registered;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
		
}
