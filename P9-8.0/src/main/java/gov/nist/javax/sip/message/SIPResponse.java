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
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.sip.header.Header;
import javax.sip.header.ReasonHeader;
import javax.sip.header.ServerHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public final class SIPResponse extends SIPMessage implements Response, ResponseExt {
    protected StatusLine statusLine;

    public static String getReasonPhrase(int rc) {
        switch (rc) {
            case Response.TRYING /*100*/:
                return "Trying";
            case Response.RINGING /*180*/:
                return "Ringing";
            case Response.CALL_IS_BEING_FORWARDED /*181*/:
                return "Call is being forwarded";
            case Response.QUEUED /*182*/:
                return "Queued";
            case Response.SESSION_PROGRESS /*183*/:
                return "Session progress";
            case Response.OK /*200*/:
                return "OK";
            case Response.ACCEPTED /*202*/:
                return "Accepted";
            case Response.MULTIPLE_CHOICES /*300*/:
                return "Multiple choices";
            case Response.MOVED_PERMANENTLY /*301*/:
                return "Moved permanently";
            case Response.MOVED_TEMPORARILY /*302*/:
                return "Moved Temporarily";
            case Response.USE_PROXY /*305*/:
                return "Use proxy";
            case Response.ALTERNATIVE_SERVICE /*380*/:
                return "Alternative service";
            case Response.BAD_REQUEST /*400*/:
                return "Bad request";
            case Response.UNAUTHORIZED /*401*/:
                return "Unauthorized";
            case Response.PAYMENT_REQUIRED /*402*/:
                return "Payment required";
            case Response.FORBIDDEN /*403*/:
                return "Forbidden";
            case Response.NOT_FOUND /*404*/:
                return "Not found";
            case Response.METHOD_NOT_ALLOWED /*405*/:
                return "Method not allowed";
            case Response.NOT_ACCEPTABLE /*406*/:
                return "Not acceptable";
            case Response.PROXY_AUTHENTICATION_REQUIRED /*407*/:
                return "Proxy Authentication required";
            case Response.REQUEST_TIMEOUT /*408*/:
                return "Request timeout";
            case Response.GONE /*410*/:
                return "Gone";
            case Response.CONDITIONAL_REQUEST_FAILED /*412*/:
                return "Conditional request failed";
            case Response.REQUEST_ENTITY_TOO_LARGE /*413*/:
                return "Request entity too large";
            case Response.REQUEST_URI_TOO_LONG /*414*/:
                return "Request-URI too large";
            case Response.UNSUPPORTED_MEDIA_TYPE /*415*/:
                return "Unsupported media type";
            case Response.UNSUPPORTED_URI_SCHEME /*416*/:
                return "Unsupported URI Scheme";
            case Response.BAD_EXTENSION /*420*/:
                return "Bad extension";
            case Response.EXTENSION_REQUIRED /*421*/:
                return "Etension Required";
            case Response.INTERVAL_TOO_BRIEF /*423*/:
                return "Interval too brief";
            case Response.TEMPORARILY_UNAVAILABLE /*480*/:
                return "Temporarily Unavailable";
            case Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST /*481*/:
                return "Call leg/Transaction does not exist";
            case Response.LOOP_DETECTED /*482*/:
                return "Loop detected";
            case Response.TOO_MANY_HOPS /*483*/:
                return "Too many hops";
            case Response.ADDRESS_INCOMPLETE /*484*/:
                return "Address incomplete";
            case Response.AMBIGUOUS /*485*/:
                return "Ambiguous";
            case Response.BUSY_HERE /*486*/:
                return "Busy here";
            case Response.REQUEST_TERMINATED /*487*/:
                return "Request Terminated";
            case Response.NOT_ACCEPTABLE_HERE /*488*/:
                return "Not Acceptable here";
            case Response.BAD_EVENT /*489*/:
                return "Bad Event";
            case Response.REQUEST_PENDING /*491*/:
                return "Request Pending";
            case Response.UNDECIPHERABLE /*493*/:
                return "Undecipherable";
            case 500:
                return "Server Internal Error";
            case Response.NOT_IMPLEMENTED /*501*/:
                return "Not implemented";
            case Response.BAD_GATEWAY /*502*/:
                return "Bad gateway";
            case Response.SERVICE_UNAVAILABLE /*503*/:
                return "Service unavailable";
            case Response.SERVER_TIMEOUT /*504*/:
                return "Gateway timeout";
            case Response.VERSION_NOT_SUPPORTED /*505*/:
                return "SIP version not supported";
            case Response.MESSAGE_TOO_LARGE /*513*/:
                return "Message Too Large";
            case Response.BUSY_EVERYWHERE /*600*/:
                return "Busy everywhere";
            case Response.DECLINE /*603*/:
                return "Decline";
            case Response.DOES_NOT_EXIST_ANYWHERE /*604*/:
                return "Does not exist anywhere";
            case Response.SESSION_NOT_ACCEPTABLE /*606*/:
                return "Session Not acceptable";
            default:
                return "Unknown Status";
        }
    }

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

    public int getStatusCode() {
        return this.statusLine.getStatusCode();
    }

    public void setReasonPhrase(String reasonPhrase) {
        if (reasonPhrase == null) {
            throw new IllegalArgumentException("Bad reason phrase");
        }
        if (this.statusLine == null) {
            this.statusLine = new StatusLine();
        }
        this.statusLine.setReasonPhrase(reasonPhrase);
    }

    public String getReasonPhrase() {
        if (this.statusLine == null || this.statusLine.getReasonPhrase() == null) {
            return "";
        }
        return this.statusLine.getReasonPhrase();
    }

    public static boolean isFinalResponse(int rc) {
        return rc >= Response.OK && rc < 700;
    }

    public boolean isFinalResponse() {
        return isFinalResponse(this.statusLine.getStatusCode());
    }

    public void setStatusLine(StatusLine sl) {
        this.statusLine = sl;
    }

    public String debugDump() {
        String superstring = super.debugDump();
        this.stringRepresentation = "";
        sprint(SIPResponse.class.getCanonicalName());
        sprint("{");
        if (this.statusLine != null) {
            sprint(this.statusLine.debugDump());
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

    public String encode() {
        if (this.statusLine != null) {
            return this.statusLine.encode() + super.encode();
        }
        return super.encode();
    }

    public String encodeMessage() {
        if (this.statusLine != null) {
            return this.statusLine.encode() + super.encodeSIPHeaders();
        }
        return super.encodeSIPHeaders();
    }

    public LinkedList getMessageAsEncodedStrings() {
        LinkedList retval = super.getMessageAsEncodedStrings();
        if (this.statusLine != null) {
            retval.addFirst(this.statusLine.encode());
        }
        return retval;
    }

    public Object clone() {
        SIPResponse retval = (SIPResponse) super.clone();
        if (this.statusLine != null) {
            retval.statusLine = (StatusLine) this.statusLine.clone();
        }
        return retval;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.statusLine.equals(((SIPResponse) other).statusLine)) {
            z = super.equals(other);
        }
        return z;
    }

    public boolean match(Object matchObj) {
        boolean z = false;
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
        if (this.statusLine == rline) {
            return super.match(matchObj);
        }
        if (this.statusLine.match(that.statusLine)) {
            z = super.match(matchObj);
        }
        return z;
    }

    public byte[] encodeAsBytes(String transport) {
        byte[] slbytes = null;
        if (this.statusLine != null) {
            try {
                slbytes = this.statusLine.encode().getBytes("UTF-8");
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes(transport);
        byte[] retval = new byte[(slbytes.length + superbytes.length)];
        System.arraycopy(slbytes, 0, retval, 0, slbytes.length);
        System.arraycopy(superbytes, 0, retval, slbytes.length, superbytes.length);
        return retval;
    }

    public String getDialogId(boolean isServer) {
        From from = (From) getFrom();
        To to = (To) getTo();
        StringBuffer retval = new StringBuffer(((CallID) getCallId()).getCallId());
        if (isServer) {
            if (to.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(to.getTag());
            }
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
        } else {
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
            if (to.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(to.getTag());
            }
        }
        return retval.toString().toLowerCase();
    }

    public String getDialogId(boolean isServer, String toTag) {
        From from = (From) getFrom();
        StringBuffer retval = new StringBuffer(((CallID) getCallId()).getCallId());
        if (isServer) {
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
        } else {
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
        }
        return retval.toString().toLowerCase();
    }

    private final void setBranch(Via via, String method) {
        String branch;
        if (method.equals("ACK")) {
            if (this.statusLine.getStatusCode() >= Response.MULTIPLE_CHOICES) {
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

    public String getFirstLine() {
        if (this.statusLine == null) {
            return null;
        }
        return this.statusLine.encode();
    }

    public void setSIPVersion(String sipVersion) {
        this.statusLine.setSipVersion(sipVersion);
    }

    public String getSIPVersion() {
        return this.statusLine.getSipVersion();
    }

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
        newRequest.setHeader((Header) via);
        newRequest.setHeader((Header) cseq);
        Iterator headerIterator = getHeaders();
        while (headerIterator.hasNext()) {
            SIPHeader nextHeader = (SIPHeader) headerIterator.next();
            if (!(SIPMessage.isResponseHeader(nextHeader) || (nextHeader instanceof ViaList) || (nextHeader instanceof CSeq) || (nextHeader instanceof ContentType) || (nextHeader instanceof ContentLength) || (nextHeader instanceof RecordRouteList) || (nextHeader instanceof RequireList) || (nextHeader instanceof ContactList) || (nextHeader instanceof ContentLength) || (nextHeader instanceof ServerHeader) || (nextHeader instanceof ReasonHeader) || (nextHeader instanceof SessionExpires) || (nextHeader instanceof ReasonList))) {
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
