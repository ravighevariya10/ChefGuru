package com.example.splashscreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Delayed;


public class EnterRecipe extends AppCompatActivity {

    ContentLoadingProgressBar progressBar;
    Button temp_btn;
    TextInputEditText temp_text;
    //TextView temp_text;
    LinearLayout layout;
    Spinner food_type_spinner;
    ImageView arraw;
    TextInputLayout recipe_name, write_food, ingredients, description;
    TextInputEditText erecipe_name, ewrite_food, eingredients, edescription;
    Button select_image, upload_recipe;
    int count = 8;
    static final int IMAGE_PICK_CODE = 1000;
    static final int PERMISSION_CODE = 1001;
    ArrayList<String> food_type = new ArrayList<>();
    ArrayAdapter<String> food_type_adapter;
    myAdapter myAdapter;
    RecyclerView recyclerView;
    ArrayList<Uri> uri = new ArrayList<>();
    int i,j=1,data_counter,counter=0;
    String food = new String();
    Button temp;
    private int upload_count=0;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference verification = firebaseDatabase.getReference().child("Verification");
    StorageReference imagefolder = FirebaseStorage.getInstance().getReference().child("verification");
    HashMap<String,Object> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_recipe);

        food_type_spinner = findViewById(R.id.spinner);
        arraw = findViewById(R.id.arraw);
        layout = findViewById(R.id.layout2);
        write_food = findViewById(R.id.write_food);
        recipe_name = findViewById(R.id.recipe_name);
        ewrite_food = findViewById(R.id.ewrite_food);
        erecipe_name = findViewById(R.id.erecipe_name);
        select_image = findViewById(R.id.select);
        upload_recipe = findViewById(R.id.upload_recipe);
        recyclerView = findViewById(R.id.recyclerview);
        ingredients = findViewById(R.id.ingredients);
        description = findViewById(R.id.description);
        eingredients = findViewById(R.id.eingredients);
        edescription = findViewById(R.id.edescription);
        temp = findViewById(R.id.temp);
        progressBar = findViewById(R.id.progress_horizontal);
       // temp_text = findViewById(R.id.temp_text);
        //temp_btn = findViewById(R.id.temp_btn);

        food_type.add("");
        food_type.add("Gujarati");
        food_type.add("Punjabi");
        food_type.add("South Indian");
        food_type.add("Chinese");
        food_type.add("Mexican");
        food_type.add("Italian");
        food_type.add("Fast Food");
        food_type.add("Other");

        food_type_adapter = new ArrayAdapter<>(this, R.layout.food_spinner, food_type);

        food_type_spinner.setAdapter(food_type_adapter);

        food_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                count=8;
                if (i == count) {
                    write_food.setVisibility(View.VISIBLE);
                    layout.setVisibility(View.GONE);
                    count++;
                    //food = write_food.getEditText().getText().toString();
                } else {
                    write_food.setVisibility(View.GONE);
                    food = food_type_spinner.getItemAtPosition(i).toString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(EnterRecipe.this,LinearLayoutManager.HORIZONTAL,true);
        linearLayoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        myAdapter = new myAdapter(uri);
        recyclerView.setAdapter(myAdapter);


    }



    public void onClickArraw(View view) {
        food_type_spinner.performClick();
    }

    public void onClickSelect(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);
            } else
                pickImageFromGallary();
        } else
            pickImageFromGallary();

    }

    private void pickImageFromGallary() {
        Intent gallary = new Intent(Intent.ACTION_GET_CONTENT);
        gallary.setType("image/*");
        gallary.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(gallary,"Select Image(s)"), IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImageFromGallary();
                else
                    Toast.makeText(this, "Permission Denied.....!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            try {
                if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE)
                {
                    if (data.getClipData() != null)
                    {


                        uri.clear();
                        j=0;
                        for (i = 0; i < data.getClipData().getItemCount(); i++)
                        {

                            uri.add(data.getClipData().getItemAt(i).getUri());
                            myAdapter.notifyDataSetChanged();

                        }
                    }
                }
                else {
                    j=1;
                    Toast.makeText(this, "Please Select multiple image", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }

    public Boolean validateRecipeName()
    {
        String val = recipe_name.getEditText().getText().toString();

        if(val.isEmpty()) {
            recipe_name.setError("Field cannot be empty");
           /* if(validateIngredients())
                temp.setVisibility(View.GONE);
            else
                temp.setVisibility(View.VISIBLE);*/
            return false;
        }
        else
        {
            recipe_name.setError(null);
            recipe_name.setErrorEnabled(false);
            return true;
        }
    }

    public Boolean validateFoodType()
    {
        if(food.isEmpty()) {
            write_food.setError("Field cannot be empty");
            return false;
        }
        else
        {
            write_food.setError(null);
            write_food.setErrorEnabled(false);
            return true;
        }
    }

    public Boolean validateDescription()
    {
        String val = description.getEditText().getText().toString();
        if(val.isEmpty()) {
            description.setError("Field cannot be empty");
            return false;
        }
        else
        {
            description.setError(null);
            description.setErrorEnabled(false);
            return true;
        }
    }

   public Boolean validateSpinner()
    {
        View view=food_type_spinner.getSelectedView();


        //TextView tvListItem = (TextView)view;
        TextView tvInvisibleError = (TextView)findViewById(R.id.tvInvisibleError);
        String errorMessage = "Item not selected";

        if (food_type_spinner.getSelectedItemPosition()==0)
        {
           // tvListItem.setError(errorMessage);
            //tvListItem.requestFocus();
            tvInvisibleError.setError(errorMessage);
            tvInvisibleError.requestFocus();
            temp.setVisibility(View.VISIBLE);
            return false;
        }
        else
        {
            //tvListItem.setError(null);
            tvInvisibleError.setError(null);
            temp.setVisibility(View.GONE);
            return true;
        }
    }

    public Boolean validateIngredients()
    {
        String val = ingredients.getEditText().getText().toString();
        if(val.isEmpty()) {
            ingredients.setError("Field cannot be empty");
           /* if(validateRecipeName())
                temp.setVisibility(View.GONE);
            else
                temp.setVisibility(View.VISIBLE);*/
            return false;
        }
        else
        {
            ingredients.setError(null);
            ingredients.setErrorEnabled(false);
            return true;
        }
    }

    private  void StoreLink(String url)
    {
        counter++;
        //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("verification").child("imagelink");

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("Image "+counter,url);
        String key = verification.getKey();
        Log.i("J",key);
        verification.child(key).push().setValue(hashMap);

    }

    public void onUploadClick(View view) {

        if(count == 9){
        food = write_food.getEditText().getText().toString();}
        if(food_type_spinner.getSelectedItemPosition()!=8) {
            if (!validateRecipeName() | !validateIngredients() | !validateDescription() | !validateSpinner()) {
                return;
            } else {
                if(j==0) {
                    map.put("Recipe Name", recipe_name.getEditText().getText().toString());
                    map.put("Food Type", food);
                    map.put("Ingredients", ingredients.getEditText().getText().toString());
                    map.put("Description", description.getEditText().getText().toString());
                    map.put("Imagelinks",null);
                    verification.push().setValue(map);
                    for (upload_count =0;upload_count < uri.size();upload_count++) {

                        Uri IndividualImage = uri.get(upload_count);
                        final StorageReference imagename = imagefolder.child("Image" + IndividualImage.getLastPathSegment());
                        imagename.putFile(IndividualImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                       String url = String.valueOf(uri);
                                       StoreLink(url);
                                    }
                                });
                            }
                        });
                    }


                }
                else
                {
                    Toast.makeText(this, "Image(s) not selected", Toast.LENGTH_SHORT).show();
                }
               /* new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 2000);*/

                /*erecipe_name.setText("");
                food_type_spinner.setSelection(0);
                eingredients.setText("");
                edescription.setText("");*/





            }
        }
        else
        {
            if (!validateRecipeName() | !validateIngredients() | !validateDescription() | !validateFoodType()) {
                return;
            } else {

                if(j==0) {
                    map.put("Recipe Name", recipe_name.getEditText().getText().toString());
                    map.put("Food Type", food);
                    map.put("Ingredients", ingredients.getEditText().getText().toString());
                    map.put("Description", description.getEditText().getText().toString());


                    verification.push().setValue(map);
                }
                else
                    Toast.makeText(this, "Image(s) not selected", Toast.LENGTH_SHORT).show();
                erecipe_name.setText("");
                ewrite_food.setText("");
                eingredients.setText("");
                edescription.setText("");

            }
        }

        /*String temp = write_food.getEditText().getText().toString();
        food_type.remove(count);
        food_type.add(count, temp);
        count++;
        food_type.add(count, "Other");
        food_type_adapter.notifyDataSetChanged();*/

    }

     /*public void Temp(View view) {
            verification.child("-MJLPDbGJE3EN56b1vpH").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String temp = dataSnapshot.child("Description").getValue(String.class);
                    temp_text.setText(temp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }*/

}