package android.view.textservice;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesManager.Stub;
import java.util.Locale;

public final class TextServicesManager {
    private static final boolean DBG = false;
    private static final String TAG = TextServicesManager.class.getSimpleName();
    private static TextServicesManager sInstance;
    private final ITextServicesManager mService = Stub.asInterface(ServiceManager.getServiceOrThrow("textservices"));

    private TextServicesManager() throws ServiceNotFoundException {
    }

    public static TextServicesManager getInstance() {
        TextServicesManager textServicesManager;
        synchronized (TextServicesManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new TextServicesManager();
                } catch (ServiceNotFoundException e) {
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

    public SpellCheckerSession newSpellCheckerSession(Bundle bundle, Locale locale, SpellCheckerSessionListener listener, boolean referToSpellCheckerLanguageSettings) {
        if (listener == null) {
            throw new NullPointerException();
        } else if (!referToSpellCheckerLanguageSettings && locale == null) {
            throw new IllegalArgumentException("Locale should not be null if you don't refer settings.");
        } else if (referToSpellCheckerLanguageSettings && (isSpellCheckerEnabled() ^ 1) != 0) {
            return null;
        } else {
            try {
                SpellCheckerInfo sci = this.mService.getCurrentSpellChecker(null);
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
                        if (subtypeLanguage.length() < 2 || (locale.getLanguage().equals(subtypeLanguage) ^ 1) != 0) {
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
                SpellCheckerSession session = new SpellCheckerSession(sci, this.mService, listener);
                try {
                    this.mService.getSpellCheckerService(sci.getId(), spellCheckerSubtype.getLocale(), session.getTextServicesSessionListener(), session.getSpellCheckerSessionListener(), bundle);
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

    public void setCurrentSpellChecker(SpellCheckerInfo sci) {
        if (sci == null) {
            try {
                throw new NullPointerException("SpellCheckerInfo is null.");
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        this.mService.setCurrentSpellChecker(null, sci.getId());
    }

    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(boolean allowImplicitlySelectedSubtype) {
        try {
            return this.mService.getCurrentSpellCheckerSubtype(null, allowImplicitlySelectedSubtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
            this.mService.setCurrentSpellCheckerSubtype(null, hashCode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSpellCheckerEnabled(boolean enabled) {
        try {
            this.mService.setSpellCheckerEnabled(enabled);
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
