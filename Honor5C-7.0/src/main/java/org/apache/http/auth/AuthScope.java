package org.apache.http.auth;

import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public class AuthScope {
    public static final AuthScope ANY = null;
    public static final String ANY_HOST = null;
    public static final int ANY_PORT = -1;
    public static final String ANY_REALM = null;
    public static final String ANY_SCHEME = null;
    private final String host;
    private final int port;
    private final String realm;
    private final String scheme;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.auth.AuthScope.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.auth.AuthScope.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.auth.AuthScope.<clinit>():void");
    }

    public AuthScope(String host, int port, String realm, String scheme) {
        String str;
        this.host = host == null ? ANY_HOST : host.toLowerCase(Locale.ENGLISH);
        if (port < 0) {
            port = ANY_PORT;
        }
        this.port = port;
        if (realm == null) {
            realm = ANY_REALM;
        }
        this.realm = realm;
        if (scheme == null) {
            str = ANY_SCHEME;
        } else {
            str = scheme.toUpperCase(Locale.ENGLISH);
        }
        this.scheme = str;
    }

    public AuthScope(String host, int port, String realm) {
        this(host, port, realm, ANY_SCHEME);
    }

    public AuthScope(String host, int port) {
        this(host, port, ANY_REALM, ANY_SCHEME);
    }

    public AuthScope(AuthScope authscope) {
        if (authscope == null) {
            throw new IllegalArgumentException("Scope may not be null");
        }
        this.host = authscope.getHost();
        this.port = authscope.getPort();
        this.realm = authscope.getRealm();
        this.scheme = authscope.getScheme();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getScheme() {
        return this.scheme;
    }

    public int match(AuthScope that) {
        int factor = 0;
        if (LangUtils.equals(this.scheme, that.scheme)) {
            factor = 1;
        } else if (!(this.scheme == ANY_SCHEME || that.scheme == ANY_SCHEME)) {
            return ANY_PORT;
        }
        if (LangUtils.equals(this.realm, that.realm)) {
            factor += 2;
        } else if (!(this.realm == ANY_REALM || that.realm == ANY_REALM)) {
            return ANY_PORT;
        }
        if (this.port == that.port) {
            factor += 4;
        } else if (!(this.port == ANY_PORT || that.port == ANY_PORT)) {
            return ANY_PORT;
        }
        if (LangUtils.equals(this.host, that.host)) {
            factor += 8;
        } else if (this.host == ANY_HOST || that.host == ANY_HOST) {
            return factor;
        } else {
            return ANY_PORT;
        }
        return factor;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof AuthScope)) {
            return super.equals(o);
        }
        AuthScope that = (AuthScope) o;
        if (LangUtils.equals(this.host, that.host) && this.port == that.port && LangUtils.equals(this.realm, that.realm)) {
            z = LangUtils.equals(this.scheme, that.scheme);
        }
        return z;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (this.scheme != null) {
            buffer.append(this.scheme.toUpperCase(Locale.ENGLISH));
            buffer.append(' ');
        }
        if (this.realm != null) {
            buffer.append('\'');
            buffer.append(this.realm);
            buffer.append('\'');
        } else {
            buffer.append("<any realm>");
        }
        if (this.host != null) {
            buffer.append('@');
            buffer.append(this.host);
            if (this.port >= 0) {
                buffer.append(':');
                buffer.append(this.port);
            }
        }
        return buffer.toString();
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(LangUtils.hashCode(17, this.host), this.port), this.realm), this.scheme);
    }
}
