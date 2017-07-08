package android.text;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.View;
import com.android.internal.R;
import com.android.internal.util.Protocol;
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
    private static final char TRIE_NULL = '\uffff';
    private static final int TRIE_OFF = 1;
    private static final int TRIE_ROOT = 0;
    private static final int TRIE_SIZEOF = 4;
    private static AutoText sInstance;
    private static Object sLock;
    private Locale mLocale;
    private int mSize;
    private String mText;
    private char[] mTrie;
    private char mTrieUsed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.AutoText.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.AutoText.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.AutoText.<clinit>():void");
    }

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

    private String lookup(CharSequence src, int start, int end) {
        int here = this.mTrie[TRIE_ROOT];
        for (int i = start; i < end; i += TRIE_OFF) {
            char c = src.charAt(i);
            while (here != Protocol.MAX_MESSAGE) {
                if (c != this.mTrie[here + TRIE_ROOT]) {
                    here = this.mTrie[here + TRIE_NEXT];
                } else if (i != end - 1 || this.mTrie[here + TRIE_OFF] == TRIE_NULL) {
                    here = this.mTrie[here + TRIE_CHILD];
                    if (here == Protocol.MAX_MESSAGE) {
                        return null;
                    }
                } else {
                    int off = this.mTrie[here + TRIE_OFF];
                    return this.mText.substring(off + TRIE_OFF, (off + TRIE_OFF) + this.mText.charAt(off));
                }
            }
            if (here == Protocol.MAX_MESSAGE) {
                return null;
            }
        }
        return null;
    }

    private void init(Resources r) {
        XmlResourceParser parser = r.getXml(R.xml.autotext);
        StringBuilder right = new StringBuilder(RIGHT);
        this.mTrie = new char[DEFAULT];
        this.mTrie[TRIE_ROOT] = TRIE_NULL;
        this.mTrieUsed = '\u0001';
        try {
            XmlUtils.beginDocument(parser, "words");
            String odest = "";
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null || !element.equals("word")) {
                    break;
                }
                String src = parser.getAttributeValue(null, "src");
                if (parser.next() == TRIE_SIZEOF) {
                    char c;
                    String dest = parser.getText();
                    if (dest.equals(odest)) {
                        c = '\u0000';
                    } else {
                        c = (char) right.length();
                        right.append((char) dest.length());
                        right.append(dest);
                    }
                    add(src, c);
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

    private void add(String src, char off) {
        int slen = src.length();
        int herep = TRIE_ROOT;
        this.mSize += TRIE_OFF;
        for (int i = TRIE_ROOT; i < slen; i += TRIE_OFF) {
            char c = src.charAt(i);
            boolean found = false;
            while (this.mTrie[herep] != TRIE_NULL) {
                if (c != this.mTrie[this.mTrie[herep] + TRIE_ROOT]) {
                    herep = this.mTrie[herep] + TRIE_NEXT;
                } else if (i == slen - 1) {
                    this.mTrie[this.mTrie[herep] + TRIE_OFF] = off;
                    return;
                } else {
                    herep = this.mTrie[herep] + TRIE_CHILD;
                    found = true;
                    if (!found) {
                        this.mTrie[herep] = newTrieNode();
                        this.mTrie[this.mTrie[herep] + TRIE_ROOT] = c;
                        this.mTrie[this.mTrie[herep] + TRIE_OFF] = TRIE_NULL;
                        this.mTrie[this.mTrie[herep] + TRIE_NEXT] = TRIE_NULL;
                        this.mTrie[this.mTrie[herep] + TRIE_CHILD] = TRIE_NULL;
                        if (i != slen - 1) {
                            this.mTrie[this.mTrie[herep] + TRIE_OFF] = off;
                            return;
                        }
                        herep = this.mTrie[herep] + TRIE_CHILD;
                    }
                }
            }
            if (!found) {
                this.mTrie[herep] = newTrieNode();
                this.mTrie[this.mTrie[herep] + TRIE_ROOT] = c;
                this.mTrie[this.mTrie[herep] + TRIE_OFF] = TRIE_NULL;
                this.mTrie[this.mTrie[herep] + TRIE_NEXT] = TRIE_NULL;
                this.mTrie[this.mTrie[herep] + TRIE_CHILD] = TRIE_NULL;
                if (i != slen - 1) {
                    herep = this.mTrie[herep] + TRIE_CHILD;
                } else {
                    this.mTrie[this.mTrie[herep] + TRIE_OFF] = off;
                    return;
                }
            }
        }
    }

    private char newTrieNode() {
        if (this.mTrieUsed + TRIE_SIZEOF > this.mTrie.length) {
            char[] copy = new char[(this.mTrie.length + INCREMENT)];
            System.arraycopy(this.mTrie, TRIE_ROOT, copy, TRIE_ROOT, this.mTrie.length);
            this.mTrie = copy;
        }
        char ret = this.mTrieUsed;
        this.mTrieUsed = (char) (this.mTrieUsed + TRIE_SIZEOF);
        return ret;
    }
}
