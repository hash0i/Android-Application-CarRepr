package com.fyp.amenms.FirebaseHelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fyp.amenms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;


/**
 * Created by Kamran on 3/15/2017.
 */

public class FirebaseStorageHelper {
    private FirebaseStorage firebaseStorage;
    private StorageReference rootRef;
    Context context;

    public FirebaseStorageHelper(Context context)
    {
        this.context = context;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.rootRef = firebaseStorage.getReferenceFromUrl("gs://amenms-391f8.appspot.com");
    }

    public void displayUserPicture(final ImageView imageView, final String UID){
        try {
            rootRef.child("UserDisplayPictures/" + UID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //Log.e("StorageHelper", "Loading profile for  " + UID);
                    Picasso.with((context)).load(uri)
                            .fit()
                            .error(R.drawable.user1)
                            .into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e("StorageHelper", "Error loading profile image! for " + UID);
                    Toast.makeText(context, "Error loading profile image!", Toast.LENGTH_LONG).show();
                    Picasso.with((context)).load(R.drawable.user1)
                            .fit()
                            .into(imageView);
                }
            });
        }catch (Exception e){
            Log.e("StorageHelper", "Error loading profile image! for " + UID + ": StackTrace: " +e.toString());
            e.printStackTrace();
        }
    }

    public void saveProfilePictureFromImageView(ImageView profileImage, String UID){
        StorageReference photoParentRef = rootRef.child("UserDisplayPictures");
        StorageReference photoRef = photoParentRef.child(UID);
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();
        Bitmap bitmap = profileImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("SignUpActivity", "OnFailure " + e.getMessage());
                Toast.makeText(context, "Photo Upload failed",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("SignUpActivity", "Photo uploaded");
            }
        });
    }

    public void saveProfilePictureFromUri(Uri imageUri, String UID){
        StorageReference photoParentRef = rootRef.child("UserDisplayPictures");
        StorageReference photoRef = photoParentRef.child(UID);

        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("SignUpActivity", "OnFailure " + e.getMessage());
                Toast.makeText(context, "Photo Upload failed",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("SignUpActivity", "Photo uploaded");
            }
        });
    }
}
