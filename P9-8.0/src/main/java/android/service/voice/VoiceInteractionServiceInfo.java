package android.service.voice;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public class VoiceInteractionServiceInfo {
    static final String TAG = "VoiceInteractionServiceInfo";
    private String mParseError;
    private String mRecognitionService;
    private ServiceInfo mServiceInfo;
    private String mSessionService;
    private String mSettingsActivity;
    private boolean mSupportsAssist;
    private boolean mSupportsLaunchFromKeyguard;
    private boolean mSupportsLocalInteraction;

    public VoiceInteractionServiceInfo(PackageManager pm, ComponentName comp) throws NameNotFoundException {
        this(pm, pm.getServiceInfo(comp, 128));
    }

    public VoiceInteractionServiceInfo(PackageManager pm, ComponentName comp, int userHandle) throws NameNotFoundException {
        this(pm, getServiceInfoOrThrow(comp, userHandle));
    }

    static ServiceInfo getServiceInfoOrThrow(ComponentName comp, int userHandle) throws NameNotFoundException {
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(comp, 269222016, userHandle);
            if (si != null) {
                return si;
            }
        } catch (RemoteException e) {
        }
        throw new NameNotFoundException(comp.toString());
    }

    public VoiceInteractionServiceInfo(PackageManager pm, ServiceInfo si) {
        if (si == null) {
            this.mParseError = "Service not available";
        } else if ("android.permission.BIND_VOICE_INTERACTION".equals(si.permission)) {
            XmlResourceParser xmlResourceParser = null;
            try {
                xmlResourceParser = si.loadXmlMetaData(pm, VoiceInteractionService.SERVICE_META_DATA);
                if (xmlResourceParser == null) {
                    this.mParseError = "No android.voice_interaction meta-data for " + si.packageName;
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    return;
                }
                Resources res = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
                int type;
                do {
                    type = xmlResourceParser.next();
                    if (type == 1) {
                        break;
                    }
                } while (type != 2);
                if ("voice-interaction-service".equals(xmlResourceParser.getName())) {
                    TypedArray array = res.obtainAttributes(attrs, R.styleable.VoiceInteractionService);
                    this.mSessionService = array.getString(1);
                    this.mRecognitionService = array.getString(2);
                    this.mSettingsActivity = array.getString(0);
                    this.mSupportsAssist = array.getBoolean(3, false);
                    this.mSupportsLaunchFromKeyguard = array.getBoolean(4, false);
                    this.mSupportsLocalInteraction = array.getBoolean(5, false);
                    array.recycle();
                    if (this.mSessionService == null) {
                        this.mParseError = "No sessionService specified";
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        return;
                    } else if (this.mRecognitionService == null) {
                        this.mParseError = "No recognitionService specified";
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        return;
                    } else {
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        this.mServiceInfo = si;
                        return;
                    }
                }
                this.mParseError = "Meta-data does not start with voice-interaction-service tag";
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (XmlPullParserException e) {
                this.mParseError = "Error parsing voice interation service meta-data: " + e;
                Log.w(TAG, "error parsing voice interaction service meta-data", e);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (IOException e2) {
                this.mParseError = "Error parsing voice interation service meta-data: " + e2;
                Log.w(TAG, "error parsing voice interaction service meta-data", e2);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (NameNotFoundException e3) {
                this.mParseError = "Error parsing voice interation service meta-data: " + e3;
                Log.w(TAG, "error parsing voice interaction service meta-data", e3);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (Throwable th) {
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                throw th;
            }
        } else {
            this.mParseError = "Service does not require permission android.permission.BIND_VOICE_INTERACTION";
        }
    }

    public String getParseError() {
        return this.mParseError;
    }

    public ServiceInfo getServiceInfo() {
        return this.mServiceInfo;
    }

    public String getSessionService() {
        return this.mSessionService;
    }

    public String getRecognitionService() {
        return this.mRecognitionService;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivity;
    }

    public boolean getSupportsAssist() {
        return this.mSupportsAssist;
    }

    public boolean getSupportsLaunchFromKeyguard() {
        return this.mSupportsLaunchFromKeyguard;
    }

    public boolean getSupportsLocalInteraction() {
        return this.mSupportsLocalInteraction;
    }
}
