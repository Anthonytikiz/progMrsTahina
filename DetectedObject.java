package jeu;

import org.opencv.core.Rect;

public class DetectedObject {
    public String type; // "rouge", "bleu", ou "ballon"
    public org.opencv.core.Rect boundingRect;

    public DetectedObject(String type, Rect boundingRect) {
        this.type = type;
        this.boundingRect = boundingRect;
    }
}