lti-xwiki
=========

# Step-by-Step Guide


* Compile the XWiki LTI Component ./src/lti_certification/ (Maven project)

* Download XWiki [XWiki](http://xwiki.org/ "Xwiki") WAR

* Include the XWiki LTI component (JAR) ./src/lti_certification/target/authentication-lti-X.Y.Z.jar to XWiki WAR in 

/WEB-INF/lib/ path

* Include all JAR dependencies from ./lib in the same path (/WEB-INF/lib/)
      - JavaUtils-1.1.2.jar
      - lti-1.0.2.jar
      - oauth-20100527.jar
      - oauth-provider-20100527.jar

* Enable LTI authentication management: editing ./WEB-INF/xwiki.cfg

	> #-# LTI authentication management
	> xwiki.authentication.authclass=com.xwiki.authentication.lti.LTIAuthServiceImpl

* Create a directory called /home/campus/configHome/lti

* Edit a new file called authorizedConsumersKey.cfg inside the path /home/campus/configHome/lti

	> #This is a configuration to be readed by providers to authorize the consumer key and gets the secret
	> #consumer_key."name_consumer".enabled=1
	> #consumer_key."name_consumer".secret=secret

* Deploy XWiki WAR


