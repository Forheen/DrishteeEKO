package com.example.drishteeeko;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> rdServiceLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureButton = findViewById(R.id.capture_fingerprint);

        // Initialize the ActivityResultLauncher
        rdServiceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle successful fingerprint capture
                        Intent data = result.getData();
                        if (data != null) {
                            String pidData = data.getStringExtra("pidData");
                            if (pidData != null) {
                                // Replace characters as per your original code
                                pidData = pidData.replace("\"", "'").replace("\n", "");
                                Toast.makeText(this, "Capture successful: " + pidData, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // Handle failure or cancellation
                        Toast.makeText(this, "Fingerprint capture failed or canceled.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRDService();
                printInstalledPackages();
            }
        });
    }

    private void startRDService() {
        Intent intent = new Intent();
        intent.setAction("com.scl.rdservice.ACTION_CAPTURE"); // Action to start Morpho RD Service capture

        // Check if the Morpho RD Service app is installed
        if (isRDServiceAppInstalled()) {
            // Set extras for fingerprint capture
            intent.putExtra("timeout", 10000); // Timeout in milliseconds
            intent.putExtra("fCount", 1); // Finger count
            intent.putExtra("fType", 2); // Finger type
            intent.putExtra("iCount", 0);
            intent.putExtra("pCount", 0);
            intent.putExtra("format", 0);
            intent.putExtra("wadh", "E0jzJ/P8UopUHAieZn8CKqS4WPMi5ZSYXgfnlfkWjrc="); // Set WADH if needed
            intent.putExtra("pidVer", "2.0");
            intent.putExtra("posh", "UNKNOWN");
            intent.putExtra("env", "P");

            rdServiceLauncher.launch(intent); // Use the ActivityResultLauncher to start the service
        } else {
            Toast.makeText(this, "Morpho RD Service app is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isRDServiceAppInstalled() {
        try {
            // Attempt to get package info for the Morpho RD Service
            getPackageManager().getPackageInfo("com.scl.rdservice", PackageManager.GET_ACTIVITIES);
            Log.d("RDServiceCheck", "Morpho RD Service is installed.");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("RDServiceCheck", "Morpho RD Service is NOT installed.");
            return false;
        }
    }

    private void printInstalledPackages() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            Log.d("InstalledApp", "Package Name: " + app.packageName);
        }
    }
}
