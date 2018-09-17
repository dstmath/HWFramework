package gov.nist.javax.sip.address;

import gov.nist.core.Debug;
import gov.nist.core.GenericObject;
import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParameterNames;
import java.text.ParseException;
import java.util.Iterator;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.header.HeaderFactory;
import org.ccil.cowan.tagsoup.XMLWriter;

public class SipUri extends GenericURI implements SipURI, SipURIExt {
    private static final long serialVersionUID = 7749781076218987044L;
    protected Authority authority;
    protected NameValueList qheaders;
    protected TelephoneNumber telephoneSubscriber;
    protected NameValueList uriParms;

    public SipUri() {
        this.scheme = "sip";
        this.uriParms = new NameValueList();
        this.qheaders = new NameValueList();
        this.qheaders.setSeparator(Separators.AND);
    }

    public void setScheme(String scheme) {
        if (scheme.compareToIgnoreCase("sip") == 0 || scheme.compareToIgnoreCase("sips") == 0) {
            this.scheme = scheme.toLowerCase();
            return;
        }
        throw new IllegalArgumentException("bad scheme " + scheme);
    }

    public String getScheme() {
        return this.scheme;
    }

    public void clearUriParms() {
        this.uriParms = new NameValueList();
    }

    public void clearPassword() {
        if (this.authority != null) {
            UserInfo userInfo = this.authority.getUserInfo();
            if (userInfo != null) {
                userInfo.clearPassword();
            }
        }
    }

    public Authority getAuthority() {
        return this.authority;
    }

    public void clearQheaders() {
        this.qheaders = new NameValueList();
    }

    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (!(that instanceof SipURI)) {
            return false;
        }
        SipURI b = (SipURI) that;
        if ((isSecure() ^ b.isSecure()) != 0) {
            return false;
        }
        if (((getUser() == null ? 1 : 0) ^ (b.getUser() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (((getUserPassword() == null ? 1 : 0) ^ (b.getUserPassword() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (getUser() != null && (RFC2396UrlDecoder.decode(getUser()).equals(RFC2396UrlDecoder.decode(b.getUser())) ^ 1) != 0) {
            return false;
        }
        if (getUserPassword() != null && (RFC2396UrlDecoder.decode(getUserPassword()).equals(RFC2396UrlDecoder.decode(b.getUserPassword())) ^ 1) != 0) {
            return false;
        }
        if (((getHost() == null ? 1 : 0) ^ (b.getHost() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (getHost() != null && (getHost().equalsIgnoreCase(b.getHost()) ^ 1) != 0) {
            return false;
        }
        if (getPort() != b.getPort()) {
            return false;
        }
        Iterator i = getParameterNames();
        while (i.hasNext()) {
            String pname = (String) i.next();
            String p1 = getParameter(pname);
            String p2 = b.getParameter(pname);
            if (p1 != null && p2 != null && (RFC2396UrlDecoder.decode(p1).equalsIgnoreCase(RFC2396UrlDecoder.decode(p2)) ^ 1) != 0) {
                return false;
            }
        }
        if (((getTransportParam() == null ? 1 : 0) ^ (b.getTransportParam() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (((getUserParam() == null ? 1 : 0) ^ (b.getUserParam() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (((getTTLParam() == -1 ? 1 : 0) ^ (b.getTTLParam() == -1 ? 1 : 0)) != 0) {
            return false;
        }
        if (((getMethodParam() == null ? 1 : 0) ^ (b.getMethodParam() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (((getMAddrParam() == null ? 1 : 0) ^ (b.getMAddrParam() == null ? 1 : 0)) != 0) {
            return false;
        }
        if (getHeaderNames().hasNext() && (b.getHeaderNames().hasNext() ^ 1) != 0) {
            return false;
        }
        if (!getHeaderNames().hasNext() && b.getHeaderNames().hasNext()) {
            return false;
        }
        if (getHeaderNames().hasNext() && b.getHeaderNames().hasNext()) {
            try {
                HeaderFactory headerFactory = SipFactory.getInstance().createHeaderFactory();
                i = getHeaderNames();
                while (i.hasNext()) {
                    String hname = (String) i.next();
                    String h1 = getHeader(hname);
                    String h2 = b.getHeader(hname);
                    if (h1 == null && h2 != null) {
                        return false;
                    }
                    if (h2 == null && h1 != null) {
                        return false;
                    }
                    if (h1 != null || h2 != null) {
                        try {
                            if (!headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h1)).equals(headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h2)))) {
                                return false;
                            }
                        } catch (ParseException e) {
                            Debug.logError("Cannot parse one of the header of the sip uris to compare " + this + Separators.SP + b, e);
                            return false;
                        }
                    }
                }
            } catch (PeerUnavailableException e2) {
                Debug.logError("Cannot get the header factory to parse the header of the sip uris to compare", e2);
                return false;
            }
        }
        return true;
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.scheme).append(Separators.COLON);
        if (this.authority != null) {
            this.authority.encode(buffer);
        }
        if (!this.uriParms.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.uriParms.encode(buffer);
        }
        if (!this.qheaders.isEmpty()) {
            buffer.append(Separators.QUESTION);
            this.qheaders.encode(buffer);
        }
        return buffer;
    }

    public String toString() {
        return encode();
    }

    public String getUserAtHost() {
        StringBuffer s;
        String user = "";
        if (this.authority.getUserInfo() != null) {
            user = this.authority.getUserInfo().getUser();
        }
        String host = this.authority.getHost().encode();
        if (user.equals("")) {
            s = new StringBuffer();
        } else {
            s = new StringBuffer(user).append(Separators.AT);
        }
        return s.append(host).toString();
    }

    public String getUserAtHostPort() {
        StringBuffer s;
        String user = "";
        if (this.authority.getUserInfo() != null) {
            user = this.authority.getUserInfo().getUser();
        }
        String host = this.authority.getHost().encode();
        int port = this.authority.getPort();
        if (user.equals("")) {
            s = new StringBuffer();
        } else {
            s = new StringBuffer(user).append(Separators.AT);
        }
        if (port != -1) {
            return s.append(host).append(Separators.COLON).append(port).toString();
        }
        return s.append(host).toString();
    }

    public Object getParm(String parmname) {
        return this.uriParms.getValue(parmname);
    }

    public String getMethod() {
        return (String) getParm(XMLWriter.METHOD);
    }

    public NameValueList getParameters() {
        return this.uriParms;
    }

    public void removeParameters() {
        this.uriParms = new NameValueList();
    }

    public NameValueList getQheaders() {
        return this.qheaders;
    }

    public String getUserType() {
        return (String) this.uriParms.getValue("user");
    }

    public String getUserPassword() {
        if (this.authority == null) {
            return null;
        }
        return this.authority.getPassword();
    }

    public void setUserPassword(String password) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setPassword(password);
    }

    public TelephoneNumber getTelephoneSubscriber() {
        if (this.telephoneSubscriber == null) {
            this.telephoneSubscriber = new TelephoneNumber();
        }
        return this.telephoneSubscriber;
    }

    public HostPort getHostPort() {
        if (this.authority == null || this.authority.getHost() == null) {
            return null;
        }
        return this.authority.getHostPort();
    }

    public int getPort() {
        HostPort hp = getHostPort();
        if (hp == null) {
            return -1;
        }
        return hp.getPort();
    }

    public String getHost() {
        if (this.authority == null || this.authority.getHost() == null) {
            return null;
        }
        return this.authority.getHost().encode();
    }

    public boolean isUserTelephoneSubscriber() {
        String usrtype = (String) this.uriParms.getValue("user");
        if (usrtype == null) {
            return false;
        }
        return usrtype.equalsIgnoreCase("phone");
    }

    public void removeTTL() {
        if (this.uriParms != null) {
            this.uriParms.delete("ttl");
        }
    }

    public void removeMAddr() {
        if (this.uriParms != null) {
            this.uriParms.delete("maddr");
        }
    }

    public void removeTransport() {
        if (this.uriParms != null) {
            this.uriParms.delete(ParameterNames.TRANSPORT);
        }
    }

    public void removeHeader(String name) {
        if (this.qheaders != null) {
            this.qheaders.delete(name);
        }
    }

    public void removeHeaders() {
        this.qheaders = new NameValueList();
    }

    public void removeUserType() {
        if (this.uriParms != null) {
            this.uriParms.delete("user");
        }
    }

    public void removePort() {
        this.authority.removePort();
    }

    public void removeMethod() {
        if (this.uriParms != null) {
            this.uriParms.delete(XMLWriter.METHOD);
        }
    }

    public void setUser(String uname) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setUser(uname);
    }

    public void removeUser() {
        this.authority.removeUserInfo();
    }

    public void setDefaultParm(String name, Object value) {
        if (this.uriParms.getValue(name) == null) {
            this.uriParms.set(new NameValue(name, value));
        }
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public void setHost(Host h) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setHost(h);
    }

    public void setUriParms(NameValueList parms) {
        this.uriParms = parms;
    }

    public void setUriParm(String name, Object value) {
        this.uriParms.set(new NameValue(name, value));
    }

    public void setQheaders(NameValueList parms) {
        this.qheaders = parms;
    }

    public void setMAddr(String mAddr) {
        NameValue nameValue = this.uriParms.getNameValue("maddr");
        Host host = new Host();
        host.setAddress(mAddr);
        if (nameValue != null) {
            nameValue.setValueAsObject(host);
            return;
        }
        this.uriParms.set(new NameValue("maddr", host));
    }

    public void setUserParam(String usertype) {
        this.uriParms.set("user", usertype);
    }

    public void setMethod(String method) {
        this.uriParms.set(XMLWriter.METHOD, method);
    }

    public void setIsdnSubAddress(String isdnSubAddress) {
        if (this.telephoneSubscriber == null) {
            this.telephoneSubscriber = new TelephoneNumber();
        }
        this.telephoneSubscriber.setIsdnSubaddress(isdnSubAddress);
    }

    public void setTelephoneSubscriber(TelephoneNumber tel) {
        this.telephoneSubscriber = tel;
    }

    public void setPort(int p) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setPort(p);
    }

    public boolean hasParameter(String name) {
        return this.uriParms.getValue(name) != null;
    }

    public void setQHeader(NameValue nameValue) {
        this.qheaders.set(nameValue);
    }

    public void setUriParameter(NameValue nameValue) {
        this.uriParms.set(nameValue);
    }

    public boolean hasTransport() {
        return hasParameter(ParameterNames.TRANSPORT);
    }

    public void removeParameter(String name) {
        this.uriParms.delete(name);
    }

    public void setHostPort(HostPort hostPort) {
        if (this.authority == null) {
            this.authority = new Authority();
        }
        this.authority.setHostPort(hostPort);
    }

    public Object clone() {
        SipUri retval = (SipUri) super.clone();
        if (this.authority != null) {
            retval.authority = (Authority) this.authority.clone();
        }
        if (this.uriParms != null) {
            retval.uriParms = (NameValueList) this.uriParms.clone();
        }
        if (this.qheaders != null) {
            retval.qheaders = (NameValueList) this.qheaders.clone();
        }
        if (this.telephoneSubscriber != null) {
            retval.telephoneSubscriber = (TelephoneNumber) this.telephoneSubscriber.clone();
        }
        return retval;
    }

    public String getHeader(String name) {
        if (this.qheaders.getValue(name) != null) {
            return this.qheaders.getValue(name).toString();
        }
        return null;
    }

    public Iterator<String> getHeaderNames() {
        return this.qheaders.getNames();
    }

    public String getLrParam() {
        return hasParameter("lr") ? "true" : null;
    }

    public String getMAddrParam() {
        NameValue maddr = this.uriParms.getNameValue("maddr");
        if (maddr == null) {
            return null;
        }
        return (String) maddr.getValueAsObject();
    }

    public String getMethodParam() {
        return getParameter(XMLWriter.METHOD);
    }

    public String getParameter(String name) {
        Object val = this.uriParms.getValue(name);
        if (val == null) {
            return null;
        }
        if (val instanceof GenericObject) {
            return ((GenericObject) val).encode();
        }
        return val.toString();
    }

    public Iterator<String> getParameterNames() {
        return this.uriParms.getNames();
    }

    public int getTTLParam() {
        Integer ttl = (Integer) this.uriParms.getValue("ttl");
        if (ttl != null) {
            return ttl.intValue();
        }
        return -1;
    }

    public String getTransportParam() {
        if (this.uriParms != null) {
            return (String) this.uriParms.getValue(ParameterNames.TRANSPORT);
        }
        return null;
    }

    public String getUser() {
        return this.authority.getUser();
    }

    public boolean isSecure() {
        return getScheme().equalsIgnoreCase("sips");
    }

    public boolean isSipURI() {
        return true;
    }

    public void setHeader(String name, String value) {
        this.qheaders.set(new NameValue(name, value));
    }

    public void setHost(String host) throws ParseException {
        setHost(new Host(host));
    }

    public void setLrParam() {
        this.uriParms.set("lr", null);
    }

    public void setMAddrParam(String maddr) throws ParseException {
        if (maddr == null) {
            throw new NullPointerException("bad maddr");
        }
        setParameter("maddr", maddr);
    }

    public void setMethodParam(String method) throws ParseException {
        setParameter(XMLWriter.METHOD, method);
    }

    public void setParameter(String name, String value) throws ParseException {
        if (name.equalsIgnoreCase("ttl")) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ParseException("bad parameter " + value, 0);
            }
        }
        this.uriParms.set(name, value);
    }

    public void setSecure(boolean secure) {
        if (secure) {
            this.scheme = "sips";
        } else {
            this.scheme = "sip";
        }
    }

    public void setTTLParam(int ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("Bad ttl value");
        } else if (this.uriParms != null) {
            this.uriParms.set(new NameValue("ttl", Integer.valueOf(ttl)));
        }
    }

    public void setTransportParam(String transport) throws ParseException {
        if (transport == null) {
            throw new NullPointerException("null arg");
        } else if (transport.compareToIgnoreCase(ListeningPoint.UDP) == 0 || transport.compareToIgnoreCase(ListeningPoint.TLS) == 0 || transport.compareToIgnoreCase(ListeningPoint.TCP) == 0 || transport.compareToIgnoreCase(ListeningPoint.SCTP) == 0) {
            this.uriParms.set(new NameValue(ParameterNames.TRANSPORT, transport.toLowerCase()));
        } else {
            throw new ParseException("bad transport " + transport, 0);
        }
    }

    public String getUserParam() {
        return getParameter("user");
    }

    public boolean hasLrParam() {
        return this.uriParms.getNameValue("lr") != null;
    }

    public boolean hasGrParam() {
        return this.uriParms.getNameValue("gr") != null;
    }

    public void setGrParam(String value) {
        this.uriParms.set("gr", value);
    }

    public String getGrParam() {
        return (String) this.uriParms.getValue("gr");
    }
}
