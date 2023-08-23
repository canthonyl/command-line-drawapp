package com.app.drawapp.console.jna;

import com.sun.jna.*;

import java.lang.reflect.*;
import java.nio.Buffer;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class NativeC {

    private final Predicate<Field> structureTypes;
    private final Predicate<Field> nonStructureTypes;
    private final Predicate<Field> checkCallbacks;
    private final Predicate<Field> checkFinalFields;
    private final Predicate<Field> isPublic;
    private final Predicate<Field> isArrayType;
    private final Predicate<Field> isInstanceField;

    private final Predicate<Class> isPrimitiveOrBoxed;
    private final Predicate<Class> validCallbacks;
    private final Predicate<Class> validPointers;
    private final Predicate<Class> validBuffers;
    private final Predicate<Class> validStringTypes;
    private final Predicate<Class> validStructureByRefTypes;
    private final Predicate<Class> usePointerSize;

    private final TypeMapper mapper;

    public NativeC(){

        //mapper = Native.getTypeMapper(Kernel32Native.Kernel32.class);
        DefaultTypeMapper mapper = new DefaultTypeMapper();
        mapper.addTypeConverter(NativeUnsignedShort.class, new NativeUnsignedShortConverter());
        this.mapper = mapper;

        structureTypes = f -> Structure.class.isAssignableFrom(f.getType());
        checkCallbacks = f -> Callback.class.isAssignableFrom(f.getType()) && !f.getType().isInterface();
        validCallbacks = c -> Callback.class.isAssignableFrom(c) && c.isInterface();
        checkFinalFields = f -> Modifier.isFinal(f.getModifiers()) && !Platform.RO_FIELDS ;
        isPublic = f -> Modifier.isPublic(f.getModifiers());
        isArrayType = f -> f.getType().isArray();
        isInstanceField = f -> !Modifier.isStatic(f.getModifiers());

        isPrimitiveOrBoxed = c -> c.isPrimitive()
                || c == Long.class || c == Integer.class || c == Short.class || c == Byte.class
                || c == Double.class || c == Float.class || c == Boolean.class || c == Character.class;

        validPointers = c -> Pointer.class.isAssignableFrom(c) && !Function.class.isAssignableFrom(c);
        validBuffers = c -> Buffer.class.isAssignableFrom(c) && Platform.HAS_BUFFERS;

        validStringTypes = c -> c == String.class || c == WString.class;
        validStructureByRefTypes = c -> Structure.ByReference.class.isAssignableFrom(c);
        usePointerSize = validPointers.and(validBuffers).and(validStringTypes).and(validCallbacks).and(validStructureByRefTypes);
        nonStructureTypes = f -> !Structure.class.isAssignableFrom(f.getType());
    }

    public <T extends Structure> Integer calculateStructureSize(Class<T> clazz) {
        List<List<Field>> allFields  = Stream.concat(getFieldList(clazz).stream().filter(structureTypes).map(Field::getType).map(c -> this.getFieldList(c)),
                        Stream.of(getFieldList(clazz).stream().filter(nonStructureTypes).collect(toList())))
                        .collect(toList());

        if (allFields.stream().anyMatch(l -> fieldListAnyMatch(l, isArrayType))) {
            throw new IllegalArgumentException("Nested Structure arrays must use a derived Structure type so that the size of the elements can be determined");
        }

        if (allFields.stream().anyMatch(l -> fieldListAnyMatch(l, checkCallbacks))) {
            throw new IllegalArgumentException("Structure Callback must be an interface");
        }

        if (allFields.stream().anyMatch(l -> fieldListAnyMatch(l, checkFinalFields))) {
            throw new IllegalArgumentException("This VM does not support read-only fields within " + clazz + ")");
        }

        List<List<Class<?>>> list = allFields.stream().map(this::toClass).collect(toList());

        Integer totalCalculatedSize = 0;
        Integer maxSize = 0;

        for (List<Class<?>> fields : list) {
            Integer structCalculatedSize = 0;
            Integer workingBlockSize = 0;
            Integer workingBlock = 0;

            for (Class<?> fieldClazz : fields){
                Integer size;

                if (isPrimitiveOrBoxed.test(fieldClazz)) {
                    size = Native.getNativeSize(fieldClazz);
                } else if (usePointerSize.test(fieldClazz)) {
                    size = Native.POINTER_SIZE;
                } else {
                    ToNativeConverter nc = mapper.getToNativeConverter(fieldClazz);
                    Class<?> nativeType = nc.nativeType();
                    size = Native.getNativeSize(nativeType);
                }
                maxSize = max(maxSize, size);

                if (size > workingBlockSize) {
                    structCalculatedSize = (max(structCalculatedSize / size, min(1, structCalculatedSize)) + 1) * size;
                    workingBlockSize = size;
                    workingBlock = size;
                } else if (workingBlock == 0 || workingBlock < size) {
                    workingBlock = workingBlockSize;
                    structCalculatedSize += workingBlockSize;
                }
                workingBlock -= size;
            }

            totalCalculatedSize += structCalculatedSize;
        }

        totalCalculatedSize = maxSize * ((totalCalculatedSize/maxSize) + min(1,(totalCalculatedSize-(totalCalculatedSize/maxSize)*maxSize)));

        return totalCalculatedSize;
    }

    private List<Field> getFieldList(Class targetClass) {
        List<Field> fieldList = new ArrayList<>();
        for (Class<?> clazz = targetClass; !clazz.equals(Structure.class); clazz = clazz.getSuperclass()) {
            List<Field> clazzFields = Arrays.stream(clazz.getDeclaredFields()).filter(isPublic.and(isInstanceField)).collect(toList());
            fieldList.addAll(clazzFields);
        }
        return fieldList;
    }

    private List<Class<?>> toClass(List<Field> fieldList){
        return fieldList.stream().map(Field::getType).collect(toList());
    }

    private Boolean fieldListAnyMatch(List<Field> list, Predicate<Field> checkField){
        return list.stream().anyMatch(checkField);
    }

    static { Native.register(Platform.C_LIBRARY_NAME); }

    public static native long malloc(long size);

    public static native long calloc(long num, long size);

    private static NativeC instance = getInstance();

    public static NativeC getInstance(){ return instance == null ? new NativeC() : instance; }

    public static class NativeUnsignedShort {
        private short nativeVal;

        public NativeUnsignedShort() { }
        public NativeUnsignedShort(byte val) { setValue(val); }
        public NativeUnsignedShort(char val) { setValue(val); }
        public NativeUnsignedShort(short val) { setValue(val); }

        public void setValue(byte val){ nativeVal = (short)(val & 0x00FF); }
        public void setValue(char val){ nativeVal = (short)(val); }
        public void setValue(short val){ nativeVal = val; }

        public short getValue() { return nativeVal; }

    }

    public class NativeUnsignedShortConverter implements TypeConverter{

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return new NativeC.NativeUnsignedShort((short)nativeValue);
        }

        @Override
        public Object toNative(Object value, ToNativeContext context) {
            return value == null ? (short)0 : ((NativeC.NativeUnsignedShort)value).getValue();
        }

        @Override
        public Class<?> nativeType() { return short.class; }
    }

}
