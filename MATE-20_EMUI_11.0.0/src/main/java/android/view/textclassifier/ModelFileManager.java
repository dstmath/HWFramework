package android.view.textclassifier;

import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class ModelFileManager {
    private final Object mLock = new Object();
    private final Supplier<List<ModelFile>> mModelFileSupplier;
    private List<ModelFile> mModelFiles;

    public ModelFileManager(Supplier<List<ModelFile>> modelFileSupplier) {
        this.mModelFileSupplier = (Supplier) Preconditions.checkNotNull(modelFileSupplier);
    }

    public List<ModelFile> listModelFiles() {
        List<ModelFile> list;
        synchronized (this.mLock) {
            if (this.mModelFiles == null) {
                this.mModelFiles = Collections.unmodifiableList(this.mModelFileSupplier.get());
            }
            list = this.mModelFiles;
        }
        return list;
    }

    public ModelFile findBestModelFile(LocaleList localeList) {
        String languages;
        if (localeList == null || localeList.isEmpty()) {
            languages = LocaleList.getDefault().toLanguageTags();
        } else {
            languages = localeList.toLanguageTags();
        }
        List<Locale.LanguageRange> languageRangeList = Locale.LanguageRange.parse(languages);
        ModelFile bestModel = null;
        for (ModelFile model : listModelFiles()) {
            if (model.isAnyLanguageSupported(languageRangeList) && model.isPreferredTo(bestModel)) {
                bestModel = model;
            }
        }
        return bestModel;
    }

    public static final class ModelFileSupplierImpl implements Supplier<List<ModelFile>> {
        private final File mFactoryModelDir;
        private final Pattern mModelFilenamePattern;
        private final Function<Integer, String> mSupportedLocalesSupplier;
        private final File mUpdatedModelFile;
        private final Function<Integer, Integer> mVersionSupplier;

        public ModelFileSupplierImpl(File factoryModelDir, String factoryModelFileNameRegex, File updatedModelFile, Function<Integer, Integer> versionSupplier, Function<Integer, String> supportedLocalesSupplier) {
            this.mUpdatedModelFile = (File) Preconditions.checkNotNull(updatedModelFile);
            this.mFactoryModelDir = (File) Preconditions.checkNotNull(factoryModelDir);
            this.mModelFilenamePattern = Pattern.compile((String) Preconditions.checkNotNull(factoryModelFileNameRegex));
            this.mVersionSupplier = (Function) Preconditions.checkNotNull(versionSupplier);
            this.mSupportedLocalesSupplier = (Function) Preconditions.checkNotNull(supportedLocalesSupplier);
        }

        @Override // java.util.function.Supplier
        public List<ModelFile> get() {
            ModelFile model;
            ModelFile updatedModel;
            List<ModelFile> modelFiles = new ArrayList<>();
            if (this.mUpdatedModelFile.exists() && (updatedModel = createModelFile(this.mUpdatedModelFile)) != null) {
                modelFiles.add(updatedModel);
            }
            if (this.mFactoryModelDir.exists() && this.mFactoryModelDir.isDirectory()) {
                File[] files = this.mFactoryModelDir.listFiles();
                for (File file : files) {
                    if (this.mModelFilenamePattern.matcher(file.getName()).matches() && file.isFile() && (model = createModelFile(file)) != null) {
                        modelFiles.add(model);
                    }
                }
            }
            return modelFiles;
        }

        private ModelFile createModelFile(File file) {
            if (!file.exists()) {
                return null;
            }
            ParcelFileDescriptor modelFd = null;
            try {
                modelFd = ParcelFileDescriptor.open(file, 268435456);
                if (modelFd == null) {
                    return null;
                }
                int modelFdInt = modelFd.getFd();
                int version = this.mVersionSupplier.apply(Integer.valueOf(modelFdInt)).intValue();
                String supportedLocalesStr = this.mSupportedLocalesSupplier.apply(Integer.valueOf(modelFdInt));
                if (supportedLocalesStr.isEmpty()) {
                    Log.d(TextClassifier.DEFAULT_LOG_TAG, "Ignoring " + file.getAbsolutePath());
                    maybeCloseAndLogError(modelFd);
                    return null;
                }
                List<Locale> supportedLocales = new ArrayList<>();
                for (String langTag : supportedLocalesStr.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                    supportedLocales.add(Locale.forLanguageTag(langTag));
                }
                ModelFile modelFile = new ModelFile(file, version, supportedLocales, supportedLocalesStr, "*".equals(supportedLocalesStr));
                maybeCloseAndLogError(modelFd);
                return modelFile;
            } catch (FileNotFoundException e) {
                Log.e(TextClassifier.DEFAULT_LOG_TAG, "Failed to find " + file.getAbsolutePath(), e);
                return null;
            } finally {
                maybeCloseAndLogError(modelFd);
            }
        }

        private static void maybeCloseAndLogError(ParcelFileDescriptor fd) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                    Log.e(TextClassifier.DEFAULT_LOG_TAG, "Error closing file.", e);
                }
            }
        }
    }

    public static final class ModelFile {
        public static final String LANGUAGE_INDEPENDENT = "*";
        private final File mFile;
        private final boolean mLanguageIndependent;
        private final List<Locale> mSupportedLocales;
        private final String mSupportedLocalesStr;
        private final int mVersion;

        public ModelFile(File file, int version, List<Locale> supportedLocales, String supportedLocalesStr, boolean languageIndependent) {
            this.mFile = (File) Preconditions.checkNotNull(file);
            this.mVersion = version;
            this.mSupportedLocales = (List) Preconditions.checkNotNull(supportedLocales);
            this.mSupportedLocalesStr = (String) Preconditions.checkNotNull(supportedLocalesStr);
            this.mLanguageIndependent = languageIndependent;
        }

        public String getPath() {
            return this.mFile.getAbsolutePath();
        }

        public String getName() {
            return this.mFile.getName();
        }

        public int getVersion() {
            return this.mVersion;
        }

        public boolean isAnyLanguageSupported(List<Locale.LanguageRange> languageRanges) {
            Preconditions.checkNotNull(languageRanges);
            return this.mLanguageIndependent || Locale.lookup(languageRanges, this.mSupportedLocales) != null;
        }

        public List<Locale> getSupportedLocales() {
            return Collections.unmodifiableList(this.mSupportedLocales);
        }

        public String getSupportedLocalesStr() {
            return this.mSupportedLocalesStr;
        }

        public boolean isPreferredTo(ModelFile model) {
            if (model == null) {
                return true;
            }
            if (!this.mLanguageIndependent && model.mLanguageIndependent) {
                return true;
            }
            if ((!this.mLanguageIndependent || model.mLanguageIndependent) && this.mVersion > model.getVersion()) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(getPath());
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof ModelFile) {
                return TextUtils.equals(getPath(), ((ModelFile) other).getPath());
            }
            return false;
        }

        public String toString() {
            StringJoiner localesJoiner = new StringJoiner(SmsManager.REGEX_PREFIX_DELIMITER);
            for (Locale locale : this.mSupportedLocales) {
                localesJoiner.add(locale.toLanguageTag());
            }
            return String.format(Locale.US, "ModelFile { path=%s name=%s version=%d locales=%s }", getPath(), getName(), Integer.valueOf(this.mVersion), localesJoiner.toString());
        }
    }
}
