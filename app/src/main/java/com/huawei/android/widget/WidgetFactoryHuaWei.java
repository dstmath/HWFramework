package com.huawei.android.widget;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater.Factory;
import android.view.View;

public class WidgetFactoryHuaWei implements Factory {
    private static final boolean DBG = false;
    private static final String TAG = "WidgetFactoryHuaWei";
    private static final Class[] mConstructorSignature = null;
    private Context mContext;
    private String mPackageName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.widget.WidgetFactoryHuaWei.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.widget.WidgetFactoryHuaWei.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.widget.WidgetFactoryHuaWei.<clinit>():void");
    }

    public WidgetFactoryHuaWei(Context context, String packageName) {
        if (packageName == null || context == null) {
            throw new NullPointerException("Both of packageName and context can not be null");
        }
        this.mContext = context;
        this.mPackageName = packageName;
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (-1 == name.indexOf(".")) {
            return null;
        }
        try {
            return (View) this.mContext.createPackageContext(this.mPackageName, 3).getClassLoader().loadClass(name).getConstructor(mConstructorSignature).newInstance(new Object[]{context, attrs});
        } catch (NameNotFoundException e1) {
            Log.e(TAG, "packageName is " + this.mPackageName + " exception is " + e1.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating class " + name + " Exception is " + e.getMessage());
            return null;
        }
    }
}
