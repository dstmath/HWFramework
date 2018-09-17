package tmsdkobf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class qk {
    private static Map<String, Pattern> Nx = null;

    public static Pattern cT(String str) {
        if (Nx == null) {
            Nx = new HashMap();
        }
        Pattern pattern = (Pattern) Nx.get(str);
        if (pattern != null) {
            return pattern;
        }
        pattern = Pattern.compile(str);
        Nx.put(str, pattern);
        return pattern;
    }

    public static int jq() {
        if (Nx == null) {
            return 0;
        }
        int size = Nx.size();
        Nx = null;
        return size;
    }
}
