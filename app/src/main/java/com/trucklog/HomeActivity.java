package com.trucklog;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;
import com.trucklog.Fragments.BaseFragment;
import com.trucklog.Fragments.DriverLogFragment;
import com.trucklog.Fragments.LoadListFragment;
import com.trucklog.Interfaces.AsyncTaskResponse;
import com.trucklog.Interfaces.HomeActivityInterface;
import com.trucklog.Models.LoadModel;
import com.trucklog.Models.UserModel;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.GpsUtils;
import com.trucklog.Utils.NewLoadManage;
import com.trucklog.Utils.TokenManage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.trucklog.Utils.Constants.JSON;

public class HomeActivity extends AppCompatActivity implements HomeActivityInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    TextView toolbar_user_name;
    CircleImageView user_image_view;
    UserModel model;
    Object obj_savedResult;

    private boolean should_restart = false;


    /**
     * GPS location variables
     */

    private AddressResultReceiver mResultReceiver;
    private String current_address = "";
    private double current_latitude = 0, current_longitude = 0;
    protected Address address;
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    ArrayList<LoadModel> loadModelArrayList;
    static HomeActivity _this;
    /**
     * Handler for Messenger of service
     */
    static Handler service_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == Constants.MESSAGE_NOTIFITY) {
                try {
                    /**
                     * If new load is updated, then get new load and display driver log fragment.
                     */
                    final int new_load_id = msg.arg2;
                    new AlertDialog.Builder(_this)
                            .setTitle("New Load")
                            .setMessage("New Load assigned.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    _this.setNotificationContent(new_load_id);
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void setNotificationContent(int new_load_id){
        /**
         * Service to get load information according to new_load_id;
         */
        JSONObject json_object_update = createLoadJsonObjectWithId(new_load_id);
        String token = TokenManage.getToken(HomeActivity.this);
        LoadJsonBasedOnIdService service = new LoadJsonBasedOnIdService(json_object_update, token, new AsyncTaskResponse() {
            @Override
            public void response(Object o) {
                Log.d("**Load data**","****new load****");
                try {
                    JSONObject object = new JSONObject((String) o);
                    LoadModel loadModel = new LoadModel(object);
                    loadModel.setIs_new(true);
                    loadModelArrayList.add(loadModel);
                    obj_savedResult = loadModel;
                    attachFragment(DriverLogFragment.newInstance(loadModel));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        service.execute();
    }


    private void serviceInitialize() {

        if (LoadService.getInstance() == null) {
            Intent service_intent = new Intent(this, LoadService.class);
            service_intent.putExtra(LoadService.EXTRA_MESSENGER, new Messenger(service_handler));
            startService(service_intent);
        }
        else{
            LoadService.getInstance().setIs_HomeActivity_Available(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
        model = (UserModel) getIntent().getSerializableExtra("user");
        String original_email = TokenManage.getUserEmail(this);
        if(original_email == null){
            TokenManage.setEmail(this, model.getEmail());
        }
        else {
            if (original_email.equals(model.getEmail()) == false) {
                NewLoadManage.removeAllNewLoad(this);
                TokenManage.setEmail(this, model.getEmail());
            }
        }

        addFragment(Constants.FRAGMENT_LOAD_LIST);
        serviceInitialize();
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar_user_name = (TextView) findViewById(R.id.txt_toolbar_username);
        toolbar_user_name.setText(model.getName());

        user_image_view = (CircleImageView) findViewById(R.id.profile_image);
        Picasso.with(this)
                .load(model.getImage_url())
                .into(user_image_view);

        mResultReceiver = new AddressResultReceiver(null);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        InitLoactionProvider();
    }


    /**
     * Initialize GPS Provider
     */
    private void InitLoactionProvider() {

        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = GpsUtils.checkGpsAvailable(location_manager);
        boolean network_enabled = GpsUtils.checkNetworkAvailable(location_manager);
        /**
         * If nothing is available, open setting screen.
         */
        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
            dialog.setMessage("Can't find Location. Choose location provider.");
            dialog.setPositiveButton("GPS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);
                    paramDialogInterface.dismiss();
                }
            });
            dialog.show();
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, GeocodeAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mGoogleApiClient.disconnect();
            LoadService.getInstance().setIs_HomeActivity_Available(false);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void addFragment(int idx) {
        Fragment f = null;
        switch (idx) {
            case 0:
                f = LoadListFragment.newInstance();
                break;
            case 1:
                f = DriverLogFragment.newInstance((LoadModel) obj_savedResult);
                break;
            default:
                f = LoadListFragment.newInstance();
        }
        attachFragment(f);
    }

    private void attachFragment(Fragment f) {
        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_container, f, null).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_load) {
            addFragment(Constants.FRAGMENT_LOAD_LIST);
        } else if (id == R.id.action_logout) {
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            LoadService.getInstance().setIs_HomeActivity_Available(false);
            startActivity(i);
            finish();
        }
        return true;
    }

    @Override
    public void saveLoadList(ArrayList list) {
        loadModelArrayList = new ArrayList<>();
        loadModelArrayList = list;
    }

    @Override
    public ArrayList getLoadList() {
        return loadModelArrayList;
    }

    @Override
    public void updateLoadList(LoadModel model) {
        for(int i = 0 ; i < loadModelArrayList.size(); i++){
            if(loadModelArrayList.get(i).getUid() == model.getUid()){
                loadModelArrayList.set(i, model);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void saveObjectToParent(Object object) {
        obj_savedResult = object;
    }

    @Override
    public String getCurrentCityName() {
        return current_address;
    }

    @Override
    public double getCurrentLatitude() {
        return current_latitude;
    }

    @Override
    public double getCurrentLongitude() {
        return current_longitude;
    }


    @Override
    public void onLocationChanged(Location location) {
        startIntentService(location);
    }

    /**
     * Google API listener
     */

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            /**
             * update current position with last position
             */
            startIntentService(mLastLocation);
        }

        LocationRequest mLocationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            return;
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onConnectionSuspended ( int i){
        Log.d("suspend","suspend");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(i);
        LoadService.getInstance().setIs_HomeActivity_Available(false);
        finish();
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult){
        Log.d("failed","failed");
        Toast.makeText(this, "Upgrade your google play service", Toast.LENGTH_SHORT).show();
    }

    /**
     * Class for ResultReceiver
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            address = resultData.getParcelable("ADDRESS_DATA");
            current_latitude = resultData.getDouble("LATITUDE");
            current_longitude = resultData.getDouble("LONGITUDE");

            if (address != null) {
                current_address = String.format("%s, %s", address.getCountryName(), address.getAddressLine(0));
            }
            String token = TokenManage.getToken(HomeActivity.this);
            UpdateUserPosition update_service = new UpdateUserPosition(createJsonObject(), token);
            update_service.execute();
        }
    }

    private JSONObject createJsonObject(){
        //{"updateProfile":{"longitude":longitude,"latitude":latitude,"id":id}}
        JSONObject request_content = new JSONObject();
        JSONObject request_data = new JSONObject();
        try {
            request_content.put("longitude", current_longitude);
            request_content.put("latitude", current_latitude);
            request_content.put("id", model.getId());
            request_data.put("updateProfile", request_content);
        }catch (Exception e){
            e.printStackTrace();
        }
        return request_data;
    }

    public class UpdateUserPosition extends AsyncTask{

        JSONObject request_object;
        String token;
        OkHttpClient okhttp;
        public UpdateUserPosition(JSONObject request_object, String token){
            this.request_object = request_object;
            this.token  = token;
            okhttp = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            RequestBody body = RequestBody.create(JSON, request_object.toString());
            Request request = new Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Accept","application/json")
                    .addHeader("Authorization",token)
                    .url(Constants.SERVER_URL)
                    .post(body)
                    .build();
            try {
                Response response = okhttp.newCall(request).execute();
                return response.body().string();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

    private JSONObject createLoadJsonObjectWithId(int id) {
        JSONObject request_content = new JSONObject();
        try {
            request_content.put("pitch", "load");
            request_content.put("data", id + "");
        }catch (JSONException e){

        }
        return request_content;
    }

    public class LoadJsonBasedOnIdService extends AsyncTask {
        AsyncTaskResponse response;
        JSONObject request_object;
        String token;
        OkHttpClient okhttp;
        public LoadJsonBasedOnIdService(JSONObject request_object, String token, AsyncTaskResponse response){
            this.response = response;
            this.request_object = request_object;
            this.token = token;
            okhttp = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            RequestBody body = RequestBody.create(JSON, request_object.toString());
            Request request = new Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Accept","application/json")
                    .addHeader("Authorization",token)
                    .url(Constants.SERVER_URL)
                    .post(body)
                    .build();
            try {
                Response response = okhttp.newCall(request).execute();
                return response.body().string();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            response.response(o);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
