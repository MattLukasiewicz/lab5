import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class ImageLoaderApp extends JFrame {
    private BufferedImage loadedImage; // Obrazek do wyświetlenia
    private DrawPanel drawPanel;      // Panel do rysowania
    private JLabel positionLabel;    // Etykieta pozycji kursora
    private JLabel colorLabel;       // Etykieta koloru punktu

    public ImageLoaderApp() {
        // Ustawienia okna
        setTitle("Image Loader");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tworzenie panelu głównego
        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        // Tworzenie panelu informacji (pozycja i kolor)
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.SOUTH);

        // Dodanie obsługi klawiatury (klawisz W)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    openFileChooser();
                }
            }
        });

        // Dodanie menu podręcznego (JPopupMenu)
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem loadMenuItem = new JMenuItem("Wczytaj");
        loadMenuItem.addActionListener(e -> openFileChooser());
        popupMenu.add(loadMenuItem);

        // Powiązanie menu podręcznego z panelem
        drawPanel.setComponentPopupMenu(popupMenu);

        setVisible(true);
    }

    // Metoda do otwierania JFileChooser i wczytywania obrazka
    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz obrazek");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadedImage = ImageIO.read(selectedFile);
                drawPanel.setImage(loadedImage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nie można wczytać pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Tworzenie panelu informacji o pozycji i kolorze
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Układ w jednym rzędzie
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Informacje o kursore i kolorze",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        // Etykieta pozycji
        positionLabel = new JLabel("Pozycja: (x: -, y: -)");
        positionLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Etykieta koloru
        colorLabel = new JLabel("Kolor: RGB(-, -, -)");
        colorLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Dodanie etykiet do panelu
        infoPanel.add(positionLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0))); // Mały odstęp
        infoPanel.add(colorLabel);

        return infoPanel;
    }

    // Panel do wyświetlania obrazka
    private class DrawPanel extends JPanel {
        private BufferedImage image;

        public DrawPanel() {
            // Obsługa ruchu myszy na panelu
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateMouseInfo(e);
                }
            });
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // Wyśrodkowanie obrazka na panelu
                int x = (getWidth() - image.getWidth()) / 2;
                int y = (getHeight() - image.getHeight()) / 2;
                g.drawImage(image, x, y, this);
            }
        }

        private void updateMouseInfo(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            positionLabel.setText(String.format("Pozycja: (x: %d, y: %d)", x, y));

            if (image != null) {
                // Wyśrodkowanie obrazka
                int offsetX = (getWidth() - image.getWidth()) / 2;
                int offsetY = (getHeight() - image.getHeight()) / 2;

                // Sprawdzenie, czy kursor znajduje się na obrazku
                int imageX = x - offsetX;
                int imageY = y - offsetY;

                if (imageX >= 0 && imageY >= 0 && imageX < image.getWidth() && imageY < image.getHeight()) {
                    int rgb = image.getRGB(imageX, imageY);
                    Color color = new Color(rgb);
                    colorLabel.setText(String.format("Kolor: RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
                } else {
                    colorLabel.setText("Kolor: RGB(-, -, -)");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageLoaderApp::new);
    }
}
