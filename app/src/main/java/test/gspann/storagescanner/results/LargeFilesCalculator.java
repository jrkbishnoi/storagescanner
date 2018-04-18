package test.gspann.storagescanner.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by noah on 17/4/18.
 */

public class LargeFilesCalculator implements ScanningListener {
    private List<UserFile> topFiles = new ArrayList<UserFile>() {
        @Override
        public boolean add(UserFile file) {
            int index = Collections.binarySearch(this, file, new Comparator<UserFile>() {
                @Override
                public int compare(UserFile o1, UserFile o2) {
                    if (o1.getSize() > o2.getSize()) {
                        return -1;
                    } else if (o1.getSize() < o2.getSize()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            if (index < 0) index = ~index;
            super.add(index, file);
            if(size() > 10) {
                remove(10);
            }
            return true;
        }
    };
    private ScanningResult scanningResult;

    public LargeFilesCalculator(ScanningResult scanningResult) {
        this.scanningResult = scanningResult;
    }
    @Override
    public void onNewFile(File file) {
        UserFile userFile = new UserFile();
        userFile.setName(file.getName());
        userFile.setSize(file.length());
        topFiles.add(userFile);
    }

    public List<UserFile> getTopFiles() {
        return topFiles;
    }

    @Override
    public void onScanningSuccess() {
        scanningResult.setLargestFiles(getTopFiles());
    }
}
