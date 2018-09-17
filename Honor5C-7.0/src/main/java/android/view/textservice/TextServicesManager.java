package android.view.textservice;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesManager.Stub;
import java.util.Locale;

public final class TextServicesManager {
    private static final boolean DBG = false;
    private static final String TAG = null;
    private static TextServicesManager sInstance;
    private static ITextServicesManager sService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.TextServicesManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.TextServicesManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.TextServicesManager.<clinit>():void");
    }

    private TextServicesManager() {
        if (sService == null) {
            sService = Stub.asInterface(ServiceManager.getService("textservices"));
        }
    }

    public static TextServicesManager getInstance() {
        synchronized (TextServicesManager.class) {
            if (sInstance != null) {
                TextServicesManager textServicesManager = sInstance;
                return textServicesManager;
            }
            sInstance = new TextServicesManager();
            return sInstance;
        }
    }

    private static String parseLanguageFromLocaleString(String locale) {
        int idx = locale.indexOf(95);
        if (idx < 0) {
            return locale;
        }
        return locale.substring(0, idx);
    }

    public SpellCheckerSession newSpellCheckerSession(Bundle bundle, Locale locale, SpellCheckerSessionListener listener, boolean referToSpellCheckerLanguageSettings) {
        if (listener == null) {
            throw new NullPointerException();
        } else if (!referToSpellCheckerLanguageSettings && locale == null) {
            throw new IllegalArgumentException("Locale should not be null if you don't refer settings.");
        } else if (referToSpellCheckerLanguageSettings && !isSpellCheckerEnabled()) {
            return null;
        } else {
            try {
                SpellCheckerInfo sci = sService.getCurrentSpellChecker(null);
                if (sci == null) {
                    return null;
                }
                SpellCheckerSubtype spellCheckerSubtype = null;
                if (referToSpellCheckerLanguageSettings) {
                    spellCheckerSubtype = getCurrentSpellCheckerSubtype(true);
                    if (spellCheckerSubtype == null) {
                        return null;
                    }
                    if (locale != null) {
                        String subtypeLanguage = parseLanguageFromLocaleString(spellCheckerSubtype.getLocale());
                        if (subtypeLanguage.length() < 2 || !locale.getLanguage().equals(subtypeLanguage)) {
                            return null;
                        }
                    }
                }
                String localeStr = locale.toString();
                for (int i = 0; i < sci.getSubtypeCount(); i++) {
                    SpellCheckerSubtype subtype = sci.getSubtypeAt(i);
                    String tempSubtypeLocale = subtype.getLocale();
                    String tempSubtypeLanguage = parseLanguageFromLocaleString(tempSubtypeLocale);
                    if (tempSubtypeLocale.equals(localeStr)) {
                        spellCheckerSubtype = subtype;
                        break;
                    }
                    if (tempSubtypeLanguage.length() >= 2 && locale.getLanguage().equals(tempSubtypeLanguage)) {
                        spellCheckerSubtype = subtype;
                    }
                }
                if (spellCheckerSubtype == null) {
                    return null;
                }
                SpellCheckerSession session = new SpellCheckerSession(sci, sService, listener, spellCheckerSubtype);
                try {
                    sService.getSpellCheckerService(sci.getId(), spellCheckerSubtype.getLocale(), session.getTextServicesSessionListener(), session.getSpellCheckerSessionListener(), bundle);
                    return session;
                } catch (RemoteException e) {
                    return null;
                }
            } catch (RemoteException e2) {
                return null;
            }
        }
    }

    public SpellCheckerInfo[] getEnabledSpellCheckers() {
        try {
            return sService.getEnabledSpellCheckers();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in getEnabledSpellCheckers: " + e);
            return null;
        }
    }

    public SpellCheckerInfo getCurrentSpellChecker() {
        try {
            return sService.getCurrentSpellChecker(null);
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setCurrentSpellChecker(SpellCheckerInfo sci) {
        if (sci == null) {
            try {
                throw new NullPointerException("SpellCheckerInfo is null.");
            } catch (RemoteException e) {
                Log.e(TAG, "Error in setCurrentSpellChecker: " + e);
                return;
            }
        }
        sService.setCurrentSpellChecker(null, sci.getId());
    }

    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(boolean allowImplicitlySelectedSubtype) {
        try {
            if (sService != null) {
                return sService.getCurrentSpellCheckerSubtype(null, allowImplicitlySelectedSubtype);
            }
            Log.e(TAG, "sService is null.");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "Error in getCurrentSpellCheckerSubtype: " + e);
            return null;
        }
    }

    public void setSpellCheckerSubtype(SpellCheckerSubtype subtype) {
        int hashCode;
        if (subtype == null) {
            hashCode = 0;
        } else {
            hashCode = subtype.hashCode();
        }
        try {
            sService.setCurrentSpellCheckerSubtype(null, hashCode);
        } catch (RemoteException e) {
            Log.e(TAG, "Error in setSpellCheckerSubtype:" + e);
        }
    }

    public void setSpellCheckerEnabled(boolean enabled) {
        try {
            sService.setSpellCheckerEnabled(enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Error in setSpellCheckerEnabled:" + e);
        }
    }

    public boolean isSpellCheckerEnabled() {
        try {
            return sService.isSpellCheckerEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "Error in isSpellCheckerEnabled:" + e);
            return false;
        }
    }
}
