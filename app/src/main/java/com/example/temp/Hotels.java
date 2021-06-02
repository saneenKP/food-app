 package com.example.temp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotels extends AppCompatActivity {

    private RecyclerView hotels;
    private RecyclerView.LayoutManager layoutManager;
    private List<HotelDetails> hotelsList;
    private DatabaseReference databaseReference;
    private List<String> hotelKeys;
    private FloatingActionButton fab;
    private Boolean imageFlag = false;
    private  Uri resultUri;
    private AlertDialog dialog;
    private CircularProgressIndicator circularProgressIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);

        hotels = findViewById(R.id.hotels);
        layoutManager =  new LinearLayoutManager(this);
        circularProgressIndicator = findViewById(R.id.circularProgress);

        hotelsList = new ArrayList<>();
        hotelKeys = new ArrayList<>();

        fab = findViewById(R.id.addNewHotel);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(null , null , false);
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode));

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    HotelDetails hotelDetails = ds.getValue(HotelDetails.class);
                    hotelsList.add(hotelDetails);
                    hotelKeys.add(ds.getKey());
                }

                HotelViewAdapter hotelViewAdapter = new HotelViewAdapter(getApplicationContext() , hotelsList, hotelKeys , new editHotelinterface() {
                    @Override
                    public void openDialogBox(HotelDetails hotelDetails , String key) {

                        showAlertDialog(hotelDetails , key , true);

                    }
                });
                hotels.setLayoutManager(layoutManager);
                hotels.setAdapter(hotelViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateHotel(HotelDetails hotelDetails , String key){

        if (resultUri!=null){

            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelName)).setValue(hotelDetails.getHotel_name());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelLocation)).setValue(hotelDetails.getLocation());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelAddress)).setValue(hotelDetails.getAddress());

            StorageReference hotelImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.HotelNode)+"/"+hotelDetails.getHotel_name());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);


            hotelImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        hotelImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    hotelDetails.setImage(task.getResult().toString());

                                    databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelImage)).setValue(hotelDetails.getImage()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Hotel Uploaded" , Toast.LENGTH_LONG).show();

                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    circularProgressIndicator.setProgress(0);
                                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        circularProgressIndicator.setProgress(0);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    circularProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    circularProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                }
            });

        }else{

            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelName)).setValue(hotelDetails.getHotel_name());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelLocation)).setValue(hotelDetails.getLocation());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelAddress)).setValue(hotelDetails.getAddress());


            Toast.makeText(getApplicationContext() , "Hotel Value Updated" , Toast.LENGTH_LONG).show();

            dialog.dismiss();
        }

    }


    private void addNewHotel(HotelDetails hotelDetails , String key){

        if (resultUri == null && key == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{

            StorageReference hotelImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.HotelNode)+"/"+hotelDetails.getHotel_name());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);

            hotelImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        hotelImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    hotelDetails.setImage(task.getResult().toString());

                                    databaseReference.push().setValue(hotelDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Hotel Uploaded" , Toast.LENGTH_LONG).show();

                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    circularProgressIndicator.setProgress(0);
                                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        circularProgressIndicator.setProgress(0);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    circularProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    circularProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                }
            });

        }

    }

    private void deleteHotel(String name , String key){

        StorageReference deleteHotelImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.HotelNode)+"/"+name);

        deleteHotelImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getApplicationContext() , "Successfully Deleted" , Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext() , "Failed ..." + e.toString(),Toast.LENGTH_LONG).show();
            }
        });

       databaseReference.child(key).removeValue();

    }


    private void showAlertDialog(HotelDetails hotelDetails , String key , boolean updateStatus){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.add_hotel_alertdialog , null);

        builder.setView(v);
        builder.setMessage(" Add New Hotel ");

        EditText hotelName = v.findViewById(R.id.newHotelName);
        EditText hotelAddress = v.findViewById(R.id.newHotelAddress);
        EditText hotelLocation = v.findViewById(R.id.newHotelLocation);
        Button addHotel = v.findViewById(R.id.addNewHotel);
        Button addImage = v.findViewById(R.id.addNewHotelImage);
        Button delete = v.findViewById(R.id.deleteHotel);
        delete.setVisibility(View.INVISIBLE);


        if (updateStatus){
            delete.setVisibility(View.VISIBLE);
            hotelName.setText(hotelDetails.getHotel_name());
            hotelAddress.setText(hotelDetails.getAddress());
            hotelLocation.setText(hotelDetails.getLocation());

        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteHotel(hotelDetails.getHotel_name() , key);

            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                       .setAspectRatio(1, 1)
                       .start(Hotels.this);

            }
        });


        addHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(hotelName.getText())){
                    hotelName.setError("Please set Hotel Name");
                }else if (TextUtils.isEmpty(hotelAddress.getText())){
                    hotelAddress.setError("Please set Address");
                }else if(TextUtils.isEmpty(hotelLocation.getText())){
                    hotelLocation.setError("Please give the location");
                }else{
                    HotelDetails newHotelDetails = new HotelDetails();
                    newHotelDetails.setHotel_name(hotelName.getText().toString());
                    newHotelDetails.setAddress(hotelAddress.getText().toString());
                    newHotelDetails.setLocation(hotelLocation.getText().toString());

                    if(updateStatus)
                        updateHotel(newHotelDetails , key);
                    else
                        addNewHotel(newHotelDetails,key);
                }

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

    private String getBase64(String code){

        byte[] data;
        String result="";
        try {
            data = code.getBytes("UTF-8");
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isInternet(){
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                return false;
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CropImage.ActivityResult result = CropImage.getActivityResult(data);

           if (resultCode == RESULT_OK){
               resultUri = Objects.requireNonNull(result).getUri();
        }
    }

}