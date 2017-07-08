package android.media;

import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;

/* compiled from: WebVttRenderer */
class Tokenizer {
    private static final String TAG = "Tokenizer";
    private TokenizerPhase mDataTokenizer;
    private int mHandledLen;
    private String mLine;
    private OnTokenListener mListener;
    private TokenizerPhase mPhase;
    private TokenizerPhase mTagTokenizer;

    /* compiled from: WebVttRenderer */
    interface TokenizerPhase {
        TokenizerPhase start();

        void tokenize();
    }

    /* compiled from: WebVttRenderer */
    class DataTokenizer implements TokenizerPhase {
        private StringBuilder mData;

        DataTokenizer() {
        }

        public TokenizerPhase start() {
            this.mData = new StringBuilder();
            return this;
        }

        private boolean replaceEscape(String escape, String replacement, int pos) {
            if (!Tokenizer.this.mLine.startsWith(escape, pos)) {
                return false;
            }
            this.mData.append(Tokenizer.this.mLine.substring(Tokenizer.this.mHandledLen, pos));
            this.mData.append(replacement);
            Tokenizer.this.mHandledLen = escape.length() + pos;
            pos = Tokenizer.this.mHandledLen - 1;
            return true;
        }

        public void tokenize() {
            int end = Tokenizer.this.mLine.length();
            int pos = Tokenizer.this.mHandledLen;
            while (pos < Tokenizer.this.mLine.length()) {
                if (Tokenizer.this.mLine.charAt(pos) == '&') {
                    if (!(replaceEscape("&amp;", "&", pos) || replaceEscape("&lt;", "<", pos) || replaceEscape("&gt;", ">", pos) || replaceEscape("&lrm;", "\u200e", pos) || replaceEscape("&rlm;", "\u200f", pos) || replaceEscape("&nbsp;", "\u00a0", pos))) {
                    }
                } else if (Tokenizer.this.mLine.charAt(pos) == '<') {
                    end = pos;
                    Tokenizer.this.mPhase = Tokenizer.this.mTagTokenizer.start();
                    break;
                }
                pos++;
            }
            this.mData.append(Tokenizer.this.mLine.substring(Tokenizer.this.mHandledLen, end));
            Tokenizer.this.mListener.onData(this.mData.toString());
            this.mData.delete(0, this.mData.length());
            Tokenizer.this.mHandledLen = end;
        }
    }

    /* compiled from: WebVttRenderer */
    interface OnTokenListener {
        void onData(String str);

        void onEnd(String str);

        void onLineEnd();

        void onStart(String str, String[] strArr, String str2);

        void onTimeStamp(long j);
    }

    /* compiled from: WebVttRenderer */
    class TagTokenizer implements TokenizerPhase {
        private String mAnnotation;
        private boolean mAtAnnotation;
        private String mName;

        TagTokenizer() {
        }

        public TokenizerPhase start() {
            String str = ProxyInfo.LOCAL_EXCL_LIST;
            this.mAnnotation = str;
            this.mName = str;
            this.mAtAnnotation = false;
            return this;
        }

        public void tokenize() {
            if (!this.mAtAnnotation) {
                Tokenizer tokenizer = Tokenizer.this;
                tokenizer.mHandledLen = tokenizer.mHandledLen + 1;
            }
            if (Tokenizer.this.mHandledLen < Tokenizer.this.mLine.length()) {
                String[] parts;
                if (this.mAtAnnotation || Tokenizer.this.mLine.charAt(Tokenizer.this.mHandledLen) == '/') {
                    parts = Tokenizer.this.mLine.substring(Tokenizer.this.mHandledLen).split(">");
                } else {
                    parts = Tokenizer.this.mLine.substring(Tokenizer.this.mHandledLen).split("[\t\f >]");
                }
                String part = Tokenizer.this.mLine.substring(Tokenizer.this.mHandledLen, Tokenizer.this.mHandledLen + parts[0].length());
                tokenizer = Tokenizer.this;
                tokenizer.mHandledLen = tokenizer.mHandledLen + parts[0].length();
                if (this.mAtAnnotation) {
                    this.mAnnotation += WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + part;
                } else {
                    this.mName = part;
                }
            }
            this.mAtAnnotation = true;
            if (Tokenizer.this.mHandledLen < Tokenizer.this.mLine.length() && Tokenizer.this.mLine.charAt(Tokenizer.this.mHandledLen) == '>') {
                yield_tag();
                Tokenizer.this.mPhase = Tokenizer.this.mDataTokenizer.start();
                tokenizer = Tokenizer.this;
                tokenizer.mHandledLen = tokenizer.mHandledLen + 1;
            }
        }

        private void yield_tag() {
            if (this.mName.startsWith("/")) {
                Tokenizer.this.mListener.onEnd(this.mName.substring(1));
            } else if (this.mName.length() <= 0 || !Character.isDigit(this.mName.charAt(0))) {
                this.mAnnotation = this.mAnnotation.replaceAll("\\s+", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (this.mAnnotation.startsWith(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                    this.mAnnotation = this.mAnnotation.substring(1);
                }
                if (this.mAnnotation.endsWith(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                    this.mAnnotation = this.mAnnotation.substring(0, this.mAnnotation.length() - 1);
                }
                String[] strArr = null;
                int dotAt = this.mName.indexOf(46);
                if (dotAt >= 0) {
                    strArr = this.mName.substring(dotAt + 1).split("\\.");
                    this.mName = this.mName.substring(0, dotAt);
                }
                Tokenizer.this.mListener.onStart(this.mName, strArr, this.mAnnotation);
            } else {
                try {
                    Tokenizer.this.mListener.onTimeStamp(WebVttParser.parseTimestampMs(this.mName));
                } catch (NumberFormatException e) {
                    Log.d(Tokenizer.TAG, "invalid timestamp tag: <" + this.mName + ">");
                }
            }
        }
    }

    Tokenizer(OnTokenListener listener) {
        this.mDataTokenizer = new DataTokenizer();
        this.mTagTokenizer = new TagTokenizer();
        reset();
        this.mListener = listener;
    }

    void reset() {
        this.mPhase = this.mDataTokenizer.start();
    }

    void tokenize(String s) {
        this.mHandledLen = 0;
        this.mLine = s;
        while (this.mHandledLen < this.mLine.length()) {
            this.mPhase.tokenize();
        }
        if (!(this.mPhase instanceof TagTokenizer)) {
            this.mListener.onLineEnd();
        }
    }
}
