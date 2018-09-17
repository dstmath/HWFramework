package com.android.server.usage;

import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UsageStatsXml {
    static final String CHECKED_IN_SUFFIX = "-c";
    private static final int CURRENT_VERSION = 1;
    private static final String TAG = "UsageStatsXml";
    private static final String USAGESTATS_TAG = "usagestats";
    private static final String VERSION_ATTR = "version";

    public static long parseBeginTime(AtomicFile file) throws IOException {
        return parseBeginTime(file.getBaseFile());
    }

    public static long parseBeginTime(File file) throws IOException {
        String name = file.getName();
        while (name.endsWith(CHECKED_IN_SUFFIX)) {
            name = name.substring(0, name.length() - CHECKED_IN_SUFFIX.length());
        }
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    public static void read(AtomicFile file, IntervalStats statsOut) throws IOException {
        InputStream in;
        try {
            in = file.openRead();
            statsOut.beginTime = parseBeginTime(file);
            read(in, statsOut);
            statsOut.lastTimeSaved = file.getLastModifiedTime();
            try {
                in.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "UsageStats Xml", e2);
            throw e2;
        } catch (Throwable th) {
            try {
                in.close();
            } catch (IOException e3) {
            }
        }
    }

    public static void write(AtomicFile file, IntervalStats stats) throws IOException {
        FileOutputStream fos = file.startWrite();
        try {
            write((OutputStream) fos, stats);
            file.finishWrite(fos);
            fos = null;
        } finally {
            file.failWrite(fos);
        }
    }

    static void read(InputStream in, IntervalStats statsOut) throws IOException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(in, "utf-8");
            XmlUtils.beginDocument(parser, USAGESTATS_TAG);
            String versionStr = parser.getAttributeValue(null, VERSION_ATTR);
            switch (Integer.parseInt(versionStr)) {
                case 1:
                    UsageStatsXmlV1.read(parser, statsOut);
                    return;
                default:
                    Slog.e(TAG, "Unrecognized version " + versionStr);
                    throw new IOException("Unrecognized version " + versionStr);
            }
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Bad version");
            throw new IOException(e);
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "Failed to parse Xml", e2);
            throw new IOException(e2);
        }
        Slog.e(TAG, "Bad version");
        throw new IOException(e);
    }

    static void write(OutputStream out, IntervalStats stats) throws IOException {
        FastXmlSerializer xml = new FastXmlSerializer();
        xml.setOutput(out, "utf-8");
        xml.startDocument("utf-8", Boolean.valueOf(true));
        xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        xml.startTag(null, USAGESTATS_TAG);
        xml.attribute(null, VERSION_ATTR, Integer.toString(1));
        UsageStatsXmlV1.write(xml, stats);
        xml.endTag(null, USAGESTATS_TAG);
        xml.endDocument();
    }
}
