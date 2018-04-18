package test.gspann.storagescanner.results;

import java.io.File;

/**
 * Created by noah on 17/4/18.
 */

public class AverageFileCalculator implements ScanningListener {
    private int numberOfFiles = 0;
    private long totalFileSize = 0L;
    private ScanningResult scanningResult;

    public AverageFileCalculator(ScanningResult scanningResult) {
        this.scanningResult = scanningResult;
    }

    @Override
    public void onNewFile(File file) {
        numberOfFiles++;
        totalFileSize += file.length();
    }

    public long getAverageFileSize() {
        if (numberOfFiles == 0){
            return 0;
        } else {
            return totalFileSize / numberOfFiles;
        }
    }

    @Override
    public void onScanningSuccess() {
        scanningResult.setAverageFileSize(getAverageFileSize());
    }
}
