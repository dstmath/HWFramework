package org.apache.http.impl.auth;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.http.HeaderElement;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class RFC2617Scheme extends AuthSchemeBase {
    private Map<String, String> params;

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.auth.AuthSchemeBase
    public void parseChallenge(CharArrayBuffer buffer, int pos, int len) throws MalformedChallengeException {
        HeaderElement[] elements = BasicHeaderValueParser.DEFAULT.parseElements(buffer, new ParserCursor(pos, buffer.length()));
        if (elements.length != 0) {
            this.params = new HashMap(elements.length);
            for (HeaderElement element : elements) {
                this.params.put(element.getName(), element.getValue());
            }
            return;
        }
        throw new MalformedChallengeException("Authentication challenge is empty");
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParameters() {
        if (this.params == null) {
            this.params = new HashMap();
        }
        return this.params;
    }

    @Override // org.apache.http.auth.AuthScheme
    public String getParameter(String name) {
        if (name != null) {
            Map<String, String> map = this.params;
            if (map == null) {
                return null;
            }
            return map.get(name.toLowerCase(Locale.ENGLISH));
        }
        throw new IllegalArgumentException("Parameter name may not be null");
    }

    @Override // org.apache.http.auth.AuthScheme
    public String getRealm() {
        return getParameter("realm");
    }
}
