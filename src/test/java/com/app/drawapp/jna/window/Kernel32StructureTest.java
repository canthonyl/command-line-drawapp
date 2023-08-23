package com.app.drawapp.jna.window;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.app.drawapp.console.window.Kernel32Native.Kernel32.CharInfo;
import static com.app.drawapp.console.window.Kernel32Native.Kernel32.Coord;

public class Kernel32StructureTest {

    @Test
    public void charInfoArrayTest(){

        CharInfo[] charInfoArray = CharInfo.array(10);
        Coord arraySize = new Coord(10, 1);

        charInfoArray[0].Char.setValue((byte)'A');  charInfoArray[0].write();
        charInfoArray[1].Char.setValue((byte)'B');  charInfoArray[1].write();
        charInfoArray[2].Char.setValue((byte)'C');  charInfoArray[2].write();
        charInfoArray[3].Char.setValue((byte)'D');  charInfoArray[3].write();
        charInfoArray[4].Char.setValue((byte)'E');  charInfoArray[4].write();
        charInfoArray[5].Char.setValue((byte)'F');  charInfoArray[5].write();
        charInfoArray[6].Char.setValue((byte)'G');  charInfoArray[6].write();
        charInfoArray[7].Char.setValue((byte)'H');  charInfoArray[7].write();
        charInfoArray[8].Char.setValue((byte)'I');  charInfoArray[8].write();
        charInfoArray[9].Char.setValue((byte)'J');  charInfoArray[9].write();

        Kernel32Test.INSTANCE.testReadWriteCharInfoArray(charInfoArray, arraySize);

        charInfoArray[0].read(); assertEquals((byte)'a', (byte)charInfoArray[0].Char.getValue());
        charInfoArray[1].read(); assertEquals((byte)'b', (byte)charInfoArray[1].Char.getValue());
        charInfoArray[2].read(); assertEquals((byte)'c', (byte)charInfoArray[2].Char.getValue());
        charInfoArray[3].read(); assertEquals((byte)'d', (byte)charInfoArray[3].Char.getValue());
        charInfoArray[4].read(); assertEquals((byte)'e', (byte)charInfoArray[4].Char.getValue());
        charInfoArray[5].read(); assertEquals((byte)'f', (byte)charInfoArray[5].Char.getValue());
        charInfoArray[6].read(); assertEquals((byte)'g', (byte)charInfoArray[6].Char.getValue());
        charInfoArray[7].read(); assertEquals((byte)'h', (byte)charInfoArray[7].Char.getValue());
        charInfoArray[8].read(); assertEquals((byte)'i', (byte)charInfoArray[8].Char.getValue());
        charInfoArray[9].read(); assertEquals((byte)'j', (byte)charInfoArray[9].Char.getValue());


    }

}
