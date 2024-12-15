import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class ImageLoaderApp extends JFrame {
    private BufferedImage loadedImage; // Obrazek do wyświetlenia
    private DrawPanel drawPanel;      // Panel do rysowania

    public ImageLoaderApp() {
        // Ustawienia okna
        setTitle("Image Loader");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tworzenie panelu głównego
        drawPanel = new DrawPanel();
        add(drawPanel);

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

    // Panel do wyświetlania obrazka
    private static class DrawPanel extends JPanel {
        private BufferedImage image;

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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageLoaderApp::new);
    }
}
