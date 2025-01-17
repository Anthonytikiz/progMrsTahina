package jeu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Fenetre extends JFrame {
    private JTextField filePathField;
    private JLabel imageLabel;
    static {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java481.dll");
    }

    public Fenetre() {
        setTitle("Hhhhhooooorrrrrsssss JJJJJJeeeeeuuuuuuuu!!!!!!!!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 9 00);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.lightGray);

        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("Button.background", Color.red);
            UIManager.put("Button.foreground", Color.red);
            UIManager.put("Panel.background", Color.red);
            UIManager.put("TextField.background", Color.white);
            UIManager.put("TextField.foreground", Color.black);

        } catch (Exception e) {
            e.printStackTrace();
        }

        filePathField = new JTextField();
        filePathField.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(filePathField);
        add(scrollPane, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton browseButton = new JButton("Entrer votre image");
        JButton submitButton = new JButton("Regarder l'Hors Jeu");
        buttonPanel.add(browseButton);
        buttonPanel.add(submitButton);
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        add(buttonPanel, BorderLayout.SOUTH);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        add(imageScrollPane, BorderLayout.CENTER);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(Fenetre.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();
                if (!filePath.isEmpty()) {
                    System.out.println("Fichier sélectionné : " + filePath);
                    Mat image = Imgcodecs.imread(filePath);
                    if(image.rows() > image.cols()){
                        Mat rotatedImage = new Mat();
                        Core.transpose(image, rotatedImage);
                        Core.flip(rotatedImage, rotatedImage, 1);
                        Imgcodecs.imwrite("rotate.jpg", image);
                        filePath="rotate.jpg";
                    }

                    try {
                        List<DetectedObject> objects = new ArrayList<>();
                        objects.addAll(ColorDetection.detectColor(filePath, "rouge", new Scalar(0, 50, 50), new Scalar(10, 255, 255)));
                        // objects.addAll(ColorDetection.detectColor(filePath, "rouge", new Scalar(170, 50, 50), new Scalar(180, 255, 255)));
                        objects.addAll(ColorDetection.detectColor(filePath, "bleu", new Scalar(100, 150, 0), new Scalar(140, 255, 255)));
                        objects.addAll(ColorDetection.detectColor(filePath, "ballon", new Scalar(0, 0, 0), new Scalar(180, 255, 50)));

                        ColorDetection.analyzeGame(objects, filePath);

                        File resultImage = new File("./resultat.jpg");
                        if (resultImage.exists()) {
                            ImageIcon icon = resizeImage(resultImage.getAbsolutePath(), 900, 600); 
                            imageLabel.setIcon(icon);
                            revalidate();
                            repaint();
                        } else {
                            JOptionPane.showMessageDialog(Fenetre.this, "L'image annotée n'a pas été générée.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(Fenetre.this, "Erreur lors de l'analyse de l'image.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(Fenetre.this, "Aucun fichier sélectionné.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
    private ImageIcon resizeImage(String imagePath, int maxWidth, int maxHeight) {
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image image = imageIcon.getImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        // Calculer les nouvelles dimensions pour maintenir le ratio
        if (width > maxWidth || height > maxHeight) {
            float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);
        }

        // Redimensionner l'image
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }


}
