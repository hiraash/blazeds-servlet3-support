#Example of a Channel Definition to use this endpoint

```
<channel-definition id="amf-stream" class="mx.messaging.channels.StreamingAMFChannel">
   <endpoint url="http://{server.name}:{server.port}/{context.root}/messagebroker/streamingamf" 
     class="org.iobit.servlet3.endpoint.AMFServlet3Endpoint"/>
    <properties>
     <idle-timeout-minutes>7</idle-timeout-minutes>
     <max-streaming-clients>500</max-streaming-clients>        
     <server-to-client-heartbeat-millis>500</server-to-client-heartbeat-millis>
     <enable-debug-mode>true</enable-debug-mode>
    </properties>
 </channel-definition>
```

**idle-timeout-minutes** - Use this to set the time-outs for the streaming connections

**max-streaming-clients** - Use this to set the total number of streaming clients you want to limit the endpoint to.

**server-to-client-heartbeat-millis** - Use this to adjust the latency vs server process consumption. Reducing this value may make your server go hot. Increasing the value will increase the latency on the messages

**enable-debug-mode** - Keep this false unless you want to see the code traces on your server log. This can make your log files bloat up pretty fast