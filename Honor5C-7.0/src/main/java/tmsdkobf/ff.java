package tmsdkobf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ff {
    public static String j(ArrayList<String> arrayList) {
        int i;
        StringBuffer stringBuffer = new StringBuffer();
        for (i = 0; i < arrayList.size(); i++) {
            arrayList.set(i, Y((String) arrayList.get(i)));
        }
        Collections.reverse(arrayList);
        for (i = 0; i < arrayList.size(); i++) {
            String str = (String) arrayList.get(i);
            if (str.equals("list")) {
                arrayList.set(i - 1, "<" + ((String) arrayList.get(i - 1)));
                arrayList.set(0, new StringBuilder(String.valueOf((String) arrayList.get(0))).append(">").toString());
            } else if (str.equals("map")) {
                arrayList.set(i - 1, "<" + ((String) arrayList.get(i - 1)) + ",");
                arrayList.set(0, new StringBuilder(String.valueOf((String) arrayList.get(0))).append(">").toString());
            } else if (str.equals("Array")) {
                arrayList.set(i - 1, "<" + ((String) arrayList.get(i - 1)));
                arrayList.set(0, new StringBuilder(String.valueOf((String) arrayList.get(0))).append(">").toString());
            }
        }
        Collections.reverse(arrayList);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            stringBuffer.append((String) it.next());
        }
        return stringBuffer.toString();
    }

    public static String Y(String str) {
        if (str.equals("java.lang.Integer") || str.equals("int")) {
            return "int32";
        }
        if (str.equals("java.lang.Boolean") || str.equals("boolean")) {
            return "bool";
        }
        if (str.equals("java.lang.Byte") || str.equals("byte")) {
            return "char";
        }
        if (str.equals("java.lang.Double") || str.equals("double")) {
            return "double";
        }
        if (str.equals("java.lang.Float") || str.equals("float")) {
            return "float";
        }
        if (str.equals("java.lang.Long") || str.equals("long")) {
            return "int64";
        }
        if (str.equals("java.lang.Short") || str.equals("short")) {
            return "short";
        }
        if (str.equals("java.lang.Character")) {
            throw new IllegalArgumentException("can not support java.lang.Character");
        } else if (str.equals("java.lang.String")) {
            return "string";
        } else {
            if (str.equals("java.util.List")) {
                return "list";
            }
            if (str.equals("java.util.Map")) {
                return "map";
            }
            return str;
        }
    }
}
