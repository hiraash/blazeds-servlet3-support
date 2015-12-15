  * Install Tomcat 7 and Blaze DS 4. The turnkey install of BlazeDS 4 is not going to work since it ships with Tomcat 6.

  * Once you have Tomcat and BlazeDS working fine, change the Tomcat Connector configuration to use the NIO connector instead of the BIO conntector. The setting is in the server.xmland should be changed as follows.
```
<!--<Connector port="8400" protocol="HTTP/1.1"
 connectionTimeout="20000" redirectPort="8443"/>-->
<Connector connectionTimeout="20000" port="8400" 
 protocol="org.apache.coyote.http11.Http11NioProtocol" redirectPort="8443"/>
```

  * In your Tomcat BlazeDS application context change the Servlet configuration to allow asynchronous capability for the MessageBrokerServlet. This should be done in the web.xml as follows. Notice the lines marked by `*`
```
<servlet>
  <servlet-name>MessageBrokerServlet</servlet-name>
  <display-name>MessageBrokerServlet</display-name>
  <servlet-class>flex.messaging.MessageBrokerServlet</servlet-class>
  <init-param>
    <param-name>services.configuration.file</param-name>
    <param-value>/WEB-INF/flex/services-config.xml</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
  *<async-supported>true</async-supported>*
</servlet>
<filter>
  <filter-name>springSecurityFilterChain</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  *<async-supported>true</async-supported>*
</filter>
```

  * Copy the compiled blazeds-servlet3-support jar file to the WEB-INF/lib folder of the context.

  * Change the WEB-INF/flex/service-config endpoint as follows.
```
<channel-definition id="amf-stream" class="mx.messaging.channels.StreamingAMFChannel">
  <endpoint url="http://{server.name}:{server.port}/{context.root}/messagebroker/streamingamf" 
     class="org.iobit.servlet3.endpoint.AMFServlet3Endpoint"/>
  <properties>
    <idle-timeout-minutes>3</idle-timeout-minutes>
    <max-streaming-clients>10000</max-streaming-clients>
    <server-to-client-heartbeat-millis>500</server-to-client-heartbeat-millis>
  </properties>
</channel-definition>
```

  * Restart Tomcat and you should be good to go!


An explanation of how this can be used can be found in the following post
[Scaling BlazeDS with Servlet 3](http://blog.hiraash.org/2012/04/13/scaling-blazeds-with-servlet-3-concurrency/)