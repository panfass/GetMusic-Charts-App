package com.example.android.getmustop;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class Istorikoclass extends AppCompatActivity {
    ListView istoriko;


    DBHandler db = new DBHandler(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_istoriko);
        istoriko = (ListView) findViewById(R.id.contentlist);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, db.getAllIstoriko());

        istoriko.setAdapter(adapter);


    }







    }
