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

    public static String makeKey(String host, int port2, String transport2) {
        StringBuffer stringBuffer = new StringBuffer(host);
        stringBuffer.append(Separators.COLON);
        stringBuffer.append(port2);
        stringBuffer.append(Separators.SLASH);
        stringBuffer.append(transport2);
        return stringBuffer.toString().toLowerCase();
    }

    /* access modifiers changed from: protected */
    public String getKey() {
        return makeKey(getIPAddress(), this.port, this.transport);
    }

    /* access modifiers changed from: protected */
    public void setSipProvider(SipProviderImpl sipProviderImpl) {
        this.sipProvider = sipProviderImpl;
    }

    /* access modifiers changed from: protected */
    public void removeSipProvider() {
        this.sipProvider = null;
    }

    protected ListeningPointImpl(SipStack sipStack2, int port2, String transport2) {
        this.sipStack = (SipStackImpl) sipStack2;
        this.port = port2;
        this.transport = transport2;
    }

    @Override // java.lang.Object
    public Object clone() {
        ListeningPointImpl lip = new ListeningPointImpl(this.sipStack, this.port, null);
        lip.sipStack = this.sipStack;
        return lip;
    }

    @Override // javax.sip.ListeningPoint
    public int getPort() {
        return this.messageProcessor.getPort();
    }

    @Override // javax.sip.ListeningPoint
    public String getTransport() {
        return this.messageProcessor.getTransport();
    }

    public SipProviderImpl getProvider() {
        return this.sipProvider;
    }

    @Override // javax.sip.ListeningPoint
    public String getIPAddress() {
        return this.messageProcessor.getIpAddress().getHostAddress();
    }

    @Override // javax.sip.ListeningPoint
    public void setSentBy(String sentBy) throws ParseException {
        this.messageProcessor.setSentBy(sentBy);
    }

    @Override // javax.sip.ListeningPoint
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

    @Override // javax.sip.ListeningPoint
    public ContactHeader createContactHeader() {
        try {
            String ipAddress = getIPAddress();
            int port2 = getPort();
            SipURI sipURI = new SipUri();
            sipURI.setHost(ipAddress);
            sipURI.setPort(port2);
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

    @Override // javax.sip.ListeningPoint
    public void sendHeartbeat(String ipAddress, int port2) throws IOException {
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(new Host(ipAddress));
        targetHostPort.setPort(port2);
        MessageChannel messageChannel = this.messageProcessor.createMessageChannel(targetHostPort);
        SIPRequest siprequest = new SIPRequest();
        siprequest.setNullRequest();
        messageChannel.sendMessage(siprequest);
    }

    @Override // gov.nist.javax.sip.ListeningPointExt
    public ViaHeader createViaHeader() {
        return getViaHeader();
    }
}
