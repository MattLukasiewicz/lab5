import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class ImageLoaderApp extends JFrame {
    private BufferedImage loadedImage;
    private DrawPanel drawPanel;
    private JLabel positionLabel;
    private JLabel colorLabel;
    private JLabel modeLabel; // Komunikat o trybie kadrowania
    private Zaznaczenie currentZaznaczenie;
    private boolean cropMode = false; // Flaga trybu kadrowania

    public ImageLoaderApp() {
        setTitle("Image Loader");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.SOUTH);

        JPanel shortcutsPanel = createShortcutsPanel();
        add(shortcutsPanel, BorderLayout.EAST);

        JPopupMenu popupMenu = createPopupMenu();
        drawPanel.setComponentPopupMenu(popupMenu);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> openFileChooser();
                    case KeyEvent.VK_Q -> confirmAndExit();
                    case KeyEvent.VK_Z -> saveSelection();
                    case KeyEvent.VK_C -> disableCropMode();
                    case KeyEvent.VK_K -> enableCropMode();
                }
            }
        });

        setVisible(true);
    }

    private void enableCropMode() {
        if (loadedImage != null) {
            cropMode = true;
            modeLabel.setText("Tryb kadrowania włączony");
        } else {
            JOptionPane.showMessageDialog(this, "Brak załadowanego obrazu! Nie można włączyć trybu kadrowania.", "Błąd", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void disableCropMode() {
        cropMode = false;
        currentZaznaczenie = null;
        modeLabel.setText("Tryb kadrowania wyłączony");
        drawPanel.repaint();
    }

    private void confirmAndExit() {
        if (currentZaznaczenie != null) {
            int result = JOptionPane.showConfirmDialog(this, "Czy zapisać zaznaczenie przed wyjściem?", "Wyjście",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveSelection();
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        System.exit(0);
    }

    private void saveSelection() {
        if (currentZaznaczenie != null && loadedImage != null) {
            try {
                int x = currentZaznaczenie.getX();
                int y = currentZaznaczenie.getY();
                int width = currentZaznaczenie.getWidth();
                int height = currentZaznaczenie.getHeight();

                if (x < 0 || y < 0 || x + width > loadedImage.getWidth() || y + height > loadedImage.getHeight()) {
                    JOptionPane.showMessageDialog(this, "Zaznaczenie wykracza poza obszar obrazu! Nie można zapisać.", "Błąd zapisu", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BufferedImage croppedImage = loadedImage.getSubimage(x, y, width, height);
                File outputFile = new File("zaznaczenie.png");
                ImageIO.write(croppedImage, "png", outputFile);
                JOptionPane.showMessageDialog(this, "Zaznaczenie zapisane jako zaznaczenie.png");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd podczas zapisywania zaznaczenia", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nie ma zaznaczenia do zapisania!", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Informacje o kursore i kolorze",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        positionLabel = new JLabel("Pozycja: (x: -, y: -)");
        colorLabel = new JLabel("Kolor: RGB(-, -, -)");
        modeLabel = new JLabel("Tryb kadrowania wyłączony");

        infoPanel.add(positionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(colorLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(modeLabel);

        return infoPanel;
    }

    private JPanel createShortcutsPanel() {
        JPanel shortcutsPanel = new JPanel();
        shortcutsPanel.setLayout(new BoxLayout(shortcutsPanel, BoxLayout.Y_AXIS));
        shortcutsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Skróty klawiaturowe",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        shortcutsPanel.add(new JLabel("W - Wczytaj obraz"));
        shortcutsPanel.add(new JLabel("K - Włącz tryb kadrowania"));
        shortcutsPanel.add(new JLabel("C - Wyłącz tryb kadrowania"));
        shortcutsPanel.add(new JLabel("Z - Zapisz zaznaczenie"));
        shortcutsPanel.add(new JLabel("Q - Wyjście"));

        return shortcutsPanel;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem loadMenuItem = new JMenuItem("Wczytaj obraz (W)");
        loadMenuItem.addActionListener(e -> openFileChooser());
        popupMenu.add(loadMenuItem);

        JMenuItem cropModeMenuItem = new JMenuItem("Włącz tryb kadrowania (K)");
        cropModeMenuItem.addActionListener(e -> enableCropMode());
        popupMenu.add(cropModeMenuItem);

        JMenuItem clearSelectionMenuItem = new JMenuItem("Wyłącz tryb kadrowania (C)");
        clearSelectionMenuItem.addActionListener(e -> disableCropMode());
        popupMenu.add(clearSelectionMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Wyjście (Q)");
        exitMenuItem.addActionListener(e -> confirmAndExit());
        popupMenu.add(exitMenuItem);

        return popupMenu;
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz obrazek");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadedImage = ImageIO.read(selectedFile);
                drawPanel.setImage(loadedImage);
                disableCropMode(); // Reset trybu kadrowania po załadowaniu nowego obrazu
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nie można wczytać pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DrawPanel extends JPanel {
        private BufferedImage image;
        private Point startPoint;
        private int offsetX, offsetY;

        public DrawPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!cropMode) {
                        return;
                    }

                    if (startPoint == null) {
                        if (!isPointInsideImage(e.getPoint())) {
                            JOptionPane.showMessageDialog(
                                    DrawPanel.this,
                                    "Nie można rozpocząć zaznaczenia poza obszarem obrazu!",
                                    "Błąd zaznaczenia",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }
                        startPoint = e.getPoint();
                    } else {
                        if (!isPointInsideImage(e.getPoint())) {
                            JOptionPane.showMessageDialog(
                                    DrawPanel.this,
                                    "Nie można zakończyć zaznaczenia poza obszarem obrazu!",
                                    "Błąd zaznaczenia",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            startPoint = null;
                            return;
                        }

                        int x1 = startPoint.x - offsetX;
                        int y1 = startPoint.y - offsetY;
                        int x2 = e.getX() - offsetX;
                        int y2 = e.getY() - offsetY;

                        int imageX1 = Math.max(0, x1);
                        int imageY1 = Math.max(0, y1);
                        int imageX2 = Math.max(0, x2);
                        int imageY2 = Math.max(0, y2);

                        currentZaznaczenie = new Zaznaczenie(
                                Math.min(imageX1, imageX2),
                                Math.min(imageY1, imageY2),
                                Math.abs(imageX2 - imageX1),
                                Math.abs(imageY2 - imageY1)
                        );

                        startPoint = null;
                        repaint();
                    }
                }
            });
        }

        private boolean isPointInsideImage(Point point) {
            if (image == null) return false;

            int x = point.x;
            int y = point.y;

            return x >= offsetX && x < offsetX + image.getWidth() &&
                    y >= offsetY && y < offsetY + image.getHeight();
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                offsetX = (getWidth() - image.getWidth()) / 2;
                offsetY = (getHeight() - image.getHeight()) / 2;
                g.drawImage(image, offsetX, offsetY, this);

                if (currentZaznaczenie != null) {
                    g.setColor(Color.RED);
                    g.drawRect(
                            currentZaznaczenie.getX() + offsetX,
                            currentZaznaczenie.getY() + offsetY,
                            currentZaznaczenie.getWidth(),
                            currentZaznaczenie.getHeight()
                    );
                }
            }
        }
    }


    private class Zaznaczenie {
        private int x, y, width, height;

        public Zaznaczenie(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageLoaderApp::new);
    }
}
