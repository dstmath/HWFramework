package android.content.pm.split;

import android.content.pm.PackageParser;
import android.content.res.AssetManager;

public interface SplitAssetLoader extends AutoCloseable {
    AssetManager getBaseAssetManager() throws PackageParser.PackageParserException;

    AssetManager getSplitAssetManager(int i) throws PackageParser.PackageParserException;
}
