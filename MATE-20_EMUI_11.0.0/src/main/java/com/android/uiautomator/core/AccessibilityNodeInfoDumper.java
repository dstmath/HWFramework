package com.android.uiautomator.core;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TableLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;

public class AccessibilityNodeInfoDumper {
    private static final String LOGTAG = AccessibilityNodeInfoDumper.class.getSimpleName();
    private static final String[] NAF_EXCLUDED_CLASSES = {GridView.class.getName(), GridLayout.class.getName(), ListView.class.getName(), TableLayout.class.getName()};

    public static void dumpWindowToFile(AccessibilityNodeInfo root, int rotation, int width, int height) {
        File baseDir = new File(Environment.getDataDirectory(), "local");
        if (!baseDir.exists()) {
            baseDir.mkdir();
            baseDir.setExecutable(true, false);
            baseDir.setWritable(true, false);
            baseDir.setReadable(true, false);
        }
        dumpWindowToFile(root, new File(new File(Environment.getDataDirectory(), "local"), "window_dump.xml"), rotation, width, height);
    }

    public static void dumpWindowToFile(AccessibilityNodeInfo root, File dumpFile, int rotation, int width, int height) {
        if (root != null) {
            long startTime = SystemClock.uptimeMillis();
            try {
                FileWriter writer = new FileWriter(dumpFile);
                XmlSerializer serializer = Xml.newSerializer();
                StringWriter stringWriter = new StringWriter();
                serializer.setOutput(stringWriter);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "hierarchy");
                serializer.attribute("", "rotation", Integer.toString(rotation));
                dumpNodeRec(root, serializer, 0, width, height);
                serializer.endTag("", "hierarchy");
                serializer.endDocument();
                writer.write(stringWriter.toString());
                writer.close();
            } catch (IOException e) {
                Log.e(LOGTAG, "failed to dump window to file", e);
            }
            long endTime = SystemClock.uptimeMillis();
            String str = LOGTAG;
            Log.w(str, "Fetch time: " + (endTime - startTime) + "ms");
        }
    }

    private static void dumpNodeRec(AccessibilityNodeInfo node, XmlSerializer serializer, int index, int width, int height) throws IOException {
        serializer.startTag("", "node");
        if (!nafExcludedClass(node) && !nafCheck(node)) {
            serializer.attribute("", "NAF", Boolean.toString(true));
        }
        serializer.attribute("", "index", Integer.toString(index));
        serializer.attribute("", "text", safeCharSeqToString(node.getText()));
        serializer.attribute("", "resource-id", safeCharSeqToString(node.getViewIdResourceName()));
        serializer.attribute("", "class", safeCharSeqToString(node.getClassName()));
        serializer.attribute("", "package", safeCharSeqToString(node.getPackageName()));
        serializer.attribute("", "content-desc", safeCharSeqToString(node.getContentDescription()));
        serializer.attribute("", "checkable", Boolean.toString(node.isCheckable()));
        serializer.attribute("", "checked", Boolean.toString(node.isChecked()));
        serializer.attribute("", "clickable", Boolean.toString(node.isClickable()));
        serializer.attribute("", "enabled", Boolean.toString(node.isEnabled()));
        serializer.attribute("", "focusable", Boolean.toString(node.isFocusable()));
        serializer.attribute("", "focused", Boolean.toString(node.isFocused()));
        serializer.attribute("", "scrollable", Boolean.toString(node.isScrollable()));
        serializer.attribute("", "long-clickable", Boolean.toString(node.isLongClickable()));
        serializer.attribute("", "password", Boolean.toString(node.isPassword()));
        serializer.attribute("", "selected", Boolean.toString(node.isSelected()));
        serializer.attribute("", "bounds", AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, width, height).toShortString());
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) {
                Log.i(LOGTAG, String.format("Null child %d/%d, parent: %s", Integer.valueOf(i), Integer.valueOf(count), node.toString()));
            } else if (child.isVisibleToUser()) {
                dumpNodeRec(child, serializer, i, width, height);
                child.recycle();
            } else {
                Log.i(LOGTAG, String.format("Skipping invisible child: %s", child.toString()));
            }
        }
        serializer.endTag("", "node");
    }

    private static boolean nafExcludedClass(AccessibilityNodeInfo node) {
        String className = safeCharSeqToString(node.getClassName());
        for (String excludedClassName : NAF_EXCLUDED_CLASSES) {
            if (className.endsWith(excludedClassName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nafCheck(AccessibilityNodeInfo node) {
        if (!(node.isClickable() && node.isEnabled() && safeCharSeqToString(node.getContentDescription()).isEmpty() && safeCharSeqToString(node.getText()).isEmpty())) {
            return true;
        }
        return childNafCheck(node);
    }

    private static boolean childNafCheck(AccessibilityNodeInfo node) {
        int childCount = node.getChildCount();
        for (int x = 0; x < childCount; x++) {
            AccessibilityNodeInfo childNode = node.getChild(x);
            if (!safeCharSeqToString(childNode.getContentDescription()).isEmpty() || !safeCharSeqToString(childNode.getText()).isEmpty() || childNafCheck(childNode)) {
                return true;
            }
        }
        return false;
    }

    private static String safeCharSeqToString(CharSequence cs) {
        if (cs == null) {
            return "";
        }
        return stripInvalidXMLChars(cs);
    }

    private static String stripInvalidXMLChars(CharSequence cs) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < cs.length(); i++) {
            char ch = cs.charAt(i);
            if ((ch < 1 || ch > '\b') && ((ch < 11 || ch > '\f') && ((ch < 14 || ch > 31) && ((ch < 127 || ch > 132) && ((ch < 134 || ch > 159) && ((ch < 64976 || ch > 64991) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && ((ch < 65534 || ch > 65535) && (ch < 65534 || ch > 65535)))))))))))))))))))))) {
                ret.append(ch);
            } else {
                ret.append(".");
            }
        }
        return ret.toString();
    }
}
