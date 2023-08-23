package com.app.drawapp.console;

import com.app.drawapp.DrawContext;
import com.app.drawapp.render.Colour;
import com.app.drawapp.render.ProjectTarget;

import static com.app.drawapp.render.Colour.BLACK;

public abstract class ConsoleOutput {

    public final Integer width;
    public final Integer height;
    public final DrawContext drawContext;
    public final ConsoleContext consoleContext;

    public ConsoleOutput(Integer w, Integer h, DrawContext dc){
        this.drawContext = dc;
        this.consoleContext = dc.getConsoleContext();
        this.width = w;
        this.height = h;
    }

    public abstract void draw();

    public abstract ProjectTarget getCoordProjectTarget();

    public Colour getCurrentBackgroundColour() { return BLACK; }

}
