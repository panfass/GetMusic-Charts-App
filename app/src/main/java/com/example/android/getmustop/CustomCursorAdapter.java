package com.example.android.getmustop;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class CustomCursorAdapter extends CursorAdapter {

    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        return LayoutInflater.from(context).inflate(R.layout.single_row_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views
        TextView tvBody = (TextView) view.findViewById(R.id.islistname);
        TextView tvPriority = (TextView) view.findViewById(R.id.isdate);

        String body = cursor.getString(cursor.getColumnIndexOrThrow("listname"));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow("date"));

        tvBody.setText(body);
        tvPriority.setText(priority);
    }
}
