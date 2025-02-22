package com.example.imagerecognition.network;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("analyze")
    Call<AnalysisResponse> analyzeImage(
            @Part MultipartBody.Part image,
            @Part("language") String language  // ✅ Accept language as plain string
    );

    class AnalysisResponse {
        private String description;
        private String audio_url;  // Matches JSON key directly
        private List<FileInfo> files;

        public String getDescription() { return description; }
        public String getAudioUrl() { return audio_url; } // Getter follows the same naming
        public List<FileInfo> getFiles() { return files; }


        public static class FileInfo {
            private String file_name;     // Matches JSON directly
            private String download_url;  // Matches JSON directly

            public String getFileName() { return file_name; }
            public String getDownloadUrl() { return download_url; }
        }

    }
}
