package test.gspann.storagescanner.results;

import java.io.File;

/**
 * Created by noah on 17/4/18.
 */

public interface ScanningListener {
    void onNewFile(File file);
    void onScanningSuccess();
}
