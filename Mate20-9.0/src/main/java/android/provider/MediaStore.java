package android.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hwgallerycache.HwGalleryCacheManager;
import android.media.MiniThumbFile;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Contacts;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;

public final class MediaStore {
    public static final String ACTION_IMAGE_CAPTURE = "android.media.action.IMAGE_CAPTURE";
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_VIDEO_CAPTURE = "android.media.action.VIDEO_CAPTURE";
    public static final String AUTHORITY = "media";
    private static final String CONTENT_AUTHORITY_SLASH = "content://media/";
    public static final String EXTRA_DURATION_LIMIT = "android.intent.extra.durationLimit";
    public static final String EXTRA_FINISH_ON_COMPLETION = "android.intent.extra.finishOnCompletion";
    public static final String EXTRA_FULL_SCREEN = "android.intent.extra.fullScreen";
    public static final String EXTRA_MEDIA_ALBUM = "android.intent.extra.album";
    public static final String EXTRA_MEDIA_ARTIST = "android.intent.extra.artist";
    public static final String EXTRA_MEDIA_FOCUS = "android.intent.extra.focus";
    public static final String EXTRA_MEDIA_GENRE = "android.intent.extra.genre";
    public static final String EXTRA_MEDIA_PLAYLIST = "android.intent.extra.playlist";
    public static final String EXTRA_MEDIA_RADIO_CHANNEL = "android.intent.extra.radio_channel";
    public static final String EXTRA_MEDIA_TITLE = "android.intent.extra.title";
    public static final String EXTRA_OUTPUT = "output";
    public static final String EXTRA_SCREEN_ORIENTATION = "android.intent.extra.screenOrientation";
    public static final String EXTRA_SHOW_ACTION_ICONS = "android.intent.extra.showActionIcons";
    public static final String EXTRA_SIZE_LIMIT = "android.intent.extra.sizeLimit";
    public static final String EXTRA_VIDEO_QUALITY = "android.intent.extra.videoQuality";
    public static final String INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH = "android.media.action.MEDIA_PLAY_FROM_SEARCH";
    public static final String INTENT_ACTION_MEDIA_SEARCH = "android.intent.action.MEDIA_SEARCH";
    @Deprecated
    public static final String INTENT_ACTION_MUSIC_PLAYER = "android.intent.action.MUSIC_PLAYER";
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA = "android.media.action.STILL_IMAGE_CAMERA";
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String INTENT_ACTION_TEXT_OPEN_FROM_SEARCH = "android.media.action.TEXT_OPEN_FROM_SEARCH";
    public static final String INTENT_ACTION_VIDEO_CAMERA = "android.media.action.VIDEO_CAMERA";
    public static final String INTENT_ACTION_VIDEO_PLAY_FROM_SEARCH = "android.media.action.VIDEO_PLAY_FROM_SEARCH";
    public static final String MEDIA_IGNORE_FILENAME = ".nomedia";
    public static final String MEDIA_SCANNER_VOLUME = "volume";
    public static final String META_DATA_STILL_IMAGE_CAMERA_PREWARM_SERVICE = "android.media.still_image_camera_preview_service";
    public static final String PARAM_DELETE_DATA = "deletedata";
    public static final String RETRANSLATE_CALL = "update_titles";
    private static final String TAG = "MediaStore";
    public static final String UNHIDE_CALL = "unhide";
    public static final String UNKNOWN_STRING = "<unknown>";

    public static final class Audio {

        public interface AlbumColumns {
            public static final String ALBUM = "album";
            public static final String ALBUM_ART = "album_art";
            public static final String ALBUM_ID = "album_id";
            public static final String ALBUM_KEY = "album_key";
            public static final String ARTIST = "artist";
            public static final String FIRST_YEAR = "minyear";
            public static final String LAST_YEAR = "maxyear";
            public static final String NUMBER_OF_SONGS = "numsongs";
            public static final String NUMBER_OF_SONGS_FOR_ARTIST = "numsongs_by_artist";
        }

        public static final class Albums implements BaseColumns, AlbumColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/albums";
            public static final String DEFAULT_SORT_ORDER = "album_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/album";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/albums");
            }
        }

        public interface ArtistColumns {
            public static final String ARTIST = "artist";
            public static final String ARTIST_KEY = "artist_key";
            public static final String NUMBER_OF_ALBUMS = "number_of_albums";
            public static final String NUMBER_OF_TRACKS = "number_of_tracks";
        }

        public static final class Artists implements BaseColumns, ArtistColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/artists";
            public static final String DEFAULT_SORT_ORDER = "artist_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/artist";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Albums implements AlbumColumns {
                public static final Uri getContentUri(String volumeName, long artistId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/artists/" + artistId + "/albums");
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/artists");
            }
        }

        public interface AudioColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ALBUM_ARTIST = "album_artist";
            public static final String ALBUM_ID = "album_id";
            public static final String ALBUM_KEY = "album_key";
            public static final String ARTIST = "artist";
            public static final String ARTIST_ID = "artist_id";
            public static final String ARTIST_KEY = "artist_key";
            public static final String BOOKMARK = "bookmark";
            public static final String COMPILATION = "compilation";
            public static final String COMPOSER = "composer";
            public static final String DURATION = "duration";
            public static final String GENRE = "genre";
            public static final String IS_ALARM = "is_alarm";
            public static final String IS_MUSIC = "is_music";
            public static final String IS_NOTIFICATION = "is_notification";
            public static final String IS_PODCAST = "is_podcast";
            public static final String IS_RINGTONE = "is_ringtone";
            public static final String TITLE_KEY = "title_key";
            public static final String TITLE_RESOURCE_URI = "title_resource_uri";
            public static final String TRACK = "track";
            public static final String YEAR = "year";
        }

        public static final class Genres implements BaseColumns, GenresColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/genre";
            public static final String DEFAULT_SORT_ORDER = "name";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/genre";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Members implements AudioColumns {
                public static final String AUDIO_ID = "audio_id";
                public static final String CONTENT_DIRECTORY = "members";
                public static final String DEFAULT_SORT_ORDER = "title_key";
                public static final String GENRE_ID = "genre_id";

                public static final Uri getContentUri(String volumeName, long genreId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/genres/" + genreId + "/members");
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/genres");
            }

            public static Uri getContentUriForAudioId(String volumeName, int audioId) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/media/" + audioId + "/genres");
            }
        }

        public interface GenresColumns {
            public static final String NAME = "name";
        }

        public static final class Media implements AudioColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/audio";
            public static final String DEFAULT_SORT_ORDER = "title_key";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/audio";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            private static final String[] EXTERNAL_PATHS;
            public static final String EXTRA_MAX_BYTES = "android.provider.MediaStore.extra.MAX_BYTES";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String RECORD_SOUND_ACTION = "android.provider.MediaStore.RECORD_SOUND";

            static {
                String secondary_storage = System.getenv("SECONDARY_STORAGE");
                if (secondary_storage != null) {
                    EXTERNAL_PATHS = secondary_storage.split(SettingsStringUtil.DELIMITER);
                } else {
                    EXTERNAL_PATHS = new String[0];
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/media");
            }

            public static Uri getContentUriForPath(String path) {
                for (String ep : EXTERNAL_PATHS) {
                    if (path.startsWith(ep)) {
                        return EXTERNAL_CONTENT_URI;
                    }
                }
                return path.startsWith(Environment.getExternalStorageDirectory().getPath()) ? EXTERNAL_CONTENT_URI : INTERNAL_CONTENT_URI;
            }
        }

        public static final class Playlists implements BaseColumns, PlaylistsColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/playlist";
            public static final String DEFAULT_SORT_ORDER = "name";
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/playlist";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final class Members implements AudioColumns {
                public static final String AUDIO_ID = "audio_id";
                public static final String CONTENT_DIRECTORY = "members";
                public static final String DEFAULT_SORT_ORDER = "play_order";
                public static final String PLAYLIST_ID = "playlist_id";
                public static final String PLAY_ORDER = "play_order";
                public static final String _ID = "_id";

                public static final Uri getContentUri(String volumeName, long playlistId) {
                    return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playlists/" + playlistId + "/members");
                }

                public static final boolean moveItem(ContentResolver res, long playlistId, int from, int to) {
                    Uri uri = getContentUri("external", playlistId).buildUpon().appendEncodedPath(String.valueOf(from)).appendQueryParameter("move", "true").build();
                    ContentValues values = new ContentValues();
                    values.put("play_order", Integer.valueOf(to));
                    return res.update(uri, values, null, null) != 0;
                }
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/audio/playlists");
            }
        }

        public interface PlaylistsColumns {
            public static final String DATA = "_data";
            public static final String DATE_ADDED = "date_added";
            public static final String DATE_MODIFIED = "date_modified";
            public static final String NAME = "name";
        }

        public static final class Radio {
            public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/radio";

            private Radio() {
            }
        }

        public static String keyFor(String name) {
            if (name == null) {
                return null;
            }
            boolean sortfirst = false;
            if (name.equals(MediaStore.UNKNOWN_STRING)) {
                return "\u0001";
            }
            if (name.startsWith("\u0001")) {
                sortfirst = true;
            }
            String name2 = name.trim().toLowerCase();
            if (name2.startsWith("the ")) {
                name2 = name2.substring(4);
            }
            if (name2.startsWith("an ")) {
                name2 = name2.substring(3);
            }
            if (name2.startsWith("a ")) {
                name2 = name2.substring(2);
            }
            if (name2.endsWith(", the") || name2.endsWith(",the") || name2.endsWith(", an") || name2.endsWith(",an") || name2.endsWith(", a") || name2.endsWith(",a")) {
                name2 = name2.substring(0, name2.lastIndexOf(44));
            }
            String name3 = name2.replaceAll("[\\[\\]\\(\\)\"'.,?!]", "").trim();
            if (name3.length() <= 0) {
                return "";
            }
            StringBuilder b = new StringBuilder();
            b.append('.');
            int nl = name3.length();
            for (int i = 0; i < nl; i++) {
                b.append(name3.charAt(i));
                b.append('.');
            }
            String key = DatabaseUtils.getCollationKey(b.toString());
            if (sortfirst) {
                key = "\u0001" + key;
            }
            return key;
        }
    }

    public static final class Files {

        public interface FileColumns extends MediaColumns {
            public static final String FORMAT = "format";
            public static final String MEDIA_TYPE = "media_type";
            public static final int MEDIA_TYPE_AUDIO = 2;
            public static final int MEDIA_TYPE_IMAGE = 1;
            public static final int MEDIA_TYPE_NONE = 0;
            public static final int MEDIA_TYPE_PLAYLIST = 4;
            public static final int MEDIA_TYPE_VIDEO = 3;
            public static final String MIME_TYPE = "mime_type";
            public static final String PARENT = "parent";
            public static final String STORAGE_ID = "storage_id";
            public static final String TITLE = "title";
        }

        public static Uri getContentUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/file");
        }

        public static final Uri getContentUri(String volumeName, long rowId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/file/" + rowId);
        }

        public static Uri getMtpObjectsUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object");
        }

        public static final Uri getMtpObjectsUri(String volumeName, long fileId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object/" + fileId);
        }

        public static final Uri getMtpReferencesUri(String volumeName, long fileId) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/object/" + fileId + "/references");
        }

        public static final Uri getDirectoryUri(String volumeName) {
            return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/dir");
        }
    }

    public static final class Images {

        public interface ImageColumns extends MediaColumns {
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
            public static final String BUCKET_ID = "bucket_id";
            public static final String DATE_TAKEN = "datetaken";
            public static final String DESCRIPTION = "description";
            public static final String IS_HDR = "is_hdr";
            public static final String IS_PRIVATE = "isprivate";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
            public static final String ORIENTATION = "orientation";
            public static final String PICASA_ID = "picasa_id";
        }

        public static final class Media implements ImageColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/image";
            public static final String DEFAULT_SORT_ORDER = "bucket_display_name";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, "bucket_display_name");
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection, String where, String orderBy) {
                String str;
                if (orderBy == null) {
                    str = "bucket_display_name";
                } else {
                    str = orderBy;
                }
                return cr.query(uri, projection, where, null, str);
            }

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
                String str;
                if (orderBy == null) {
                    str = "bucket_display_name";
                } else {
                    str = orderBy;
                }
                return cr.query(uri, projection, selection, selectionArgs, str);
            }

            public static final Bitmap getBitmap(ContentResolver cr, Uri url) throws FileNotFoundException, IOException {
                InputStream input = cr.openInputStream(url);
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                return bitmap;
            }

            public static final String insertImage(ContentResolver cr, String imagePath, String name, String description) throws FileNotFoundException {
                FileInputStream stream = new FileInputStream(imagePath);
                try {
                    Bitmap bm = BitmapFactory.decodeFile(imagePath);
                    String ret = insertImage(cr, bm, name, description);
                    if (bm != null) {
                        bm.recycle();
                    }
                    return ret;
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }

            private static final Bitmap StoreThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
                ContentResolver contentResolver = cr;
                Matrix matrix = new Matrix();
                matrix.setScale(width / ((float) source.getWidth()), height / ((float) source.getHeight()));
                Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                ContentValues values = new ContentValues(4);
                values.put("kind", Integer.valueOf(kind));
                values.put("image_id", Integer.valueOf((int) id));
                values.put("height", Integer.valueOf(thumb.getHeight()));
                values.put("width", Integer.valueOf(thumb.getWidth()));
                try {
                    OutputStream thumbOut = contentResolver.openOutputStream(contentResolver.insert(Thumbnails.EXTERNAL_CONTENT_URI, values));
                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
                    thumbOut.close();
                    return thumb;
                } catch (FileNotFoundException e) {
                    return null;
                } catch (IOException e2) {
                    return null;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:23:0x007b  */
            /* JADX WARNING: Removed duplicated region for block: B:25:0x0082  */
            /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
            public static final String insertImage(ContentResolver cr, Bitmap source, String title, String description) {
                Uri url;
                OutputStream imageOut;
                ContentResolver contentResolver = cr;
                Bitmap bitmap = source;
                ContentValues values = new ContentValues();
                values.put("title", title);
                values.put("description", description);
                values.put("mime_type", "image/jpeg");
                try {
                    url = contentResolver.insert(EXTERNAL_CONTENT_URI, values);
                    if (bitmap != null) {
                        try {
                            imageOut = contentResolver.openOutputStream(url);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                            imageOut.close();
                            long id = ContentUris.parseId(url);
                            long j = id;
                            OutputStream outputStream = imageOut;
                            StoreThumbnail(contentResolver, Thumbnails.getThumbnail(contentResolver, id, 1, null), id, 50.0f, 50.0f, 3);
                        } catch (Exception e) {
                            e = e;
                            Log.e(MediaStore.TAG, "Failed to insert image", e);
                            if (url != null) {
                            }
                            if (url != null) {
                            }
                        } catch (Throwable th) {
                            imageOut.close();
                            throw th;
                        }
                    } else {
                        Log.e(MediaStore.TAG, "Failed to create thumbnail, removing original");
                        contentResolver.delete(url, null, null);
                        url = null;
                    }
                } catch (Exception e2) {
                    e = e2;
                    url = null;
                    Log.e(MediaStore.TAG, "Failed to insert image", e);
                    if (url != null) {
                        contentResolver.delete(url, null, null);
                        url = null;
                    }
                    if (url != null) {
                    }
                }
                if (url != null) {
                    return url.toString();
                }
                return null;
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/images/media");
            }
        }

        public static class Thumbnails implements BaseColumns {
            public static final String DATA = "_data";
            public static final String DEFAULT_SORT_ORDER = "image_id ASC";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final int FULL_SCREEN_KIND = 2;
            public static final String HEIGHT = "height";
            public static final String IMAGE_ID = "image_id";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String KIND = "kind";
            public static final int MICRO_KIND = 3;
            public static final int MINI_KIND = 1;
            public static final String THUMB_DATA = "thumb_data";
            public static final String WIDTH = "width";

            public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
                return cr.query(uri, projection, null, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnails(ContentResolver cr, Uri uri, int kind, String[] projection) {
                return cr.query(uri, projection, "kind = " + kind, null, DEFAULT_SORT_ORDER);
            }

            public static final Cursor queryMiniThumbnail(ContentResolver cr, long origId, int kind, String[] projection) {
                Uri uri = EXTERNAL_CONTENT_URI;
                return cr.query(uri, projection, "image_id = " + origId + " AND " + "kind" + " = " + kind, null, null);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, 0);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind, BitmapFactory.Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, 0, kind, options, EXTERNAL_CONTENT_URI, false);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId, long groupId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, groupId);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, BitmapFactory.Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, groupId, kind, options, EXTERNAL_CONTENT_URI, false);
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/images/thumbnails");
            }
        }
    }

    private static class InternalThumbnails implements BaseColumns {
        static final int DEFAULT_GROUP_ID = 0;
        private static final int FULL_SCREEN_KIND = 2;
        private static final int MICRO_KIND = 3;
        private static final int MINI_KIND = 1;
        private static final String[] PROJECTION = {"_id", "_data"};
        private static byte[] sThumbBuf;
        private static final Object sThumbBufLock = new Object();

        private InternalThumbnails() {
        }

        private static Bitmap getMiniThumbFromFile(Cursor c, Uri baseUri, ContentResolver cr, BitmapFactory.Options options) {
            Bitmap bitmap = null;
            Uri thumbUri = null;
            try {
                long thumbId = c.getLong(0);
                String string = c.getString(1);
                thumbUri = ContentUris.withAppendedId(baseUri, thumbId);
                ParcelFileDescriptor pfdInput = cr.openFileDescriptor(thumbUri, "r");
                bitmap = BitmapFactory.decodeFileDescriptor(pfdInput.getFileDescriptor(), null, options);
                pfdInput.close();
                return bitmap;
            } catch (FileNotFoundException ex) {
                Log.e(MediaStore.TAG, "couldn't open thumbnail " + thumbUri + "; " + ex);
                return bitmap;
            } catch (IOException ex2) {
                Log.e(MediaStore.TAG, "couldn't open thumbnail " + thumbUri + "; " + ex2);
                return bitmap;
            } catch (OutOfMemoryError ex3) {
                Log.e(MediaStore.TAG, "failed to allocate memory for thumbnail " + thumbUri + "; " + ex3);
                return bitmap;
            }
        }

        static void cancelThumbnailRequest(ContentResolver cr, long origId, Uri baseUri, long groupId) {
            Cursor c = null;
            try {
                c = cr.query(baseUri.buildUpon().appendQueryParameter("cancel", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter(Contacts.GroupMembership.GROUP_ID, String.valueOf(groupId)).build(), PROJECTION, null, null, null);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x007a, code lost:
            if (r1 != null) goto L_0x007c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x007c, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x008f, code lost:
            if (r1 != null) goto L_0x007c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0092, code lost:
            android.util.Log.w(android.provider.MediaStore.TAG, "getThumbnail for video from kvdb faild!");
         */
        /* JADX WARNING: Removed duplicated region for block: B:110:0x01a4 A[SYNTHETIC, Splitter:B:110:0x01a4] */
        /* JADX WARNING: Removed duplicated region for block: B:123:0x01be  */
        /* JADX WARNING: Removed duplicated region for block: B:127:0x01c8  */
        /* JADX WARNING: Removed duplicated region for block: B:227:0x0339  */
        /* JADX WARNING: Removed duplicated region for block: B:233:0x0347  */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x009d  */
        static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, BitmapFactory.Options options, Uri baseUri, boolean isVideo) {
            MiniThumbFile thumbFile;
            Bitmap bitmap;
            int i;
            MiniThumbFile thumbFile2;
            Cursor c;
            Cursor c2;
            Cursor c3;
            int i2;
            MiniThumbFile thumbFile3;
            Cursor c4;
            ContentResolver contentResolver = cr;
            long j = origId;
            int i3 = kind;
            BitmapFactory.Options options2 = options;
            Uri uri = baseUri;
            boolean z = isVideo;
            Bitmap bitmap2 = null;
            if (HwGalleryCacheManager.isGalleryCacheEffect() && 1 == i3 && z && j <= 2147483647L) {
                try {
                    c4 = contentResolver.query(Uri.parse(baseUri.buildUpon().appendPath(String.valueOf(origId)).toString().replaceFirst("thumbnails", MediaStore.AUTHORITY)), new String[]{"_id", "_data", "date_modified"}, null, null, null);
                    if (c4 != null) {
                        try {
                            if (c4.moveToFirst()) {
                                bitmap2 = HwGalleryCacheManager.getGalleryCachedVideo((int) j, c4.getLong(c4.getColumnIndexOrThrow("date_modified")), options2);
                                if (bitmap2 != null) {
                                    if (c4 != null) {
                                        c4.close();
                                    }
                                    return bitmap2;
                                }
                            }
                        } catch (SQLiteException e) {
                            try {
                                Log.w(MediaStore.TAG, "sqlite exception");
                            } catch (Throwable th) {
                                th = th;
                                if (c4 != null) {
                                }
                                throw th;
                            }
                        }
                    }
                } catch (SQLiteException e2) {
                    c4 = null;
                    Log.w(MediaStore.TAG, "sqlite exception");
                } catch (Throwable th2) {
                    th = th2;
                    c4 = null;
                    if (c4 != null) {
                        c4.close();
                    }
                    throw th;
                }
            }
            MiniThumbFile thumbFile4 = MiniThumbFile.instance(z ? Video.Media.EXTERNAL_CONTENT_URI : Images.Media.EXTERNAL_CONTENT_URI);
            Cursor c5 = null;
            try {
                if (thumbFile4.getMagic(j) != 0) {
                    if (i3 == 3) {
                        try {
                            synchronized (sThumbBufLock) {
                                if (sThumbBuf == null) {
                                    sThumbBuf = new byte[10000];
                                }
                                if (thumbFile4.getMiniThumbFromFile(j, sThumbBuf) != null) {
                                    bitmap2 = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                                    if (bitmap2 == null) {
                                        Log.w(MediaStore.TAG, "couldn't decode byte array.");
                                    }
                                }
                            }
                            if (c5 != null) {
                                c5.close();
                            }
                            thumbFile4.deactivate();
                            return bitmap2;
                        } catch (SQLiteException e3) {
                            ex = e3;
                            thumbFile = thumbFile4;
                            try {
                                Log.w(MediaStore.TAG, (Throwable) ex);
                                if (c5 != null) {
                                }
                                thumbFile.deactivate();
                                return bitmap2;
                            } catch (Throwable th3) {
                                thumbFile = th3;
                                if (c5 != null) {
                                    c5.close();
                                }
                                thumbFile.deactivate();
                                throw thumbFile;
                            }
                        } catch (Throwable th4) {
                            thumbFile = th4;
                            thumbFile = thumbFile4;
                            if (c5 != null) {
                            }
                            thumbFile.deactivate();
                            throw thumbFile;
                        }
                    } else if (i3 == 1) {
                        String column = z ? "video_id=" : "image_id=";
                        try {
                            i = 3;
                            Cursor c6 = c5;
                            bitmap = bitmap2;
                            thumbFile2 = thumbFile4;
                            try {
                                c5 = contentResolver.query(uri, PROJECTION, column + j, null, null);
                                if (c5 != null) {
                                    try {
                                        if (c5.moveToFirst()) {
                                            Bitmap bitmap3 = getMiniThumbFromFile(c5, uri, contentResolver, options2);
                                            if (bitmap3 != null) {
                                                if (c5 != null) {
                                                    c5.close();
                                                }
                                                thumbFile2.deactivate();
                                                return bitmap3;
                                            }
                                            bitmap = bitmap3;
                                        }
                                    } catch (SQLiteException e4) {
                                        ex = e4;
                                        thumbFile = thumbFile2;
                                        bitmap2 = bitmap;
                                        Log.w(MediaStore.TAG, (Throwable) ex);
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        return bitmap2;
                                    } catch (Throwable th5) {
                                        thumbFile = th5;
                                        thumbFile = thumbFile2;
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        throw thumbFile;
                                    }
                                }
                                c = c5;
                                Uri blockingUri = baseUri.buildUpon().appendQueryParameter("blocking", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter(Contacts.GroupMembership.GROUP_ID, String.valueOf(groupId)).build();
                                if (c != null) {
                                    try {
                                        c.close();
                                    } catch (SQLiteException e5) {
                                        ex = e5;
                                        c5 = c;
                                    } catch (Throwable th6) {
                                        thumbFile = th6;
                                        c5 = c;
                                        thumbFile = thumbFile2;
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        throw thumbFile;
                                    }
                                }
                                c2 = c;
                                c3 = contentResolver.query(blockingUri, PROJECTION, null, null, null);
                                if (c3 != null) {
                                    if (c3 != null) {
                                        c3.close();
                                    }
                                    thumbFile2.deactivate();
                                    return null;
                                }
                                if (i3 == i) {
                                    try {
                                        synchronized (sThumbBufLock) {
                                            if (sThumbBuf == null) {
                                                sThumbBuf = new byte[10000];
                                            }
                                            Arrays.fill(sThumbBuf, (byte) 0);
                                            if (thumbFile2.getMiniThumbFromFile(j, sThumbBuf) != null) {
                                                bitmap = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                                                if (bitmap == null) {
                                                    Log.w(MediaStore.TAG, "couldn't decode byte array.");
                                                }
                                            }
                                        }
                                        i2 = 1;
                                    } catch (SQLiteException e6) {
                                        ex = e6;
                                        c5 = c3;
                                    } catch (Throwable th7) {
                                        thumbFile = th7;
                                        c5 = c3;
                                        thumbFile = thumbFile2;
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        throw thumbFile;
                                    }
                                } else {
                                    i2 = 1;
                                    if (i3 == 1) {
                                        try {
                                            if (c3.moveToFirst()) {
                                                bitmap = getMiniThumbFromFile(c3, uri, contentResolver, options2);
                                            }
                                        } catch (SQLiteException e7) {
                                            ex = e7;
                                            Cursor cursor = c3;
                                            thumbFile = thumbFile2;
                                            bitmap2 = bitmap;
                                            c5 = cursor;
                                            Log.w(MediaStore.TAG, (Throwable) ex);
                                            if (c5 != null) {
                                            }
                                            thumbFile.deactivate();
                                            return bitmap2;
                                        } catch (Throwable th8) {
                                            thumbFile = th8;
                                            Cursor cursor2 = c3;
                                            thumbFile = thumbFile2;
                                            c5 = cursor2;
                                            if (c5 != null) {
                                            }
                                            thumbFile.deactivate();
                                            throw thumbFile;
                                        }
                                    } else {
                                        Cursor c7 = c3;
                                        thumbFile = thumbFile2;
                                        try {
                                            throw new IllegalArgumentException("Unsupported kind: " + i3);
                                        } catch (SQLiteException e8) {
                                            ex = e8;
                                            bitmap2 = bitmap;
                                            c5 = c7;
                                            Log.w(MediaStore.TAG, (Throwable) ex);
                                            if (c5 != null) {
                                            }
                                            thumbFile.deactivate();
                                            return bitmap2;
                                        } catch (Throwable th9) {
                                            thumbFile = th9;
                                            c5 = c7;
                                            if (c5 != null) {
                                            }
                                            thumbFile.deactivate();
                                            throw thumbFile;
                                        }
                                    }
                                }
                                if (bitmap == null) {
                                    Log.v(MediaStore.TAG, "Create the thumbnail in memory: origId=" + j + ", kind=" + i3 + ", isVideo=" + z);
                                    Uri uri2 = Uri.parse(baseUri.buildUpon().appendPath(String.valueOf(origId)).toString().replaceFirst("thumbnails", MediaStore.AUTHORITY));
                                    if (c3 != null) {
                                        c3.close();
                                    }
                                    MiniThumbFile thumbFile5 = thumbFile2;
                                    Cursor c8 = c3;
                                    int i4 = i2;
                                    try {
                                        c5 = contentResolver.query(uri2, PROJECTION, null, null, null);
                                        if (c5 != null) {
                                            try {
                                                if (c5.moveToFirst()) {
                                                    String filePath = c5.getString(i4);
                                                    if (filePath != null) {
                                                        if (z) {
                                                            bitmap = ThumbnailUtils.createVideoThumbnail(filePath, i3);
                                                        } else {
                                                            bitmap = ThumbnailUtils.createImageThumbnail(filePath, i3);
                                                        }
                                                    }
                                                    thumbFile3 = thumbFile5;
                                                }
                                            } catch (SQLiteException e9) {
                                                ex = e9;
                                                bitmap2 = bitmap;
                                                thumbFile = thumbFile5;
                                                Log.w(MediaStore.TAG, (Throwable) ex);
                                                if (c5 != null) {
                                                }
                                                thumbFile.deactivate();
                                                return bitmap2;
                                            } catch (Throwable th10) {
                                                thumbFile = th10;
                                                thumbFile = thumbFile5;
                                                if (c5 != null) {
                                                }
                                                thumbFile.deactivate();
                                                throw thumbFile;
                                            }
                                        }
                                        if (c5 != null) {
                                            c5.close();
                                        }
                                        thumbFile5.deactivate();
                                        return null;
                                    } catch (SQLiteException e10) {
                                        ex = e10;
                                        thumbFile = thumbFile5;
                                        bitmap2 = bitmap;
                                        c5 = c8;
                                        Log.w(MediaStore.TAG, (Throwable) ex);
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        return bitmap2;
                                    } catch (Throwable th11) {
                                        thumbFile = th11;
                                        thumbFile = thumbFile5;
                                        c5 = c8;
                                        if (c5 != null) {
                                        }
                                        thumbFile.deactivate();
                                        throw thumbFile;
                                    }
                                } else {
                                    Cursor c9 = c3;
                                    thumbFile3 = thumbFile2;
                                    c5 = c9;
                                }
                                if (c5 != null) {
                                    c5.close();
                                }
                                thumbFile3.deactivate();
                                bitmap2 = bitmap;
                                return bitmap2;
                            } catch (SQLiteException e11) {
                                ex = e11;
                                thumbFile = thumbFile2;
                                c5 = c6;
                                bitmap2 = bitmap;
                                Log.w(MediaStore.TAG, (Throwable) ex);
                                if (c5 != null) {
                                }
                                thumbFile.deactivate();
                                return bitmap2;
                            } catch (Throwable th12) {
                                thumbFile = th12;
                                thumbFile = thumbFile2;
                                c5 = c6;
                                if (c5 != null) {
                                }
                                thumbFile.deactivate();
                                throw thumbFile;
                            }
                        } catch (SQLiteException e12) {
                            ex = e12;
                            Cursor cursor3 = c5;
                            Bitmap bitmap4 = bitmap2;
                            thumbFile = thumbFile4;
                            Log.w(MediaStore.TAG, (Throwable) ex);
                            if (c5 != null) {
                            }
                            thumbFile.deactivate();
                            return bitmap2;
                        } catch (Throwable th13) {
                            thumbFile = th13;
                            Cursor cursor4 = c5;
                            Bitmap bitmap5 = bitmap2;
                            thumbFile = thumbFile4;
                            if (c5 != null) {
                            }
                            thumbFile.deactivate();
                            throw thumbFile;
                        }
                    }
                }
                i = 3;
                bitmap = bitmap2;
                thumbFile2 = thumbFile4;
                c = c5;
                try {
                    Uri blockingUri2 = baseUri.buildUpon().appendQueryParameter("blocking", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter(Contacts.GroupMembership.GROUP_ID, String.valueOf(groupId)).build();
                    if (c != null) {
                    }
                    c2 = c;
                } catch (SQLiteException e13) {
                    ex = e13;
                    thumbFile = thumbFile2;
                    c5 = c;
                    bitmap2 = bitmap;
                    Log.w(MediaStore.TAG, (Throwable) ex);
                    if (c5 != null) {
                    }
                    thumbFile.deactivate();
                    return bitmap2;
                } catch (Throwable th14) {
                    thumbFile = th14;
                    thumbFile = thumbFile2;
                    c5 = c;
                    if (c5 != null) {
                    }
                    thumbFile.deactivate();
                    throw thumbFile;
                }
                try {
                    c3 = contentResolver.query(blockingUri2, PROJECTION, null, null, null);
                    if (c3 != null) {
                    }
                } catch (SQLiteException e14) {
                    ex = e14;
                    thumbFile = thumbFile2;
                    c5 = c2;
                    bitmap2 = bitmap;
                    Log.w(MediaStore.TAG, (Throwable) ex);
                    if (c5 != null) {
                        c5.close();
                    }
                    thumbFile.deactivate();
                    return bitmap2;
                } catch (Throwable th15) {
                    thumbFile = th15;
                    thumbFile = thumbFile2;
                    c5 = c2;
                    if (c5 != null) {
                    }
                    thumbFile.deactivate();
                    throw thumbFile;
                }
            } catch (SQLiteException e15) {
                ex = e15;
                Cursor cursor5 = c5;
                thumbFile = thumbFile4;
                Bitmap bitmap6 = bitmap2;
                Log.w(MediaStore.TAG, (Throwable) ex);
                if (c5 != null) {
                }
                thumbFile.deactivate();
                return bitmap2;
            } catch (Throwable th16) {
                thumbFile = th16;
                Cursor cursor6 = c5;
                thumbFile = thumbFile4;
                Bitmap bitmap7 = bitmap2;
                if (c5 != null) {
                }
                thumbFile.deactivate();
                throw thumbFile;
            }
        }
    }

    public interface MediaColumns extends BaseColumns {
        public static final String DATA = "_data";
        public static final String DATE_ADDED = "date_added";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String HEIGHT = "height";
        public static final String IS_DRM = "is_drm";
        public static final String MEDIA_SCANNER_NEW_OBJECT_ID = "media_scanner_new_object_id";
        public static final String MIME_TYPE = "mime_type";
        public static final String SIZE = "_size";
        public static final String TITLE = "title";
        public static final String WIDTH = "width";
    }

    public static final class Video {
        public static final String DEFAULT_SORT_ORDER = "_display_name";

        public static final class Media implements VideoColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/video";
            public static final String DEFAULT_SORT_ORDER = "title";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/video/media");
            }
        }

        public static class Thumbnails implements BaseColumns {
            public static final String DATA = "_data";
            public static final String DEFAULT_SORT_ORDER = "video_id ASC";
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");
            public static final int FULL_SCREEN_KIND = 2;
            public static final String HEIGHT = "height";
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");
            public static final String KIND = "kind";
            public static final int MICRO_KIND = 3;
            public static final int MINI_KIND = 1;
            public static final String VIDEO_ID = "video_id";
            public static final String WIDTH = "width";

            public static void cancelThumbnailRequest(ContentResolver cr, long origId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, 0);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind, BitmapFactory.Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, 0, kind, options, EXTERNAL_CONTENT_URI, true);
            }

            public static Bitmap getThumbnail(ContentResolver cr, long origId, long groupId, int kind, BitmapFactory.Options options) {
                return InternalThumbnails.getThumbnail(cr, origId, groupId, kind, options, EXTERNAL_CONTENT_URI, true);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId, long groupId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, EXTERNAL_CONTENT_URI, groupId);
            }

            public static Uri getContentUri(String volumeName) {
                return Uri.parse(MediaStore.CONTENT_AUTHORITY_SLASH + volumeName + "/video/thumbnails");
            }
        }

        public interface VideoColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ARTIST = "artist";
            public static final String BOOKMARK = "bookmark";
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
            public static final String BUCKET_ID = "bucket_id";
            public static final String CATEGORY = "category";
            public static final String DATE_TAKEN = "datetaken";
            public static final String DESCRIPTION = "description";
            public static final String DURATION = "duration";
            public static final String IS_PRIVATE = "isprivate";
            public static final String LANGUAGE = "language";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
            public static final String RESOLUTION = "resolution";
            public static final String TAGS = "tags";
        }

        public static final Cursor query(ContentResolver cr, Uri uri, String[] projection) {
            return cr.query(uri, projection, null, null, "_display_name");
        }
    }

    public static Uri getMediaScannerUri() {
        return Uri.parse("content://media/none/media_scanner");
    }

    public static String getVersion(Context context) {
        Cursor c = context.getContentResolver().query(Uri.parse("content://media/none/version"), null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getString(0);
                }
                c.close();
            } finally {
                c.close();
            }
        }
        return null;
    }

    public static Uri getDocumentUri(Context context, Uri mediaUri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            return getDocumentUri(resolver, getFilePath(resolver, mediaUri), resolver.getPersistedUriPermissions());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    private static String getFilePath(ContentResolver resolver, Uri mediaUri) throws RemoteException {
        Cursor c;
        ContentProviderClient client = resolver.acquireUnstableContentProviderClient(AUTHORITY);
        try {
            c = client.query(mediaUri, new String[]{"_data"}, null, null, null);
            if (c.getCount() == 0) {
                throw new IllegalStateException("Not found media file under URI: " + mediaUri);
            } else if (c.moveToFirst()) {
                String path = c.getString(0);
                IoUtils.closeQuietly(c);
                if (client != null) {
                    $closeResource(null, client);
                }
                return path;
            } else {
                throw new IllegalStateException("Failed to move cursor to the first item.");
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            try {
                throw th2;
            } catch (Throwable th3) {
                if (client != null) {
                    $closeResource(th2, client);
                }
                throw th3;
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002a, code lost:
        if (r0 != null) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002c, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0026, code lost:
        r2 = move-exception;
     */
    private static Uri getDocumentUri(ContentResolver resolver, String path, List<UriPermission> uriPermissions) throws RemoteException {
        ContentProviderClient client = resolver.acquireUnstableContentProviderClient(DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY);
        Bundle in = new Bundle();
        in.putParcelableList("com.android.externalstorage.documents.extra.uriPermissions", uriPermissions);
        Uri uri = (Uri) client.call("getDocumentId", path, in).getParcelable("uri");
        if (client != null) {
            $closeResource(null, client);
        }
        return uri;
    }

    public static String getPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(SettingsStringUtil.DELIMITER);
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                try {
                    return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(DocumentsContract.getDocumentId(uri))), null, null);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "exception get contenturi.");
                    return null;
                }
            } else if (isMediaDocument(uri)) {
                String[] split2 = DocumentsContract.getDocumentId(uri).split(SettingsStringUtil.DELIMITER);
                String type = split2[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
                }
                return getDataColumn(context, contentUri, "_id=?", new String[]{split2[1]});
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002e, code lost:
        if (r0 != null) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0049, code lost:
        if (r0 == null) goto L_0x004c;
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                if (cursor != null) {
                    cursor.close();
                }
                return string;
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "SQLiteException when getDataColumn");
        } catch (IllegalArgumentException e2) {
            Log.w(TAG, "IllegalArgumentException when getDataColumn");
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY.equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
