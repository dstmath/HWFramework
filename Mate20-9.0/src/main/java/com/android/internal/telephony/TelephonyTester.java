package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsExternalCallState;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsCall;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.gsm.SuppServiceNotification;
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
    private static final String ACTION_RESET = "reset";
    private static final String ACTION_TEST_CONFERENCE_EVENT_PACKAGE = "com.android.internal.telephony.TestConferenceEventPackage";
    private static final String ACTION_TEST_DIALOG_EVENT_PACKAGE = "com.android.internal.telephony.TestDialogEventPackage";
    private static final String ACTION_TEST_HANDOVER_FAIL = "com.android.internal.telephony.TestHandoverFail";
    private static final String ACTION_TEST_SERVICE_STATE = "com.android.internal.telephony.TestServiceState";
    private static final String ACTION_TEST_SUPP_SRVC_FAIL = "com.android.internal.telephony.TestSuppSrvcFail";
    private static final String ACTION_TEST_SUPP_SRVC_NOTIFICATION = "com.android.internal.telephony.TestSuppSrvcNotification";
    private static final boolean DBG = true;
    private static final String EXTRA_ACTION = "action";
    private static final String EXTRA_CANPULL = "canPull";
    private static final String EXTRA_CODE = "code";
    private static final String EXTRA_DATA_RAT = "data_rat";
    private static final String EXTRA_DATA_REG_STATE = "data_reg_state";
    private static final String EXTRA_DATA_ROAMING_TYPE = "data_roaming_type";
    private static final String EXTRA_DIALOGID = "dialogId";
    private static final String EXTRA_FAILURE_CODE = "failureCode";
    private static final String EXTRA_FILENAME = "filename";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_SENDPACKAGE = "sendPackage";
    private static final String EXTRA_STARTPACKAGE = "startPackage";
    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_TYPE = "type";
    private static final String EXTRA_VOICE_RAT = "voice_rat";
    private static final String EXTRA_VOICE_REG_STATE = "voice_reg_state";
    private static final String EXTRA_VOICE_ROAMING_TYPE = "voice_roaming_type";
    private static List<ImsExternalCallState> mImsExternalCallStates = null;
    /* access modifiers changed from: private */
    public String LOG_TAG = "TelephonyTester";
    protected BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                try {
                    TelephonyTester telephonyTester = TelephonyTester.this;
                    telephonyTester.log("sIntentReceiver.onReceive: action=" + action);
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
                    } else if (action.equals(TelephonyTester.ACTION_TEST_SUPP_SRVC_FAIL)) {
                        TelephonyTester.this.log("handle test supp svc failed intent");
                        TelephonyTester.this.handleSuppServiceFailedIntent(intent);
                    } else if (action.equals(TelephonyTester.ACTION_TEST_HANDOVER_FAIL)) {
                        TelephonyTester.this.log("handle handover fail test intent");
                        TelephonyTester.this.handleHandoverFailedIntent();
                    } else if (action.equals(TelephonyTester.ACTION_TEST_SUPP_SRVC_NOTIFICATION)) {
                        TelephonyTester.this.log("handle supp service notification test intent");
                        TelephonyTester.this.sendTestSuppServiceNotification(intent);
                    } else if (action.equals(TelephonyTester.ACTION_TEST_SERVICE_STATE)) {
                        TelephonyTester.this.log("handle test service state changed intent");
                        Intent unused = TelephonyTester.this.mServiceStateTestIntent = intent;
                        TelephonyTester.this.mPhone.getServiceStateTracker().sendEmptyMessage(2);
                    } else {
                        TelephonyTester telephonyTester2 = TelephonyTester.this;
                        telephonyTester2.log("onReceive: unknown action=" + action);
                    }
                } catch (BadParcelableException e) {
                    Rlog.w(TelephonyTester.this.LOG_TAG, e);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Phone mPhone;
    /* access modifiers changed from: private */
    public Intent mServiceStateTestIntent;

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
                filter.addAction(ACTION_TEST_SUPP_SRVC_FAIL);
                filter.addAction(ACTION_TEST_HANDOVER_FAIL);
                filter.addAction(ACTION_TEST_SUPP_SRVC_NOTIFICATION);
                mImsExternalCallStates = new ArrayList();
            } else {
                filter.addAction(ACTION_TEST_SERVICE_STATE);
                log("register for intent action=com.android.internal.telephony.TestServiceState");
            }
            phone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone.getHandler());
        }
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        if (Build.IS_DEBUGGABLE) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    public void handleSuppServiceFailedIntent(Intent intent) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone;
        if (imsPhone != null) {
            imsPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.values()[intent.getIntExtra(EXTRA_FAILURE_CODE, 0)]);
        }
    }

    /* access modifiers changed from: private */
    public void handleHandoverFailedIntent() {
        ImsPhone imsPhone = (ImsPhone) this.mPhone;
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

    /* access modifiers changed from: private */
    public void handleTestConferenceEventPackage(Context context, String fileName) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone;
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

    /* access modifiers changed from: private */
    public void handleTestDialogEventPackageIntent(Intent intent) {
        ImsPhone imsPhone = (ImsPhone) this.mPhone;
        if (imsPhone != null) {
            ImsExternalCallTracker externalCallTracker = imsPhone.getExternalCallTracker();
            if (externalCallTracker != null) {
                if (intent.hasExtra(EXTRA_STARTPACKAGE)) {
                    mImsExternalCallStates.clear();
                } else if (intent.hasExtra(EXTRA_SENDPACKAGE)) {
                    externalCallTracker.refreshExternalCallState(mImsExternalCallStates);
                    mImsExternalCallStates.clear();
                } else if (intent.hasExtra(EXTRA_DIALOGID)) {
                    ImsExternalCallState imsExternalCallState = new ImsExternalCallState(intent.getIntExtra(EXTRA_DIALOGID, 0), Uri.parse(intent.getStringExtra(EXTRA_NUMBER)), intent.getBooleanExtra(EXTRA_CANPULL, true), intent.getIntExtra(EXTRA_STATE, 1), 2, false);
                    mImsExternalCallStates.add(imsExternalCallState);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendTestSuppServiceNotification(Intent intent) {
        if (intent.hasExtra(EXTRA_CODE) && intent.hasExtra(EXTRA_TYPE)) {
            int code = intent.getIntExtra(EXTRA_CODE, -1);
            int type = intent.getIntExtra(EXTRA_TYPE, -1);
            ImsPhone imsPhone = (ImsPhone) this.mPhone;
            if (imsPhone != null) {
                log("Test supp service notification:" + code);
                SuppServiceNotification suppServiceNotification = new SuppServiceNotification();
                suppServiceNotification.code = code;
                suppServiceNotification.notificationType = type;
                imsPhone.notifySuppSvcNotification(suppServiceNotification);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void overrideServiceState(ServiceState ss) {
        if (this.mServiceStateTestIntent != null && ss != null) {
            if (!this.mServiceStateTestIntent.hasExtra(EXTRA_ACTION) || !ACTION_RESET.equals(this.mServiceStateTestIntent.getStringExtra(EXTRA_ACTION))) {
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_VOICE_REG_STATE)) {
                    ss.setVoiceRegState(this.mServiceStateTestIntent.getIntExtra(EXTRA_VOICE_REG_STATE, 4));
                    log("Override voice reg state with " + ss.getVoiceRegState());
                }
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_DATA_REG_STATE)) {
                    ss.setDataRegState(this.mServiceStateTestIntent.getIntExtra(EXTRA_DATA_REG_STATE, 4));
                    log("Override data reg state with " + ss.getDataRegState());
                }
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_VOICE_RAT)) {
                    ss.setRilVoiceRadioTechnology(this.mServiceStateTestIntent.getIntExtra(EXTRA_VOICE_RAT, 0));
                    log("Override voice rat with " + ss.getRilVoiceRadioTechnology());
                }
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_DATA_RAT)) {
                    ss.setRilDataRadioTechnology(this.mServiceStateTestIntent.getIntExtra(EXTRA_DATA_RAT, 0));
                    log("Override data rat with " + ss.getRilDataRadioTechnology());
                }
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_VOICE_ROAMING_TYPE)) {
                    ss.setVoiceRoamingType(this.mServiceStateTestIntent.getIntExtra(EXTRA_VOICE_ROAMING_TYPE, 1));
                    log("Override voice roaming type with " + ss.getVoiceRoamingType());
                }
                if (this.mServiceStateTestIntent.hasExtra(EXTRA_DATA_ROAMING_TYPE)) {
                    ss.setDataRoamingType(this.mServiceStateTestIntent.getIntExtra(EXTRA_DATA_ROAMING_TYPE, 1));
                    log("Override data roaming type with " + ss.getDataRoamingType());
                }
                return;
            }
            log("Service state override reset");
        }
    }
}
