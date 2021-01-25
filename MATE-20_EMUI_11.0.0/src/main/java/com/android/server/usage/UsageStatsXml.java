package com.android.server.usage;

import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
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

    public static void read(InputStream in, IntervalStats statsOut, boolean dropEvent) throws IOException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(in, "utf-8");
            XmlUtils.beginDocument(parser, USAGESTATS_TAG);
            String versionStr = parser.getAttributeValue(null, VERSION_ATTR);
            try {
                if (Integer.parseInt(versionStr) == 1) {
                    UsageStatsXmlV1.read(parser, statsOut, dropEvent);
                    return;
                }
                Slog.e(TAG, "Unrecognized version " + versionStr);
                throw new IOException("Unrecognized version " + versionStr);
            } catch (NumberFormatException e) {
                Slog.e(TAG, "Bad version");
                throw new IOException(e);
            }
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "Failed to parse Xml", e2);
            throw new IOException(e2);
        }
    }

    public static void write(OutputStream out, IntervalStats stats) throws IOException {
        FastXmlSerializer xml = new FastXmlSerializer();
        xml.setOutput(out, "utf-8");
        xml.startDocument("utf-8", true);
        xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        xml.startTag((String) null, USAGESTATS_TAG);
        xml.attribute((String) null, VERSION_ATTR, Integer.toString(1));
        UsageStatsXmlV1.write(xml, stats);
        xml.endTag((String) null, USAGESTATS_TAG);
        xml.endDocument();
    }
}
