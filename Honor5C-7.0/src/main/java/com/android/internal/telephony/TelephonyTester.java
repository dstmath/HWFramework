package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.telephony.Rlog;
import com.android.ims.ImsCall;
import com.android.ims.ImsConferenceState;
import com.android.ims.ImsExternalCallState;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.test.TestConferenceEventPackageParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TelephonyTester {
    private static final String ACTION_TEST_CONFERENCE_EVENT_PACKAGE = "com.android.internal.telephony.TestConferenceEventPackage";
    private static final String ACTION_TEST_DIALOG_EVENT_PACKAGE = "com.android.internal.telephony.TestDialogEventPackage";
    private static final boolean DBG = true;
    private static final String EXTRA_CANPULL = "canPull";
    private static final String EXTRA_DIALOGID = "dialogId";
    private static final String EXTRA_FILENAME = "filename";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_SENDPACKAGE = "sendPackage";
    private static final String EXTRA_STARTPACKAGE = "startPackage";
    private static final String EXTRA_STATE = "state";
    private static List<ImsExternalCallState> mImsExternalCallStates;
    private String LOG_TAG;
    protected BroadcastReceiver mIntentReceiver;
    private Phone mPhone;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.TelephonyTester.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.TelephonyTester.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.TelephonyTester.<clinit>():void");
    }

    TelephonyTester(Phone phone) {
        this.LOG_TAG = "TelephonyTester";
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                TelephonyTester.this.log("sIntentReceiver.onReceive: action=" + action);
                if (action.equals(TelephonyTester.this.mPhone.getActionDetached())) {
                    TelephonyTester.this.log("simulate detaching");
                    TelephonyTester.this.mPhone.getServiceStateTracker().mDetachedRegistrants.notifyRegistrants();
                } else if (action.equals(TelephonyTester.this.mPhone.getActionAttached())) {
                    TelephonyTester.this.log("simulate attaching");
                    TelephonyTester.this.mPhone.getServiceStateTracker().mAttachedRegistrants.notifyRegistrants();
                } else if (action.equals(TelephonyTester.ACTION_TEST_CONFERENCE_EVENT_PACKAGE)) {
                    TelephonyTester.this.log("inject simulated conference event package");
                    TelephonyTester.this.handleTestConferenceEventPackage(context, intent.getStringExtra(TelephonyTester.EXTRA_FILENAME));
                } else if (action.equals(TelephonyTester.ACTION_TEST_DIALOG_EVENT_PACKAGE)) {
                    TelephonyTester.this.log("handle test dialog event package intent");
                    TelephonyTester.this.handleTestDialogEventPackageIntent(intent);
                } else {
                    TelephonyTester.this.log("onReceive: unknown action=" + action);
                }
            }
        };
        this.mPhone = phone;
        if (Build.IS_DEBUGGABLE) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(this.mPhone.getActionDetached());
            log("register for intent action=" + this.mPhone.getActionDetached());
            filter.addAction(this.mPhone.getActionAttached());
            log("register for intent action=" + this.mPhone.getActionAttached());
            if (this.mPhone.getPhoneType() == 5) {
                log("register for intent action=com.android.internal.telephony.TestConferenceEventPackage");
                filter.addAction(ACTION_TEST_CONFERENCE_EVENT_PACKAGE);
                filter.addAction(ACTION_TEST_DIALOG_EVENT_PACKAGE);
                mImsExternalCallStates = new ArrayList();
            }
            phone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone.getHandler());
        }
    }

    void dispose() {
        if (Build.IS_DEBUGGABLE) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    private void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    private void handleTestConferenceEventPackage(Context context, String fileName) {
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone != null) {
            ImsPhoneCall imsPhoneCall = imsPhone.getForegroundCall();
            if (imsPhoneCall != null) {
                ImsCall imsCall = imsPhoneCall.getImsCall();
                if (imsCall != null) {
                    File packageFile = new File(context.getFilesDir(), fileName);
                    try {
                        ImsConferenceState imsConferenceState = new TestConferenceEventPackageParser(new FileInputStream(packageFile)).parse();
                        if (imsConferenceState != null) {
                            imsCall.conferenceStateUpdated(imsConferenceState);
                        }
                    } catch (FileNotFoundException e) {
                        log("Test conference event package file not found: " + packageFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void handleTestDialogEventPackageIntent(Intent intent) {
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone != null) {
            ImsExternalCallTracker externalCallTracker = imsPhone.getExternalCallTracker();
            if (externalCallTracker != null) {
                if (intent.hasExtra(EXTRA_STARTPACKAGE)) {
                    mImsExternalCallStates.clear();
                } else if (intent.hasExtra(EXTRA_SENDPACKAGE)) {
                    externalCallTracker.refreshExternalCallState(mImsExternalCallStates);
                    mImsExternalCallStates.clear();
                } else if (intent.hasExtra(EXTRA_DIALOGID)) {
                    mImsExternalCallStates.add(new ImsExternalCallState(intent.getIntExtra(EXTRA_DIALOGID, 0), Uri.parse(intent.getStringExtra(EXTRA_NUMBER)), intent.getBooleanExtra(EXTRA_CANPULL, DBG), intent.getIntExtra(EXTRA_STATE, 1), 2, false));
                }
            }
        }
    }
}
