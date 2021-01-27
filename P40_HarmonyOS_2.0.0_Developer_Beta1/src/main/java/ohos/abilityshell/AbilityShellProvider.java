package ohos.abilityshell;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityShellProvider extends ContentProvider {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private AbilityShellProviderDelegate delegate = new AbilityShellProviderDelegate(this);

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::onCreate called", new Object[0]);
        Context context = getContext();
        if (context == null) {
            AppLog.e(SHELL_LABEL, "AbilityShellProvider::onCreate getContext failed", new Object[0]);
            return false;
        }
        this.delegate.createProviderShellInfo(context.getPackageName());
        return this.delegate.onCreate();
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::query called", new Object[0]);
        return this.delegate.query(uri, strArr, str, strArr2, str2);
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::insert called", new Object[0]);
        return this.delegate.insert(uri, contentValues);
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::delete called", new Object[0]);
        return this.delegate.delete(uri, str, strArr);
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::update called", new Object[0]);
        return this.delegate.update(uri, contentValues, str, strArr);
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::getType called", new Object[0]);
        return this.delegate.getType(uri);
    }

    @Override // android.content.ContentProvider
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::bulkInsert called", new Object[0]);
        return this.delegate.bulkInsert(uri, contentValuesArr);
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::call method %{private}s", str);
        return this.delegate.call(str, str2, bundle);
    }

    @Override // android.content.ContentProvider
    public String[] getStreamTypes(Uri uri, String str) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::getStreamType called", new Object[0]);
        return this.delegate.getStreamTypes(uri, str);
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::openFile called", new Object[0]);
        return this.delegate.openFile(uri, str);
    }

    @Override // android.content.ContentProvider
    public AssetFileDescriptor openAssetFile(Uri uri, String str) throws FileNotFoundException {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::openAssetFile called", new Object[0]);
        return this.delegate.openAssetFile(uri, str);
    }

    @Override // android.content.ContentProvider
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::applyBatch called", new Object[0]);
        return this.delegate.applyBatch(arrayList);
    }

    @Override // android.content.ContentProvider, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::onTrimMemory called", new Object[0]);
        super.onTrimMemory(i);
        this.delegate.onTrimMemory(i);
    }

    @Override // android.content.ContentProvider
    public Uri canonicalize(Uri uri) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::normalizeUri called", new Object[0]);
        return this.delegate.canonicalize(uri);
    }

    @Override // android.content.ContentProvider
    public Uri uncanonicalize(Uri uri) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::denormalizeUri called", new Object[0]);
        return this.delegate.uncanonicalize(uri);
    }

    @Override // android.content.ContentProvider, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.delegate.updateConfiguration(configuration);
    }

    @Override // android.content.ContentProvider
    public boolean refresh(Uri uri, Bundle bundle, CancellationSignal cancellationSignal) {
        AppLog.d(SHELL_LABEL, "AbilityShellProvider::refresh called", new Object[0]);
        return this.delegate.reload(uri, bundle);
    }
}
