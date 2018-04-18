package test.gspann.storagescanner.results;

import java.io.File;

/**
 * Created by noah on 17/4/18.
 */

public class CurrentDirectoryTracker implements ScanningListener {
    private String currentDir;
    private CurrentDirectoryListener listener;
    private ScanningResult scanningResult;

    public CurrentDirectoryTracker(ScanningResult scanningResult, CurrentDirectoryListener listener) {
        this.listener = listener;
        this.scanningResult = scanningResult;
    }

    @Override
    public void onNewFile(File file) {
        String dir;
        if (file.getParent() != null) {
            dir  = file.getParent();
        } else {
            dir = file.getName();
        }
        if (currentDir == null || !currentDir.equals(dir)) {
            currentDir = dir;
            listener.updateCurrentDirectory(currentDir);
        }
    }

    @Override
    public void onScanningSuccess() {

    }
}
