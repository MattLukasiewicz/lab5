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

        // Tworzenie panelu skrótów klawiaturowych
        JPanel shortcutsPanel = createShortcutsPanel();
        add(shortcutsPanel, BorderLayout.EAST);

        // Dodanie menu podręcznego (JPopupMenu)
        JPopupMenu popupMenu = createPopupMenu();
        drawPanel.setComponentPopupMenu(popupMenu);

        // Dodanie obsługi klawiatury
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    openFileChooser();
                } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    // Tworzenie menu podręcznego
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Opcja "Wczytaj obraz"
        JMenuItem loadMenuItem = new JMenuItem("Wczytaj obraz (W)");
        loadMenuItem.addActionListener(e -> openFileChooser());
        popupMenu.add(loadMenuItem);

        // Opcja "Wyjście"
        JMenuItem exitMenuItem = new JMenuItem("Wyjście (Q)");
        exitMenuItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitMenuItem);

        return popupMenu;
    }

    // Tworzenie panelu informacji o pozycji i kolorze
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
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

    // Tworzenie panelu skrótów klawiaturowych
    private JPanel createShortcutsPanel() {
        JPanel shortcutsPanel = new JPanel();
        shortcutsPanel.setLayout(new BoxLayout(shortcutsPanel, BoxLayout.Y_AXIS));
        shortcutsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Skróty klawiaturowe",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        // Dodanie opisów skrótów
        JLabel shortcut1 = new JLabel("W - Wczytaj obraz");
        JLabel shortcut2 = new JLabel("Q - Wyjście");

        shortcut1.setAlignmentX(Component.LEFT_ALIGNMENT);
        shortcut2.setAlignmentX(Component.LEFT_ALIGNMENT);

        shortcutsPanel.add(shortcut1);
        shortcutsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Odstęp
        shortcutsPanel.add(shortcut2);

        return shortcutsPanel;
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

    // Panel do wyświetlania obrazka
    private class DrawPanel extends JPanel {
        private BufferedImage image;

        public DrawPanel() {
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
                int offsetX = (getWidth() - image.getWidth()) / 2;
                int offsetY = (getHeight() - image.getHeight()) / 2;

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
