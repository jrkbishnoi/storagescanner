package test.gspann.storagescanner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import test.gspann.storagescanner.results.AverageFileCalculator;
import test.gspann.storagescanner.results.ExtensionFrequencyCalculator;
import test.gspann.storagescanner.results.ScanningListener;
import test.gspann.storagescanner.results.CurrentDirectoryTracker;
import test.gspann.storagescanner.results.CurrentDirectoryListener;
import test.gspann.storagescanner.results.ScanningCompletionListener;
import test.gspann.storagescanner.results.ScanningResult;
import test.gspann.storagescanner.results.ScanningResultListener;
import test.gspann.storagescanner.results.ScanningStatus;
import test.gspann.storagescanner.results.LargeFilesCalculator;

public class ScannerService extends Service {

    private static final String TAG = ScannerService.class.getSimpleName();
    private SBinder binder = new SBinder();
    private Thread scanningThread;
    private List<ScanningListener> scanningListeners;
    private ScanningResultListener scanningResultListener;
    private CurrentDirectoryListener currentDirectoryListener;
    private ScanningResult scanningResult;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public ScannerService() {
    }

    public class SBinder  extends Binder{
         ScannerService getService() {
            return ScannerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startScan(ScanningResultListener scanningResultListener,
                          CurrentDirectoryListener currentDirectoryListener) {
        this.currentDirectoryListener = currentDirectoryListener;
        Notification notification = getNotification(getString(R.string.scanning_sdcard));
        startForeground(1,notification);
        this.scanningResultListener = scanningResultListener;
        if (scanningThread != null) {
            return;
        }

        scanningResult = new ScanningResult();
        setUpNewFileListeners(scanningResult);
        scanningThread = new ScanningThread(new ScanningCompletionListener() {
            @Override
            public void onScanningComplete(final ScanningStatus scanningStatus) {
                 mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scanningCompleted(scanningStatus);
                    }
                });
            }
        }, scanningListeners);
        scanningThread.start();
    }

    private void scanningCompleted(ScanningStatus status) {
        stopForeground(true);
        if (scanningResultListener == null) {
            //discard results;
            scanningResult = null;
            return;
        }
        if (status.isCompleted()) {
            if (scanningListeners != null) {
                for (ScanningListener scanningListener : scanningListeners) {
                    scanningListener.onScanningSuccess();
                }
            }
        } else {
            scanningResult.setCancelled(true);
        }
        scanningThread = null;
        if (scanningResultListener != null) {
            scanningResultListener.onScanningResult(scanningResult);
        }
    }

    private  class ScanningThread extends Thread {
        List<ScanningListener> scanningListeners;
        ScanningCompletionListener completionListener;
        ScanningThread(ScanningCompletionListener completionListener, List<ScanningListener> scanningListeners) {
            this.completionListener = completionListener;
            this.scanningListeners = scanningListeners;
        }

        @Override
        public void run() {
            ScanningStatus status = new ScanningStatus();
            try {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                externalStorageDirectory.length();
                readFiles(externalStorageDirectory);
                status.setCompleted(true);
            } catch (InterruptedException ie) {
                status.setCancelled(true);
            } catch (Exception e) {
                status.setException(e);
            }
            completionListener.onScanningComplete(status);
        }
        private void readFiles(File dir) throws InterruptedException {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isInterrupted()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "readFiles: scanning thread interrupted");
                        }
                        throw new InterruptedException();
                    }
                    if (file.isDirectory()) {
                        readFiles(file);
                    } else {
                        newFileFound(file);
                    }
                }
            }
        }

        private void newFileFound(File file) {
            for (ScanningListener scanningListener : scanningListeners) {
                scanningListener.onNewFile(file);
            }
        }
    }

    public void stopScanning() {
        if (scanningThread != null && scanningThread.isAlive()) {
            scanningThread.interrupt();
        }
    }

    public void setScanningResultListener(ScanningResultListener scanningResultListener) {
        this.scanningResultListener = scanningResultListener;
    }

    public void setCurrentDirectoryListener(CurrentDirectoryListener currentDirectoryListener) {
        this.currentDirectoryListener = currentDirectoryListener;
    }

    public void setUpNewFileListeners(ScanningResult scanningResult) {
        scanningListeners = new ArrayList<>();
        scanningListeners.add(new CurrentDirectoryTracker(scanningResult, new CurrentDirectoryListener() {
            @Override
            public void updateCurrentDirectory(final String curDir) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (currentDirectoryListener != null){
                            currentDirectoryListener.updateCurrentDirectory(curDir);
                        }
                    }
                });
            }
        }));
        scanningListeners.add(new LargeFilesCalculator(scanningResult));
        scanningListeners.add(new AverageFileCalculator(scanningResult));
        scanningListeners.add(new ExtensionFrequencyCalculator(scanningResult));
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private Notification getNotification(String title) {
        String channelId = "scanning_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    channelId, getString(R.string.scanning_channel), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        return builder.build();
    }
}
