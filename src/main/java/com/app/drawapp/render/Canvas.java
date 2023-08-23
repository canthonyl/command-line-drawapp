package com.app.drawapp.render;


import com.app.drawapp.DrawContext;
import com.app.drawapp.console.ConsoleContext;
import com.app.drawapp.console.ConsoleOutput;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Canvas {

    private final String backgroundRepresentation = " ";
    private final String lineRepresentation = "X";
    private final AttributeGroup backgroundAttribute;
    private final AttributeGroup lineAttribute;

    private final Integer canvasWidth;
    private final Integer canvasHeight;
    private final DrawContext drawContext;
    private final ConsoleContext consoleContext;
    private final ConsoleOutput consoleOutput;

    private final Map<Long, CoordSet> connectedGroup;
    private final Map<Long, CoordSet> connectedGroupWithDefaultLineAttribute;
    private final Map<Long, AttributeGroup> connectedGroupAttribute;
    private final Long[][] connectedGroupByCoord;
    private final ProjectTarget coordAttributes;

    private final CoordSet initialBackground;
    private final CoordSet blank;
    private final Integer[][] step;

    private final Predicate<Long> groupUsesOriginalLineAttribute;

    public Canvas(Integer width, Integer height, DrawContext context){
        canvasWidth = width;
        canvasHeight = height;
        drawContext = context;
        consoleContext = drawContext.consoleContext;
        consoleOutput = drawContext.consoleContext.createConsoleOutput(width, height);


        Colour bgColour = Optional.ofNullable(consoleOutput.getCurrentBackgroundColour()).orElse(Colour.BLACK);
        step = new Integer[][]{{0,1}, {1,0}};

        backgroundAttribute = new AttributeGroup(backgroundRepresentation, bgColour);
        lineAttribute = new AttributeGroup(lineRepresentation);

        connectedGroup = new HashMap<>();
        connectedGroupWithDefaultLineAttribute = new HashMap<>();
        connectedGroupAttribute = new HashMap<>();
        connectedGroupByCoord = new Long[height][width];

        Long groupId = Long.valueOf(connectedGroup.size());
        AttributeGroup initialBackgroundAttribute = new AttributeGroup(backgroundAttribute.text);
        initialBackground = new CoordSet(width, height);
        blank = new CoordSet(canvasWidth, canvasHeight);

        coordAttributes = consoleOutput.getCoordProjectTarget();

        connectedGroup.put(groupId, initialBackground);
        connectedGroupAttribute.put(groupId, initialBackgroundAttribute);

        initialBackground.addAllCoords();
        initialBackground.project(coordAttributes, initialBackgroundAttribute);
        initialBackground.project(connectedGroupByCoord, groupId);

        groupUsesOriginalLineAttribute = id -> connectedGroupWithDefaultLineAttribute.containsKey(id);
    }


    private Set<Long> groupsAtLine(Integer x1, Integer y1, Integer x2, Integer y2){
        Integer[] delta = step[x1 == x2 ? 0 : 1];

        Set<Long> groups = new HashSet<>();
        for (Integer x=x1, y=y1; x<=x2 && y<=y2; x+=delta[0], y+=delta[1]){
            Long groupIdAtCoord = connectedGroupByCoord[y][x];
            groups.add(groupIdAtCoord);
        }
       return groups;
    }

    private Set<Long> adjacentGroups(CoordSet coordsAtLine){
        Set<Long> ids = new HashSet();

        connectedGroupWithDefaultLineAttribute.forEach((id, coordSet) -> {
            CoordSet s = coordsAtLine.retain(coordSet);
            if (s.size() > 0) {
                ids.add(id);
            }
        });

        return ids;
    }

    public void addLine(Integer x1, Integer y1, Integer x2, Integer y2) {
        Boolean deferScanAndUpdateGroup = Boolean.FALSE;
        addLine(x1, y1, x2, y2, deferScanAndUpdateGroup);
    }

    private Set<Long> addLine(Integer x1, Integer y1, Integer x2, Integer y2, Boolean deferScanAndUpdateGroup) {
        CoordSet coordsDrawn = new CoordSet(canvasWidth, canvasHeight);
        coordsDrawn.add(x1, y1, x2, y2);

        Set<Long> groupsAtCoord = groupsAtLine(x1, y1, x2, y2);

        Set<Long> adjacentGroups = adjacentGroups(coordsDrawn);
        Set<Long> allGroups = combine(groupsAtCoord, adjacentGroups);

        if (!deferScanAndUpdateGroup) {
            Set<Long> connectableGroups = allGroups.stream()
                    .filter(groupUsesOriginalLineAttribute)
                    .collect(Collectors.toSet());

            Set<Long> dividableGroups = allGroups.stream()
                    .filter(groupUsesOriginalLineAttribute.negate())
                    .collect(Collectors.toSet());

            scanForConnectedRegion(connectableGroups, coordsDrawn, lineAttribute);

            scanForDividedRegion(dividableGroups, coordsDrawn);
        }

        return allGroups;
    }

    private void scanForConnectedRegion(Set<Long> groupsToScan, CoordSet coordsDrawn, AttributeGroup attribute){
        GroupScanner groupScanner = drawContext.getGroupScanner();
        Set<Long> connectedGroupIds = new HashSet<>();

        for (Long groupId : groupsToScan) {
            CoordSet group = connectedGroup.get(groupId).copy();
            group.addAll(coordsDrawn);
            List<CoordSet> newGroups = groupScanner.scanForNewGroup(group);
            if (newGroups.size() == 1) {
                connectedGroupIds.add(groupId);
            }
        }

        Long targetId;
        CoordSet targetCoords;
        AttributeGroup targetAttribute;

        if (connectedGroupIds.size() == 0) {
            targetId = Long.valueOf(connectedGroup.size());
            targetCoords = coordsDrawn;
            targetAttribute = new AttributeGroup(attribute);
            connectedGroup.put(targetId, coordsDrawn);
            connectedGroupAttribute.put(targetId,targetAttribute);
            connectedGroupWithDefaultLineAttribute.put(targetId, coordsDrawn);
            connectedGroupIds.add(targetId);
        } else {
            targetId = connectedGroupIds.iterator().next();
            targetCoords = connectedGroup.get(targetId);
            targetAttribute = connectedGroupAttribute.get(targetId);
            for (Long id : connectedGroupIds) {
                if (!Objects.equals(id, targetId)) {
                    targetCoords.addAll(connectedGroup.get(id));
                    connectedGroup.put(id, blank);
                    connectedGroupAttribute.put(id, backgroundAttribute);
                    connectedGroupWithDefaultLineAttribute.remove(id);
                }
            }
            targetCoords.addAll(coordsDrawn);
        }

        targetCoords.project(coordAttributes, targetAttribute);
        targetCoords.project(connectedGroupByCoord, targetId);

    }

    private Set<Long> scanForDividedRegion(Set<Long> groupsToScan, CoordSet coordsDrawn){
        Set<Long> newGroupIds = new HashSet<>();
        GroupScanner groupScanner = drawContext.getGroupScanner();

        for (Long groupId : groupsToScan) {
            CoordSet originalGroup = connectedGroup.get(groupId);
            originalGroup.removeAll(coordsDrawn);
            List<CoordSet> newGroups = groupScanner.scanForNewGroup(originalGroup);

            AttributeGroup existingAttributeGroup = connectedGroupAttribute.get(groupId);

            if (newGroups.size() > 1) {
                for (Integer i = 0; i < newGroups.size() - 1; i++) {
                    CoordSet newGroup = newGroups.get(i);
                    originalGroup.removeAll(newGroup);
                    Long newGroupId = Long.valueOf(connectedGroup.size());
                    connectedGroup.put(newGroupId, newGroup);
                    AttributeGroup newAttributeGroup = new AttributeGroup(existingAttributeGroup);
                    connectedGroupAttribute.put(newGroupId, newAttributeGroup);
                    newGroup.project(coordAttributes, newAttributeGroup);
                    newGroup.project(connectedGroupByCoord, newGroupId);
                    newGroupIds.add(newGroupId);
                }
            }
        }
        return newGroupIds;
    }

    public void addRectangle(Integer x1, Integer y1, Integer x2, Integer y2) {
        Boolean deferScanAndUpdateGroup = Boolean.TRUE;

        Set<Long> proximityGroup0 = addLine(x1, y1, x2, y1, deferScanAndUpdateGroup);
        Set<Long> proximityGroup1 = addLine(x2, y1, x2, y2, deferScanAndUpdateGroup);
        Set<Long> proximityGroup2 = addLine(x1, y2, x2, y2, deferScanAndUpdateGroup);
        Set<Long> proximityGroup3 = addLine(x1, y1, x1, y2, deferScanAndUpdateGroup);

        Set<Long> allGroups = combine(proximityGroup0, proximityGroup1, proximityGroup2, proximityGroup3);

        Set<Long> connectableGroups = allGroups.stream()
                .filter(groupUsesOriginalLineAttribute)
                .collect(Collectors.toSet());

        Set<Long> dividableGroups = allGroups.stream()
                .filter(groupUsesOriginalLineAttribute.negate())
                .collect(Collectors.toSet());

        CoordSet coordsAtLine = new CoordSet(canvasWidth, canvasHeight);
        coordsAtLine.add(x1, y1, x2, y1);
        coordsAtLine.add(x2, y1, x2, y2);
        coordsAtLine.add(x1, y2, x2, y2);
        coordsAtLine.add(x1, y1, x1, y2);

        scanForConnectedRegion(connectableGroups, coordsAtLine, lineAttribute);

        scanForDividedRegion(dividableGroups, coordsAtLine);

    }

    public void bucketFill(Integer x, Integer y, String bgColorCode) {
        Long groupId = connectedGroupByCoord[y][x];
        CoordSet group = connectedGroup.get(groupId);
        AttributeGroup attribute = connectedGroupAttribute.get(groupId);

        Colour bgColour = ConsoleColour.colourByCode.get(bgColorCode);

        AttributeGroup targetAttribute = new AttributeGroup(attribute.getText(), bgColour);

        Set<Long> connectableGroups = connectedGroupAttribute.entrySet()
                .stream().filter(e -> e.getValue().equals(targetAttribute))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());

        Set<Long> connectedGroupId = new HashSet<>();
        for (Long id : connectableGroups) {
            CoordSet cGroup = connectedGroup.get(id);
            if (group.retain(cGroup).size() > 0) {
                connectedGroupId.add(id);
            }
        }

        if (connectedGroupId.size() == 0) {
            if (isLineGroup(groupId) && !attribute.getBgColour().isPresent()
                    && connectedGroupWithDefaultLineAttribute.containsKey(groupId)) {
                connectedGroupWithDefaultLineAttribute.remove(groupId);
            }

            attribute.setBgColour(bgColour);
            group.project(coordAttributes, attribute);

        } else {
            Iterator<Long> groupIdIter = connectedGroupId.iterator();
            Long joinToGroupId = groupIdIter.next();
            CoordSet joinGroup = connectedGroup.get(joinToGroupId);
            AttributeGroup joinGroupAttribute = connectedGroupAttribute.get(joinToGroupId);

            while(groupIdIter.hasNext()){
                Long id = groupIdIter.next();
                CoordSet cGroup = connectedGroup.get(id);

                joinGroup.addAll(cGroup);
                connectedGroup.put(id, blank);
                connectedGroupAttribute.put(id, backgroundAttribute);
            }

            joinGroup.addAll(group);
            joinGroupAttribute.setBgColour(bgColour);

            if (connectedGroupAttribute.get(groupId).getText().contentEquals(lineRepresentation) && !attribute.bgColour.isPresent()
                    && connectedGroupWithDefaultLineAttribute.containsKey(groupId)) {
                connectedGroupWithDefaultLineAttribute.remove(groupId);
            }
            connectedGroup.put(groupId, blank);
            connectedGroupAttribute.put(groupId, backgroundAttribute);
            joinGroup.project(coordAttributes, joinGroupAttribute);
            joinGroup.project(connectedGroupByCoord, joinToGroupId);
        }
    }

    CoordSet getConnectedGroup(Long id){
        return connectedGroup.get(id);
    }

    Map<Long, CoordSet> getAllConnectedGroup() { return connectedGroup; }

    public Integer getCanvasWidth() { return canvasWidth; }
    public Integer getCanvasHeight() { return canvasHeight; }

    public Boolean coordinatesInRange(Integer x, Integer y){
        return x < canvasWidth && y < canvasHeight && x >= 0 && y >= 0;
    }

    public void drawToConsole(){
        consoleOutput.draw();
    }

    public ConsoleOutput getConsoleOutput(){
        return consoleOutput;
    }

    private Boolean isBackgroundGroup(Long id){
        return connectedGroupAttribute.get(id).getText().contentEquals(backgroundRepresentation);
    }

    private Boolean isLineGroup(Long id){
        return connectedGroupAttribute.get(id).getText().contentEquals(lineRepresentation);
    }

    private Set<Long> combine(Set<Long>... groups) {
        return Arrays.stream(groups).flatMap(Set::stream).collect(Collectors.toSet());
    }

    public class AttributeGroup {
        private final String text;
        private Optional<Colour> bgColour;

        public AttributeGroup(String val){
            text = val;
            bgColour = Optional.empty();
        }

        public AttributeGroup(String text, Colour bgColour){
            this.text = text;
            this.bgColour = Optional.of(bgColour);
        }

        public AttributeGroup(AttributeGroup other) {
            this.text = other.text;
            this.bgColour = other.bgColour;
        }

        public String getText() { return text; }
        public Optional<Colour> getBgColour(){ return bgColour; }
        public void setBgColour(Colour colour) { this.bgColour = Optional.of(colour); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AttributeGroup other = (AttributeGroup) o;
            return Objects.equals(text, other.text) && Objects.equals(bgColour, other.bgColour);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bgColour, text);
        }
    }


}
