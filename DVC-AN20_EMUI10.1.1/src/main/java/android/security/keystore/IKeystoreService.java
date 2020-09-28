package android.security.keystore;

import android.app.slice.Slice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterBlob;
import android.security.keystore.IKeystoreCertificateChainCallback;
import android.security.keystore.IKeystoreExportKeyCallback;
import android.security.keystore.IKeystoreKeyCharacteristicsCallback;
import android.security.keystore.IKeystoreOperationResultCallback;
import android.security.keystore.IKeystoreResponseCallback;
import java.util.ArrayList;
import java.util.List;

public interface IKeystoreService extends IInterface {
    int abort(IKeystoreResponseCallback iKeystoreResponseCallback, IBinder iBinder) throws RemoteException;

    int addAuthToken(byte[] bArr) throws RemoteException;

    int addRngEntropy(IKeystoreResponseCallback iKeystoreResponseCallback, byte[] bArr, int i) throws RemoteException;

    int attestDeviceIds(IKeystoreCertificateChainCallback iKeystoreCertificateChainCallback, KeymasterArguments keymasterArguments) throws RemoteException;

    int attestKey(IKeystoreCertificateChainCallback iKeystoreCertificateChainCallback, String str, KeymasterArguments keymasterArguments) throws RemoteException;

    int begin(IKeystoreOperationResultCallback iKeystoreOperationResultCallback, IBinder iBinder, String str, int i, boolean z, KeymasterArguments keymasterArguments, byte[] bArr, int i2) throws RemoteException;

    int cancelConfirmationPrompt(IBinder iBinder) throws RemoteException;

    int clear_uid(long j) throws RemoteException;

    int del(String str, int i) throws RemoteException;

    int exist(String str, int i) throws RemoteException;

    int exportKey(IKeystoreExportKeyCallback iKeystoreExportKeyCallback, String str, int i, KeymasterBlob keymasterBlob, KeymasterBlob keymasterBlob2, int i2) throws RemoteException;

    int finish(IKeystoreOperationResultCallback iKeystoreOperationResultCallback, IBinder iBinder, KeymasterArguments keymasterArguments, byte[] bArr, byte[] bArr2) throws RemoteException;

    int generateKey(IKeystoreKeyCharacteristicsCallback iKeystoreKeyCharacteristicsCallback, String str, KeymasterArguments keymasterArguments, byte[] bArr, int i, int i2) throws RemoteException;

    byte[] get(String str, int i) throws RemoteException;

    int getKeyCharacteristics(IKeystoreKeyCharacteristicsCallback iKeystoreKeyCharacteristicsCallback, String str, KeymasterBlob keymasterBlob, KeymasterBlob keymasterBlob2, int i) throws RemoteException;

    int getState(int i) throws RemoteException;

    long getmtime(String str, int i) throws RemoteException;

    String grant(String str, int i) throws RemoteException;

    int importKey(IKeystoreKeyCharacteristicsCallback iKeystoreKeyCharacteristicsCallback, String str, KeymasterArguments keymasterArguments, int i, byte[] bArr, int i2, int i3) throws RemoteException;

    int importWrappedKey(IKeystoreKeyCharacteristicsCallback iKeystoreKeyCharacteristicsCallback, String str, byte[] bArr, String str2, byte[] bArr2, KeymasterArguments keymasterArguments, long j, long j2) throws RemoteException;

    int insert(String str, byte[] bArr, int i, int i2) throws RemoteException;

    boolean isConfirmationPromptSupported() throws RemoteException;

    int isEmpty(int i) throws RemoteException;

    int is_hardware_backed(String str) throws RemoteException;

    String[] list(String str, int i) throws RemoteException;

    int listUidsOfAuthBoundKeys(List<String> list) throws RemoteException;

    int lock(int i) throws RemoteException;

    int onDeviceOffBody() throws RemoteException;

    int onKeyguardVisibilityChanged(boolean z, int i) throws RemoteException;

    int onUserAdded(int i, int i2) throws RemoteException;

    int onUserPasswordChanged(int i, String str) throws RemoteException;

    int onUserRemoved(int i) throws RemoteException;

    int presentConfirmationPrompt(IBinder iBinder, String str, byte[] bArr, String str2, int i) throws RemoteException;

    int reset() throws RemoteException;

    int ungrant(String str, int i) throws RemoteException;

    int unlock(int i, String str) throws RemoteException;

    int update(IKeystoreOperationResultCallback iKeystoreOperationResultCallback, IBinder iBinder, KeymasterArguments keymasterArguments, byte[] bArr) throws RemoteException;

    public static class Default implements IKeystoreService {
        @Override // android.security.keystore.IKeystoreService
        public int getState(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public byte[] get(String name, int uid) throws RemoteException {
            return null;
        }

        @Override // android.security.keystore.IKeystoreService
        public int insert(String name, byte[] item, int uid, int flags) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int del(String name, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int exist(String name, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public String[] list(String namePrefix, int uid) throws RemoteException {
            return null;
        }

        @Override // android.security.keystore.IKeystoreService
        public int reset() throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int onUserPasswordChanged(int userId, String newPassword) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int lock(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int unlock(int userId, String userPassword) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int isEmpty(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public String grant(String name, int granteeUid) throws RemoteException {
            return null;
        }

        @Override // android.security.keystore.IKeystoreService
        public int ungrant(String name, int granteeUid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public long getmtime(String name, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int is_hardware_backed(String string) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int clear_uid(long uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int addRngEntropy(IKeystoreResponseCallback cb, byte[] data, int flags) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int generateKey(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterArguments arguments, byte[] entropy, int uid, int flags) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int getKeyCharacteristics(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterBlob clientId, KeymasterBlob appData, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int importKey(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterArguments arguments, int format, byte[] keyData, int uid, int flags) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int exportKey(IKeystoreExportKeyCallback cb, String alias, int format, KeymasterBlob clientId, KeymasterBlob appData, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int begin(IKeystoreOperationResultCallback cb, IBinder appToken, String alias, int purpose, boolean pruneable, KeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int update(IKeystoreOperationResultCallback cb, IBinder token, KeymasterArguments params, byte[] input) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int finish(IKeystoreOperationResultCallback cb, IBinder token, KeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int abort(IKeystoreResponseCallback cb, IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int addAuthToken(byte[] authToken) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int onUserAdded(int userId, int parentId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int onUserRemoved(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int attestKey(IKeystoreCertificateChainCallback cb, String alias, KeymasterArguments params) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int attestDeviceIds(IKeystoreCertificateChainCallback cb, KeymasterArguments params) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int onDeviceOffBody() throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int importWrappedKey(IKeystoreKeyCharacteristicsCallback cb, String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments arguments, long rootSid, long fingerprintSid) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int presentConfirmationPrompt(IBinder listener, String promptText, byte[] extraData, String locale, int uiOptionsAsFlags) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int cancelConfirmationPrompt(IBinder listener) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public boolean isConfirmationPromptSupported() throws RemoteException {
            return false;
        }

        @Override // android.security.keystore.IKeystoreService
        public int onKeyguardVisibilityChanged(boolean isShowing, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.security.keystore.IKeystoreService
        public int listUidsOfAuthBoundKeys(List<String> list) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IKeystoreService {
        private static final String DESCRIPTOR = "android.security.keystore.IKeystoreService";
        static final int TRANSACTION_abort = 25;
        static final int TRANSACTION_addAuthToken = 26;
        static final int TRANSACTION_addRngEntropy = 17;
        static final int TRANSACTION_attestDeviceIds = 30;
        static final int TRANSACTION_attestKey = 29;
        static final int TRANSACTION_begin = 22;
        static final int TRANSACTION_cancelConfirmationPrompt = 34;
        static final int TRANSACTION_clear_uid = 16;
        static final int TRANSACTION_del = 4;
        static final int TRANSACTION_exist = 5;
        static final int TRANSACTION_exportKey = 21;
        static final int TRANSACTION_finish = 24;
        static final int TRANSACTION_generateKey = 18;
        static final int TRANSACTION_get = 2;
        static final int TRANSACTION_getKeyCharacteristics = 19;
        static final int TRANSACTION_getState = 1;
        static final int TRANSACTION_getmtime = 14;
        static final int TRANSACTION_grant = 12;
        static final int TRANSACTION_importKey = 20;
        static final int TRANSACTION_importWrappedKey = 32;
        static final int TRANSACTION_insert = 3;
        static final int TRANSACTION_isConfirmationPromptSupported = 35;
        static final int TRANSACTION_isEmpty = 11;
        static final int TRANSACTION_is_hardware_backed = 15;
        static final int TRANSACTION_list = 6;
        static final int TRANSACTION_listUidsOfAuthBoundKeys = 37;
        static final int TRANSACTION_lock = 9;
        static final int TRANSACTION_onDeviceOffBody = 31;
        static final int TRANSACTION_onKeyguardVisibilityChanged = 36;
        static final int TRANSACTION_onUserAdded = 27;
        static final int TRANSACTION_onUserPasswordChanged = 8;
        static final int TRANSACTION_onUserRemoved = 28;
        static final int TRANSACTION_presentConfirmationPrompt = 33;
        static final int TRANSACTION_reset = 7;
        static final int TRANSACTION_ungrant = 13;
        static final int TRANSACTION_unlock = 10;
        static final int TRANSACTION_update = 23;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKeystoreService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeystoreService)) {
                return new Proxy(obj);
            }
            return (IKeystoreService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getState";
                case 2:
                    return "get";
                case 3:
                    return "insert";
                case 4:
                    return "del";
                case 5:
                    return "exist";
                case 6:
                    return Slice.HINT_LIST;
                case 7:
                    return "reset";
                case 8:
                    return "onUserPasswordChanged";
                case 9:
                    return "lock";
                case 10:
                    return "unlock";
                case 11:
                    return "isEmpty";
                case 12:
                    return "grant";
                case 13:
                    return "ungrant";
                case 14:
                    return "getmtime";
                case 15:
                    return "is_hardware_backed";
                case 16:
                    return "clear_uid";
                case 17:
                    return "addRngEntropy";
                case 18:
                    return "generateKey";
                case 19:
                    return "getKeyCharacteristics";
                case 20:
                    return "importKey";
                case 21:
                    return "exportKey";
                case 22:
                    return "begin";
                case 23:
                    return "update";
                case 24:
                    return "finish";
                case 25:
                    return "abort";
                case 26:
                    return "addAuthToken";
                case 27:
                    return "onUserAdded";
                case 28:
                    return "onUserRemoved";
                case 29:
                    return "attestKey";
                case 30:
                    return "attestDeviceIds";
                case 31:
                    return "onDeviceOffBody";
                case 32:
                    return "importWrappedKey";
                case 33:
                    return "presentConfirmationPrompt";
                case 34:
                    return "cancelConfirmationPrompt";
                case 35:
                    return "isConfirmationPromptSupported";
                case 36:
                    return "onKeyguardVisibilityChanged";
                case 37:
                    return "listUidsOfAuthBoundKeys";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeymasterArguments _arg2;
            KeymasterBlob _arg22;
            KeymasterBlob _arg3;
            KeymasterArguments _arg23;
            KeymasterBlob _arg32;
            KeymasterBlob _arg4;
            KeymasterArguments _arg5;
            KeymasterArguments _arg24;
            KeymasterArguments _arg25;
            KeymasterArguments _arg26;
            KeymasterArguments _arg1;
            KeymasterArguments _arg52;
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = get(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = insert(data.readString(), data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = del(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = exist(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result6 = list(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = reset();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = onUserPasswordChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = lock(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = unlock(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = isEmpty(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = grant(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = ungrant(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        long _result14 = getmtime(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = is_hardware_backed(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = clear_uid(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = addRngEntropy(IKeystoreResponseCallback.Stub.asInterface(data.readStrongBinder()), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreKeyCharacteristicsCallback _arg02 = IKeystoreKeyCharacteristicsCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result18 = generateKey(_arg02, _arg12, _arg2, data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreKeyCharacteristicsCallback _arg03 = IKeystoreKeyCharacteristicsCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = KeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = KeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result19 = getKeyCharacteristics(_arg03, _arg13, _arg22, _arg3, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreKeyCharacteristicsCallback _arg04 = IKeystoreKeyCharacteristicsCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result20 = importKey(_arg04, _arg14, _arg23, data.readInt(), data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreExportKeyCallback _arg05 = IKeystoreExportKeyCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg15 = data.readString();
                        int _arg27 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = KeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = KeymasterBlob.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result21 = exportKey(_arg05, _arg15, _arg27, _arg32, _arg4, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreOperationResultCallback _arg06 = IKeystoreOperationResultCallback.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg16 = data.readStrongBinder();
                        String _arg28 = data.readString();
                        int _arg33 = data.readInt();
                        boolean _arg42 = data.readInt() != 0 ? true : false;
                        if (data.readInt() != 0) {
                            _arg5 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        int _result22 = begin(_arg06, _arg16, _arg28, _arg33, _arg42, _arg5, data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreOperationResultCallback _arg07 = IKeystoreOperationResultCallback.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg17 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg24 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result23 = update(_arg07, _arg17, _arg24, data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreOperationResultCallback _arg08 = IKeystoreOperationResultCallback.Stub.asInterface(data.readStrongBinder());
                        IBinder _arg18 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg25 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        int _result24 = finish(_arg08, _arg18, _arg25, data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = abort(IKeystoreResponseCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = addAuthToken(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = onUserAdded(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = onUserRemoved(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreCertificateChainCallback _arg09 = IKeystoreCertificateChainCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        int _result29 = attestKey(_arg09, _arg19, _arg26);
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreCertificateChainCallback _arg010 = IKeystoreCertificateChainCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result30 = attestDeviceIds(_arg010, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = onDeviceOffBody();
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        IKeystoreKeyCharacteristicsCallback _arg011 = IKeystoreKeyCharacteristicsCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg110 = data.readString();
                        byte[] _arg29 = data.createByteArray();
                        String _arg34 = data.readString();
                        byte[] _arg43 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg52 = KeymasterArguments.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        int _result32 = importWrappedKey(_arg011, _arg110, _arg29, _arg34, _arg43, _arg52, data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result33 = presentConfirmationPrompt(data.readStrongBinder(), data.readString(), data.createByteArray(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result34 = cancelConfirmationPrompt(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConfirmationPromptSupported = isConfirmationPromptSupported();
                        reply.writeNoException();
                        reply.writeInt(isConfirmationPromptSupported == 1 ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        int _result35 = onKeyguardVisibilityChanged(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg012 = new ArrayList<>();
                        int _result36 = listUidsOfAuthBoundKeys(_arg012);
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        reply.writeStringList(_arg012);
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
        public static class Proxy implements IKeystoreService {
            public static IKeystoreService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.security.keystore.IKeystoreService
            public int getState(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getState(userId);
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

            @Override // android.security.keystore.IKeystoreService
            public byte[] get(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(2, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().get(name, uid);
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

            @Override // android.security.keystore.IKeystoreService
            public int insert(String name, byte[] item, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeByteArray(item);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(3, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().insert(name, item, uid, flags);
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

            @Override // android.security.keystore.IKeystoreService
            public int del(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(4, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().del(name, uid);
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

            @Override // android.security.keystore.IKeystoreService
            public int exist(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(5, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exist(name, uid);
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

            @Override // android.security.keystore.IKeystoreService
            public String[] list(String namePrefix, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(namePrefix);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(6, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().list(namePrefix, uid);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int reset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reset();
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

            @Override // android.security.keystore.IKeystoreService
            public int onUserPasswordChanged(int userId, String newPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(newPassword);
                    if (this.mRemote.transact(8, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onUserPasswordChanged(userId, newPassword);
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

            @Override // android.security.keystore.IKeystoreService
            public int lock(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(9, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().lock(userId);
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

            @Override // android.security.keystore.IKeystoreService
            public int unlock(int userId, String userPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(userPassword);
                    if (this.mRemote.transact(10, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlock(userId, userPassword);
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

            @Override // android.security.keystore.IKeystoreService
            public int isEmpty(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(11, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEmpty(userId);
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

            @Override // android.security.keystore.IKeystoreService
            public String grant(String name, int granteeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(granteeUid);
                    if (this.mRemote.transact(12, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().grant(name, granteeUid);
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

            @Override // android.security.keystore.IKeystoreService
            public int ungrant(String name, int granteeUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(granteeUid);
                    if (this.mRemote.transact(13, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ungrant(name, granteeUid);
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

            @Override // android.security.keystore.IKeystoreService
            public long getmtime(String name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(14, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getmtime(name, uid);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int is_hardware_backed(String string) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(string);
                    if (this.mRemote.transact(15, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().is_hardware_backed(string);
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

            @Override // android.security.keystore.IKeystoreService
            public int clear_uid(long uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(uid);
                    if (this.mRemote.transact(16, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clear_uid(uid);
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

            @Override // android.security.keystore.IKeystoreService
            public int addRngEntropy(IKeystoreResponseCallback cb, byte[] data, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeByteArray(data);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(17, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addRngEntropy(cb, data, flags);
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

            @Override // android.security.keystore.IKeystoreService
            public int generateKey(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterArguments arguments, byte[] entropy, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    try {
                        _data.writeString(alias);
                        if (arguments != null) {
                            _data.writeInt(1);
                            arguments.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeByteArray(entropy);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(uid);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        try {
                            if (this.mRemote.transact(18, _data, _reply, 0) == 1 || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int generateKey = Stub.getDefaultImpl().generateKey(cb, alias, arguments, entropy, uid, flags);
                            _reply.recycle();
                            _data.recycle();
                            return generateKey;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int getKeyCharacteristics(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterBlob clientId, KeymasterBlob appData, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(alias);
                    if (clientId != null) {
                        _data.writeInt(1);
                        clientId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appData != null) {
                        _data.writeInt(1);
                        appData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (this.mRemote.transact(19, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyCharacteristics(cb, alias, clientId, appData, uid);
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

            @Override // android.security.keystore.IKeystoreService
            public int importKey(IKeystoreKeyCharacteristicsCallback cb, String alias, KeymasterArguments arguments, int format, byte[] keyData, int uid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    try {
                        _data.writeString(alias);
                        if (arguments != null) {
                            _data.writeInt(1);
                            arguments.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(format);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(keyData);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _data.writeInt(flags);
                        if (this.mRemote.transact(20, _data, _reply, 0) == 1 || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int importKey = Stub.getDefaultImpl().importKey(cb, alias, arguments, format, keyData, uid, flags);
                        _reply.recycle();
                        _data.recycle();
                        return importKey;
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int exportKey(IKeystoreExportKeyCallback cb, String alias, int format, KeymasterBlob clientId, KeymasterBlob appData, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    try {
                        _data.writeString(alias);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(format);
                        if (clientId != null) {
                            _data.writeInt(1);
                            clientId.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (appData != null) {
                            _data.writeInt(1);
                            appData.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        try {
                            if (this.mRemote.transact(21, _data, _reply, 0) == 1 || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int exportKey = Stub.getDefaultImpl().exportKey(cb, alias, format, clientId, appData, uid);
                            _reply.recycle();
                            _data.recycle();
                            return exportKey;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int begin(IKeystoreOperationResultCallback cb, IBinder appToken, String alias, int purpose, boolean pruneable, KeymasterArguments params, byte[] entropy, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    try {
                        _data.writeStrongBinder(appToken);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(alias);
                        try {
                            _data.writeInt(purpose);
                            _data.writeInt(pruneable ? 1 : 0);
                            if (params != null) {
                                _data.writeInt(1);
                                params.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeByteArray(entropy);
                            _data.writeInt(uid);
                            if (this.mRemote.transact(22, _data, _reply, 0) == 1 || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int begin = Stub.getDefaultImpl().begin(cb, appToken, alias, purpose, pruneable, params, entropy, uid);
                            _reply.recycle();
                            _data.recycle();
                            return begin;
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int update(IKeystoreOperationResultCallback cb, IBinder token, KeymasterArguments params, byte[] input) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(input);
                    if (this.mRemote.transact(23, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().update(cb, token, params, input);
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

            @Override // android.security.keystore.IKeystoreService
            public int finish(IKeystoreOperationResultCallback cb, IBinder token, KeymasterArguments params, byte[] signature, byte[] entropy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(signature);
                    _data.writeByteArray(entropy);
                    if (this.mRemote.transact(24, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().finish(cb, token, params, signature, entropy);
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

            @Override // android.security.keystore.IKeystoreService
            public int abort(IKeystoreResponseCallback cb, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(25, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().abort(cb, token);
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

            @Override // android.security.keystore.IKeystoreService
            public int addAuthToken(byte[] authToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(authToken);
                    if (this.mRemote.transact(26, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addAuthToken(authToken);
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

            @Override // android.security.keystore.IKeystoreService
            public int onUserAdded(int userId, int parentId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(parentId);
                    if (this.mRemote.transact(27, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onUserAdded(userId, parentId);
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

            @Override // android.security.keystore.IKeystoreService
            public int onUserRemoved(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(28, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onUserRemoved(userId);
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

            @Override // android.security.keystore.IKeystoreService
            public int attestKey(IKeystoreCertificateChainCallback cb, String alias, KeymasterArguments params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(alias);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(29, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().attestKey(cb, alias, params);
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

            @Override // android.security.keystore.IKeystoreService
            public int attestDeviceIds(IKeystoreCertificateChainCallback cb, KeymasterArguments params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(30, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().attestDeviceIds(cb, params);
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

            @Override // android.security.keystore.IKeystoreService
            public int onDeviceOffBody() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(31, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onDeviceOffBody();
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

            @Override // android.security.keystore.IKeystoreService
            public int importWrappedKey(IKeystoreKeyCharacteristicsCallback cb, String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments arguments, long rootSid, long fingerprintSid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    try {
                        _data.writeString(wrappedKeyAlias);
                        _data.writeByteArray(wrappedKey);
                        _data.writeString(wrappingKeyAlias);
                        _data.writeByteArray(maskingKey);
                        if (arguments != null) {
                            _data.writeInt(1);
                            arguments.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeLong(rootSid);
                        _data.writeLong(fingerprintSid);
                        if (this.mRemote.transact(32, _data, _reply, 0) == 1 || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int importWrappedKey = Stub.getDefaultImpl().importWrappedKey(cb, wrappedKeyAlias, wrappedKey, wrappingKeyAlias, maskingKey, arguments, rootSid, fingerprintSid);
                        _reply.recycle();
                        _data.recycle();
                        return importWrappedKey;
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.security.keystore.IKeystoreService
            public int presentConfirmationPrompt(IBinder listener, String promptText, byte[] extraData, String locale, int uiOptionsAsFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener);
                    _data.writeString(promptText);
                    _data.writeByteArray(extraData);
                    _data.writeString(locale);
                    _data.writeInt(uiOptionsAsFlags);
                    if (this.mRemote.transact(33, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().presentConfirmationPrompt(listener, promptText, extraData, locale, uiOptionsAsFlags);
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

            @Override // android.security.keystore.IKeystoreService
            public int cancelConfirmationPrompt(IBinder listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener);
                    if (this.mRemote.transact(34, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelConfirmationPrompt(listener);
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

            @Override // android.security.keystore.IKeystoreService
            public boolean isConfirmationPromptSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (this.mRemote.transact(35, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConfirmationPromptSupported();
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

            @Override // android.security.keystore.IKeystoreService
            public int onKeyguardVisibilityChanged(boolean isShowing, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShowing ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(36, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onKeyguardVisibilityChanged(isShowing, userId);
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

            @Override // android.security.keystore.IKeystoreService
            public int listUidsOfAuthBoundKeys(List<String> uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(37, _data, _reply, 0) != 1 && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listUidsOfAuthBoundKeys(uids);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringList(uids);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IKeystoreService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IKeystoreService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
