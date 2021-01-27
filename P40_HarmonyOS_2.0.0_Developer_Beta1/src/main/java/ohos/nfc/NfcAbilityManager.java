package ohos.nfc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilityLifecycleCallbacks;
import ohos.aafwk.ability.HarmonyosApplication;
import ohos.aafwk.ability.Lifecycle;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.PacMapEx;
import ohos.nfc.NfcController;
import ohos.nfc.tag.NdefMessage;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

/* access modifiers changed from: package-private */
public class NfcAbilityManager extends NfcCommProxy implements AbilityLifecycleCallbacks {
    static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcAbilityManager");
    final List<NfcAbilityState> mAbilities = new LinkedList();
    final List<NfcAppState> mAppStates = new ArrayList(1);
    final NfcController mNfcController;
    private NfcControllerProxy mNfcControllerProxy = NfcControllerProxy.getInstance();

    public void onAbilityActive(Ability ability) {
    }

    public void onAbilityInactive(Ability ability) {
    }

    public void onAbilitySaveState(PacMap pacMap) {
    }

    public void onAbilityStart(Ability ability) {
    }

    /* access modifiers changed from: package-private */
    public class NfcAppState {
        final HarmonyosApplication app;
        int refCount = 0;

        public NfcAppState(HarmonyosApplication harmonyosApplication) {
            this.app = harmonyosApplication;
        }

        public void register() {
            this.refCount++;
            if (this.refCount == 1) {
                this.app.registerAbilityLifecycleCallbacks(NfcAbilityManager.this);
            }
        }

        public void unregister() {
            this.refCount--;
            int i = this.refCount;
            if (i == 0) {
                this.app.unregisterAbilityLifecycleCallbacks(NfcAbilityManager.this);
            } else if (i < 0) {
                HiLog.error(NfcAbilityManager.LABEL, "refCount less than zero.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<NfcAppState> getAppState(HarmonyosApplication harmonyosApplication) {
        for (NfcAppState nfcAppState : this.mAppStates) {
            if (nfcAppState.app == harmonyosApplication) {
                return Optional.of(nfcAppState);
            }
        }
        return Optional.empty();
    }

    /* access modifiers changed from: package-private */
    public void registerHarmonyApplication(HarmonyosApplication harmonyosApplication) {
        Optional<NfcAppState> appState = getAppState(harmonyosApplication);
        if (!appState.isPresent()) {
            appState = Optional.of(new NfcAppState(harmonyosApplication));
            this.mAppStates.add(appState.get());
        }
        appState.get().register();
    }

    /* access modifiers changed from: package-private */
    public void unregisterHarmonyApplication(HarmonyosApplication harmonyosApplication) {
        Optional<NfcAppState> appState = getAppState(harmonyosApplication);
        if (!appState.isPresent()) {
            HiLog.error(LABEL, "app was not registered : %{public}s", harmonyosApplication);
        } else {
            appState.get().unregister();
        }
    }

    /* access modifiers changed from: package-private */
    public class NfcAbilityState {
        Ability ability;
        boolean actived = false;
        int flags = 0;
        Optional<NdefMessage> ndefMessage = Optional.empty();
        NfcController.ReaderModeCallback readerModeCallback = null;
        PacMapEx readerModeExtras = null;
        int readerModeFlags = 0;
        RemoteObject token;
        Uri[] uris = null;

        public NfcAbilityState(Ability ability2) {
            boolean z = false;
            if (ability2.getLifecycle().getLifecycleState() != Lifecycle.Event.valueOf("ON_STOP")) {
                this.actived = ability2.getLifecycle().getLifecycleState() == Lifecycle.Event.valueOf("ON_ACTIVE") ? true : z;
                this.ability = ability2;
                this.token = new RemoteObject("NfcAbilityState");
                NfcAbilityManager.this.registerHarmonyApplication(ability2.getHarmonyosApplication());
                return;
            }
            throw new IllegalStateException("ability is already stopped.");
        }

        public void terminate() {
            NfcAbilityManager.this.unregisterHarmonyApplication(this.ability.getHarmonyosApplication());
            this.actived = false;
            this.ability = null;
            this.ndefMessage = null;
            this.uris = null;
            this.readerModeFlags = 0;
            this.token = null;
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void terminateAbilityState(Ability ability) {
        Optional<NfcAbilityState> findAbilityState = findAbilityState(ability);
        if (!findAbilityState.isPresent()) {
            findAbilityState.get().terminate();
            this.mAbilities.remove(findAbilityState.get());
        }
    }

    public NfcAbilityManager(NfcController nfcController) {
        super(SystemAbilityDefinition.NFC_MANAGER_SYS_ABILITY_ID);
        this.mNfcController = nfcController;
    }

    public void unsetReaderMode(Ability ability) {
        RemoteObject remoteObject;
        boolean z;
        synchronized (this) {
            Optional<NfcAbilityState> abilityState = getAbilityState(ability);
            abilityState.get().readerModeCallback = null;
            abilityState.get().readerModeFlags = 0;
            abilityState.get().readerModeExtras = null;
            remoteObject = abilityState.get().token;
            z = abilityState.get().actived;
        }
        if (z) {
            setReaderMode(remoteObject, (NfcController.ReaderModeCallback) null, 0, (PacMapEx) null);
        }
    }

    public void setReaderMode(Ability ability, NfcController.ReaderModeCallback readerModeCallback, int i, PacMapEx pacMapEx) {
        RemoteObject remoteObject;
        boolean z;
        synchronized (this) {
            Optional<NfcAbilityState> abilityState = getAbilityState(ability);
            abilityState.get().readerModeCallback = readerModeCallback;
            abilityState.get().readerModeFlags = i;
            abilityState.get().readerModeExtras = pacMapEx;
            remoteObject = abilityState.get().token;
            z = abilityState.get().actived;
        }
        if (z) {
            setReaderMode(remoteObject, readerModeCallback, i, pacMapEx);
        }
    }

    public void setReaderMode(RemoteObject remoteObject, NfcController.ReaderModeCallback readerModeCallback, int i, PacMapEx pacMapEx) {
        HiLog.debug(LABEL, "Setting reader mode", new Object[0]);
        try {
            this.mNfcControllerProxy.setReaderMode(remoteObject, readerModeCallback, i, pacMapEx);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "setReaderMode failed!", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized Optional<NfcAbilityState> getAbilityState(Ability ability) {
        Optional<NfcAbilityState> findAbilityState;
        findAbilityState = findAbilityState(ability);
        if (!findAbilityState.isPresent()) {
            findAbilityState = Optional.of(new NfcAbilityState(ability));
            this.mAbilities.add(findAbilityState.get());
        }
        return findAbilityState;
    }

    /* access modifiers changed from: package-private */
    public synchronized Optional<NfcAbilityState> findAbilityState(Ability ability) {
        for (NfcAbilityState nfcAbilityState : this.mAbilities) {
            if (nfcAbilityState.ability == ability) {
                return Optional.of(nfcAbilityState);
            }
        }
        return Optional.empty();
    }

    public void onAbilityForeground(Ability ability) {
        RemoteObject remoteObject;
        int i;
        PacMapEx pacMapEx;
        NfcController.ReaderModeCallback readerModeCallback;
        synchronized (this) {
            Optional<NfcAbilityState> findAbilityState = findAbilityState(ability);
            HiLog.debug(LABEL, "onResume() for  %{public}s state", ability);
            if (findAbilityState.isPresent()) {
                findAbilityState.get().actived = true;
                remoteObject = findAbilityState.get().token;
                i = findAbilityState.get().readerModeFlags;
                pacMapEx = findAbilityState.get().readerModeExtras;
                readerModeCallback = findAbilityState.get().readerModeCallback;
            } else {
                return;
            }
        }
        if (i != 0) {
            setReaderMode(remoteObject, readerModeCallback, i, pacMapEx);
        }
    }

    public void onAbilityBackground(Ability ability) {
        boolean z;
        RemoteObject remoteObject;
        synchronized (this) {
            Optional<NfcAbilityState> findAbilityState = findAbilityState(ability);
            z = true;
            HiLog.debug(LABEL, "onPause() for  %{public}s state", ability);
            if (findAbilityState.isPresent()) {
                findAbilityState.get().actived = false;
                remoteObject = findAbilityState.get().token;
                if (findAbilityState.get().readerModeFlags == 0) {
                    z = false;
                }
            } else {
                return;
            }
        }
        if (z) {
            setReaderMode(remoteObject, (NfcController.ReaderModeCallback) null, 0, (PacMapEx) null);
        }
    }

    public void onAbilityStop(Ability ability) {
        synchronized (this) {
            Optional<NfcAbilityState> findAbilityState = findAbilityState(ability);
            HiLog.debug(LABEL, "onAbilityStop() for %{public}s state", ability);
            if (findAbilityState.isPresent()) {
                terminateAbilityState(ability);
            }
        }
    }
}
