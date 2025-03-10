package com.example.drishteeeko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<Intent> deviceInfoResultLauncher;
    private ActivityResultLauncher<Intent> captureResultLauncher;
    private EditText resultTextView;

    // Variables to hold the extracted meta details
    private String dpId, rdsId, rdsVer, dc, mi, mc,ci;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureButton = findViewById(R.id.capture_fingerprint);
        Button deviceInfoButton = findViewById(R.id.device_info_button);
        resultTextView = findViewById(R.id.result_text_view);

        // Debugging
        Log.d(TAG, "onCreate: App Started");

        // Initialize device info launcher
        deviceInfoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            for (String key : extras.keySet()) {
                                Object value = extras.get(key);
                                showResult(TAG+String.format("Extras: Key=%s, Value=%s", key, value));
                            }
                        } else {
                            showResult(TAG+"No extras found in the intent");
                        }

                        // Retrieve device info XML
 //                       String deviceInfoXml = result.getData().toString();
//                        Log.d(TAG, "Device Info Retrieved: " + deviceInfoXml);
 //                       showResult("Device Info Retrieved: " + deviceInfoXml);

                        // Parse the XML and extract meta details
                       // parseDeviceInfoXml(deviceInfoXml);

                        // Start biometric capture after device info retrieval
                    //  captureBiometricData();
                    } else {
                        Log.e(TAG, "Failed to retrieve Device Info");
                        showResult("Failed to retrieve Device Info");
                    }
                });

        // Initialize capture result launcher
        captureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String pidData = result.getData().getStringExtra("pidData");
                        Log.d(TAG, "Capture Success: " + pidData);
                        showResult("Capture Success: " + pidData);
                    } else {
                        Log.e(TAG, "Capture failed or returned no data");
                        showResult("Capture failed or returned no data");
                    }
                });

        // Device Info Button Click
        deviceInfoButton.setOnClickListener(v -> {
            Log.d(TAG, "Device Info Button clicked");

            Intent intent = new Intent("in.gov.uidai.rdservice.fp.INFO");
            if (intent.resolveActivity(getPackageManager()) != null) {
                deviceInfoResultLauncher.launch(intent);
            } else {
                Log.e(TAG, "RD Service INFO not available");
                showResult("RD Service INFO not available");
            }
        });

        // Capture Button Click
        captureButton.setOnClickListener(v -> {
            Log.d(TAG, "Capture Button clicked");

            // First retrieve device info
            Intent infoIntent = new Intent("in.gov.uidai.rdservice.fp.INFO");
            if (infoIntent.resolveActivity(getPackageManager()) != null) {
                deviceInfoResultLauncher.launch(infoIntent);
            } else {
                Log.e(TAG, "RD Service INFO not available for Capture");
                showResult("RD Service INFO not available for Capture");
            }
        });
    }

    // Capture Biometric Data
    private void captureBiometricData() {
        Intent captureIntent = new Intent("in.gov.uidai.rdservice.fp.CAPTURE");

        // Add required attributes for biometric capture
        captureIntent.putExtra("ver", "1.0");
        captureIntent.putExtra("fCount", "1"); // Number of fingers
        captureIntent.putExtra("fType", "0"); // Finger type (e.g., left/right hand)
        captureIntent.putExtra("iCount", "0"); // Iris count (if used)
        captureIntent.putExtra("iType", "0"); // Iris type (if used)
        captureIntent.putExtra("pCount", "0"); // Palm count (if used)
        captureIntent.putExtra("pType", "0"); // Palm type (if used)
        captureIntent.putExtra("format", "0"); // Data format (XML = 0)
        captureIntent.putExtra("posh", ""); // Position hint (optional)
        captureIntent.putExtra("timeout", "10000"); // Timeout in milliseconds
        captureIntent.putExtra("wadh", ""); // WADH (if required)
        captureIntent.putExtra("otp", ""); // OTP (if required)
        captureIntent.putExtra("pidVer", "2.0"); // PID version

//        // Use parsed meta details
        captureIntent.putExtra("dpId", dpId);
        captureIntent.putExtra("rdsId", rdsId);
        captureIntent.putExtra("rdsVer", rdsVer);
        captureIntent.putExtra("dc", dc);
        captureIntent.putExtra("mi", mi);
        captureIntent.putExtra("mc", mc);
        captureIntent.putExtra("ci", ci);

        showResult("BiometricCapture"+ "Sending intent: " + captureIntent.toUri(0));
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            captureResultLauncher.launch(captureIntent); // Launch capture intent
        } else {
            Log.e(TAG, "RD Service CAPTURE not available");
            showResult("RD Service CAPTURE not available");
        }
    }

    // Method to parse device info XML and extract Meta details
    private void parseDeviceInfoXml(String xml) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("DeviceInfo")) {
                    dpId = parser.getAttributeValue(null, "dpId");
                    rdsId = parser.getAttributeValue(null, "rdsId");
                    rdsVer = parser.getAttributeValue(null, "rdsVer");
                    dc = parser.getAttributeValue(null, "dc");
                    mi = parser.getAttributeValue(null, "mi");
                    mc = parser.getAttributeValue(null, "mc");
                    ci = parser.getAttributeValue(null, "ci");

                    Log.d(TAG, "Meta Details Parsed: dpId=" + dpId + ", rdsId=" + rdsId + ", rdsVer=" + rdsVer + ", dc=" + dc + ", mi=" + mi + ", mc=" + mc);
                    showResult("Meta Details Parsed Successfully");
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing DeviceInfo XML", e);
            showResult("Error parsing DeviceInfo XML");
        }
    }

    // Show result in UI
    private void showResult(final String message) {
        runOnUiThread(() -> resultTextView.setText(message));
    }
}
