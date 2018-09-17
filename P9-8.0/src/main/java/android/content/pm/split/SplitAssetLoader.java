package android.content.pm.split;

import android.content.pm.PackageParser.PackageParserException;
import android.content.res.AssetManager;

public interface SplitAssetLoader extends AutoCloseable {
    AssetManager getBaseAssetManager() throws PackageParserException;

    AssetManager getSplitAssetManager(int i) throws PackageParserException;
}
