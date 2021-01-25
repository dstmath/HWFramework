package com.android.server.autofill;

import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.metrics.LogMaker;
import android.service.autofill.Dataset;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.view.WindowManager;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import com.android.internal.util.ArrayUtils;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

public final class Helper {
    private static final String TAG = "AutofillHelper";
    public static boolean sDebug = false;
    public static Boolean sFullScreenMode = null;
    public static boolean sVerbose = false;

    /* access modifiers changed from: private */
    public interface ViewNodeFilter {
        boolean matches(AssistStructure.ViewNode viewNode);
    }

    private Helper() {
        throw new UnsupportedOperationException("contains static members only");
    }

    static AutofillId[] toArray(ArraySet<AutofillId> set) {
        if (set == null) {
            return null;
        }
        AutofillId[] array = new AutofillId[set.size()];
        for (int i = 0; i < set.size(); i++) {
            array[i] = set.valueAt(i);
        }
        return array;
    }

    public static String paramsToString(WindowManager.LayoutParams params) {
        StringBuilder builder = new StringBuilder(25);
        params.dumpDimensions(builder);
        return builder.toString();
    }

    static ArrayMap<AutofillId, AutofillValue> getFields(Dataset dataset) {
        ArrayList<AutofillId> ids = dataset.getFieldIds();
        ArrayList<AutofillValue> values = dataset.getFieldValues();
        int size = ids == null ? 0 : ids.size();
        ArrayMap<AutofillId, AutofillValue> fields = new ArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            fields.put(ids.get(i), values.get(i));
        }
        return fields;
    }

    private static LogMaker newLogMaker(int category, String servicePackageName, int sessionId, boolean compatMode) {
        LogMaker log = new LogMaker(category).addTaggedData(908, servicePackageName).addTaggedData(1456, Integer.toString(sessionId));
        if (compatMode) {
            log.addTaggedData(1414, 1);
        }
        return log;
    }

    public static LogMaker newLogMaker(int category, String packageName, String servicePackageName, int sessionId, boolean compatMode) {
        return newLogMaker(category, servicePackageName, sessionId, compatMode).setPackageName(packageName);
    }

    public static LogMaker newLogMaker(int category, ComponentName componentName, String servicePackageName, int sessionId, boolean compatMode) {
        return newLogMaker(category, servicePackageName, sessionId, compatMode).setComponentName(componentName);
    }

    public static void printlnRedactedText(PrintWriter pw, CharSequence text) {
        if (text == null) {
            pw.println("null");
            return;
        }
        pw.print(text.length());
        pw.println("_chars");
    }

    public static AssistStructure.ViewNode findViewNodeByAutofillId(AssistStructure structure, AutofillId autofillId) {
        return findViewNode(structure, new ViewNodeFilter(autofillId) {
            /* class com.android.server.autofill.$$Lambda$Helper$nK3g_oXXf8NGajcUf0W5JsQzf3w */
            private final /* synthetic */ AutofillId f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.autofill.Helper.ViewNodeFilter
            public final boolean matches(AssistStructure.ViewNode viewNode) {
                return this.f$0.equals(viewNode.getAutofillId());
            }
        });
    }

    private static AssistStructure.ViewNode findViewNode(AssistStructure structure, ViewNodeFilter filter) {
        ArrayDeque<AssistStructure.ViewNode> nodesToProcess = new ArrayDeque<>();
        int numWindowNodes = structure.getWindowNodeCount();
        for (int i = 0; i < numWindowNodes; i++) {
            nodesToProcess.add(structure.getWindowNodeAt(i).getRootViewNode());
        }
        while (!nodesToProcess.isEmpty()) {
            AssistStructure.ViewNode node = nodesToProcess.removeFirst();
            if (filter.matches(node)) {
                return node;
            }
            for (int i2 = 0; i2 < node.getChildCount(); i2++) {
                nodesToProcess.addLast(node.getChildAt(i2));
            }
        }
        return null;
    }

    public static AssistStructure.ViewNode sanitizeUrlBar(AssistStructure structure, String[] urlBarIds) {
        AssistStructure.ViewNode urlBarNode = findViewNode(structure, new ViewNodeFilter(urlBarIds) {
            /* class com.android.server.autofill.$$Lambda$Helper$laLKWmsGqkFIaRXW5rR6_s66Vsw */
            private final /* synthetic */ String[] f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.autofill.Helper.ViewNodeFilter
            public final boolean matches(AssistStructure.ViewNode viewNode) {
                return ArrayUtils.contains(this.f$0, viewNode.getIdEntry());
            }
        });
        if (urlBarNode != null) {
            String domain = urlBarNode.getText().toString();
            if (!domain.isEmpty()) {
                urlBarNode.setWebDomain(domain);
                if (sDebug) {
                    Slog.d(TAG, "sanitizeUrlBar(): id=" + urlBarNode.getIdEntry() + ", domain=" + urlBarNode.getWebDomain());
                }
            } else if (!sDebug) {
                return null;
            } else {
                Slog.d(TAG, "sanitizeUrlBar(): empty on " + urlBarNode.getIdEntry());
                return null;
            }
        }
        return urlBarNode;
    }

    static int getNumericValue(LogMaker log, int tag) {
        Object value = log.getTaggedData(tag);
        if (!(value instanceof Number)) {
            return 0;
        }
        return ((Number) value).intValue();
    }

    static ArrayList<AutofillId> getAutofillIds(AssistStructure structure, boolean autofillableOnly) {
        ArrayList<AutofillId> ids = new ArrayList<>();
        int size = structure.getWindowNodeCount();
        for (int i = 0; i < size; i++) {
            addAutofillableIds(structure.getWindowNodeAt(i).getRootViewNode(), ids, autofillableOnly);
        }
        return ids;
    }

    private static void addAutofillableIds(AssistStructure.ViewNode node, ArrayList<AutofillId> ids, boolean autofillableOnly) {
        if (!autofillableOnly || node.getAutofillType() != 0) {
            ids.add(node.getAutofillId());
        }
        int size = node.getChildCount();
        for (int i = 0; i < size; i++) {
            addAutofillableIds(node.getChildAt(i), ids, autofillableOnly);
        }
    }
}
