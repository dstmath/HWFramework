package android.icu.util;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class TimeUnit extends MeasureUnit {
    private static final long serialVersionUID = -2839973855554750484L;
    private final int index;

    TimeUnit(String type, String code) {
        super(type, code);
        this.index = 0;
    }

    public static TimeUnit[] values() {
        return new TimeUnit[]{SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR};
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(this.type, this.subType);
    }

    private Object readResolve() throws ObjectStreamException {
        switch (this.index) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                return YEAR;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return MONTH;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return WEEK;
            case XmlPullParser.END_TAG /*3*/:
                return DAY;
            case NodeFilter.SHOW_TEXT /*4*/:
                return HOUR;
            case XmlPullParser.CDSECT /*5*/:
                return MINUTE;
            case XmlPullParser.ENTITY_REF /*6*/:
                return SECOND;
            default:
                throw new InvalidObjectException("Bad index: " + this.index);
        }
    }
}
