package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;

public final class AbilitySliceRoute {
    private static final LogLabel LABEL = LogLabel.create();
    private Map<String, String> actionEntrys = new HashMap();
    private String mainEntry = null;

    public void setMainRoute(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("The entry is illegal.");
        }
        this.mainEntry = str;
    }

    public String getMainRoute() {
        return this.mainEntry;
    }

    public void addActionRoute(String str, String str2) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("The action is illegal.");
        } else if (str2 == null || str2.isEmpty()) {
            throw new IllegalArgumentException("The entry is illegal.");
        } else {
            this.actionEntrys.put(str, str2);
        }
    }

    public String matchRoute(String str) {
        if (str != null && !str.isEmpty()) {
            return this.actionEntrys.get(str);
        }
        throw new IllegalArgumentException("The action is illegal.");
    }

    public void clear() {
        Log.info(LABEL, "All entry clear.", new Object[0]);
        this.mainEntry = null;
        this.actionEntrys.clear();
    }

    /* access modifiers changed from: package-private */
    public void dumpSliceRoute(String str, PrintWriter printWriter) {
        printWriter.println(str + "Ability main entry:");
        printWriter.print(Ability.PREFIX + str);
        printWriter.println(this.mainEntry);
        printWriter.println(str + "Ability action route:");
        if (this.actionEntrys.isEmpty()) {
            printWriter.print(Ability.PREFIX + str);
            printWriter.println("none");
            return;
        }
        for (Map.Entry<String, String> entry : this.actionEntrys.entrySet()) {
            printWriter.print(Ability.PREFIX + str);
            printWriter.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
