package com.android.internal.telephony;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.ServiceManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import com.android.internal.telephony.imsphone.ImsExternalCallTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TelephonyComponentFactory {
    private static final String TAG = TelephonyComponentFactory.class.getSimpleName();
    static final boolean USE_NEW_NITZ_STATE_MACHINE = true;
    private static TelephonyComponentFactory sInstance;
    private InjectedComponents mInjectedComponents;

    /* access modifiers changed from: private */
    public static class InjectedComponents {
        private static final String ATTRIBUTE_JAR = "jar";
        private static final String ATTRIBUTE_PACKAGE = "package";
        private static final String PRODUCT = "/product/";
        private static final String SYSTEM = "/system/";
        private static final String TAG_COMPONENT = "component";
        private static final String TAG_COMPONENTS = "components";
        private static final String TAG_INJECTION = "injection";
        private final Set<String> mComponentNames;
        private TelephonyComponentFactory mInjectedInstance;
        private String mJarPath;
        private String mPackageName;

        private InjectedComponents() {
            this.mComponentNames = new HashSet();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getValidatedPaths() {
            if (TextUtils.isEmpty(this.mPackageName) || TextUtils.isEmpty(this.mJarPath)) {
                return null;
            }
            return (String) Arrays.stream(this.mJarPath.split(File.pathSeparator)).filter($$Lambda$TelephonyComponentFactory$InjectedComponents$09rMKC8001jAR0zFrzzlPx26Xjs.INSTANCE).filter($$Lambda$TelephonyComponentFactory$InjectedComponents$UYUq9z2WZwxqOLXquU0tTNN9wAs.INSTANCE).distinct().collect(Collectors.joining(File.pathSeparator));
        }

        static /* synthetic */ boolean lambda$getValidatedPaths$0(String s) {
            return s.startsWith(SYSTEM) || s.startsWith(PRODUCT);
        }

        static /* synthetic */ boolean lambda$getValidatedPaths$1(String s) {
            try {
                if ((Os.statvfs(s).f_flag & ((long) OsConstants.ST_RDONLY)) != 0) {
                    return true;
                }
                return false;
            } catch (ErrnoException e) {
                String str = TelephonyComponentFactory.TAG;
                Rlog.w(str, "Injection jar is not protected , path: " + s + e.getMessage());
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void makeInjectedInstance() {
            String validatedPaths = getValidatedPaths();
            String str = TelephonyComponentFactory.TAG;
            Rlog.d(str, "validated paths: " + validatedPaths);
            if (!TextUtils.isEmpty(validatedPaths)) {
                try {
                    this.mInjectedInstance = (TelephonyComponentFactory) new PathClassLoader(validatedPaths, ClassLoader.getSystemClassLoader()).loadClass(this.mPackageName).newInstance();
                } catch (ClassNotFoundException e) {
                    String str2 = TelephonyComponentFactory.TAG;
                    Rlog.e(str2, "failed: " + e.getMessage());
                } catch (IllegalAccessException | InstantiationException e2) {
                    String str3 = TelephonyComponentFactory.TAG;
                    Rlog.e(str3, "injection failed: " + e2.getMessage());
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isComponentInjected(String componentName) {
            if (this.mInjectedInstance == null) {
                return false;
            }
            return this.mComponentNames.contains(componentName);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseXml(XmlPullParser parser) {
            parseXmlByTag(parser, false, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$nLdppNQT1Bv7QyIU3LwAwVD2K60 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseXml$2$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_INJECTION);
        }

        public /* synthetic */ void lambda$parseXml$2$TelephonyComponentFactory$InjectedComponents(XmlPullParser p) {
            setAttributes(p);
            parseInjection(p);
        }

        private void parseInjection(XmlPullParser parser) {
            parseXmlByTag(parser, false, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$eUdIxJOKoyVP5UmFJtWXBUO93Qk */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseInjection$3$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_COMPONENTS);
        }

        /* access modifiers changed from: private */
        /* renamed from: parseComponents */
        public void lambda$parseInjection$3$TelephonyComponentFactory$InjectedComponents(XmlPullParser parser) {
            parseXmlByTag(parser, true, new Consumer() {
                /* class com.android.internal.telephony.$$Lambda$TelephonyComponentFactory$InjectedComponents$DKjB_mCxFOHomOyKLPFU99Dywc */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TelephonyComponentFactory.InjectedComponents.this.lambda$parseComponents$4$TelephonyComponentFactory$InjectedComponents((XmlPullParser) obj);
                }
            }, TAG_COMPONENT);
        }

        /* access modifiers changed from: private */
        /* renamed from: parseComponent */
        public void lambda$parseComponents$4$TelephonyComponentFactory$InjectedComponents(XmlPullParser parser) {
            try {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        return;
                    }
                    if (type == 3 && parser.getDepth() <= outerDepth) {
                        return;
                    }
                    if (type == 4) {
                        this.mComponentNames.add(parser.getText());
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Rlog.e(TelephonyComponentFactory.TAG, "Failed to parse the component.", e);
            }
        }

        private void parseXmlByTag(XmlPullParser parser, boolean allowDuplicate, Consumer<XmlPullParser> consumer, String tag) {
            try {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1) {
                        return;
                    }
                    if (type == 3 && parser.getDepth() <= outerDepth) {
                        return;
                    }
                    if (type == 2 && tag.equals(parser.getName())) {
                        consumer.accept(parser);
                        if (!allowDuplicate) {
                            return;
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                String str = TelephonyComponentFactory.TAG;
                Rlog.e(str, "Failed to parse or find tag: " + tag, e);
            }
        }

        private void setAttributes(XmlPullParser parser) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String name = parser.getAttributeName(i);
                String value = parser.getAttributeValue(i);
                if (ATTRIBUTE_PACKAGE.equals(name)) {
                    this.mPackageName = value;
                } else if (ATTRIBUTE_JAR.equals(name)) {
                    this.mJarPath = value;
                }
            }
        }
    }

    public static TelephonyComponentFactory getInstance() {
        if (sInstance == null) {
            sInstance = new TelephonyComponentFactory();
        }
        return sInstance;
    }

    public void injectTheComponentFactory(XmlResourceParser parser) {
        if (this.mInjectedComponents != null) {
            Rlog.d(TAG, "Already injected.");
        } else if (parser != null) {
            this.mInjectedComponents = new InjectedComponents();
            this.mInjectedComponents.parseXml(parser);
            this.mInjectedComponents.makeInjectedInstance();
            boolean injectSuccessful = !TextUtils.isEmpty(this.mInjectedComponents.getValidatedPaths());
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Total components injected: ");
            sb.append(injectSuccessful ? this.mInjectedComponents.mComponentNames.size() : 0);
            Rlog.d(str, sb.toString());
        }
    }

    public TelephonyComponentFactory inject(String componentName) {
        InjectedComponents injectedComponents = this.mInjectedComponents;
        if (injectedComponents == null || !injectedComponents.isComponentInjected(componentName)) {
            return sInstance;
        }
        return this.mInjectedComponents.mInjectedInstance;
    }

    public GsmCdmaCallTracker makeGsmCdmaCallTracker(GsmCdmaPhone phone) {
        return new GsmCdmaCallTracker(phone);
    }

    public SmsStorageMonitor makeSmsStorageMonitor(Phone phone) {
        return new SmsStorageMonitor(phone);
    }

    public SmsUsageMonitor makeSmsUsageMonitor(Context context, Phone phone) {
        return new SmsUsageMonitor(context, phone);
    }

    public ServiceStateTracker makeServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        if (phone == null || !VSimUtilsInner.isVSimSub(phone.getPhoneId())) {
            return new ServiceStateTracker(phone, ci);
        }
        return VSimUtilsInner.makeVSimServiceStateTracker(phone, ci);
    }

    public EmergencyNumberTracker makeEmergencyNumberTracker(Phone phone, CommandsInterface ci) {
        return new EmergencyNumberTracker(phone, ci);
    }

    public NitzStateMachine makeNitzStateMachine(GsmCdmaPhone phone) {
        return new NewNitzStateMachine(phone);
    }

    public SimActivationTracker makeSimActivationTracker(Phone phone) {
        return new SimActivationTracker(phone);
    }

    public DcTracker makeDcTracker(Phone phone, int transportType) {
        return new DcTracker(phone, transportType);
    }

    public CarrierSignalAgent makeCarrierSignalAgent(Phone phone) {
        return new CarrierSignalAgent(phone);
    }

    public CarrierActionAgent makeCarrierActionAgent(Phone phone) {
        return new CarrierActionAgent(phone);
    }

    public CarrierResolver makeCarrierResolver(Phone phone) {
        return new CarrierResolver(phone);
    }

    public IccPhoneBookInterfaceManager makeIccPhoneBookInterfaceManager(Phone phone) {
        return new IccPhoneBookInterfaceManager(phone);
    }

    public IccSmsInterfaceManager makeIccSmsInterfaceManager(Phone phone) {
        return new IccSmsInterfaceManager(phone);
    }

    public UiccProfile makeUiccProfile(Context context, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        return new UiccProfile(context, ci, ics, phoneId, uiccCard, lock);
    }

    public EriManager makeEriManager(Phone phone, int eriFileSource) {
        return new EriManager(phone, eriFileSource);
    }

    public WspTypeDecoder makeWspTypeDecoder(byte[] pdu) {
        return new WspTypeDecoder(pdu);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, boolean is3gpp2WapPdu, String address, String displayAddr, String messageBody, boolean isClass0) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, is3gpp2WapPdu, address, displayAddr, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(byte[] pdu, long timestamp, int destPort, boolean is3gpp2, String address, String displayAddr, int referenceNumber, int sequenceNumber, int messageCount, boolean is3gpp2WapPdu, String messageBody, boolean isClass0) {
        return new InboundSmsTracker(pdu, timestamp, destPort, is3gpp2, address, displayAddr, referenceNumber, sequenceNumber, messageCount, is3gpp2WapPdu, messageBody, isClass0);
    }

    public InboundSmsTracker makeInboundSmsTracker(Cursor cursor, boolean isCurrentFormat3gpp2) {
        return new InboundSmsTracker(cursor, isCurrentFormat3gpp2);
    }

    public ImsPhoneCallTracker makeImsPhoneCallTracker(ImsPhone imsPhone) {
        return new ImsPhoneCallTracker(imsPhone);
    }

    public ImsExternalCallTracker makeImsExternalCallTracker(ImsPhone imsPhone) {
        return new ImsExternalCallTracker(imsPhone);
    }

    public AppSmsManager makeAppSmsManager(Context context) {
        return new AppSmsManager(context);
    }

    public DeviceStateMonitor makeDeviceStateMonitor(Phone phone) {
        return new DeviceStateMonitor(phone);
    }

    public TransportManager makeTransportManager(Phone phone) {
        return new TransportManager(phone);
    }

    public CdmaSubscriptionSourceManager getCdmaSubscriptionSourceManagerInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        return CdmaSubscriptionSourceManager.getInstance(context, ci, h, what, obj);
    }

    public IDeviceIdleController getIDeviceIdleController() {
        return IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    }

    public LocaleTracker makeLocaleTracker(Phone phone, NitzStateMachine nitzStateMachine, Looper looper) {
        return new LocaleTracker(phone, nitzStateMachine, looper);
    }

    public DataEnabledSettings makeDataEnabledSettings(Phone phone) {
        return new DataEnabledSettings(phone);
    }
}
