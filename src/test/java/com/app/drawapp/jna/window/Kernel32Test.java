package com.app.drawapp.jna.window;

import com.app.drawapp.console.window.Kernel32Native;
import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.W32APIOptions;

import java.util.Collections;
import java.util.HashMap;

public interface Kernel32Test extends Kernel32Native.Kernel32 {

    String libraryName = System.mapLibraryName("kernel32");
    Kernel32Test INSTANCE = Native.load(libraryName.replace(".dll","test"), Kernel32Test.class,
            Collections.unmodifiableMap(new HashMap<String, Object>(W32APIOptions.DEFAULT_OPTIONS) {{
                put(OPTION_TYPE_MAPPER, optionTypeMapper);
                if (Platform.isWindows()) {
                    put(OPTION_CALLING_CONVENTION, Function.ALT_CONVENTION);
                } else {
                    put(OPTION_CALLING_CONVENTION, Function.C_CONVENTION);
                }
            }}));

    boolean testReadWriteCharInfoArray(CharInfo[] charInfoArray, Coord arraySize);

}
