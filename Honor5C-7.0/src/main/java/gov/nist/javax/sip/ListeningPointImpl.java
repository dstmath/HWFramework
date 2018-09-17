package gov.nist.javax.sip;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.MessageProcessor;
import java.io.IOException;
import java.text.ParseException;
import javax.sip.ListeningPoint;
import javax.sip.SipStack;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;

public class ListeningPointImpl implements ListeningPoint, ListeningPointExt {
    protected MessageProcessor messageProcessor;
    int port;
    protected SipProviderImpl sipProvider;
    protected SipStackImpl sipStack;
    protected String transport;

    public static String makeKey(String host, int port, String transport) {
        return new StringBuffer(host).append(Separators.COLON).append(port).append(Separators.SLASH).append(transport).toString().toLowerCase();
    }

    protected String getKey() {
        return makeKey(getIPAddress(), this.port, this.transport);
    }

    protected void setSipProvider(SipProviderImpl sipProviderImpl) {
        this.sipProvider = sipProviderImpl;
    }

    protected void removeSipProvider() {
        this.sipProvider = null;
    }

    protected ListeningPointImpl(SipStack sipStack, int port, String transport) {
        this.sipStack = (SipStackImpl) sipStack;
        this.port = port;
        this.transport = transport;
    }

    public Object clone() {
        ListeningPointImpl lip = new ListeningPointImpl(this.sipStack, this.port, null);
        lip.sipStack = this.sipStack;
        return lip;
    }

    public int getPort() {
        return this.messageProcessor.getPort();
    }

    public String getTransport() {
        return this.messageProcessor.getTransport();
    }

    public SipProviderImpl getProvider() {
        return this.sipProvider;
    }

    public String getIPAddress() {
        return this.messageProcessor.getIpAddress().getHostAddress();
    }

    public void setSentBy(String sentBy) throws ParseException {
        this.messageProcessor.setSentBy(sentBy);
    }

    public String getSentBy() {
        return this.messageProcessor.getSentBy();
    }

    public boolean isSentBySet() {
        return this.messageProcessor.isSentBySet();
    }

    public Via getViaHeader() {
        return this.messageProcessor.getViaHeader();
    }

    public MessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }

    public ContactHeader createContactHeader() {
        try {
            String ipAddress = getIPAddress();
            int port = getPort();
            SipURI sipURI = new SipUri();
            sipURI.setHost(ipAddress);
            sipURI.setPort(port);
            sipURI.setTransportParam(this.transport);
            Contact contact = new Contact();
            AddressImpl address = new AddressImpl();
            address.setURI(sipURI);
            contact.setAddress(address);
            return contact;
        } catch (Exception e) {
            InternalErrorHandler.handleException("Unexpected exception", this.sipStack.getStackLogger());
            return null;
        }
    }

    public void sendHeartbeat(String ipAddress, int port) throws IOException {
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(new Host(ipAddress));
        targetHostPort.setPort(port);
        MessageChannel messageChannel = this.messageProcessor.createMessageChannel(targetHostPort);
        SIPRequest siprequest = new SIPRequest();
        siprequest.setNullRequest();
        messageChannel.sendMessage(siprequest);
    }

    public ViaHeader createViaHeader() {
        return getViaHeader();
    }
}
