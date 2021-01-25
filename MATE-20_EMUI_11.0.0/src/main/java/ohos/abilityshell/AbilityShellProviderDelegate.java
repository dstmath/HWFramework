package ohos.abilityshell;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.Libcore;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityResult;
import ohos.aafwk.ability.OperationExecuteException;
import ohos.abilityshell.utils.AbilityShellConverterUtils;
import ohos.abilityshell.utils.LifecycleState;
import ohos.app.ContextDeal;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.ShellInfo;
import ohos.data.dataability.ContentProviderConverter;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.dataability.RemoteDataAbility;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.RawFileDescriptor;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.tools.Bytrace;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class AbilityShellProviderDelegate extends AbilityShellDelegate {
    private static final String GET_REMOTE_DATA_ABILITY = "DMS_GetRemoteDataAbility";
    private static final String GET_REMOTE_DATA_ABILITY_KEY = "RemoteDataAbility";
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static final int WAIT_PROVIDER_CREATE_DONE_TIMEOUT = 2000;
    private Object abilityShell;
    private ContextDeal contextDeal;
    private final CountDownLatch createLatch = new CountDownLatch(1);
    private ShellInfo shellInfo;

    public AbilityShellProviderDelegate(Object obj) {
        this.abilityShell = obj;
    }

    public boolean onCreate() {
        this.abilityInfo = AbilityShellConverterUtils.convertToAbilityInfo(this.shellInfo);
        if (this.abilityInfo == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::onCreate could not find ability info from bms, stop start!", new Object[0]);
            return false;
        }
        HarmonyApplication.registerDataAbility(new Runnable() {
            /* class ohos.abilityshell.$$Lambda$AbilityShellProviderDelegate$ZN38_YHwuO9SXx31gArK7O8emEU */

            @Override // java.lang.Runnable
            public final void run() {
                AbilityShellProviderDelegate.this.lambda$onCreate$0$AbilityShellProviderDelegate();
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$onCreate$0$AbilityShellProviderDelegate() {
        AppLog.i(SHELL_LABEL, "AbilityShellProviderDelegate::onCreate registerDataAbility", new Object[0]);
        Bytrace.startTrace(2147483648L, "create Data Ability");
        BundleInfo bundleInfo = HarmonyApplication.getInstance().getBundleInfo();
        if (bundleInfo.isDifferentName()) {
            this.abilityInfo.setClassName(this.abilityInfo.getClassName().replaceFirst(bundleInfo.getOriginalName(), bundleInfo.getName()));
            AppLog.d(SHELL_LABEL, "AbilityShellProviderDelegate::onCreate ability class name %{private}s", this.abilityInfo.getClassName());
        }
        AbilityInfo abilityInfoByName = bundleInfo.getAbilityInfoByName(this.abilityInfo.getClassName());
        if (abilityInfoByName != null) {
            this.abilityInfo = abilityInfoByName;
        }
        this.contextDeal = createProviderContextdeal(this.abilityInfo);
        checkHapHasLoaded(this.abilityInfo);
        if (this.abilityShell instanceof ContentProvider) {
            loadAbility(this.abilityInfo, this.contextDeal, this.abilityShell);
            HarmonyApplication.getInstance().waitForUserApplicationStart();
            scheduleAbilityLifecycle(null, LifecycleState.AbilityState.INACTIVE_STATE.getValue());
            this.createLatch.countDown();
        }
        Bytrace.finishTrace(2147483648L, "create Data Ability");
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider query");
        DataAbilityPredicates selectionToPredicates = selectionToPredicates(uri, str, strArr2, str2);
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability query");
        ResultSet query = this.ability.query(convertToZidaneContentUri, strArr, selectionToPredicates);
        Bytrace.finishTrace(2147483648L, "ability query");
        if (query == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::query failed", new Object[0]);
            Bytrace.finishTrace(2147483648L, "ShellProvider query");
            return null;
        }
        Cursor resultSetToCursor = ContentProviderConverter.resultSetToCursor(query);
        Bytrace.finishTrace(2147483648L, "ShellProvider query");
        return resultSetToCursor;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider insert");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        ValuesBucket contentValuesToValuesBucket = ContentProviderConverter.contentValuesToValuesBucket(contentValues);
        Bytrace.startTrace(2147483648L, "ability insert");
        int insert = this.ability.insert(convertToZidaneContentUri, contentValuesToValuesBucket);
        Bytrace.finishTrace(2147483648L, "ability insert");
        Uri convertIndexToUri = convertIndexToUri(uri, insert);
        Bytrace.finishTrace(2147483648L, "ShellProvider insert");
        return convertIndexToUri;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider delete");
        DataAbilityPredicates selectionToPredicates = selectionToPredicates(uri, str, strArr, null);
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability delete");
        int delete = this.ability.delete(convertToZidaneContentUri, selectionToPredicates);
        Bytrace.finishTrace(2147483648L, "ability delete");
        Bytrace.finishTrace(2147483648L, "ShellProvider delete");
        return delete;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider update");
        DataAbilityPredicates selectionToPredicates = selectionToPredicates(uri, str, strArr, null);
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        ValuesBucket contentValuesToValuesBucket = ContentProviderConverter.contentValuesToValuesBucket(contentValues);
        Bytrace.startTrace(2147483648L, "ability update");
        int update = this.ability.update(convertToZidaneContentUri, contentValuesToValuesBucket, selectionToPredicates);
        Bytrace.finishTrace(2147483648L, "ability update");
        Bytrace.finishTrace(2147483648L, "ShellProvider update");
        return update;
    }

    public String getType(Uri uri) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider getType");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability getType");
        String type = this.ability.getType(convertToZidaneContentUri);
        Bytrace.finishTrace(2147483648L, "ability getType");
        if (type == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::getType failed", new Object[0]);
        }
        Bytrace.finishTrace(2147483648L, "ShellProvider getType");
        return type;
    }

    public String[] getStreamTypes(Uri uri, String str) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider getFileTypes");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability getFileTypes");
        String[] fileTypes = this.ability.getFileTypes(convertToZidaneContentUri, str);
        Bytrace.finishTrace(2147483648L, "ability getFileTypes");
        if (fileTypes == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::getStreamTypes failed", new Object[0]);
        }
        Bytrace.finishTrace(2147483648L, "ShellProvider getFileTypes");
        return fileTypes;
    }

    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider openFile");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        try {
            Bytrace.startTrace(2147483648L, "ability openFile");
            FileDescriptor openFile = this.ability.openFile(convertToZidaneContentUri, str);
            Bytrace.finishTrace(2147483648L, "ability openFile");
            ParcelFileDescriptor parcelFileDescriptor = null;
            if (openFile != null) {
                parcelFileDescriptor = new ParcelFileDescriptor(openFile);
            }
            Bytrace.finishTrace(2147483648L, "ShellProvider openFile");
            return parcelFileDescriptor;
        } catch (FileNotFoundException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::openFile FileNotFoundException occur", new Object[0]);
            Bytrace.finishTrace(2147483648L, "ShellProvider openFile");
            throw e;
        }
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String str) throws FileNotFoundException {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider openAssetFile");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        try {
            Bytrace.startTrace(2147483648L, "ability openRawFile");
            RawFileDescriptor openRawFile = this.ability.openRawFile(convertToZidaneContentUri, str);
            Bytrace.finishTrace(2147483648L, "ability openRawFile");
            AssetFileDescriptor assetFileDescriptor = null;
            if (!(openRawFile == null || openRawFile.getFileDescriptor() == null)) {
                try {
                    AssetFileDescriptor assetFileDescriptor2 = new AssetFileDescriptor(new ParcelFileDescriptor(Libcore.os.dup(openRawFile.getFileDescriptor())), openRawFile.getStartPosition(), openRawFile.getFileSize());
                    try {
                        Libcore.os.close(openRawFile.getFileDescriptor());
                        assetFileDescriptor = assetFileDescriptor2;
                    } catch (ErrnoException unused) {
                        assetFileDescriptor = assetFileDescriptor2;
                        AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::openAssetFile dup FileDescriptor error", new Object[0]);
                        Bytrace.finishTrace(2147483648L, "ShellProvider openAssetFile");
                        return assetFileDescriptor;
                    }
                } catch (ErrnoException unused2) {
                    AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::openAssetFile dup FileDescriptor error", new Object[0]);
                    Bytrace.finishTrace(2147483648L, "ShellProvider openAssetFile");
                    return assetFileDescriptor;
                }
            }
            Bytrace.finishTrace(2147483648L, "ShellProvider openAssetFile");
            return assetFileDescriptor;
        } catch (FileNotFoundException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::openAssetFile FileNotFoundException occur", new Object[0]);
            Bytrace.finishTrace(2147483648L, "ShellProvider openAssetFile");
            throw e;
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        waitOnCreateDone();
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        int length = contentValuesArr.length;
        ValuesBucket[] valuesBucketArr = new ValuesBucket[length];
        for (int i = 0; i < length; i++) {
            valuesBucketArr[i] = ContentProviderConverter.contentValuesToValuesBucket(contentValuesArr[i]);
        }
        return this.ability.batchInsert(convertToZidaneContentUri, valuesBucketArr);
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider call");
        if (GET_REMOTE_DATA_ABILITY.equals(str)) {
            AppLog.i(SHELL_LABEL, "AbilityShellProviderDelegate::call getRemoteDataAbility", new Object[0]);
            Optional translateToIBinder = IPCAdapter.translateToIBinder(createRemoteDataAbility());
            if (!translateToIBinder.isPresent()) {
                AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::call createAndroidBinder failed", new Object[0]);
                Bytrace.finishTrace(2147483648L, "ShellProvider call");
                return null;
            }
            Bundle bundle2 = new Bundle();
            if (translateToIBinder.get() instanceof IBinder) {
                bundle2.putBinder(GET_REMOTE_DATA_ABILITY_KEY, (IBinder) translateToIBinder.get());
            }
            Bytrace.finishTrace(2147483648L, "ShellProvider call");
            return bundle2;
        }
        PacMap convertFromBundle = bundle != null ? PacMapUtils.convertFromBundle(bundle) : null;
        Bytrace.startTrace(2147483648L, "ability call");
        PacMap call = this.ability.call(str, str2, convertFromBundle);
        Bytrace.finishTrace(2147483648L, "ability call");
        if (call == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::call failed", new Object[0]);
            Bytrace.finishTrace(2147483648L, "ShellProvider call");
            return null;
        }
        Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(call);
        Bytrace.finishTrace(2147483648L, "ShellProvider call");
        return convertIntoBundle;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        waitOnCreateDone();
        ArrayList<DataAbilityOperation> arrayList2 = new ArrayList<>();
        Iterator<ContentProviderOperation> it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add(AbilityContentProviderConverter.contentProviderOperationToDataAbilityOperation(it.next()));
        }
        try {
            DataAbilityResult[] executeBatch = this.ability.executeBatch(arrayList2);
            int length = executeBatch.length;
            ContentProviderResult[] contentProviderResultArr = new ContentProviderResult[length];
            for (int i = 0; i < length; i++) {
                contentProviderResultArr[i] = AbilityContentProviderConverter.dataAbilityResultToContentProviderResult(executeBatch[i]);
            }
            return contentProviderResultArr;
        } catch (OperationExecuteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::applyBatch failed", new Object[0]);
            throw new OperationApplicationException(e.getMessage(), e.getCause());
        }
    }

    public void createProviderShellInfo(String str) {
        this.shellInfo = new ShellInfo();
        this.shellInfo.setPackageName(str);
        this.shellInfo.setName(this.abilityShell.getClass().getName());
        this.shellInfo.setType(ShellInfo.ShellType.PROVIDER);
    }

    public void onTrimMemory(int i) {
        waitOnCreateDone();
        this.ability.onMemoryLevel(i);
    }

    public Uri canonicalize(Uri uri) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider normalizeUri");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability normalizeUri");
        ohos.utils.net.Uri normalizeUri = this.ability.normalizeUri(convertToZidaneContentUri);
        Bytrace.finishTrace(2147483648L, "ability normalizeUri");
        Uri convertToAndroidContentUri = normalizeUri != null ? UriConverter.convertToAndroidContentUri(normalizeUri) : null;
        Bytrace.finishTrace(2147483648L, "ShellProvider normalizeUri");
        return convertToAndroidContentUri;
    }

    public Uri uncanonicalize(Uri uri) {
        waitOnCreateDone();
        Bytrace.startTrace(2147483648L, "ShellProvider denormalizeUri");
        ohos.utils.net.Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uri, "");
        Bytrace.startTrace(2147483648L, "ability denormalizeUri");
        ohos.utils.net.Uri denormalizeUri = this.ability.denormalizeUri(convertToZidaneContentUri);
        Bytrace.finishTrace(2147483648L, "ability denormalizeUri");
        Uri convertToAndroidContentUri = denormalizeUri != null ? UriConverter.convertToAndroidContentUri(denormalizeUri) : null;
        Bytrace.finishTrace(2147483648L, "ShellProvider denormalizeUri");
        return convertToAndroidContentUri;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.abilityshell.AbilityShellDelegate
    public void updateConfiguration(Configuration configuration) {
        waitOnCreateDone();
        super.updateConfiguration(configuration);
    }

    private IRemoteObject createRemoteDataAbility() {
        Context context = ((ContentProvider) this.abilityShell).getContext();
        try {
            ProviderInfo providerInfo = context.getPackageManager().getProviderInfo(new ComponentName(context.getPackageName(), this.abilityShell.getClass().getName()), 0);
            return new RemoteDataAbility.Builder(this.ability).context(context).permission(this.contextDeal, "", providerInfo.readPermission, providerInfo.writePermission).build();
        } catch (PackageManager.NameNotFoundException unused) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::createRemoteDataAbility NameNotFoundException occur", new Object[0]);
            return null;
        }
    }

    private Uri convertIndexToUri(Uri uri, int i) {
        if (i < 0) {
            return null;
        }
        return Uri.withAppendedPath(uri, String.valueOf(i));
    }

    private void waitOnCreateDone() {
        try {
            if (!this.createLatch.await(2000, TimeUnit.MILLISECONDS)) {
                AppLog.w(SHELL_LABEL, "AbilityShellProviderDelegate::waitOnCreateDone exceed time", new Object[0]);
            }
        } catch (InterruptedException unused) {
            AppLog.e(SHELL_LABEL, "AbilityShellProviderDelegate::waitOnCreateDone InterruptedException occur", new Object[0]);
        }
    }

    private DataAbilityPredicates selectionToPredicates(Uri uri, String str, String[] strArr, String str2) {
        if (str == null || strArr == null) {
            return null;
        }
        DataAbilityPredicates selectionToDataAbilityPredicates = ContentProviderConverter.selectionToDataAbilityPredicates(str, strArr);
        if (str2 == null) {
            return selectionToDataAbilityPredicates;
        }
        selectionToDataAbilityPredicates.setOrder(str2);
        return selectionToDataAbilityPredicates;
    }

    private ContextDeal createProviderContextdeal(AbilityInfo abilityInfo) {
        ContentProvider contentProvider = (ContentProvider) this.abilityShell;
        ContextDeal contextDeal2 = new ContextDeal(contentProvider.getContext(), contentProvider.getContext().getClassLoader());
        contextDeal2.setAbilityInfo(abilityInfo);
        contextDeal2.setHapModuleInfo(HarmonyApplication.getInstance().getHapModuleInfoByAbilityInfo(abilityInfo));
        contextDeal2.setApplication(HarmonyApplication.getInstance().getApplication());
        HarmonyApplication.getInstance().getApplication().addAbilityRecord(this.abilityShell, contextDeal2);
        return contextDeal2;
    }
}
