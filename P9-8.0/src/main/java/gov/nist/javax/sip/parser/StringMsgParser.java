package gov.nist.javax.sip.parser;

import gov.nist.core.Host;
import gov.nist.core.HostNameParser;
import gov.nist.core.Separators;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.address.TelephoneNumber;
import gov.nist.javax.sip.header.ExtensionHeaderImpl;
import gov.nist.javax.sip.header.NameMap;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class StringMsgParser {
    private static boolean computeContentLengthFromMessage = false;
    private ParseExceptionListener parseExceptionListener;
    private String rawStringMessage;
    protected boolean readBody;
    private boolean strict;

    /* renamed from: gov.nist.javax.sip.parser.StringMsgParser$1ParserThread */
    class AnonymousClass1ParserThread implements Runnable {
        String[] messages;

        public AnonymousClass1ParserThread(String[] messagesToParse) {
            this.messages = messagesToParse;
        }

        public void run() {
            for (int i = 0; i < this.messages.length; i++) {
                try {
                    System.out.println(" i = " + i + " branchId = " + new StringMsgParser().parseSIPMessage(this.messages[i]).getTopmostVia().getBranch());
                } catch (ParseException e) {
                }
            }
        }
    }

    public StringMsgParser() {
        this.readBody = true;
    }

    public StringMsgParser(ParseExceptionListener exhandler) {
        this();
        this.parseExceptionListener = exhandler;
    }

    public void setParseExceptionListener(ParseExceptionListener pexhandler) {
        this.parseExceptionListener = pexhandler;
    }

    public SIPMessage parseSIPMessage(byte[] msgBuffer) throws ParseException {
        if (msgBuffer == null || msgBuffer.length == 0) {
            return null;
        }
        int i = 0;
        while (msgBuffer[i] < (byte) 32) {
            try {
                i++;
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
        String currentHeader = null;
        boolean isFirstLine = true;
        SIPMessage message = null;
        String currentLine;
        do {
            int lineStart = i;
            while (msgBuffer[i] != (byte) 13 && msgBuffer[i] != (byte) 10) {
                try {
                    i++;
                } catch (ArrayIndexOutOfBoundsException e2) {
                }
            }
            try {
                currentLine = trimEndOfLine(new String(msgBuffer, lineStart, i - lineStart, "UTF-8"));
                if (currentLine.length() == 0) {
                    if (!(currentHeader == null || message == null)) {
                        processHeader(currentHeader, message);
                    }
                } else if (isFirstLine) {
                    message = processFirstLine(currentLine);
                } else {
                    char firstChar = currentLine.charAt(0);
                    if (firstChar != 9 && firstChar != ' ') {
                        if (!(currentHeader == null || message == null)) {
                            processHeader(currentHeader, message);
                        }
                        currentHeader = currentLine;
                    } else if (currentHeader == null) {
                        throw new ParseException("Bad header continuation.", 0);
                    } else {
                        currentHeader = currentHeader + currentLine.substring(1);
                    }
                }
                if (msgBuffer[i] == (byte) 13 && msgBuffer.length > i + 1 && msgBuffer[i + 1] == (byte) 10) {
                    i++;
                }
                i++;
                isFirstLine = false;
            } catch (UnsupportedEncodingException e3) {
                throw new ParseException("Bad message encoding!", 0);
            }
        } while (currentLine.length() > 0);
        if (message == null) {
            throw new ParseException("Bad message", 0);
        }
        message.setSize(i);
        if (!(!this.readBody || message.getContentLength() == null || message.getContentLength().getContentLength() == 0)) {
            int bodyLength = msgBuffer.length - i;
            byte[] body = new byte[bodyLength];
            System.arraycopy(msgBuffer, i, body, 0, bodyLength);
            message.setMessageContent(body, computeContentLengthFromMessage, message.getContentLength().getContentLength());
        }
        return message;
    }

    public SIPMessage parseSIPMessage(String msgString) throws ParseException {
        if (msgString == null || msgString.length() == 0) {
            return null;
        }
        this.rawStringMessage = msgString;
        int i = 0;
        while (msgString.charAt(i) < ' ') {
            try {
                i++;
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            } catch (StringIndexOutOfBoundsException e2) {
                return null;
            }
        }
        String currentHeader = null;
        boolean isFirstLine = true;
        SIPMessage message = null;
        String currentLine;
        do {
            int lineStart = i;
            try {
                char c = msgString.charAt(i);
                while (c != 13 && c != 10) {
                    i++;
                    c = msgString.charAt(i);
                }
                currentLine = trimEndOfLine(msgString.substring(lineStart, i));
                if (currentLine.length() == 0) {
                    if (currentHeader != null) {
                        processHeader(currentHeader, message);
                    }
                } else if (isFirstLine) {
                    message = processFirstLine(currentLine);
                } else {
                    char firstChar = currentLine.charAt(0);
                    if (firstChar != 9 && firstChar != ' ') {
                        if (currentHeader != null) {
                            processHeader(currentHeader, message);
                        }
                        currentHeader = currentLine;
                    } else if (currentHeader == null) {
                        throw new ParseException("Bad header continuation.", 0);
                    } else {
                        currentHeader = currentHeader + currentLine.substring(1);
                    }
                }
                if (msgString.charAt(i) == 13 && msgString.length() > i + 1 && msgString.charAt(i + 1) == 10) {
                    i++;
                }
                i++;
                isFirstLine = false;
            } catch (ArrayIndexOutOfBoundsException e3) {
            } catch (StringIndexOutOfBoundsException e4) {
            }
        } while (currentLine.length() > 0);
        message.setSize(i);
        if (this.readBody && message.getContentLength() != null) {
            if (message.getContentLength().getContentLength() != 0) {
                message.setMessageContent(msgString.substring(i), this.strict, computeContentLengthFromMessage, message.getContentLength().getContentLength());
            } else if (!computeContentLengthFromMessage && message.getContentLength().getContentLength() == 0 && (msgString.endsWith("\r\n\r\n") ^ 1) != 0 && this.strict) {
                throw new ParseException("Extraneous characters at the end of the message ", i);
            }
        }
        return message;
    }

    private String trimEndOfLine(String line) {
        if (line == null) {
            return line;
        }
        int i = line.length() - 1;
        while (i >= 0 && line.charAt(i) <= ' ') {
            i--;
        }
        if (i == line.length() - 1) {
            return line;
        }
        if (i == -1) {
            return "";
        }
        return line.substring(0, i + 1);
    }

    private SIPMessage processFirstLine(String firstLine) throws ParseException {
        SIPMessage message;
        if (firstLine.startsWith(SIPConstants.SIP_VERSION_STRING)) {
            message = new SIPResponse();
            try {
                ((SIPResponse) message).setStatusLine(new StatusLineParser(firstLine + Separators.RETURN).parse());
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null) {
                    this.parseExceptionListener.handleException(ex, message, StatusLine.class, firstLine, this.rawStringMessage);
                } else {
                    throw ex;
                }
            }
        }
        message = new SIPRequest();
        try {
            ((SIPRequest) message).setRequestLine(new RequestLineParser(firstLine + Separators.RETURN).parse());
        } catch (ParseException ex2) {
            if (this.parseExceptionListener != null) {
                this.parseExceptionListener.handleException(ex2, message, RequestLine.class, firstLine, this.rawStringMessage);
            } else {
                throw ex2;
            }
        }
        return message;
    }

    private void processHeader(String header, SIPMessage message) throws ParseException {
        if (header != null && header.length() != 0) {
            try {
                try {
                    message.attachHeader(ParserFactory.createParser(header + Separators.RETURN).parse(), false);
                } catch (ParseException ex) {
                    if (this.parseExceptionListener != null) {
                        Class headerClass = NameMap.getClassFromName(Lexer.getHeaderName(header));
                        if (headerClass == null) {
                            headerClass = ExtensionHeaderImpl.class;
                        }
                        this.parseExceptionListener.handleException(ex, message, headerClass, header, this.rawStringMessage);
                    }
                }
            } catch (ParseException ex2) {
                this.parseExceptionListener.handleException(ex2, message, null, header, this.rawStringMessage);
            }
        }
    }

    public AddressImpl parseAddress(String address) throws ParseException {
        return new AddressParser(address).address(true);
    }

    public Host parseHost(String host) throws ParseException {
        return new HostNameParser(new Lexer("charLexer", host)).host();
    }

    public TelephoneNumber parseTelephoneNumber(String telephone_number) throws ParseException {
        return new URLParser(telephone_number).parseTelephoneNumber(true);
    }

    public SipUri parseSIPUrl(String url) throws ParseException {
        try {
            return new URLParser(url).sipURL(true);
        } catch (ClassCastException e) {
            throw new ParseException(url + " Not a SIP URL ", 0);
        }
    }

    public GenericURI parseUrl(String url) throws ParseException {
        return new URLParser(url).parse();
    }

    public SIPHeader parseSIPHeader(String header) throws ParseException {
        int start = 0;
        int end = header.length() - 1;
        while (header.charAt(start) <= ' ') {
            try {
                start++;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ParseException("Empty header.", 0);
            }
        }
        while (header.charAt(end) <= ' ') {
            end--;
        }
        StringBuffer buffer = new StringBuffer(end + 1);
        int i = start;
        int lineStart = start;
        boolean endOfLine = false;
        while (i <= end) {
            char c = header.charAt(i);
            if (c == 13 || c == 10) {
                if (!endOfLine) {
                    buffer.append(header.substring(lineStart, i));
                    endOfLine = true;
                }
            } else if (endOfLine) {
                endOfLine = false;
                if (c == ' ' || c == 9) {
                    buffer.append(' ');
                    lineStart = i + 1;
                } else {
                    lineStart = i;
                }
            }
            i++;
        }
        buffer.append(header.substring(lineStart, i));
        buffer.append(10);
        HeaderParser hp = ParserFactory.createParser(buffer.toString());
        if (hp != null) {
            return hp.parse();
        }
        throw new ParseException("could not create parser", 0);
    }

    public RequestLine parseSIPRequestLine(String requestLine) throws ParseException {
        return new RequestLineParser(requestLine + Separators.RETURN).parse();
    }

    public StatusLine parseSIPStatusLine(String statusLine) throws ParseException {
        return new StatusLineParser(statusLine + Separators.RETURN).parse();
    }

    public static void setComputeContentLengthFromMessage(boolean computeContentLengthFromMessage) {
        computeContentLengthFromMessage = computeContentLengthFromMessage;
    }

    public static void main(String[] args) throws ParseException {
        String[] messages = new String[]{"SIP/2.0 200 OK\r\nTo: \"The Little Blister\" <sip:LittleGuy@there.com>;tag=469bc066\r\nFrom: \"The Master Blaster\" <sip:BigGuy@here.com>;tag=11\r\nVia: SIP/2.0/UDP 139.10.134.246:5060;branch=z9hG4bK8b0a86f6_1030c7d18e0_17;received=139.10.134.246\r\nCall-ID: 1030c7d18ae_a97b0b_b@8b0a86f6\r\nCSeq: 1 SUBSCRIBE\r\nContact: <sip:172.16.11.162:5070>\r\nContent-Length: 0\r\n\r\n", "SIP/2.0 180 Ringing\r\nVia: SIP/2.0/UDP 172.18.1.29:5060;branch=z9hG4bK43fc10fb4446d55fc5c8f969607991f4\r\nTo: \"0440\" <sip:0440@212.209.220.131>;tag=2600\r\nFrom: \"Andreas\" <sip:andreas@e-horizon.se>;tag=8524\r\nCall-ID: f51a1851c5f570606140f14c8eb64fd3@172.18.1.29\r\nCSeq: 1 INVITE\r\nMax-Forwards: 70\r\nRecord-Route: <sip:212.209.220.131:5060>\r\nContent-Length: 0\r\n\r\n", "REGISTER sip:nist.gov SIP/2.0\r\nVia: SIP/2.0/UDP 129.6.55.182:14826\r\nMax-Forwards: 70\r\nFrom: <sip:mranga@nist.gov>;tag=6fcd5c7ace8b4a45acf0f0cd539b168b;epid=0d4c418ddf\r\nTo: <sip:mranga@nist.gov>\r\nCall-ID: c5679907eb954a8da9f9dceb282d7230@129.6.55.182\r\nCSeq: 1 REGISTER\r\nContact: <sip:129.6.55.182:14826>;methods=\"INVITE, MESSAGE, INFO, SUBSCRIBE, OPTIONS, BYE, CANCEL, NOTIFY, ACK, REFER\"\r\nUser-Agent: RTC/(Microsoft RTC)\r\nEvent:  registration\r\nAllow-Events: presence\r\nContent-Length: 0\r\n\r\nINVITE sip:littleguy@there.com:5060 SIP/2.0\r\nVia: SIP/2.0/UDP 65.243.118.100:5050\r\nFrom: M. Ranganathan  <sip:M.Ranganathan@sipbakeoff.com>;tag=1234\r\nTo: \"littleguy@there.com\" <sip:littleguy@there.com:5060> \r\nCall-ID: Q2AboBsaGn9!?x6@sipbakeoff.com \r\nCSeq: 1 INVITE \r\nContent-Length: 247\r\n\r\nv=0\r\no=4855 13760799956958020 13760799956958020 IN IP4  129.6.55.78\r\ns=mysession session\r\np=+46 8 52018010\r\nc=IN IP4  129.6.55.78\r\nt=0 0\r\nm=audio 6022 RTP/AVP 0 4 18\r\na=rtpmap:0 PCMU/8000\r\na=rtpmap:4 G723/8000\r\na=rtpmap:18 G729A/8000\r\na=ptime:20\r\n"};
        for (int i = 0; i < 20; i++) {
            new Thread(new AnonymousClass1ParserThread(messages)).start();
        }
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
