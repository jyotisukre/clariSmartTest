package com.contact.recyler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import widgets.MyDividerItemDecoration;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<Contact> rowsArrayList = new ArrayList<>();
    ArrayList<Contact> StoreContacts =new ArrayList<>();

    boolean isLoading = false;
    public static final int RequestPermissionCode = 1;
    Cursor cursor;
    String name, phonenumber, photo;

     int recyclerViewPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        if(savedInstanceState!=null){
            recyclerViewPosition = savedInstanceState.getInt("position");
            Log.e("position",":"+recyclerViewPosition);
            recyclerView.scrollToPosition(recyclerViewPosition);
        }

        EnableRuntimePermission();
        initScrollListener();
    }

    private void populateData() {
        int i = 0;
        while (i < 10) {
            rowsArrayList.add(StoreContacts.get(i));
            i++;
        }
    }

    private void initAdapter() {

        recyclerViewAdapter = new RecyclerViewAdapter(rowsArrayList);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(recyclerViewAdapter);

    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void loadMore() {
        rowsArrayList.add(null);
        recyclerViewAdapter.notifyItemInserted(rowsArrayList.size() - 1);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rowsArrayList.remove(rowsArrayList.size() - 1);
                int scrollPosition = rowsArrayList.size();
                recyclerViewAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;
                int nextLimit = 0;

                if(currentSize + 10 > StoreContacts.size()){
                    nextLimit = StoreContacts.size();
                }else{
                    nextLimit = currentSize + 10;
                }

                while (currentSize - 1 < nextLimit) {

                        rowsArrayList.add(StoreContacts.get(currentSize));
                        if (rowsArrayList.size() != StoreContacts.size()) {
                            currentSize++;
                        }else{
                            break;
                        }
                }

                recyclerViewAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
     }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        int  lastFirstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.e("VisiblePosition",":"+lastFirstVisiblePosition);
        savedInstanceState.putInt("position", lastFirstVisiblePosition); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }

    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_CONTACTS)) {

            Toast.makeText(MainActivity.this, "CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, "Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();
                    GetContactsIntoArrayList();
                    populateData();
                    initAdapter();
                    initScrollListener();
                } else {

                    Toast.makeText(MainActivity.this, "Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    public void GetContactsIntoArrayList() {

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

           // StoreContacts.add(name + " " + ":" + " " + phonenumber + " " + ":" + " " + photo);

            Contact contact =new Contact();
            contact.setName(name);contact.setNumber(phonenumber); contact.setImage(photo);
            StoreContacts.add(contact);

        }
        cursor.close();
    }
}

