package com.app.drawapp.console.window;


import com.app.drawapp.console.jna.NativeC;
import com.app.drawapp.render.CellValue;
import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

public class WindowsConsoleTypeMapper extends DefaultTypeMapper {

    public WindowsConsoleTypeMapper(){
        addTypeConverter(NativeC.NativeUnsignedShort.class, NativeC.getInstance().new NativeUnsignedShortConverter());
        addTypeConverter(Kernel32Native.Kernel32.CompositeChar.class, new CompositeCharConverter());
        addTypeConverter(CellValue.class, new CellValueConverter());
    }

    class BooleanConverter implements TypeConverter {
        @Override
        public Object toNative(Object value, ToNativeContext context) {
            return Integer.valueOf(Boolean.TRUE.equals(value) ? 1 : 0);
        }
        @Override
        public Object fromNative(Object value, FromNativeContext context) {
            return ((Integer)value).intValue() != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        @Override
        public Class<?> nativeType() {
            return Integer.class;
        }
    }

    /*class NativeUnsignedShortConverter implements TypeConverter{

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
    }*/

    class CellValueConverter implements TypeConverter{

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            if (nativeValue.getClass() == int.class) {
                int val = (int)nativeValue;
                CellValue unsignedNumber = new CellValue(4);
                unsignedNumber.setValue(val);
                return unsignedNumber;
            } else {
                long val = (long)nativeValue;
                CellValue unsignedNumber = new CellValue(8);
                unsignedNumber.setValue(val);
                return unsignedNumber;
            }
        }

        @Override
        public Object toNative(Object value, ToNativeContext context) {
            CellValue unsignedNumber = (CellValue) value;
            if (unsignedNumber.getSize() == 4) {
                return ((-1^(-1<<16)) & unsignedNumber.getInteger()) | ((-1 << 16) & unsignedNumber.getInteger());
            } else {
                return unsignedNumber.getLong();
            }
        }

        @Override
        public Class<?> nativeType() { return long.class; }
    }

    class CompositeCharConverter implements TypeConverter{

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context){
            return new Kernel32Native.Kernel32.CompositeChar((char)((short)nativeValue));
        }

        @Override
        public Object toNative(Object value, ToNativeContext context){
            return value == null ? (short)0 :(((Kernel32Native.Kernel32.CompositeChar)value).getValue());
        }

        @Override
        public Class<?> nativeType() { return short.class; }
    }


}
