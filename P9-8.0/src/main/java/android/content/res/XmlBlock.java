package android.content.res;

import android.app.Instrumentation;
import android.net.ProxyInfo;
import android.util.TypedValue;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.xmlpull.v1.XmlPullParserException;

final class XmlBlock {
    private static final boolean DEBUG = false;
    private final AssetManager mAssets;
    private final long mNative;
    private boolean mOpen;
    private int mOpenCount;
    final StringBlock mStrings;

    final class Parser implements XmlResourceParser {
        private final XmlBlock mBlock;
        private boolean mDecNextDepth = false;
        private int mDepth = 0;
        private int mEventType = 0;
        long mParseState;
        private boolean mStarted = false;

        Parser(long parseState, XmlBlock block) {
            this.mParseState = parseState;
            this.mBlock = block;
            block.mOpenCount = block.mOpenCount + 1;
        }

        public void setFeature(String name, boolean state) throws XmlPullParserException {
            if (!"http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(name) || !state) {
                if (!"http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes".equals(name) || !state) {
                    throw new XmlPullParserException("Unsupported feature: " + name);
                }
            }
        }

        public boolean getFeature(String name) {
            if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(name) || "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes".equals(name)) {
                return true;
            }
            return false;
        }

        public void setProperty(String name, Object value) throws XmlPullParserException {
            throw new XmlPullParserException("setProperty() not supported");
        }

        public Object getProperty(String name) {
            return null;
        }

        public void setInput(Reader in) throws XmlPullParserException {
            throw new XmlPullParserException("setInput() not supported");
        }

        public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
            throw new XmlPullParserException("setInput() not supported");
        }

        public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
            throw new XmlPullParserException("defineEntityReplacementText() not supported");
        }

        public String getNamespacePrefix(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("getNamespacePrefix() not supported");
        }

        public String getInputEncoding() {
            return null;
        }

        public String getNamespace(String prefix) {
            throw new RuntimeException("getNamespace() not supported");
        }

        public int getNamespaceCount(int depth) throws XmlPullParserException {
            throw new XmlPullParserException("getNamespaceCount() not supported");
        }

        public String getPositionDescription() {
            return "Binary XML file line #" + getLineNumber();
        }

        public String getNamespaceUri(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("getNamespaceUri() not supported");
        }

        public int getColumnNumber() {
            return -1;
        }

        public int getDepth() {
            return this.mDepth;
        }

        public String getText() {
            int id = XmlBlock.nativeGetText(this.mParseState);
            return id >= 0 ? XmlBlock.this.mStrings.get(id).toString() : null;
        }

        public int getLineNumber() {
            return XmlBlock.nativeGetLineNumber(this.mParseState);
        }

        public int getEventType() throws XmlPullParserException {
            return this.mEventType;
        }

        public boolean isWhitespace() throws XmlPullParserException {
            return false;
        }

        public String getPrefix() {
            throw new RuntimeException("getPrefix not supported");
        }

        public char[] getTextCharacters(int[] holderForStartAndLength) {
            String txt = getText();
            if (txt == null) {
                return null;
            }
            holderForStartAndLength[0] = 0;
            holderForStartAndLength[1] = txt.length();
            char[] chars = new char[txt.length()];
            txt.getChars(0, txt.length(), chars, 0);
            return chars;
        }

        public String getNamespace() {
            int id = XmlBlock.nativeGetNamespace(this.mParseState);
            return id >= 0 ? XmlBlock.this.mStrings.get(id).toString() : ProxyInfo.LOCAL_EXCL_LIST;
        }

        public String getName() {
            int id = XmlBlock.nativeGetName(this.mParseState);
            return id >= 0 ? XmlBlock.this.mStrings.get(id).toString() : null;
        }

        public String getAttributeNamespace(int index) {
            int id = XmlBlock.nativeGetAttributeNamespace(this.mParseState, index);
            if (id >= 0) {
                return XmlBlock.this.mStrings.get(id).toString();
            }
            if (id == -1) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        public String getAttributeName(int index) {
            int id = XmlBlock.nativeGetAttributeName(this.mParseState, index);
            if (id >= 0) {
                return XmlBlock.this.mStrings.get(id).toString();
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        public String getAttributePrefix(int index) {
            throw new RuntimeException("getAttributePrefix not supported");
        }

        public boolean isEmptyElementTag() throws XmlPullParserException {
            return false;
        }

        public int getAttributeCount() {
            return this.mEventType == 2 ? XmlBlock.nativeGetAttributeCount(this.mParseState) : -1;
        }

        public String getAttributeValue(int index) {
            int id = XmlBlock.nativeGetAttributeStringValue(this.mParseState, index);
            if (id >= 0) {
                return XmlBlock.this.mStrings.get(id).toString();
            }
            int t = XmlBlock.nativeGetAttributeDataType(this.mParseState, index);
            if (t != 0) {
                return TypedValue.coerceToString(t, XmlBlock.nativeGetAttributeData(this.mParseState, index));
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        public String getAttributeType(int index) {
            return "CDATA";
        }

        public boolean isAttributeDefault(int index) {
            return false;
        }

        public int nextToken() throws XmlPullParserException, IOException {
            return next();
        }

        public String getAttributeValue(String namespace, String name) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, name);
            if (idx >= 0) {
                return getAttributeValue(idx);
            }
            return null;
        }

        public int next() throws XmlPullParserException, IOException {
            if (!this.mStarted) {
                this.mStarted = true;
                return 0;
            } else if (this.mParseState == 0) {
                return 1;
            } else {
                int ev = XmlBlock.nativeNext(this.mParseState);
                if (this.mDecNextDepth) {
                    this.mDepth--;
                    this.mDecNextDepth = false;
                }
                switch (ev) {
                    case 2:
                        this.mDepth++;
                        break;
                    case 3:
                        this.mDecNextDepth = true;
                        break;
                }
                this.mEventType = ev;
                if (ev == 1) {
                    close();
                }
                return ev;
            }
        }

        public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
            if (type != getEventType() || ((namespace != null && (namespace.equals(getNamespace()) ^ 1) != 0) || (name != null && (name.equals(getName()) ^ 1) != 0))) {
                throw new XmlPullParserException("expected " + TYPES[type] + getPositionDescription());
            }
        }

        public String nextText() throws XmlPullParserException, IOException {
            if (getEventType() != 2) {
                throw new XmlPullParserException(getPositionDescription() + ": parser must be on START_TAG to read next text", this, null);
            }
            int eventType = next();
            if (eventType == 4) {
                String result = getText();
                if (next() == 3) {
                    return result;
                }
                throw new XmlPullParserException(getPositionDescription() + ": event TEXT it must be immediately followed by END_TAG", this, null);
            } else if (eventType == 3) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                throw new XmlPullParserException(getPositionDescription() + ": parser must be on START_TAG or TEXT to read text", this, null);
            }
        }

        public int nextTag() throws XmlPullParserException, IOException {
            int eventType = next();
            if (eventType == 4 && isWhitespace()) {
                eventType = next();
            }
            if (eventType == 2 || eventType == 3) {
                return eventType;
            }
            throw new XmlPullParserException(getPositionDescription() + ": expected start or end tag", this, null);
        }

        public int getAttributeNameResource(int index) {
            return XmlBlock.nativeGetAttributeResource(this.mParseState, index);
        }

        public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeListValue(idx, options, defaultValue);
            }
            return defaultValue;
        }

        public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeBooleanValue(idx, defaultValue);
            }
            return defaultValue;
        }

        public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeResourceValue(idx, defaultValue);
            }
            return defaultValue;
        }

        public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeIntValue(idx, defaultValue);
            }
            return defaultValue;
        }

        public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeUnsignedIntValue(idx, defaultValue);
            }
            return defaultValue;
        }

        public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
            int idx = XmlBlock.nativeGetAttributeIndex(this.mParseState, namespace, attribute);
            if (idx >= 0) {
                return getAttributeFloatValue(idx, defaultValue);
            }
            return defaultValue;
        }

        public int getAttributeListValue(int idx, String[] options, int defaultValue) {
            int t = XmlBlock.nativeGetAttributeDataType(this.mParseState, idx);
            int v = XmlBlock.nativeGetAttributeData(this.mParseState, idx);
            if (t == 3) {
                return XmlUtils.convertValueToList(XmlBlock.this.mStrings.get(v), options, defaultValue);
            }
            return v;
        }

        public boolean getAttributeBooleanValue(int idx, boolean defaultValue) {
            boolean z = false;
            int t = XmlBlock.nativeGetAttributeDataType(this.mParseState, idx);
            if (t < 16 || t > 31) {
                return defaultValue;
            }
            if (XmlBlock.nativeGetAttributeData(this.mParseState, idx) != 0) {
                z = true;
            }
            return z;
        }

        public int getAttributeResourceValue(int idx, int defaultValue) {
            if (XmlBlock.nativeGetAttributeDataType(this.mParseState, idx) == 1) {
                return XmlBlock.nativeGetAttributeData(this.mParseState, idx);
            }
            return defaultValue;
        }

        public int getAttributeIntValue(int idx, int defaultValue) {
            int t = XmlBlock.nativeGetAttributeDataType(this.mParseState, idx);
            if (t < 16 || t > 31) {
                return defaultValue;
            }
            return XmlBlock.nativeGetAttributeData(this.mParseState, idx);
        }

        public int getAttributeUnsignedIntValue(int idx, int defaultValue) {
            int t = XmlBlock.nativeGetAttributeDataType(this.mParseState, idx);
            if (t < 16 || t > 31) {
                return defaultValue;
            }
            return XmlBlock.nativeGetAttributeData(this.mParseState, idx);
        }

        public float getAttributeFloatValue(int idx, float defaultValue) {
            if (XmlBlock.nativeGetAttributeDataType(this.mParseState, idx) == 4) {
                return Float.intBitsToFloat(XmlBlock.nativeGetAttributeData(this.mParseState, idx));
            }
            throw new RuntimeException("not a float!");
        }

        public String getIdAttribute() {
            int id = XmlBlock.nativeGetIdAttribute(this.mParseState);
            return id >= 0 ? XmlBlock.this.mStrings.get(id).toString() : null;
        }

        public String getClassAttribute() {
            int id = XmlBlock.nativeGetClassAttribute(this.mParseState);
            return id >= 0 ? XmlBlock.this.mStrings.get(id).toString() : null;
        }

        public int getIdAttributeResourceValue(int defaultValue) {
            return getAttributeResourceValue(null, Instrumentation.REPORT_KEY_IDENTIFIER, defaultValue);
        }

        public int getStyleAttribute() {
            return XmlBlock.nativeGetStyleAttribute(this.mParseState);
        }

        public void close() {
            synchronized (this.mBlock) {
                if (this.mParseState != 0) {
                    XmlBlock.nativeDestroyParseState(this.mParseState);
                    this.mParseState = 0;
                    this.mBlock.decOpenCountLocked();
                }
            }
        }

        protected void finalize() throws Throwable {
            close();
        }

        final CharSequence getPooledString(int id) {
            return XmlBlock.this.mStrings.get(id);
        }
    }

    private static final native long nativeCreate(byte[] bArr, int i, int i2);

    private static final native long nativeCreateParseState(long j);

    private static final native void nativeDestroy(long j);

    private static final native void nativeDestroyParseState(long j);

    private static final native int nativeGetAttributeCount(long j);

    private static final native int nativeGetAttributeData(long j, int i);

    private static final native int nativeGetAttributeDataType(long j, int i);

    private static final native int nativeGetAttributeIndex(long j, String str, String str2);

    private static final native int nativeGetAttributeName(long j, int i);

    private static final native int nativeGetAttributeNamespace(long j, int i);

    private static final native int nativeGetAttributeResource(long j, int i);

    private static final native int nativeGetAttributeStringValue(long j, int i);

    private static final native int nativeGetClassAttribute(long j);

    private static final native int nativeGetIdAttribute(long j);

    private static final native int nativeGetLineNumber(long j);

    static final native int nativeGetName(long j);

    private static final native int nativeGetNamespace(long j);

    private static final native long nativeGetStringBlock(long j);

    private static final native int nativeGetStyleAttribute(long j);

    private static final native int nativeGetText(long j);

    static final native int nativeNext(long j);

    public XmlBlock(byte[] data) {
        this.mOpen = true;
        this.mOpenCount = 1;
        this.mAssets = null;
        this.mNative = nativeCreate(data, 0, data.length);
        this.mStrings = new StringBlock(nativeGetStringBlock(this.mNative), false);
    }

    public XmlBlock(byte[] data, int offset, int size) {
        this.mOpen = true;
        this.mOpenCount = 1;
        this.mAssets = null;
        this.mNative = nativeCreate(data, offset, size);
        this.mStrings = new StringBlock(nativeGetStringBlock(this.mNative), false);
    }

    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decOpenCountLocked();
            }
        }
    }

    private void decOpenCountLocked() {
        this.mOpenCount--;
        if (this.mOpenCount == 0) {
            nativeDestroy(this.mNative);
            if (this.mAssets != null) {
                this.mAssets.xmlBlockGone(hashCode());
            }
        }
    }

    public XmlResourceParser newParser() {
        synchronized (this) {
            if (this.mNative != 0) {
                XmlResourceParser parser = new Parser(nativeCreateParseState(this.mNative), this);
                return parser;
            }
            return null;
        }
    }

    protected void finalize() throws Throwable {
        close();
    }

    XmlBlock(AssetManager assets, long xmlBlock) {
        this.mOpen = true;
        this.mOpenCount = 1;
        this.mAssets = assets;
        this.mNative = xmlBlock;
        this.mStrings = new StringBlock(nativeGetStringBlock(xmlBlock), false);
    }
}
