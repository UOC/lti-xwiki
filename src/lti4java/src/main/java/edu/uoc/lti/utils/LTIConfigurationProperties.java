package edu.uoc.lti.utils;

import java.io.IOException;

import org.osid.utils.UtilsProperties;

/**
 * This class extracts the configuration properties from the environment.
 * 
 * Config file location is first searched as a configuration parameter under @see PARAM_CONFIGFILE_LOCATION_KEY . 
 * If not found there the default is taken: @see PARAM_CONFIGFILE_LOCATION_DEFAULT .
 * 
 * Config file location can be one of (order matters): <OL>
 * <LI>Classpath entry. E.g.: classpath:/config/campusGateway.properties</LI>
 * <LI>URI. E.g.: http://www.tecsidel.es/campusGateway.properties</LI>
 * <LI>File. E.g.: C:/campusGateway.properties</LI>
 * </OL>
 * When config file location is set to @see PARAM_CONFIGFILE_LOCATION_VALUE_ENV  
 * then configuration properties are taken directly from the environment.
 * 
 * @author Roberto.Marrodan@tecsidel.es
 */
public class LTIConfigurationProperties {

	public static final String CONFIGFILE_ENVKEY = "LTIConfigFile";
	public static final String CONFIGFILE_LOCATION_DEFAULT = UtilsProperties.CLASSPATH_PREFIX + "/config/authorizedConsumersKey.cfg";
	public static final String PARAM_ENABLEDEBUGINFO = "enableDebugInfo";
	public static final boolean PARAM_ENABLEDEBUGINFO_DEFAULT = false;
	
	protected static UtilsProperties instance = null;
	protected static boolean enableDebugInfo;
	
	public static UtilsProperties getSingleton() throws IOException {
		ensureSingletonIsLoaded();
		return instance;
	}
	
	public static boolean provideDebugInfo() throws IOException {
		ensureSingletonIsLoaded();
		return enableDebugInfo;
	}
	
	public static synchronized void ensureSingletonIsLoaded() throws IOException {
		if (instance == null) {
			instance = new UtilsProperties(CONFIGFILE_LOCATION_DEFAULT, CONFIGFILE_ENVKEY, LTIConfigurationProperties.class);
			enableDebugInfo = instance.getPropertyAsBoolean(PARAM_ENABLEDEBUGINFO, PARAM_ENABLEDEBUGINFO_DEFAULT);
			instance.setDebugInformation(enableDebugInfo);
		}
	}
	
}
