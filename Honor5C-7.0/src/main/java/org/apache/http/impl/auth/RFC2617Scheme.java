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

    protected void parseChallenge(CharArrayBuffer buffer, int pos, int len) throws MalformedChallengeException {
        HeaderElement[] elements = BasicHeaderValueParser.DEFAULT.parseElements(buffer, new ParserCursor(pos, buffer.length()));
        if (elements.length == 0) {
            throw new MalformedChallengeException("Authentication challenge is empty");
        }
        this.params = new HashMap(elements.length);
        for (HeaderElement element : elements) {
            this.params.put(element.getName(), element.getValue());
        }
    }

    protected Map<String, String> getParameters() {
        if (this.params == null) {
            this.params = new HashMap();
        }
        return this.params;
    }

    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null");
        } else if (this.params == null) {
            return null;
        } else {
            return (String) this.params.get(name.toLowerCase(Locale.ENGLISH));
        }
    }

    public String getRealm() {
        return getParameter("realm");
    }
}
