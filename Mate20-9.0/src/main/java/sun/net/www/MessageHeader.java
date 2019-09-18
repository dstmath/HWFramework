package sun.net.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

public class MessageHeader {
    /* access modifiers changed from: private */
    public String[] keys;
    /* access modifiers changed from: private */
    public int nkeys;
    /* access modifiers changed from: private */
    public String[] values;

    class HeaderIterator implements Iterator<String> {
        boolean haveNext = false;
        int index = 0;
        String key;
        Object lock;
        int next = -1;

        public HeaderIterator(String k, Object lock2) {
            this.key = k;
            this.lock = lock2;
        }

        public boolean hasNext() {
            synchronized (this.lock) {
                if (this.haveNext) {
                    return true;
                }
                while (this.index < MessageHeader.this.nkeys) {
                    if (this.key.equalsIgnoreCase(MessageHeader.this.keys[this.index])) {
                        this.haveNext = true;
                        int i = this.index;
                        this.index = i + 1;
                        this.next = i;
                        return true;
                    }
                    this.index++;
                }
                return false;
            }
        }

        public String next() {
            synchronized (this.lock) {
                if (this.haveNext) {
                    this.haveNext = false;
                    String str = MessageHeader.this.values[this.next];
                    return str;
                } else if (hasNext()) {
                    String next2 = next();
                    return next2;
                } else {
                    throw new NoSuchElementException("No more elements");
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not allowed");
        }
    }

    public MessageHeader() {
        grow();
    }

    public MessageHeader(InputStream is) throws IOException {
        parseHeader(is);
    }

    public synchronized String getHeaderNamesInList() {
        StringJoiner joiner;
        joiner = new StringJoiner(",");
        for (int i = 0; i < this.nkeys; i++) {
            joiner.add(this.keys[i]);
        }
        return joiner.toString();
    }

    public synchronized void reset() {
        this.keys = null;
        this.values = null;
        this.nkeys = 0;
        grow();
    }

    public synchronized String findValue(String k) {
        if (k == null) {
            int i = this.nkeys;
            do {
                i--;
                if (i >= 0) {
                }
            } while (this.keys[i] != null);
            return this.values[i];
        }
        int i2 = this.nkeys;
        do {
            i2--;
            if (i2 >= 0) {
            }
        } while (!k.equalsIgnoreCase(this.keys[i2]));
        return this.values[i2];
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        return r0;
     */
    public synchronized int getKey(String k) {
        int i = this.nkeys;
        while (true) {
            i--;
            if (i < 0) {
                return -1;
            }
            if (this.keys[i] == k || (k != null && k.equalsIgnoreCase(this.keys[i]))) {
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized String getKey(int n) {
        if (n >= 0) {
            if (n < this.nkeys) {
                return this.keys[n];
            }
        }
        return null;
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized String getValue(int n) {
        if (n >= 0) {
            if (n < this.nkeys) {
                return this.values[n];
            }
        }
        return null;
    }

    public synchronized String findNextValue(String k, String v) {
        boolean foundV = false;
        if (k != null) {
            int i = this.nkeys;
            while (true) {
                i--;
                if (i < 0) {
                    break;
                } else if (k.equalsIgnoreCase(this.keys[i])) {
                    if (foundV) {
                        return this.values[i];
                    } else if (this.values[i] == v) {
                        foundV = true;
                    }
                }
            }
        } else {
            try {
                int i2 = this.nkeys;
                while (true) {
                    i2--;
                    if (i2 < 0) {
                        break;
                    } else if (this.keys[i2] == null) {
                        if (foundV) {
                            return this.values[i2];
                        } else if (this.values[i2] == v) {
                            foundV = true;
                        }
                    }
                }
            } finally {
            }
        }
        return null;
    }

    public boolean filterNTLMResponses(String k) {
        boolean found = false;
        int i = 0;
        while (true) {
            if (i < this.nkeys) {
                if (k.equalsIgnoreCase(this.keys[i]) && this.values[i] != null && this.values[i].length() > 5 && this.values[i].substring(0, 5).equalsIgnoreCase("NTLM ")) {
                    found = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (found) {
            int j = 0;
            for (int i2 = 0; i2 < this.nkeys; i2++) {
                if (!k.equalsIgnoreCase(this.keys[i2]) || (!"Negotiate".equalsIgnoreCase(this.values[i2]) && !"Kerberos".equalsIgnoreCase(this.values[i2]))) {
                    if (i2 != j) {
                        this.keys[j] = this.keys[i2];
                        this.values[j] = this.values[i2];
                    }
                    j++;
                }
            }
            if (j != this.nkeys) {
                this.nkeys = j;
                return true;
            }
        }
        return false;
    }

    public Iterator<String> multiValueIterator(String k) {
        return new HeaderIterator(k, this);
    }

    public synchronized Map<String, List<String>> getHeaders() {
        return getHeaders(null);
    }

    public synchronized Map<String, List<String>> getHeaders(String[] excludeList) {
        return filterAndAddHeaders(excludeList, null);
    }

    public synchronized Map<String, List<String>> filterAndAddHeaders(String[] excludeList, Map<String, List<String>> include) {
        Map<String, List<String>> m;
        boolean skipIt = false;
        m = new HashMap<>();
        int i = this.nkeys;
        while (true) {
            i--;
            if (i < 0) {
                break;
            }
            if (excludeList != null) {
                int j = 0;
                while (true) {
                    if (j < excludeList.length) {
                        if (excludeList[j] != null && excludeList[j].equalsIgnoreCase(this.keys[i])) {
                            skipIt = true;
                            break;
                        }
                        j++;
                    } else {
                        break;
                    }
                }
            }
            if (!skipIt) {
                List<String> l = m.get(this.keys[i]);
                if (l == null) {
                    l = new ArrayList<>();
                    m.put(this.keys[i], l);
                }
                l.add(this.values[i]);
            } else {
                skipIt = false;
            }
        }
        if (include != null) {
            for (Map.Entry<String, List<String>> entry : include.entrySet()) {
                List<String> l2 = m.get(entry.getKey());
                if (l2 == null) {
                    l2 = new ArrayList<>();
                    m.put(entry.getKey(), l2);
                }
                l2.addAll(entry.getValue());
            }
        }
        for (String key : m.keySet()) {
            m.put(key, Collections.unmodifiableList(m.get(key)));
        }
        return Collections.unmodifiableMap(m);
    }

    public synchronized void print(PrintStream p) {
        String str;
        for (int i = 0; i < this.nkeys; i++) {
            if (this.keys[i] != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.keys[i]);
                if (this.values[i] != null) {
                    str = ": " + this.values[i];
                } else {
                    str = "";
                }
                sb.append(str);
                sb.append("\r\n");
                p.print(sb.toString());
            }
        }
        p.print("\r\n");
        p.flush();
    }

    public synchronized void add(String k, String v) {
        grow();
        this.keys[this.nkeys] = k;
        this.values[this.nkeys] = v;
        this.nkeys++;
    }

    public synchronized void prepend(String k, String v) {
        grow();
        for (int i = this.nkeys; i > 0; i--) {
            this.keys[i] = this.keys[i - 1];
            this.values[i] = this.values[i - 1];
        }
        this.keys[0] = k;
        this.values[0] = v;
        this.nkeys++;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        return;
     */
    public synchronized void set(int i, String k, String v) {
        grow();
        if (i >= 0) {
            if (i >= this.nkeys) {
                add(k, v);
            } else {
                this.keys[i] = k;
                this.values[i] = v;
            }
        }
    }

    private void grow() {
        if (this.keys == null || this.nkeys >= this.keys.length) {
            String[] nk = new String[(this.nkeys + 4)];
            String[] nv = new String[(this.nkeys + 4)];
            if (this.keys != null) {
                System.arraycopy((Object) this.keys, 0, (Object) nk, 0, this.nkeys);
            }
            if (this.values != null) {
                System.arraycopy((Object) this.values, 0, (Object) nv, 0, this.nkeys);
            }
            this.keys = nk;
            this.values = nv;
        }
    }

    public synchronized void remove(String k) {
        int i = 0;
        if (k == null) {
            while (i < this.nkeys) {
                try {
                    while (this.keys[i] == null && i < this.nkeys) {
                        for (int j = i; j < this.nkeys - 1; j++) {
                            this.keys[j] = this.keys[j + 1];
                            this.values[j] = this.values[j + 1];
                        }
                        this.nkeys--;
                    }
                    i++;
                } finally {
                }
            }
        } else {
            while (i < this.nkeys) {
                while (k.equalsIgnoreCase(this.keys[i]) && i < this.nkeys) {
                    for (int j2 = i; j2 < this.nkeys - 1; j2++) {
                        this.keys[j2] = this.keys[j2 + 1];
                        this.values[j2] = this.values[j2 + 1];
                    }
                    this.nkeys--;
                }
                i++;
            }
        }
    }

    public synchronized void set(String k, String v) {
        int i = this.nkeys;
        do {
            i--;
            if (i < 0) {
                add(k, v);
                return;
            }
        } while (!k.equalsIgnoreCase(this.keys[i]));
        this.values[i] = v;
    }

    public synchronized void setIfNotSet(String k, String v) {
        if (findValue(k) == null) {
            add(k, v);
        }
    }

    public static String canonicalID(String id) {
        if (id == null) {
            return "";
        }
        int st = 0;
        int len = id.length();
        boolean substr = false;
        while (st < len) {
            int charAt = id.charAt(st);
            int c = charAt;
            if (charAt != 60 && c > 32) {
                break;
            }
            st++;
            substr = true;
        }
        while (st < len) {
            int charAt2 = id.charAt(len - 1);
            int c2 = charAt2;
            if (charAt2 != 62 && c2 > 32) {
                break;
            }
            len--;
            substr = true;
        }
        return substr ? id.substring(st, len) : id;
    }

    public void parseHeader(InputStream is) throws IOException {
        synchronized (this) {
            this.nkeys = 0;
        }
        mergeHeader(is);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x006b A[SYNTHETIC] */
    public void mergeHeader(InputStream is) throws IOException {
        int keyend;
        String k;
        String v;
        boolean inKey;
        if (is != null) {
            char[] s = new char[10];
            int firstc = is.read();
            while (firstc != 10 && firstc != 13 && firstc >= 0) {
                int keyend2 = -1;
                boolean inKey2 = firstc > 32;
                int len = 0 + 1;
                s[0] = (char) firstc;
                while (true) {
                    int len2 = is.read();
                    int c = len2;
                    if (len2 >= 0) {
                        if (c != 13) {
                            if (c != 32) {
                                if (c != 58) {
                                    switch (c) {
                                        case 9:
                                            c = 32;
                                            break;
                                        case 10:
                                            break;
                                    }
                                } else {
                                    if (inKey2 && len > 0) {
                                        keyend2 = len;
                                    }
                                    inKey = false;
                                    inKey2 = inKey;
                                    if (len >= s.length) {
                                        char[] ns = new char[(s.length * 2)];
                                        System.arraycopy((Object) s, 0, (Object) ns, 0, len);
                                        s = ns;
                                    }
                                    s[len] = (char) c;
                                    len++;
                                }
                            }
                            inKey = false;
                            inKey2 = inKey;
                            if (len >= s.length) {
                            }
                            s[len] = (char) c;
                            len++;
                        }
                        firstc = is.read();
                        if (c == 13 && firstc == 10) {
                            firstc = is.read();
                            if (firstc == 13) {
                                firstc = is.read();
                            }
                        }
                        if (!(firstc == 10 || firstc == 13 || firstc > 32)) {
                            c = 32;
                            if (len >= s.length) {
                            }
                            s[len] = (char) c;
                            len++;
                        }
                    } else {
                        firstc = -1;
                    }
                }
                while (len > 0 && s[len - 1] <= ' ') {
                    len--;
                }
                if (keyend2 <= 0) {
                    k = null;
                    keyend = 0;
                } else {
                    k = String.copyValueOf(s, 0, keyend2);
                    if (keyend2 < len && s[keyend2] == ':') {
                        keyend2++;
                    }
                    keyend = keyend2;
                    while (keyend < len && s[keyend] <= ' ') {
                        keyend++;
                    }
                }
                if (keyend >= len) {
                    v = new String();
                } else {
                    v = String.copyValueOf(s, keyend, len - keyend);
                }
                add(k, v);
            }
        }
    }

    public synchronized String toString() {
        String result;
        result = super.toString() + this.nkeys + " pairs: ";
        int i = 0;
        while (i < this.keys.length && i < this.nkeys) {
            result = result + "{" + this.keys[i] + ": " + this.values[i] + "}";
            i++;
        }
        return result;
    }
}
