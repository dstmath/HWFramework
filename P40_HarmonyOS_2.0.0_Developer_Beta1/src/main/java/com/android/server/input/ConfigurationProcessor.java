package com.android.server.input;

import android.text.TextUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.Settings;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

class ConfigurationProcessor {
    private static final String TAG = "ConfigurationProcessor";

    ConfigurationProcessor() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003b, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003e, code lost:
        throw r3;
     */
    static List<String> processExcludedDeviceNames(InputStream xml) throws Exception {
        List<String> names = new ArrayList<>();
        InputStreamReader confReader = new InputStreamReader(xml);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(confReader);
        XmlUtils.beginDocument(parser, "devices");
        while (true) {
            XmlUtils.nextElement(parser);
            if (!"device".equals(parser.getName())) {
                $closeResource(null, confReader);
                return names;
            }
            String name = parser.getAttributeValue(null, Settings.ATTR_NAME);
            if (name != null) {
                names.add(name);
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006a, code lost:
        throw r3;
     */
    @VisibleForTesting
    static List<Pair<String, String>> processInputPortAssociations(InputStream xml) throws Exception {
        List<Pair<String, String>> associations = new ArrayList<>();
        InputStreamReader confReader = new InputStreamReader(xml);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(confReader);
        XmlUtils.beginDocument(parser, "ports");
        while (true) {
            XmlUtils.nextElement(parser);
            if (!"port".equals(parser.getName())) {
                $closeResource(null, confReader);
                return associations;
            }
            String inputPort = parser.getAttributeValue(null, "input");
            String displayPort = parser.getAttributeValue(null, "display");
            if (TextUtils.isEmpty(inputPort) || TextUtils.isEmpty(displayPort)) {
                Slog.wtf(TAG, "Ignoring incomplete entry");
            } else {
                try {
                    Integer.parseUnsignedInt(displayPort);
                    associations.add(new Pair<>(inputPort, displayPort));
                } catch (NumberFormatException e) {
                    Slog.wtf(TAG, "Display port should be an integer");
                }
            }
        }
    }
}
