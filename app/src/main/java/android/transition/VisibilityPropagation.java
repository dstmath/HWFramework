package android.transition;

import android.view.View;

public abstract class VisibilityPropagation extends TransitionPropagation {
    private static final String PROPNAME_VIEW_CENTER = "android:visibilityPropagation:center";
    private static final String PROPNAME_VISIBILITY = "android:visibilityPropagation:visibility";
    private static final String[] VISIBILITY_PROPAGATION_VALUES = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.VisibilityPropagation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.VisibilityPropagation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.VisibilityPropagation.<clinit>():void");
    }

    public void captureValues(TransitionValues values) {
        View view = values.view;
        Integer visibility = (Integer) values.values.get("android:visibility:visibility");
        if (visibility == null) {
            visibility = Integer.valueOf(view.getVisibility());
        }
        values.values.put(PROPNAME_VISIBILITY, visibility);
        loc = new int[2];
        view.getLocationOnScreen(loc);
        loc[0] = loc[0] + Math.round(view.getTranslationX());
        loc[0] = loc[0] + (view.getWidth() / 2);
        loc[1] = loc[1] + Math.round(view.getTranslationY());
        loc[1] = loc[1] + (view.getHeight() / 2);
        values.values.put(PROPNAME_VIEW_CENTER, loc);
    }

    public String[] getPropagationProperties() {
        return VISIBILITY_PROPAGATION_VALUES;
    }

    public int getViewVisibility(TransitionValues values) {
        if (values == null) {
            return 8;
        }
        Integer visibility = (Integer) values.values.get(PROPNAME_VISIBILITY);
        if (visibility == null) {
            return 8;
        }
        return visibility.intValue();
    }

    public int getViewX(TransitionValues values) {
        return getViewCoordinate(values, 0);
    }

    public int getViewY(TransitionValues values) {
        return getViewCoordinate(values, 1);
    }

    private static int getViewCoordinate(TransitionValues values, int coordinateIndex) {
        if (values == null) {
            return -1;
        }
        int[] coordinates = (int[]) values.values.get(PROPNAME_VIEW_CENTER);
        if (coordinates == null) {
            return -1;
        }
        return coordinates[coordinateIndex];
    }
}
