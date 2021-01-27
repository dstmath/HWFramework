package com.huawei.server;

import android.content.pm.FeatureInfo;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustSystemConfigImpl extends HwCustSystemConfig {
    private static final int CUST_TYPE_CONFIG = 0;
    private static final String FEATURE_FILE_PATH = "/xml/system.feature.replace_all.xml";
    private static final boolean IS_FEATURE_OVERLAY = SystemProperties.getBoolean("ro.system.feature.Overlay", (boolean) IS_FEATURE_OVERLAY);
    private static final String TAG = "HwCustSystemConfigImpl";

    public void readOverlayFeaturesAndClearOld(ArrayMap<String, FeatureInfo> oriAvailableFeatures, ArraySet<String> oriUnavailableFeatures) {
        if (IS_FEATURE_OVERLAY) {
            Slog.i(TAG, "start read overlay feature.");
            ArrayMap<String, FeatureInfo> overlayAvailableFeatures = new ArrayMap<>();
            try {
                File overlayFeatureFile = HwCfgFilePolicy.getCfgFile(FEATURE_FILE_PATH, (int) CUST_TYPE_CONFIG);
                if (overlayFeatureFile != null && overlayFeatureFile.isFile()) {
                    if (overlayFeatureFile.canRead()) {
                        if (readPermissionsFromXml(overlayFeatureFile, overlayAvailableFeatures)) {
                            oriAvailableFeatures.clear();
                            oriUnavailableFeatures.clear();
                            oriAvailableFeatures.putAll((ArrayMap<? extends String, ? extends FeatureInfo>) overlayAvailableFeatures);
                            Slog.i(TAG, "overlay feature take effect.");
                            return;
                        }
                        return;
                    }
                }
                Slog.e(TAG, "overlay feature file don't exist or can not read.");
            } catch (NoClassDefFoundError e) {
                Slog.e(TAG, "readOverlayFeaturesAndClearOld NoClassDefFoundError");
            } catch (Exception e2) {
                Slog.e(TAG, "readOverlayFeaturesAndClearOld got exception");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        throw r4;
     */
    private boolean readPermissionsFromXml(File permFile, ArrayMap<String, FeatureInfo> availableFeatures) {
        try {
            FileInputStream fileInputStream = new FileInputStream(permFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fileInputStream, "utf-8");
            validateXmlFile(parser, permFile);
            readFeatureTag(permFile, parser, availableFeatures);
            fileInputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "Couldn't find or open permissions overlay features file.");
            return IS_FEATURE_OVERLAY;
        } catch (XmlPullParserException e2) {
            Slog.w(TAG, "Got XmlPullParserException parsing permissions.");
            return IS_FEATURE_OVERLAY;
        } catch (IOException e3) {
            Slog.w(TAG, "Got IOException parsing permissions.");
            return IS_FEATURE_OVERLAY;
        }
    }

    private void readFeatureTag(File permFile, XmlPullParser parser, ArrayMap<String, FeatureInfo> availableFeatures) throws XmlPullParserException, IOException {
        while (true) {
            XmlUtils.nextElement(parser);
            if (parser.getEventType() != 1) {
                String name = parser.getName();
                if (name == null) {
                    XmlUtils.skipCurrentTag(parser);
                } else if ("feature".equals(name)) {
                    addFeatureToList(parser, permFile, availableFeatures);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Tag " + name + " is not feature in overlay feature xml");
                    XmlUtils.skipCurrentTag(parser);
                }
            } else {
                return;
            }
        }
    }

    private void addFeatureToList(XmlPullParser parser, File permFile, ArrayMap<String, FeatureInfo> availableFeatures) {
        String featureName = parser.getAttributeValue(null, "name");
        int featureVersion = XmlUtils.readIntAttribute(parser, "version", (int) CUST_TYPE_CONFIG);
        if (featureName == null) {
            Slog.w(TAG, "featureName is null");
        } else {
            addFeature(featureName, featureVersion, availableFeatures);
        }
    }

    private void validateXmlFile(XmlPullParser parser, File permFile) throws XmlPullParserException, IOException {
        int type = parser.next();
        while (type != 2 && type != 1) {
            type = parser.next();
        }
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (!"permissions".equals(parser.getName()) && !"config".equals(parser.getName())) {
            throw new XmlPullParserException("Unexpected start tag in overlay feature xml");
        }
    }

    private void addFeature(String name, int version, ArrayMap<String, FeatureInfo> availableFeatures) {
        FeatureInfo featureInfo = availableFeatures.get(name);
        if (featureInfo == null) {
            FeatureInfo featureInfo2 = new FeatureInfo();
            featureInfo2.name = name;
            featureInfo2.version = version;
            availableFeatures.put(name, featureInfo2);
            return;
        }
        featureInfo.version = Math.max(featureInfo.version, version);
    }
}
