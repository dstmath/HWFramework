package org.apache.xalan;

import org.apache.xalan.templates.Constants;

public class Version {
    public static String getVersion() {
        StringBuilder sb;
        int maintenanceVersionNum;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(getProduct());
        sb2.append(" ");
        sb2.append(getImplementationLanguage());
        sb2.append(" ");
        sb2.append(getMajorVersionNum());
        sb2.append(Constants.ATTRVAL_THIS);
        sb2.append(getReleaseVersionNum());
        sb2.append(Constants.ATTRVAL_THIS);
        if (getDevelopmentVersionNum() > 0) {
            sb = new StringBuilder();
            sb.append("D");
            maintenanceVersionNum = getDevelopmentVersionNum();
        } else {
            sb = new StringBuilder();
            sb.append("");
            maintenanceVersionNum = getMaintenanceVersionNum();
        }
        sb.append(maintenanceVersionNum);
        sb2.append(sb.toString());
        return sb2.toString();
    }

    public static void main(String[] argv) {
        System.out.println(getVersion());
    }

    public static String getProduct() {
        return "Xalan";
    }

    public static String getImplementationLanguage() {
        return "Java";
    }

    public static int getMajorVersionNum() {
        return 2;
    }

    public static int getReleaseVersionNum() {
        return 7;
    }

    public static int getMaintenanceVersionNum() {
        return 1;
    }

    public static int getDevelopmentVersionNum() {
        try {
            if (new String("").length() == 0) {
                return 0;
            }
            return Integer.parseInt("");
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
