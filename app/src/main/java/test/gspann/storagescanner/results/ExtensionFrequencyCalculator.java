package test.gspann.storagescanner.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by noah on 17/4/18.
 */

public class ExtensionFrequencyCalculator implements ScanningListener {
    Map<String,Counter> freqMap = new HashMap<>();
    private ScanningResult scanningResult;

    public ExtensionFrequencyCalculator(ScanningResult scanningResult) {
        this.scanningResult = scanningResult;
    }

    @Override
    public void onNewFile(File file) {
        String extension = getExtension(file);
        if (extension == null) return;
        Counter counter = freqMap.get(extension);
        if (counter == null) {
            freqMap.put(extension, new Counter());
        } else {
            counter.increment();
        }
    }

    private String getExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf(".");
        if (i != -1 && name.length() > i+1) {
            return name.substring(i);
        } else {
            return null;
        }
    }

    public List<ExtensionFrequency> getFrequentExtensions() {
        List<ExtensionFrequency> result = new ArrayList<>();
        Set<Map.Entry<String, Counter>> entries = freqMap.entrySet();
        List<Map.Entry<String, Counter>> list = new ArrayList<>(entries);
        Collections.sort(list, new Comparator<Map.Entry<String, Counter>>() {
            @Override
            public int compare(Map.Entry<String, Counter> o1, Map.Entry<String, Counter> o2) {
                if (o1.getValue().getValue() < o2.getValue().getValue() ) {
                    return 1;
                } else if (o1.getValue().getValue() > o2.getValue().getValue()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        Iterator<Map.Entry<String, Counter>> iterator = list.iterator();
        int i = 0;
        while (i < 5 && iterator.hasNext()){
            Map.Entry<String, Counter> entry = iterator.next();
            result.add(new ExtensionFrequency(entry.getKey(), entry.getValue().getValue()));
            i++;
        }
        return result;
    }

    @Override
    public void onScanningSuccess() {
        scanningResult.setFrequentExtensions(getFrequentExtensions());
    }

    private static class Counter {
        private int value = 1;

        public void increment() {
            value++;
        }

        public int getValue() {
            return value;
        }
    }
}
