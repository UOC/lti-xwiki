# This is a repo for dependencies.

You have to add the repository in pom.xml file:

    <repositories>
        <repository>
            <id>lti-xwiki-mvn-repo</id>
            <url>https://raw.github.com/UOC/lti-xwiki/mvn-repo/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
    
...


    <dependency>
      <groupId>edu.uoc</groupId>
      <artifactId>lti</artifactId>
      <version>1.0.3</version>
      <type>jar</type>
    </dependency>

    
