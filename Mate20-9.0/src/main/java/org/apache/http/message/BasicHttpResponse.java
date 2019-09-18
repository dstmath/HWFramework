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

    public BasicHttpResponse(StatusLine statusline2, ReasonPhraseCatalog catalog, Locale locale2) {
        if (statusline2 != null) {
            this.statusline = statusline2;
            this.reasonCatalog = catalog;
            this.locale = locale2 != null ? locale2 : Locale.getDefault();
            return;
        }
        throw new IllegalArgumentException("Status line may not be null.");
    }

    public BasicHttpResponse(StatusLine statusline2) {
        this(statusline2, (ReasonPhraseCatalog) null, (Locale) null);
    }

    public BasicHttpResponse(ProtocolVersion ver, int code, String reason) {
        this((StatusLine) new BasicStatusLine(ver, code, reason), (ReasonPhraseCatalog) null, (Locale) null);
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

    public void setStatusLine(StatusLine statusline2) {
        if (statusline2 != null) {
            this.statusline = statusline2;
            return;
        }
        throw new IllegalArgumentException("Status line may not be null");
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

    public void setEntity(HttpEntity entity2) {
        this.entity = entity2;
    }

    public void setLocale(Locale loc) {
        if (loc != null) {
            this.locale = loc;
            int code = this.statusline.getStatusCode();
            this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
            return;
        }
        throw new IllegalArgumentException("Locale may not be null.");
    }

    /* access modifiers changed from: protected */
    public String getReason(int code) {
        if (this.reasonCatalog == null) {
            return null;
        }
        return this.reasonCatalog.getReason(code, this.locale);
    }
}
