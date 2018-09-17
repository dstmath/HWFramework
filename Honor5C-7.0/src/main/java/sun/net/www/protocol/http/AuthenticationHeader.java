package sun.net.www.protocol.http;

import java.util.HashMap;
import java.util.Iterator;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;

public class AuthenticationHeader {
    static String authPref;
    boolean dontUseNegotiate;
    private final HttpCallerInfo hci;
    String hdrname;
    HeaderParser preferred;
    String preferred_r;
    MessageHeader rsp;
    HashMap schemes;

    static class SchemeMapValue {
        HeaderParser parser;
        String raw;

        SchemeMapValue(HeaderParser h, String r) {
            this.raw = r;
            this.parser = h;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.http.AuthenticationHeader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.http.AuthenticationHeader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.http.AuthenticationHeader.<clinit>():void");
    }

    public String toString() {
        return "AuthenticationHeader: prefer " + this.preferred_r;
    }

    public AuthenticationHeader(String hdrname, MessageHeader response, HttpCallerInfo hci, boolean dontUseNegotiate) {
        this.dontUseNegotiate = false;
        this.hci = hci;
        this.dontUseNegotiate = dontUseNegotiate;
        this.rsp = response;
        this.hdrname = hdrname;
        this.schemes = new HashMap();
        parse();
    }

    public HttpCallerInfo getHttpCallerInfo() {
        return this.hci;
    }

    private void parse() {
        SchemeMapValue tmp;
        Iterator iter = this.rsp.multiValueIterator(this.hdrname);
        while (iter.hasNext()) {
            String raw = (String) iter.next();
            HeaderParser hp = new HeaderParser(raw);
            Iterator keys = hp.keys();
            int i = 0;
            int lastSchemeIndex = -1;
            while (keys.hasNext()) {
                keys.next();
                if (hp.findValue(i) == null) {
                    if (lastSchemeIndex != -1) {
                        HeaderParser hpn = hp.subsequence(lastSchemeIndex, i);
                        this.schemes.put(hpn.findKey(0), new SchemeMapValue(hpn, raw));
                    }
                    lastSchemeIndex = i;
                }
                i++;
            }
            if (i > lastSchemeIndex) {
                hpn = hp.subsequence(lastSchemeIndex, i);
                this.schemes.put(hpn.findKey(0), new SchemeMapValue(hpn, raw));
            }
        }
        SchemeMapValue schemeMapValue = null;
        if (authPref != null) {
            schemeMapValue = (SchemeMapValue) this.schemes.get(authPref);
            if (schemeMapValue != null) {
                if (this.dontUseNegotiate && authPref.equals("negotiate")) {
                    schemeMapValue = null;
                }
                if (schemeMapValue != null) {
                    this.preferred = schemeMapValue.parser;
                    this.preferred_r = schemeMapValue.raw;
                }
            }
        }
        if (schemeMapValue == null && !this.dontUseNegotiate) {
            tmp = (SchemeMapValue) this.schemes.get("negotiate");
            if (tmp != null) {
                if (this.hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(this.hci, "Negotiate"))) {
                    tmp = null;
                }
                schemeMapValue = tmp;
            }
        }
        if (schemeMapValue == null && !this.dontUseNegotiate) {
            tmp = (SchemeMapValue) this.schemes.get("kerberos");
            if (tmp != null) {
                if (this.hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(this.hci, "Kerberos"))) {
                    tmp = null;
                }
                schemeMapValue = tmp;
            }
        }
        if (schemeMapValue == null) {
            schemeMapValue = (SchemeMapValue) this.schemes.get("digest");
            if (schemeMapValue == null) {
                schemeMapValue = (SchemeMapValue) this.schemes.get("ntlm");
                if (schemeMapValue == null) {
                    schemeMapValue = (SchemeMapValue) this.schemes.get("basic");
                }
            }
        }
        if (schemeMapValue != null) {
            this.preferred = schemeMapValue.parser;
            this.preferred_r = schemeMapValue.raw;
        }
    }

    public HeaderParser headerParser() {
        return this.preferred;
    }

    public String scheme() {
        if (this.preferred != null) {
            return this.preferred.findKey(0);
        }
        return null;
    }

    public String raw() {
        return this.preferred_r;
    }

    public boolean isPresent() {
        return this.preferred != null;
    }
}
