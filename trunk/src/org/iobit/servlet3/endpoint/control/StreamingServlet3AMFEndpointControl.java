package org.iobit.servlet3.endpoint.control;

import org.iobit.servlet3.endpoint.AMFServlet3Endpoint;

import flex.management.BaseControl;
import flex.management.runtime.messaging.endpoints.StreamingAMFEndpointControlMBean;
import flex.management.runtime.messaging.endpoints.StreamingEndpointControl;

/**
 * The <code>StreamingAMFEndpointControl</code> class is the MBean implemenation
 * for monitoring and managing an <code>StreamingAMFEndpoint</code> at runtime.
 */
public class StreamingServlet3AMFEndpointControl extends StreamingEndpointControl implements
        StreamingAMFEndpointControlMBean {
    private static final String TYPE = "AMFServlet3Endpoint";

    /**
     * Constructs a <code>StreamingAMFEndpointControl</code>, assigning managed message
     * endpoint and parent MBean.
     *
     * @param endpoint The <code>StreamingAMFEndpoint</code> managed by this MBean.
     * @param parent The parent MBean in the management hierarchy.
     */
    public StreamingServlet3AMFEndpointControl(AMFServlet3Endpoint endpoint, BaseControl parent) {
        super(endpoint, parent);
    }

    /** {@inheritDoc} */
    public String getType() {
        return TYPE;
    }
}