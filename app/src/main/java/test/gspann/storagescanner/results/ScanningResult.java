package test.gspann.storagescanner.results;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by noah on 17/4/18.
 */

public class ScanningResult {
    private List<UserFile> largestFiles;
    private List<ExtensionFrequency> frequentExtensions;
    private long averageFileSize;
    private boolean isCancelled;

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public List<UserFile> getLargestFiles() {
        return largestFiles;
    }

    public void setLargestFiles(List<UserFile> largestFiles) {
        this.largestFiles = largestFiles;
    }

    public List<ExtensionFrequency> getFrequentExtensions() {
        return frequentExtensions;
    }

    public void setFrequentExtensions(List<ExtensionFrequency> frequentExtensions) {
        this.frequentExtensions = frequentExtensions;
    }

    public long getAverageFileSize() {
        return averageFileSize;
    }

    public void setAverageFileSize(long averageFileSize) {
        this.averageFileSize = averageFileSize;
    }
}
