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
import java.util.ArrayList;
import java.util.LinkedList;

public final class Helper {
    private static final String TAG = "AutofillHelper";
    public static boolean sDebug = false;
    public static Boolean sFullScreenMode = null;
    static int sPartitionMaxCount = 10;
    public static boolean sVerbose = false;
    public static int sVisibleDatasetsMaxCount = 3;

    private interface ViewNodeFilter {
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
            private final /* synthetic */ AutofillId f$0;

            {
                this.f$0 = r1;
            }

            public final boolean matches(AssistStructure.ViewNode viewNode) {
                return this.f$0.equals(viewNode.getAutofillId());
            }
        });
    }

    private static AssistStructure.ViewNode findViewNode(AssistStructure structure, ViewNodeFilter filter) {
        LinkedList<AssistStructure.ViewNode> nodesToProcess = new LinkedList<>();
        int numWindowNodes = structure.getWindowNodeCount();
        for (int i = 0; i < numWindowNodes; i++) {
            nodesToProcess.add(structure.getWindowNodeAt(i).getRootViewNode());
        }
        while (nodesToProcess.isEmpty() == 0) {
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
            private final /* synthetic */ String[] f$0;

            {
                this.f$0 = r1;
            }

            public final boolean matches(AssistStructure.ViewNode viewNode) {
                return ArrayUtils.contains(this.f$0, viewNode.getIdEntry());
            }
        });
        if (urlBarNode != null) {
            String domain = urlBarNode.getText().toString();
            if (domain.isEmpty()) {
                if (sDebug) {
                    Slog.d(TAG, "sanitizeUrlBar(): empty on " + urlBarNode.getIdEntry());
                }
                return null;
            }
            urlBarNode.setWebDomain(domain);
            if (sDebug) {
                Slog.d(TAG, "sanitizeUrlBar(): id=" + urlBarNode.getIdEntry() + ", domain=" + urlBarNode.getWebDomain());
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
}
