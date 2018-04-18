package test.gspann.storagescanner;

/**
 * Created by noah on 18/4/18.
 */

public class Util {

    public static String getFileSizeString(long bytes) {
        if (bytes < 1024) {
            return bytes + "b";
        }
        double kb = bytes / 1024;
        if (kb < 1024)
            return doubleToString(kb) +"Kb";
        double mb = kb / 1024;
        if (mb < 1024)
            return doubleToString(mb) +"Mb";
        double gb = mb / 1024;
        return doubleToString(gb) +"Gb";
    }

    public static String doubleToString(double value) {
        return String.format("%.2f", value);
    }
}
