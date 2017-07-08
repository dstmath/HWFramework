package android.content;

import android.net.NetworkCapabilities;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class UriMatcher {
    private static final int EXACT = 0;
    public static final int NO_MATCH = -1;
    private static final int NUMBER = 1;
    private static final int TEXT = 2;
    private ArrayList<UriMatcher> mChildren;
    private int mCode;
    private String mText;
    private int mWhich;

    public UriMatcher(int code) {
        this.mCode = code;
        this.mWhich = NO_MATCH;
        this.mChildren = new ArrayList();
        this.mText = null;
    }

    private UriMatcher() {
        this.mCode = NO_MATCH;
        this.mWhich = NO_MATCH;
        this.mChildren = new ArrayList();
        this.mText = null;
    }

    public void addURI(String authority, String path, int code) {
        if (code < 0) {
            throw new IllegalArgumentException("code " + code + " is invalid: it must be positive");
        }
        String[] tokens = null;
        if (path != null) {
            String newPath = path;
            if (path.length() > 0 && path.charAt(EXACT) == '/') {
                newPath = path.substring(NUMBER);
            }
            tokens = newPath.split("/");
        }
        int numTokens = tokens != null ? tokens.length : EXACT;
        UriMatcher node = this;
        int i = NO_MATCH;
        while (i < numTokens) {
            String token = i < 0 ? authority : tokens[i];
            ArrayList<UriMatcher> children = node.mChildren;
            int numChildren = children.size();
            int j = EXACT;
            while (j < numChildren) {
                UriMatcher child = (UriMatcher) children.get(j);
                if (token.equals(child.mText)) {
                    node = child;
                    break;
                }
                j += NUMBER;
            }
            if (j == numChildren) {
                child = new UriMatcher();
                if (token.equals("#")) {
                    child.mWhich = NUMBER;
                } else if (token.equals(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER)) {
                    child.mWhich = TEXT;
                } else {
                    child.mWhich = EXACT;
                }
                child.mText = token;
                node.mChildren.add(child);
                node = child;
            }
            i += NUMBER;
        }
        node.mCode = code;
    }

    public int match(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        int li = pathSegments.size();
        UriMatcher node = this;
        if (li == 0 && uri.getAuthority() == null) {
            return this.mCode;
        }
        int i = NO_MATCH;
        while (i < li) {
            String u = i < 0 ? uri.getAuthority() : (String) pathSegments.get(i);
            ArrayList<UriMatcher> list = node.mChildren;
            if (list == null) {
                return node.mCode;
            }
            node = null;
            int lj = list.size();
            int j = EXACT;
            while (j < lj) {
                UriMatcher n = (UriMatcher) list.get(j);
                switch (n.mWhich) {
                    case EXACT /*0*/:
                        if (n.mText.equals(u)) {
                            node = n;
                            break;
                        }
                        break;
                    case NUMBER /*1*/:
                        int lk = u.length();
                        int k = EXACT;
                        while (k < lk) {
                            char c = u.charAt(k);
                            if (c >= '0' && c <= '9') {
                                k += NUMBER;
                            }
                        }
                        node = n;
                        break;
                    case TEXT /*2*/:
                        node = n;
                        break;
                }
                if (node == null) {
                    j += NUMBER;
                } else if (node == null) {
                    return NO_MATCH;
                } else {
                    i += NUMBER;
                }
            }
            if (node == null) {
                return NO_MATCH;
            }
            i += NUMBER;
        }
        return node.mCode;
    }
}
