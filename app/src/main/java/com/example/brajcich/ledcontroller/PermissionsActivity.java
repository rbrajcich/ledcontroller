package com.example.brajcich.ledcontroller;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PermissionsActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(PermissionsActivity.this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                        MY_PERMISSION_ACCESS_COURSE_LOCATION );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSION_ACCESS_COURSE_LOCATION
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            finish();
        }

    }

    @Override
    public void onBackPressed() {
        //do nothing - user can't go back until they allow permission
    }
}
