lti-xwiki
=========

# Step-by-Step Guide


* Compile the XWiki LTI Component ./src/lti_certification/ (Maven project)

* Download XWiki [XWiki](http://xwiki.org/ "Xwiki") WAR

* Include the XWiki LTI component (JAR) ./src/lti_certification/target/authentication-lti-X.Y.Z.jar to XWiki WAR in /WEB-INF/lib/ path. You can download compiled one from https://github.com/UOC/lti-xwiki/tree/mvn-repo/edu/uoc/xwiki 

* Include all JAR dependencies from ./lib in the same path (/WEB-INF/lib/). You can download from repository https://github.com/UOC/lti-xwiki/tree/mvn-repo
      - JavaUtils-1.1.2.jar
      - lti-1.0.3.jar
      - oauth-20100527.jar
      - oauth-provider-20100527.jar

* Enable LTI authentication management: editing ./WEB-INF/xwiki.cfg

	> \#\-\# LTI authentication management

	> xwiki.authentication.authclass=com.xwiki.authentication.lti.LTIAuthServiceImpl

* Copy the file src/lti_certification/src/main/resources/authorizedConsumersKey.properties to  your Xwiki installation into folder WEB-INF

* Edit a new file called authorizedConsumersKey.properties This is a configuration to be read by XWiki provider to authorize the consumer key and gets the secret
    
    	consumer_key."name_consumer".enabled=1
    	consumer_key."name_consumer".secret=secret
    	consumer_key."name_consumer".callBackUrl=
    	consumer_key."name_consumer".fieldSessionId=token

	The file has configured a consumer key sample and secret as 12345, change it

* Restart your Tomcat or servlet container


