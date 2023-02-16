package ru.scancode.gpslocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

/**
 * FusedLocationProviderClient
 *
 * https://stackoverflow.com/a/47951456/6346970
 */
public class MyFusedLocationProviderClient extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 14;

    private static final long UPDATE_INTERVAL = 1500, FASTEST_INTERVAL = 1500;

    private TextView locationTv;
    // private AddressResultReceiver mResultReceiver;
    // removed here because cause wrong code when implemented and
    // its not necessary like the author says

    //Define fields for Google API Client
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fused_location_provider_client);

        Logs.info(this,"onCreate");

        locationTv = findViewById(R.id.location);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try{

            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    Logs.info(this,"onSuccessListener");
                    showLocation();
                }
            });

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Logs.info(this,"onLocationResult, locations count: "
                            +locationResult.getLocations().size());

                    for (Location location : locationResult.getLocations()) {

                        // Update location data and UI
                        Logs.info(this, shortLocation(location));

                        lastLocation=location;

                        showLocation();
                    }
                }
            };

        }catch (SecurityException ex){
            Logs.error(this,ex.getMessage(),ex);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Logs.info(this,"onStart");

        if (!checkPermissions()) {
            startLocationUpdates();
            requestPermissions();
        } else {
            getLastLocation();
            startLocationUpdates();
        }

    }

    @Override
    public void onPause() {
        Logs.info(this,"onPause");
        stopLocationUpdates();
        super.onPause();
    }

    /** Return the current state of the permissions needed */
    private boolean checkPermissions() {

        Logs.info(this,"checkPermissions");

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {

        Logs.info(this,"startLocationPermissionRequest");

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {

        Logs.info(this,"requestPermission");

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Logs.info(this, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    view -> {
                        // Request permission
                        startLocationPermissionRequest();
                    });

        } else {
            Logs.info(this, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /** Callback received when a permissions request has been completed. */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Logs.info(this, "onRequestPermissionResult");

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Logs.info(this, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);

                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
            }
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {

        Logs.info(this,"getLastLocation");

        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        Logs.info(this,"onCompleteListener");

                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();

                            Logs.info(this,"onCompleteListener last "
                                    +shortLocation(lastLocation));

                            showLocation();

                        } else {

                            Logs.info(this,"onCompleteListener NO LAST LOCATION");
                            showSnackbar(R.string.no_location_detected, android.R.string.ok, null);

                            if (task.getException()!=null){
                                Logs.error(this, task.getException().getMessage(), task.getException());
                            }
                        }
                    }
                });
    }

    private void stopLocationUpdates() {
        Logs.info(this,"stopLocationUpdates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {

        Logs.info(this,"startLocationUpdates");

        if (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    private void showSnackbar(final int mainTextStringId,
                              final int actionStringId,
                              View.OnClickListener listener) {

        Logs.info(this,"showSnackbar");

        Snackbar.make(this.findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_SHORT)
                .setAction(getString(actionStringId), listener).show();
    }

    private void showLocation(){
        if (lastLocation!=null){
            locationTv.setText("Latitude : " + lastLocation.getLatitude()
                    + "\nLongitude : " + lastLocation.getLongitude());
        }
    }

    private String shortLocation(Location loc){

        return "location, latitude: " +loc.getLatitude() +", longitude: "+loc.getLongitude();
    }
}