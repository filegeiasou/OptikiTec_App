package com.example.first_androidstudio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{
//    private final int STORAGE_PERMISSIONS_CODE = 1;

    SwitchCompat switchMode;
    boolean nightMode;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private String username;
    private String data;

    private EditText editTextWidth;
    private EditText editTextLength;
    private EditText editTextHeight;

    private Button buttonCreate;
    private Button buttonView;
    private Button buttonShare;
    private Button buttonClearAll;

    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Some code to prevent the app from crashing with the Share feature
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

//        // Request READ_EXTERNAL_STORAGE permission if not granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//        }

        username = getIntent().getStringExtra("username");

        switchMode = findViewById(R.id.switchMode);
        sharedPreferences = getSharedPreferences("MODEs", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        if(nightMode)
        {
            switchMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Set switch button listener to change theme
        switchMode.setOnClickListener(view -> changeTheme());

        // Initialize text fields
        // Text Fields
        TextView textViewUsername = findViewById(R.id.textViewUsername);
        editTextWidth = findViewById(R.id.editTextWidth);
        editTextLength = findViewById(R.id.editTextLength);
        editTextHeight = findViewById(R.id.editTextHeight);

        // Set username text
        textViewUsername.setText(username);

        // Initialize buttons
        buttonCreate = findViewById(R.id.createButton);
        buttonView = findViewById(R.id.viewButton);
        buttonShare = findViewById(R.id.shareButton);
        buttonClearAll = findViewById(R.id.clearAllButton);

        buttonCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createFile();
            }
        });

        buttonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent viewPDF = new Intent(MainActivity.this, ViewActivity.class);
                viewPDF.putExtra("filename", filename);
                startActivity(viewPDF);
            }
        });

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

    public void changeTheme()
    {
        if(nightMode)
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor = sharedPreferences.edit();
            editor.putBoolean("nightMode", false);
        }
        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor = sharedPreferences.edit();
            editor.putBoolean("nightMode", true);
        }
        editor.apply();

    }

    public void createFile()
    {
        String widthStr = editTextWidth.getText().toString().trim();
        String lengthStr = editTextLength.getText().toString().trim();
        String heightStr = editTextHeight.getText().toString().trim();

        data = "Width: " + widthStr + "\n" +
                "Length: " + lengthStr + "\n" +
                "Height: " + heightStr;

//        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = formatter.format(date);

        filename = username + "_" + dateStr + ".pdf";

        convertToPDF();
        buttonView.setEnabled(true);
    }

    public void convertToPDF()
    {
        String path = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + filename;
        File file = new File(path);

        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Document document = new Document(PageSize.A4);

                    try {
                        PdfWriter.getInstance(document, new FileOutputStream(file.getAbsoluteFile()));
                        document.open();

                        Paragraph paragraph = new Paragraph();
                        paragraph.add(data);

                        try {
                            document.add(paragraph);
                            document.close(); // Close the document here
                            Toast.makeText(this, "PDF Document has been created", Toast.LENGTH_SHORT).show();
                        } catch (DocumentException de) {
                            de.printStackTrace();
                        }
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Failed to create the file", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "File already exists", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the user taps the Share button
     */
    public void onShare()
    {
        String path = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + filename;
        File file = new File(path);

        if(file.exists())
        {
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.setType("text/plain");
            startActivity(Intent.createChooser(share, "Share via"));
        }
    }

    /**
     * Called when the user taps the Save button and permission is not granted
     */
//    private void requestStoragePermission()
//    {
//        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
//        {
//            new AlertDialog.Builder(this)
//                    .setTitle("Permission needed")
//                    .setMessage("This permission is needed in order to save the data to a file")
//                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            ActivityCompat.requestPermissions(MainActivity.this,
//                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
//                        }
//                    })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    }).create().show();
//        }
//        else
//        {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_CODE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSIONS_CODE)
//        {
//            // Toast.makeText(this, grantResults.length + " " + grantResults[0], Toast.LENGTH_SHORT).show();
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            {
//                saveToTextFile(null);
//            }
//            else
//            {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    /**
     * Called when the user taps the Save button and permission is granted
     * @param v The view that was clicked
     */
//    private void saveToTextFile(View v) {
//        Date date = new Date();
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//        String dateStr = formatter.format(date);
//
//        String widthStr = editTextWidth.getText().toString().trim();
//        String lengthStr = editTextLength.getText().toString().trim();
//        String heightStr = editTextHeight.getText().toString().trim();
//
//        String data = "Width: " + widthStr + "\n" +
//                "Length: " + lengthStr + "\n" +
//                "Height: " + heightStr;
//
//        FileOutputStream fOut = null;
//
//        try {
//            String path = Environment.getExternalStorageDirectory().toString();
//            File dir = new File(path + "/OptikiTec");
//            dir.mkdirs();
//
//            filename = username + "_" + dateStr + ".txt";
//
//            File file = new File(dir, filename);
//
//            fOut = new FileOutputStream(file);
//            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//
//            myOutWriter.append(data);
//            myOutWriter.close();
//
//            resetAll(null);
//
//            Toast.makeText(this, filename + " is saved to " + dir, Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "There was an error with the file", Toast.LENGTH_SHORT).show();
//        } finally
//        {
//            if (fOut != null)
//            {
//                try {
//                    fOut.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private final TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }

        /**
         * Overridden method to enable/disable button based on text field input
         * @param charSequence
         * @param i
         * @param i1
         * @param i2
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            String widthStr = editTextWidth.getText().toString().trim();
            String lengthStr = editTextLength.getText().toString().trim();
            String heightStr = editTextHeight.getText().toString().trim();

            buttonCreate.setEnabled(!widthStr.isEmpty() && !lengthStr.isEmpty() && !heightStr.isEmpty());
            buttonShare.setEnabled(!widthStr.isEmpty() || !lengthStr.isEmpty() || !heightStr.isEmpty());
            buttonClearAll.setEnabled(!widthStr.isEmpty() || !lengthStr.isEmpty() || !heightStr.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable)
        {

        }
    };

    /**
     * Called when the user taps the Reset button on the width text field
     * @param view
     */
    public void resetWidth(View view)
    {
        editTextWidth.getText().clear();
    }

    /**
     * Called when the user taps the Reset button on the length text field
     * @param view
     */
    public void resetLength(View view)
    {
        editTextLength.getText().clear();
    }

    /**
     * Called when the user taps the Reset button on the height text field
     * @param view
     */
    public void resetHeight(View view)
    {
        editTextHeight.getText().clear();
    }

    /**
     * Called when the user taps the Clear All button
     * @param view
     */
    public void resetAll(View view)
    {
        editTextWidth.getText().clear();
        editTextLength.getText().clear();
        editTextHeight.getText().clear();
    }
}