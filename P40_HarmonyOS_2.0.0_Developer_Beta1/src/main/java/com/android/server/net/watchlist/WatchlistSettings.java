package com.android.server.net.watchlist;

import android.os.Environment;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.HexDump;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class WatchlistSettings {
    private static final String FILE_NAME = "watchlist_settings.xml";
    private static final int SECRET_KEY_LENGTH = 48;
    private static final String TAG = "WatchlistSettings";
    private static final WatchlistSettings sInstance = new WatchlistSettings();
    private byte[] mPrivacySecretKey;
    private final AtomicFile mXmlFile;

    public static WatchlistSettings getInstance() {
        return sInstance;
    }

    private WatchlistSettings() {
        this(getSystemWatchlistFile());
    }

    static File getSystemWatchlistFile() {
        return new File(Environment.getDataSystemDirectory(), FILE_NAME);
    }

    @VisibleForTesting
    protected WatchlistSettings(File xmlFile) {
        this.mPrivacySecretKey = null;
        this.mXmlFile = new AtomicFile(xmlFile, "net-watchlist");
        reloadSettings();
        if (this.mPrivacySecretKey == null) {
            this.mPrivacySecretKey = generatePrivacySecretKey();
            saveSettings();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0051, code lost:
        if (r1 != null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0058, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005b, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        android.util.Slog.e(com.android.server.net.watchlist.WatchlistSettings.TAG, "Failed parsing xml", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c A[ExcHandler: IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException (r1v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:13:0x004a] */
    private void reloadSettings() {
        if (this.mXmlFile.exists()) {
            FileInputStream stream = this.mXmlFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            XmlUtils.beginDocument(parser, "network-watchlist-settings");
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals("secret-key")) {
                    this.mPrivacySecretKey = parseSecretKey(parser);
                }
            }
            Slog.i(TAG, "Reload watchlist settings done");
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException | IllegalStateException | IndexOutOfBoundsException | NullPointerException | NumberFormatException | XmlPullParserException e) {
                }
            }
        }
    }

    private byte[] parseSecretKey(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "secret-key");
        byte[] key = HexDump.hexStringToByteArray(parser.nextText());
        parser.require(3, null, "secret-key");
        if (key != null && key.length == 48) {
            return key;
        }
        Log.e(TAG, "Unable to parse secret key");
        return null;
    }

    /* access modifiers changed from: package-private */
    public synchronized byte[] getPrivacySecretKey() {
        byte[] key;
        key = new byte[48];
        System.arraycopy(this.mPrivacySecretKey, 0, key, 0, 48);
        return key;
    }

    private byte[] generatePrivacySecretKey() {
        byte[] key = new byte[48];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private void saveSettings() {
        try {
            FileOutputStream stream = this.mXmlFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.startTag(null, "network-watchlist-settings");
                out.startTag(null, "secret-key");
                out.text(HexDump.toHexString(this.mPrivacySecretKey));
                out.endTag(null, "secret-key");
                out.endTag(null, "network-watchlist-settings");
                out.endDocument();
                this.mXmlFile.finishWrite(stream);
            } catch (IOException e) {
                Log.w(TAG, "Failed to write display settings, restoring backup.", e);
                this.mXmlFile.failWrite(stream);
            }
        } catch (IOException e2) {
            Log.w(TAG, "Failed to write display settings: " + e2);
        }
    }
}
