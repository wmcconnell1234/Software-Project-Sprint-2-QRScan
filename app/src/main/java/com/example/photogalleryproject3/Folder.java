package com.example.photogalleryproject3;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

//The purpose of this class is to create the folder activity and pass the user's chosen folder back to MainActivity
public class Folder extends AppCompatActivity implements View.OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Show folder activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        //Set up listeners
        TextView folderAll = (TextView) findViewById(R.id.textViewFolderAll);
        TextView folder1 = (TextView) findViewById(R.id.textViewFolder1);
        TextView folder2 = (TextView) findViewById(R.id.textViewFolder2);
        TextView folder3 = (TextView) findViewById(R.id.textViewFolder3);
        folder1.setOnClickListener(this);
        folder2.setOnClickListener(this);
        folder3.setOnClickListener(this);
        folderAll.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        //Find out what was clicked
        int id = v.getId();
        TextView tv = (TextView) findViewById(id);

        //Send it back
        Intent i = new Intent();
        i.putExtra("FOLDER", tv.getText().toString());
        setResult(RESULT_OK, i);
        finish();
    }
}
