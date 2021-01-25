package ohos.media.camera.mode.adapter.utils.constant;

public enum AiMovieEffects {
    AI_MOVIE_NO_EFFECT((byte) 0),
    AI_MOVIE_PORTRAIT_FICTITIOUS_EFFECT((byte) 1),
    AI_MOVIE_AICOLOR_EFFECT((byte) 2),
    AI_MOVIE_NOSTALGIA_EFFECT((byte) 3),
    AI_MOVIE_FRESH_EFFECT((byte) 4),
    AI_MOVIE_HITCHCOCK_EFFECT((byte) 5);
    
    private final Byte aiMovieValue;

    private AiMovieEffects(Byte b) {
        this.aiMovieValue = b;
    }

    public Byte getAiMovieValue() {
        return this.aiMovieValue;
    }
}
