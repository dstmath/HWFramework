package org.apache.http.impl.cookie;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class BasicClientCookie implements SetCookie, ClientCookie, Cloneable {
    private Map<String, String> attribs;
    private String cookieComment;
    private String cookieDomain;
    private Date cookieExpiryDate;
    private String cookiePath;
    private int cookieVersion;
    private boolean isSecure;
    private final String name;
    private String value;

    public BasicClientCookie(String name2, String value2) {
        if (name2 != null) {
            this.name = name2;
            this.attribs = new HashMap();
            this.value = value2;
            return;
        }
        throw new IllegalArgumentException("Name may not be null");
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }

    public String getComment() {
        return this.cookieComment;
    }

    public void setComment(String comment) {
        this.cookieComment = comment;
    }

    public String getCommentURL() {
        return null;
    }

    public Date getExpiryDate() {
        return this.cookieExpiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.cookieExpiryDate = expiryDate;
    }

    public boolean isPersistent() {
        return this.cookieExpiryDate != null;
    }

    public String getDomain() {
        return this.cookieDomain;
    }

    public void setDomain(String domain) {
        if (domain != null) {
            this.cookieDomain = domain.toLowerCase(Locale.ENGLISH);
        } else {
            this.cookieDomain = null;
        }
    }

    public String getPath() {
        return this.cookiePath;
    }

    public void setPath(String path) {
        this.cookiePath = path;
    }

    public boolean isSecure() {
        return this.isSecure;
    }

    public void setSecure(boolean secure) {
        this.isSecure = secure;
    }

    public int[] getPorts() {
        return null;
    }

    public int getVersion() {
        return this.cookieVersion;
    }

    public void setVersion(int version) {
        this.cookieVersion = version;
    }

    public boolean isExpired(Date date) {
        if (date != null) {
            return this.cookieExpiryDate != null && this.cookieExpiryDate.getTime() <= date.getTime();
        }
        throw new IllegalArgumentException("Date may not be null");
    }

    public void setAttribute(String name2, String value2) {
        this.attribs.put(name2, value2);
    }

    public String getAttribute(String name2) {
        return this.attribs.get(name2);
    }

    public boolean containsAttribute(String name2) {
        return this.attribs.get(name2) != null;
    }

    public Object clone() throws CloneNotSupportedException {
        BasicClientCookie clone = (BasicClientCookie) super.clone();
        clone.attribs = new HashMap(this.attribs);
        return clone;
    }

    public String toString() {
        return "[version: " + Integer.toString(this.cookieVersion) + "]" + "[name: " + this.name + "]" + "[value: " + this.value + "]" + "[domain: " + this.cookieDomain + "]" + "[path: " + this.cookiePath + "]" + "[expiry: " + this.cookieExpiryDate + "]";
    }
}
