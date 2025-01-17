package jeu;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
// import org.w3c.dom.css.Rect;

import jeu.DetectedObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ColorDetection {
    static {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java481.dll");
    }

    public static List<DetectedObject> detectColor(String filePath, String colorName, Scalar lowerBound, Scalar upperBound) {
        List<DetectedObject> detectedObjects = new ArrayList<>();
        Mat image = Imgcodecs.imread(filePath);
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerBound, upperBound, mask);
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        for (MatOfPoint contour : contours) {
            org.opencv.core.Rect boundingRect = Imgproc.boundingRect(contour);
            detectedObjects.add(new DetectedObject(colorName, boundingRect));
        }
        return detectedObjects;
    }

    public static void analyzeGame(List<DetectedObject> objects, String filePath) {
        DetectedObject ball = null;
        List<DetectedObject> redPlayers = new ArrayList<>();
        List<DetectedObject> bluePlayers = new ArrayList<>();

        for (DetectedObject obj : objects) {
            switch (obj.type) {
                case "ballon":
                if (obj.boundingRect.height>1 && obj.boundingRect.width>1) {
                    ball = obj;
                }
                    break;
                case "rouge":
                    redPlayers.add(obj);
                    break;
                case "bleu":
                    bluePlayers.add(obj);
                    break;
            }
        }

        if (ball != null) {
            DetectedObject closestPlayer = null;
            double minDistance = Double.MAX_VALUE;
            for (DetectedObject player : redPlayers) {
                double distance = calculateDistance(ball.boundingRect, player.boundingRect);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPlayer = player;
                }
            }
            System.out.println(minDistance);
            for (DetectedObject player : bluePlayers) {
                double distance = calculateDistance(ball.boundingRect, player.boundingRect);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPlayer = player;
                }
            }

            if (closestPlayer != null) {
                System.out.println("Possession : " + closestPlayer.type + " joueur.");
                // annotateImage(filePath, objects, closestPlayer);
            }

            detectHj(redPlayers, bluePlayers, ball, filePath,closestPlayer);
        }
    }

    private static double calculateDistance(org.opencv.core.Rect rect1, org.opencv.core.Rect rect2) {
        double x1 = rect1.x + rect1.width / 2.0;
        double y1 = rect1.y + rect1.height / 2.0;
        double x2 = rect2.x + rect2.width / 2.0;
        double y2 = rect2.y + rect2.height / 2.0;
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // private static void annotateImage(String filePath, List<DetectedObject> objects, DetectedObject closestPlayer) {
    //     Mat image = Imgcodecs.imread(filePath);
    //     for (DetectedObject obj : objects) {
    //         Scalar color = obj.type.equals("rouge") ? new Scalar(0, 0, 255) : obj.type.equals("bleu") ? new Scalar(255, 0, 0) : new Scalar(0, 255, 0);
    //         // Imgproc.rectangle(image, obj.boundingRect.tl(), obj.boundingRect.br(), color, 2);

    //         if (obj == closestPlayer) {
    //             Imgproc.putText(image, "Possesseur", obj.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, color);
    //         }
    //     }
    //     Imgcodecs.imwrite("annotated_result.jpg", image);
    // }

    private static void detectHj(List<DetectedObject> redPlayers, List<DetectedObject> bluePlayers, DetectedObject ball, String filePath, DetectedObject closestPlayer)
    {
        Mat image = Imgcodecs.imread(filePath);
        List<DetectedObject> attackingPlayers = null;
        List<DetectedObject> defendingPlayers = null;

        if (redPlayers.get(0).type == closestPlayer.type) {
            attackingPlayers = redPlayers;
            defendingPlayers = bluePlayers;
        }
        else {
            attackingPlayers = bluePlayers;
            defendingPlayers = redPlayers;
        }
        defendingPlayers.sort(Comparator.comparingInt(o -> o.boundingRect.y));
        attackingPlayers.sort(Comparator.comparingInt(o -> o.boundingRect.y));
        if (attackingPlayers.get(0).boundingRect.y < defendingPlayers.get(0).boundingRect.y && attackingPlayers.get(attackingPlayers.size()-1).boundingRect.y < defendingPlayers.get(defendingPlayers.size()-1).boundingRect.y && attackingPlayers.get(0).type == closestPlayer.type) {
            defendingPlayers.sort((o1, o2) -> Integer.compare(o2.boundingRect.y, o1.boundingRect.y));
            DetectedObject lastDefender = defendingPlayers.size() > 1 ? defendingPlayers.get(1) : defendingPlayers.get(0);
            // Imgproc.putTeyt(image, "lastDefender", lastDefender.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEy, 1, new Scalar(255, 0, 0));
            for (DetectedObject attacker : attackingPlayers) {
                defendingPlayers.sort((o1, o2) -> Integer.compare(o2.boundingRect.y, o1.boundingRect.y));
                lastDefender = defendingPlayers.size() > 1 ? defendingPlayers.get(1) : defendingPlayers.get(0);
    
                if (attacker.boundingRect.y+attacker.boundingRect.height > lastDefender.boundingRect.y+lastDefender.boundingRect.height && attacker.boundingRect.y > ball.boundingRect.y) {

                    Imgproc.putText(image, "HJ", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
                if (attacker.boundingRect.y+attacker.boundingRect.height < lastDefender.boundingRect.y+lastDefender.boundingRect.height && attacker.boundingRect.y > ball.boundingRect.y && attacker!=closestPlayer) {
                    Imgproc.putText(image, "M", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
                defendingPlayers.sort(Comparator.comparingInt(o -> o.boundingRect.x));
                //hors jeux droite
                lastDefender = defendingPlayers.get(0);
                if (attacker.boundingRect.x < lastDefender.boundingRect.x && attacker.boundingRect.y > ball.boundingRect.y) {
                    int x = lastDefender.boundingRect.x;
                    int yStart = 0;
                    int yEnd = image.rows();
                    Imgproc.line(image, new Point(x, yStart), new Point(x, yEnd),  new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image, "HJL", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
                defendingPlayers.sort((o1, o2) -> Integer.compare(o2.boundingRect.y, o1.boundingRect.x));
                lastDefender = defendingPlayers.get(0);
                //hors jeux gauche
                if (attacker.boundingRect.x + attacker.boundingRect.width > lastDefender.boundingRect.x+lastDefender.boundingRect.width && attacker.boundingRect.y > ball.boundingRect.y) {
                    int yStart = 0;
                    int yEnd = image.rows();
                    int x = lastDefender.boundingRect.x+lastDefender.boundingRect.width;
                    Imgproc.line(image, new Point(x, yStart), new Point(x, yEnd),  new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image, "HJL", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
            }
        }
        else
        {
            DetectedObject lastDefender = defendingPlayers.size() > 1 ? defendingPlayers.get(1) : defendingPlayers.get(0);
            System.out.println("Dernier défenseur : " + lastDefender.boundingRect);
            for (DetectedObject attacker : attackingPlayers) {
                defendingPlayers.sort(Comparator.comparingInt(o -> o.boundingRect.x));
                lastDefender = defendingPlayers.get(1);
                if (attacker.boundingRect.y < lastDefender.boundingRect.y && attacker.boundingRect.y < ball.boundingRect.y) {
                    int y = lastDefender.boundingRect.y;
                    int xStart = 0;
                    int xEnd = image.cols();
                    Imgproc.line(image, new Point(xStart, y), new Point(xEnd, y),  new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image, "HJ", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
                if (attacker.boundingRect.y > lastDefender.boundingRect.y && attacker.boundingRect.y < ball.boundingRect.y && attacker!=closestPlayer) {
                    Imgproc.putText(image, "M", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 0),4);
                }
                defendingPlayers.sort(Comparator.comparingInt(o -> o.boundingRect.x));
                lastDefender = defendingPlayers.get(0);
                if (attacker.boundingRect.x < lastDefender.boundingRect.x && attacker.boundingRect.y < ball.boundingRect.y) {
                    int x = lastDefender.boundingRect.x;
                    int yStart = 0;
                    int yEnd = image.rows();
                    Imgproc.line(image, new Point(x, yStart), new Point(x, yEnd),  new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image, "HJL", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 3, new Scalar(0, 0, 0),4);
                }
                defendingPlayers.sort((o1, o2) -> Integer.compare(o2.boundingRect.y, o1.boundingRect.x));
                lastDefender = defendingPlayers.get(0);
                if (attacker.boundingRect.x + attacker.boundingRect.width > lastDefender.boundingRect.x+lastDefender.boundingRect.width && attacker.boundingRect.y < ball.boundingRect.y) {
                    int yStart = 0;
                    int yEnd = image.rows();
                    int x = lastDefender.boundingRect.x+lastDefender.boundingRect.width;
                    Imgproc.line(image, new Point(x, yStart), new Point(x, yEnd),  new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image, "HJg", attacker.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 3, new Scalar(0, 0, 0),4);
                }
            }
        }
        // Imgproc.putText(image, "Possesseur", closestPlayer.boundingRect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0),4);
        Imgcodecs.imwrite("resultat.jpg", image);
        System.out.println("Image avec détection de hors-jeu sauvegardée : annotated_offside_result.jpg");
    }

}
