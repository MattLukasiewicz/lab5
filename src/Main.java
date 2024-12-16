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
    private JLabel modeLabel;
    private boolean cropMode = false;
    private boolean lineCropMode = false; // Flaga trybu kadrowania liniami
    private Rectangle currentSelection; // Prostokąt do kadrowania

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
                    case KeyEvent.VK_C -> disableCropMode();
                    case KeyEvent.VK_K -> enableCropMode();
                    case KeyEvent.VK_L -> enableLineCropMode();
                    case KeyEvent.VK_Z -> saveSelection();
                }
            }
        });

        setVisible(true);
    }

    private void enableCropMode() {
        if (loadedImage != null) {
            cropMode = true;
            lineCropMode = false;
            modeLabel.setText("Tryb kadrowania (zaznaczenie prostokątem)");
        } else {
            showNoImageError();
        }
    }

    private void enableLineCropMode() {
        if (loadedImage != null) {
            lineCropMode = true;
            cropMode = false;
            modeLabel.setText("Tryb kadrowania (cztery linie)");
            drawPanel.initializeLines();
        } else {
            showNoImageError();
        }
    }

    private void disableCropMode() {
        cropMode = false;
        lineCropMode = false;
        modeLabel.setText("Tryb kadrowania wyłączony");
        drawPanel.clearSelection();
        currentSelection = null;
    }

    private void confirmAndExit() {
        int result = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz wyjść?", "Wyjście",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
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
                disableCropMode();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nie można wczytać pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSelection() {
        if (lineCropMode) {
            currentSelection = drawPanel.getLineSelection();
        }

        if (currentSelection != null && loadedImage != null) {
            try {
                int x = currentSelection.x - drawPanel.offsetX;
                int y = currentSelection.y - drawPanel.offsetY;
                int width = currentSelection.width;
                int height = currentSelection.height;

                if (x < 0 || y < 0 || x + width > loadedImage.getWidth() || y + height > loadedImage.getHeight()) {
                    JOptionPane.showMessageDialog(this, "Zaznaczenie wykracza poza obszar obrazu!", "Błąd", JOptionPane.ERROR_MESSAGE);
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

    private void showNoImageError() {
        JOptionPane.showMessageDialog(this, "Brak załadowanego obrazu!", "Błąd", JOptionPane.WARNING_MESSAGE);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Informacje o kursore i trybie",
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
        shortcutsPanel.add(new JLabel("K - Tryb kadrowania (prostokąt)"));
        shortcutsPanel.add(new JLabel("L - Tryb kadrowania (linie)"));
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

        JMenuItem cropModeMenuItem = new JMenuItem("Tryb kadrowania (prostokąt) (K)");
        cropModeMenuItem.addActionListener(e -> enableCropMode());
        popupMenu.add(cropModeMenuItem);

        JMenuItem lineCropModeMenuItem = new JMenuItem("Tryb kadrowania (linie) (L)");
        lineCropModeMenuItem.addActionListener(e -> enableLineCropMode());
        popupMenu.add(lineCropModeMenuItem);

        JMenuItem disableCropMenuItem = new JMenuItem("Wyłącz tryb kadrowania (C)");
        disableCropMenuItem.addActionListener(e -> disableCropMode());
        popupMenu.add(disableCropMenuItem);

        JMenuItem saveSelectionMenuItem = new JMenuItem("Zapisz zaznaczenie (Z)");
        saveSelectionMenuItem.addActionListener(e -> saveSelection());
        popupMenu.add(saveSelectionMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Wyjście (Q)");
        exitMenuItem.addActionListener(e -> confirmAndExit());
        popupMenu.add(exitMenuItem);

        return popupMenu;
    }

    private class DrawPanel extends JPanel {
        private BufferedImage image;
        private int offsetX, offsetY;
        private Point startPoint, currentPoint;
        private Rectangle horizontalLine1, horizontalLine2, verticalLine1, verticalLine2;
        private boolean draggingLine;
        private Rectangle draggedLine;

        public DrawPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (image == null) return; // Nie wykonuj niczego, jeśli obrazek nie został załadowany.

                    Point clickPoint = e.getPoint();
                    boolean isInsideImage = clickPoint.x >= offsetX && clickPoint.x < offsetX + image.getWidth()
                            && clickPoint.y >= offsetY && clickPoint.y < offsetY + image.getHeight();

                    if (cropMode) {
                        if (isInsideImage) {
                            startPoint = clickPoint;
                            currentSelection = null;
                        } else {
                            startPoint = null; // Ignoruj kliknięcia poza obrazem.
                        }
                    } else if (lineCropMode) {
                        draggingLine = false;
                        for (Rectangle line : new Rectangle[]{horizontalLine1, horizontalLine2, verticalLine1, verticalLine2}) {
                            if (line != null && line.contains(clickPoint)) {
                                draggingLine = true;
                                draggedLine = line;
                                break;
                            }
                        }
                    }
                }


                @Override
                public void mouseReleased(MouseEvent e) {
                    draggingLine = false;
                    startPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (cropMode && startPoint != null) {
                        currentPoint = e.getPoint();

                        // Ograniczenie prostokąta do obszaru obrazu
                        int x1 = Math.max(offsetX, Math.min(startPoint.x, currentPoint.x));
                        int y1 = Math.max(offsetY, Math.min(startPoint.y, currentPoint.y));
                        int x2 = Math.min(offsetX + image.getWidth(), Math.max(startPoint.x, currentPoint.x));
                        int y2 = Math.min(offsetY + image.getHeight(), Math.max(startPoint.y, currentPoint.y));

                        currentSelection = new Rectangle(x1, y1, x2 - x1, y2 - y1);
                        repaint();
                    } else if (lineCropMode && draggingLine) {
                        int dx = e.getX() - draggedLine.x;
                        int dy = e.getY() - draggedLine.y;

                        if (draggedLine == horizontalLine1) {
                            horizontalLine1.y = Math.max(offsetY, Math.min(horizontalLine2.y - 10, horizontalLine1.y + dy));
                        } else if (draggedLine == horizontalLine2) {
                            horizontalLine2.y = Math.max(horizontalLine1.y + 10, Math.min(offsetY + image.getHeight(), horizontalLine2.y + dy));
                        } else if (draggedLine == verticalLine1) {
                            verticalLine1.x = Math.max(offsetX, Math.min(verticalLine2.x - 10, verticalLine1.x + dx));
                        } else if (draggedLine == verticalLine2) {
                            verticalLine2.x = Math.max(verticalLine1.x + 10, Math.min(offsetX + image.getWidth(), verticalLine2.x + dx));
                        }

                        repaint();
                    }
                }
            });
        }

        public void initializeLines() {
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                horizontalLine1 = new Rectangle(offsetX, offsetY + height / 4, width, 5);
                horizontalLine2 = new Rectangle(offsetX, offsetY + 3 * height / 4, width, 5);
                verticalLine1 = new Rectangle(offsetX + width / 4, offsetY, 5, height);
                verticalLine2 = new Rectangle(offsetX + 3 * width / 4, offsetY, 5, height);
                repaint();
            }
        }

        public Rectangle getLineSelection() {
            if (horizontalLine1 != null && horizontalLine2 != null && verticalLine1 != null && verticalLine2 != null) {
                int x = verticalLine1.x;
                int y = horizontalLine1.y;
                int width = verticalLine2.x - verticalLine1.x;
                int height = horizontalLine2.y - horizontalLine1.y;
                return new Rectangle(x, y, width, height);
            }
            return null;
        }

        public void clearSelection() {
            currentSelection = null;
            horizontalLine1 = null;
            horizontalLine2 = null;
            verticalLine1 = null;
            verticalLine2 = null;
            repaint();
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

                if (cropMode && currentSelection != null) {
                    g.setColor(Color.RED);
                    g.drawRect(currentSelection.x, currentSelection.y, currentSelection.width, currentSelection.height);
                }

                if (lineCropMode) {
                    g.setColor(Color.RED);
                    if (horizontalLine1 != null) {
                        g.fillRect(horizontalLine1.x, horizontalLine1.y, horizontalLine1.width, horizontalLine1.height);
                    }
                    if (horizontalLine2 != null) {
                        g.fillRect(horizontalLine2.x, horizontalLine2.y, horizontalLine2.width, horizontalLine2.height);
                    }
                    if (verticalLine1 != null) {
                        g.fillRect(verticalLine1.x, verticalLine1.y, verticalLine1.width, verticalLine1.height);
                    }
                    if (verticalLine2 != null) {
                        g.fillRect(verticalLine2.x, verticalLine2.y, verticalLine2.width, verticalLine2.height);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageLoaderApp::new);
    }
} 