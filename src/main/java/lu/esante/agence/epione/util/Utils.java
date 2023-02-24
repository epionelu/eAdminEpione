package lu.esante.agence.epione.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.datatype.XMLGregorianCalendar;

public class Utils {

    private Utils() {
        throw new IllegalStateException();
    }

    public static byte[] getBytes(Byte[] byteObjects) {
        byte[] bytes = new byte[byteObjects.length];
        int j = 0;
        // Unboxing Byte values. (Byte[] to byte[])
        for (Byte b : byteObjects)
            bytes[j++] = b.byteValue();
        return bytes;
    }

    public static Byte[] getBytes(byte[] bytePrimitive) {
        Byte[] byteObjects = new Byte[bytePrimitive.length];

        int i = 0;
        // Associating Byte array values with bytes. (byte[] to Byte[])
        for (byte b : bytePrimitive)
            byteObjects[i++] = b; // Autoboxing.
        return byteObjects;
    }

    public static OffsetDateTime offsetDateTimeFromXmlGregorianCalendar(XMLGregorianCalendar input) {
        String s = input.toGregorianCalendar()
                .toZonedDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return OffsetDateTime.parse(s);
    }

}
