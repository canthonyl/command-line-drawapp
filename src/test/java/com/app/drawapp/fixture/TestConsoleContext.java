package com.app.drawapp.fixture;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.ConsoleContext;
import com.app.drawapp.console.mac.MacConsoleOutput;
import com.app.drawapp.console.window.WindowConsoleOutput;
import com.sun.jna.Platform;

public class TestConsoleContext extends ConsoleContext {

    public TestConsoleContext(DrawContext context) {
        super(context);
    }

    public TestConsoleOutput createConsoleOutput(Integer width, Integer height){
        TestConsoleOutput consoleOutput = new TestConsoleOutput(width, height, drawContext);
        consoleOutput.setConsoleOutput(Platform.isWindows() ? new WindowConsoleOutput(width, height, drawContext) : new MacConsoleOutput(width, height, drawContext));
        return consoleOutput;
    }
}
