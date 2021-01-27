package ohos.com.sun.org.apache.xml.internal.serializer;

public final class Version {
    public static String getImplementationLanguage() {
        return "Java";
    }

    public static int getMaintenanceVersionNum() {
        return 0;
    }

    public static int getMajorVersionNum() {
        return 2;
    }

    public static String getProduct() {
        return "Serializer";
    }

    public static int getReleaseVersionNum() {
        return 7;
    }

    public static String getVersion() {
        int i;
        StringBuilder sb;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(getProduct());
        sb2.append(" ");
        sb2.append(getImplementationLanguage());
        sb2.append(" ");
        sb2.append(getMajorVersionNum());
        sb2.append(".");
        sb2.append(getReleaseVersionNum());
        sb2.append(".");
        if (getDevelopmentVersionNum() > 0) {
            sb = new StringBuilder();
            sb.append("D");
            i = getDevelopmentVersionNum();
        } else {
            sb = new StringBuilder();
            sb.append("");
            i = getMaintenanceVersionNum();
        }
        sb.append(i);
        sb2.append(sb.toString());
        return sb2.toString();
    }

    public static void _main(String[] strArr) {
        System.out.println(getVersion());
    }

    public static int getDevelopmentVersionNum() {
        try {
            if (new String("").length() == 0) {
                return 0;
            }
            return Integer.parseInt("");
        } catch (NumberFormatException unused) {
            return 0;
        }
    }
}
