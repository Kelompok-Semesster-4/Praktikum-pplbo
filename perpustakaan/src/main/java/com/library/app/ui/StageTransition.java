package com.library.app.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class StageTransition {
    private static final double FADE_MIN_OPACITY = 0.88;
    private static final Duration FADE_OUT_DURATION = Duration.millis(110);
    private static final Duration FADE_IN_DURATION = Duration.millis(150);

    private StageTransition() {
    }

    public static void switchScene(Stage stage, Runnable sceneSwitcher) {
        if (sceneSwitcher == null) {
            return;
        }

        if (stage == null) {
            sceneSwitcher.run();
            return;
        }

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> switchScene(stage, sceneSwitcher));
            return;
        }

        Scene currentScene = stage.getScene();
        Node currentRoot = currentScene == null ? null : currentScene.getRoot();
        if (!stage.isShowing() || currentRoot == null) {
            sceneSwitcher.run();
            return;
        }

        currentRoot.setDisable(true);

        FadeTransition fadeOut = new FadeTransition(FADE_OUT_DURATION, currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(FADE_MIN_OPACITY);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);
        fadeOut.setOnFinished(event -> runSwitchAndFadeIn(stage, sceneSwitcher, currentRoot));
        fadeOut.play();
    }

    private static void runSwitchAndFadeIn(Stage stage, Runnable sceneSwitcher, Node previousRoot) {
        try {
            sceneSwitcher.run();
        } catch (RuntimeException exception) {
            previousRoot.setDisable(false);
            previousRoot.setOpacity(1.0);
            throw exception;
        }

        Scene nextScene = stage.getScene();
        Node nextRoot = nextScene == null ? null : nextScene.getRoot();
        if (nextRoot == null) {
            return;
        }

        nextRoot.setDisable(true);
        nextRoot.setOpacity(FADE_MIN_OPACITY);

        FadeTransition fadeIn = new FadeTransition(FADE_IN_DURATION, nextRoot);
        fadeIn.setFromValue(FADE_MIN_OPACITY);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_BOTH);
        fadeIn.setOnFinished(done -> nextRoot.setDisable(false));
        fadeIn.play();
    }
}