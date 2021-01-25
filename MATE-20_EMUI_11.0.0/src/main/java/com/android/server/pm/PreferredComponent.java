package com.android.server.pm;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    private final String mPCAssistantPackageName = "com.huawei.pcassistant";
    private String mParseError;
    final String[] mSetClasses;
    final String[] mSetComponents;
    final String[] mSetPackages;
    private final String mSetupWizardPackageName;
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
        this.mSetupWizardPackageName = null;
        if (set != null) {
            int N = set.length;
            String[] myPackages = new String[N];
            String[] myClasses = new String[N];
            String[] myComponents = new String[N];
            int j = 0;
            for (ComponentName cn : set) {
                if (cn == null) {
                    this.mSetPackages = null;
                    this.mSetClasses = null;
                    this.mSetComponents = null;
                    return;
                }
                if (!cn.getPackageName().equals("com.huawei.pcassistant")) {
                    myPackages[j] = cn.getPackageName().intern();
                    myClasses[j] = cn.getClassName().intern();
                    myComponents[j] = cn.flattenToShortString();
                    j++;
                }
            }
            this.mSetPackages = (String[]) ArrayUtils.trimToSize(myPackages, j);
            this.mSetClasses = (String[]) ArrayUtils.trimToSize(myClasses, j);
            this.mSetComponents = (String[]) ArrayUtils.trimToSize(myComponents, j);
            return;
        }
        this.mSetPackages = null;
        this.mSetClasses = null;
        this.mSetComponents = null;
    }

    public PreferredComponent(Callbacks callbacks, XmlPullParser parser) throws XmlPullParserException, IOException {
        String str;
        String str2;
        String matchStr;
        this.mCallbacks = callbacks;
        String str3 = "name";
        this.mShortComponent = parser.getAttributeValue(null, str3);
        this.mComponent = ComponentName.unflattenFromString(this.mShortComponent);
        if (this.mComponent == null) {
            this.mParseError = "Bad activity name " + this.mShortComponent;
        }
        String matchStr2 = parser.getAttributeValue(null, ATTR_MATCH);
        int setCount = 0;
        this.mMatch = matchStr2 != null ? Integer.parseInt(matchStr2, 16) : 0;
        String str4 = "set";
        String setCountStr = parser.getAttributeValue(null, str4);
        setCount = setCountStr != null ? Integer.parseInt(setCountStr) : setCount;
        String alwaysStr = parser.getAttributeValue(null, ATTR_ALWAYS);
        int type = 1;
        this.mAlways = alwaysStr != null ? Boolean.parseBoolean(alwaysStr) : true;
        String[] myPackages = setCount > 0 ? new String[setCount] : null;
        String[] myClasses = setCount > 0 ? new String[setCount] : null;
        String[] myComponents = setCount > 0 ? new String[setCount] : null;
        int setPos = 0;
        int outerDepth = parser.getDepth();
        while (true) {
            int type2 = parser.next();
            if (type2 != type) {
                if (type2 == 3 && parser.getDepth() <= outerDepth) {
                    break;
                } else if (type2 == 3 || type2 == 4) {
                    matchStr2 = matchStr2;
                    str3 = str3;
                    str4 = str4;
                    type = 1;
                } else {
                    String tagName = parser.getName();
                    if (tagName.equals(str4)) {
                        matchStr = matchStr2;
                        String name = parser.getAttributeValue(null, str3);
                        if (name != null) {
                            str2 = str3;
                            if (setPos < setCount) {
                                ComponentName cn = ComponentName.unflattenFromString(name);
                                if (cn != null) {
                                    str = str4;
                                    myPackages[setPos] = cn.getPackageName();
                                    myClasses[setPos] = cn.getClassName();
                                    myComponents[setPos] = name;
                                    setPos++;
                                } else if (this.mParseError == null) {
                                    StringBuilder sb = new StringBuilder();
                                    str = str4;
                                    sb.append("Bad set name ");
                                    sb.append(name);
                                    sb.append(" in preferred activity ");
                                    sb.append(this.mShortComponent);
                                    this.mParseError = sb.toString();
                                } else {
                                    str = str4;
                                }
                            } else if (this.mParseError == null) {
                                this.mParseError = "Too many set tags in preferred activity " + this.mShortComponent;
                                str = str4;
                            } else {
                                str = str4;
                            }
                        } else if (this.mParseError == null) {
                            StringBuilder sb2 = new StringBuilder();
                            str2 = str3;
                            sb2.append("No name in set tag in preferred activity ");
                            sb2.append(this.mShortComponent);
                            this.mParseError = sb2.toString();
                            str = str4;
                        } else {
                            str2 = str3;
                            str = str4;
                        }
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        str2 = str3;
                        matchStr = matchStr2;
                        str = str4;
                        if (!this.mCallbacks.onReadTag(tagName, parser)) {
                            Slog.w("PreferredComponent", "Unknown element: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                    matchStr2 = matchStr;
                    str3 = str2;
                    str4 = str;
                    type = 1;
                }
            } else {
                break;
            }
        }
        if (setPos != setCount && this.mParseError == null) {
            this.mParseError = "Not enough set tags (expected " + setCount + " but found " + setPos + ") in " + this.mShortComponent;
        }
        this.mSetPackages = myPackages;
        this.mSetClasses = myClasses;
        this.mSetComponents = myComponents;
        this.mSetupWizardPackageName = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getSetupWizardPackageName();
    }

    public String getParseError() {
        return this.mParseError;
    }

    public void writeToXml(XmlSerializer serializer, boolean full) throws IOException {
        String[] strArr = this.mSetClasses;
        int NS = strArr != null ? strArr.length : 0;
        serializer.attribute(null, "name", this.mShortComponent);
        if (full) {
            int i = this.mMatch;
            if (i != 0) {
                serializer.attribute(null, ATTR_MATCH, Integer.toHexString(i));
            }
            serializer.attribute(null, ATTR_ALWAYS, Boolean.toString(this.mAlways));
            serializer.attribute(null, "set", Integer.toString(NS));
            for (int s = 0; s < NS; s++) {
                serializer.startTag(null, "set");
                serializer.attribute(null, "name", this.mSetComponents[s]);
                serializer.endTag(null, "set");
            }
        }
    }

    public boolean sameSet(List<ResolveInfo> query, boolean excludeSetupWizardPackage) {
        if (this.mSetPackages == null) {
            return query == null;
        }
        if (query == null) {
            return false;
        }
        int NQ = query.size();
        int NS = this.mSetPackages.length;
        int numMatch = 0;
        for (int i = 0; i < NQ; i++) {
            ActivityInfo ai = query.get(i).activityInfo;
            boolean good = false;
            if ((!excludeSetupWizardPackage || !ai.packageName.equals(this.mSetupWizardPackageName)) && !ai.packageName.equals("com.huawei.pcassistant")) {
                int j = 0;
                while (true) {
                    if (j >= NS) {
                        break;
                    }
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
        }
        return numMatch == NS;
    }

    public boolean sameSet(ComponentName[] comps) {
        String[] strArr = this.mSetPackages;
        if (strArr == null) {
            return false;
        }
        int NS = strArr.length;
        int numMatch = 0;
        for (ComponentName cn : comps) {
            if (!cn.getPackageName().equals("com.huawei.pcassistant")) {
                boolean good = false;
                int j = 0;
                while (true) {
                    if (j >= NS) {
                        break;
                    }
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
        }
        if (numMatch == NS) {
            return true;
        }
        return false;
    }

    public boolean isSuperset(List<ResolveInfo> query, boolean excludeSetupWizardPackage) {
        if (this.mSetPackages == null) {
            return query == null;
        }
        if (query == null) {
            return true;
        }
        int NQ = query.size();
        int NS = this.mSetPackages.length;
        if (!excludeSetupWizardPackage && NS < NQ) {
            return false;
        }
        for (int i = 0; i < NQ; i++) {
            ActivityInfo ai = query.get(i).activityInfo;
            boolean foundMatch = false;
            if ((!excludeSetupWizardPackage || (!ai.packageName.equals(this.mSetupWizardPackageName) && !"com.huawei.hwstartupguide".equals(ai.packageName))) && !ai.packageName.equals("com.huawei.pcassistant")) {
                int j = 0;
                while (true) {
                    if (j >= NS) {
                        break;
                    }
                    if (this.mSetPackages[j].equals(ai.packageName) && this.mSetClasses[j].equals(ai.name)) {
                        foundMatch = true;
                        break;
                    }
                    j++;
                }
                if (!foundMatch) {
                    return false;
                }
            }
        }
        return true;
    }

    public ComponentName[] discardObsoleteComponents(List<ResolveInfo> query) {
        if (this.mSetPackages == null || query == null) {
            return new ComponentName[0];
        }
        int NQ = query.size();
        int NS = this.mSetPackages.length;
        ArrayList<ComponentName> aliveComponents = new ArrayList<>();
        for (int i = 0; i < NQ; i++) {
            ActivityInfo ai = query.get(i).activityInfo;
            int j = 0;
            while (true) {
                if (j >= NS) {
                    break;
                }
                if (this.mSetPackages[j].equals(ai.packageName) && this.mSetClasses[j].equals(ai.name)) {
                    aliveComponents.add(new ComponentName(this.mSetPackages[j], this.mSetClasses[j]));
                    break;
                }
                j++;
            }
        }
        return (ComponentName[]) aliveComponents.toArray(new ComponentName[aliveComponents.size()]);
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
            for (int i = 0; i < this.mSetComponents.length; i++) {
                out.print(prefix);
                out.print("    ");
                out.println(this.mSetComponents[i]);
            }
        }
    }
}
