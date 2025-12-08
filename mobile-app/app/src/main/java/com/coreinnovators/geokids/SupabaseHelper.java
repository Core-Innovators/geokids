package com.coreinnovators.geokids;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseHelper {
    private static final String TAG = "SupabaseHelper";

    // TODO: Replace these with your actual Supabase credentials
    private static final String SUPABASE_URL = "https://nixfjcmsrsfmdurcokvd.supabase.co"; // e.g., https://xxxxx.supabase.co
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5peGZqY21zcnNmbWR1cmNva3ZkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxODQyNjcsImV4cCI6MjA4MDc2MDI2N30.Aw10_22vK4Y1CS2LOu_HxCuw-Qalq7ijuVxVtGoylNM"; // Your anon/public key
    private static final String BUCKET_NAME = "profile_image";

    private static final OkHttpClient client = new OkHttpClient();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Uploads an image to Supabase Storage and returns the public URL
     *
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @return CompletableFuture with the public URL of the uploaded image
     */
    public static CompletableFuture<String> uploadImage(Context context, Uri imageUri) {
        CompletableFuture<String> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                // Generate unique filename
                String fileName = "driver_" + UUID.randomUUID().toString() + ".jpg";

                // Read image bytes from URI
                byte[] imageBytes = readBytesFromUri(context, imageUri);

                if (imageBytes == null) {
                    future.completeExceptionally(new Exception("Failed to read image file"));
                    return;
                }

                // Build upload URL
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

                // Create request body
                RequestBody requestBody = RequestBody.create(
                        imageBytes,
                        MediaType.parse("image/jpeg")
                );

                // Build request
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Content-Type", "image/jpeg")
                        .build();

                // Execute request
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        // Build public URL
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" +
                                BUCKET_NAME + "/" + fileName;

                        Log.d(TAG, "Image uploaded successfully: " + publicUrl);
                        future.complete(publicUrl);
                    } else {
                        String error = "Upload failed: " + response.code() + " - " +
                                response.message();
                        if (response.body() != null) {
                            error += " - " + response.body().string();
                        }
                        Log.e(TAG, error);
                        future.completeExceptionally(new Exception(error));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Reads bytes from a URI
     *
     * @param context Application context
     * @param uri URI to read from
     * @return Byte array of the file content, or null if failed
     */


    private static byte[] readBytesFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            return buffer;

        } catch (Exception e) {
            Log.e(TAG, "Error reading bytes from URI: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes an image from Supabase Storage
     *
     * @param imageUrl The full public URL of the image to delete
     * @return CompletableFuture indicating success or failure
     */
    public static CompletableFuture<Boolean> deleteImage(String imageUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                // Extract filename from URL
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                // Build delete URL
                String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

                // Build request
                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("apikey", SUPABASE_KEY)
                        .build();

                // Execute request
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Image deleted successfully");
                        future.complete(true);
                    } else {
                        String error = "Delete failed: " + response.code();
                        Log.e(TAG, error);
                        future.completeExceptionally(new Exception(error));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error deleting image: " + e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Gets the public URL for an uploaded file
     *
     * @param fileName The name of the file in the bucket
     * @return The public URL
     */
    public static String getPublicUrl(String fileName) {
        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
    }
}