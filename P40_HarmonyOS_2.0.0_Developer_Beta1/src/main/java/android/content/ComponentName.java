package android.content;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

public final class ComponentName implements Parcelable, Cloneable, Comparable<ComponentName> {
    public static final Parcelable.Creator<ComponentName> CREATOR = new Parcelable.Creator<ComponentName>() {
        /* class android.content.ComponentName.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ComponentName createFromParcel(Parcel in) {
            return new ComponentName(in);
        }

        @Override // android.os.Parcelable.Creator
        public ComponentName[] newArray(int size) {
            return new ComponentName[size];
        }
    };
    private final String mClass;
    private final String mPackage;

    @FunctionalInterface
    public interface WithComponentName {
        ComponentName getComponentName();
    }

    public static ComponentName createRelative(String pkg, String cls) {
        String fullName;
        if (!TextUtils.isEmpty(cls)) {
            if (cls.charAt(0) == '.') {
                fullName = pkg + cls;
            } else {
                fullName = cls;
            }
            return new ComponentName(pkg, fullName);
        }
        throw new IllegalArgumentException("class name cannot be empty");
    }

    public static ComponentName createRelative(Context pkg, String cls) {
        return createRelative(pkg.getPackageName(), cls);
    }

    public ComponentName(String pkg, String cls) {
        if (pkg == null) {
            throw new NullPointerException("package name is null");
        } else if (cls != null) {
            this.mPackage = pkg;
            this.mClass = cls;
        } else {
            throw new NullPointerException("class name is null");
        }
    }

    public ComponentName(Context pkg, String cls) {
        if (cls != null) {
            this.mPackage = pkg.getPackageName();
            this.mClass = cls;
            return;
        }
        throw new NullPointerException("class name is null");
    }

    public ComponentName(Context pkg, Class<?> cls) {
        this.mPackage = pkg.getPackageName();
        this.mClass = cls.getName();
    }

    @Override // java.lang.Object
    public ComponentName clone() {
        return new ComponentName(this.mPackage, this.mClass);
    }

    public String getPackageName() {
        return this.mPackage;
    }

    public String getClassName() {
        return this.mClass;
    }

    public String getShortClassName() {
        int PN;
        int CN;
        if (!this.mClass.startsWith(this.mPackage) || (CN = this.mClass.length()) <= (PN = this.mPackage.length()) || this.mClass.charAt(PN) != '.') {
            return this.mClass;
        }
        return this.mClass.substring(PN, CN);
    }

    private static void appendShortClassName(StringBuilder sb, String packageName, String className) {
        int PN;
        int CN;
        if (!className.startsWith(packageName) || (CN = className.length()) <= (PN = packageName.length()) || className.charAt(PN) != '.') {
            sb.append(className);
        } else {
            sb.append((CharSequence) className, PN, CN);
        }
    }

    private static void printShortClassName(PrintWriter pw, String packageName, String className) {
        int PN;
        int CN;
        if (!className.startsWith(packageName) || (CN = className.length()) <= (PN = packageName.length()) || className.charAt(PN) != '.') {
            pw.print(className);
        } else {
            pw.write(className, PN, CN - PN);
        }
    }

    public static String flattenToShortString(ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        return componentName.flattenToShortString();
    }

    public String flattenToString() {
        return this.mPackage + "/" + this.mClass;
    }

    public String flattenToShortString() {
        StringBuilder sb = new StringBuilder(this.mPackage.length() + this.mClass.length());
        appendShortString(sb, this.mPackage, this.mClass);
        return sb.toString();
    }

    public void appendShortString(StringBuilder sb) {
        appendShortString(sb, this.mPackage, this.mClass);
    }

    @UnsupportedAppUsage
    public static void appendShortString(StringBuilder sb, String packageName, String className) {
        sb.append(packageName);
        sb.append('/');
        appendShortClassName(sb, packageName, className);
    }

    @UnsupportedAppUsage
    public static void printShortString(PrintWriter pw, String packageName, String className) {
        pw.print(packageName);
        pw.print('/');
        printShortClassName(pw, packageName, className);
    }

    public static ComponentName unflattenFromString(String str) {
        int sep = str.indexOf(47);
        if (sep < 0 || sep + 1 >= str.length()) {
            return null;
        }
        String pkg = str.substring(0, sep);
        String cls = str.substring(sep + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        return new ComponentName(pkg, cls);
    }

    public String toShortString() {
        return "{" + this.mPackage + "/" + this.mClass + "}";
    }

    @Override // java.lang.Object
    public String toString() {
        return "ComponentInfo{" + this.mPackage + "/" + this.mClass + "}";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mPackage);
        proto.write(1138166333442L, this.mClass);
        proto.end(token);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj != null) {
            try {
                ComponentName other = (ComponentName) obj;
                if (!this.mPackage.equals(other.mPackage) || !this.mClass.equals(other.mClass)) {
                    return false;
                }
                return true;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.mPackage.hashCode() + this.mClass.hashCode();
    }

    public int compareTo(ComponentName that) {
        int v = this.mPackage.compareTo(that.mPackage);
        if (v != 0) {
            return v;
        }
        return this.mClass.compareTo(that.mClass);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackage);
        out.writeString(this.mClass);
    }

    public static void writeToParcel(ComponentName c, Parcel out) {
        if (c != null) {
            c.writeToParcel(out, 0);
        } else {
            out.writeString(null);
        }
    }

    public static ComponentName readFromParcel(Parcel in) {
        String pkg = in.readString();
        if (pkg != null) {
            return new ComponentName(pkg, in);
        }
        return null;
    }

    public ComponentName(Parcel in) {
        this.mPackage = in.readString();
        if (this.mPackage != null) {
            this.mClass = in.readString();
            if (this.mClass == null) {
                throw new NullPointerException("class name is null");
            }
            return;
        }
        throw new NullPointerException("package name is null");
    }

    private ComponentName(String pkg, Parcel in) {
        this.mPackage = pkg;
        this.mClass = in.readString();
    }
}
