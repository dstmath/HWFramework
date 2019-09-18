package android.mtp;

import android.app.DownloadManager;
import android.content.ContentProviderClient;
import android.database.Cursor;
import android.media.midi.MidiDeviceInfo;
import android.mtp.MtpStorageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;

class MtpPropertyGroup {
    private static final int FILE_SIZE_MAGNIFICATION = 3;
    private static final String PATH_WHERE = "_data=?";
    private static final String SUFFIX_HEIC_FILE = ".HEIC";
    private static final String SUFFIX_HEIF_FILE = ".HEIF";
    private static final String SUFFIX_JPG_FILE = ".jpg";
    private static final String TAG = MtpPropertyGroup.class.getSimpleName();
    private String[] mColumns;
    private final Property[] mProperties;
    private final ContentProviderClient mProvider;
    private final Uri mUri;
    private final String mVolumeName;

    private class Property {
        int code;
        int column;
        int type;

        Property(int code2, int type2, int column2) {
            this.code = code2;
            this.type = type2;
            this.column = column2;
        }
    }

    public static native String formatDateTime(long j);

    public MtpPropertyGroup(ContentProviderClient provider, String volumeName, int[] properties) {
        this.mProvider = provider;
        this.mVolumeName = volumeName;
        this.mUri = MediaStore.Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
        int count = properties.length;
        ArrayList<String> columns = new ArrayList<>(count);
        columns.add(DownloadManager.COLUMN_ID);
        columns.add(DownloadManager.COLUMN_MEDIA_TYPE);
        this.mProperties = new Property[count];
        for (int i = 0; i < count; i++) {
            this.mProperties[i] = createProperty(properties[i], columns);
        }
        int count2 = columns.size();
        this.mColumns = new String[count2];
        for (int i2 = 0; i2 < count2; i2++) {
            this.mColumns[i2] = columns.get(i2);
        }
    }

    private Property createProperty(int code, ArrayList<String> columns) {
        int type;
        String column = null;
        switch (code) {
            case MtpConstants.PROPERTY_STORAGE_ID:
                type = 6;
                break;
            case MtpConstants.PROPERTY_OBJECT_FORMAT:
                type = 4;
                break;
            case MtpConstants.PROPERTY_PROTECTION_STATUS:
                type = 4;
                break;
            case MtpConstants.PROPERTY_OBJECT_SIZE:
                type = 8;
                break;
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_MODIFIED:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_PARENT_OBJECT:
                type = 6;
                break;
            case MtpConstants.PROPERTY_PERSISTENT_UID:
                type = 10;
                break;
            case MtpConstants.PROPERTY_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ARTIST:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DESCRIPTION:
                column = "description";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_ADDED:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DURATION:
                column = "duration";
                type = 6;
                break;
            case MtpConstants.PROPERTY_TRACK:
                column = "track";
                type = 4;
                break;
            case MtpConstants.PROPERTY_GENRE:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_COMPOSER:
                column = "composer";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                column = "year";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_ARTIST:
                column = "album_artist";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DISPLAY_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_BITRATE_TYPE:
            case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                type = 4;
                break;
            case MtpConstants.PROPERTY_SAMPLE_RATE:
            case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
            case MtpConstants.PROPERTY_AUDIO_BITRATE:
                type = 6;
                break;
            default:
                type = 0;
                String str = TAG;
                Log.e(str, "unsupported property " + code);
                break;
        }
        if (column == null) {
            return new Property(code, type, -1);
        }
        columns.add(column);
        return new Property(code, type, columns.size() - 1);
    }

    private String queryAudio(String path, String column) {
        Cursor c = null;
        try {
            Uri build = MediaStore.Audio.Media.getContentUri(this.mVolumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
            Cursor c2 = this.mProvider.query(MediaStore.Audio.Media.getContentUri(this.mVolumeName), new String[]{column}, PATH_WHERE, new String[]{path}, null, null);
            if (c2 == null || !c2.moveToNext()) {
                if (c2 != null) {
                    c2.close();
                }
                return "";
            }
            String string = c2.getString(0);
            if (c2 != null) {
                c2.close();
            }
            return string;
        } catch (Exception e) {
            if (c != null) {
                c.close();
            }
            return "";
        }
    }

    private String queryGenre(String path) {
        Cursor c = null;
        try {
            Uri uri = MediaStore.Audio.Genres.getContentUri(this.mVolumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
            Cursor c2 = this.mProvider.query(uri, new String[]{MidiDeviceInfo.PROPERTY_NAME}, PATH_WHERE, new String[]{path}, null, null);
            if (c2 == null || !c2.moveToNext()) {
                if (c2 != null) {
                    c2.close();
                }
                return "";
            }
            String string = c2.getString(0);
            if (c2 != null) {
                c2.close();
            }
            return string;
        } catch (Exception e) {
            if (c != null) {
                c.close();
            }
            return "";
        }
    }

    public int getPropertyList(MtpStorageManager.MtpObject object, MtpPropertyList list) {
        MtpPropertyGroup mtpPropertyGroup = this;
        MtpPropertyList mtpPropertyList = list;
        int id = object.getId();
        String path = object.getPath().toString();
        Property[] propertyArr = mtpPropertyGroup.mProperties;
        int length = propertyArr.length;
        Cursor c = null;
        int i = 0;
        while (i < length) {
            Property property = propertyArr[i];
            boolean isHeifFile = true;
            if (property.column != -1 && c == null) {
                try {
                    c = mtpPropertyGroup.mProvider.query(mtpPropertyGroup.mUri, mtpPropertyGroup.mColumns, PATH_WHERE, new String[]{path}, null, null);
                    if (c != null && !c.moveToNext()) {
                        c.close();
                        c = null;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Mediaprovider lookup failed");
                }
            }
            Cursor c2 = c;
            switch (property.code) {
                case MtpConstants.PROPERTY_STORAGE_ID:
                    mtpPropertyList.append(id, property.code, property.type, (long) object.getStorageId());
                    break;
                case MtpConstants.PROPERTY_OBJECT_FORMAT:
                    mtpPropertyList.append(id, property.code, property.type, (long) object.getFormat());
                    break;
                case MtpConstants.PROPERTY_PROTECTION_STATUS:
                    mtpPropertyList.append(id, property.code, property.type, 0);
                    break;
                case MtpConstants.PROPERTY_OBJECT_SIZE:
                    boolean isAutomaticMode = MtpDatabase.issIsHeifSettingAutomaticMode();
                    if (object.getFormat() != 14354) {
                        isHeifFile = false;
                    }
                    boolean isHeifFile2 = isHeifFile;
                    if (isAutomaticMode && isHeifFile2) {
                        mtpPropertyList.append(id, property.code, property.type, 3 * object.getSize());
                        break;
                    } else {
                        mtpPropertyList.append(id, property.code, property.type, object.getSize());
                        break;
                    }
                case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                case MtpConstants.PROPERTY_NAME:
                case MtpConstants.PROPERTY_DISPLAY_NAME:
                    String oriName = object.getName();
                    String filePath = oriName;
                    boolean isAutomaticMode2 = MtpDatabase.issIsHeifSettingAutomaticMode();
                    if (!filePath.toUpperCase().endsWith(SUFFIX_HEIC_FILE) && !filePath.toUpperCase().endsWith(SUFFIX_HEIF_FILE)) {
                        isHeifFile = false;
                    }
                    if (isAutomaticMode2 && isHeifFile) {
                        int suffixIndex = filePath.lastIndexOf(46);
                        mtpPropertyList.append(id, property.code, filePath.substring(0, suffixIndex) + SUFFIX_JPG_FILE);
                        break;
                    } else {
                        mtpPropertyList.append(id, property.code, oriName);
                        break;
                    }
                case MtpConstants.PROPERTY_DATE_MODIFIED:
                case MtpConstants.PROPERTY_DATE_ADDED:
                    mtpPropertyList.append(id, property.code, formatDateTime(object.getModifiedTime()));
                    break;
                case MtpConstants.PROPERTY_PARENT_OBJECT:
                    mtpPropertyList.append(id, property.code, property.type, object.getParent().isRoot() ? 0 : (long) object.getParent().getId());
                    break;
                case MtpConstants.PROPERTY_PERSISTENT_UID:
                    mtpPropertyList.append(id, property.code, property.type, ((long) (object.getPath().toString().hashCode() << 32)) + object.getModifiedTime());
                    break;
                case MtpConstants.PROPERTY_ARTIST:
                    mtpPropertyList.append(id, property.code, mtpPropertyGroup.queryAudio(path, "artist"));
                    break;
                case MtpConstants.PROPERTY_TRACK:
                    int track = 0;
                    if (c2 != null) {
                        track = c2.getInt(property.column);
                    }
                    int track2 = track;
                    int i2 = track2;
                    mtpPropertyList.append(id, property.code, 4, (long) (track2 % 1000));
                    break;
                case MtpConstants.PROPERTY_GENRE:
                    String genre = mtpPropertyGroup.queryGenre(path);
                    if (genre == null) {
                        break;
                    } else {
                        mtpPropertyList.append(id, property.code, genre);
                        break;
                    }
                case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                    int year = 0;
                    if (c2 != null) {
                        year = c2.getInt(property.column);
                    }
                    mtpPropertyList.append(id, property.code, Integer.toString(year) + "0101T000000");
                    break;
                case MtpConstants.PROPERTY_ALBUM_NAME:
                    mtpPropertyList.append(id, property.code, mtpPropertyGroup.queryAudio(path, "album"));
                    break;
                case MtpConstants.PROPERTY_BITRATE_TYPE:
                case MtpConstants.PROPERTY_NUMBER_OF_CHANNELS:
                    mtpPropertyList.append(id, property.code, 4, 0);
                    break;
                case MtpConstants.PROPERTY_SAMPLE_RATE:
                case MtpConstants.PROPERTY_AUDIO_WAVE_CODEC:
                case MtpConstants.PROPERTY_AUDIO_BITRATE:
                    mtpPropertyList.append(id, property.code, 6, 0);
                    break;
                default:
                    int i3 = property.type;
                    if (i3 != 0) {
                        if (i3 == 65535) {
                            String value = "";
                            if (c2 != null) {
                                value = c2.getString(property.column);
                            }
                            mtpPropertyList.append(id, property.code, value);
                            break;
                        } else {
                            long longValue = 0;
                            if (c2 != null) {
                                longValue = c2.getLong(property.column);
                            }
                            mtpPropertyList.append(id, property.code, property.type, longValue);
                            break;
                        }
                    } else {
                        mtpPropertyList.append(id, property.code, property.type, 0);
                        break;
                    }
            }
            i++;
            c = c2;
            mtpPropertyGroup = this;
        }
        if (c != null) {
            c.close();
        }
        return MtpConstants.RESPONSE_OK;
    }
}
