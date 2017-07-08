package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.cookie.SetCookie2;

@Deprecated
public class BasicClientCookie2 extends BasicClientCookie implements SetCookie2 {
    private String commentURL;
    private boolean discard;
    private int[] ports;

    public BasicClientCookie2(String name, String value) {
        super(name, value);
    }

    public int[] getPorts() {
        return this.ports;
    }

    public void setPorts(int[] ports) {
        this.ports = ports;
    }

    public String getCommentURL() {
        return this.commentURL;
    }

    public void setCommentURL(String commentURL) {
        this.commentURL = commentURL;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    public boolean isPersistent() {
        return !this.discard ? super.isPersistent() : false;
    }

    public boolean isExpired(Date date) {
        return !this.discard ? super.isExpired(date) : true;
    }

    public Object clone() throws CloneNotSupportedException {
        BasicClientCookie2 clone = (BasicClientCookie2) super.clone();
        clone.ports = (int[]) this.ports.clone();
        return clone;
    }
}
