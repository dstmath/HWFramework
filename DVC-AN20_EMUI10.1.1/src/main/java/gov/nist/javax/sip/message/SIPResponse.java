package gov.nist.javax.sip.message;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.MaxForwards;
import gov.nist.javax.sip.header.ReasonList;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.RequireList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.header.extensions.SessionExpires;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.sip.header.ReasonHeader;
import javax.sip.header.ServerHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public final class SIPResponse extends SIPMessage implements Response, ResponseExt {
    protected StatusLine statusLine;

    public static String getReasonPhrase(int rc) {
        if (rc == 420) {
            return "Bad extension";
        }
        if (rc == 421) {
            return "Etension Required";
        }
        if (rc == 603) {
            return "Decline";
        }
        if (rc == 604) {
            return "Does not exist anywhere";
        }
        switch (rc) {
            case Response.TRYING /*{ENCODED_INT: 100}*/:
                return "Trying";
            case Response.OK /*{ENCODED_INT: 200}*/:
                return "OK";
            case Response.ACCEPTED /*{ENCODED_INT: 202}*/:
                return "Accepted";
            case Response.USE_PROXY /*{ENCODED_INT: 305}*/:
                return "Use proxy";
            case Response.ALTERNATIVE_SERVICE /*{ENCODED_INT: 380}*/:
                return "Alternative service";
            case Response.GONE /*{ENCODED_INT: 410}*/:
                return "Gone";
            case Response.INTERVAL_TOO_BRIEF /*{ENCODED_INT: 423}*/:
                return "Interval too brief";
            case Response.REQUEST_PENDING /*{ENCODED_INT: 491}*/:
                return "Request Pending";
            case Response.UNDECIPHERABLE /*{ENCODED_INT: 493}*/:
                return "Undecipherable";
            case Response.MESSAGE_TOO_LARGE /*{ENCODED_INT: 513}*/:
                return "Message Too Large";
            case Response.BUSY_EVERYWHERE /*{ENCODED_INT: 600}*/:
                return "Busy everywhere";
            case Response.SESSION_NOT_ACCEPTABLE /*{ENCODED_INT: 606}*/:
                return "Session Not acceptable";
            default:
                switch (rc) {
                    case Response.RINGING /*{ENCODED_INT: 180}*/:
                        return "Ringing";
                    case Response.CALL_IS_BEING_FORWARDED /*{ENCODED_INT: 181}*/:
                        return "Call is being forwarded";
                    case Response.QUEUED /*{ENCODED_INT: 182}*/:
                        return "Queued";
                    case Response.SESSION_PROGRESS /*{ENCODED_INT: 183}*/:
                        return "Session progress";
                    default:
                        switch (rc) {
                            case Response.MULTIPLE_CHOICES /*{ENCODED_INT: 300}*/:
                                return "Multiple choices";
                            case Response.MOVED_PERMANENTLY /*{ENCODED_INT: 301}*/:
                                return "Moved permanently";
                            case Response.MOVED_TEMPORARILY /*{ENCODED_INT: 302}*/:
                                return "Moved Temporarily";
                            default:
                                switch (rc) {
                                    case Response.BAD_REQUEST /*{ENCODED_INT: 400}*/:
                                        return "Bad request";
                                    case Response.UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                                        return "Unauthorized";
                                    case Response.PAYMENT_REQUIRED /*{ENCODED_INT: 402}*/:
                                        return "Payment required";
                                    case Response.FORBIDDEN /*{ENCODED_INT: 403}*/:
                                        return "Forbidden";
                                    case Response.NOT_FOUND /*{ENCODED_INT: 404}*/:
                                        return "Not found";
                                    case Response.METHOD_NOT_ALLOWED /*{ENCODED_INT: 405}*/:
                                        return "Method not allowed";
                                    case Response.NOT_ACCEPTABLE /*{ENCODED_INT: 406}*/:
                                        return "Not acceptable";
                                    case Response.PROXY_AUTHENTICATION_REQUIRED /*{ENCODED_INT: 407}*/:
                                        return "Proxy Authentication required";
                                    case Response.REQUEST_TIMEOUT /*{ENCODED_INT: 408}*/:
                                        return "Request timeout";
                                    default:
                                        switch (rc) {
                                            case Response.CONDITIONAL_REQUEST_FAILED /*{ENCODED_INT: 412}*/:
                                                return "Conditional request failed";
                                            case Response.REQUEST_ENTITY_TOO_LARGE /*{ENCODED_INT: 413}*/:
                                                return "Request entity too large";
                                            case Response.REQUEST_URI_TOO_LONG /*{ENCODED_INT: 414}*/:
                                                return "Request-URI too large";
                                            case Response.UNSUPPORTED_MEDIA_TYPE /*{ENCODED_INT: 415}*/:
                                                return "Unsupported media type";
                                            case Response.UNSUPPORTED_URI_SCHEME /*{ENCODED_INT: 416}*/:
                                                return "Unsupported URI Scheme";
                                            default:
                                                switch (rc) {
                                                    case Response.TEMPORARILY_UNAVAILABLE /*{ENCODED_INT: 480}*/:
                                                        return "Temporarily Unavailable";
                                                    case Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST /*{ENCODED_INT: 481}*/:
                                                        return "Call leg/Transaction does not exist";
                                                    case Response.LOOP_DETECTED /*{ENCODED_INT: 482}*/:
                                                        return "Loop detected";
                                                    case Response.TOO_MANY_HOPS /*{ENCODED_INT: 483}*/:
                                                        return "Too many hops";
                                                    case Response.ADDRESS_INCOMPLETE /*{ENCODED_INT: 484}*/:
                                                        return "Address incomplete";
                                                    case Response.AMBIGUOUS /*{ENCODED_INT: 485}*/:
                                                        return "Ambiguous";
                                                    case Response.BUSY_HERE /*{ENCODED_INT: 486}*/:
                                                        return "Busy here";
                                                    case Response.REQUEST_TERMINATED /*{ENCODED_INT: 487}*/:
                                                        return "Request Terminated";
                                                    case Response.NOT_ACCEPTABLE_HERE /*{ENCODED_INT: 488}*/:
                                                        return "Not Acceptable here";
                                                    case Response.BAD_EVENT /*{ENCODED_INT: 489}*/:
                                                        return "Bad Event";
                                                    default:
                                                        switch (rc) {
                                                            case 500:
                                                                return "Server Internal Error";
                                                            case Response.NOT_IMPLEMENTED /*{ENCODED_INT: 501}*/:
                                                                return "Not implemented";
                                                            case Response.BAD_GATEWAY /*{ENCODED_INT: 502}*/:
                                                                return "Bad gateway";
                                                            case Response.SERVICE_UNAVAILABLE /*{ENCODED_INT: 503}*/:
                                                                return "Service unavailable";
                                                            case Response.SERVER_TIMEOUT /*{ENCODED_INT: 504}*/:
                                                                return "Gateway timeout";
                                                            case Response.VERSION_NOT_SUPPORTED /*{ENCODED_INT: 505}*/:
                                                                return "SIP version not supported";
                                                            default:
                                                                return "Unknown Status";
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }

    @Override // javax.sip.message.Response
    public void setStatusCode(int statusCode) throws ParseException {
        if (statusCode < 100 || statusCode > 699) {
            throw new ParseException("bad status code", 0);
        }
        if (this.statusLine == null) {
            this.statusLine = new StatusLine();
        }
        this.statusLine.setStatusCode(statusCode);
    }

    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    @Override // javax.sip.message.Response
    public int getStatusCode() {
        return this.statusLine.getStatusCode();
    }

    @Override // javax.sip.message.Response
    public void setReasonPhrase(String reasonPhrase) {
        if (reasonPhrase != null) {
            if (this.statusLine == null) {
                this.statusLine = new StatusLine();
            }
            this.statusLine.setReasonPhrase(reasonPhrase);
            return;
        }
        throw new IllegalArgumentException("Bad reason phrase");
    }

    @Override // javax.sip.message.Response
    public String getReasonPhrase() {
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 == null || statusLine2.getReasonPhrase() == null) {
            return "";
        }
        return this.statusLine.getReasonPhrase();
    }

    public static boolean isFinalResponse(int rc) {
        return rc >= 200 && rc < 700;
    }

    public boolean isFinalResponse() {
        return isFinalResponse(this.statusLine.getStatusCode());
    }

    public void setStatusLine(StatusLine sl) {
        this.statusLine = sl;
    }

    @Override // gov.nist.javax.sip.message.MessageObject, gov.nist.javax.sip.message.SIPMessage, gov.nist.core.GenericObject
    public String debugDump() {
        String superstring = super.debugDump();
        this.stringRepresentation = "";
        sprint(SIPResponse.class.getCanonicalName());
        sprint("{");
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 != null) {
            sprint(statusLine2.debugDump());
        }
        sprint(superstring);
        sprint("}");
        return this.stringRepresentation;
    }

    public void checkHeaders() throws ParseException {
        if (getCSeq() == null) {
            throw new ParseException("CSeq Is missing ", 0);
        } else if (getTo() == null) {
            throw new ParseException("To Is missing ", 0);
        } else if (getFrom() == null) {
            throw new ParseException("From Is missing ", 0);
        } else if (getViaHeaders() == null) {
            throw new ParseException("Via Is missing ", 0);
        } else if (getCallId() == null) {
            throw new ParseException("Call-ID Is missing ", 0);
        } else if (getStatusCode() > 699) {
            throw new ParseException("Unknown error code!" + getStatusCode(), 0);
        }
    }

    @Override // gov.nist.javax.sip.message.MessageObject, gov.nist.javax.sip.message.SIPMessage, gov.nist.core.GenericObject
    public String encode() {
        if (this.statusLine == null) {
            return super.encode();
        }
        return this.statusLine.encode() + super.encode();
    }

    @Override // gov.nist.javax.sip.message.SIPMessage
    public String encodeMessage() {
        if (this.statusLine == null) {
            return super.encodeSIPHeaders();
        }
        return this.statusLine.encode() + super.encodeSIPHeaders();
    }

    @Override // gov.nist.javax.sip.message.SIPMessage
    public LinkedList getMessageAsEncodedStrings() {
        LinkedList retval = super.getMessageAsEncodedStrings();
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 != null) {
            retval.addFirst(statusLine2.encode());
        }
        return retval;
    }

    @Override // javax.sip.message.Message, java.lang.Object, gov.nist.javax.sip.message.SIPMessage, gov.nist.core.GenericObject
    public Object clone() {
        SIPResponse retval = (SIPResponse) super.clone();
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 != null) {
            retval.statusLine = (StatusLine) statusLine2.clone();
        }
        return retval;
    }

    @Override // javax.sip.message.Message, gov.nist.javax.sip.message.SIPMessage, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (getClass().equals(other.getClass()) && this.statusLine.equals(((SIPResponse) other).statusLine) && super.equals(other)) {
            return true;
        }
        return false;
    }

    @Override // gov.nist.javax.sip.message.SIPMessage, gov.nist.core.GenericObject
    public boolean match(Object matchObj) {
        if (matchObj == null) {
            return true;
        }
        if (!matchObj.getClass().equals(getClass())) {
            return false;
        }
        if (matchObj == this) {
            return true;
        }
        SIPResponse that = (SIPResponse) matchObj;
        StatusLine rline = that.statusLine;
        if (this.statusLine == null && rline != null) {
            return false;
        }
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 == rline) {
            return super.match(matchObj);
        }
        if (!statusLine2.match(that.statusLine) || !super.match(matchObj)) {
            return false;
        }
        return true;
    }

    @Override // gov.nist.javax.sip.message.SIPMessage
    public byte[] encodeAsBytes(String transport) {
        byte[] slbytes = null;
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 != null) {
            try {
                slbytes = statusLine2.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes(transport);
        byte[] retval = new byte[(slbytes.length + superbytes.length)];
        System.arraycopy(slbytes, 0, retval, 0, slbytes.length);
        System.arraycopy(superbytes, 0, retval, slbytes.length, superbytes.length);
        return retval;
    }

    @Override // gov.nist.javax.sip.message.SIPMessage
    public String getDialogId(boolean isServer) {
        From from = (From) getFrom();
        To to = (To) getTo();
        StringBuffer retval = new StringBuffer(((CallID) getCallId()).getCallId());
        if (!isServer) {
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
            if (to.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(to.getTag());
            }
        } else {
            if (to.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(to.getTag());
            }
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
        }
        return retval.toString().toLowerCase();
    }

    public String getDialogId(boolean isServer, String toTag) {
        From from = (From) getFrom();
        StringBuffer retval = new StringBuffer(((CallID) getCallId()).getCallId());
        if (!isServer) {
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
        } else {
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
        }
        return retval.toString().toLowerCase();
    }

    private final void setBranch(Via via, String method) {
        String branch;
        if (method.equals("ACK")) {
            if (this.statusLine.getStatusCode() >= 300) {
                branch = getTopmostVia().getBranch();
            } else {
                branch = Utils.getInstance().generateBranchId();
            }
        } else if (method.equals(Request.CANCEL)) {
            branch = getTopmostVia().getBranch();
        } else {
            return;
        }
        try {
            via.setBranch(branch);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override // gov.nist.javax.sip.message.MessageExt, gov.nist.javax.sip.message.SIPMessage
    public String getFirstLine() {
        StatusLine statusLine2 = this.statusLine;
        if (statusLine2 == null) {
            return null;
        }
        return statusLine2.encode();
    }

    @Override // javax.sip.message.Message, gov.nist.javax.sip.message.SIPMessage
    public void setSIPVersion(String sipVersion) {
        this.statusLine.setSipVersion(sipVersion);
    }

    @Override // javax.sip.message.Message, gov.nist.javax.sip.message.SIPMessage
    public String getSIPVersion() {
        return this.statusLine.getSipVersion();
    }

    @Override // javax.sip.message.Message, gov.nist.javax.sip.message.SIPMessage
    public String toString() {
        if (this.statusLine == null) {
            return "";
        }
        return this.statusLine.encode() + super.encode();
    }

    public SIPRequest createRequest(SipUri requestURI, Via via, CSeq cseq, From from, To to) {
        SIPRequest newRequest = new SIPRequest();
        String method = cseq.getMethod();
        newRequest.setMethod(method);
        newRequest.setRequestURI(requestURI);
        setBranch(via, method);
        newRequest.setHeader(via);
        newRequest.setHeader(cseq);
        Iterator headerIterator = getHeaders();
        while (headerIterator.hasNext()) {
            SIPHeader nextHeader = headerIterator.next();
            if (!SIPMessage.isResponseHeader(nextHeader) && !(nextHeader instanceof ViaList) && !(nextHeader instanceof CSeq) && !(nextHeader instanceof ContentType) && !(nextHeader instanceof ContentLength) && !(nextHeader instanceof RecordRouteList) && !(nextHeader instanceof RequireList) && !(nextHeader instanceof ContactList) && !(nextHeader instanceof ContentLength) && !(nextHeader instanceof ServerHeader) && !(nextHeader instanceof ReasonHeader) && !(nextHeader instanceof SessionExpires) && !(nextHeader instanceof ReasonList)) {
                if (nextHeader instanceof To) {
                    nextHeader = to;
                } else if (nextHeader instanceof From) {
                    nextHeader = from;
                }
                try {
                    newRequest.attachHeader(nextHeader, false);
                } catch (SIPDuplicateHeaderException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            newRequest.attachHeader(new MaxForwards(70), false);
        } catch (Exception e2) {
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
        }
        return newRequest;
    }
}
