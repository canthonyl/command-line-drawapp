package com.app.drawapp.render;

import com.app.drawapp.DrawContext;
import com.app.drawapp.fixture.Point;
import com.app.drawapp.fixture.TestDrawContext;
import com.app.drawapp.fixture.TestPrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CanvasTest {

    private Canvas canvas;
    private TestPrintStream testPrintStream;
    private TestDrawContext context;

    @BeforeEach
    public void setup(){
        testPrintStream = new TestPrintStream(new ByteArrayOutputStream());
        context = new TestDrawContext(testPrintStream);
    }

    @Test
    public void canvasStartsWithBackgroundConnectedGroup(){
        canvas = new Canvas(10, 10, context);
        CoordSet background = canvas.getConnectedGroup(0L);
        forAllCordsIn(Point.of(0,0), Point.of(9, 9), background, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        assertCoordInUniqueSet(10, 10, canvas.getAllConnectedGroup());
    }

    @Test
    public void newConnectedGroupCreatedWhenLinesDrawn(){
        canvas = new Canvas(5, 5, context);
        canvas.addLine(2,0,2,4);
        CoordSet originalGroup = canvas.getConnectedGroup(2L);
        CoordSet lineGroup = canvas.getConnectedGroup(1L);
        CoordSet newGroup = canvas.getConnectedGroup(0L);
        forAllCordsIn(Point.of(0,0), Point.of(1, 4), originalGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(2,0), Point.of(2, 4), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(3,0), Point.of(4, 4), newGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        assertCoordInUniqueSet(5, 5, canvas.getAllConnectedGroup());
        canvas.drawToConsole();
    }

    @Test
    public void newConnectedGroupCreatedWhenLinesDrawn2(){
        canvas = new Canvas(5, 5, context);
        canvas.addLine(0,2,4,2);

        CoordSet topGroup = canvas.getConnectedGroup(2L);
        CoordSet lineGroup = canvas.getConnectedGroup(1L);
        CoordSet bottomGroup = canvas.getConnectedGroup(0L);

        forAllCordsIn(Point.of(0,0), Point.of(4, 1), topGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,2), Point.of(4, 2), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,3), Point.of(4, 4), bottomGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        canvas.addLine(2,0,2,4);

        CoordSet topLeft = canvas.getConnectedGroup(4L);
        CoordSet bottomLeft = canvas.getConnectedGroup(3L);
        CoordSet topRight = canvas.getConnectedGroup(2L);
        CoordSet bottomRight = canvas.getConnectedGroup(0L);

        forAllCordsIn(Point.of(2,0), Point.of(2, 4), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,2), Point.of(4, 2), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        forAllCordsIn(Point.of(0,0), Point.of(1, 1), topLeft, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,3), Point.of(1, 4), bottomLeft, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(3,0), Point.of(4, 1), topRight, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(3,3), Point.of(4, 4), bottomRight, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        assertCoordInUniqueSet(5, 5, canvas.getAllConnectedGroup());
    }


   /* @Test
    public void newConnectedGroupCreatedWhenRectangleDrawn(){
        canvas = new Canvas(6, 6, context);
        canvas.addRectangle(1,1,4,4);

        CoordSet backgroundGroup = canvas.getConnectedGroup(2L);
        CoordSet lineGroup = canvas.getConnectedGroup(1L);
        CoordSet enclosedGroup = canvas.getConnectedGroup(0L);

        forAllCordsIn(Point.of(2,2), Point.of(3, 3), enclosedGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        forAllCordsIn(Point.of(1,1), Point.of(4, 1), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(4,1), Point.of(4, 4), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(1,4), Point.of(4, 4), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(1,1), Point.of(1, 4), lineGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        forAllCordsIn(Point.of(0,0), Point.of(5, 0), backgroundGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(5,0), Point.of(5, 5), backgroundGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,5), Point.of(5, 5), backgroundGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,0), Point.of(0, 5), backgroundGroup, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));

        assertCoordInUniqueSet(canvas);
    }*/

    @Test
    public void linesAddedToExistingConnectedLineGroup(){
        canvas = new Canvas(5, 5, context);
        canvas.addLine(2,0,2,4);
        canvas.addLine(0,2,4,2);
        CoordSet cross = canvas.getConnectedGroup(1L);
        forAllCordsIn(Point.of(2,0), Point.of(2, 4), cross, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,2), Point.of(4, 2), cross, (s, p) -> assertEquals(true, s.contains(p.x, p.y)));
    }

    @Test
    public void lineDrawnWithOnePixelToEdge() {
        canvas = new Canvas(4, 3, context);
        canvas.addLine(1,1,3,1);

        Map<Long, CoordSet> allSet = canvas.getAllConnectedGroup();
        //System.out.println("allSet size = "+allSet.size());
        CoordSet backgroundSet = allSet.get(0L);
        CoordSet lineSet = allSet.get(1L);
        assertEquals(2, allSet.size());

        forAllCordsIn(Point.of(1,1), Point.of(3,1), lineSet, (s,p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,0), Point.of(3,0), backgroundSet, (s,p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,0), Point.of(0,2), backgroundSet, (s,p) -> assertEquals(true, s.contains(p.x, p.y)));
        forAllCordsIn(Point.of(0,2), Point.of(3,2), backgroundSet, (s,p) -> assertEquals(true, s.contains(p.x, p.y)));

    }

    @Test
    public void drawPattern(){
        DrawContext dc = new DrawContext(System.out);
        Canvas canvas = new Canvas(10, 10, dc);

        canvas.addLine(0,1, 3,1);
        canvas.addLine(5,2, 9,2);
        canvas.addLine(0,3, 3,3);
        canvas.addLine(5,4, 9,4);
        canvas.addLine(0,5, 3,5);
        canvas.addLine(5,6, 9,6);
        canvas.addLine(0,7, 3,7);
        canvas.addLine(5,8, 9,8);

        canvas.bucketFill(0, 1, "M");
        canvas.bucketFill(5,2,"Y");
        canvas.bucketFill(0,5,"G");
        canvas.bucketFill(5,8, "C");
        canvas.addLine(4,0,4,9);
        canvas.bucketFill(4, 0, "Y");
        canvas.bucketFill(4, 0, "M");
        canvas.bucketFill(4, 0, "C");
        canvas.bucketFill(4, 0, "G");
        canvas.addLine(4,0,4,9);

        canvas.bucketFill(0, 1, "M");
        canvas.bucketFill(5,2,"Y");
        canvas.bucketFill(0,5,"B");
        canvas.bucketFill(5,8, "C");
        canvas.bucketFill(5,4,"R");

        canvas.addRectangle(0,0,9,9);
        canvas.bucketFill(5,1,"M");
        canvas.drawToConsole();

    }


    @Test
    public void drawMaze(){
        DrawContext dc = new DrawContext(System.out);
        Canvas canvas = new Canvas(32, 16, dc);

        canvas.addLine(5, 5, 8, 5);
        canvas.addLine(5, 6, 5, 8);
        canvas.addLine(7, 7, 11, 7);
        canvas.addLine(10, 2, 10, 7);
        canvas.addLine(6,3,9,3);
        canvas.addLine(8, 9, 12, 9);
        canvas.addLine(1,7,1,9);
        canvas.addLine(3,6,3,8);
        canvas.addLine(1,10,5,10);

        canvas.addLine(7,9,7,12);
        canvas.addLine(2,12,4,12);
        canvas.addLine(1,14,2,14);
        canvas.addLine(3,13,3,14);

        canvas.addLine(9,11,9,14);
        canvas.addLine(5,13,5,14);
        canvas.addLine(6,14,7,14);
        canvas.addLine(11,11,13,11);
        canvas.addLine(14,11,14,12);
        canvas.addLine(11,12,11,14);
        canvas.addLine(12,14,18,14);

        canvas.addLine(17,10,17,14);

        canvas.addLine(15,7,15, 9);
        canvas.addLine(16,7,17, 7);
        canvas.addLine(17,8,19,8);
        canvas.addLine(19,7,21,7);
        canvas.addLine(21,8,21,9);
        canvas.addLine(19, 10, 19, 12);
        canvas.addLine(20, 12, 23,12);
        canvas.addLine(23, 5, 23,11);

        canvas.addLine(17,5,22,5);
        canvas.addLine(13, 4, 13, 7);
        canvas.addLine(14, 3, 17, 3);

        canvas.addLine(15,4,15,5);
        canvas.addLine(20,1,20,4);
        canvas.addLine(22,2,22,3);
        canvas.addLine(24,1,24,3);
        canvas.addLine(26,3,27,3);
        canvas.addLine(25,5,27,5);
        canvas.addLine(26,1,29,1);
        canvas.addLine(29,2,29,7);

        canvas.addLine(27,7, 27,8);
        canvas.addLine(25,7, 25,10);

        canvas.addLine(21, 13, 21,14);
        canvas.addLine(29, 9, 29, 10);
        canvas.addLine(27, 10, 27, 11);
        canvas.addLine(26, 12, 27, 12);
        canvas.addLine(25,12, 25,13);
        canvas.addLine(23,14,29,14);
        canvas.addLine(29,12, 29,13);

        canvas.addLine(1, 1, 17, 1);
        canvas.addLine(1, 2, 1, 5);
        canvas.addLine(2,4,3,4);
        canvas.addLine(3,3,4,3);

        canvas.bucketFill(15, 7, "G");
        canvas.bucketFill(1, 1, "Y");
        canvas.bucketFill(20, 5, "M");
        canvas.bucketFill(7,9,"G");
        canvas.bucketFill(14, 14, "C");
        canvas.bucketFill(29, 14, "Y");
        canvas.bucketFill(29, 1, "G");
        canvas.bucketFill(1,7,"M");
        canvas.bucketFill(1, 14, "G");
        canvas.bucketFill(5,5,"C");
        canvas.bucketFill(9,13,"Y");
        canvas.bucketFill(15,3,"G");
        canvas.bucketFill(24, 3, "C");
        canvas.bucketFill(5, 14, "M");
        canvas.bucketFill(29, 9, "M");
        canvas.bucketFill(27,7,"C");
        canvas.bucketFill(25, 10, "G");
        canvas.bucketFill(26, 5, "Y");
        canvas.bucketFill(3, 7, "G");
        canvas.bucketFill(13,5,"M");
        canvas.bucketFill(22,3, "G");
        canvas.bucketFill(26, 3, "M");

        canvas.addRectangle(2,2,26,13);
        canvas.addRectangle(4,4,24,11);

        canvas.drawToConsole();

    }
    
    @Test
    public void checkCoords(){
        addCoords(5, 5, 8, 5);
        addCoords(5, 6, 5, 8);
        addCoords(7, 7, 11, 7);
        addCoords(10, 2, 10, 7);
        addCoords(6,3,9,3);
        addCoords(8, 9, 12, 9);
        addCoords(1,7,1,9);
        addCoords(3,6,3,8);
        addCoords(1,10,5,10);

        addCoords(7,9,7,12);
        addCoords(2,12,4,12);
        addCoords(1,14,2,14);
        addCoords(3,13,3,14);

        addCoords(9,11,9,14);
        addCoords(5,13,5,14);
        addCoords(6,14,7,14);
        addCoords(11,11,13,11);
        addCoords(14,11,14,12);
        addCoords(11,12,11,14);
        addCoords(12,14,18,14);

        addCoords(17,10,17,14);

        addCoords(15,7,15, 9);
        addCoords(16,7,17, 7);
        addCoords(17,8,19,8);
        addCoords(19,7,21,7);
        addCoords(21,8,21,9);
        addCoords(19, 10, 19, 12);
        addCoords(20, 12, 23,12);
        addCoords(23, 5, 23,11);

        addCoords(17,5,22,5);
        addCoords(13, 4, 13, 7);
        addCoords(14, 3, 17, 3);

        addCoords(15,4,15,5);
        addCoords(20,1,20,4);
        addCoords(22,2,22,3);
        addCoords(24,1,24,3);
        addCoords(26,3,27,3);
        addCoords(25,5,27,5);
        addCoords(26,1,29,1);
        addCoords(29,2,29,7);

        addCoords(27,7, 27,8);
        addCoords(25,7, 25,10);

        addCoords(21, 13, 21,14);
        addCoords(29, 9, 29, 10);
        addCoords(27, 10, 27, 11);
        addCoords(26, 12, 27, 12);
        addCoords(25,12, 25,13);
        addCoords(23,14,29,14);
        addCoords(29,12, 29,13);

        addCoords(1, 1, 17, 1);
        addCoords(1, 2, 1, 5);
        addCoords(2,4,3,4);
        addCoords(3,3,4,3);
    }

    private Set<Point> coords = new HashSet<>();

    private void addCoords(Integer x1, Integer y1, Integer x2, Integer y2){
        if (!coords.add(Point.of(x1, y1))){
            fail("Points already added");
        }
        if (!coords.add(Point.of(x2, y2))){
            fail("Points already added");
        }
    }
    

    @Test
    public void drawMazeCase(){
        DrawContext dc = new DrawContext(System.out);
        Canvas canvas = new Canvas(32, 16, dc);

        canvas.addLine(10, 2, 10, 7);
        canvas.addLine(6,3,9,3);
        canvas.addLine(6,3,9,3);
        canvas.addLine(8, 9, 12, 9);
        canvas.addLine(1,7,1,9);
        canvas.addLine(3,6,3,8);
        canvas.addLine(1,10,5,10);

        canvas.addLine(7,9,7,12);
        canvas.addLine(2,12,4,12);
        canvas.addLine(1,14,3,14);
        canvas.addLine(3,13,3,13);

        canvas.addLine(9,11,9,14);
        canvas.addLine(5,13,5,14);
        canvas.addLine(6,14,7,14);
        canvas.addLine(11,11,14,11);
        canvas.addLine(14,12,14,12);
        canvas.addLine(11,12,11,14);
        canvas.addLine(11,14,18,14);

        canvas.addLine(17,10,17,14);

        canvas.addLine(15,7,15, 9);
        canvas.addLine(16,7,17, 7);
        canvas.addLine(17,8,17,8);
        canvas.addLine(18,8,19,8);
        canvas.addLine(19,7,21,7);
        canvas.addLine(21,8,21,9);
        canvas.addLine(19, 10, 19, 12);
        canvas.addLine(20, 12, 23,12);
        canvas.addLine(23, 5, 23,12);

        canvas.drawToConsole();

    }
    @Test
    public void doesNotCreateNewGroup(){
        canvas = new Canvas(4, 3, context);
        canvas.addLine(1,0,3,0);
        //canvas.addLine(3,0,3,2);
        //canvas.addLine(1,2,3,2);

        Map<Long, CoordSet> allSet = canvas.getAllConnectedGroup();
        System.out.println(allSet.size());
    }

    private void assertCoordInUniqueSet(Canvas canvas) {
        Integer width = canvas.getCanvasWidth();
        Integer height = canvas.getCanvasHeight();
        Map<Long,CoordSet> setsById = canvas.getAllConnectedGroup();
        for (int x=0; x<width; x++){
            for (int y=0; y<height; y++){
                Point p = Point.of(x,y);
                Long[] id = setsById.entrySet().stream().filter(e -> e.getValue().contains(p.x, p.y)).map(Map.Entry::getKey).toArray(Long[]::new);
                if (id.length != 1) {
                    fail(p +" appears in "+Arrays.toString(id));
                }
            }
        }
    }

    private void assertCoordInUniqueSet(Integer width, Integer height, Map<Long,CoordSet> setsById) {
        for (int x=0; x<width; x++){
            for (int y=0; y<height; y++){
                Point p = Point.of(x,y);
                Long[] id = setsById.entrySet().stream().filter(e -> e.getValue().contains(p.x, p.y)).map(Map.Entry::getKey).toArray(Long[]::new);
                if (id.length != 1) {
                    fail(p +" appears in "+Arrays.toString(id));
                }
            }
        }
    }

    private void assertCoordInUniqueSet(Integer width, Integer height, CoordSet[] startX, CoordSet[] startY, CoordSet[] endX, CoordSet[] endY) {
        for (int x=0; x<width; x++){
            for (int y=0; y<height; y++){
                Point p = Point.of(x,y);
                int[] startXSetCount = IntStream.range(0, width).filter(i -> startX[i].contains(p.x, p.y)).toArray();
                int[] endXSetCount = IntStream.range(0, width).filter(i -> endX[i].contains(p.x, p.y)).toArray();
                int[] startYSetCount = IntStream.range(0, height).filter(i -> startY[i].contains(p.x, p.y)).toArray();
                int[] endYSetCount = IntStream.range(0, height).filter(i -> endY[i].contains(p.x, p.y)).toArray();
                if (startXSetCount.length != 1 || endXSetCount.length != 1 || startYSetCount.length != 1 || endYSetCount.length != 1) {
                    String startXSets = " StartX: "+Arrays.toString(startXSetCount);
                    String startYSets = " StartY: "+Arrays.toString(startYSetCount);
                    String endXSet = " EndX: "+Arrays.toString(endXSetCount);
                    String endYSets = " EndY: "+Arrays.toString(endYSetCount);
                    fail(p +" appears in "+startXSets+startYSets+endXSet+endYSets);
                }
            }
        }
    }

    private void forAllCordsIn(Point topLeft, Point bottomRight, CoordSet coordSet, BiConsumer<CoordSet, Point> action) {
        for (int x = topLeft.x; x<= bottomRight.x; x++){
            for (int y= topLeft.y; y<= bottomRight.y; y++){
                action.accept(coordSet, Point.of(x,y));
            }
        }
    }

    private void forAllCordsIn(Integer x1, Integer y1, Integer x2, Integer y2, CoordSet coordSet, BiConsumer<CoordSet, Point> action) {
        for (int x = x1; x<=x2; x++){
            for (int y=y1; y<=y2; y++){
                action.accept(coordSet, Point.of(x,y));
            }
        }
    }

    
    private CoordSet createCoordSet(String input, Integer width, Integer height) {
        if (input.length() != width * height) {
            throw new IllegalArgumentException("Input string does not have "+width*height+" characters");
        }
        CoordSet set = new CoordSet(width, height);
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (input.charAt(y*width+x) == 'X') {
                    set.add(x, y);
                }
            }
        }
        return set;
    }
    
}
