package com.example.myfirstmojio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mojio.mojiosdk.MojioClient;
import com.mojio.mojiosdk.models.User;
import com.mojio.mojiosdk.models.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {

    //===========================================================================
    // Mojio App Setup
    // These values will match the keys given to you for your Mojio application in the
    // Mojio Developer panel.
    //===========================================================================
    private final static String MOJIO_APP_ID = "<YOUR_APP_ID>";
    private final static String REDIRECT_URL = "<YOUR_APP_REDIRECT>://"; // Example "myfirstmojio://"

    //===========================================================================
    // Activity properties
    //===========================================================================
    // Activity request ID to allow us to listen for the OAuth2 response
    private static int OAUTH_REQUEST = 0;

    // The main mojio client object; allows login and data retrieval to occur.
    private MojioClient mMojio;

    private User mCurrentUser;
    private Vehicle[] mUserVehicles;

    private Button mLoginButton;
    private TextView mUserName, mUserEmail;
    private ListView mVehicleList;
    private MapFragment mMap;

    //===========================================================================
    // Activity implementation
    //===========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup mojio client with app keys.
        mMojio = new MojioClient(this, MOJIO_APP_ID, null, REDIRECT_URL);

        mUserName = (TextView)findViewById(R.id.user_name);
        mUserEmail = (TextView)findViewById(R.id.user_email);
        mVehicleList = (ListView)findViewById(R.id.vehicle_list);
        mMap = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);

        mLoginButton = (Button)findViewById(R.id.oauth2_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOauth2Login();
            }
        });
    }

    // IMPORTANT: This uses the com.mojio.mojiosdk.networking.OAuthLoginActivity class.
    // For this to work correctly, we must declare it as an Activity in our app's AndroidManifest.xml file.
    private void doOauth2Login() {
        // Launch the OAuth request; this will launch a web view Activity for the user enter their login.
        // When the Activity finishes, we listen for it in the onActivityResult method
        mMojio.launchLoginActivity(this, OAUTH_REQUEST);

    }

    // IMPORTANT: Must be overridden so that we can listen for the OAuth2 result and know if we were
    // logged in successfully. We do not have to bother with storing the auth tokens, the SDK codes that
    // for us.
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_LONG).show();
                getCurrentUser(); // Now attempt to get user info
            }
            else {
                Toast.makeText(MainActivity.this, "Problem logging in", Toast.LENGTH_LONG).show();
            }
        }
    }

    // We have our access token stored now with the client, but we now need to grab our user ID.
    private void getCurrentUser() {
        String entityPath = "Users"; // TODO need userID?
        HashMap<String, String> queryParams = new HashMap<>();

        mMojio.get(User[].class, entityPath, queryParams, new MojioClient.ResponseListener<User[]>() {
            @Override
            public void onSuccess(User[] result) {
                // Should have one result
                try {
                    mCurrentUser = result[0]; // Save user info so we can use ID later

                    // Show user data
                    mUserName.setText("Hello " + mCurrentUser.FirstName + " " + mCurrentUser.LastName);
                    mUserEmail.setText(mCurrentUser.Email);
                    mLoginButton.setVisibility(View.GONE);

                    getUserVehicles();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Problem getting users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Problem getting users", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Now that we have the current user, we can use their ID to get data
    private void getUserVehicles() {
        String entityPath = String.format("Users/%s/Vehicles", mCurrentUser._id);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("sortBy", "Name");
        queryParams.put("desc", "true");

        mMojio.get(Vehicle[].class, entityPath, queryParams, new MojioClient.ResponseListener<Vehicle[]>() {
            @Override
            public void onSuccess(Vehicle[] result) {
                mUserVehicles = result; // Save

                if (mUserVehicles.length == 0) {
                    Toast.makeText(MainActivity.this, "No vehicles found", Toast.LENGTH_LONG).show();
                }

                // Create list data from result
                ArrayList<String> listData = new ArrayList<String>();
                Vehicle v;
                for (int i=0; i < mUserVehicles.length; i++) {
                    v = result[i];
                    listData.add(String.format("%s %s", v.getNameDescription(), v.LicensePlate));
                }

                // Show result in list
                ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listData);
                mVehicleList.setAdapter(itemsAdapter);

                putVehiclesOnMap();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Problem getting vehicles", Toast.LENGTH_LONG).show();
            }
        });
    }

    // If we are here then we must already have vehicles!
    // Load the MapFragment as recommended by google, and then display the vehicle locations
    private void putVehiclesOnMap() {
        // Setup map!
        mMap.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Map is ready
        LatLng vPos = new LatLng(49.282, -123.1207); // Default to Vancouver
        Vehicle v;

        // Iterate over each vehicle and show on the map
        for (int i=0; i < mUserVehicles.length; i++) {
            v = mUserVehicles[i];
            try {
                vPos = new LatLng(v.LastLocation.Lat, v.LastLocation.Lng);
                map.addMarker(new MarkerOptions()
                        .title(v.getNameDescription())
                        .position(vPos));
            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, "No location for " + v.getNameDescription(), Toast.LENGTH_LONG).show();
            }
        }

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(vPos, 12));
    }

}
