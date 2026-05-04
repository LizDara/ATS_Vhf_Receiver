package com.atstrack.ats.ats_vhf_receiver.Services;

import static com.google.api.client.json.gson.GsonFactory.getDefaultInstance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final String TAG = DriveServiceHelper.class.getSimpleName();
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;
    private final java.io.File root;
    private final String fileName;
    private final Context context;

    public DriveServiceHelper(java.io.File root, String fileName, Context context) {
        this.root = root;
        this.fileName = fileName;
        this.context = context;
    }

    /**
     * Access the drive files of the logged in account.
     * @param data Content of account information.
     */
    public void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(googleSignInAccount -> {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(googleSignInAccount.getAccount());
            mDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    getDefaultInstance(),
                    credential)
                    .setApplicationName("ATS Bridge")
                    .build();

            uploadFile();
        }).addOnFailureListener(e -> Log.i(TAG, "Sign-in failed: " + e));
    }


    /**
     * Saves the document in the drive account.
     */
    private void uploadFile() {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Uploading to Google Drive.");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        createFile(root.getAbsolutePath(), fileName).addOnSuccessListener(s -> {
            progressDialog.dismiss();
            Toast.makeText(context, "Uploaded successfully.", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(context, "Check your Google Drive Api key", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Sends a file from the local storage to the cloud.
     * @param path The directory path of the file to be saved to the cloud.
     * @param name The name of the file in that directory path.
     * @return Return the file id if it was saved successfully.
     */
    public Task<String> createFile(String path, String name) {
        return Tasks.call(mExecutor, () -> {
            File fileMetaData = new File();
            fileMetaData.setName(name.substring(0, name.length() - 4));
            java.io.File file = new java.io.File(path + "/" + name);
            FileContent fileContent = new FileContent("text/plain", file);
            File myFile = null;
            try {
                myFile = mDriveService.files().create(fileMetaData, fileContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (myFile == null)
                throw new IOException("Null result when requesting file creation.");
            return myFile.getId();
        });
    }
}
