package com.android.server.slice;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SliceFullAccessList {
    static final int DB_VERSION = 1;
    private static final String TAG = "SliceFullAccessList";
    private static final String TAG_LIST = "slice-access-list";
    private static final String TAG_PKG = "pkg";
    private static final String TAG_USER = "user";
    private final String ATT_USER_ID = TAG_USER;
    private final String ATT_VERSION = "version";
    private final Context mContext;
    private final SparseArray<ArraySet<String>> mFullAccessPkgs = new SparseArray<>();

    public SliceFullAccessList(Context context) {
        this.mContext = context;
    }

    public boolean hasFullAccess(String pkg, int userId) {
        ArraySet<String> pkgs = this.mFullAccessPkgs.get(userId, null);
        return pkgs != null && pkgs.contains(pkg);
    }

    public void grantFullAccess(String pkg, int userId) {
        ArraySet<String> pkgs = this.mFullAccessPkgs.get(userId, null);
        if (pkgs == null) {
            pkgs = new ArraySet<>();
            this.mFullAccessPkgs.put(userId, pkgs);
        }
        pkgs.add(pkg);
    }

    public void removeGrant(String pkg, int userId) {
        ArraySet<String> pkgs = this.mFullAccessPkgs.get(userId, null);
        if (pkgs == null) {
            pkgs = new ArraySet<>();
            this.mFullAccessPkgs.put(userId, pkgs);
        }
        pkgs.remove(pkg);
    }

    public void writeXml(XmlSerializer out, int user) throws IOException {
        out.startTag(null, TAG_LIST);
        out.attribute(null, "version", String.valueOf(1));
        int N = this.mFullAccessPkgs.size();
        for (int i = 0; i < N; i++) {
            int userId = this.mFullAccessPkgs.keyAt(i);
            ArraySet<String> pkgs = this.mFullAccessPkgs.valueAt(i);
            if (user == -1 || user == userId) {
                out.startTag(null, TAG_USER);
                out.attribute(null, TAG_USER, Integer.toString(userId));
                if (pkgs != null) {
                    int M = pkgs.size();
                    for (int j = 0; j < M; j++) {
                        out.startTag(null, "pkg");
                        out.text(pkgs.valueAt(j));
                        out.endTag(null, "pkg");
                    }
                }
                out.endTag(null, TAG_USER);
            }
        }
        out.endTag(null, TAG_LIST);
    }

    public void readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        int xmlVersion = XmlUtils.readIntAttribute(parser, "version", 0);
        for (UserInfo userInfo : UserManager.get(this.mContext).getUsers(true)) {
            upgradeXml(xmlVersion, userInfo.getUserHandle().getIdentifier());
        }
        this.mFullAccessPkgs.clear();
        while (true) {
            int type = parser.next();
            if (type != 1) {
                String tag = parser.getName();
                if (type == 3 && TAG_LIST.equals(tag)) {
                    return;
                }
                if (type == 2 && TAG_USER.equals(tag)) {
                    int userId = XmlUtils.readIntAttribute(parser, TAG_USER, 0);
                    ArraySet<String> pkgs = new ArraySet<>();
                    while (true) {
                        int type2 = parser.next();
                        if (type2 == 1) {
                            break;
                        }
                        String userTag = parser.getName();
                        if (type2 == 3 && TAG_USER.equals(userTag)) {
                            break;
                        } else if (type2 == 2 && "pkg".equals(userTag)) {
                            pkgs.add(parser.nextText());
                        }
                    }
                    this.mFullAccessPkgs.put(userId, pkgs);
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void upgradeXml(int xmlVersion, int userId) {
    }
}
