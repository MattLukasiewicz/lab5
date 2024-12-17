import java.awt.*;

class Zaznaczenie {
    //Przechowuje informacje o zaznaczonym obszarze
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