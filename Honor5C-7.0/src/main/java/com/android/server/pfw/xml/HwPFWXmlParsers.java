package com.android.server.pfw.xml;

import android.content.Context;
import com.android.internal.util.Preconditions;
import com.android.server.pfw.log.HwPFWLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Element;

public class HwPFWXmlParsers {
    private static final String TAG = "HwPFWXmlParsers";

    public static Element diskXmlRootElement(String diskFile) throws HwPFWXmlException {
        return xmlRootElement(diskInputStream(diskFile));
    }

    public static Element assetXmlRootElement(Context context, String assetFile) throws HwPFWXmlException {
        return xmlRootElement(assetInputStream(context, assetFile));
    }

    private static Element xmlRootElement(InputStream is) throws HwPFWXmlException {
        Element element;
        try {
            Preconditions.checkNotNull(is, "InputStream parameter can't be null");
            element = (Element) Preconditions.checkNotNull(new HwPFWDOMXmlParser(is).rootElement(), "result element can't be null");
            return element;
        } catch (NullPointerException ex) {
            element = TAG;
            HwPFWLogger.e(element, "xmlRootElement NullPointerException:" + ex.getMessage());
            throw new HwPFWXmlException("xmlRootElement parse failed");
        } finally {
            closeStream(is);
        }
    }

    public static void closeStream(InputStream is) {
        if (is == null) {
            HwPFWLogger.d(TAG, "close inputsteam is null!");
            return;
        }
        try {
            is.close();
        } catch (IOException e) {
            HwPFWLogger.e(TAG, "close catch IOException");
        }
    }

    private static InputStream diskInputStream(String diskPath) {
        try {
            File file = new File(diskPath);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            HwPFWLogger.e(TAG, "diskInputStream file not found:" + diskPath);
        }
        return null;
    }

    private static InputStream assetInputStream(Context context, String assetFile) {
        try {
            Preconditions.checkNotNull(context, "context can't be null");
            return context.getAssets().open(assetFile);
        } catch (IllegalArgumentException e) {
            HwPFWLogger.e(TAG, "assetInputStream catch IllegalArgumentException:" + assetFile);
            return null;
        } catch (IOException e2) {
            HwPFWLogger.e(TAG, "assetInputStream catch IO exception:" + assetFile);
            return null;
        }
    }
}
