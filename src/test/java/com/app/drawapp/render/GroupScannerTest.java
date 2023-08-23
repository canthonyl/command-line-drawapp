package com.app.drawapp.render;

import com.app.drawapp.render.CoordSet;
import com.app.drawapp.render.GroupScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GroupScannerTest {

    private GroupScanner groupScanner;

    @BeforeEach
    public void setup(){
        groupScanner = new GroupScanner();
    }

    @Test
    public void scanSpaceOutsideAndInsideRectangle(){
        String group0Str =
                "XXXXXXXXX"+
                "X       X"+
                "X       X"+
                "X       X"+
                "X       X"+
                "X       X"+
                "X       X"+
                "X       X"+
                "XXXXXXXXX";

        String group1Str =
                "         "+
                "         "+
                "  XXXXX  "+
                "  X   X  "+
                "  X   X  "+
                "  X   X  "+
                "  XXXXX  "+
                "         "+
                "         ";

        String group2Str =
                "         "+
                "         "+
                "         "+
                "         "+
                "    X    "+
                "         "+
                "         "+
                "         "+
                "         ";

        CoordSet group0 = createCoordSet(group0Str, 9,9);
        CoordSet group1 = createCoordSet(group1Str, 9,9);
        CoordSet group2 = createCoordSet(group2Str, 9,9);
        CoordSet input = combine(group0, group1, group2);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }


    @Test
    public void continuousRegionDeepInto() {
        String group0Str =
                "XXXXXXXX" +
                "X   X  X" +
                "X XXX  X" +
                "X  X   X" +
                "X XXX  X" +
                "X   X  X" +
                "X   X  X" +
                "XXXXXXXX";

        CoordSet group0 = createCoordSet(group0Str, 8, 8);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }


    @Test
    public void scanEnvelopedSpaceAsSeparateRegion(){
        String group0Str =
                "XXXXXXX"+
                "X     X"+
                "X     X"+
                "X     X"+
                "X XXX X"+
                "X  X  X"+
                "XXXXXXX";
        
        String group1Str =
                "       "+
                "       "+
                "  XXX  "+
                "       "+
                "       "+
                "       "+
                "       ";

        CoordSet group0 = createCoordSet(group0Str, 7, 7);
        CoordSet group1 = createCoordSet(group1Str, 7, 7);
        CoordSet input = combine(group0, group1);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }
    
    @Test
    public void scanGroupFromTopBottomHalfRegion(){
        String group0Str =
                " XXX "+
                "     "+
                "     ";

        String group1Str =
                "     "+
                "     "+
                " XXX ";

        CoordSet group0 = createCoordSet(group1Str, 5, 3);
        CoordSet group1 = createCoordSet(group0Str, 5, 3);
        CoordSet input = combine(group0, group1);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void continuousRegion_letterH(){
        String group0Str =
                " X X "+
                " XXX "+
                " X X ";

        CoordSet group0 = createCoordSet(group0Str, 5, 3);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void continuousRegion_block(){
        String group0Str =
                "           "+
                " XXXXXXXXX "+
                " XXXXXXXXX "+
                " XXXXXXXXX "+
                "           ";

        CoordSet group0 = createCoordSet(group0Str, 11, 5);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);

    }

    @Test
    public void continuousRegion_verticalBlock(){
        String group0Str =
                "     "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                " XXX "+
                "     ";

        CoordSet group0 = createCoordSet(group0Str, 5, 11);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void scanGroupFromLeftRightHalfRegion(){
        String group0Str =
                "   XX"+
                "   XX"+
                "   XX";

        String group1Str =
                "XX   "+
                "XX   "+
                "XX   ";

        CoordSet group0 = createCoordSet(group0Str, 5, 3);
        CoordSet group1 = createCoordSet(group1Str, 5, 3);
        CoordSet input = combine(group0, group1);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void scanGroupFromQuadrant(){
        String group0Str = "   XX"+
                            "     "+
                            "     ";

        String group1Str = "     "+
                            "     "+
                            "   XX";

        String group2Str = "     "+
                            "     "+
                            "XX   ";

        String group3Str = "XX   "+
                        "     "+
                        "     ";

        CoordSet group0 = createCoordSet(group0Str, 5, 3);
        CoordSet group1 = createCoordSet(group1Str, 5, 3);
        CoordSet group2 = createCoordSet(group2Str, 5, 3);
        CoordSet group3 = createCoordSet(group3Str, 5, 3);
        CoordSet input = combine(group0, group1, group2, group3);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2, group3);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void enclosedStripes(){
        String group0Str =
                "XXXXX"+
                "X   X"+
                "X X X"+
                "XXXXX";

        CoordSet group0 = createCoordSet(group0Str, 5, 4);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void dottedEdges(){
        String group0Str =
                "XXXXXXX"+
                "XX X XX"+
                "X     X"+
                "XX   XX"+
                "X     X"+
                "XX X XX"+
                "XXXXXXX";

        String group1Str =
                "       "+
                "       "+
                "       "+
                "   X   "+
                "       "+
                "       "+
                "       ";

        CoordSet group0 = createCoordSet(group0Str, 7, 7);
        CoordSet group1 = createCoordSet(group1Str, 7, 7);
        CoordSet input = combine(group0, group1);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void findContinuousRegion_letterE(){
        String group0Str =
                "XXXXXX"+
                "X    X"+
                "X XXXX"+
                "X X  X"+
                "X XX X"+
                "X X  X"+
                "X XXXX"+
                "X    X"+
                "XXXXXX"
                ;

        CoordSet group0 = createCoordSet(group0Str, 6, 9);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);

    }


    @Test
    public void nestedRegionB(){
        String group0Str =
                "XXXXXXXXX"+
                "X       X"+
                "X XXXXX X"+
                "X X   X X"+
                "X X     X"+
                "X XXXXXXX"
                ;

        CoordSet group0 = createCoordSet(group0Str, 9, 6);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }


    @Test
    public void stripes(){
        String group0Str =
                "XXXXXXX"+
                "X X X X"+
                "X X X X"+
                "X X X X"+
                "XXXXXXX"
                ;

        CoordSet group0 = createCoordSet(group0Str, 7, 5);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void nestedRectangle(){
        String group0Str =
            "XXXXXXX"+
            "XX X XX"+
            "X     X"+
            "XX   XX"+
            "X     X"+
            "XX X XX"+
            "XXXXXXX";

        String group1Str =
            "       "+
            "       "+
            "       "+
            "   X   "+
            "       "+
            "       "+
            "       ";

        CoordSet group0 = createCoordSet(group0Str, 7,7);
        CoordSet group1 = createCoordSet(group1Str, 7,7);
        CoordSet input = combine(group0, group1);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }


    @Test
    public void nestedRegionA(){
        String group0Str =
                "XXXXXXXXX"+
                "X       X"+
                "X XXXXX X"+
                "X X   X X"+
                "X X X X X"+
                "XXXXXXXXX"
                ;

        CoordSet group0 = createCoordSet(group0Str, 9, 6);
        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);

    }

    @Test
    public void testExtractGroup(){
        CoordSet group0 = new CoordSet(16, 16);
        CoordSet group1 = new CoordSet(16, 16);
        CoordSet group2 = new CoordSet(16, 16);
        CoordSet group3 = new CoordSet(16, 16);
        CoordSet group4 = new CoordSet(16, 16);

        group0.add(1,1);
        group1.add(14,1);
        group2.add(1,14);
        group3.add(14,14);

        group4.add(4,4,12, 4);
        group4.add(12, 4,12, 12);
        group4.add(4,12,12,12);
        group4.add(4, 4, 4, 12);

        CoordSet input = combine(group0, group1, group2, group3, group4);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2, group3, group4);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void testExtractGroup2(){
        CoordSet group0 = new CoordSet(24, 24);
        CoordSet group1 = new CoordSet(24, 24);
        CoordSet group2 = new CoordSet(24, 24);

        group0.add(1,1, 22, 1);
        group0.add(22,1, 22, 22);
        group0.add(1,22, 22, 22);
        group0.add(1,1, 1, 22);

        group1.add(3,3, 20, 3);
        group1.add(20,3, 20, 20);
        group1.add(3,20, 20, 20);
        group1.add(3,3, 3, 20);

        group2.add(10,10, 12, 10);
        group2.add(12,10, 12, 12);
        group2.add(10,12, 12, 12);
        group2.add(10,10, 10, 12);

        CoordSet input = combine(group0, group1, group2);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);

    }

    @Test
    public void testExtractGroup3(){
        CoordSet group0 = new CoordSet(16, 8);
        group0.add(6,0,10,0);
        group0.add(6,0,6,2);
        group0.add(6,2,8,2);
        group0.add(8,2,8,4);
        group0.add(4,6,8,4);
        group0.add(6,5,8,5);
        group0.add(10,0,10,6);

        CoordSet input = combine(group0);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);

    }

    @Test
    public void testExtractGroup4(){
        CoordSet group0 = new CoordSet(8, 16);
        CoordSet group1 = new CoordSet(8, 16);
        CoordSet group2 = new CoordSet(8, 16);
        CoordSet group3 = new CoordSet(8, 16);
        CoordSet group4 = new CoordSet(8, 16);

        group0.add(1,1);
        group1.add(6,1);
        group2.add(1,14);
        group3.add(6,14);

        group4.add(2,3,5, 3);
        group4.add(5, 3,5, 12);
        group4.add(2,12,5,12);
        group4.add(2, 3, 2, 12);

        CoordSet input = combine(group0, group1, group2, group3, group4);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2, group3, group4);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void testExtractGroup5(){
        CoordSet group0 = new CoordSet(1, 10);
        CoordSet group1 = new CoordSet(1, 10);
        CoordSet group2 = new CoordSet(1, 10);
        CoordSet group3 = new CoordSet(1, 10);
        CoordSet group4 = new CoordSet(1, 10);

        group0.add(0,1);
        group1.add(0,3);
        group2.add(0,5);
        group3.add(0,7);
        group4.add(0,9);

        CoordSet input = combine(group0, group1, group2, group3, group4);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2, group3, group4);
        Set<String> resultKeys = toKeys(result);

        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void testGroup6(){
        CoordSet group0 = new CoordSet(16, 8);
        CoordSet group1 = new CoordSet(16, 8);
        CoordSet group2 = new CoordSet(16, 8);
        CoordSet group3 = new CoordSet(16, 8);

        group0.add(1,1);
        group1.add(3,1);
        group2.add(5,1);
        group3.add(7,1,8,1);
        group3.add(8,1,8,3);
        group3.add(7,3,8,3);
        group3.add(7,3, 7,5);

        CoordSet input = combine(group0, group1, group2, group3);

        List<CoordSet> result = groupScanner.scanForNewGroup(input);

        Set<String> groupKeys = toKeys(group0, group1, group2, group3);
        Set<String> resultKeys = toKeys(result);
        assertEquals(groupKeys, resultKeys);
    }

    @Test
    public void testConnectedGroup(){
        String key = "MTAsMTAKLTI3Njk0MzgwMTgwMzIzNCwyMTcwMTcyMDcwNjA2OTI5OTIKNDkyMTYsMjU3";
        //String key = "MTAsOAotNDU5MzM5MTIyNjc1MjkyMTA1OCw3MjM0MjM2NzU0OTU4NzQ1Ng==";
        CoordSet set = new CoordSet(key);
        List<CoordSet> result = groupScanner.scanForNewGroup(set);
        System.out.println(result.size());
        assertEquals(1, result.size());
    }

    @Test
    public void testConnectedGroup2(){
        String key = "MjUsMTUKLTcyMzM5OTQ4NjI1MTk5MTA0MSw5MTg2MjEyOTI0NjM1MDIwMjg3LDU4NTU5NDU2MDMyMjEwMjg4NjMsNzIzNDAxNzI4MzgwNzY2NzMKNzE3OTU3MzU4NDYxNDIzMzMsNzE3ODI2NzE5MzE4MDE0NDAsNzIwNDk4ODQ2NjMyMTU0NTUsMjgyNTc4ODAwMTQ4NzM3";

        CoordSet set = new CoordSet(key);
        List<CoordSet> result = groupScanner.scanForNewGroup(set);
        assertEquals(1, result.size());
    }

    @Test
    public void testConnectedGroup3(){
        String key = "MzIsMTYKNTgyMzE4MTczNzc5ODEzOTkwNCw1ODI2MzQ5ODkwNzYxOTE2NDE2LDQ5MzU2NjQ4ODA3NTg4MTY3NjgsMAo0MTQzMzQzNTE1NjgsNTczNDc4NzU2NDc5LDIzNDQ1NDYyODY1LDA=";

        CoordSet set = new CoordSet(key);
        List<CoordSet> result = groupScanner.scanForNewGroup(set);
        assertEquals(key, result.get(0).getKey());
        assertEquals(1, result.size());
    }

    @Test
    public void testCombineGroupByEdge(){
        CoordSet prevXG0 = new CoordSet(16, 16);
        CoordSet prevXG1 = new CoordSet(16, 16);
        CoordSet g0 = new CoordSet(16, 16);
        CoordSet g1 = new CoordSet(16, 16);
        CoordSet g2 = new CoordSet(16, 16);
        CoordSet g3 = new CoordSet(16, 16);

        prevXG0.add(7,1,7,3);
        prevXG1.add(7,5,7,7);

        g0.add(8,1);
        g1.add(8,3);
        g2.add(8,5);
        g3.add(8,7);

        List<CoordSet> prevCellX = toList(prevXG0, prevXG1);
        List<CoordSet> cell = toList(g0, g1,g2,g3);

        groupScanner.combine(prevCellX, cell, 1, 0, 0);

        CoordSet combinedGroupA = combine(prevXG0, g0, g1);
        CoordSet combinedGroupB = combine(prevXG1, g2, g3);

        Set<String> combinedKeys = toKeys(combinedGroupA, combinedGroupB);
        Set<String> prevCombinedKeys = toKeys(prevCellX);
        Set<String> currentCombinedKeys = toKeys(cell);

        assertEquals(combinedKeys, prevCombinedKeys);
        assertEquals(combinedKeys, currentCombinedKeys);
        assertEquals(prevCombinedKeys.size(), prevCellX.size());
        assertEquals(currentCombinedKeys.size(), cell.size());
    }

    @Test
    public void testCombineGroupByEdge2(){

        CoordSet prevG0 = new CoordSet(16, 16);
        CoordSet prevG1 = new CoordSet(16, 16);
        CoordSet prevG2 = new CoordSet(16, 16);
        CoordSet prevG3 = new CoordSet(16, 16);

        CoordSet g0 = new CoordSet(16, 16);
        CoordSet g1 = new CoordSet(16, 16);

        prevG0.add(7,1);
        prevG1.add(7,3);
        prevG2.add(7,5);
        prevG3.add(7,7);

        g0.add(8,1,8,3);
        g1.add(8,5,8,7);

        List<CoordSet> prevCellX = toList(prevG0, prevG1, prevG2, prevG3);
        List<CoordSet> cell = toList(g0, g1);

        groupScanner.combine(prevCellX, cell, 1, 0, 0);

        CoordSet combinedGroupA = combine(g0, prevG0, prevG1);
        CoordSet combinedGroupB = combine(g1, prevG2, prevG3);

        Set<String> combinedKeys = toKeys(combinedGroupA, combinedGroupB);
        Set<String> prevCombinedKeys = toKeys(prevCellX);
        Set<String> currentCombinedKeys = toKeys(cell);

        assertEquals(combinedKeys, prevCombinedKeys);
        assertEquals(combinedKeys, currentCombinedKeys);
        assertEquals(prevCombinedKeys.size(), prevCellX.size());
        assertEquals(currentCombinedKeys.size(), cell.size());
    }

    @Test
    public void testCombineEdgeGroup3(){
        CoordSet prevXG0 = new CoordSet(16, 16);
        CoordSet prevXG1 = new CoordSet(16, 16);

        CoordSet g0 = new CoordSet(16, 16);
        CoordSet g1 = new CoordSet(16, 16);

        prevXG0.add(7,1,7,3);
        prevXG1.add(7,5,7,7);

        g0.add(8,1);
        g1.add(8,3,8,5);

        List<CoordSet> prevCellX = toList(prevXG0, prevXG1);
        List<CoordSet> cell = toList(g0, g1);

        groupScanner.combine(prevCellX, cell, 1, 0, 0);

        CoordSet combinedGroup0 = combine(prevXG0, g0, prevXG1, g1);

        Set<String> combinedGroupKeys = toKeys(combinedGroup0);
        Set<String> prevCellGroupKeys = toKeys(prevCellX);
        Set<String> currentCellGroupKeys = toKeys(cell);

        assertEquals(combinedGroupKeys, prevCellGroupKeys);
        assertEquals(combinedGroupKeys, currentCellGroupKeys);
    }

    @Test
    public void testCombineEdgeGroup4(){
        CoordSet prevXG0 = new CoordSet(16, 16);
        CoordSet prevXG1 = new CoordSet(16, 16);

        CoordSet g0 = new CoordSet(16, 16);
        CoordSet g1 = new CoordSet(16, 16);

        prevXG0.add(7,1);
        prevXG1.add(7,3,7,5);

        g0.add(8,1,8,3);
        g1.add(8,5,8,7);

        List<CoordSet> prevCellX = toList(prevXG0, prevXG1);
        List<CoordSet> cell = toList(g0, g1);

        groupScanner.combine(prevCellX, cell, 1, 0, 0);

        CoordSet combinedGroup0 = combine(prevXG0, g0, prevXG1, g1);

        Set<String> combinedGroupKeys = toKeys(combinedGroup0);
        Set<String> prevCellGroupKeys = toKeys(prevCellX);
        Set<String> currentCellGroupKeys = toKeys(cell);

        assertEquals(combinedGroupKeys, prevCellGroupKeys);
        assertEquals(combinedGroupKeys, currentCellGroupKeys);
    }

    private Set<String> toKeys(List<CoordSet> list){
        return list.stream().map(CoordSet::getKey).collect(Collectors.toSet());
    }

    private Set<String> toKeys(CoordSet... groups){
        return toKeys(Arrays.asList(groups));
    }

    private List<CoordSet> toList(CoordSet... groups){
        return Stream.of(groups).collect(Collectors.toList());
    }

    private CoordSet combine(CoordSet... groups){
        CoordSet result = new CoordSet(groups[0].getWidth(), groups[0].getHeight());
        for (CoordSet set : groups){
            result.addAll(set);
        }
        return result;
    }

    private void assertCoordSetEquals(CoordSet expectedSet, CoordSet actualSet) {
        if (expectedSet.getWidth() != actualSet.getWidth() || expectedSet.getHeight() != actualSet.getHeight()) {
            fail("Dimension does not match (expected " + expectedSet.getWidth() + "x" + expectedSet.getHeight()
                    + " vs actual " + actualSet.getWidth() + "x" + actualSet.getHeight() + ")");
        }
        for (int x=0; x<expectedSet.getWidth(); x++){
            for (int y=0; y<expectedSet.getHeight(); y++){
                boolean expectedContains = expectedSet.contains(x,y);
                boolean actualContains = actualSet.contains(x,y);
                assertEquals(expectedContains, actualContains,
                        "Coord "+x+","+y+" "+(!expectedContains?"not in":"in")
                                +" expected while it is "+(!actualContains?"not in":"in")+" actual");
            }
        }

    }

    private CoordSet createCoordSet(String input, Integer width, Integer height) {
        if (input.length() != width * height) {
            throw new IllegalArgumentException("Input string has "+input.length() +" characters instead of expected "+ width * height);
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
