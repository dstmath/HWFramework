package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.intf.IeSEClientServicesAdapter;
import com.nxp.nfc.INfcEventCallback;
import com.nxp.nfc.INfcVzw;
import com.nxp.nfc.INxpNfcAccessExtras;
import com.nxp.nfc.INxpNfcAdapterExtras;
import com.nxp.nfc.ISecureElementCallback;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import java.util.List;
import java.util.Map;

public interface INxpNfcAdapter extends IInterface {
    void DefaultRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareCLTRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareDesfireRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void SetListenTechMask(int i, int i2) throws RemoteException;

    void bindSeService(ISecureElementCallback iSecureElementCallback) throws RemoteException;

    int deselectSecureElement(String str) throws RemoteException;

    void disablePolling() throws RemoteException;

    void enablePolling() throws RemoteException;

    int[] getActiveSecureElementList(String str) throws RemoteException;

    int getCommittedAidRoutingTableSize() throws RemoteException;

    String getDumpInfoForChr() throws RemoteException;

    byte[] getFWVersion() throws RemoteException;

    String getFirmwareVersion() throws RemoteException;

    int getInfoFromConfigFile(int i) throws RemoteException;

    int getMaxAidRoutingTableSize() throws RemoteException;

    IeSEClientServicesAdapter getNfcEseClientServicesAdapterInterface() throws RemoteException;

    String getNfcInfo(String str) throws RemoteException;

    INfcVzw getNfcVzwInterface() throws RemoteException;

    INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String str) throws RemoteException;

    INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException;

    INxpNfcController getNxpNfcControllerInterface() throws RemoteException;

    int getPollingState() throws RemoteException;

    int getSeInterface(int i) throws RemoteException;

    int[] getSecureElementList(String str) throws RemoteException;

    int getSelectedCardEmulation() throws RemoteException;

    int getSelectedSecureElement(String str) throws RemoteException;

    Map getServicesAidCacheSize(int i, String str) throws RemoteException;

    int getSupportCardEmulation() throws RemoteException;

    boolean is2ndLevelMenuOn() throws RemoteException;

    boolean isAutoSwitchAidSupported() throws RemoteException;

    boolean isFelicaRouteToEse() throws RemoteException;

    boolean isListenTechMaskEnable() throws RemoteException;

    void manageAutoSwitch(int i) throws RemoteException;

    void notifyCardMatchInfos(String str, int i) throws RemoteException;

    void notifyCardRemoved(String str) throws RemoteException;

    void selectCardEmulation(int i) throws RemoteException;

    int selectSecureElement(String str, int i) throws RemoteException;

    void set2ndLevelMenu(boolean z) throws RemoteException;

    int setAutoAdjustRf(String str, int i) throws RemoteException;

    void setAutoSwitchSysCfg(String str) throws RemoteException;

    int setConfig(String str, String str2) throws RemoteException;

    int setEmvCoPollProfile(boolean z, int i) throws RemoteException;

    void setFelicaRouteToEse(boolean z) throws RemoteException;

    void setNfcEventCallback(INfcEventCallback iNfcEventCallback) throws RemoteException;

    int setNfcPolling(int i) throws RemoteException;

    void storeSePreference(int i) throws RemoteException;

    int syncActiveCard(String str) throws RemoteException;

    int syncDeactiveCard(String str) throws RemoteException;

    boolean syncNfcServiceTa(int i) throws RemoteException;

    void updateAutoActivedAidsList(List list) throws RemoteException;

    void updateCurrentDefaultAid(String str, boolean z, boolean z2) throws RemoteException;

    void updateDefaultAid(String str) throws RemoteException;

    int updateServiceState(int i, Map map) throws RemoteException;

    public static class Default implements INxpNfcAdapter {
        @Override // com.nxp.nfc.INxpNfcAdapter
        public INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public INfcVzw getNfcVzwInterface() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public INxpNfcController getNxpNfcControllerInterface() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int[] getSecureElementList(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getSelectedSecureElement(String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int selectSecureElement(String pkg, int seId) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int deselectSecureElement(String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void storeSePreference(int seId) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setEmvCoPollProfile(boolean enable, int route) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public IeSEClientServicesAdapter getNfcEseClientServicesAdapterInterface() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getSeInterface(int type) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void SetListenTechMask(int flags_ListenMask, int enable_override) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void notifyCardRemoved(String aid) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setAutoAdjustRf(String aid, int mode) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean isAutoSwitchAidSupported() throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void updateCurrentDefaultAid(String aid, boolean isOpenCardEmulation, boolean isActive) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void updateDefaultAid(String aid) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void setAutoSwitchSysCfg(String cfgParams) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void manageAutoSwitch(int state) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean syncNfcServiceTa(int reason) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void notifyCardMatchInfos(String cardMatchInfos, int scene) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void updateAutoActivedAidsList(List aids) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int syncDeactiveCard(String currentAid) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int syncActiveCard(String targetAid) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public byte[] getFWVersion() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public Map getServicesAidCacheSize(int userId, String category) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getMaxAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getCommittedAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int[] getActiveSecureElementList(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int updateServiceState(int userId, Map serviceState) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setConfig(String configs, String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public String getNfcInfo(String key) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setNfcPolling(int mode) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean isListenTechMaskEnable() throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void setNfcEventCallback(INfcEventCallback callback) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void enablePolling() throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void disablePolling() throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getPollingState() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getSelectedCardEmulation() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void selectCardEmulation(int sub) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getSupportCardEmulation() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public String getFirmwareVersion() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean is2ndLevelMenuOn() throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void set2ndLevelMenu(boolean onOff) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public String getDumpInfoForChr() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void bindSeService(ISecureElementCallback callback) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getInfoFromConfigFile(int key) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void setFelicaRouteToEse(boolean isEnable) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean isFelicaRouteToEse() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcAdapter {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapter";
        static final int TRANSACTION_DefaultRouteSet = 12;
        static final int TRANSACTION_MifareCLTRouteSet = 13;
        static final int TRANSACTION_MifareDesfireRouteSet = 11;
        static final int TRANSACTION_SetListenTechMask = 16;
        static final int TRANSACTION_bindSeService = 50;
        static final int TRANSACTION_deselectSecureElement = 8;
        static final int TRANSACTION_disablePolling = 41;
        static final int TRANSACTION_enablePolling = 40;
        static final int TRANSACTION_getActiveSecureElementList = 33;
        static final int TRANSACTION_getCommittedAidRoutingTableSize = 32;
        static final int TRANSACTION_getDumpInfoForChr = 49;
        static final int TRANSACTION_getFWVersion = 29;
        static final int TRANSACTION_getFirmwareVersion = 46;
        static final int TRANSACTION_getInfoFromConfigFile = 51;
        static final int TRANSACTION_getMaxAidRoutingTableSize = 31;
        static final int TRANSACTION_getNfcEseClientServicesAdapterInterface = 14;
        static final int TRANSACTION_getNfcInfo = 36;
        static final int TRANSACTION_getNfcVzwInterface = 2;
        static final int TRANSACTION_getNxpNfcAccessExtrasInterface = 1;
        static final int TRANSACTION_getNxpNfcAdapterExtrasInterface = 3;
        static final int TRANSACTION_getNxpNfcControllerInterface = 4;
        static final int TRANSACTION_getPollingState = 42;
        static final int TRANSACTION_getSeInterface = 15;
        static final int TRANSACTION_getSecureElementList = 5;
        static final int TRANSACTION_getSelectedCardEmulation = 43;
        static final int TRANSACTION_getSelectedSecureElement = 6;
        static final int TRANSACTION_getServicesAidCacheSize = 30;
        static final int TRANSACTION_getSupportCardEmulation = 45;
        static final int TRANSACTION_is2ndLevelMenuOn = 47;
        static final int TRANSACTION_isAutoSwitchAidSupported = 19;
        static final int TRANSACTION_isFelicaRouteToEse = 53;
        static final int TRANSACTION_isListenTechMaskEnable = 38;
        static final int TRANSACTION_manageAutoSwitch = 23;
        static final int TRANSACTION_notifyCardMatchInfos = 25;
        static final int TRANSACTION_notifyCardRemoved = 17;
        static final int TRANSACTION_selectCardEmulation = 44;
        static final int TRANSACTION_selectSecureElement = 7;
        static final int TRANSACTION_set2ndLevelMenu = 48;
        static final int TRANSACTION_setAutoAdjustRf = 18;
        static final int TRANSACTION_setAutoSwitchSysCfg = 22;
        static final int TRANSACTION_setConfig = 35;
        static final int TRANSACTION_setEmvCoPollProfile = 10;
        static final int TRANSACTION_setFelicaRouteToEse = 52;
        static final int TRANSACTION_setNfcEventCallback = 39;
        static final int TRANSACTION_setNfcPolling = 37;
        static final int TRANSACTION_storeSePreference = 9;
        static final int TRANSACTION_syncActiveCard = 28;
        static final int TRANSACTION_syncDeactiveCard = 27;
        static final int TRANSACTION_syncNfcServiceTa = 24;
        static final int TRANSACTION_updateAutoActivedAidsList = 26;
        static final int TRANSACTION_updateCurrentDefaultAid = 20;
        static final int TRANSACTION_updateDefaultAid = 21;
        static final int TRANSACTION_updateServiceState = 34;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAdapter)) {
                return new Proxy(obj);
            }
            return (INxpNfcAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                IBinder iBinder4 = null;
                IBinder iBinder5 = null;
                boolean _arg0 = false;
                boolean _arg02 = false;
                boolean _arg03 = false;
                boolean _arg2 = false;
                boolean _arg3 = false;
                boolean _arg32 = false;
                boolean _arg33 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcAccessExtras _result = getNxpNfcAccessExtrasInterface(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        INfcVzw _result2 = getNfcVzwInterface();
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder5 = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder5);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcAdapterExtras _result3 = getNxpNfcAdapterExtrasInterface();
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder4 = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder4);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcController _result4 = getNxpNfcControllerInterface();
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder3 = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result5 = getSecureElementList(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result5);
                        return true;
                    case TRANSACTION_getSelectedSecureElement /*{ENCODED_INT: 6}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getSelectedSecureElement(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_selectSecureElement /*{ENCODED_INT: 7}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = selectSecureElement(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_deselectSecureElement /*{ENCODED_INT: 8}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = deselectSecureElement(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_storeSePreference /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        storeSePreference(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setEmvCoPollProfile /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result9 = setEmvCoPollProfile(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_MifareDesfireRouteSet /*{ENCODED_INT: 11}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        boolean _arg1 = data.readInt() != 0;
                        boolean _arg22 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg33 = true;
                        }
                        MifareDesfireRouteSet(_arg04, _arg1, _arg22, _arg33);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_DefaultRouteSet /*{ENCODED_INT: 12}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        boolean _arg12 = data.readInt() != 0;
                        boolean _arg23 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg32 = true;
                        }
                        DefaultRouteSet(_arg05, _arg12, _arg23, _arg32);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_MifareCLTRouteSet /*{ENCODED_INT: 13}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        boolean _arg13 = data.readInt() != 0;
                        boolean _arg24 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        MifareCLTRouteSet(_arg06, _arg13, _arg24, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getNfcEseClientServicesAdapterInterface /*{ENCODED_INT: 14}*/:
                        data.enforceInterface(DESCRIPTOR);
                        IeSEClientServicesAdapter _result10 = getNfcEseClientServicesAdapterInterface();
                        reply.writeNoException();
                        if (_result10 != null) {
                            iBinder2 = _result10.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
                        return true;
                    case TRANSACTION_getSeInterface /*{ENCODED_INT: 15}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getSeInterface(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        SetListenTechMask(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_notifyCardRemoved /*{ENCODED_INT: 17}*/:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCardRemoved(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setAutoAdjustRf /*{ENCODED_INT: 18}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = setAutoAdjustRf(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case TRANSACTION_isAutoSwitchAidSupported /*{ENCODED_INT: 19}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAutoSwitchAidSupported = isAutoSwitchAidSupported();
                        reply.writeNoException();
                        reply.writeInt(isAutoSwitchAidSupported ? 1 : 0);
                        return true;
                    case TRANSACTION_updateCurrentDefaultAid /*{ENCODED_INT: 20}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        boolean _arg14 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        updateCurrentDefaultAid(_arg07, _arg14, _arg2);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_updateDefaultAid /*{ENCODED_INT: 21}*/:
                        data.enforceInterface(DESCRIPTOR);
                        updateDefaultAid(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setAutoSwitchSysCfg /*{ENCODED_INT: 22}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setAutoSwitchSysCfg(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_manageAutoSwitch /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        manageAutoSwitch(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_syncNfcServiceTa /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean syncNfcServiceTa = syncNfcServiceTa(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(syncNfcServiceTa ? 1 : 0);
                        return true;
                    case TRANSACTION_notifyCardMatchInfos /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCardMatchInfos(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_updateAutoActivedAidsList /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        updateAutoActivedAidsList(data.readArrayList(getClass().getClassLoader()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_syncDeactiveCard /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = syncDeactiveCard(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case TRANSACTION_syncActiveCard /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = syncActiveCard(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case TRANSACTION_getFWVersion /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result15 = getFWVersion();
                        reply.writeNoException();
                        reply.writeByteArray(_result15);
                        return true;
                    case TRANSACTION_getServicesAidCacheSize /*{ENCODED_INT: 30}*/:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result16 = getServicesAidCacheSize(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeMap(_result16);
                        return true;
                    case TRANSACTION_getMaxAidRoutingTableSize /*{ENCODED_INT: 31}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getMaxAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case TRANSACTION_getCommittedAidRoutingTableSize /*{ENCODED_INT: 32}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getCommittedAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_getActiveSecureElementList /*{ENCODED_INT: 33}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result19 = getActiveSecureElementList(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result19);
                        return true;
                    case TRANSACTION_updateServiceState /*{ENCODED_INT: 34}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = updateServiceState(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_setConfig /*{ENCODED_INT: 35}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = setConfig(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case TRANSACTION_getNfcInfo /*{ENCODED_INT: 36}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = getNfcInfo(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case TRANSACTION_setNfcPolling /*{ENCODED_INT: 37}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = setNfcPolling(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case TRANSACTION_isListenTechMaskEnable /*{ENCODED_INT: 38}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isListenTechMaskEnable = isListenTechMaskEnable();
                        reply.writeNoException();
                        reply.writeInt(isListenTechMaskEnable ? 1 : 0);
                        return true;
                    case TRANSACTION_setNfcEventCallback /*{ENCODED_INT: 39}*/:
                        data.enforceInterface(DESCRIPTOR);
                        setNfcEventCallback(INfcEventCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_enablePolling /*{ENCODED_INT: 40}*/:
                        data.enforceInterface(DESCRIPTOR);
                        enablePolling();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_disablePolling /*{ENCODED_INT: 41}*/:
                        data.enforceInterface(DESCRIPTOR);
                        disablePolling();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getPollingState /*{ENCODED_INT: 42}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = getPollingState();
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case TRANSACTION_getSelectedCardEmulation /*{ENCODED_INT: 43}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getSelectedCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case TRANSACTION_selectCardEmulation /*{ENCODED_INT: 44}*/:
                        data.enforceInterface(DESCRIPTOR);
                        selectCardEmulation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getSupportCardEmulation /*{ENCODED_INT: 45}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getSupportCardEmulation();
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case TRANSACTION_getFirmwareVersion /*{ENCODED_INT: 46}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result27 = getFirmwareVersion();
                        reply.writeNoException();
                        reply.writeString(_result27);
                        return true;
                    case TRANSACTION_is2ndLevelMenuOn /*{ENCODED_INT: 47}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean is2ndLevelMenuOn = is2ndLevelMenuOn();
                        reply.writeNoException();
                        reply.writeInt(is2ndLevelMenuOn ? 1 : 0);
                        return true;
                    case TRANSACTION_set2ndLevelMenu /*{ENCODED_INT: 48}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        set2ndLevelMenu(_arg03);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getDumpInfoForChr /*{ENCODED_INT: 49}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result28 = getDumpInfoForChr();
                        reply.writeNoException();
                        reply.writeString(_result28);
                        return true;
                    case TRANSACTION_bindSeService /*{ENCODED_INT: 50}*/:
                        data.enforceInterface(DESCRIPTOR);
                        bindSeService(ISecureElementCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getInfoFromConfigFile /*{ENCODED_INT: 51}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = getInfoFromConfigFile(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case TRANSACTION_setFelicaRouteToEse /*{ENCODED_INT: 52}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setFelicaRouteToEse(_arg02);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isFelicaRouteToEse /*{ENCODED_INT: 53}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFelicaRouteToEse = isFelicaRouteToEse();
                        reply.writeNoException();
                        reply.writeInt(isFelicaRouteToEse ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INxpNfcAdapter {
            public static INxpNfcAdapter sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public INxpNfcAccessExtras getNxpNfcAccessExtrasInterface(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNxpNfcAccessExtrasInterface(pkg);
                    }
                    _reply.readException();
                    INxpNfcAccessExtras _result = INxpNfcAccessExtras.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public INfcVzw getNfcVzwInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcVzwInterface();
                    }
                    _reply.readException();
                    INfcVzw _result = INfcVzw.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNxpNfcAdapterExtrasInterface();
                    }
                    _reply.readException();
                    INxpNfcAdapterExtras _result = INxpNfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public INxpNfcController getNxpNfcControllerInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNxpNfcControllerInterface();
                    }
                    _reply.readException();
                    INxpNfcController _result = INxpNfcController.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int[] getSecureElementList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureElementList(pkg);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getSelectedSecureElement(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSelectedSecureElement, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelectedSecureElement(pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int selectSecureElement(String pkg, int seId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(seId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_selectSecureElement, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().selectSecureElement(pkg, seId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int deselectSecureElement(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_deselectSecureElement, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deselectSecureElement(pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void storeSePreference(int seId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seId);
                    if (this.mRemote.transact(Stub.TRANSACTION_storeSePreference, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().storeSePreference(seId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setEmvCoPollProfile(boolean enable, int route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(route);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setEmvCoPollProfile, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEmvCoPollProfile(enable, route);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    int i = 1;
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(Stub.TRANSACTION_MifareDesfireRouteSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().MifareDesfireRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    int i = 1;
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(Stub.TRANSACTION_DefaultRouteSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().DefaultRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    int i = 1;
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(Stub.TRANSACTION_MifareCLTRouteSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().MifareCLTRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public IeSEClientServicesAdapter getNfcEseClientServicesAdapterInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNfcEseClientServicesAdapterInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcEseClientServicesAdapterInterface();
                    }
                    _reply.readException();
                    IeSEClientServicesAdapter _result = IeSEClientServicesAdapter.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getSeInterface(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSeInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeInterface(type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void SetListenTechMask(int flags_ListenMask, int enable_override) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags_ListenMask);
                    _data.writeInt(enable_override);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetListenTechMask(flags_ListenMask, enable_override);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void notifyCardRemoved(String aid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(aid);
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyCardRemoved, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCardRemoved(aid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setAutoAdjustRf(String aid, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(aid);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setAutoAdjustRf, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAutoAdjustRf(aid, mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean isAutoSwitchAidSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isAutoSwitchAidSupported, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAutoSwitchAidSupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void updateCurrentDefaultAid(String aid, boolean isOpenCardEmulation, boolean isActive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(aid);
                    int i = 1;
                    _data.writeInt(isOpenCardEmulation ? 1 : 0);
                    if (!isActive) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateCurrentDefaultAid, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCurrentDefaultAid(aid, isOpenCardEmulation, isActive);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void updateDefaultAid(String aid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(aid);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateDefaultAid, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateDefaultAid(aid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void setAutoSwitchSysCfg(String cfgParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cfgParams);
                    if (this.mRemote.transact(Stub.TRANSACTION_setAutoSwitchSysCfg, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAutoSwitchSysCfg(cfgParams);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void manageAutoSwitch(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(Stub.TRANSACTION_manageAutoSwitch, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().manageAutoSwitch(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean syncNfcServiceTa(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_syncNfcServiceTa, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().syncNfcServiceTa(reason);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void notifyCardMatchInfos(String cardMatchInfos, int scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardMatchInfos);
                    _data.writeInt(scene);
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyCardMatchInfos, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCardMatchInfos(cardMatchInfos, scene);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void updateAutoActivedAidsList(List aids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeList(aids);
                    if (this.mRemote.transact(Stub.TRANSACTION_updateAutoActivedAidsList, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateAutoActivedAidsList(aids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int syncDeactiveCard(String currentAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentAid);
                    if (!this.mRemote.transact(Stub.TRANSACTION_syncDeactiveCard, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().syncDeactiveCard(currentAid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int syncActiveCard(String targetAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetAid);
                    if (!this.mRemote.transact(Stub.TRANSACTION_syncActiveCard, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().syncActiveCard(targetAid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public byte[] getFWVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getFWVersion, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFWVersion();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public Map getServicesAidCacheSize(int userId, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(category);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getServicesAidCacheSize, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServicesAidCacheSize(userId, category);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getMaxAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMaxAidRoutingTableSize, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxAidRoutingTableSize();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getCommittedAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCommittedAidRoutingTableSize, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCommittedAidRoutingTableSize();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int[] getActiveSecureElementList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getActiveSecureElementList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveSecureElementList(pkg);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int updateServiceState(int userId, Map serviceState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeMap(serviceState);
                    if (!this.mRemote.transact(Stub.TRANSACTION_updateServiceState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateServiceState(userId, serviceState);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setConfig(String configs, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(configs);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfig(configs, pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public String getNfcInfo(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNfcInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNfcInfo(key);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setNfcPolling(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setNfcPolling, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNfcPolling(mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean isListenTechMaskEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isListenTechMaskEnable, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isListenTechMaskEnable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void setNfcEventCallback(INfcEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_setNfcEventCallback, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNfcEventCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void enablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_enablePolling, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enablePolling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void disablePolling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_disablePolling, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disablePolling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getPollingState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getPollingState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPollingState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getSelectedCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSelectedCardEmulation, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelectedCardEmulation();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void selectCardEmulation(int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sub);
                    if (this.mRemote.transact(Stub.TRANSACTION_selectCardEmulation, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().selectCardEmulation(sub);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getSupportCardEmulation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSupportCardEmulation, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportCardEmulation();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public String getFirmwareVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getFirmwareVersion, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFirmwareVersion();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean is2ndLevelMenuOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_is2ndLevelMenuOn, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().is2ndLevelMenuOn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void set2ndLevelMenu(boolean onOff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(onOff ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_set2ndLevelMenu, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().set2ndLevelMenu(onOff);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public String getDumpInfoForChr() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDumpInfoForChr, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDumpInfoForChr();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void bindSeService(ISecureElementCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_bindSeService, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bindSeService(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getInfoFromConfigFile(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getInfoFromConfigFile, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInfoFromConfigFile(key);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void setFelicaRouteToEse(boolean isEnable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isEnable ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setFelicaRouteToEse, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFelicaRouteToEse(isEnable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean isFelicaRouteToEse() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isFelicaRouteToEse, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFelicaRouteToEse();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INxpNfcAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
