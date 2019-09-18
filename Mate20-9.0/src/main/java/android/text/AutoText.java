package android.text;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.provider.UserDictionary;
import android.view.View;
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

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v5, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v6, types: [char] */
    private String lookup(CharSequence src, int start, int end) {
        int here = this.mTrie[0];
        for (int i = start; i < end; i++) {
            char c = src.charAt(i);
            while (true) {
                if (here == 65535) {
                    break;
                } else if (c != this.mTrie[here + 0]) {
                    here = this.mTrie[here + 3];
                } else if (i != end - 1 || this.mTrie[here + 1] == 65535) {
                    here = this.mTrie[here + 2];
                } else {
                    char off = this.mTrie[here + 1];
                    return this.mText.substring(off + 1, off + 1 + this.mText.charAt(off));
                }
            }
            if (here == 65535) {
                return null;
            }
        }
        return null;
    }

    private void init(Resources r) {
        char off;
        XmlResourceParser parser = r.getXml(18284547);
        StringBuilder right = new StringBuilder(RIGHT);
        this.mTrie = new char[DEFAULT];
        this.mTrie[0] = TRIE_NULL;
        this.mTrieUsed = 1;
        try {
            XmlUtils.beginDocument(parser, "words");
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    break;
                } else if (!element.equals(UserDictionary.Words.WORD)) {
                    break;
                } else {
                    String src = parser.getAttributeValue(null, "src");
                    if (parser.next() == 4) {
                        String dest = parser.getText();
                        if (dest.equals("")) {
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
            throw th;
        }
    }

    private void add(String src, char off) {
        int slen = src.length();
        this.mSize++;
        int herep = 0;
        for (int i = 0; i < slen; i++) {
            char c = src.charAt(i);
            int herep2 = herep;
            boolean found = false;
            while (true) {
                if (this.mTrie[herep2] == 65535) {
                    break;
                } else if (c != this.mTrie[this.mTrie[herep2] + 0]) {
                    herep2 = this.mTrie[herep2] + 3;
                } else if (i == slen - 1) {
                    this.mTrie[this.mTrie[herep2] + 1] = off;
                    return;
                } else {
                    herep2 = this.mTrie[herep2] + 2;
                    found = true;
                }
            }
            if (!found) {
                this.mTrie[herep2] = newTrieNode();
                this.mTrie[this.mTrie[herep2] + 0] = c;
                this.mTrie[this.mTrie[herep2] + 1] = TRIE_NULL;
                this.mTrie[this.mTrie[herep2] + 3] = TRIE_NULL;
                this.mTrie[this.mTrie[herep2] + 2] = TRIE_NULL;
                if (i == slen - 1) {
                    this.mTrie[this.mTrie[herep2] + 1] = off;
                    return;
                }
                herep = this.mTrie[herep2] + 2;
            } else {
                herep = herep2;
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
