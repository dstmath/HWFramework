package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.telephony.Rlog;
import com.android.ims.ImsCall;
import com.android.ims.ImsConferenceState;
import com.android.ims.ImsExternalCallState;
import com.android.ims.ImsReasonInfo;
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
    private static final String ACTION_TEST_HANDOVER_FAIL = "com.android.internal.telephony.TestHandoverFail";
    private static final boolean DBG = true;
    private static final String EXTRA_CANPULL = "canPull";
    private static final String EXTRA_DIALOGID = "dialogId";
    private static final String EXTRA_FILENAME = "filename";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_SENDPACKAGE = "sendPackage";
    private static final String EXTRA_STARTPACKAGE = "startPackage";
    private static final String EXTRA_STATE = "state";
    private static List<ImsExternalCallState> mImsExternalCallStates = null;
    private String LOG_TAG = "TelephonyTester";
    protected BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
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
                } else if (action.equals(TelephonyTester.ACTION_TEST_HANDOVER_FAIL)) {
                    TelephonyTester.this.log("handle handover fail test intent");
                    TelephonyTester.this.handleHandoverFailedIntent();
                } else {
                    TelephonyTester.this.log("onReceive: unknown action=" + action);
                }
            } catch (BadParcelableException e) {
                Rlog.w(TelephonyTester.this.LOG_TAG, e);
            }
        }
    };
    private Phone mPhone;

    TelephonyTester(Phone phone) {
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
                filter.addAction(ACTION_TEST_HANDOVER_FAIL);
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

    private void handleHandoverFailedIntent() {
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone != null) {
            ImsPhoneCall imsPhoneCall = imsPhone.getForegroundCall();
            if (imsPhoneCall != null) {
                ImsCall imsCall = imsPhoneCall.getImsCall();
                if (imsCall != null) {
                    imsCall.getImsCallSessionListenerProxy().callSessionHandoverFailed(imsCall.getCallSession(), 14, 18, new ImsReasonInfo());
                }
            }
        }
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
                    mImsExternalCallStates.add(new ImsExternalCallState(intent.getIntExtra(EXTRA_DIALOGID, 0), Uri.parse(intent.getStringExtra(EXTRA_NUMBER)), intent.getBooleanExtra(EXTRA_CANPULL, true), intent.getIntExtra(EXTRA_STATE, 1), 2, false));
                }
            }
        }
    }
}
