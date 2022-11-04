package com.example.leeconmonclick;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.leerconmonclick.util.Content;


public class AddContentActivity extends AppCompatActivity {

    private Spinner spinner;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> someActivityResultCamera;
    private ActivityResultLauncher<String> someActivityResultGalery;
    private StorageReference storageReference;
    private Uri uri;
    private String uriStr;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private TextInputEditText word;
    private Context context;
    private Bundle data;
    private ArrayAdapter<String> adapterSpinner;
    private StorageReference filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        spinner = (Spinner) findViewById(R.id.spinnerId);
        imageView = (ImageView) findViewById(R.id.imgViewId);
        word = (TextInputEditText) findViewById(R.id.wordInputId);

        String[] opciones = {"-", "El", "La", "Los", "Las", "Un", "Una", "Unos", "Unas"};

        adapterSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones);
        spinner.setAdapter(adapterSpinner);
        context = getApplicationContext();

        someActivityResultGalery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        uri = result;
                        imageView.setImageURI(result);
                    }
                });


        someActivityResultCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Bundle extras = result.getData().getExtras();
                            Bitmap img = (Bitmap) extras.get("data");
                            uri = getImageUri(context, img);
                            imageView.setImageBitmap(img);
                        }
                    }
                });

        data = getIntent().getExtras();
        if (data.getBoolean("modeEdit")){
                modeEditOn();
        }




    }
    public void cameraImageBtn(View v){ someActivityResultCamera.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)); }

    public void galeryImageBtn(View v){
        someActivityResultGalery.launch("image/*");
    }

    public void saveContent (View v){

        if (!data.getBoolean("modeEdit") || uri !=null){
            if(uri != null){

                if (data.getBoolean("modeEdit")){
                    filePath = storageReference.child("contenidos").child(data.getString("word"));
                    filePath.delete();
                }

                filePath = storageReference.child("contenidos").child(word.getText().toString());
                filePath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw new Exception();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri2 = task.getResult();
                            List<String> sylablle = new ArrayList<>();
                            Content content = new Content(word.getText().toString(), downloadUri2.toString(), sylablle, spinner.getSelectedItem().toString());
                            databaseReference.child("content").child(word.getText().toString()).setValue(content).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), "Se ha creado el contenido correctamente", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }
                    }
                });
            }else{ Toast.makeText(getApplicationContext(),"Debe elegir una imagen",Toast.LENGTH_LONG).show(); }


            }else{
                editContent();
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void goHelp(View v){
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    public void goBack(View view){
        onBackPressed();
    }

    private void modeEditOn() {

        Glide.with(this).load(data.getString("image")).into(imageView);
        word.setText(data.getString("word"));
        uriStr = data.getString("image");
        int selectionPosition= adapterSpinner.getPosition(data.getString("determinant"));
        spinner.setSelection(selectionPosition);
    }

    private void editContent(){
        databaseReference.child("content").child(data.getString("word")).removeValue();
        List<String> sylablle = new ArrayList<>();
        Content content = new Content(word.getText().toString(), uriStr ,sylablle , spinner.getSelectedItem().toString());
        databaseReference.child("content").child(word.getText().toString()).setValue(content).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Se ha editado el contenido correctamente",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

}