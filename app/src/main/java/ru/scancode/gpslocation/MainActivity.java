package ru.scancode.gpslocation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 *
 * Google GPS location
 *
 * Deprecated FusedLocationApi and FusedLocationProviderClient
 * @author arvalon
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fused_location_api).setOnClickListener(v -> {
            startActivity(new Intent(this, MyFusedLocationApi.class));
        });

        findViewById(R.id.fused_location_provider_client).setOnClickListener(v -> {
            startActivity(new Intent(this, MyFusedLocationProviderClient.class));
        });

    }
}
