package com.example.first_androidstudio;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{
    private int STORAGE_PERMISSIONS_CODE = 1;
    private String username;
    // Text Fields
    private TextView textViewUsername;
    private EditText editTextWidth;
    private EditText editTextLength;
    private EditText editTextHeight;

    private Button buttonSend;
    private Button buttonShare;
    private Button buttonClearAll;

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    private String dateStr = formatter.format(date);
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Some code to prevent the app from crashing with the Share feature
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        // Request READ_EXTERNAL_STORAGE permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        username = getIntent().getStringExtra("username");

        // Initialize text fields
        textViewUsername = findViewById(R.id.textViewUsername);
        editTextWidth = findViewById(R.id.editTextWidth);
        editTextLength = findViewById(R.id.editTextLength);
        editTextHeight = findViewById(R.id.editTextHeight);

        // Set username text
        textViewUsername.setText(username);

        // Initialize buttons
        buttonSend = findViewById(R.id.downloadButton);
        buttonShare = findViewById(R.id.shareButton);
        buttonClearAll = findViewById(R.id.clearAllButton);

//        buttonSend.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                if(ContextCompat.checkSelfPermission(MainActivity.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
//                {
//                    requestStoragePermission();
//                }
//                else
//                {
//                    saveToTextFile(view);
//                }
//            }
//        });

        buttonShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onShare();
            }
        });

        // Add text fields to text watcher listener
        editTextWidth.addTextChangedListener(textWatcher);
        editTextLength.addTextChangedListener(textWatcher);
        editTextHeight.addTextChangedListener(textWatcher);
    }

    public void onShare()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            requestStoragePermission();
        }
        else
        {
            saveToTextFile(null);

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

            if(file.exists())
            {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse("file://" + file.getAbsolutePath()));
                startActivity(Intent.createChooser(share, "Share using"));
            }
            else
            {
                Toast.makeText(this, "Failed to create file for sharing", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestStoragePermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed in order to save the data to a file")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSIONS_CODE)
//        {
//            // Toast.makeText(this, grantResults.length + " " + grantResults[0], Toast.LENGTH_SHORT).show();
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    public void saveToTextFile(View v)
    {
        FileOutputStream fos = null;
        try
        {
            String widthStr = editTextWidth.getText().toString().trim();
            String lengthStr = editTextLength.getText().toString().trim();
            String heightStr = editTextHeight.getText().toString().trim();

            String data = "Width: " + widthStr + "\n" +
                          "Length: " + lengthStr + "\n" +
                          "Height: " + heightStr;

            fileName = username + "_" + dateStr + ".txt";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

            resetAll(null);

            // For the download pop-up
//            DownloadManager.Request request = new DownloadManager.Request(android.net.Uri.parse("file://" + file.getAbsolutePath()));
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//            request.setTitle(fileName);
//            request.setDescription("File is being downloaded...");
//
//            request.allowScanningByMediaScanner();
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//
//            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//            manager.enqueue(request);

            Toast.makeText(this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
        catch (FileNotFoundException fnfe)
        {
            Toast.makeText(this, "There was an error with the file", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(fos != null)
            {
                try
                {
                    fos.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private final TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            String widthStr = editTextWidth.getText().toString().trim();
            String lengthStr = editTextLength.getText().toString().trim();
            String heightStr = editTextHeight.getText().toString().trim();

            buttonSend.setEnabled(!widthStr.isEmpty() && !lengthStr.isEmpty() && !heightStr.isEmpty());
//            buttonShare.setEnabled(!widthStr.isEmpty() || !lengthStr.isEmpty() || !heightStr.isEmpty());
            buttonClearAll.setEnabled(!widthStr.isEmpty() || !lengthStr.isEmpty() || !heightStr.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable)
        {

        }
    };

    public void resetWidth(View view)
    {
        editTextWidth.getText().clear();
    }

    public void resetLength(View view)
    {
        editTextLength.getText().clear();
    }

    public void resetHeight(View view)
    {
        editTextHeight.getText().clear();
    }

    public void resetAll(View view)
    {
        editTextWidth.getText().clear();
        editTextLength.getText().clear();
        editTextHeight.getText().clear();
    }
}