package com.huawei.displayengine;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public abstract class HwXmlElement {
    protected final boolean HWDEBUG;
    protected final boolean HWFLOW;
    protected final String TAG = ("HwXmlElement_" + getName());
    private Map<String, HwXmlElement> mChildMap;
    protected boolean mIsParsed;

    public abstract String getName();

    /* access modifiers changed from: protected */
    public abstract boolean parseValue(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;

    HwXmlElement() {
        boolean z = false;
        this.HWDEBUG = Log.HWLog || (Log.HWModuleLog && Log.isLoggable(this.TAG, 3));
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(this.TAG, 4))) {
            z = true;
        }
        this.HWFLOW = z;
    }

    /* access modifiers changed from: protected */
    public List<String> getNameList() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isOptional() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkValue() {
        return true;
    }

    public HwXmlElement registerChildElement(HwXmlElement element) {
        if (element == null) {
            Slog.e(this.TAG, "registerChildElement() error! input element is null!");
            return null;
        }
        if (this.HWDEBUG) {
            String str = this.TAG;
            Slog.d(str, "registerChildElement() " + element.getName());
        }
        if (this.mChildMap == null) {
            this.mChildMap = new HashMap();
        }
        List<String> nameList = element.getNameList();
        if (nameList != null) {
            for (String name : nameList) {
                if (this.mChildMap.put(name, element) != null) {
                    String str2 = this.TAG;
                    Slog.w(str2, "registerChildElement() warning! " + name + " already registered!");
                }
            }
        } else if (this.mChildMap.put(element.getName(), element) != null) {
            String str3 = this.TAG;
            Slog.w(str3, "registerChildElement() warning! " + element.getName() + " already registered!");
        }
        return element;
    }

    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        try {
            if (!parseValue(parser)) {
                XmlUtils.skipCurrentTag(parser);
                return;
            }
            this.mIsParsed = true;
            if (this.mChildMap != null) {
                int specifiedDepth = parser.getDepth();
                int type = parser.next();
                int currentDepth = parser.getDepth();
                while (inSpecifiedDepth(specifiedDepth, type, currentDepth)) {
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.HWDEBUG) {
                            String str = this.TAG;
                            Slog.d(str, "parse() tagName = " + tagName);
                        }
                        HwXmlElement element = this.mChildMap.get(tagName);
                        if (element != null) {
                            element.parse(parser);
                        }
                    }
                    type = parser.next();
                    currentDepth = parser.getDepth();
                }
            }
        } catch (XmlPullParserException e) {
            throw new XmlPullParserException("Element:" + getName() + ", " + e);
        } catch (IOException e2) {
            throw new IOException("Element:" + getName() + ", " + e2);
        }
    }

    private boolean inSpecifiedDepth(int specifiedDepth, int type, int currentDepth) {
        if (type == 1) {
            return false;
        }
        return type != 3 || currentDepth > specifiedDepth;
    }

    public boolean check() {
        if (this.mIsParsed) {
            Map<String, HwXmlElement> map = this.mChildMap;
            if (map != null) {
                for (HwXmlElement element : map.values()) {
                    if (!element.check()) {
                        String str = this.TAG;
                        Slog.e(str, "check() " + element.getName() + " check() failed!");
                        return false;
                    }
                }
            }
            if (checkValue()) {
                return true;
            }
            Slog.e(this.TAG, "check() checkValue() failed!");
            return false;
        } else if (isOptional()) {
            return true;
        } else {
            Slog.e(this.TAG, "check() required tag didn't parsed");
            return false;
        }
    }

    protected static boolean string2Boolean(String str) throws XmlPullParserException {
        if (str != null && !str.isEmpty()) {
            return Boolean.parseBoolean(str);
        }
        throw new XmlPullParserException("string2Boolean() input str is null or empty!");
    }

    protected static int string2Int(String str) throws XmlPullParserException {
        if (str == null || str.isEmpty()) {
            throw new XmlPullParserException("string2Int() input str is null or empty!");
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("string2Int() " + e);
        }
    }

    protected static long string2Long(String str) throws XmlPullParserException {
        if (str == null || str.isEmpty()) {
            throw new XmlPullParserException("string2Long() input str is null or empty!");
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("string2Long() " + e);
        }
    }

    protected static float string2Float(String str) throws XmlPullParserException {
        if (str == null || str.isEmpty()) {
            throw new XmlPullParserException("string2Float() input str is null or empty!");
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("string2Float() " + e);
        }
    }

    protected static List<PointF> parsePointFList(XmlPullParser parser, List<PointF> list) throws XmlPullParserException, IOException {
        try {
            String s = parser.nextText();
            String[] pointSplited = s.split(",");
            if (pointSplited.length == 2) {
                float x = string2Float(pointSplited[0]);
                float y = string2Float(pointSplited[1]);
                if (list == null) {
                    list = new ArrayList();
                }
                list.add(new PointF(x, y));
                return list;
            }
            throw new XmlPullParserException("parsePointFList() split failed, text=" + s);
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("parsePointFList() " + e);
        }
    }
}
