package test.gspann.storagescanner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import test.gspann.storagescanner.results.CurrentDirectoryListener;
import test.gspann.storagescanner.results.ExtensionFrequency;
import test.gspann.storagescanner.results.ScanningResult;
import test.gspann.storagescanner.results.ScanningResultListener;
import test.gspann.storagescanner.results.UserFile;
import test.gspann.storagescanner.ui.FrequentExtensionRecyclerViewAdapter;
import test.gspann.storagescanner.ui.TopFilesRecyclerViewAdapter;


public class ScannerFragment extends Fragment implements ScanningResultListener, CurrentDirectoryListener{

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private Button scanButton;
    private TextView tvScanningStatus;
    private View rlScanningResults;
    private TextView tvAvgFileSize;
    private RecyclerView rcTopFiles;
    private RecyclerView rcExtensionFrequencies;
    private ScannerService scannerService;
    private ServiceConnection connection;
    private boolean isScanning;
    private ScanningResult scanningResult;
    private ShareActionProvider shareActionProvider;
    boolean visible;

    public ScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        visible = true;
        scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    stopScan();
                    scanButton.setText(R.string.start_scan);
                } else {
                    checkPermissionAndStartScan();
                    scanButton.setText(R.string.stop_scan);
                }
            }
        });
        tvScanningStatus = view.findViewById(R.id.tvScanningStatus);
        rlScanningResults= view.findViewById(R.id.rlScanningResults);
        tvAvgFileSize= view.findViewById(R.id.tvAverageFileSize);
        rcTopFiles= view.findViewById(R.id.rcLargestFiles);
        rcTopFiles.setNestedScrollingEnabled(false);
        rcExtensionFrequencies= view.findViewById(R.id.rcFrequentExtensions);
        rcExtensionFrequencies.setNestedScrollingEnabled(false);
        if (scanningResult != null && !scanningResult.isCancelled()) {
            showScanningResult();
        } else {
            rlScanningResults.setVisibility(View.GONE);
        }
        if (isScanning) {
            scanButton.setText(R.string.stop_scan);
            scannerService.setCurrentDirectoryListener(this);
        } else {
            scanButton.setText(R.string.start_scan);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share_details, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void stopScan() {
        isScanning = false;
        if (scannerService != null) {
            scannerService.stopScanning();
        }
    }

    private void checkPermissionAndStartScan() {
        if (ensurePermission(REQUEST_CODE_STORAGE_PERMISSION)) {
            startScan();
        }
    }

    private void startScan() {
        setShareIntent(new Intent());
        rlScanningResults.setVisibility(View.GONE);
        scanningResult = null;
        if (scannerService != null) {
            isScanning = true;
            scannerService.startScan(this, this);
        } else {
            Intent intent = new Intent(getContext(), ScannerService.class);
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ScannerService.SBinder binder = (ScannerService.SBinder) service;
                    scannerService = binder.getService();
                    scannerService.startScan(ScannerFragment.this, ScannerFragment.this);
                    isScanning = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "onServiceDisconnected: ");
                    scannerService = null;
                }
            };
            getContext().getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        visible = false;
        if (scannerService != null) {
            scannerService.setCurrentDirectoryListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            getContext().getApplicationContext().unbindService(connection);
            connection = null;
        }
        if (scannerService != null) {
            scannerService.setScanningResultListener(null);
            scannerService.stopScanning();
        }
        scannerService = null;
    }

    @Override
    public void updateCurrentDirectory(String curDir) {
        if (!visible) return;
        tvScanningStatus.setVisibility(View.VISIBLE);
        tvScanningStatus.setText(getString(R.string.scanning_dir,curDir));
    }

    @Override
    public void onScanningResult(ScanningResult scanningResult) {
        isScanning = false;
        if (!scanningResult.isCancelled()) {
            this.scanningResult = scanningResult;
            showScanningResult();
        } else {
            scanningStopped();
        }
    }

    private void showScanningResult() {
        if (!visible) return;
        updateShareIntent();
        scanButton.setText(R.string.start_scan);
        tvScanningStatus.setVisibility(View.GONE);
        rlScanningResults.setVisibility(View.VISIBLE);
        long averageFileSize = scanningResult.getAverageFileSize();
        tvAvgFileSize.setText(getString(R.string.average_file_size, Util.getFileSizeString(averageFileSize)));
        List<UserFile> largestFiles = scanningResult.getLargestFiles();
        rcTopFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        rcTopFiles.setAdapter(new TopFilesRecyclerViewAdapter(largestFiles));
        List<ExtensionFrequency>  frequentExtensions= scanningResult.getFrequentExtensions();
        rcExtensionFrequencies.setLayoutManager(new LinearLayoutManager(getContext()));
        rcExtensionFrequencies.setAdapter(new FrequentExtensionRecyclerViewAdapter(frequentExtensions));
        Iterator<ExtensionFrequency> iterator = frequentExtensions.iterator();
    }

    private void updateShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResultString());
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);
    }

    private String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.average_file_size,
                Util.getFileSizeString(scanningResult.getAverageFileSize()))).append("\n");
        sb.append("Top Files").append("\n");
        List<UserFile> largestFiles = scanningResult.getLargestFiles();
        for (UserFile largestFile : largestFiles) {
            sb.append(largestFile.getName()).append(" ")
                    .append(Util.getFileSizeString(largestFile.getSize())).append("\n");
        }
        sb.append("Frequent Extensions ").append("\n");
        List<ExtensionFrequency> frequentExtensions = scanningResult.getFrequentExtensions();
        for (ExtensionFrequency frequency : frequentExtensions) {
            sb.append(frequency.getExtension()).append(" ")
                    .append(frequency.getFrequency()).append("\n");
        }
        return sb.toString();
    }

    private void scanningStopped() {
        if (!visible) return;
        rlScanningResults.setVisibility(View.GONE);
        tvScanningStatus.setVisibility(View.GONE);
    }

    private boolean ensurePermission(int requestCode) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();

                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onRequestPermissionsResult: ext storage read permission denied");
                    }
                    Toast.makeText(getContext(), R.string.permission_required_for_scan, Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
        }
    }
}
