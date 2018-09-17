package android.test;

import android.app.Activity;

@Deprecated
public abstract class SingleLaunchActivityTestCase<T extends Activity> extends InstrumentationTestCase {
    private static Activity sActivity;
    private static boolean sActivityLaunchedFlag;
    private static int sTestCaseCounter;
    Class<T> mActivityClass;
    String mPackage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.test.SingleLaunchActivityTestCase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.test.SingleLaunchActivityTestCase.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.test.SingleLaunchActivityTestCase.<clinit>():void");
    }

    public SingleLaunchActivityTestCase(String pkg, Class<T> activityClass) {
        this.mPackage = pkg;
        this.mActivityClass = activityClass;
        sTestCaseCounter++;
    }

    public T getActivity() {
        return sActivity;
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!sActivityLaunchedFlag) {
            getInstrumentation().setInTouchMode(false);
            sActivity = launchActivity(this.mPackage, this.mActivityClass, null);
            sActivityLaunchedFlag = true;
        }
    }

    protected void tearDown() throws Exception {
        sTestCaseCounter--;
        if (sTestCaseCounter == 0) {
            sActivity.finish();
        }
        super.tearDown();
    }

    public void testActivityTestCaseSetUpProperly() throws Exception {
        assertNotNull("activity should be launched successfully", sActivity);
    }
}
