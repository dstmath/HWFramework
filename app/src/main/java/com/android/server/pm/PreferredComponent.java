package com.android.server.pm;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.util.Slog;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PreferredComponent {
    private static final String ATTR_ALWAYS = "always";
    private static final String ATTR_MATCH = "match";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SET = "set";
    private static final String TAG_SET = "set";
    public boolean mAlways;
    private final Callbacks mCallbacks;
    public final ComponentName mComponent;
    public final int mMatch;
    private String mParseError;
    final String[] mSetClasses;
    final String[] mSetComponents;
    final String[] mSetPackages;
    final String mShortComponent;

    public interface Callbacks {
        boolean onReadTag(String str, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;
    }

    public PreferredComponent(Callbacks callbacks, int match, ComponentName[] set, ComponentName component, boolean always) {
        this.mCallbacks = callbacks;
        this.mMatch = 268369920 & match;
        this.mComponent = component;
        this.mAlways = always;
        this.mShortComponent = component.flattenToShortString();
        this.mParseError = null;
        if (set != null) {
            int N = set.length;
            String[] myPackages = new String[N];
            String[] myClasses = new String[N];
            String[] myComponents = new String[N];
            for (int i = 0; i < N; i++) {
                ComponentName cn = set[i];
                if (cn == null) {
                    this.mSetPackages = null;
                    this.mSetClasses = null;
                    this.mSetComponents = null;
                    return;
                }
                myPackages[i] = cn.getPackageName().intern();
                myClasses[i] = cn.getClassName().intern();
                myComponents[i] = cn.flattenToShortString();
            }
            this.mSetPackages = myPackages;
            this.mSetClasses = myClasses;
            this.mSetComponents = myComponents;
        } else {
            this.mSetPackages = null;
            this.mSetClasses = null;
            this.mSetComponents = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PreferredComponent(Callbacks callbacks, XmlPullParser parser) throws XmlPullParserException, IOException {
        this.mCallbacks = callbacks;
        this.mShortComponent = parser.getAttributeValue(null, ATTR_NAME);
        this.mComponent = ComponentName.unflattenFromString(this.mShortComponent);
        if (this.mComponent == null) {
            this.mParseError = "Bad activity name " + this.mShortComponent;
        }
        String matchStr = parser.getAttributeValue(null, ATTR_MATCH);
        this.mMatch = matchStr != null ? Integer.parseInt(matchStr, 16) : 0;
        String setCountStr = parser.getAttributeValue(null, TAG_SET);
        int setCount = setCountStr != null ? Integer.parseInt(setCountStr) : 0;
        String alwaysStr = parser.getAttributeValue(null, ATTR_ALWAYS);
        this.mAlways = alwaysStr != null ? Boolean.parseBoolean(alwaysStr) : true;
        String[] strArr = setCount > 0 ? new String[setCount] : null;
        String[] strArr2 = setCount > 0 ? new String[setCount] : null;
        String[] strArr3 = setCount > 0 ? new String[setCount] : null;
        int setPos = 0;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (setPos != setCount && this.mParseError == null) {
                    this.mParseError = "Not enough set tags (expected " + setCount + " but found " + setPos + ") in " + this.mShortComponent;
                }
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_SET)) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name == null) {
                        if (this.mParseError == null) {
                            this.mParseError = "No name in set tag in preferred activity " + this.mShortComponent;
                        }
                    } else if (setPos < setCount) {
                        ComponentName cn = ComponentName.unflattenFromString(name);
                        if (cn != null) {
                            strArr[setPos] = cn.getPackageName();
                            strArr2[setPos] = cn.getClassName();
                            strArr3[setPos] = name;
                            setPos++;
                        } else if (this.mParseError == null) {
                            this.mParseError = "Bad set name " + name + " in preferred activity " + this.mShortComponent;
                        }
                    } else if (this.mParseError == null) {
                        this.mParseError = "Too many set tags in preferred activity " + this.mShortComponent;
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else if (!this.mCallbacks.onReadTag(tagName, parser)) {
                    Slog.w("PreferredComponent", "Unknown element: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        this.mParseError = "Not enough set tags (expected " + setCount + " but found " + setPos + ") in " + this.mShortComponent;
        this.mSetPackages = strArr;
        this.mSetClasses = strArr2;
        this.mSetComponents = strArr3;
    }

    public String getParseError() {
        return this.mParseError;
    }

    public void writeToXml(XmlSerializer serializer, boolean full) throws IOException {
        int NS = this.mSetClasses != null ? this.mSetClasses.length : 0;
        serializer.attribute(null, ATTR_NAME, this.mShortComponent);
        if (full) {
            if (this.mMatch != 0) {
                serializer.attribute(null, ATTR_MATCH, Integer.toHexString(this.mMatch));
            }
            serializer.attribute(null, ATTR_ALWAYS, Boolean.toString(this.mAlways));
            serializer.attribute(null, TAG_SET, Integer.toString(NS));
            for (int s = 0; s < NS; s++) {
                serializer.startTag(null, TAG_SET);
                serializer.attribute(null, ATTR_NAME, this.mSetComponents[s]);
                serializer.endTag(null, TAG_SET);
            }
        }
    }

    public boolean sameSet(List<ResolveInfo> query) {
        boolean z = true;
        if (this.mSetPackages == null) {
            if (query != null) {
                z = false;
            }
            return z;
        } else if (query == null) {
            return false;
        } else {
            int NQ = query.size();
            int NS = this.mSetPackages.length;
            int numMatch = 0;
            for (int i = 0; i < NQ; i++) {
                ActivityInfo ai = ((ResolveInfo) query.get(i)).activityInfo;
                boolean good = false;
                int j = 0;
                while (j < NS) {
                    if (this.mSetPackages[j].equals(ai.packageName) && this.mSetClasses[j].equals(ai.name)) {
                        numMatch++;
                        good = true;
                        break;
                    }
                    j++;
                }
                if (!good) {
                    return false;
                }
            }
            if (numMatch != NS) {
                z = false;
            }
            return z;
        }
    }

    public boolean sameSet(ComponentName[] comps) {
        boolean z = false;
        if (this.mSetPackages == null) {
            return false;
        }
        int NS = this.mSetPackages.length;
        int numMatch = 0;
        for (ComponentName cn : comps) {
            boolean good = false;
            int j = 0;
            while (j < NS) {
                if (this.mSetPackages[j].equals(cn.getPackageName()) && this.mSetClasses[j].equals(cn.getClassName())) {
                    numMatch++;
                    good = true;
                    break;
                }
                j++;
            }
            if (!good) {
                return false;
            }
        }
        if (numMatch == NS) {
            z = true;
        }
        return z;
    }

    public void dump(PrintWriter out, String prefix, Object ident) {
        out.print(prefix);
        out.print(Integer.toHexString(System.identityHashCode(ident)));
        out.print(' ');
        out.println(this.mShortComponent);
        out.print(prefix);
        out.print(" mMatch=0x");
        out.print(Integer.toHexString(this.mMatch));
        out.print(" mAlways=");
        out.println(this.mAlways);
        if (this.mSetComponents != null) {
            out.print(prefix);
            out.println("  Selected from:");
            for (String println : this.mSetComponents) {
                out.print(prefix);
                out.print("    ");
                out.println(println);
            }
        }
    }
}
