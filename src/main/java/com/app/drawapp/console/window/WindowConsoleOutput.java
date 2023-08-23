package com.app.drawapp.console.window;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.ConsoleOutput;
import com.app.drawapp.render.CoordSet;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;
import com.app.drawapp.render.ProjectTarget;
import com.sun.jna.Pointer;

import java.util.Arrays;

public class WindowConsoleOutput extends ConsoleOutput {

    private final Kernel32Native.Kernel32.Coord canvasDimension;
    private final Kernel32Native.Kernel32.Coord hBorderDimension;
    private final Kernel32Native.Kernel32.Coord vBorderDimension;

    private final Kernel32Native.Kernel32.CharInfo[] canvas;
    private final Kernel32Native.Kernel32.CharInfo[] hBorder;
    private final Kernel32Native.Kernel32.CharInfo[] vBorder;

    private final Kernel32Native.Kernel32.CharInfo fillChar;
    private final Pointer hConsoleOutput;
    private final Kernel32Native.Kernel32.ConsoleScreenBufferInfo screenBufferInfo;

    public WindowConsoleOutput(Integer width, Integer height, DrawContext drawContext) {
        super(width, height, drawContext);
        canvasDimension = new Kernel32Native.Kernel32.Coord(width, height); canvasDimension.write();
        hBorderDimension = new Kernel32Native.Kernel32.Coord(canvasDimension.X + 2, 1); hBorderDimension.write();
        vBorderDimension = new Kernel32Native.Kernel32.Coord(1, (int) canvasDimension.Y); vBorderDimension.write();

        canvas = Kernel32Native.Kernel32.CharInfo.array(canvasDimension.X * canvasDimension.Y);
        hBorder = Kernel32Native.Kernel32.CharInfo.array((int) canvasDimension.X + 2);
        vBorder = Kernel32Native.Kernel32.CharInfo.array((int) canvasDimension.Y);

        Arrays.stream(hBorder).forEach(charInfo -> {charInfo.Char.setValue((byte)'='); charInfo.write(); } );
        Arrays.stream(vBorder).forEach(charInfo -> {charInfo.Char.setValue((byte)'|'); charInfo.write(); } );
        for (Integer i=0; i<canvas.length; i++){
            Kernel32Native.Kernel32.CharInfo charInfo = canvas[i];
            charInfo.Char.setValue((byte)' ');
            charInfo.write();
        }
        fillChar = new Kernel32Native.Kernel32.CharInfo();
        fillChar.Char.setValue((byte)' ');
        hConsoleOutput = Kernel32Native.Kernel32.INSTANCE.GetStdHandle(Kernel32Native.STD_OUTPUT_HANDLE);
        screenBufferInfo = new Kernel32Native.Kernel32.ConsoleScreenBufferInfo(); screenBufferInfo.write();
    }

    @Override
    public void draw() {
        Integer left = 0;
        Integer top = 1;
        Integer right = 2;
        Integer bottom = 3;
        Pointer hConsoleOutput = Kernel32Native.Kernel32.INSTANCE.GetStdHandle(Kernel32Native.STD_OUTPUT_HANDLE);
        Kernel32Native.Kernel32.INSTANCE.GetConsoleScreenBufferInfo(hConsoleOutput, screenBufferInfo.getPointer());
        screenBufferInfo.read();

        Integer scrollRectTop = (int) screenBufferInfo.srWindow.Top;
        Integer scrollRectBottom = (int) screenBufferInfo.dwCursorCoord.Y;
        Integer scrollRectLeft = 0;
        Integer scrollRectRight = (int) screenBufferInfo.dwScreenSize.X;

        Kernel32Native.Kernel32.SmallRect scrollRect = new Kernel32Native.Kernel32.SmallRect(scrollRectLeft, scrollRectTop, scrollRectRight, scrollRectBottom); scrollRect.write();
        Kernel32Native.Kernel32.SmallRect clipRect = new Kernel32Native.Kernel32.SmallRect(scrollRectLeft, scrollRectTop, scrollRectRight, scrollRectBottom); clipRect.write();
        Kernel32Native.Kernel32.Coord newCoord = new Kernel32Native.Kernel32.Coord(0, screenBufferInfo.srWindow.Top - (screenBufferInfo.dwCursorCoord.Y - screenBufferInfo.srWindow.Top) - 1);newCoord.write();
        fillChar.Attributes.setValue(screenBufferInfo.attributes.getValue()); fillChar.write();

        Kernel32Native.Kernel32.INSTANCE.ScrollConsoleScreenBuffer(hConsoleOutput, scrollRect.getPointer(), scrollRect.getPointer(), newCoord, fillChar.getPointer());

        for (Integer i = 0; i < hBorder.length; i++){
            Kernel32Native.Kernel32.CharInfo charInfo = hBorder[i];
            charInfo.Attributes.setValue(screenBufferInfo.attributes.getValue());
            charInfo.write();
        }

        for (Integer i = 0; i < vBorder.length; i++){
            Kernel32Native.Kernel32.CharInfo charInfo = vBorder[i];
            charInfo.Attributes.setValue(screenBufferInfo.attributes.getValue());
            charInfo.write();
        }

        for (Integer i = 0; i < canvas.length; i++) {
            Kernel32Native.Kernel32.CharInfo charInfo = canvas[i];
            charInfo.read();
        }

        Integer[] tBorder = {0, (int)screenBufferInfo.srWindow.Top, canvasDimension.X + 1, screenBufferInfo.srWindow.Top + hBorderDimension.Y - 1};
        Integer[] bBorder = {0, (int)screenBufferInfo.srWindow.Top, canvasDimension.X + 1, screenBufferInfo.srWindow.Top + hBorderDimension.Y - 1};
        Integer[] canvasRect = { (int) vBorderDimension.X, screenBufferInfo.srWindow.Top + hBorderDimension.Y, vBorderDimension.X + canvasDimension.X - 1, screenBufferInfo.srWindow.Top + hBorderDimension.Y + canvasDimension.Y - 1};
        Integer[] lBorder = {0, screenBufferInfo.srWindow.Top + 1, vBorderDimension.X - 1, screenBufferInfo.srWindow.Top + 1 + vBorderDimension.Y - 1};
        Integer[] rBorder = {0, screenBufferInfo.srWindow.Top + 1, vBorderDimension.X - 1, screenBufferInfo.srWindow.Top + 1 + vBorderDimension.Y - 1};

        bBorder[top] += hBorderDimension.Y + canvasDimension.Y ;
        bBorder[bottom] += hBorderDimension.Y + canvasDimension.Y ;
        rBorder[left] += vBorderDimension.X + canvasDimension.X ;
        rBorder[right] += vBorderDimension.X + canvasDimension.X ;

        Kernel32Native.Kernel32.SmallRect topBorderWriteRegion = new Kernel32Native.Kernel32.SmallRect(tBorder[left], tBorder[top], tBorder[right], tBorder[bottom]); topBorderWriteRegion.write();
        Kernel32Native.Kernel32.SmallRect bottomBorderWriteRegion = new Kernel32Native.Kernel32.SmallRect(bBorder[left], bBorder[top], bBorder[right], bBorder[bottom]); bottomBorderWriteRegion.write();
        Kernel32Native.Kernel32.SmallRect leftBorderWriteRegion = new Kernel32Native.Kernel32.SmallRect(lBorder[left], lBorder[top], lBorder[right], lBorder[bottom]); leftBorderWriteRegion.write();
        Kernel32Native.Kernel32.SmallRect rightBorderWriteRegion = new Kernel32Native.Kernel32.SmallRect(rBorder[left], rBorder[top], rBorder[right], rBorder[bottom]); rightBorderWriteRegion.write();

        Kernel32Native.Kernel32.SmallRect canvasWriteRegion = new Kernel32Native.Kernel32.SmallRect(canvasRect[left], canvasRect[top], canvasRect[right], canvasRect[bottom]); canvasWriteRegion.write();

        Kernel32Native.Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, hBorder, hBorderDimension, Kernel32Native.Kernel32.Coord.origin, topBorderWriteRegion.getPointer());
        Kernel32Native.Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, hBorder, hBorderDimension, Kernel32Native.Kernel32.Coord.origin, bottomBorderWriteRegion.getPointer());
        Kernel32Native.Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, vBorder, vBorderDimension, Kernel32Native.Kernel32.Coord.origin, leftBorderWriteRegion.getPointer());
        Kernel32Native.Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, vBorder, vBorderDimension, Kernel32Native.Kernel32.Coord.origin, rightBorderWriteRegion.getPointer());

        Kernel32Native.Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, canvas, canvasDimension, Kernel32Native.Kernel32.Coord.origin, canvasWriteRegion.getPointer());

        canvasWriteRegion.read();
        Kernel32Native.Kernel32.Coord dwCursorPosition = new Kernel32Native.Kernel32.Coord(0, bBorder[bottom]+1); dwCursorPosition.write();
        Kernel32Native.Kernel32.INSTANCE.SetConsoleCursorPosition(hConsoleOutput, dwCursorPosition);
    }

    @Override
    public ProjectTarget getCoordProjectTarget() {
        ProjectTarget<Canvas.AttributeGroup, Long> coordProjectTarget = new CoordSet<>(width, height, Kernel32Native.Kernel32.CharInfo.calculatedStructureSize, canvas[0].getPointer());
        coordProjectTarget.setConverter(consoleContext.attributeConverter::convert);
        return coordProjectTarget;
    }

    @Override
    public Colour getCurrentBackgroundColour() {
        Kernel32Native.Kernel32.ConsoleScreenBufferInfo screenBufferInfo = new Kernel32Native.Kernel32.ConsoleScreenBufferInfo(); screenBufferInfo.write();
        Kernel32Native.Kernel32.INSTANCE.GetConsoleScreenBufferInfo(hConsoleOutput, screenBufferInfo.getPointer());
        screenBufferInfo.read();
        return consoleContext.attributeConverter.nativeToBgColour(screenBufferInfo.attributes.getValue());
    }

}
