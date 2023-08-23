package com.app.drawapp.render;

import com.app.drawapp.render.CellValue;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.app.drawapp.render.CoordSet.col;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CellValueTest {

    @Test
    public void testStoreInteger(){
        CellValue cellValue = new CellValue(4);
        Integer val = IntStream.range(0, 4).map(i -> ((-1>>>-2)<<2*i)<<8*i).reduce(0, (a,b) -> a|b);
        cellValue.setValue(val);
        assertEquals(true, Objects.equals(val, cellValue.getInteger()));
    }


    @Test
    public void testStoreLong(){
        CellValue cellValue = new CellValue(8);
        Long val = LongStream.range(0L, 8L).map(i -> 1L<<9*i).reduce(0L, (a, b) -> a|b);
        cellValue.setValue(val);
        assertEquals(true, Objects.equals(val, cellValue.getLong()));
    }

    @Test
    public void testStoreLong2(){
        CellValue cellValue = new CellValue(8);
        Long val = 0L;
        val |= col[1];
        val |= col[3];
        cellValue.setValue(val);
        assertEquals(true, Objects.equals(val, cellValue.getLong()));
    }

}
