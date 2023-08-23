package com.app.drawapp.console;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.mac.MacAttributeConverter;
import com.app.drawapp.console.mac.MacConsoleOutput;
import com.app.drawapp.console.window.Kernel32Native;
import com.app.drawapp.console.window.WindowAttributeConverter;
import com.app.drawapp.console.window.WindowConsoleOutput;
import com.app.drawapp.render.ConsoleColour;
import com.sun.jna.Platform;

public class ConsoleContext {

    public final DrawContext drawContext;
    public final AttributeConverter attributeConverter;

    public ConsoleContext(DrawContext context){
        drawContext = context;
        attributeConverter = createAttributeConverter();
    }

     private AttributeConverter createAttributeConverter() {
        if (Platform.isWindows()) {
            return new WindowAttributeConverter();
        } else {
            return new MacAttributeConverter();
        }
    }

    public ConsoleOutput createConsoleOutput(Integer width, Integer height){
        if (Platform.isWindows()) {
            return new WindowConsoleOutput(width, height, drawContext);
        } else {
            return new MacConsoleOutput(width, height, drawContext);
        }
    }

    public void clearConsole(){
        if (Platform.isMac() || Platform.isLinux()) {
            drawContext.print(ConsoleColour.CLEAR_TO_CURSOR);
            drawContext.print(ConsoleColour.MOVE_CURSOR);
        } else {
            Kernel32Native.clearScreen();
        }
    }

}
