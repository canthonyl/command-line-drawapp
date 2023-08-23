package com.app.drawapp.jna;

import com.app.drawapp.console.window.Kernel32Native;
import com.app.drawapp.console.jna.NativeC;
import com.sun.jna.Structure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NativeCTest {

    @Test
    public void calculateSize_maxAlignmentFieldOrderedFirst(){
        assertEquals(8, NativeC.getInstance().calculateStructureSize(TestStructA.class));
    }

    @Test
    public void calculateSize_maxAlignmentFieldOrderedSecond(){
        assertEquals(12, NativeC.getInstance().calculateStructureSize(TestStructB.class));
    }

    @Test
    public void calculateSize_fieldInIncreasingAlignmentOrder(){
        assertEquals(16, NativeC.getInstance().calculateStructureSize(TestStructC.class));
    }

    @Test
    public void calculateSize_fieldInSameAlignment(){
        assertEquals(10, NativeC.getInstance().calculateStructureSize(TestStructA10.class));
    }

    @Test
    public void calculateSize_nestedFields(){
        assertEquals(40, NativeC.getInstance().calculateStructureSize(TestStructureNested.class));
    }

    @Test
    public void calculateSize_nestedFields2(){
        assertEquals(40, NativeC.getInstance().calculateStructureSize(TestStructureNested2.class));
    }


    @Test
    public void calculateNestedStructureTest(){
        Class<? extends Structure> type = Kernel32Native.Kernel32.ConsoleScreenBufferInfo.class;
        Integer calculatedSize = NativeC.getInstance().calculateStructureSize(type);
        assertEquals(22, calculatedSize);
    }

    @Test
    public void calculateNestedStructureTest2(){
        Class<? extends Structure> type = Kernel32Native.Kernel32.CharInfo.class;
        Integer calculatedSize = NativeC.getInstance().calculateStructureSize(type);
        assertEquals(4, calculatedSize);
    }

    @Test
    public void calculateNestedStructureTest3(){
        Class<? extends Structure> type = Kernel32Native.Kernel32.SmallRect.class;
        Integer calculatedSize = NativeC.getInstance().calculateStructureSize(type);
        assertEquals(8, calculatedSize);
    }


    class TestStructA extends Structure {
        public int a;
        public byte b;
        public byte c;
        public byte d;
    }

    class TestStructB extends Structure {
        public byte a;
        public int b;
        public byte c;
    }

    class TestStructC extends Structure {
        public byte a;
        public int b;
        public long c;
    }

    class TestStructureNested extends Structure {
        public TestStructA a;
        public TestStructB b;
        public TestStructC c;
    }

    class TestStructureNested2 extends Structure {
        public TestStructA10 a;
        public TestStructB b;
        public TestStructC c;
    }

    class TestStructA10 extends Structure {
        public short a;
        public short b;
        public short c;
        public short d;
        public short e;
    }


}
