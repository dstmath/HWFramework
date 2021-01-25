package com.android.server.pm;

import android.text.TextUtils;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class ShareTargetInfo {
    private static final String ATTR_HOST = "host";
    private static final String ATTR_MIME_TYPE = "mimeType";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";
    private static final String ATTR_PATH_PATTERN = "pathPattern";
    private static final String ATTR_PATH_PREFIX = "pathPrefix";
    private static final String ATTR_PORT = "port";
    private static final String ATTR_SCHEME = "scheme";
    private static final String ATTR_TARGET_CLASS = "targetClass";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_DATA = "data";
    private static final String TAG_SHARE_TARGET = "share-target";
    final String[] mCategories;
    final String mTargetClass;
    final TargetData[] mTargetData;

    /* access modifiers changed from: package-private */
    public static class TargetData {
        final String mHost;
        final String mMimeType;
        final String mPath;
        final String mPathPattern;
        final String mPathPrefix;
        final String mPort;
        final String mScheme;

        TargetData(String scheme, String host, String port, String path, String pathPattern, String pathPrefix, String mimeType) {
            this.mScheme = scheme;
            this.mHost = host;
            this.mPort = port;
            this.mPath = path;
            this.mPathPattern = pathPattern;
            this.mPathPrefix = pathPrefix;
            this.mMimeType = mimeType;
        }

        public void toStringInner(StringBuilder strBuilder) {
            if (!TextUtils.isEmpty(this.mScheme)) {
                strBuilder.append(" scheme=");
                strBuilder.append(this.mScheme);
            }
            if (!TextUtils.isEmpty(this.mHost)) {
                strBuilder.append(" host=");
                strBuilder.append(this.mHost);
            }
            if (!TextUtils.isEmpty(this.mPort)) {
                strBuilder.append(" port=");
                strBuilder.append(this.mPort);
            }
            if (!TextUtils.isEmpty(this.mPath)) {
                strBuilder.append(" path=");
                strBuilder.append(this.mPath);
            }
            if (!TextUtils.isEmpty(this.mPathPattern)) {
                strBuilder.append(" pathPattern=");
                strBuilder.append(this.mPathPattern);
            }
            if (!TextUtils.isEmpty(this.mPathPrefix)) {
                strBuilder.append(" pathPrefix=");
                strBuilder.append(this.mPathPrefix);
            }
            if (!TextUtils.isEmpty(this.mMimeType)) {
                strBuilder.append(" mimeType=");
                strBuilder.append(this.mMimeType);
            }
        }

        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            toStringInner(strBuilder);
            return strBuilder.toString();
        }
    }

    ShareTargetInfo(TargetData[] data, String targetClass, String[] categories) {
        this.mTargetData = data;
        this.mTargetClass = targetClass;
        this.mCategories = categories;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("targetClass=");
        strBuilder.append(this.mTargetClass);
        for (int i = 0; i < this.mTargetData.length; i++) {
            strBuilder.append(" data={");
            this.mTargetData[i].toStringInner(strBuilder);
            strBuilder.append("}");
        }
        for (int i2 = 0; i2 < this.mCategories.length; i2++) {
            strBuilder.append(" category=");
            strBuilder.append(this.mCategories[i2]);
        }
        return strBuilder.toString();
    }

    /* access modifiers changed from: package-private */
    public void saveToXml(XmlSerializer out) throws IOException {
        out.startTag(null, TAG_SHARE_TARGET);
        ShortcutService.writeAttr(out, ATTR_TARGET_CLASS, this.mTargetClass);
        for (int i = 0; i < this.mTargetData.length; i++) {
            out.startTag(null, "data");
            ShortcutService.writeAttr(out, ATTR_SCHEME, this.mTargetData[i].mScheme);
            ShortcutService.writeAttr(out, "host", this.mTargetData[i].mHost);
            ShortcutService.writeAttr(out, ATTR_PORT, this.mTargetData[i].mPort);
            ShortcutService.writeAttr(out, ATTR_PATH, this.mTargetData[i].mPath);
            ShortcutService.writeAttr(out, ATTR_PATH_PATTERN, this.mTargetData[i].mPathPattern);
            ShortcutService.writeAttr(out, ATTR_PATH_PREFIX, this.mTargetData[i].mPathPrefix);
            ShortcutService.writeAttr(out, ATTR_MIME_TYPE, this.mTargetData[i].mMimeType);
            out.endTag(null, "data");
        }
        for (int i2 = 0; i2 < this.mCategories.length; i2++) {
            out.startTag(null, TAG_CATEGORY);
            ShortcutService.writeAttr(out, "name", this.mCategories[i2]);
            out.endTag(null, TAG_CATEGORY);
        }
        out.endTag(null, TAG_SHARE_TARGET);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0055  */
    static ShareTargetInfo loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean z;
        String targetClass = ShortcutService.parseStringAttribute(parser, ATTR_TARGET_CLASS);
        ArrayList<TargetData> targetData = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();
        while (true) {
            int type = parser.next();
            if (type != 1) {
                if (type != 2) {
                    if (type == 3 && parser.getName().equals(TAG_SHARE_TARGET)) {
                        break;
                    }
                } else {
                    String name = parser.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != 3076010) {
                        if (hashCode == 50511102 && name.equals(TAG_CATEGORY)) {
                            z = true;
                            if (!z) {
                                targetData.add(parseTargetData(parser));
                            } else if (z) {
                                categories.add(ShortcutService.parseStringAttribute(parser, "name"));
                            }
                        }
                    } else if (name.equals("data")) {
                        z = false;
                        if (!z) {
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                }
            } else {
                break;
            }
        }
        if (targetData.isEmpty() || targetClass == null || categories.isEmpty()) {
            return null;
        }
        return new ShareTargetInfo((TargetData[]) targetData.toArray(new TargetData[targetData.size()]), targetClass, (String[]) categories.toArray(new String[categories.size()]));
    }

    private static TargetData parseTargetData(XmlPullParser parser) {
        return new TargetData(ShortcutService.parseStringAttribute(parser, ATTR_SCHEME), ShortcutService.parseStringAttribute(parser, "host"), ShortcutService.parseStringAttribute(parser, ATTR_PORT), ShortcutService.parseStringAttribute(parser, ATTR_PATH), ShortcutService.parseStringAttribute(parser, ATTR_PATH_PATTERN), ShortcutService.parseStringAttribute(parser, ATTR_PATH_PREFIX), ShortcutService.parseStringAttribute(parser, ATTR_MIME_TYPE));
    }
}
