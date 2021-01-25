package com.android.server.lights;

import android.util.Log;
import android.util.Xml;
import com.huawei.android.util.SlogEx;
import com.huawei.util.LogEx;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwXmlParser {
    private static final boolean HWDEBUG = (LogEx.getLogHWInfo() && LogEx.getHWModuleLog() && Log.isLoggable(TAG, 3));
    private static final boolean HWFLOW;
    private static final String TAG = "HwXmlParser";
    private boolean mIsParseFinished;
    private HwXmlElement mRootElement;
    private final String mXmlPath;

    static {
        boolean z = true;
        if (!LogEx.getLogHWInfo() && (!LogEx.getHWModuleLog() || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public HwXmlParser(String xmlPath) {
        this.mXmlPath = xmlPath;
        if (this.mXmlPath == null) {
            SlogEx.e(TAG, "HwXmlParser() error! input xmlPath is null!");
        }
    }

    public HwXmlElement registerRootElement(HwXmlElement element) {
        if (HWDEBUG) {
            SlogEx.d(TAG, "registerRootElement()");
        }
        this.mRootElement = element;
        return element;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a1, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a4, code lost:
        throw r4;
     */
    public boolean parse() {
        if (HWFLOW) {
            SlogEx.i(TAG, "parse()");
        }
        String str = this.mXmlPath;
        if (str == null) {
            SlogEx.e(TAG, "parse() error! mXmlPath is null!");
            return false;
        } else if (this.mRootElement == null) {
            SlogEx.e(TAG, "parse() error! hasn't registerRootElement");
            return false;
        } else {
            try {
                FileInputStream inputStream = new FileInputStream(str);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, StandardCharsets.UTF_8.name());
                String rootTagName = this.mRootElement.getName();
                int specifiedDepth = parser.getDepth();
                int type = parser.next();
                for (int currentDepth = parser.getDepth(); inSpecifiedDepth(specifiedDepth, type, currentDepth); currentDepth = parser.getDepth()) {
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (HWDEBUG) {
                            SlogEx.d(TAG, "parse() rootTagName=" + rootTagName + ",tagName=" + tagName);
                        }
                        if (tagName.equals(rootTagName)) {
                            this.mRootElement.parse(parser);
                        }
                    }
                    type = parser.next();
                }
                this.mIsParseFinished = true;
                if (HWFLOW) {
                    SlogEx.i(TAG, "parse() done");
                }
                inputStream.close();
                return true;
            } catch (XmlPullParserException e) {
                SlogEx.e(TAG, "parse() error! XmlPullParserException");
                return false;
            } catch (IOException e2) {
                SlogEx.e(TAG, "parse() error! IOException");
                return false;
            }
        }
    }

    private boolean inSpecifiedDepth(int specifiedDepth, int type, int currentDepth) {
        if (type == 1) {
            return false;
        }
        return type != 3 || currentDepth > specifiedDepth;
    }

    public boolean isXmlDataValid() {
        if (HWFLOW) {
            SlogEx.i(TAG, "isXmlDataValid()");
        }
        if (this.mIsParseFinished && this.mRootElement.isXmlDataValid()) {
            return true;
        }
        return false;
    }
}
