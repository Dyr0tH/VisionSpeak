package com.example.imagerecognition;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.imagerecognition.network.ApiService;
import com.example.imagerecognition.network.RetrofitClient;

import java.io.File;
import android.Manifest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button uploadButton, analyzeButton, openCameraButton;
    private TextView descriptionText;
    private ImageView uploadedImage;
    private EditText serverURL;
    private Spinner languageSpinner;
    private MediaPlayer mediaPlayer;
    private Handler seekHandler = new Handler();
    private Runnable updateSeekBar;
    private boolean isPlaying = false;
    private ImageButton playPauseButton;
    private SeekBar audioSeekBar;
    private Uri cameraImageUri;
    private AlertDialog loadingDialog;

    private Uri selectedImageUri;
    private SharedPreferences sharedPref;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    uploadedImage.setImageURI(uri);
                    analyzeButton.setEnabled(true);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedImageUri = cameraImageUri;
                    uploadedImage.setImageURI(selectedImageUri);
                    analyzeButton.setEnabled(true);
                } else {
                    Toast.makeText(this, "Camera capture failed", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    cameraLauncher.launch(cameraIntent);
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        uploadButton = findViewById(R.id.uploadButton);
        openCameraButton = findViewById(R.id.openCameraButton);
        uploadedImage = findViewById(R.id.selectedImage);
        analyzeButton = findViewById(R.id.analyzeButton);
        descriptionText = findViewById(R.id.descriptionText);
        serverURL = findViewById(R.id.serverURL);
        languageSpinner = findViewById(R.id.spinner); // ✅ Added Spinner reference
        playPauseButton = findViewById(R.id.playPauseButton);
        audioSeekBar = findViewById(R.id.audioSeekBar);

        // Load saved server URL
        sharedPref = getPreferences(MODE_PRIVATE);
        serverURL.setText(sharedPref.getString("server_url", ""));

        // Setup language spinner
        setupLanguageSpinner();

        // Setup buttons
        analyzeButton.setEnabled(false);
        uploadButton.setOnClickListener(v -> chooseImage());
        analyzeButton.setOnClickListener(v -> validateAndAnalyze());
        openCameraButton.setOnClickListener(v -> openCamera());

        // Get camera permissions



// MediaPlayer setup
        mediaPlayer = new MediaPlayer();

// Play/Pause button click
        playPauseButton.setOnClickListener(v -> toggleAudioPlayback());

// SeekBar change listener
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

// Runnable to update SeekBar
        updateSeekBar = new Runnable() {
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    audioSeekBar.setProgress(currentPosition);

                    // Update every second for smoother streaming
                    seekHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void setupLanguageSpinner() {
        // Language options
        String[] languages = {"English", "Hindi", "French", "German", "Italian", "Tamil"};

        // Set up adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        languageSpinner.setAdapter(adapter);

        // Set default to English
        languageSpinner.setSelection(0);
    }

    private void chooseImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void openCamera() {
        File imageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyApp");
        if (!imageDir.exists()) {
            imageDir.mkdirs();  // Ensure the directory exists
        }

        File imageFile = new File(imageDir, "captured_image.jpg");
        try {
            if (!imageFile.exists()) {
                imageFile.createNewFile();  // Create the file if it doesn't exist
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            cameraLauncher.launch(cameraIntent);
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this)
                    .setTitle("Camera Permission Needed")
                    .setMessage("This app requires camera access to take pictures. Please grant the permission.")
                    .setPositiveButton("OK", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton("No Thanks", (dialog, which) -> {
                        Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }


    }


    private void validateAndAnalyze() {
        String url = serverURL.getText().toString().trim();

        if (!android.util.Patterns.WEB_URL.matcher(url).matches()) {
            Toast.makeText(this, "Invalid server URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save server URL
        sharedPref.edit().putString("server_url", url).apply();

        // Get selected language
        String selectedLanguage = languageSpinner.getSelectedItem().toString();

        // Prepare image file
        File imageFile = new File(Objects.requireNonNull(getRealPathFromUri(this, selectedImageUri)));
        RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/*"));
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        // Get API service
        ApiService apiService = RetrofitClient.getApiService(url);
        final int[] retryCount = {0};
        int maxRetries = 3;

        loadingDialog = new AlertDialog.Builder(this)
                .setView(R.layout.loading_dialog)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Execute API call
        apiService.analyzeImage(imagePart, selectedLanguage)
                .enqueue(new Callback<ApiService.AnalysisResponse>() {
                    @Override
                    public void onResponse(Call<ApiService.AnalysisResponse> call, Response<ApiService.AnalysisResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            handleAnalysisResult(response.body());
                            loadingDialog.dismiss();
                        } else {
                            if (retryCount[0] < maxRetries) {
                                retryCount[0]++;
                                apiService.analyzeImage(imagePart, selectedLanguage).enqueue(this);
                            } else {
                                showError("Server error: " + response.code());
                                loadingDialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.AnalysisResponse> call, Throwable t) {
                        if (retryCount[0] < maxRetries) {
                            retryCount[0]++;
                            apiService.analyzeImage(imagePart, selectedLanguage).enqueue(this);
                        } else {
                            showError("Network error: " + t.getMessage());
                            loadingDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    public static String getRealPathFromUri(Context context, Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath(); // Direct file path
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return copyFileFromUri(context, uri); // Copy content to accessible path
        }
        return null;
    }

    private static String copyFileFromUri(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            File file = new File(context.getCacheDir(), fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        }
        return "temp_file";
    }

    private void handleAnalysisResult(ApiService.AnalysisResponse response) {
        runOnUiThread(() -> {
            // populating description text area
            descriptionText.setText(response.getDescription());

            // handling table and populating with files
            if (response.getFiles() != null && !response.getFiles().isEmpty()) {
                populateFileTable(response.getFiles());
            }

            // Handle audio file if present
            if (response.getAudioUrl() != null) {
                setupAudioPlayer(response.getAudioUrl());
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void populateFileTable(List<ApiService.AnalysisResponse.FileInfo> files) {
        TableLayout fileTable = findViewById(R.id.fileTable);
        fileTable.removeAllViews(); // Clear existing rows

        // Create header row
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createTextView("S.No", true));
        headerRow.addView(createTextView("File Name", true));
        headerRow.addView(createTextView("Download", true));
        fileTable.addView(headerRow);

        // Populate table rows with files
        int serialNumber = 1;
        for (ApiService.AnalysisResponse.FileInfo file : files) {
            TableRow row = new TableRow(this);
            row.addView(createTextView(String.valueOf(serialNumber), false));
            row.addView(createTextView(file.getFileName(), false));

            // Create Download button
            ImageButton downloadButton = new ImageButton(this);
            downloadButton.setImageResource(android.R.drawable.stat_sys_download);
            downloadButton.setOnClickListener(v -> downloadFile(file.getDownloadUrl(), file.getFileName()));
            row.addView(downloadButton);

            fileTable.addView(row);
            serialNumber++;
        }
    }

    // Helper method to create TextView for table cells
    private TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, isHeader ? 16 : 14);
        textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        return textView;
    }

    // Open file download URL -- NOT USING ANYMORE
    private void openDownloadLink(String url) {
        // Get the base server URL from the textbox
        String baseServerUrl = serverURL.getText().toString().trim();

        // Check if the base URL ends with a slash, add one if not
        if (!baseServerUrl.endsWith("/")) {
            baseServerUrl += "/";
        }

        // Construct the full URL by appending the relative download path
        String fullDownloadUrl = baseServerUrl + url;

        // Open the constructed full URL in a browser or relevant app
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullDownloadUrl));
        startActivity(intent);
    }

    private void downloadFile(String url, String fileName) {
        // Get the base server URL from the textbox
        String baseServerUrl = serverURL.getText().toString().trim();

        // Ensure the base URL ends with a slash
        if (!baseServerUrl.endsWith("/")) {
            baseServerUrl += "/";
        }

        // Construct the full file URL
        String fullDownloadUrl = baseServerUrl + url;

        // Use DownloadManager to handle the file download
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fullDownloadUrl));
        request.setTitle("Downloading File");
        request.setDescription("File is being downloaded...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Set destination to Downloads folder
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Enqueue the request
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Downloading: " + fileName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download Manager not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleAudioPlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            isPlaying = true;
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            seekHandler.post(updateSeekBar);
        }
    }

    private void setupAudioPlayer(String audioUrl) {
        String baseServerUrl = serverURL.getText().toString().trim();

        if (!baseServerUrl.endsWith("/")) {
            baseServerUrl += "/";
        }

        String fullAudioUrl = baseServerUrl + audioUrl;
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            // Set data source from URL
            mediaPlayer.setDataSource(fullAudioUrl);

            // Prepare asynchronously
            mediaPlayer.prepareAsync();

            // Set listeners
            mediaPlayer.setOnPreparedListener(mp -> {
                audioSeekBar.setMax(mediaPlayer.getDuration());
                playPauseButton.setEnabled(true);
                Toast.makeText(this, "Audio ready to play", Toast.LENGTH_SHORT).show();
            });


            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (IOException e) {
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
        }
    }
}
