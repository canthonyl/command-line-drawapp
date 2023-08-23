package com.app.drawapp.console.window;

import com.app.drawapp.console.jna.NativeC;
import com.sun.jna.*;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.lang.reflect.Array;
import java.util.*;


public class Kernel32Native {

    public static final int STD_INPUT_HANDLE = -10;
    public static final int STD_OUTPUT_HANDLE = -11;
    public static final int STD_ERR_HANDLE = -12;

    public static final Integer GENERIC_READ = 0x80000000;
    public static final Integer GENERIC_WRITE = 0x40000000;
    public static final Integer GENERIC_READ_WRITE = GENERIC_READ | GENERIC_WRITE;

    public static final Integer FILE_SHARE_READ = 1;
    public static final Integer FILE_SHARE_WRITE = 2;
    public static final Integer FILE_SHARE_READ_WRITE = FILE_SHARE_READ | FILE_SHARE_WRITE;


    public static final Short FOREGROUND_BLUE =       0x0001;
    public static final Short FOREGROUND_GREEN =      0x0002;
    public static final Short FOREGROUND_RED =        0x0004;
    public static final Short FOREGROUND_INTENSITY =  0x0008;
    public static final Short BACKGROUND_BLUE =       0x0010;
    public static final Short BACKGROUND_GREEN =      0x0020;
    public static final Short BACKGROUND_RED =        0x0040;
    public static final Short BACKGROUND_INTENSITY =  0x0080;

    public static final Pointer DEFAULT_SECURITY_ATTRIBUTE = null;
    public static final Pointer RESERVED_SCREEN_BUFFER_DATA = null;

    public static final Integer CONSOLE_TEXTMODE_BUFFER = 1;

    static {
        Kernel32.Coord.origin.write();
    }

    public static void clearScreen(){
        Pointer hConsoleOutput = Kernel32.INSTANCE.GetStdHandle(STD_OUTPUT_HANDLE);
        Kernel32.ConsoleScreenBufferInfo screenBufferInfo = new Kernel32.ConsoleScreenBufferInfo(); screenBufferInfo.write();
        Kernel32.INSTANCE.GetConsoleScreenBufferInfo(hConsoleOutput, screenBufferInfo.getPointer());
        screenBufferInfo.read();

        Kernel32.SmallRect lpWriteRegion = new Kernel32.SmallRect((int)screenBufferInfo.srWindow.Left, (int)screenBufferInfo.srWindow.Top, (int)screenBufferInfo.srWindow.Right, (int)screenBufferInfo.srWindow.Bottom); lpWriteRegion.write();
        Kernel32.Coord charInfoDimension = new Kernel32.Coord(screenBufferInfo.dwScreenSize.X, screenBufferInfo.dwScreenSize.Y); charInfoDimension.write();
        Kernel32.CharInfo[] charInfos = Kernel32.CharInfo.array(charInfoDimension.X * charInfoDimension.Y);

        Short attributes = screenBufferInfo.attributes.getValue();
        for (Integer i=0; i<charInfos.length; i++){
            Kernel32.CharInfo charInfo = charInfos[i];
            charInfo.Char.setValue((byte)' ');
            charInfo.Attributes.setValue(attributes); charInfos[i].write();
        }

        Kernel32.INSTANCE.WriteConsoleOutput(hConsoleOutput, charInfos, charInfoDimension, Kernel32.Coord.origin, lpWriteRegion.getPointer());

        Kernel32.Coord cursorPosition = new Kernel32.Coord((short)0, screenBufferInfo.srWindow.Top); cursorPosition.write();
        Kernel32.INSTANCE.SetConsoleCursorPosition(hConsoleOutput, cursorPosition);
    }

    public static interface Kernel32 extends StdCallLibrary {

        WindowsConsoleTypeMapper optionTypeMapper = new WindowsConsoleTypeMapper();
        String libraryName = "kernel32";
        Kernel32 INSTANCE = Native.load(libraryName, Kernel32.class,
                Collections.unmodifiableMap(new HashMap<String, Object>(W32APIOptions.DEFAULT_OPTIONS) {{
                    put(OPTION_TYPE_MAPPER, optionTypeMapper);
                    if (Platform.isWindows()) { put(OPTION_CALLING_CONVENTION, Function.ALT_CONVENTION);
                    } else { put(OPTION_CALLING_CONVENTION, Function.C_CONVENTION); }
                }}));


        Pointer CreateConsoleScreenBuffer(int dwDesiredAccess, int dwShareMode, Pointer lpSecurityAttributes, int dwFlags, Pointer lpScreenBufferData) throws LastErrorException;

        Pointer CloseHandle(Pointer hConsoleOutput) throws LastErrorException;

        Pointer GetStdHandle(int handleType) throws LastErrorException;

        boolean GetConsoleScreenBufferInfo(Pointer hConsoleOutput, Pointer lpWriteRegion) throws LastErrorException;

        boolean ScrollConsoleScreenBuffer(Pointer hConsoleOutput, Pointer scrollRectangle, Pointer clipRectangle, Coord rectUpperLeft, Pointer charInfo) throws LastErrorException;

        boolean SetConsoleCursorPosition(Pointer hConsoleOutput, Coord dwCursorPosition) throws LastErrorException;

        boolean SetConsoleActiveScreenBuffer(Pointer hConsoleOutput) throws LastErrorException;

        boolean SetStdHandle(Integer nStdHandle, Pointer hConsoleOutput) throws LastErrorException;

        boolean WriteConsoleOutput(Pointer hConsoleOutput, CharInfo[] lpBuffer, Coord dwBufferSize, Coord dwBufferCoord, Pointer lpWriteRegion) throws LastErrorException;

        public static class CharInfo extends Structure implements Structure.ByValue {

            public CharInfo(){
                super(ALIGN_DEFAULT, Kernel32Native.Kernel32.optionTypeMapper);
                Char  = new Kernel32.CompositeChar();
                Attributes = new NativeC.NativeUnsignedShort();
            }

            public CharInfo(Pointer pointer){
                super(pointer, ALIGN_DEFAULT, optionTypeMapper);
                Char  = new Kernel32.CompositeChar();
                Attributes = new NativeC.NativeUnsignedShort();
                write();
            }

            public Kernel32.CompositeChar Char;
            public NativeC.NativeUnsignedShort Attributes;

            public static final Integer calculatedStructureSize = NativeC.getInstance().calculateStructureSize(CharInfo.class);

            public static CharInfo[] array(Integer num) {
                try {
                    Memory addressPointer = new Memory(num * calculatedStructureSize);
                    CharInfo[] array = (CharInfo[]) Array.newInstance(CharInfo.class, num);
                    for (Integer i = 0; i < num; i++) {
                        CharInfo charInfo = new CharInfo(addressPointer.share(i * calculatedStructureSize, calculatedStructureSize));
                        array[i] = charInfo;
                    }
                    return array;
                } catch (Throwable t){
                    t.printStackTrace();
                    throw new RuntimeException(t);
                }
            }

            @Override
            protected List<String> getFieldOrder(){
                return Arrays.asList("Char", "Attributes");
            }
        }

        public static class Coord extends Structure implements Structure.ByValue{
            public Coord(){}
            public Coord(Integer xVal, Integer yVal){ X = xVal.shortValue(); Y = yVal.shortValue(); }
            public Coord(Short xVal, Short yVal){ X = xVal; Y = yVal; }

            public short X;
            public short Y;

            public static Coord origin = new Coord(0,0);
            @Override
            protected List<String> getFieldOrder(){
                return Arrays.asList("X", "Y");
            }
        }

        public static class CompositeChar extends Structure implements Structure.ByValue{

            public CompositeChar() { charVal = new NativeC.NativeUnsignedShort(); }
            public CompositeChar(char val) { this(); charVal.setValue((short)val);}

            public void setValue(byte val){ charVal.setValue(val); }
            public void setValue(char val){ charVal.setValue(val); }

            public short getValue() { return charVal.getValue(); }

            public NativeC.NativeUnsignedShort charVal;

            @Override
            protected List<String> getFieldOrder(){
                return Arrays.asList("charVal");
            }

        }

        public static class SmallRect extends Structure implements Structure.ByValue {

            public SmallRect() {}

            public SmallRect(Integer l, Integer t, Integer r, Integer b){
                Left = l.shortValue(); Top = t.shortValue(); Right = r.shortValue();Bottom = b.shortValue();
            }

            public short Left;
            public short Top;
            public short Right;
            public short Bottom;

            static final Integer calculatedStructureSize = NativeC.getInstance().calculateStructureSize(SmallRect.class);

            @Override
            protected List<String> getFieldOrder(){
                return Arrays.asList("Left","Top","Right","Bottom");
            }
        }

        /*public static class NativeUnsignedShort  {
            private short nativeVal;

            public NativeUnsignedShort() { }
            public NativeUnsignedShort(byte val) { setValue(val); }
            public NativeUnsignedShort(char val) { setValue(val); }
            public NativeUnsignedShort(short val) { setValue(val); }

            public void setValue(byte val){ nativeVal = (short)(val & 0x00FF); }
            public void setValue(char val){ nativeVal = (short)(val); }
            public void setValue(short val){ nativeVal = val; }

            public short getValue() { return nativeVal; }
        }*/


        public static class ConsoleScreenBufferInfo extends Structure implements Structure.ByValue {
            public ConsoleScreenBufferInfo(){
                dwMaxScreenSize = new Coord();
                dwCursorCoord = new Coord();
                attributes = new NativeC.NativeUnsignedShort();
                srWindow = new SmallRect();
                dwScreenSize = new Coord();
            }

            public Coord dwMaxScreenSize;
            public Coord dwCursorCoord;
            public NativeC.NativeUnsignedShort attributes;
            public SmallRect srWindow;
            public Coord dwScreenSize;

            static final Integer calculatedStructureSize = NativeC.getInstance().calculateStructureSize(ConsoleScreenBufferInfo.class);

            @Override
            public List<String> getFieldOrder(){
                return Arrays.asList("dwMaxScreenSize", "dwCursorCoord", "attributes", "srWindow", "dwScreenSize");
            }
        }


    }
}
