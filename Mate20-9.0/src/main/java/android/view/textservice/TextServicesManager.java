package android.view.textservice;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.textservice.SpellCheckerSession;
import com.android.internal.textservice.ITextServicesManager;
import java.util.Locale;

public final class TextServicesManager {
    private static final boolean DBG = false;
    public static final boolean DISABLE_PER_PROFILE_SPELL_CHECKER = true;
    private static final String TAG = TextServicesManager.class.getSimpleName();
    private static TextServicesManager sInstance;
    private final ITextServicesManager mService = ITextServicesManager.Stub.asInterface(ServiceManager.getServiceOrThrow("textservices"));

    private TextServicesManager() throws ServiceManager.ServiceNotFoundException {
    }

    public static TextServicesManager getInstance() {
        TextServicesManager textServicesManager;
        synchronized (TextServicesManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new TextServicesManager();
                } catch (ServiceManager.ServiceNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            textServicesManager = sInstance;
        }
        return textServicesManager;
    }

    private static String parseLanguageFromLocaleString(String locale) {
        int idx = locale.indexOf(95);
        if (idx < 0) {
            return locale;
        }
        return locale.substring(0, idx);
    }

    public SpellCheckerSession newSpellCheckerSession(Bundle bundle, Locale locale, SpellCheckerSession.SpellCheckerSessionListener listener, boolean referToSpellCheckerLanguageSettings) {
        if (listener == null) {
            throw new NullPointerException();
        } else if (!referToSpellCheckerLanguageSettings && locale == null) {
            throw new IllegalArgumentException("Locale should not be null if you don't refer settings.");
        } else if (referToSpellCheckerLanguageSettings && !isSpellCheckerEnabled()) {
            return null;
        } else {
            try {
                SpellCheckerInfo sci = this.mService.getCurrentSpellChecker(null);
                if (sci == null) {
                    return null;
                }
                SpellCheckerSubtype subtypeInUse = null;
                if (!referToSpellCheckerLanguageSettings) {
                    String localeStr = locale.toString();
                    int i = 0;
                    while (true) {
                        if (i >= sci.getSubtypeCount()) {
                            break;
                        }
                        SpellCheckerSubtype subtype = sci.getSubtypeAt(i);
                        String tempSubtypeLocale = subtype.getLocale();
                        String tempSubtypeLanguage = parseLanguageFromLocaleString(tempSubtypeLocale);
                        if (tempSubtypeLocale.equals(localeStr)) {
                            subtypeInUse = subtype;
                            break;
                        }
                        if (tempSubtypeLanguage.length() >= 2 && locale.getLanguage().equals(tempSubtypeLanguage)) {
                            subtypeInUse = subtype;
                        }
                        i++;
                    }
                } else {
                    subtypeInUse = getCurrentSpellCheckerSubtype(true);
                    if (subtypeInUse == null) {
                        return null;
                    }
                    if (locale != null) {
                        String subtypeLanguage = parseLanguageFromLocaleString(subtypeInUse.getLocale());
                        if (subtypeLanguage.length() < 2 || !locale.getLanguage().equals(subtypeLanguage)) {
                            return null;
                        }
                    }
                }
                if (subtypeInUse == null) {
                    return null;
                }
                SpellCheckerSession session = new SpellCheckerSession(sci, this.mService, listener);
                try {
                    this.mService.getSpellCheckerService(sci.getId(), subtypeInUse.getLocale(), session.getTextServicesSessionListener(), session.getSpellCheckerSessionListener(), bundle);
                    return session;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } catch (RemoteException e2) {
                return null;
            }
        }
    }

    public SpellCheckerInfo[] getEnabledSpellCheckers() {
        try {
            return this.mService.getEnabledSpellCheckers();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public SpellCheckerInfo getCurrentSpellChecker() {
        try {
            return this.mService.getCurrentSpellChecker(null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(boolean allowImplicitlySelectedSubtype) {
        try {
            return this.mService.getCurrentSpellCheckerSubtype(null, allowImplicitlySelectedSubtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSpellCheckerEnabled() {
        try {
            return this.mService.isSpellCheckerEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
