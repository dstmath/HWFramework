package sun.misc;

public class MessageUtils {
    public static String subst(String patt, String arg) {
        return subst(patt, new String[]{arg});
    }

    public static String subst(String patt, String arg1, String arg2) {
        return subst(patt, new String[]{arg1, arg2});
    }

    public static String subst(String patt, String arg1, String arg2, String arg3) {
        return subst(patt, new String[]{arg1, arg2, arg3});
    }

    public static String subst(String patt, String[] args) {
        StringBuffer result = new StringBuffer();
        int len = patt.length();
        int i = 0;
        while (i >= 0 && i < len) {
            char ch = patt.charAt(i);
            if (ch != '%') {
                result.append(ch);
            } else if (i != len) {
                int index = Character.digit(patt.charAt(i + 1), 10);
                if (index == -1) {
                    result.append(patt.charAt(i + 1));
                    i++;
                } else if (index < args.length) {
                    result.append(args[index]);
                    i++;
                }
            }
            i++;
        }
        return result.toString();
    }

    public static String substProp(String propName, String arg) {
        return subst(System.getProperty(propName), arg);
    }

    public static String substProp(String propName, String arg1, String arg2) {
        return subst(System.getProperty(propName), arg1, arg2);
    }

    public static String substProp(String propName, String arg1, String arg2, String arg3) {
        return subst(System.getProperty(propName), arg1, arg2, arg3);
    }

    public static void err(String s) {
        System.err.println(s);
    }

    public static void out(String s) {
        System.out.println(s);
    }

    public static void where() {
        StackTraceElement[] es = new Throwable().getStackTrace();
        for (int i = 1; i < es.length; i++) {
            System.err.println("\t" + es[i].toString());
        }
    }
}
