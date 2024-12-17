import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class Zaznaczenie {
    //Przechowuje informacje o zaznaczeniu prostokątnym.
    private int x;
    private int y;
    private int W;
    private int H;

    public Zaznaczenie(int x, int y, int W, int H) {
        this.x = x;
        this.y = y;
        this.W = W;
        this.H = H;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return W;
    }

    public int getHeight() {
        return H;
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, W, H);
    }
}

class ProgramKadrowanieObrazow extends JFrame {
    private BufferedImage loadedImage;
    private DrawPanel drawPanel;
    private JLabel etykietaPozycji;
    private JLabel etykietaKoloru;
    private JLabel modeLabel;
    private boolean cropMode = false;
    private boolean lineCropMode = false; // Flaga trybu kadrowania liniami
    private Zaznaczenie AktualneZaznaczenie; // Zaznaczenie do kadrowania

    public ProgramKadrowanieObrazow() {

        //Tworzenie
        setTitle("ProgramKadrowanieObrazow");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        JPanel lnformacyjnyPanel = StworzlnformacyjnyPanel();
        add(lnformacyjnyPanel, BorderLayout.SOUTH);

        JPanel PanelSkrotow = StworzPanelSkrotow();
        add(PanelSkrotow, BorderLayout.EAST);

        JPopupMenu popupMenu =  utworzMenuKontekstowe();
        drawPanel.setComponentPopupMenu(popupMenu);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> WybieranieObrazu(); // Wczytaj obraz
                    case KeyEvent.VK_Q -> PotwierdzWyjscie(); // Wyjście
                    case KeyEvent.VK_C -> WylaczTrybkadrowania(); // Wyłącz tryby kadrowania i usuń zaznaczenia
                    case KeyEvent.VK_K -> {
                        if (lineCropMode) { // Jeśli tryb liniowy jest aktywny, wyłącz go
                            WylaczTrybkadrowania();
                        }
                        WlaczTrybkadrowaniaProstokatem(); // Włącz tryb prostokątny
                    }
                    case KeyEvent.VK_L -> {
                        if (cropMode) { // Jeśli tryb prostokątny jest aktywny, wyłącz go
                            WylaczTrybkadrowania();
                        }
                        WlaczTrybkadrowaniaLiniami(); // Włącz tryb liniowy
                    }
                    case KeyEvent.VK_Z -> ZapiszZaznaczenie(); // Zapisz zaznaczenie
                }
            }
        });



        setVisible(true);
    }

    private void WlaczTrybkadrowaniaProstokatem() {
        //Włączenie trybu prostokątnego zaznaczenia
        if (loadedImage != null) {
            cropMode = true;
            lineCropMode = false;
            modeLabel.setText("Tryb kadrowania (zaznaczenie prostokątem)");
        } else {
            BladBrakuObrazu();
        }
    }

    private void WlaczTrybkadrowaniaLiniami() {
        //Włączenie trybu kadrowania liniowego
        if (loadedImage != null) {
            lineCropMode = true;
            cropMode = false;
            modeLabel.setText("Tryb kadrowania (Liniami)");
            drawPanel.InicjowanieLinii();
        } else {
            BladBrakuObrazu();
        }
    }

    private void WylaczTrybkadrowania() {
        //Wyłączenie trybów kadrowania
        cropMode = false;
        lineCropMode = false;
        modeLabel.setText("Tryb kadrowania wyłączony");
        drawPanel.WyczyscZaznaczenie();
        AktualneZaznaczenie = null;
    }

    private void PotwierdzWyjscie() {
        //Zamykanie programu z pytaniem o zapis zaznaczenia
        if (AktualneZaznaczenie != null) {
            int ZapiszZaznaczenie = JOptionPane.showConfirmDialog(
                    this,
                    "Masz zaznaczenie. Czy chcesz je zapisać przed wyjściem?",
                    "Zapisz zaznaczenie",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (ZapiszZaznaczenie == JOptionPane.YES_OPTION) {
                ZapiszZaznaczenie(); // Wywołanie metody zapisu zaznaczenia
            } else if (ZapiszZaznaczenie == JOptionPane.CANCEL_OPTION) {
                return; // Przerwij zamykanie programu
            }
        }

        // Potwierdzenie zamknięcia, jeśli nie było zaznaczenia lub użytkownik zdecydował
        int result = JOptionPane.showConfirmDialog(
                this,
                "Czy na pewno chcesz wyjść?",
                "Wyjście",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }


    private void WybieranieObrazu() {
        //Wczytywanie obrazu
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz obraz");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadedImage = ImageIO.read(selectedFile);
                drawPanel.setImage(loadedImage);
                WylaczTrybkadrowania();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nie można wczytac pliku", "Blad", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ZapiszZaznaczenie() {
        //Pobiera zaznaczenie prostokątne lub liniowe z panelu rysowania
        if (lineCropMode) {
            Rectangle lineSelection = drawPanel.getLineSelection();
            if (lineSelection != null) {
                AktualneZaznaczenie = new Zaznaczenie(
                        lineSelection.x, lineSelection.y,
                        lineSelection.width, lineSelection.height
                );
            }
        }

        if (AktualneZaznaczenie != null && loadedImage != null) {
            try {
                // Skalowanie zaznaczenia do oryginalnego rozmiaru obrazu
                Rectangle scaledSelection = AktualneZaznaczenie.toRectangle();
                Rectangle originalSelection = drawPanel.SkalowanieDoOrginalu(scaledSelection);

                int x = originalSelection.x;
                int y = originalSelection.y;
                int width = originalSelection.width;
                int height = originalSelection.height;

                if (x < 0 || y < 0 || x + width > loadedImage.getWidth() || y + height > loadedImage.getHeight()) {
                    JOptionPane.showMessageDialog(this, "Zaznaczenie wykracza poza obszar obrazu!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BufferedImage croppedImage = loadedImage.getSubimage(x, y, width, height);

                // Wybor gdzie zapisac
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Zapisz zaznaczenie jako...");
                fileChooser.setSelectedFile(new File("zaznaczenie.png")); // Domyślna nazwa pliku

                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    // Dodanie rozszerzenia .png, jeśli brak
                    if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
                    }

                    // Zapis obrazu
                    ImageIO.write(croppedImage, "png", fileToSave);
                    JOptionPane.showMessageDialog(this, "Zaznaczenie zapisane jako: " + fileToSave.getAbsolutePath());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd podczas zapisywania zaznaczenia", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nie ma zaznaczenia do zapisania!", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        }
    }



    private void BladBrakuObrazu() {
        JOptionPane.showMessageDialog(this, "Brak załadowanego obrazu!", "Błąd", JOptionPane.WARNING_MESSAGE);
    }

    private JPanel StworzlnformacyjnyPanel() {
        JPanel lnformacyjnyPanel = new JPanel();
        lnformacyjnyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lnformacyjnyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Informacje o kursorze i trybie",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        etykietaPozycji = new JLabel("Pozycja: (x: -, y: -)");
        etykietaKoloru = new JLabel("Kolor: RGB(-, -, -)");
        modeLabel = new JLabel("Tryb kadrowania wyłączony");

        lnformacyjnyPanel.add(etykietaPozycji);
        lnformacyjnyPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        lnformacyjnyPanel.add(etykietaKoloru);
        lnformacyjnyPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        lnformacyjnyPanel.add(modeLabel);

        return lnformacyjnyPanel;
    }

    private JPanel StworzPanelSkrotow() {
        JPanel PanelSkrotow = new JPanel();
        PanelSkrotow.setLayout(new BoxLayout(PanelSkrotow, BoxLayout.Y_AXIS));
        PanelSkrotow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Skróty klawiaturowe",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        PanelSkrotow.add(new JLabel("W - Wczytaj obraz"));
        PanelSkrotow.add(new JLabel("K - Tryb kadrowania (prostokąt)"));
        PanelSkrotow.add(new JLabel("L - Tryb kadrowania (linie)"));
        PanelSkrotow.add(new JLabel("C - Wyłącz tryb kadrowania"));
        PanelSkrotow.add(new JLabel("Z - Zapisz zaznaczenie"));
        PanelSkrotow.add(new JLabel("Q - Wyjście"));

        return PanelSkrotow;
    }

    private JPopupMenu utworzMenuKontekstowe() {
        JPopupMenu menuKontekstowe = new JPopupMenu();

        JMenuItem opcjaWczytajObraz = new JMenuItem("Wczytaj obraz (W)");
        opcjaWczytajObraz.addActionListener(e -> WybieranieObrazu());
        menuKontekstowe.add(opcjaWczytajObraz);

        JMenuItem opcjaKadrowanieProstokatem = new JMenuItem("Tryb kadrowania (prostokąt) (K)");
        opcjaKadrowanieProstokatem.addActionListener(e -> WlaczTrybkadrowaniaProstokatem());
        menuKontekstowe.add(opcjaKadrowanieProstokatem);

        JMenuItem opcjaKadrowanieLiniami = new JMenuItem("Tryb kadrowania (linie) (L)");
        opcjaKadrowanieLiniami.addActionListener(e -> WlaczTrybkadrowaniaLiniami());
        menuKontekstowe.add(opcjaKadrowanieLiniami);

        JMenuItem opcjaWylaczKadrowanie = new JMenuItem("Wyłącz tryb kadrowania (C)");
        opcjaWylaczKadrowanie.addActionListener(e -> WylaczTrybkadrowania());
        menuKontekstowe.add(opcjaWylaczKadrowanie);

        JMenuItem opcjaZapiszZaznaczenie = new JMenuItem("Zapisz zaznaczenie (Z)");
        opcjaZapiszZaznaczenie.addActionListener(e -> ZapiszZaznaczenie());
        menuKontekstowe.add(opcjaZapiszZaznaczenie);

        JMenuItem opcjaWyjscie = new JMenuItem("Wyjście (Q)");
        opcjaWyjscie.addActionListener(e -> PotwierdzWyjscie());
        menuKontekstowe.add(opcjaWyjscie);

        return menuKontekstowe;
    }


    private class DrawPanel extends JPanel {
        private BufferedImage image;
        private int przesuniecieX, przesuniecieY;
        private int skalowanieSzerokosci, skalowanieWysokosci;
        private Point punktStartu, aktualnyPunkt;
        private Rectangle liniaPozioma1, liniaPozioma2, liniaPionowa1, liniaPionowa2;
        private boolean przesuwanieLinii;
        private Rectangle przesuwanaLinia;
        private final int margines = 10; // Stały margineses wokół obrazu

        public DrawPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (image == null) return;

                    Point clickPoint = e.getPoint();
                    boolean isInsideImage = clickPoint.x >= przesuniecieX && clickPoint.x < przesuniecieX + skalowanieSzerokosci
                            && clickPoint.y >= przesuniecieY && clickPoint.y < przesuniecieY + skalowanieWysokosci;

                    if (cropMode) {
                        if (isInsideImage) {
                            punktStartu = clickPoint;
                            AktualneZaznaczenie = null;
                        } else {
                            punktStartu = null;
                        }
                    } else if (lineCropMode) {
                        przesuwanieLinii = false;
                        for (Rectangle line : new Rectangle[]{liniaPozioma1, liniaPozioma2, liniaPionowa1, liniaPionowa2}) {
                            if (line != null && line.contains(clickPoint)) {
                                przesuwanieLinii = true;
                                przesuwanaLinia = line;
                                break;
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    przesuwanieLinii = false;
                    punktStartu = null;
                }
            });
            // W klasie DrawPanel (dodajemy MouseMotionListener do śledzenia pozycji i koloru)
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    // Śledzenie pozycji kursora
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    etykietaPozycji.setText(String.format("Pozycja: (x: %d, y: %d)", mouseX, mouseY));

                    // Wyświetlanie koloru, tylko jeśli obraz jest załadowany i kursor jest na obrazie
                    if (image != null && mouseX >= przesuniecieX && mouseX < przesuniecieX + skalowanieSzerokosci
                            && mouseY >= przesuniecieY && mouseY < przesuniecieY + skalowanieWysokosci) {
                        int imageX = (mouseX - przesuniecieX) * image.getWidth() / skalowanieSzerokosci;
                        int imageY = (mouseY - przesuniecieY) * image.getHeight() / skalowanieWysokosci;
                        int rgb = image.getRGB(imageX, imageY);
                        Color color = new Color(rgb);

                        etykietaKoloru.setText(String.format("Kolor: RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
                    } else {
                        etykietaKoloru.setText("Kolor: RGB(-, -, -)");
                    }
                }
            });


            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (cropMode && punktStartu != null) {
                        aktualnyPunkt = e.getPoint();

                        int x1 = Math.max(przesuniecieX, Math.min(punktStartu.x, aktualnyPunkt.x));
                        int y1 = Math.max(przesuniecieY, Math.min(punktStartu.y, aktualnyPunkt.y));
                        int x2 = Math.min(przesuniecieX + skalowanieSzerokosci, Math.max(punktStartu.x, aktualnyPunkt.x));
                        int y2 = Math.min(przesuniecieY + skalowanieWysokosci, Math.max(punktStartu.y, aktualnyPunkt.y));

                        AktualneZaznaczenie = new Zaznaczenie(x1, y1, x2 - x1, y2 - y1);
                        repaint();
                    } else if (lineCropMode && przesuwanieLinii) {
                        int dx = e.getX() - przesuwanaLinia.x;
                        int dy = e.getY() - przesuwanaLinia.y;

                        if (przesuwanaLinia == liniaPozioma1) {
                            liniaPozioma1.y = Math.max(przesuniecieY, Math.min(liniaPozioma2.y - 10, liniaPozioma1.y + dy));
                        } else if (przesuwanaLinia == liniaPozioma2) {
                            liniaPozioma2.y = Math.max(liniaPozioma1.y + 10, Math.min(przesuniecieY + skalowanieWysokosci, liniaPozioma2.y + dy));
                        } else if (przesuwanaLinia == liniaPionowa1) {
                            liniaPionowa1.x = Math.max(przesuniecieX, Math.min(liniaPionowa2.x - 10, liniaPionowa1.x + dx));
                        } else if (przesuwanaLinia == liniaPionowa2) {
                            liniaPionowa2.x = Math.max(liniaPionowa1.x + 10, Math.min(przesuniecieX + skalowanieSzerokosci, liniaPionowa2.x + dx));
                        }

                        repaint();
                    }
                }
            });
        }

        public void InicjowanieLinii() {
            if (image != null) {
                liniaPozioma1 = new Rectangle(przesuniecieX, przesuniecieY + skalowanieWysokosci / 4, skalowanieSzerokosci, 5);
                liniaPozioma2 = new Rectangle(przesuniecieX, przesuniecieY + 3 * skalowanieWysokosci / 4, skalowanieSzerokosci, 5);
                liniaPionowa1 = new Rectangle(przesuniecieX + skalowanieSzerokosci / 4, przesuniecieY, 5, skalowanieWysokosci);
                liniaPionowa2 = new Rectangle(przesuniecieX + 3 * skalowanieSzerokosci / 4, przesuniecieY, 5, skalowanieWysokosci);
                repaint();
            }
        }
        private Rectangle SkalowanieDoOrginalu(Rectangle scaledRect) {
            double scaleX = (double) image.getWidth() / skalowanieSzerokosci;
            double scaleY = (double) image.getHeight() / skalowanieWysokosci;

            int originalX = (int) ((scaledRect.x - przesuniecieX) * scaleX);
            int originalY = (int) ((scaledRect.y - przesuniecieY) * scaleY);
            int originalWidth = (int) (scaledRect.width * scaleX);
            int originalHeight = (int) (scaledRect.height * scaleY);

            return new Rectangle(originalX, originalY, originalWidth, originalHeight);
        }

        public Rectangle getLineSelection() {
            if (liniaPozioma1 != null && liniaPozioma2 != null && liniaPionowa1 != null && liniaPionowa2 != null) {
                int x = liniaPionowa1.x;
                int y = liniaPozioma1.y;
                int width = liniaPionowa2.x - liniaPionowa1.x;
                int height = liniaPozioma2.y - liniaPozioma1.y;
                return new Rectangle(x, y, width, height);
            }
            return null;
        }

        public void WyczyscZaznaczenie() {
            AktualneZaznaczenie = null;
            liniaPozioma1 = null;
            liniaPozioma2 = null;
            liniaPionowa1 = null;
            liniaPionowa2 = null;
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
                // Obliczanie przesunięcia i skalowania obrazu
                double panelWidth = getWidth() - 2 * margines;
                double panelHeight = getHeight() - 2 * margines;
                double imageAspect = (double) image.getWidth() / image.getHeight();
                double panelAspect = panelWidth / panelHeight;

                if (imageAspect > panelAspect) {
                    skalowanieSzerokosci = (int) panelWidth;
                    skalowanieWysokosci = (int) (panelWidth / imageAspect);
                } else {
                    skalowanieWysokosci = (int) panelHeight;
                    skalowanieSzerokosci = (int) (panelHeight * imageAspect);
                }

                przesuniecieX = (getWidth() - skalowanieSzerokosci) / 2;
                przesuniecieY = (getHeight() - skalowanieWysokosci) / 2;

                // Rysowanie obrazu
                g.drawImage(image, przesuniecieX, przesuniecieY, skalowanieSzerokosci, skalowanieWysokosci, this);

                // Rysowanie prostokątnego zaznaczenia w trybie kadrowania
                if (cropMode && AktualneZaznaczenie != null) {
                    g.setColor(Color.RED);
                    Rectangle rect = AktualneZaznaczenie.toRectangle();
                    g.drawRect(rect.x, rect.y, rect.width, rect.height);
                }

                // Rysowanie linii w trybie kadrowania liniami
                if (lineCropMode) {
                    g.setColor(Color.RED);
                    if (liniaPozioma1 != null) {
                        g.fillRect(liniaPozioma1.x, liniaPozioma1.y, liniaPozioma1.width, liniaPozioma1.height);
                    }
                    if (liniaPozioma2 != null) {
                        g.fillRect(liniaPozioma2.x, liniaPozioma2.y, liniaPozioma2.width, liniaPozioma2.height);
                    }
                    if (liniaPionowa1 != null) {
                        g.fillRect(liniaPionowa1.x, liniaPionowa1.y, liniaPionowa1.width, liniaPionowa1.height);
                    }
                    if (liniaPionowa2 != null) {
                        g.fillRect(liniaPionowa2.x, liniaPionowa2.y, liniaPionowa2.width, liniaPionowa2.height);
                    }
                }
            }
        }

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProgramKadrowanieObrazow::new);
    }
}
