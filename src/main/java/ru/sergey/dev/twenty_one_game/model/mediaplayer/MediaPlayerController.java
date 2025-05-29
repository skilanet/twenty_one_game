package ru.sergey.dev.twenty_one_game.model.mediaplayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

public class MediaPlayerController {
    private static MediaPlayer mediaPlayer;
    private static boolean playing = false;

    public static void initialize(Class<?> clazz) {
        String musicFile = "/music/mikhail-krug-vladimirskijj-central.mp3";
        Media sound = new Media(Objects.requireNonNull(clazz.getResource(musicFile)).toExternalForm());

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = new MediaPlayer(sound);

        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        play();
    }

    public static void play() {
        playing = true;
        mediaPlayer.play();
    }

    public static void pause() {
        playing = false;
        mediaPlayer.pause();
    }

    public static void stop() {
        playing = false;
        mediaPlayer.stop();
        mediaPlayer = null;
    }
    public static boolean isMediaPlayerNotNull() {
        return mediaPlayer != null;
    }

    public static boolean isPlaying() {
        return playing;
    }
}
