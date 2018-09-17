package org.apache.http.message;

import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;

@Deprecated
public class BasicHttpResponse extends AbstractHttpMessage implements HttpResponse {
    private HttpEntity entity;
    private Locale locale;
    private ReasonPhraseCatalog reasonCatalog;
    private StatusLine statusline;

    public BasicHttpResponse(StatusLine statusline, ReasonPhraseCatalog catalog, Locale locale) {
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null.");
        }
        this.statusline = statusline;
        this.reasonCatalog = catalog;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        this.locale = locale;
    }

    public BasicHttpResponse(StatusLine statusline) {
        this(statusline, null, null);
    }

    public BasicHttpResponse(ProtocolVersion ver, int code, String reason) {
        this(new BasicStatusLine(ver, code, reason), null, null);
    }

    public ProtocolVersion getProtocolVersion() {
        return this.statusline.getProtocolVersion();
    }

    public StatusLine getStatusLine() {
        return this.statusline;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setStatusLine(StatusLine statusline) {
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        this.statusline = statusline;
    }

    public void setStatusLine(ProtocolVersion ver, int code) {
        this.statusline = new BasicStatusLine(ver, code, getReason(code));
    }

    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        this.statusline = new BasicStatusLine(ver, code, reason);
    }

    public void setStatusCode(int code) {
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
    }

    public void setReasonPhrase(String reason) {
        if (reason == null || (reason.indexOf(10) < 0 && reason.indexOf(13) < 0)) {
            this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), this.statusline.getStatusCode(), reason);
            return;
        }
        throw new IllegalArgumentException("Line break in reason phrase.");
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public void setLocale(Locale loc) {
        if (loc == null) {
            throw new IllegalArgumentException("Locale may not be null.");
        }
        this.locale = loc;
        int code = this.statusline.getStatusCode();
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
    }

    protected String getReason(int code) {
        if (this.reasonCatalog == null) {
            return null;
        }
        return this.reasonCatalog.getReason(code, this.locale);
    }
}
