package com.android.server.display;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwXmlParser {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwXmlParser";
    private boolean mIsParseFinished;
    private HwXmlElement mRootElement;
    private final String mXmlPath;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z;
    }

    public HwXmlParser(String xmlPath) {
        this.mXmlPath = xmlPath;
        if (this.mXmlPath == null) {
            Slog.e(TAG, "HwXmlParser() error! input xmlPath is null!");
        } else if (HWFLOW) {
            Slog.i(TAG, "HwXmlParser() xmlPath = " + this.mXmlPath);
        }
    }

    private FileInputStream getXmlFile() {
        if (HWDEBUG) {
            Slog.d(TAG, "getXmlFile()");
        }
        if (this.mXmlPath == null) {
            Slog.e(TAG, "getXmlFile() error! mXmlPath is null!");
            return null;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(this.mXmlPath);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "getXmlFile() failed! FileNotFoundException");
        }
        return fileInputStream;
    }

    public HwXmlElement registerRootElement(HwXmlElement element) {
        if (HWDEBUG) {
            Slog.d(TAG, "registerRootElement()");
        }
        this.mRootElement = element;
        return element;
    }

    public boolean parse() {
        if (HWFLOW) {
            Slog.i(TAG, "parse()");
        }
        if (this.mRootElement == null) {
            Slog.e(TAG, "parse() error! hasn't registerRootElement");
            return false;
        }
        FileInputStream inputStream = getXmlFile();
        if (inputStream == null) {
            return false;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
            String rootTagName = this.mRootElement.getName();
            int specifiedDepth = parser.getDepth();
            int type = parser.next();
            for (int currentDepth = parser.getDepth(); inSpecifiedDepth(specifiedDepth, type, currentDepth); currentDepth = parser.getDepth()) {
                if (type == 2) {
                    String tagName = parser.getName();
                    if (HWDEBUG) {
                        Slog.d(TAG, "parse() rootTagName=" + rootTagName + ",tagName=" + tagName);
                    }
                    if (tagName.equals(rootTagName)) {
                        this.mRootElement.parse(parser);
                    }
                }
                type = parser.next();
            }
            this.mIsParseFinished = true;
            if (HWFLOW) {
                Slog.i(TAG, "parse() done");
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                Slog.e(TAG, "parse() error! close FileInputStream failed");
            }
            return true;
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "parse() error! " + e2);
            try {
                inputStream.close();
            } catch (IOException e3) {
                Slog.e(TAG, "parse() error! close FileInputStream failed");
            }
        } catch (IOException e4) {
            Slog.e(TAG, "parse() error! " + e4);
            try {
                inputStream.close();
            } catch (IOException e5) {
                Slog.e(TAG, "parse() error! close FileInputStream failed");
            }
        } catch (Throwable th) {
            try {
                inputStream.close();
            } catch (IOException e6) {
                Slog.e(TAG, "parse() error! close FileInputStream failed");
            }
            throw th;
        }
        return false;
    }

    private boolean inSpecifiedDepth(int specifiedDepth, int type, int currentDepth) {
        if (type == 1) {
            return false;
        }
        return type != 3 || currentDepth > specifiedDepth;
    }

    public boolean check() {
        if (HWFLOW) {
            Slog.i(TAG, "check()");
        }
        if (this.mIsParseFinished && this.mRootElement.check()) {
            return true;
        }
        return false;
    }
}
