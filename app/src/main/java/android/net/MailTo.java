package android.net;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class MailTo {
    private static final String BODY = "body";
    private static final String CC = "cc";
    public static final String MAILTO_SCHEME = "mailto:";
    private static final String SUBJECT = "subject";
    private static final String TO = "to";
    private HashMap<String, String> mHeaders;

    public static boolean isMailTo(String url) {
        if (url == null || !url.startsWith(MAILTO_SCHEME)) {
            return false;
        }
        return true;
    }

    public static MailTo parse(String url) throws ParseException {
        if (url == null) {
            throw new NullPointerException();
        } else if (isMailTo(url)) {
            Uri email = Uri.parse(url.substring(MAILTO_SCHEME.length()));
            MailTo m = new MailTo();
            String query = email.getQuery();
            if (query != null) {
                for (String q : query.split("&")) {
                    String[] nameval = q.split("=");
                    if (nameval.length != 0) {
                        m.mHeaders.put(Uri.decode(nameval[0]).toLowerCase(Locale.ROOT), nameval.length > 1 ? Uri.decode(nameval[1]) : null);
                    }
                }
            }
            String address = email.getPath();
            if (address != null) {
                String addr = m.getTo();
                if (addr != null) {
                    address = address + ", " + addr;
                }
                m.mHeaders.put(TO, address);
            }
            return m;
        } else {
            throw new ParseException("Not a mailto scheme");
        }
    }

    public String getTo() {
        return (String) this.mHeaders.get(TO);
    }

    public String getCc() {
        return (String) this.mHeaders.get(CC);
    }

    public String getSubject() {
        return (String) this.mHeaders.get(SUBJECT);
    }

    public String getBody() {
        return (String) this.mHeaders.get(BODY);
    }

    public Map<String, String> getHeaders() {
        return this.mHeaders;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(MAILTO_SCHEME);
        sb.append('?');
        for (Entry<String, String> header : this.mHeaders.entrySet()) {
            sb.append(Uri.encode((String) header.getKey()));
            sb.append('=');
            sb.append(Uri.encode((String) header.getValue()));
            sb.append('&');
        }
        return sb.toString();
    }

    private MailTo() {
        this.mHeaders = new HashMap();
    }
}
