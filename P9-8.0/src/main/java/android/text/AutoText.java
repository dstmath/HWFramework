package android.text;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.provider.UserDictionary.Words;
import android.util.LogException;
import android.view.View;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

public class AutoText {
    private static final int DEFAULT = 14337;
    private static final int INCREMENT = 1024;
    private static final int RIGHT = 9300;
    private static final int TRIE_C = 0;
    private static final int TRIE_CHILD = 2;
    private static final int TRIE_NEXT = 3;
    private static final char TRIE_NULL = '￿';
    private static final int TRIE_OFF = 1;
    private static final int TRIE_ROOT = 0;
    private static final int TRIE_SIZEOF = 4;
    private static AutoText sInstance = new AutoText(Resources.getSystem());
    private static Object sLock = new Object();
    private Locale mLocale;
    private int mSize;
    private String mText;
    private char[] mTrie;
    private char mTrieUsed;

    private AutoText(Resources resources) {
        this.mLocale = resources.getConfiguration().locale;
        init(resources);
    }

    private static AutoText getInstance(View view) {
        AutoText instance;
        Resources res = view.getContext().getResources();
        Locale locale = res.getConfiguration().locale;
        synchronized (sLock) {
            instance = sInstance;
            if (!locale.equals(instance.mLocale)) {
                instance = new AutoText(res);
                sInstance = instance;
            }
        }
        return instance;
    }

    public static String get(CharSequence src, int start, int end, View view) {
        return getInstance(view).lookup(src, start, end);
    }

    public static int getSize(View view) {
        return getInstance(view).getSize();
    }

    private int getSize() {
        return this.mSize;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e A:{LOOP_END, LOOP:0: B:1:0x000a->B:16:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0046 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String lookup(CharSequence src, int start, int end) {
        int here = this.mTrie[0];
        int i = start;
        while (i < end) {
            char c = src.charAt(i);
            while (here != 65535) {
                if (c != this.mTrie[here + 0]) {
                    here = this.mTrie[here + 3];
                } else if (i != end - 1 || this.mTrie[here + 1] == TRIE_NULL) {
                    here = this.mTrie[here + 2];
                    if (here != 65535) {
                        return null;
                    }
                    i++;
                } else {
                    int off = this.mTrie[here + 1];
                    return this.mText.substring(off + 1, (off + 1) + this.mText.charAt(off));
                }
            }
            if (here != 65535) {
            }
        }
        return null;
    }

    private void init(Resources r) {
        XmlResourceParser parser = r.getXml(R.xml.autotext);
        StringBuilder right = new StringBuilder(RIGHT);
        this.mTrie = new char[DEFAULT];
        this.mTrie[0] = TRIE_NULL;
        this.mTrieUsed = 1;
        try {
            XmlUtils.beginDocument(parser, "words");
            String odest = LogException.NO_VALUE;
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null || (element.equals(Words.WORD) ^ 1) != 0) {
                    r.flushLayoutCache();
                } else {
                    String src = parser.getAttributeValue(null, "src");
                    if (parser.next() == 4) {
                        char off;
                        String dest = parser.getText();
                        if (dest.equals(odest)) {
                            off = 0;
                        } else {
                            off = (char) right.length();
                            right.append((char) dest.length());
                            right.append(dest);
                        }
                        add(src, off);
                    }
                }
            }
            r.flushLayoutCache();
            parser.close();
            this.mText = right.toString();
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        } catch (Throwable th) {
            parser.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x008c A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void add(String src, char off) {
        int slen = src.length();
        int herep = 0;
        this.mSize++;
        for (int i = 0; i < slen; i++) {
            char c = src.charAt(i);
            boolean found = false;
            while (this.mTrie[herep] != TRIE_NULL) {
                if (c != this.mTrie[this.mTrie[herep] + 0]) {
                    herep = this.mTrie[herep] + 3;
                } else if (i == slen - 1) {
                    this.mTrie[this.mTrie[herep] + 1] = off;
                    return;
                } else {
                    herep = this.mTrie[herep] + 2;
                    found = true;
                    if (found) {
                        this.mTrie[herep] = newTrieNode();
                        this.mTrie[this.mTrie[herep] + 0] = c;
                        this.mTrie[this.mTrie[herep] + 1] = TRIE_NULL;
                        this.mTrie[this.mTrie[herep] + 3] = TRIE_NULL;
                        this.mTrie[this.mTrie[herep] + 2] = TRIE_NULL;
                        if (i == slen - 1) {
                            this.mTrie[this.mTrie[herep] + 1] = off;
                            return;
                        }
                        herep = this.mTrie[herep] + 2;
                    }
                }
            }
            if (found) {
            }
        }
    }

    private char newTrieNode() {
        if (this.mTrieUsed + 4 > this.mTrie.length) {
            char[] copy = new char[(this.mTrie.length + 1024)];
            System.arraycopy(this.mTrie, 0, copy, 0, this.mTrie.length);
            this.mTrie = copy;
        }
        char ret = this.mTrieUsed;
        this.mTrieUsed = (char) (this.mTrieUsed + 4);
        return ret;
    }
}
