package com.library.app.ui.panel;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public final class KioskIconFactory {
    private KioskIconFactory() {
    }

    public static Node createLibraryLogo(double size) {
        double bookWidth = size * 0.42;
        double bookHeight = size * 0.62;

        Rectangle leftBook = new Rectangle(bookWidth, bookHeight);
        leftBook.setArcWidth(8);
        leftBook.setArcHeight(8);
        leftBook.setFill(Color.web("#374151"));
        leftBook.setTranslateX(-bookWidth * 0.34);

        Rectangle rightBook = new Rectangle(bookWidth, bookHeight);
        rightBook.setArcWidth(8);
        rightBook.setArcHeight(8);
        rightBook.setFill(Color.web("#2FAAC6"));
        rightBook.setTranslateX(bookWidth * 0.34);

        Rectangle spine = new Rectangle(size * 0.12, bookHeight);
        spine.setArcWidth(6);
        spine.setArcHeight(6);
        spine.setFill(Color.web("#1F2937"));

        StackPane icon = new StackPane(leftBook, rightBook, spine);
        icon.setPrefSize(size, size * 0.8);
        return icon;
    }

    public static Node createVisitIcon(Color color) {
        Group group = new Group();
        group.getChildren().addAll(
                iconPath("M9 8A3 3 0 1 1 15 8A3 3 0 0 1 9 8", color, 2.0),
                iconPath("M4.8 19A5.2 5.2 0 0 1 12.2 14.4", color, 2.0),
                iconPath("M14.2 15.8L16.3 17.9L20 14", color, 2.0)
        );
        return centeredIcon(group);
    }

    public static Node createSearchIcon(Color color) {
        Pane pane = new Pane();
        pane.setPrefSize(24, 24);
        pane.getChildren().addAll(
                iconPath("M11 4A7 7 0 1 1 11 18A7 7 0 0 1 11 4", color, 2.0),
                iconPath("M16 16L20 20", color, 2.0)
        );
        return centeredIcon(pane);
    }

    public static Node createFeedbackIcon(Color color) {
        Pane pane = new Pane();
        pane.setPrefSize(24, 24);
        pane.getChildren().addAll(
                iconPath("M6 5.5H18A2 2 0 0 1 20 7.5V14A2 2 0 0 1 18 16H11L7.8 18.8V16H6A2 2 0 0 1 4 14V7.5A2 2 0 0 1 6 5.5", color, 2.0),
                iconPath("M12 8.8V12.5", color, 2.0),
                iconPath("M12 14.8H12.01", color, 2.3)
        );
        return centeredIcon(pane);
    }

    public static Node createRequestIcon(Color color) {
        Pane pane = new Pane();
        pane.setPrefSize(24, 24);
        pane.getChildren().addAll(
                iconPath("M5.5 6H18.5A1.5 1.5 0 0 1 20 7.5V17.5A1.5 1.5 0 0 1 18.5 19H5.5A1.5 1.5 0 0 1 4 17.5V7.5A1.5 1.5 0 0 1 5.5 6", color, 2.0),
                iconPath("M4 10.5H20", color, 2.0),
                iconPath("M12 11.5V15", color, 2.0),
                iconPath("M9.9 13.7L12 15.8L14.1 13.7", color, 2.0)
        );
        return centeredIcon(pane);
    }

    private static Node centeredIcon(Node icon) {
        StackPane wrapper = new StackPane(icon);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMinSize(24, 24);
        wrapper.setPrefSize(24, 24);
        wrapper.setMaxSize(24, 24);
        return wrapper;
    }

    private static SVGPath iconPath(String pathData, Color color, double strokeWidth) {
        SVGPath path = strokeShape(new SVGPath(), color, strokeWidth);
        path.setContent(pathData);
        path.setFill(Color.TRANSPARENT);
        return path;
    }

    private static <T extends Shape> T strokeShape(T shape, Color color, double strokeWidth) {
        shape.setStroke(color);
        shape.setStrokeWidth(strokeWidth);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
        shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return shape;
    }
}
