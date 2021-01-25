package ohos.media.photokit.metadata;

import java.util.List;
import java.util.Set;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.media.photokit.adapter.AVStorageAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.net.Uri;

public final class AVStorage {
    private static final String EXTERNAL_PRIMARY = "external_primary";
    private static final String EXTERNAL_STORAGE = "external";
    private static final String INTERNAL_STORAGE = "internal";
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVStorage.class);
    public static final Uri MEDIA_AUTH_HEAD_URI = Uri.parse("dataability:///media");

    public interface AVBaseColumns {
        public static final String DATA = "_data";
        public static final String DATE_ADDED = "date_added";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String DURATION = "duration";
        public static final String ID = "_id";
        public static final String MIME_TYPE = "mime_type";
        public static final String OUTPUT = "output";
        public static final String SIZE = "_size";
        public static final String TITLE = "title";
        public static final String VOLUME_NAME = "volume_name";
    }

    private AVStorage() {
    }

    public static Uri appendPendingResource(Uri uri) {
        if (uri != null) {
            return uri.makeBuilder().appendDecodedQueryParam("includePending", "1").build();
        }
        throw new IllegalArgumentException("uri should not be null");
    }

    public static Uri appendRequireOriginalResource(Uri uri) {
        if (uri != null) {
            return uri.makeBuilder().appendDecodedQueryParam("requireOriginal", "1").build();
        }
        throw new IllegalArgumentException("uri should not be null");
    }

    public static String fetchVolumeName(Uri uri) {
        if (uri != null) {
            List<String> decodedPathList = uri.getDecodedPathList();
            if (decodedPathList != null && decodedPathList.size() > 1) {
                return decodedPathList.get(1);
            }
            throw new IllegalArgumentException("missing volume name: " + uri);
        }
        throw new IllegalArgumentException("uri should not be null");
    }

    public static Set<String> fetchExternalVolumeNames(Context context) {
        return AVStorageAdapter.fetchExternalVolumeNames(context);
    }

    public static Uri fetchMediaResource(Context context, Uri uri) {
        return AVStorageAdapter.fetchMediaResource(context, uri);
    }

    public static Uri fetchDocumentResource(Context context, Uri uri) {
        return AVStorageAdapter.fetchDocumentResource(context, uri);
    }

    public static String fetchVersion(Context context) {
        return fetchVersion(context, EXTERNAL_PRIMARY);
    }

    public static String fetchVersion(Context context, String str) {
        return AVStorageAdapter.fetchVersion(context, str);
    }

    public static Uri fetchLoggerResource() {
        return MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath("none/media_scanner").build();
    }

    public static final class Audio {
        private Audio() {
        }

        public static String convertNameToKey(String str) {
            return AVStorageAdapter.Audio.convertNameToKey(str);
        }

        public static final class Media implements AVBaseColumns {
            public static final Uri EXTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.EXTERNAL_STORAGE + "/audio/media");
            public static final Uri INTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.INTERNAL_STORAGE + "/audio/media");

            private Media() {
            }

            public static Uri fetchResource(String str) {
                if (str == null) {
                    throw new IllegalArgumentException("volumeName should not be null");
                } else if (str.equals(AVStorage.INTERNAL_STORAGE)) {
                    return INTERNAL_DATA_ABILITY_URI;
                } else {
                    if (str.equals(AVStorage.EXTERNAL_PRIMARY)) {
                        return EXTERNAL_DATA_ABILITY_URI;
                    }
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("audio/media").build();
                }
            }
        }

        public static final class Genres implements AVBaseColumns {
            private Genres() {
            }

            public static Uri fetchResource(String str) {
                if (str != null) {
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("audio/genres").build();
                }
                throw new IllegalArgumentException("volumeName should not be null");
            }

            public static Uri fetchResourceForAudioId(String str, int i) {
                if (str != null) {
                    Uri.Builder appendDecodedPath = AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str);
                    return appendDecodedPath.appendDecodedPath("audio/media/" + String.valueOf(i) + "/genres").build();
                }
                throw new IllegalArgumentException("volumeName should not be null");
            }

            public static final class Members implements AVBaseColumns {
                private Members() {
                }

                public static Uri fetchResource(String str, long j) {
                    if (str != null) {
                        Uri.Builder appendDecodedPath = AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str);
                        return appendDecodedPath.appendDecodedPath("audio/genres/" + String.valueOf(j) + "/members").build();
                    }
                    throw new IllegalArgumentException("volumeName should not be null");
                }
            }
        }

        public static final class Playlists implements AVBaseColumns {
            public static final Uri EXTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.EXTERNAL_STORAGE + "/audio/playlists");
            public static final Uri INTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.INTERNAL_STORAGE + "/audio/playlists");

            private Playlists() {
            }

            public static Uri fetchResource(String str) {
                if (str == null) {
                    throw new IllegalArgumentException("volumeName should not be null");
                } else if (str.equals(AVStorage.INTERNAL_STORAGE)) {
                    return INTERNAL_DATA_ABILITY_URI;
                } else {
                    if (str.equals(AVStorage.EXTERNAL_STORAGE)) {
                        return EXTERNAL_DATA_ABILITY_URI;
                    }
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("audio/playlists").build();
                }
            }

            public static final class Members implements AVBaseColumns {
                private Members() {
                }

                public static Uri fetchResource(String str, long j) {
                    if (str != null) {
                        Uri.Builder appendDecodedPath = AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str);
                        return appendDecodedPath.appendDecodedPath("audio/playlists/" + String.valueOf(j) + "/members").build();
                    }
                    throw new IllegalArgumentException("volumeName should not be null");
                }

                public static boolean updatePlaylistItem(DataAbilityHelper dataAbilityHelper, long j, int i, int i2) {
                    int i3;
                    Uri.Builder makeBuilder = AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder();
                    Uri build = makeBuilder.appendDecodedPath("external/audio/playlists/" + String.valueOf(j) + "/members/" + String.valueOf(i)).appendDecodedQueryParam("move", "true").build();
                    ValuesBucket valuesBucket = new ValuesBucket();
                    valuesBucket.putInteger("play_order", Integer.valueOf(i2));
                    try {
                        i3 = dataAbilityHelper.update(build, valuesBucket, (DataAbilityPredicates) null);
                    } catch (DataAbilityRemoteException e) {
                        AVStorage.LOGGER.error("dataAbilityHelper update playlist failed, ex: %{public}s", e.getMessage());
                        i3 = -1;
                    }
                    return i3 != 0;
                }
            }
        }

        public static final class Albums implements AVBaseColumns {
            private Albums() {
            }

            public static Uri fetchResource(String str) {
                if (str != null) {
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("audio/albums").build();
                }
                throw new IllegalArgumentException("volumeName should not be null");
            }
        }

        public static final class Artists implements AVBaseColumns {
            private Artists() {
            }

            public static Uri fetchResource(String str) {
                if (str != null) {
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("audio/artists").build();
                }
                throw new IllegalArgumentException("volumeName should not be null");
            }

            public static final class Albums {
                private Albums() {
                }

                public static Uri fetchResource(String str, long j) {
                    if (str != null) {
                        Uri.Builder appendDecodedPath = AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str);
                        return appendDecodedPath.appendDecodedPath("audio/artists/" + String.valueOf(j) + "/albums").build();
                    }
                    throw new IllegalArgumentException("volumeName should not be null");
                }
            }
        }
    }

    public static final class Downloads implements AVBaseColumns {
        public static final Uri EXTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.EXTERNAL_STORAGE + "/downloads");
        public static final Uri INTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.INTERNAL_STORAGE + "/downloads");

        private Downloads() {
        }

        public static Uri fetchResource(String str) {
            if (str == null) {
                throw new IllegalArgumentException("volumeName should not be null");
            } else if (str.equals(AVStorage.INTERNAL_STORAGE)) {
                return INTERNAL_DATA_ABILITY_URI;
            } else {
                if (str.equals(AVStorage.EXTERNAL_STORAGE)) {
                    return EXTERNAL_DATA_ABILITY_URI;
                }
                return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("downloads").build();
            }
        }
    }

    public static final class Files {
        private Files() {
        }

        public static Uri fetchResource(String str) {
            if (str != null) {
                return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("file").build();
            }
            throw new IllegalArgumentException("volumeName should not be null");
        }
    }

    public static final class Images {
        private Images() {
        }

        public static final class Media implements AVBaseColumns {
            public static final Uri EXTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.EXTERNAL_STORAGE + "/images/media");
            public static final Uri INTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.INTERNAL_STORAGE + "/images/media");

            private Media() {
            }

            public static Uri fetchResource(String str) {
                if (str == null) {
                    throw new IllegalArgumentException("volumeName should not be null");
                } else if (str.equals(AVStorage.INTERNAL_STORAGE)) {
                    return INTERNAL_DATA_ABILITY_URI;
                } else {
                    if (str.equals(AVStorage.EXTERNAL_PRIMARY)) {
                        return EXTERNAL_DATA_ABILITY_URI;
                    }
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("images/media").build();
                }
            }
        }
    }

    public static final class Video {
        private Video() {
        }

        public static final class Media implements AVBaseColumns {
            public static final Uri EXTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.EXTERNAL_STORAGE + "/video/media");
            public static final Uri INTERNAL_DATA_ABILITY_URI = Uri.parse(AVStorage.MEDIA_AUTH_HEAD_URI + "/" + AVStorage.INTERNAL_STORAGE + "/video/media");

            private Media() {
            }

            public static Uri fetchResource(String str) {
                if (str == null) {
                    throw new IllegalArgumentException("volumeName should not be null");
                } else if (str.equals(AVStorage.INTERNAL_STORAGE)) {
                    return INTERNAL_DATA_ABILITY_URI;
                } else {
                    if (str.equals(AVStorage.EXTERNAL_PRIMARY)) {
                        return EXTERNAL_DATA_ABILITY_URI;
                    }
                    return AVStorage.MEDIA_AUTH_HEAD_URI.makeBuilder().appendDecodedPath(str).appendDecodedPath("video/media").build();
                }
            }
        }
    }
}
