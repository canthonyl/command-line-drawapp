package com.app.drawapp.render;


public class Colour {

    private final Integer r;
    private final Integer g;
    private final Integer b;

    public Colour(Integer r, Integer g, Integer b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Short getRed() { return r.shortValue(); }
    public Short getGreen() { return g.shortValue(); }
    public Short getBlue() { return b.shortValue(); }

    public static final Colour BLACK = new Colour(12, 12, 12);
    public static final Colour RED = new Colour(197,15,31);
    public static final Colour GREEN = new Colour(19, 161, 14);
    public static final Colour YELLOW = new Colour(193, 156, 0);
    public static final Colour BLUE = new Colour(0, 55, 218);
    public static final Colour MAGENTA = new Colour(136, 23, 152);
    public static final Colour CYAN = new Colour(58, 150, 221);
    public static final Colour WHITE = new Colour(204, 204, 204);

}
