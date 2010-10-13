package org.iobit.servlet3.endpoint;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.iobit.servlet3.endpoint.amf.SerializationFilter;
import org.iobit.servlet3.endpoint.control.StreamingServlet3AMFEndpointControl;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.endpoints.amf.BatchProcessFilter;
import flex.messaging.endpoints.amf.LegacyFilter;
import flex.messaging.endpoints.amf.MessageBrokerFilter;
import flex.messaging.endpoints.amf.SessionFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;

public class AMFServlet3Endpoint extends BaseServlet3Endpoint {
    /**
     * The log category for this endpoint.
     */
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_NIO_AMF;

    /**
     * Constructs an unmanaged <code>StreamingAMFEndpoint</code>.
     */
    public AMFServlet3Endpoint() {
        this(false);
    }

    /**
     * Constructs a <code>StreamingAMFEndpoint</code> with the indicated management.
     *
     * @param enableManagement <code>true</code> if the <code>StreamingAMFEndpoint</code>
     * is manageable; otherwise <code>false</code>.
     */
    public AMFServlet3Endpoint(boolean enableManagement) {
        super(enableManagement);
    }

    /**
     * Create the gateway filters that transform action requests
     * and responses.
     */
    protected AMFFilter createFilterChain() {
        AMFFilter serializationFilter = new SerializationFilter(getLogCategory());
        AMFFilter batchFilter = new BatchProcessFilter();
        AMFFilter sessionFilter = new SessionFilter();
        AMFFilter envelopeFilter = new LegacyFilter(this);
        AMFFilter messageBrokerFilter = new MessageBrokerFilter(this);

        serializationFilter.setNext(batchFilter);
        batchFilter.setNext(sessionFilter);
        sessionFilter.setNext(envelopeFilter);
        envelopeFilter.setNext(messageBrokerFilter);

        return serializationFilter;
    }

    /**
     * Returns MessageIOConstants.AMF_CONTENT_TYPE.
     */
    protected String getResponseContentType() {
        return MessageIOConstants.AMF_CONTENT_TYPE;
    }

    /**
     * Returns the log category of the endpoint.
     *
     * @return The log category of the endpoint.
     */
    protected String getLogCategory() {
        return LOG_CATEGORY;
    }

    /**
     * Used internally for performance information gathering; not intended for
     * public use. Serializes the message in AMF format and returns the size of
     * the serialized message.
     *
     * @param message Message to get the size for.
     *
     * @return The size of the message after message is serialized.
     */
    protected long getMessageSizeForPerformanceInfo(Message message) {
        Amf3Output amfOut = new Amf3Output(serializationContext);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(outStream);
        amfOut.setOutputStream(dataOutStream);
        try {
            amfOut.writeObject(message);
        } catch (IOException e) {
            if (Log.isDebug())
                log.debug("MPI exception while retrieving the size of the serialized message: " + e.toString());
        }
        return dataOutStream.size();
    }

    /**
     * Returns the deserializer class name used by the endpoint.
     *
     * @return The deserializer class name used by the endpoint.
     */
    protected String getDeserializerClassName() {
        return " flex.messaging.io.amf.AmfMessageDeserializer";
    }

    /**
     * Returns the serializer class name used by the endpoint.
     *
     * @return The serializer class name used by the endpoint.
     */
    protected String getSerializerClassName() {
        return "flex.messaging.io.amf.AmfMessageSerializer";
    }

    /**
     * Invoked automatically to allow the <code>StreamingAMFEndpoint</code> to setup its
     * corresponding MBean control.
     *
     * @param broker The <code>MessageBroker</code> that manages this
     * <code>StreamingAMFEndpoint</code>.
     */
    protected void setupEndpointControl(MessageBroker broker) {
        controller = new StreamingServlet3AMFEndpointControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }

    /**
     * Helper method invoked by the endpoint request handler thread cycling in wait-notify.
     * Serializes messages and streams each to the client as a response chunk using streamChunk().
     *
     * @param messages The messages to serialize and push to the client.
     * @param os The output stream the chunk will be written to.
     * @param response The HttpServletResponse, used to flush the chunk to the client.
     */
    protected void streamMessages(List messages, ServletOutputStream os, HttpServletResponse response)
        throws IOException {
        if (messages == null || messages.isEmpty())
            return;

        // Serialize each message as a separate chunk of bytes.
        TypeMarshallingContext.setTypeMarshaller(getTypeMarshaller());
        for (Iterator iter = messages.iterator(); iter.hasNext();) {
            Amf3Output amfOut = new Amf3Output(serializationContext);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(outStream);
            amfOut.setOutputStream(dataOutStream);

            Message message = (Message)iter.next();

            // Add performance information if MPI is enabled.
            if (isRecordMessageSizes() || isRecordMessageTimes())
                addPerformanceInfo(message);

            if (Log.isDebug())
                log.debug("Endpoint with id '" + getId() + "' is streaming message: " + message);

            amfOut.writeObject(message);
            dataOutStream.flush();
            byte[] messageBytes = outStream.toByteArray();
            streamChunk(messageBytes, os, response);

            // Update the push count for the StreamingEndpoint mbean.
            if (isManaged()){
                ((StreamingServlet3AMFEndpointControl)controller).incrementPushCount();
            }
        }
        TypeMarshallingContext.setTypeMarshaller(null);
    }
}
