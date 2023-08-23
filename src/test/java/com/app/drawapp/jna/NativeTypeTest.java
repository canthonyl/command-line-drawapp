package com.app.drawapp.jna;

import com.app.drawapp.console.jna.NativeC;
import com.app.drawapp.console.window.Kernel32Native;
import com.app.drawapp.render.CellValue;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.app.drawapp.console.window.Kernel32Native.Kernel32.CharInfo;

public class NativeTypeTest {

    @Test
    public void testNativeUnsignedShort(){
        byte byteVal=(byte)0xFF;
        char charVal=0xFFFF;
        short shortVal=(short)0xFFFF;

        NativeC.NativeUnsignedShort storeByte = new NativeC.NativeUnsignedShort(byteVal);
        short valStoreByte = storeByte.getValue();
        assertEquals((short)0x00FF, valStoreByte);

        NativeC.NativeUnsignedShort storeChar = new NativeC.NativeUnsignedShort(charVal);
        short valStoreChar = storeChar.getValue();
        assertEquals((short)-1, valStoreChar);

        NativeC.NativeUnsignedShort storeShort = new NativeC.NativeUnsignedShort(shortVal);
        short valStoreShort = storeShort.getValue();
        assertEquals((short)-1, valStoreShort);
    }


    @Test
    public void testReadWriteToSameMemoryLocation_WindowsType(){
        CharInfo[] charInfos = CharInfo.array(16);
        Pointer startAddress = charInfos[0].getPointer();

        CellValue[][] integers = CellValue.array(8, 2, 4, startAddress);

        CellValue i0 = integers[0][0];
        Long val = ((-1L^(-1L<<8))&'A')|(1L<<16)|(1L<<17)|(1L<<23);
        i0.setValue(val);
        i0.write();

        CharInfo charInfo0 = charInfos[0];
        charInfo0.read();

        assertEquals('A', (char)charInfo0.Char.getValue());
        assertEquals((short)((1L<<0)|(1L<<1)|(1L<<7)), charInfo0.Attributes.getValue());
    }


    @Test
    public void testBackground(){
        CharInfo charInfo = new CharInfo();
        charInfo.Attributes.setValue((short)(Kernel32Native.BACKGROUND_GREEN|Kernel32Native.FOREGROUND_RED));

        Short attribute = charInfo.Attributes.getValue();
        Short filteredAttribute = (short)(((-1^(-1<<4))<<4) & attribute);
        assertEquals(Kernel32Native.BACKGROUND_GREEN, filteredAttribute);
    }

    @Test
    public void testForeground(){
        CharInfo charInfo = new CharInfo();
        charInfo.Attributes.setValue((short)(Kernel32Native.BACKGROUND_GREEN|Kernel32Native.FOREGROUND_RED));

        Short attribute = charInfo.Attributes.getValue();
        Short filteredAttribute = (short)((-1>>>-4) & attribute);
        assertEquals(Kernel32Native.FOREGROUND_RED, filteredAttribute);
    }

    @Test
    public void testCellValueByteArray(){
        Pointer p = new Pointer(Native.malloc(4));
        CellValue cellValue = new CellValue(4, p);
        assertEquals(4, cellValue.size());
    }

    public String format(Long val){
        String binaryString = String.format("%64s", Long.toBinaryString(Long.reverse(val))).replace(' ', '0');
        StringBuilder sb = new StringBuilder();
        for (Integer i=0; i<8; i++){
            if (sb.length()>0){
                sb.append("\n");
            }
            sb.append(binaryString, i*8, i*8+8);
        }
        return sb.toString();
    }

}
