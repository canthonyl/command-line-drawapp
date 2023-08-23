package com.app.drawapp.render;

import java.util.*;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class GroupScanner {

    private final Map<String, List<CoordSet>> memoizedResult;

    private final Integer[][] deltaByOrientation = {{1,0},{0,1}};
    private final Long[][] edgeMaskByOrientation = {
            {
                (1L | 1L << 8 | 1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56)<<7,
                (1L | 1L << 8 | 1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56)
            },
            {
                (-1L>>>8)^-1L,
                    -1L>>>-8
            }};

    private final LongUnaryOperator[][] edgeBitMask = {{i->1L<<(8*i+7), i->1L<<(8*i)},{i->(1L<<i)<<56,i->(1L<<i)}};

    public GroupScanner() {
        memoizedResult = new HashMap<>();
    }

    private List<CoordSet> scale(List<CoordSet> result, CoordSet scalingSet) {
        return result.stream()
                .map(s -> s.scale(scalingSet.getCompressionInfo()))
                .collect(Collectors.toList());
    }

    public List<CoordSet> scanForNewGroup(CoordSet group) {
        CoordSet scalingSet = group.copy();
        CoordSet compacted = scalingSet.compact();
        String key = compacted.getKey();
        if (memoizedResult.containsKey(key)){
            return scale(memoizedResult.get(key), scalingSet);
        }

        List<CoordSet> compactedResult = extract(compacted);
        memoizedResult.put(key, compactedResult);
        return scale(compactedResult, scalingSet);
    }

    private Long extractGroupFrom(Long val, Integer index){
        Long left = 1L | 1L << 8 | 1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56;
        Long[] blank= { (left<<7)^-1, -1L>>>8, left ^-1, -1L<<8 };

        Long mask = 1L << index;
        Long result = 0L;
        Long workingVal = val;
        do {
            result |= mask;
            workingVal ^= mask;
            mask = (((mask & blank[0]) << 1) | ((mask & blank[1]) << 8) | ((mask& blank[2]) >>> 1) | ((mask& blank[3]) >>> 8)) ;
            mask &= workingVal;
        } while (!Objects.equals(mask, 0L));

        return result;
    }

    private List<Long> extract(Long val){
        List<Long> result = new LinkedList<>();
        Long working = val;
        while (Long.bitCount(working) > 0) {
            Integer index = Long.numberOfTrailingZeros(working);
            Long group = extractGroupFrom(working, index);
            result.add(group);
            working ^= group;
        }
        return result;
    }

    private CoordSet toCoordSet(Long val, Integer w, Integer h, Integer x, Integer y){
        return new CoordSet(w, h, (cellY, cellX) -> Objects.equals(cellY, Long.valueOf(y)) && Objects.equals(cellX, Long.valueOf(x)) ? val : 0L);
    }

    private List<CoordSet> extractGroupsAt(CoordSet set, Integer cellX, Integer cellY){
        Long cellVal = set.getAsLong(cellX, cellY);
        List<Long> extracted = this.extract(cellVal);
        return extracted.stream()
                .map(v -> toCoordSet(v, set.getWidth(), set.getHeight(), cellX, cellY))
                .collect(Collectors.toList());
    }

    private List<List<CoordSet>> extend(List<List<CoordSet>> result, Integer numCellsX, Integer numCellsY, Integer length){
        if (numCellsX > numCellsY) {
            List<CoordSet> padList = Collections.emptyList();
            List<List<CoordSet>> padded = Stream.generate(() -> padList)
                    .limit(length)
                    .collect(Collectors.toList());
            result.addAll(padded);
        }
        return result;
    }

    private List<Optional<Integer>> initList(){
        return Arrays.asList(IntStream.range(0,8).mapToObj(i -> Optional.empty()).toArray(Optional[]::new));
    }

    void combine(List<CoordSet> prevGroups, List<CoordSet> currentGroups, Integer cellX, Integer cellY, Integer orientation) {
        LongUnaryOperator prevEdgeBitAt = edgeBitMask[orientation][0];
        LongUnaryOperator currentEdgeBitAt = edgeBitMask[orientation][1];

        Integer prevCellX = cellX - deltaByOrientation[orientation][0];
        Integer prevCellY = cellY - deltaByOrientation[orientation][1];

        Long prevEdgeMask = edgeMaskByOrientation[orientation][0];
        Long currentEdgeMask = edgeMaskByOrientation[orientation][1];

        List<Optional<Integer>> prevBits = initList();
        List<Optional<Integer>> currentBits = initList();
        List<Optional<Integer>> prevCombinedBits = initList();
        List<Optional<Integer>> currentCombinedBits = initList();

        IntStream.range(0, currentGroups.size()).forEach(li -> {
            CoordSet c = currentGroups.get(li);
            Long val = c.getAsLong(cellX, cellY);
            if (!Objects.equals(val & currentEdgeMask, 0L)) {
                LongStream.range(0, 8)
                        .filter(bitIndex -> !Objects.equals((currentEdgeBitAt.applyAsLong(bitIndex) & val),0L))
                        .forEach(bitIndex -> currentBits.set(Long.valueOf(bitIndex).intValue(), Optional.of(li)));
            }
        });

        IntStream.range(0, prevGroups.size()).forEach(li -> {
            CoordSet p = prevGroups.get(li);
            Long val = p.getAsLong(prevCellX, prevCellY);
            Boolean hasBitsAtEdge = !Objects.equals(val & prevEdgeMask, 0L);
            if (hasBitsAtEdge) {
                Boolean isIndependentGroup = true;
                for (Integer bitIndex=0; bitIndex<8; bitIndex++){
                    if (!Objects.equals((prevEdgeBitAt.applyAsLong(bitIndex) & val),0L)) {
                        if (currentBits.get(bitIndex).isPresent()) {
                            Integer cli = currentBits.get(bitIndex).get();
                            prevBits.set(bitIndex, Optional.of(li));
                            isIndependentGroup = false;

                            CoordSet prev = prevGroups.get(li);
                            CoordSet current = currentGroups.get(cli);

                            if (current != prev) {
                                current.addAll(prev);
                                prevGroups.set(li, current);
                                prevCombinedBits.set(bitIndex,Optional.of(cli));
                                currentCombinedBits.set(bitIndex, Optional.of(li));
                            }
                        }
                    }
                }
                if (isIndependentGroup) {
                    if (currentGroups.stream().noneMatch(c -> p==c)) {
                        currentGroups.add(p);
                    }
                }
            } else {
                if (currentGroups.stream().noneMatch(c -> p==c)) {
                    currentGroups.add(p);
                }
            }
        });

        IntStream.range(0, 8).filter(i -> prevCombinedBits.get(i).isPresent())
                .mapToObj( i -> {
                    Integer groupVal = prevCombinedBits.get(i).get();
                    List<Integer> prevCombinedBitsIndex = IntStream.range(0, 8)
                            .limit(8)
                            .filter(j -> prevCombinedBits.get(j).isPresent())
                            .filter(j -> prevCombinedBits.get(j).get().equals(groupVal))
                            .boxed().collect(Collectors.toList());

                    Set<Integer> removeSet = prevCombinedBitsIndex.stream()
                            .map(j -> prevBits.get(j).get())
                            .collect(Collectors.toSet());

                    Integer lastIndex = prevCombinedBitsIndex.get(prevCombinedBitsIndex.size()-1);
                    Integer retainGroup = prevBits.get(lastIndex).get();
                    removeSet.remove(retainGroup);
                    return removeSet;})
                .flatMap(Set::stream).collect(Collectors.toSet()).stream()
                .sorted(Comparator.reverseOrder())
                .forEach(i -> prevGroups.remove(i.intValue()));

        IntStream.range(0, 8).filter(i -> currentCombinedBits.get(i).isPresent())
                .mapToObj(i -> {
                    Integer groupVal = currentCombinedBits.get(i).get();
                    List<Integer> currentCombinedBitsIndex = IntStream.range(0, 8)
                            .filter(j -> currentCombinedBits.get(j).isPresent())
                            .filter(j -> currentCombinedBits.get(j).get().equals(groupVal))
                            .boxed().collect(Collectors.toList());
                    Set<Integer> removeSet = currentCombinedBitsIndex.stream()
                            .map(j -> currentBits.get(j).get())
                            .collect(Collectors.toSet());
                    Integer lastIndex = currentCombinedBitsIndex.get(currentCombinedBitsIndex.size()-1);
                    Integer retainGroup = currentBits.get(lastIndex).get();
                    removeSet.remove(retainGroup);
                    return removeSet;})
                .flatMap(Set::stream).collect(Collectors.toSet()).stream()
                .sorted(Comparator.reverseOrder())
                .forEach(i -> currentGroups.remove(i.intValue()));

        IntStream.range(0, currentGroups.size())
                .mapToObj(i -> { CoordSet group = currentGroups.get(i);
                    return IntStream.range(0, currentGroups.size())
                            .filter(j -> currentGroups.get(j).containsAnyCoordsIn(group))
                            .boxed().collect(Collectors.toSet());})
                .collect(Collectors.toSet()).stream()
                .map(s ->  s.stream().map(i -> currentGroups.get(i)).collect(Collectors.toList()))
                .collect(Collectors.toList())
                .forEach(l -> {
                    CoordSet retainSet = l.get(0);
                    l.subList(1, l.size()).forEach(s -> {
                        retainSet.addAll(s);
                        currentGroups.removeIf(g -> g==s);
                    });
                });
    }

    List<List<CoordSet>> combine(List<List<CoordSet>> prev, List<List<CoordSet>> current,
                                 Integer numCellsX, Integer numCellsY){
        Integer startX = current.size()-1;
        Integer startY = 0;
        Integer e = max(numCellsX,numCellsY)-1;

        Integer listStartH = 0;
        Integer listStartV = 1;
        Integer prevListStartH = 0;
        Integer prevListStartV = -1;

        Integer hScanLength = current.size()-1;
        Integer vScanLength = current.size()-1;

        if (current.size() < prev.size()) {
            startX = numCellsX - 1;
            startY = e-(current.size()-1);

            listStartH = 0;
            listStartV = 0;
            prevListStartH = 1;
            prevListStartV = 0;
            hScanLength = current.size();
            vScanLength = current.size();
        }

        if (numCellsY < e+1) {
            if (current.size() > prev.size()) {
                vScanLength = min(vScanLength, numCellsY-1);
            } else {
                vScanLength -= (e + 1 - numCellsY);
            }
        }

        Integer x = startX;
        Integer y = startY;

        Integer horizontal = 0;
        Integer vertical = 1;

        for (Integer i = 0; i<max(listStartH+hScanLength, listStartV+vScanLength); i++, x--, y++){
            if (i >= listStartH && i < (listStartH+hScanLength)) {
               List<CoordSet> cellGroup = current.get(i);
               List<CoordSet> prevCellGroup = prev.get(i + prevListStartH);
               combine(prevCellGroup, cellGroup, x, y, horizontal);
            }
            if (i >= listStartV && i < (listStartV+vScanLength)) {
                List<CoordSet> cellGroup = current.get(i);
                List<CoordSet> prevCellGroup = prev.get(i + prevListStartV);
                combine(prevCellGroup, cellGroup, x, y, vertical);
            }
       }

       return current;
    }


    List<CoordSet> extract(CoordSet set){
        CoordSet workingSet = set.getNumCellsX() < set.getNumCellsY() ? set.transpose(0) : set;
        Integer numCellsX = workingSet.getNumCellsX();
        Integer numCellsY = workingSet.getNumCellsY();

        List<CoordSet> extractedResult = IntStream.range(0, numCellsX + numCellsY - 1)
                .mapToObj(d -> {
                            Integer startX = min(d, numCellsX-1);
                            Integer e = max(numCellsX,numCellsY)-1;
                            Integer dte = e-d;
                            Integer numTimes = min(e - abs(dte) + 1, min(numCellsX, numCellsY)+ (dte < 0 ? dte : 0));
                            Integer length =  e - abs(dte) + 1 - numTimes;
                            List<List<CoordSet>> result = IntStream.iterate(startX, i->i-1)
                                    .limit(numTimes)
                                    .mapToObj(i -> extractGroupsAt(workingSet, i, d - i))
                                    .collect(Collectors.toList());
                            return extend(result, numCellsX, numCellsY, length);
                        })
                .reduce((a,b) -> combine(a, b, numCellsX, numCellsY))
                .get()
                .stream().flatMap(List::stream)
                .collect(Collectors.toList());

        if (set.getNumCellsX() < set.getNumCellsY()) {
            return extractedResult.stream().map(r -> r.transpose(1)).collect(Collectors.toList());
        } else {
            return extractedResult;
        }
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
