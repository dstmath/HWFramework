package com.android.internal.policy;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class HwPhoneLayoutInflater extends PhoneLayoutInflater {
    private static final String TAG = "HwPhoneLayoutInflater";
    private static final String[] sAndroidClassList = null;
    private static final String[] sHwClassPrefixList = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.HwPhoneLayoutInflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.HwPhoneLayoutInflater.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.HwPhoneLayoutInflater.<clinit>():void");
    }

    private static boolean isAndoridClass(String name) {
        for (String clazz : sAndroidClassList) {
            if (name.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public HwPhoneLayoutInflater(Context context) {
        super(context);
    }

    protected HwPhoneLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        if (HwWidgetFactory.isHwTheme(getContext())) {
            if (isAndoridClass(name)) {
                return super.onCreateView(name, attrs);
            }
            String[] strArr = sHwClassPrefixList;
            int i = 0;
            int length = strArr.length;
            while (i < length) {
                try {
                    View view = createView(name, strArr[i], attrs);
                    if (view != null) {
                        return view;
                    }
                    i++;
                } catch (ClassNotFoundException e) {
                    Log.w(TAG, "onCreateView : ClassNotFoundException, In this case we want to let the base class take a crack at it");
                }
            }
        }
        return super.onCreateView(name, attrs);
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new HwPhoneLayoutInflater(this, newContext);
    }
}
