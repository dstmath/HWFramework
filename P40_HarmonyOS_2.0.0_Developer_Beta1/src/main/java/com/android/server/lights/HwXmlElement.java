package com.android.server.lights;

import android.graphics.PointF;
import android.util.Log;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.util.LogEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class HwXmlElement {
    protected final boolean HWDEBUG;
    protected final boolean HWFLOW;
    protected final String TAG = ("HwXmlElement_" + getName());
    private Map<String, HwXmlElement> mChildMap;
    protected boolean mIsParsed;

    public abstract String getName();

    /* access modifiers changed from: protected */
    public abstract boolean parseValue(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;

    public HwXmlElement() {
        boolean z = true;
        this.HWDEBUG = LogEx.getLogHWInfo() && LogEx.getHWModuleLog() && Log.isLoggable(this.TAG, 3);
        if (!LogEx.getLogHWInfo() && (!LogEx.getHWModuleLog() || !Log.isLoggable(this.TAG, 4))) {
            z = false;
        }
        this.HWFLOW = z;
    }

    /* access modifiers changed from: protected */
    public List<String> getNameList() {
        return Collections.emptyList();
    }

    /* access modifiers changed from: protected */
    public boolean isOptional() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isParsedValueValid() {
        return true;
    }

    public HwXmlElement registerChildElement(HwXmlElement element) {
        if (element == null) {
            SlogEx.e(this.TAG, "registerChildElement() error! input element is null!");
            return element;
        }
        if (this.HWDEBUG) {
            String str = this.TAG;
            SlogEx.d(str, "registerChildElement() " + element.getName());
        }
        if (this.mChildMap == null) {
            this.mChildMap = new HashMap();
        }
        List<String> nameList = element.getNameList();
        if (nameList != null && !nameList.isEmpty()) {
            for (String name : nameList) {
                if (this.mChildMap.put(name, element) != null) {
                    String str2 = this.TAG;
                    SlogEx.w(str2, "registerChildElement() warning! " + name + " already registered!");
                }
            }
        } else if (this.mChildMap.put(element.getName(), element) != null) {
            String str3 = this.TAG;
            SlogEx.w(str3, "registerChildElement() warning! " + element.getName() + " already registered!");
        }
        return element;
    }

    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser != null) {
            try {
                if (!parseValue(parser)) {
                    XmlUtilsEx.skipCurrentTag(parser);
                    return;
                }
                this.mIsParsed = true;
                if (this.mChildMap != null) {
                    int specifiedDepth = parser.getDepth();
                    int type = parser.next();
                    int currentDepth = parser.getDepth();
                    while (isInSpecifiedDepth(specifiedDepth, type, currentDepth)) {
                        if (type == 2) {
                            String tagName = parser.getName();
                            if (this.HWDEBUG) {
                                String str = this.TAG;
                                SlogEx.d(str, "parse() tagName = " + tagName);
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
        } else {
            throw new XmlPullParserException("parse() failed, parser==null");
        }
    }

    private boolean isInSpecifiedDepth(int specifiedDepth, int type, int currentDepth) {
        if (type == 1) {
            return false;
        }
        return type != 3 || currentDepth > specifiedDepth;
    }

    public boolean isXmlDataValid() {
        if (this.mIsParsed) {
            Map<String, HwXmlElement> map = this.mChildMap;
            if (map != null) {
                for (HwXmlElement element : map.values()) {
                    if (!element.isXmlDataValid()) {
                        String str = this.TAG;
                        SlogEx.e(str, "isXmlDataValid() " + element.getName() + " isXmlDataValid() failed!");
                        return false;
                    }
                }
            }
            if (isParsedValueValid()) {
                return true;
            }
            SlogEx.e(this.TAG, "isXmlDataValid() isParsedValueValid() failed!");
            return false;
        } else if (isOptional()) {
            return true;
        } else {
            SlogEx.e(this.TAG, "isXmlDataValid() required tag didn't parsed");
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
        List<PointF> result = list;
        if (parser != null) {
            try {
                String str = parser.nextText();
                String[] pointSplited = str.split(",");
                if (pointSplited.length == 2) {
                    float valueX = string2Float(pointSplited[0]);
                    float valueY = string2Float(pointSplited[1]);
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(new PointF(valueX, valueY));
                    return result;
                }
                throw new XmlPullParserException("parsePointFList() split failed, text=" + str);
            } catch (NumberFormatException e) {
                throw new XmlPullParserException("parsePointFList() " + e);
            }
        } else {
            throw new XmlPullParserException("parsePointFList() failed, parser==null");
        }
    }
}
