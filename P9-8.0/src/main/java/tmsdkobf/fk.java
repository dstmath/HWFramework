package tmsdkobf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class fk {
    public static String f(ArrayList<String> listTpye) {
        int i;
        StringBuffer sb = new StringBuffer();
        for (i = 0; i < listTpye.size(); i++) {
            listTpye.set(i, A((String) listTpye.get(i)));
        }
        Collections.reverse(listTpye);
        for (i = 0; i < listTpye.size(); i++) {
            String type = (String) listTpye.get(i);
            if (type.equals("list")) {
                listTpye.set(i - 1, "<" + ((String) listTpye.get(i - 1)));
                listTpye.set(0, new StringBuilder(String.valueOf((String) listTpye.get(0))).append(">").toString());
            } else if (type.equals("map")) {
                listTpye.set(i - 1, "<" + ((String) listTpye.get(i - 1)) + ",");
                listTpye.set(0, new StringBuilder(String.valueOf((String) listTpye.get(0))).append(">").toString());
            } else if (type.equals("Array")) {
                listTpye.set(i - 1, "<" + ((String) listTpye.get(i - 1)));
                listTpye.set(0, new StringBuilder(String.valueOf((String) listTpye.get(0))).append(">").toString());
            }
        }
        Collections.reverse(listTpye);
        Iterator it = listTpye.iterator();
        while (it.hasNext()) {
            sb.append((String) it.next());
        }
        return sb.toString();
    }

    public static String A(String srcType) {
        if (srcType.equals("java.lang.Integer") || srcType.equals("int")) {
            return "int32";
        }
        if (srcType.equals("java.lang.Boolean") || srcType.equals("boolean")) {
            return "bool";
        }
        if (srcType.equals("java.lang.Byte") || srcType.equals("byte")) {
            return "char";
        }
        if (srcType.equals("java.lang.Double") || srcType.equals("double")) {
            return "double";
        }
        if (srcType.equals("java.lang.Float") || srcType.equals("float")) {
            return "float";
        }
        if (srcType.equals("java.lang.Long") || srcType.equals("long")) {
            return "int64";
        }
        if (srcType.equals("java.lang.Short") || srcType.equals("short")) {
            return "short";
        }
        if (srcType.equals("java.lang.Character")) {
            throw new IllegalArgumentException("can not support java.lang.Character");
        } else if (srcType.equals("java.lang.String")) {
            return "string";
        } else {
            if (srcType.equals("java.util.List")) {
                return "list";
            }
            if (srcType.equals("java.util.Map")) {
                return "map";
            }
            return srcType;
        }
    }
}
