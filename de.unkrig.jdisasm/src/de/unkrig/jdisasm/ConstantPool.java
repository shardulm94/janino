
package de.unkrig.jdisasm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConstantPool {

    public interface ConstantPoolEntry {
    }

    public static class ConstantClassInfo implements ConstantPoolEntry {
        public String name;
    }
    public static class ConstantFieldrefInfo implements ConstantPoolEntry {
        public ConstantClassInfo       clasS;
        public ConstantNameAndTypeInfo nameAndType;
    }
    public static class ConstantMethodrefInfo implements ConstantPoolEntry {
        public ConstantClassInfo       clasS;
        public ConstantNameAndTypeInfo nameAndType;
    }
    public static class ConstantInterfaceMethodrefInfo implements ConstantPoolEntry {
        public ConstantClassInfo       clasS;
        public ConstantNameAndTypeInfo nameAndType;
    }
    public static class ConstantStringInfo implements ConstantPoolEntry {
        public String string;
    }
    public static class ConstantIntegerInfo implements ConstantPoolEntry {
        public int bytes;
    }
    public static class ConstantFloatInfo implements ConstantPoolEntry {
        public float bytes;
    }
    public static class ConstantLongInfo implements ConstantPoolEntry {
        public long bytes;
    }
    public static class ConstantDoubleInfo implements ConstantPoolEntry {
        public double bytes;
    }
    public static class ConstantNameAndTypeInfo implements ConstantPoolEntry {
        public ConstantUtf8Info name;
        public ConstantUtf8Info descriptor;
    }
    public static class ConstantUtf8Info implements ConstantPoolEntry {
        public String bytes;
    }

    private final ConstantPoolEntry[] entries;

    /**
     * Reads a constant pool from the given {@link InputStream}. Afterwards, entries can be retrieved by invoking
     * the {@code getConstant*Info()} method family.
     *
     * @throws ClassCastException             An entry has the "wrong" type
     * @throws NullPointerException           An "unusable" entry (the magic "zero" entry and the entries after a LONG
     *                                        or DOUBLE entry) is referenced
     * @throws ArrayIndexOutOfBoundsException An index is too small or to great
     */
    public ConstantPool(DataInputStream dis) throws IOException {
        short count = dis.readShort();

        // Read the entries into a temporary data structure - this is necessary because there may be forward
        // references.
        abstract class RawEntry {
            abstract ConstantPoolEntry cook();
            ConstantClassInfo       getConstantClassInfo(short index)       { return (ConstantClassInfo)       get(index); }
            ConstantNameAndTypeInfo getConstantNameAndTypeInfo(short index) { return (ConstantNameAndTypeInfo) get(index); }
            ConstantUtf8Info        getConstantUtf8Info(short index)        { return (ConstantUtf8Info)        get(index); }
            abstract ConstantPoolEntry get(short index);
        }
        final RawEntry[] rawEntries = new RawEntry[count];

        abstract class RawEntry2 extends RawEntry {
            ConstantPoolEntry get(short index) {
                if (entries[index] == null) {
                    entries[index] = new ConstantPoolEntry() {}; // To prevent recursion.
                    entries[index] = rawEntries[index].cook();
                }
                return entries[index];
            }
        }

        for (short i = 1; i < count;) {
            int idx = i;
            RawEntry re;
            byte tag = dis.readByte();
            switch (tag) {
            case 7: // CONSTANT_Class_info
                {
                    final short nameIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantClassInfo() {{
                                name = getConstantUtf8Info(nameIndex).bytes.replace('/', '.');
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 9: // CONSTANT_Fieldref_info
                {
                    final short classIndex = dis.readShort();
                    final short nameAndTypeIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantFieldrefInfo() {{
                                clasS = getConstantClassInfo(classIndex);
                                nameAndType = getConstantNameAndTypeInfo(nameAndTypeIndex);
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 10: // CONSTANT_Methodref_info
                {
                    final short classIndex = dis.readShort();
                    final short nameAndTypeIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantMethodrefInfo() {{
                                clasS = getConstantClassInfo(classIndex);
                                nameAndType = getConstantNameAndTypeInfo(nameAndTypeIndex);
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 11: // CONSTANT_InterfaceMethodref_info
                {
                    final short classIndex = dis.readShort();
                    final short nameAndTypeIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantInterfaceMethodrefInfo() {{
                                clasS = getConstantClassInfo(classIndex);
                                nameAndType = getConstantNameAndTypeInfo(nameAndTypeIndex);
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 8: // CONSTANT_String_info
                {
                    final short stringIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantStringInfo() {{
                                string = getConstantUtf8Info(stringIndex).bytes;
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 3: // CONSTANT_Integer_info
                {
                    final int byteS = dis.readInt();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantIntegerInfo() {{
                                bytes = byteS;
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 4: // CONSTANT_Float_info
                {
                    final float byteS = dis.readFloat();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantFloatInfo() {{
                                bytes = byteS;
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 5: // CONSTANT_Long_info
                {
                    final long byteS = dis.readLong();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantLongInfo() {{
                                bytes = byteS;
                            }};
                        }
                    };
                    i += 2;
                    break;
                }
            case 6: // CONSTANT_Double_info
                {
                    final double byteS = dis.readDouble();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantDoubleInfo() {{
                                bytes = byteS;
                            }};
                        }
                    };
                    i += 2;
                    break;
                }
            case 12: // CONSTANT_NameAndType_info
                {
                    final short nameIndex = dis.readShort();
                    final short descriptorIndex = dis.readShort();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantNameAndTypeInfo() {{
                                name = getConstantUtf8Info(nameIndex);
                                descriptor = getConstantUtf8Info(descriptorIndex);
                            }};
                        }
                    };
                    i++;
                    break;
                }
            case 1: // CONSTANT_Utf8_info
                {
                    final String byteS = dis.readUTF();
                    re = new RawEntry2() {
                        ConstantPoolEntry cook() {
                            return new ConstantUtf8Info() {{
                                bytes = byteS;
                            }};
                        }
                    };
                    i++;
                    break;
                }
            default:
                throw new RuntimeException("Invalid cp_info tag \"" + (int) tag + "\"");
            }
            rawEntries[idx] = re;
        }

        entries = new ConstantPoolEntry[count];
        for (int i = 0; i < count; ++i) {
            if (entries[i] == null && rawEntries[i] != null) entries[i] = rawEntries[i].cook();
        }
    }

    public ConstantPoolEntry              get(short index)                               { return entries[index]; }
    public ConstantClassInfo              getConstantClassInfo(short index)              { return (ConstantClassInfo) entries[index]; }
    public ConstantFieldrefInfo           getConstantFieldrefInfo(short index)           { return (ConstantFieldrefInfo) entries[index]; }
    public ConstantMethodrefInfo          getConstantMethodrefInfo(short index)          { return (ConstantMethodrefInfo) entries[index]; }
    public ConstantInterfaceMethodrefInfo getConstantInterfaceMethodrefInfo(short index) { return (ConstantInterfaceMethodrefInfo) entries[index]; }
    public ConstantStringInfo             getConstantStringInfo(short index)             { return (ConstantStringInfo) entries[index]; }
    public ConstantIntegerInfo            getConstantIntegerInfo(short index)            { return (ConstantIntegerInfo) entries[index]; }
    public ConstantFloatInfo              getConstantFloatInfo(short index)              { return (ConstantFloatInfo) entries[index]; }
    public ConstantLongInfo               getConstantLongInfo(short index)               { return (ConstantLongInfo) entries[index]; }
    public ConstantDoubleInfo             getConstantDoubleInfo(short index)             { return (ConstantDoubleInfo) entries[index]; }
    public ConstantNameAndTypeInfo        getConstantNameAndTypeInfo(short index)        { return (ConstantNameAndTypeInfo) entries[index]; }
    public ConstantUtf8Info               getConstantUtf8Info(short index)               { return (ConstantUtf8Info) entries[index]; }

    /**
     * Checks that the indexed constant pool entry is of type {@code CONSTANT_(Integer|Float|Class|String)_info}, and
     * returns its value converted to {@link String}.
     */
    public String getIntegerFloatClassString(short index) {
        ConstantPoolEntry e = entries[index];
        if (e instanceof ConstantIntegerInfo) return Integer.toString(((ConstantIntegerInfo) e).bytes);
        if (e instanceof ConstantFloatInfo) return Float.toString(((ConstantFloatInfo) e).bytes);
        if (e instanceof ConstantClassInfo) return ((ConstantClassInfo) e).name;
        if (e instanceof ConstantStringInfo) return stringToJavaLiteral(((ConstantStringInfo) e).string);
        throw new ClassCastException(e.getClass().getName());
    }

    /**
     * Checks that the indexed constant pool entry is of type {@code CONSTANT_(Integer|Float|Long|Double|String)_info},
     * and returns its value converted to {@link String}.
     */
    public String getIntegerFloatLongDoubleString(short index) {
        ConstantPoolEntry e = entries[index];
        if (e instanceof ConstantIntegerInfo) return Integer.toString(((ConstantIntegerInfo) e).bytes);
        if (e instanceof ConstantFloatInfo) return Float.toString(((ConstantFloatInfo) e).bytes);
        if (e instanceof ConstantLongInfo) return Long.toString(((ConstantLongInfo) e).bytes);
        if (e instanceof ConstantDoubleInfo) return Double.toString(((ConstantDoubleInfo) e).bytes);
        if (e instanceof ConstantStringInfo) return stringToJavaLiteral(((ConstantStringInfo) e).string);
        throw new ClassCastException(e.getClass().getName());
    }
    
    /**
     * Checks that the indexed constant pool entry is of type {@code CONSTANT_(Long|Double|String)_info}, and returns
     * its value converted to {@link String}.
     */
    public String getLongDoubleString(short index) {
        ConstantPoolEntry e = entries[index];
        if (e instanceof ConstantLongInfo) return Long.toString(((ConstantLongInfo) e).bytes) + 'L';
        if (e instanceof ConstantDoubleInfo) return Double.toString(((ConstantDoubleInfo) e).bytes) + 'D';
        throw new ClassCastException(e.getClass().getName());
    }
    
    /**
     * Checks that the indexed constant pool entry is of type {@code CONSTANT_(Integer|Float|Long|Double)_info}, and
     * returns its value converted to {@link String}.
     */
    public String getIntegerFloatLongDouble(short index) {
        ConstantPoolEntry e = entries[index];
        if (e instanceof ConstantIntegerInfo) return Integer.toString(((ConstantIntegerInfo) e).bytes);
        if (e instanceof ConstantFloatInfo) return Float.toString(((ConstantFloatInfo) e).bytes);
        if (e instanceof ConstantLongInfo) return Long.toString(((ConstantLongInfo) e).bytes);
        if (e instanceof ConstantDoubleInfo) return Double.toString(((ConstantDoubleInfo) e).bytes);
        throw new ClassCastException(e.getClass().getName());
    }

    public static String stringToJavaLiteral(String s) {
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            int idx = "\r\n\"\t\b".indexOf(c);
            if (idx == -1) {
                ++i;
            } else {
                s = s.substring(0, i) + '\\' + "rn\"tb".charAt(idx) + s.substring(i + 1);
                i += 2;
            }
            if (i >= 80) break;
        }
        return '"' + s + '"';
    }
}