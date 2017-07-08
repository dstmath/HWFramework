package android.widget;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.Field;

public class HwSpinner extends Spinner {
    private static final String TAG = "HwSpinner";
    private static Field privatePopUpfield;
    private int preferTopPosition;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.HwSpinner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.HwSpinner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.HwSpinner.<clinit>():void");
    }

    public HwSpinner(Context context) {
        super(context);
        this.preferTopPosition = -1;
    }

    public boolean performClick() {
        boolean handled = super.performClick();
        if (handled) {
            try {
                Object privatePopupWindow = privatePopUpfield.get(this);
                if (privatePopupWindow != null && (privatePopupWindow instanceof ListPopupWindow)) {
                    ListPopupWindow listPopupWindow = (ListPopupWindow) privatePopupWindow;
                    if (listPopupWindow.isShowing() && this.preferTopPosition >= 0) {
                        listPopupWindow.setSelection(this.preferTopPosition);
                    }
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, "get IllegalAccessException | IllegalArgumentException when excute filed : mPopup");
            }
        }
        return handled;
    }

    public void setPreferTopPosition(int position) {
        this.preferTopPosition = position;
    }
}
