package android.support.v4.content.res;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.compat.R;
import android.support.v4.provider.FontRequest;
import android.util.Base64;
import android.util.Xml;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@RestrictTo({Scope.LIBRARY_GROUP})
public class FontResourcesParserCompat {
    private static final int DEFAULT_TIMEOUT_MILLIS = 500;
    public static final int FETCH_STRATEGY_ASYNC = 1;
    public static final int FETCH_STRATEGY_BLOCKING = 0;
    public static final int INFINITE_TIMEOUT_VALUE = -1;
    private static final int ITALIC = 1;
    private static final int NORMAL_WEIGHT = 400;

    public interface FamilyResourceEntry {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FetchStrategy {
    }

    public static final class FontFamilyFilesResourceEntry implements FamilyResourceEntry {
        @NonNull
        private final FontFileResourceEntry[] mEntries;

        public FontFamilyFilesResourceEntry(@NonNull FontFileResourceEntry[] entries) {
            this.mEntries = entries;
        }

        @NonNull
        public FontFileResourceEntry[] getEntries() {
            return this.mEntries;
        }
    }

    public static final class FontFileResourceEntry {
        private boolean mItalic;
        private int mResourceId;
        private int mWeight;

        public FontFileResourceEntry(int weight, boolean italic, int resourceId) {
            this.mWeight = weight;
            this.mItalic = italic;
            this.mResourceId = resourceId;
        }

        public int getWeight() {
            return this.mWeight;
        }

        public boolean isItalic() {
            return this.mItalic;
        }

        public int getResourceId() {
            return this.mResourceId;
        }
    }

    public static final class ProviderResourceEntry implements FamilyResourceEntry {
        @NonNull
        private final FontRequest mRequest;
        private final int mStrategy;
        private final int mTimeoutMs;

        public ProviderResourceEntry(@NonNull FontRequest request, int strategy, int timeoutMs) {
            this.mRequest = request;
            this.mStrategy = strategy;
            this.mTimeoutMs = timeoutMs;
        }

        @NonNull
        public FontRequest getRequest() {
            return this.mRequest;
        }

        public int getFetchStrategy() {
            return this.mStrategy;
        }

        public int getTimeout() {
            return this.mTimeoutMs;
        }
    }

    @Nullable
    public static FamilyResourceEntry parse(XmlPullParser parser, Resources resources) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            return readFamilies(parser, resources);
        }
        throw new XmlPullParserException("No start tag found");
    }

    @Nullable
    private static FamilyResourceEntry readFamilies(XmlPullParser parser, Resources resources) throws XmlPullParserException, IOException {
        parser.require(2, null, "font-family");
        if (parser.getName().equals("font-family")) {
            return readFamily(parser, resources);
        }
        skip(parser);
        return null;
    }

    @Nullable
    private static FamilyResourceEntry readFamily(XmlPullParser parser, Resources resources) throws XmlPullParserException, IOException {
        TypedArray array = resources.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.FontFamily);
        String authority = array.getString(R.styleable.FontFamily_fontProviderAuthority);
        String providerPackage = array.getString(R.styleable.FontFamily_fontProviderPackage);
        String query = array.getString(R.styleable.FontFamily_fontProviderQuery);
        int certsId = array.getResourceId(R.styleable.FontFamily_fontProviderCerts, 0);
        int strategy = array.getInteger(R.styleable.FontFamily_fontProviderFetchStrategy, 1);
        int timeoutMs = array.getInteger(R.styleable.FontFamily_fontProviderFetchTimeout, DEFAULT_TIMEOUT_MILLIS);
        array.recycle();
        if (authority == null || providerPackage == null || query == null) {
            List<FontFileResourceEntry> fonts = new ArrayList();
            while (parser.next() != 3) {
                if (parser.getEventType() == 2) {
                    if (parser.getName().equals("font")) {
                        fonts.add(readFont(parser, resources));
                    } else {
                        skip(parser);
                    }
                }
            }
            if (fonts.isEmpty()) {
                return null;
            }
            return new FontFamilyFilesResourceEntry((FontFileResourceEntry[]) fonts.toArray(new FontFileResourceEntry[fonts.size()]));
        }
        while (parser.next() != 3) {
            skip(parser);
        }
        return new ProviderResourceEntry(new FontRequest(authority, providerPackage, query, readCerts(resources, certsId)), strategy, timeoutMs);
    }

    public static List<List<byte[]>> readCerts(Resources resources, @ArrayRes int certsId) {
        List<List<byte[]>> certs = null;
        if (certsId != 0) {
            TypedArray typedArray = resources.obtainTypedArray(certsId);
            if (typedArray.length() > 0) {
                certs = new ArrayList();
                if (typedArray.getResourceId(0, 0) != 0) {
                    for (int i = 0; i < typedArray.length(); i++) {
                        certs.add(toByteArrayList(resources.getStringArray(typedArray.getResourceId(i, 0))));
                    }
                } else {
                    certs.add(toByteArrayList(resources.getStringArray(certsId)));
                }
            }
        }
        return certs != null ? certs : Collections.emptyList();
    }

    private static List<byte[]> toByteArrayList(String[] stringArray) {
        List<byte[]> result = new ArrayList();
        for (String item : stringArray) {
            result.add(Base64.decode(item, 0));
        }
        return result;
    }

    private static FontFileResourceEntry readFont(XmlPullParser parser, Resources resources) throws XmlPullParserException, IOException {
        TypedArray array = resources.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.FontFamilyFont);
        int weight = array.getInt(R.styleable.FontFamilyFont_fontWeight, NORMAL_WEIGHT);
        boolean isItalic = 1 == array.getInt(R.styleable.FontFamilyFont_fontStyle, 0);
        int resourceId = array.getResourceId(R.styleable.FontFamilyFont_font, 0);
        array.recycle();
        while (parser.next() != 3) {
            skip(parser);
        }
        return new FontFileResourceEntry(weight, isItalic, resourceId);
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        int depth = 1;
        while (depth > 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }
}
