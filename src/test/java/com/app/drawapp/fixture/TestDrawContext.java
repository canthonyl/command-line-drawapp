package com.app.drawapp.fixture;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.window.Kernel32Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import java.io.PrintStream;

import static com.app.drawapp.console.window.Kernel32Native.CONSOLE_TEXTMODE_BUFFER;
import static com.app.drawapp.console.window.Kernel32Native.DEFAULT_SECURITY_ATTRIBUTE;
import static com.app.drawapp.console.window.Kernel32Native.FILE_SHARE_READ_WRITE;
import static com.app.drawapp.console.window.Kernel32Native.GENERIC_READ_WRITE;
import static com.app.drawapp.console.window.Kernel32Native.RESERVED_SCREEN_BUFFER_DATA;
import static com.app.drawapp.console.window.Kernel32Native.STD_OUTPUT_HANDLE;
import static com.app.drawapp.console.window.Kernel32Native.Kernel32;

public class TestDrawContext extends DrawContext {

    public TestDrawContext(PrintStream ps) {
        super(ps);
        consoleContext = new TestConsoleContext(this);

        if (Platform.isWindows()) {
            //System.out.println("TestDrawContext: create console screen buffer");
            Pointer hConsoleOutput = Kernel32Native.Kernel32.INSTANCE.CreateConsoleScreenBuffer(GENERIC_READ_WRITE, FILE_SHARE_READ_WRITE, DEFAULT_SECURITY_ATTRIBUTE, CONSOLE_TEXTMODE_BUFFER, RESERVED_SCREEN_BUFFER_DATA);

            //System.out.println("TestDrawContext: set active screen buffer");
            Kernel32.INSTANCE.SetConsoleActiveScreenBuffer(hConsoleOutput);

            //System.out.println("TestDrawContext: set output handle");
            Kernel32.INSTANCE.SetStdHandle(STD_OUTPUT_HANDLE, hConsoleOutput);

            //System.out.println("TestDrawContext: creating char info array");
            Kernel32.CharInfo[] charInfos = Kernel32.CharInfo.array(2);

            //System.out.println("TestDrawContext: creating coord");
            Kernel32.Coord dwScreenBufferSize = new Kernel32.Coord(2, 1); dwScreenBufferSize.write();

            //System.out.println("TestDrawContext: creating small rect");
            Kernel32.SmallRect drawRect = new Kernel32.SmallRect(0, 0, 1, 0); drawRect.write();

            for (Integer i=0; i<charInfos.length; i++){
                Kernel32.CharInfo charInfo = charInfos[i];
                charInfo.Char.setValue((byte)' ');
                charInfo.Attributes.setValue((short)100);
                charInfo.write();
            }

            //System.out.println("TestDrawContext: Write Console Output");
            Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, charInfos, dwScreenBufferSize, Kernel32.Coord.origin, drawRect.getPointer());
            //System.out.println("Written char infos to console output");

        }
    }

}
