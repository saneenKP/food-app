package com.example.temp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SharedPreferenceConfig {

    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPreferenceConfig(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.user_address),Context.MODE_PRIVATE);
    }

    public void writeUserAddress(HashMap<String ,  String> address){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getResources().getString(R.string.Add_name) , address.get(context.getResources().getString(R.string.Add_name)));
        editor.putString(context.getResources().getString(R.string.houseNo) , address.get(context.getResources().getString(R.string.houseNo)));
        editor.putString(context.getResources().getString(R.string.houseName) , address.get(context.getResources().getString(R.string.houseName)));
        editor.putString(context.getResources().getString(R.string.landmark) , address.get(context.getResources().getString(R.string.landmark)));
        editor.putString(context.getResources().getString(R.string.street) , address.get(context.getResources().getString(R.string.street)));
        editor.commit();

    }

    public void setDefault(){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.Add_name) , "Saneen k p");
        editor.putString(context.getResources().getString(R.string.houseNo) , "pudupally");
        editor.putString(context.getResources().getString(R.string.houseName) , "namet");
        editor.putString(context.getResources().getString(R.string.landmark) , "Green Land Autditorium");
        editor.putString(context.getResources().getString(R.string.street) , "Malappuram");
        editor.commit();


    }

    public HashMap<String , String> readUserAddress(){

        HashMap<String , String> address = new HashMap<>();
        address.put(context.getResources().getString(R.string.Add_name) , sharedPreferences.getString(context.getResources().getString(R.string.Add_name) , ""));
        address.put(context.getResources().getString(R.string.houseNo) , sharedPreferences.getString(context.getResources().getString(R.string.houseNo) , ""));
        address.put(context.getResources().getString(R.string.houseName) , sharedPreferences.getString(context.getResources().getString(R.string.houseName) , ""));
        address.put(context.getResources().getString(R.string.landmark) , sharedPreferences.getString(context.getResources().getString(R.string.landmark) , ""));
        address.put(context.getResources().getString(R.string.street) , sharedPreferences.getString(context.getResources().getString(R.string.street) , ""));

        return address;

    }

}