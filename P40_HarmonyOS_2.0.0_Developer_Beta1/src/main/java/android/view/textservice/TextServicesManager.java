package android.view.textservice;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.textservice.SpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import java.util.Locale;

public final class TextServicesManager {
    private static final boolean DBG = false;
    private static final String TAG = TextServicesManager.class.getSimpleName();
    @Deprecated
    private static TextServicesManager sInstance;
    private final ITextServicesManager mService = ITextServicesManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.TEXT_SERVICES_MANAGER_SERVICE));
    private final int mUserId;

    private TextServicesManager(int userId) throws ServiceManager.ServiceNotFoundException {
        this.mUserId = userId;
    }

    public static TextServicesManager createInstance(Context context) throws ServiceManager.ServiceNotFoundException {
        return new TextServicesManager(context.getUserId());
    }

    @UnsupportedAppUsage
    public static TextServicesManager getInstance() {
        TextServicesManager textServicesManager;
        synchronized (TextServicesManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new TextServicesManager(UserHandle.myUserId());
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
                SpellCheckerInfo sci = this.mService.getCurrentSpellChecker(this.mUserId, null);
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
                SpellCheckerSession session = new SpellCheckerSession(sci, this, listener);
                try {
                    this.mService.getSpellCheckerService(this.mUserId, sci.getId(), subtypeInUse.getLocale(), session.getTextServicesSessionListener(), session.getSpellCheckerSessionListener(), bundle);
                    return session;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } catch (RemoteException e2) {
                return null;
            }
        }
    }

    @UnsupportedAppUsage
    public SpellCheckerInfo[] getEnabledSpellCheckers() {
        try {
            return this.mService.getEnabledSpellCheckers(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public SpellCheckerInfo getCurrentSpellChecker() {
        try {
            return this.mService.getCurrentSpellChecker(this.mUserId, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public SpellCheckerSubtype getCurrentSpellCheckerSubtype(boolean allowImplicitlySelectedSubtype) {
        try {
            return this.mService.getCurrentSpellCheckerSubtype(this.mUserId, allowImplicitlySelectedSubtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isSpellCheckerEnabled() {
        try {
            return this.mService.isSpellCheckerEnabled(this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public void finishSpellCheckerService(ISpellCheckerSessionListener listener) {
        try {
            this.mService.finishSpellCheckerService(this.mUserId, listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
