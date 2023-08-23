package com.app.drawapp;

import com.app.drawapp.console.ConsoleContext;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.ConsoleColour;
import com.app.drawapp.render.GroupScanner;

import java.io.PrintStream;
import java.util.Optional;

public class DrawContext {

    public Boolean displayBelowCanvas;
    public Boolean done;

    public ConsoleContext consoleContext;
    public Optional<Canvas> canvas;

    public PrintStream printStream;
    public GroupScanner groupScanner;

    public DrawContext(PrintStream ps) {
        canvas = Optional.empty();
        done = Boolean.FALSE;
        printStream = ps;
        groupScanner = new GroupScanner();
        displayBelowCanvas = false;
        consoleContext = new ConsoleContext(this);
    }

    public Boolean hasCanvas(){ return canvas.isPresent(); }

    public Optional<Canvas> getCanvas() {
        return canvas;
    }

    public void print(String output){
        printStream.print(output);
    }

    public void print(String color, String output){
        printStream.print(ConsoleColour.foregroundColors.get(color));
        printStream.print(output);
        printStream.print(ConsoleColour.RESET);
    }

    public void println(String output){
        printStream.println(output);
    }

    public void setColor(String color){
        printStream.print(ConsoleColour.foregroundColors.get(color));
    }

    public void resetColor(){
        printStream.print(ConsoleColour.RESET);
    }

    public Boolean isDone() {
        return done;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    public GroupScanner getGroupScanner(){ return groupScanner; }

    public ConsoleContext getConsoleContext(){ return consoleContext; }

}
