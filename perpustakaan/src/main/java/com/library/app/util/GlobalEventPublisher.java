package com.library.app.util;

import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalEventPublisher {
    private static final List<Runnable> feedbackListeners = new CopyOnWriteArrayList<>();
    private static final List<Runnable> procurementListeners = new CopyOnWriteArrayList<>();

    public static void addFeedbackListener(Runnable listener) {
        feedbackListeners.add(listener);
    }

    public static void addProcurementListener(Runnable listener) {
        procurementListeners.add(listener);
    }

    public static void publishFeedbackUpdated() {
        for (Runnable listener : feedbackListeners) {
            Platform.runLater(listener);
        }
    }

    public static void publishProcurementUpdated() {
        for (Runnable listener : procurementListeners) {
            Platform.runLater(listener);
        }
    }
}
