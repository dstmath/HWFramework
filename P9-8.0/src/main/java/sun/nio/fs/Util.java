package sun.nio.fs;

import java.nio.charset.Charset;
import java.nio.file.LinkOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class Util {
    private static final Charset jnuEncoding = Charset.forName("UTF-8");

    private Util() {
    }

    static Charset jnuEncoding() {
        return jnuEncoding;
    }

    static byte[] toBytes(String s) {
        return s.getBytes(jnuEncoding);
    }

    static String toString(byte[] bytes) {
        return new String(bytes, jnuEncoding);
    }

    static String[] split(String s, char c) {
        int i;
        int count = 0;
        for (i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        String[] result = new String[(count + 1)];
        int n = 0;
        int last = 0;
        for (i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                int n2 = n + 1;
                result[n] = s.substring(last, i);
                last = i + 1;
                n = n2;
            }
        }
        result[n] = s.substring(last, s.length());
        return result;
    }

    @SafeVarargs
    static <E> Set<E> newSet(E... elements) {
        HashSet<E> set = new HashSet();
        for (E e : elements) {
            set.-java_util_stream_DistinctOps$1-mthref-1(e);
        }
        return set;
    }

    @SafeVarargs
    static <E> Set<E> newSet(Set<E> other, E... elements) {
        HashSet<E> set = new HashSet((Collection) other);
        for (E e : elements) {
            set.-java_util_stream_DistinctOps$1-mthref-1(e);
        }
        return set;
    }

    static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        int i = 0;
        int length = options.length;
        while (i < length) {
            LinkOption option = options[i];
            if (option == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
                i++;
            } else if (option == null) {
                throw new NullPointerException();
            } else {
                throw new AssertionError((Object) "Should not get here");
            }
        }
        return followLinks;
    }
}
