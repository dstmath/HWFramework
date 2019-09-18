package android.os;

import java.io.FileDescriptor;

public interface IInstalld extends IInterface {

    public static abstract class Stub extends Binder implements IInstalld {
        private static final String DESCRIPTOR = "android.os.IInstalld";
        static final int TRANSACTION_assertFsverityRootHashMatches = 34;
        static final int TRANSACTION_clearAppData = 6;
        static final int TRANSACTION_clearAppProfiles = 19;
        static final int TRANSACTION_copySystemProfile = 18;
        static final int TRANSACTION_createAppData = 3;
        static final int TRANSACTION_createOatDir = 29;
        static final int TRANSACTION_createProfileSnapshot = 21;
        static final int TRANSACTION_createUserData = 1;
        static final int TRANSACTION_deleteOdex = 32;
        static final int TRANSACTION_destroyAppData = 7;
        static final int TRANSACTION_destroyAppProfiles = 20;
        static final int TRANSACTION_destroyProfileSnapshot = 22;
        static final int TRANSACTION_destroyUserData = 2;
        static final int TRANSACTION_dexopt = 14;
        static final int TRANSACTION_dumpProfiles = 17;
        static final int TRANSACTION_executeBackupTask = 42;
        static final int TRANSACTION_finishBackupSession = 43;
        static final int TRANSACTION_fixupAppData = 8;
        static final int TRANSACTION_freeCache = 27;
        static final int TRANSACTION_getAppSize = 9;
        static final int TRANSACTION_getDexFileOptimizationStatus = 49;
        static final int TRANSACTION_getDexFileOutputPaths = 47;
        static final int TRANSACTION_getDexFileStatus = 46;
        static final int TRANSACTION_getDexOptNeeded = 45;
        static final int TRANSACTION_getExternalSize = 11;
        static final int TRANSACTION_getUserSize = 10;
        static final int TRANSACTION_hashSecondaryDexFile = 36;
        static final int TRANSACTION_idmap = 23;
        static final int TRANSACTION_installApkVerity = 33;
        static final int TRANSACTION_invalidateMounts = 37;
        static final int TRANSACTION_isDexOptNeeded = 48;
        static final int TRANSACTION_isQuotaSupported = 38;
        static final int TRANSACTION_linkFile = 30;
        static final int TRANSACTION_linkNativeLibraryDirectory = 28;
        static final int TRANSACTION_markBootComplete = 26;
        static final int TRANSACTION_mergeProfiles = 16;
        static final int TRANSACTION_migrateAppData = 5;
        static final int TRANSACTION_moveAb = 31;
        static final int TRANSACTION_moveCompleteApp = 13;
        static final int TRANSACTION_prepareAppProfile = 39;
        static final int TRANSACTION_reconcileSecondaryDexFile = 35;
        static final int TRANSACTION_removeIdmap = 24;
        static final int TRANSACTION_restoreCloneAppData = 40;
        static final int TRANSACTION_restoreconAppData = 4;
        static final int TRANSACTION_rmPackageDir = 25;
        static final int TRANSACTION_rmdex = 15;
        static final int TRANSACTION_setAppQuota = 12;
        static final int TRANSACTION_setFileXattr = 44;
        static final int TRANSACTION_startBackupSession = 41;

        private static class Proxy implements IInstalld {
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

            public void createUserData(String uuid, int userId, int userSerial, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(flags);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyUserData(String uuid, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long createAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeInt(appId);
                    _data.writeString(seInfo);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreconAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeInt(appId);
                    _data.writeString(seInfo);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void migrateAppData(String uuid, String packageName, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeLong(ceDataInode);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeLong(ceDataInode);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fixupAppData(String uuid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(flags);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getAppSize(String uuid, String[] packageNames, int userId, int flags, int appId, long[] ceDataInodes, String[] codePaths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeInt(appId);
                    _data.writeLongArray(ceDataInodes);
                    _data.writeStringArray(codePaths);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createLongArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getUserSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeIntArray(appIds);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createLongArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getExternalSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeIntArray(appIds);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createLongArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppQuota(String uuid, int userId, int appId, long cacheQuota) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(appId);
                    _data.writeLong(cacheQuota);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveCompleteApp(String fromUuid, String toUuid, String packageName, String dataAppName, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromUuid);
                    _data.writeString(toUuid);
                    _data.writeString(packageName);
                    _data.writeString(dataAppName);
                    _data.writeInt(appId);
                    _data.writeString(seInfo);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dexopt(String apkPath, int uid, String packageName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String uuid, String sharedLibraries, String seInfo, boolean downgrade, int targetSdkVersion, String profileName, String dexMetadataPath, String compilationReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeInt(uid);
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeString(instructionSet);
                        } catch (Throwable th) {
                            th = th;
                            int i = dexoptNeeded;
                            String str = outputPath;
                            int i2 = dexFlags;
                            String str2 = compilerFilter;
                            String str3 = uuid;
                            String str4 = sharedLibraries;
                            String str5 = seInfo;
                            boolean z = downgrade;
                            int i3 = targetSdkVersion;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        String str6 = instructionSet;
                        int i4 = dexoptNeeded;
                        String str7 = outputPath;
                        int i22 = dexFlags;
                        String str22 = compilerFilter;
                        String str32 = uuid;
                        String str42 = sharedLibraries;
                        String str52 = seInfo;
                        boolean z2 = downgrade;
                        int i32 = targetSdkVersion;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(dexoptNeeded);
                        try {
                            _data.writeString(outputPath);
                            try {
                                _data.writeInt(dexFlags);
                            } catch (Throwable th3) {
                                th = th3;
                                String str222 = compilerFilter;
                                String str322 = uuid;
                                String str422 = sharedLibraries;
                                String str522 = seInfo;
                                boolean z22 = downgrade;
                                int i322 = targetSdkVersion;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i222 = dexFlags;
                            String str2222 = compilerFilter;
                            String str3222 = uuid;
                            String str4222 = sharedLibraries;
                            String str5222 = seInfo;
                            boolean z222 = downgrade;
                            int i3222 = targetSdkVersion;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        String str72 = outputPath;
                        int i2222 = dexFlags;
                        String str22222 = compilerFilter;
                        String str32222 = uuid;
                        String str42222 = sharedLibraries;
                        String str52222 = seInfo;
                        boolean z2222 = downgrade;
                        int i32222 = targetSdkVersion;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(compilerFilter);
                        try {
                            _data.writeString(uuid);
                            try {
                                _data.writeString(sharedLibraries);
                            } catch (Throwable th6) {
                                th = th6;
                                String str522222 = seInfo;
                                boolean z22222 = downgrade;
                                int i322222 = targetSdkVersion;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            String str422222 = sharedLibraries;
                            String str5222222 = seInfo;
                            boolean z222222 = downgrade;
                            int i3222222 = targetSdkVersion;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        String str322222 = uuid;
                        String str4222222 = sharedLibraries;
                        String str52222222 = seInfo;
                        boolean z2222222 = downgrade;
                        int i32222222 = targetSdkVersion;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(seInfo);
                        try {
                            _data.writeInt(downgrade ? 1 : 0);
                            try {
                                _data.writeInt(targetSdkVersion);
                                _data.writeString(profileName);
                                _data.writeString(dexMetadataPath);
                                _data.writeString(compilationReason);
                                this.mRemote.transact(14, _data, _reply, 0);
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th9) {
                                th = th9;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            int i322222222 = targetSdkVersion;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        boolean z22222222 = downgrade;
                        int i3222222222 = targetSdkVersion;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    String str8 = packageName;
                    String str62 = instructionSet;
                    int i42 = dexoptNeeded;
                    String str722 = outputPath;
                    int i22222 = dexFlags;
                    String str222222 = compilerFilter;
                    String str3222222 = uuid;
                    String str42222222 = sharedLibraries;
                    String str522222222 = seInfo;
                    boolean z222222222 = downgrade;
                    int i32222222222 = targetSdkVersion;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void rmdex(String codePath, String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(codePath);
                    _data.writeString(instructionSet);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean mergeProfiles(int uid, String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean dumpProfiles(int uid, String packageName, String profileName, String codePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    _data.writeString(codePath);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean copySystemProfile(String systemProfile, int uid, String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(systemProfile);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearAppProfiles(String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyAppProfiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean createProfileSnapshot(int appId, String packageName, String profileName, String classpath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appId);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    _data.writeString(classpath);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyProfileSnapshot(String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void idmap(String targetApkPath, String overlayApkPath, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetApkPath);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(uid);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeIdmap(String overlayApkPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(overlayApkPath);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rmPackageDir(String packageDir) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageDir);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void markBootComplete(String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(instructionSet);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freeCache(String uuid, long targetFreeBytes, long cacheReservedBytes, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeLong(targetFreeBytes);
                    _data.writeLong(cacheReservedBytes);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_freeCache, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void linkNativeLibraryDirectory(String uuid, String packageName, String nativeLibPath32, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeString(nativeLibPath32);
                    _data.writeInt(userId);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createOatDir(String oatDir, String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oatDir);
                    _data.writeString(instructionSet);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void linkFile(String relativePath, String fromBase, String toBase) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(relativePath);
                    _data.writeString(fromBase);
                    _data.writeString(toBase);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveAb(String apkPath, String instructionSet, String outputPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeString(instructionSet);
                    _data.writeString(outputPath);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteOdex(String apkPath, String instructionSet, String outputPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeString(instructionSet);
                    _data.writeString(outputPath);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void installApkVerity(String filePath, FileDescriptor verityInput, int contentSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeRawFileDescriptor(verityInput);
                    _data.writeInt(contentSize);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void assertFsverityRootHashMatches(String filePath, byte[] expectedHash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeByteArray(expectedHash);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean reconcileSecondaryDexFile(String dexPath, String pkgName, int uid, String[] isas, String volume_uuid, int storage_flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dexPath);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeStringArray(isas);
                    _data.writeString(volume_uuid);
                    _data.writeInt(storage_flag);
                    boolean _result = false;
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] hashSecondaryDexFile(String dexPath, String pkgName, int uid, String volumeUuid, int storageFlag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dexPath);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeString(volumeUuid);
                    _data.writeInt(storageFlag);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void invalidateMounts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isQuotaSupported(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    boolean _result = false;
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean prepareAppProfile(String packageName, int userId, int appId, String profileName, String codePath, String dexMetadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(appId);
                    _data.writeString(profileName);
                    _data.writeString(codePath);
                    _data.writeString(dexMetadata);
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreCloneAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo, String parentDataUserCePkgDir, String cloneDataUserCePkgDir, String parentDataUserDePkgDir, String cloneDataUserDePkgDir) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeInt(appId);
                    _data.writeString(seInfo);
                    _data.writeString(parentDataUserCePkgDir);
                    _data.writeString(cloneDataUserCePkgDir);
                    _data.writeString(parentDataUserDePkgDir);
                    _data.writeString(cloneDataUserDePkgDir);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(taskCmd);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int finishBackupSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFileXattr(String path, String keyDesc, int storageType, int fileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(keyDesc);
                    _data.writeInt(storageType);
                    _data.writeInt(fileType);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getDexOptNeeded(String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeStringArray(instructionSets);
                    _data.writeStringArray(compilerFilters);
                    _data.writeStringArray(clContexts);
                    _data.writeBooleanArray(newProfiles);
                    _data.writeBooleanArray(downGrades);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getDexFileStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] dex_file_status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeStringArray(instructionSets);
                    _data.writeIntArray(uids);
                    if (dex_file_status == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(dex_file_status.length);
                    }
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringArray(dex_file_status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getDexFileOutputPaths(String fileName, String instructionSet, int uid, String[] output_paths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    _data.writeString(instructionSet);
                    _data.writeInt(uid);
                    if (output_paths == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(output_paths.length);
                    }
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringArray(output_paths);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean[] isDexOptNeeded(String[] fileNames, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBooleanArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getDexFileOptimizationStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] opt_status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeStringArray(instructionSets);
                    _data.writeIntArray(uids);
                    if (opt_status == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(opt_status.length);
                    }
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringArray(opt_status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInstalld asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInstalld)) {
                return new Proxy(obj);
            }
            return (IInstalld) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public boolean onTransact(int r36, android.os.Parcel r37, android.os.Parcel r38, int r39) throws android.os.RemoteException {
            /*
                r35 = this;
                r15 = r35
                r14 = r36
                r13 = r37
                r11 = r38
                java.lang.String r12 = "android.os.IInstalld"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r17 = 1
                if (r14 == r0) goto L_0x0620
                switch(r14) {
                    case 1: goto L_0x0603;
                    case 2: goto L_0x05ea;
                    case 3: goto L_0x05b2;
                    case 4: goto L_0x0585;
                    case 5: goto L_0x0568;
                    case 6: goto L_0x0541;
                    case 7: goto L_0x051a;
                    case 8: goto L_0x0505;
                    case 9: goto L_0x04cd;
                    case 10: goto L_0x04ac;
                    case 11: goto L_0x0488;
                    case 12: goto L_0x0466;
                    case 13: goto L_0x0430;
                    case 14: goto L_0x03bd;
                    case 15: goto L_0x03ab;
                    case 16: goto L_0x0391;
                    case 17: goto L_0x0373;
                    case 18: goto L_0x0355;
                    case 19: goto L_0x0343;
                    case 20: goto L_0x0335;
                    case 21: goto L_0x0317;
                    case 22: goto L_0x0305;
                    case 23: goto L_0x02ef;
                    case 24: goto L_0x02e1;
                    case 25: goto L_0x02d3;
                    case 26: goto L_0x02c5;
                    case 27: goto L_0x02a5;
                    case 28: goto L_0x028b;
                    case 29: goto L_0x0279;
                    case 30: goto L_0x0263;
                    case 31: goto L_0x024d;
                    case 32: goto L_0x0237;
                    case 33: goto L_0x0221;
                    case 34: goto L_0x020f;
                    case 35: goto L_0x01e0;
                    case 36: goto L_0x01b8;
                    case 37: goto L_0x01ae;
                    case 38: goto L_0x019c;
                    case 39: goto L_0x016d;
                    case 40: goto L_0x0126;
                    case 41: goto L_0x0110;
                    case 42: goto L_0x00fa;
                    case 43: goto L_0x00e8;
                    case 44: goto L_0x00ce;
                    case 45: goto L_0x0098;
                    case 46: goto L_0x0075;
                    case 47: goto L_0x0052;
                    case 48: goto L_0x003c;
                    case 49: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r0 = super.onTransact(r36, r37, r38, r39)
                return r0
            L_0x0019:
                r13.enforceInterface(r12)
                java.lang.String[] r0 = r37.createStringArray()
                java.lang.String[] r1 = r37.createStringArray()
                int[] r2 = r37.createIntArray()
                int r3 = r37.readInt()
                if (r3 >= 0) goto L_0x0030
                r4 = 0
                goto L_0x0032
            L_0x0030:
                java.lang.String[] r4 = new java.lang.String[r3]
            L_0x0032:
                r15.getDexFileOptimizationStatus(r0, r1, r2, r4)
                r38.writeNoException()
                r11.writeStringArray(r4)
                return r17
            L_0x003c:
                r13.enforceInterface(r12)
                java.lang.String[] r0 = r37.createStringArray()
                int[] r1 = r37.createIntArray()
                boolean[] r2 = r15.isDexOptNeeded(r0, r1)
                r38.writeNoException()
                r11.writeBooleanArray(r2)
                return r17
            L_0x0052:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                int r2 = r37.readInt()
                int r3 = r37.readInt()
                if (r3 >= 0) goto L_0x0069
                r4 = 0
                goto L_0x006b
            L_0x0069:
                java.lang.String[] r4 = new java.lang.String[r3]
            L_0x006b:
                r15.getDexFileOutputPaths(r0, r1, r2, r4)
                r38.writeNoException()
                r11.writeStringArray(r4)
                return r17
            L_0x0075:
                r13.enforceInterface(r12)
                java.lang.String[] r0 = r37.createStringArray()
                java.lang.String[] r1 = r37.createStringArray()
                int[] r2 = r37.createIntArray()
                int r3 = r37.readInt()
                if (r3 >= 0) goto L_0x008c
                r4 = 0
                goto L_0x008e
            L_0x008c:
                java.lang.String[] r4 = new java.lang.String[r3]
            L_0x008e:
                r15.getDexFileStatus(r0, r1, r2, r4)
                r38.writeNoException()
                r11.writeStringArray(r4)
                return r17
            L_0x0098:
                r13.enforceInterface(r12)
                java.lang.String[] r8 = r37.createStringArray()
                java.lang.String[] r9 = r37.createStringArray()
                java.lang.String[] r10 = r37.createStringArray()
                java.lang.String[] r16 = r37.createStringArray()
                boolean[] r18 = r37.createBooleanArray()
                boolean[] r19 = r37.createBooleanArray()
                int[] r20 = r37.createIntArray()
                r0 = r15
                r1 = r8
                r2 = r9
                r3 = r10
                r4 = r16
                r5 = r18
                r6 = r19
                r7 = r20
                int[] r0 = r0.getDexOptNeeded(r1, r2, r3, r4, r5, r6, r7)
                r38.writeNoException()
                r11.writeIntArray(r0)
                return r17
            L_0x00ce:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                int r2 = r37.readInt()
                int r3 = r37.readInt()
                r15.setFileXattr(r0, r1, r2, r3)
                r38.writeNoException()
                return r17
            L_0x00e8:
                r13.enforceInterface(r12)
                int r0 = r37.readInt()
                int r1 = r15.finishBackupSession(r0)
                r38.writeNoException()
                r11.writeInt(r1)
                return r17
            L_0x00fa:
                r13.enforceInterface(r12)
                int r0 = r37.readInt()
                java.lang.String r1 = r37.readString()
                int r2 = r15.executeBackupTask(r0, r1)
                r38.writeNoException()
                r11.writeInt(r2)
                return r17
            L_0x0110:
                r13.enforceInterface(r12)
                android.os.IBinder r0 = r37.readStrongBinder()
                android.os.IBackupSessionCallback r0 = android.os.IBackupSessionCallback.Stub.asInterface(r0)
                int r1 = r15.startBackupSession(r0)
                r38.writeNoException()
                r11.writeInt(r1)
                return r17
            L_0x0126:
                r13.enforceInterface(r12)
                java.lang.String r16 = r37.readString()
                java.lang.String r18 = r37.readString()
                int r19 = r37.readInt()
                int r20 = r37.readInt()
                int r21 = r37.readInt()
                java.lang.String r22 = r37.readString()
                java.lang.String r23 = r37.readString()
                java.lang.String r24 = r37.readString()
                java.lang.String r25 = r37.readString()
                java.lang.String r26 = r37.readString()
                r0 = r15
                r1 = r16
                r2 = r18
                r3 = r19
                r4 = r20
                r5 = r21
                r6 = r22
                r7 = r23
                r8 = r24
                r9 = r25
                r10 = r26
                r0.restoreCloneAppData(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r38.writeNoException()
                return r17
            L_0x016d:
                r13.enforceInterface(r12)
                java.lang.String r7 = r37.readString()
                int r8 = r37.readInt()
                int r9 = r37.readInt()
                java.lang.String r10 = r37.readString()
                java.lang.String r16 = r37.readString()
                java.lang.String r18 = r37.readString()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r16
                r6 = r18
                boolean r0 = r0.prepareAppProfile(r1, r2, r3, r4, r5, r6)
                r38.writeNoException()
                r11.writeInt(r0)
                return r17
            L_0x019c:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                boolean r1 = r15.isQuotaSupported(r0)
                r38.writeNoException()
                r11.writeInt(r1)
                return r17
            L_0x01ae:
                r13.enforceInterface(r12)
                r35.invalidateMounts()
                r38.writeNoException()
                return r17
            L_0x01b8:
                r13.enforceInterface(r12)
                java.lang.String r6 = r37.readString()
                java.lang.String r7 = r37.readString()
                int r8 = r37.readInt()
                java.lang.String r9 = r37.readString()
                int r10 = r37.readInt()
                r0 = r15
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                byte[] r0 = r0.hashSecondaryDexFile(r1, r2, r3, r4, r5)
                r38.writeNoException()
                r11.writeByteArray(r0)
                return r17
            L_0x01e0:
                r13.enforceInterface(r12)
                java.lang.String r7 = r37.readString()
                java.lang.String r8 = r37.readString()
                int r9 = r37.readInt()
                java.lang.String[] r10 = r37.createStringArray()
                java.lang.String r16 = r37.readString()
                int r18 = r37.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r10
                r5 = r16
                r6 = r18
                boolean r0 = r0.reconcileSecondaryDexFile(r1, r2, r3, r4, r5, r6)
                r38.writeNoException()
                r11.writeInt(r0)
                return r17
            L_0x020f:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                byte[] r1 = r37.createByteArray()
                r15.assertFsverityRootHashMatches(r0, r1)
                r38.writeNoException()
                return r17
            L_0x0221:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.io.FileDescriptor r1 = r37.readRawFileDescriptor()
                int r2 = r37.readInt()
                r15.installApkVerity(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x0237:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                r15.deleteOdex(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x024d:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                r15.moveAb(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x0263:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                r15.linkFile(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x0279:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                r15.createOatDir(r0, r1)
                r38.writeNoException()
                return r17
            L_0x028b:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                int r3 = r37.readInt()
                r15.linkNativeLibraryDirectory(r0, r1, r2, r3)
                r38.writeNoException()
                return r17
            L_0x02a5:
                r13.enforceInterface(r12)
                java.lang.String r7 = r37.readString()
                long r8 = r37.readLong()
                long r18 = r37.readLong()
                int r10 = r37.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r4 = r18
                r6 = r10
                r0.freeCache(r1, r2, r4, r6)
                r38.writeNoException()
                return r17
            L_0x02c5:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                r15.markBootComplete(r0)
                r38.writeNoException()
                return r17
            L_0x02d3:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                r15.rmPackageDir(r0)
                r38.writeNoException()
                return r17
            L_0x02e1:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                r15.removeIdmap(r0)
                r38.writeNoException()
                return r17
            L_0x02ef:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                int r2 = r37.readInt()
                r15.idmap(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x0305:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                r15.destroyProfileSnapshot(r0, r1)
                r38.writeNoException()
                return r17
            L_0x0317:
                r13.enforceInterface(r12)
                int r0 = r37.readInt()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                java.lang.String r3 = r37.readString()
                boolean r4 = r15.createProfileSnapshot(r0, r1, r2, r3)
                r38.writeNoException()
                r11.writeInt(r4)
                return r17
            L_0x0335:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                r15.destroyAppProfiles(r0)
                r38.writeNoException()
                return r17
            L_0x0343:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                r15.clearAppProfiles(r0, r1)
                r38.writeNoException()
                return r17
            L_0x0355:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                java.lang.String r2 = r37.readString()
                java.lang.String r3 = r37.readString()
                boolean r4 = r15.copySystemProfile(r0, r1, r2, r3)
                r38.writeNoException()
                r11.writeInt(r4)
                return r17
            L_0x0373:
                r13.enforceInterface(r12)
                int r0 = r37.readInt()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                java.lang.String r3 = r37.readString()
                boolean r4 = r15.dumpProfiles(r0, r1, r2, r3)
                r38.writeNoException()
                r11.writeInt(r4)
                return r17
            L_0x0391:
                r13.enforceInterface(r12)
                int r0 = r37.readInt()
                java.lang.String r1 = r37.readString()
                java.lang.String r2 = r37.readString()
                boolean r3 = r15.mergeProfiles(r0, r1, r2)
                r38.writeNoException()
                r11.writeInt(r3)
                return r17
            L_0x03ab:
                r13.enforceInterface(r12)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                r15.rmdex(r0, r1)
                r38.writeNoException()
                return r17
            L_0x03bd:
                r13.enforceInterface(r12)
                java.lang.String r18 = r37.readString()
                int r19 = r37.readInt()
                java.lang.String r20 = r37.readString()
                java.lang.String r21 = r37.readString()
                int r22 = r37.readInt()
                java.lang.String r23 = r37.readString()
                int r24 = r37.readInt()
                java.lang.String r25 = r37.readString()
                java.lang.String r26 = r37.readString()
                java.lang.String r27 = r37.readString()
                java.lang.String r28 = r37.readString()
                int r0 = r37.readInt()
                if (r0 == 0) goto L_0x03f5
                r0 = r17
                goto L_0x03f6
            L_0x03f5:
                r0 = 0
            L_0x03f6:
                r10 = r12
                r12 = r0
                int r29 = r37.readInt()
                java.lang.String r30 = r37.readString()
                java.lang.String r31 = r37.readString()
                java.lang.String r32 = r37.readString()
                r0 = r15
                r1 = r18
                r2 = r19
                r3 = r20
                r4 = r21
                r5 = r22
                r6 = r23
                r7 = r24
                r8 = r25
                r9 = r26
                r33 = r10
                r10 = r27
                r11 = r28
                r13 = r29
                r14 = r30
                r15 = r31
                r16 = r32
                r0.dexopt(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16)
                r38.writeNoException()
                return r17
            L_0x0430:
                r33 = r12
                r9 = r33
                r8 = r37
                r8.enforceInterface(r9)
                java.lang.String r10 = r37.readString()
                java.lang.String r11 = r37.readString()
                java.lang.String r12 = r37.readString()
                java.lang.String r13 = r37.readString()
                int r14 = r37.readInt()
                java.lang.String r15 = r37.readString()
                int r16 = r37.readInt()
                r0 = r35
                r1 = r10
                r2 = r11
                r3 = r12
                r4 = r13
                r5 = r14
                r6 = r15
                r7 = r16
                r0.moveCompleteApp(r1, r2, r3, r4, r5, r6, r7)
                r38.writeNoException()
                return r17
            L_0x0466:
                r9 = r12
                r8 = r13
                r8.enforceInterface(r9)
                java.lang.String r6 = r37.readString()
                int r7 = r37.readInt()
                int r10 = r37.readInt()
                long r11 = r37.readLong()
                r0 = r35
                r1 = r6
                r2 = r7
                r3 = r10
                r4 = r11
                r0.setAppQuota(r1, r2, r3, r4)
                r38.writeNoException()
                return r17
            L_0x0488:
                r9 = r12
                r8 = r13
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                int r2 = r37.readInt()
                int[] r3 = r37.createIntArray()
                r10 = r35
                long[] r4 = r10.getExternalSize(r0, r1, r2, r3)
                r38.writeNoException()
                r11 = r38
                r11.writeLongArray(r4)
                return r17
            L_0x04ac:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                int r2 = r37.readInt()
                int[] r3 = r37.createIntArray()
                long[] r4 = r10.getUserSize(r0, r1, r2, r3)
                r38.writeNoException()
                r11.writeLongArray(r4)
                return r17
            L_0x04cd:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r12 = r37.readString()
                java.lang.String[] r13 = r37.createStringArray()
                int r14 = r37.readInt()
                int r15 = r37.readInt()
                int r16 = r37.readInt()
                long[] r18 = r37.createLongArray()
                java.lang.String[] r19 = r37.createStringArray()
                r0 = r10
                r1 = r12
                r2 = r13
                r3 = r14
                r4 = r15
                r5 = r16
                r6 = r18
                r7 = r19
                long[] r0 = r0.getAppSize(r1, r2, r3, r4, r5, r6, r7)
                r38.writeNoException()
                r11.writeLongArray(r0)
                return r17
            L_0x0505:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                r10.fixupAppData(r0, r1)
                r38.writeNoException()
                return r17
            L_0x051a:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r7 = r37.readString()
                java.lang.String r12 = r37.readString()
                int r13 = r37.readInt()
                int r14 = r37.readInt()
                long r15 = r37.readLong()
                r0 = r10
                r1 = r7
                r2 = r12
                r3 = r13
                r4 = r14
                r5 = r15
                r0.destroyAppData(r1, r2, r3, r4, r5)
                r38.writeNoException()
                return r17
            L_0x0541:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r7 = r37.readString()
                java.lang.String r12 = r37.readString()
                int r13 = r37.readInt()
                int r14 = r37.readInt()
                long r15 = r37.readLong()
                r0 = r10
                r1 = r7
                r2 = r12
                r3 = r13
                r4 = r14
                r5 = r15
                r0.clearAppData(r1, r2, r3, r4, r5)
                r38.writeNoException()
                return r17
            L_0x0568:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                java.lang.String r1 = r37.readString()
                int r2 = r37.readInt()
                int r3 = r37.readInt()
                r10.migrateAppData(r0, r1, r2, r3)
                r38.writeNoException()
                return r17
            L_0x0585:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r7 = r37.readString()
                java.lang.String r12 = r37.readString()
                int r13 = r37.readInt()
                int r14 = r37.readInt()
                int r15 = r37.readInt()
                java.lang.String r16 = r37.readString()
                r0 = r10
                r1 = r7
                r2 = r12
                r3 = r13
                r4 = r14
                r5 = r15
                r6 = r16
                r0.restoreconAppData(r1, r2, r3, r4, r5, r6)
                r38.writeNoException()
                return r17
            L_0x05b2:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r12 = r37.readString()
                java.lang.String r13 = r37.readString()
                int r14 = r37.readInt()
                int r15 = r37.readInt()
                int r16 = r37.readInt()
                java.lang.String r18 = r37.readString()
                int r19 = r37.readInt()
                r0 = r10
                r1 = r12
                r2 = r13
                r3 = r14
                r4 = r15
                r5 = r16
                r6 = r18
                r7 = r19
                long r0 = r0.createAppData(r1, r2, r3, r4, r5, r6, r7)
                r38.writeNoException()
                r11.writeLong(r0)
                return r17
            L_0x05ea:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                int r2 = r37.readInt()
                r10.destroyUserData(r0, r1, r2)
                r38.writeNoException()
                return r17
            L_0x0603:
                r9 = r12
                r8 = r13
                r10 = r15
                r8.enforceInterface(r9)
                java.lang.String r0 = r37.readString()
                int r1 = r37.readInt()
                int r2 = r37.readInt()
                int r3 = r37.readInt()
                r10.createUserData(r0, r1, r2, r3)
                r38.writeNoException()
                return r17
            L_0x0620:
                r9 = r12
                r8 = r13
                r10 = r15
                r11.writeString(r9)
                return r17
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.IInstalld.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void assertFsverityRootHashMatches(String str, byte[] bArr) throws RemoteException;

    void clearAppData(String str, String str2, int i, int i2, long j) throws RemoteException;

    void clearAppProfiles(String str, String str2) throws RemoteException;

    boolean copySystemProfile(String str, int i, String str2, String str3) throws RemoteException;

    long createAppData(String str, String str2, int i, int i2, int i3, String str3, int i4) throws RemoteException;

    void createOatDir(String str, String str2) throws RemoteException;

    boolean createProfileSnapshot(int i, String str, String str2, String str3) throws RemoteException;

    void createUserData(String str, int i, int i2, int i3) throws RemoteException;

    void deleteOdex(String str, String str2, String str3) throws RemoteException;

    void destroyAppData(String str, String str2, int i, int i2, long j) throws RemoteException;

    void destroyAppProfiles(String str) throws RemoteException;

    void destroyProfileSnapshot(String str, String str2) throws RemoteException;

    void destroyUserData(String str, int i, int i2) throws RemoteException;

    void dexopt(String str, int i, String str2, String str3, int i2, String str4, int i3, String str5, String str6, String str7, String str8, boolean z, int i4, String str9, String str10, String str11) throws RemoteException;

    boolean dumpProfiles(int i, String str, String str2, String str3) throws RemoteException;

    int executeBackupTask(int i, String str) throws RemoteException;

    int finishBackupSession(int i) throws RemoteException;

    void fixupAppData(String str, int i) throws RemoteException;

    void freeCache(String str, long j, long j2, int i) throws RemoteException;

    long[] getAppSize(String str, String[] strArr, int i, int i2, int i3, long[] jArr, String[] strArr2) throws RemoteException;

    void getDexFileOptimizationStatus(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) throws RemoteException;

    void getDexFileOutputPaths(String str, String str2, int i, String[] strArr) throws RemoteException;

    void getDexFileStatus(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) throws RemoteException;

    int[] getDexOptNeeded(String[] strArr, String[] strArr2, String[] strArr3, String[] strArr4, boolean[] zArr, boolean[] zArr2, int[] iArr) throws RemoteException;

    long[] getExternalSize(String str, int i, int i2, int[] iArr) throws RemoteException;

    long[] getUserSize(String str, int i, int i2, int[] iArr) throws RemoteException;

    byte[] hashSecondaryDexFile(String str, String str2, int i, String str3, int i2) throws RemoteException;

    void idmap(String str, String str2, int i) throws RemoteException;

    void installApkVerity(String str, FileDescriptor fileDescriptor, int i) throws RemoteException;

    void invalidateMounts() throws RemoteException;

    boolean[] isDexOptNeeded(String[] strArr, int[] iArr) throws RemoteException;

    boolean isQuotaSupported(String str) throws RemoteException;

    void linkFile(String str, String str2, String str3) throws RemoteException;

    void linkNativeLibraryDirectory(String str, String str2, String str3, int i) throws RemoteException;

    void markBootComplete(String str) throws RemoteException;

    boolean mergeProfiles(int i, String str, String str2) throws RemoteException;

    void migrateAppData(String str, String str2, int i, int i2) throws RemoteException;

    void moveAb(String str, String str2, String str3) throws RemoteException;

    void moveCompleteApp(String str, String str2, String str3, String str4, int i, String str5, int i2) throws RemoteException;

    boolean prepareAppProfile(String str, int i, int i2, String str2, String str3, String str4) throws RemoteException;

    boolean reconcileSecondaryDexFile(String str, String str2, int i, String[] strArr, String str3, int i2) throws RemoteException;

    void removeIdmap(String str) throws RemoteException;

    void restoreCloneAppData(String str, String str2, int i, int i2, int i3, String str3, String str4, String str5, String str6, String str7) throws RemoteException;

    void restoreconAppData(String str, String str2, int i, int i2, int i3, String str3) throws RemoteException;

    void rmPackageDir(String str) throws RemoteException;

    void rmdex(String str, String str2) throws RemoteException;

    void setAppQuota(String str, int i, int i2, long j) throws RemoteException;

    void setFileXattr(String str, String str2, int i, int i2) throws RemoteException;

    int startBackupSession(IBackupSessionCallback iBackupSessionCallback) throws RemoteException;
}
