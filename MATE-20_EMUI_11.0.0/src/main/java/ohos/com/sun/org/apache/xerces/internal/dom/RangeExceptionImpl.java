package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.ranges.RangeException;

public class RangeExceptionImpl extends RangeException {
    static final long serialVersionUID = -9058052627467240856L;

    public RangeExceptionImpl(short s, String str) {
        super(s, str);
    }
}
