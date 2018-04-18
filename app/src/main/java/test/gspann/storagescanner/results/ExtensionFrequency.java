package test.gspann.storagescanner.results;

/**
 * Created by noah on 18/4/18.
 */

public class ExtensionFrequency {
    private String extension;
    private int frequency;

    public ExtensionFrequency(String extension, int frequency) {
        this.extension = extension;
        this.frequency = frequency;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
