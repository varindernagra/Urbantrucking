package com.trucklog;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.trucklog.Utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocodeAddressIntentService extends IntentService {

    private String errorMessage;
    protected ResultReceiver mReceiver;
    protected  Location mLocation;
    public GeocodeAddressIntentService() {
        super("GeocodeAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLocation = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        Geocoder mGeocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(),1);
        } catch (IOException ioException) {
            errorMessage = "service_not_available";
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "invalid_lat_long_used";
        }
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found";
            }
            deliverResultToReceiver(Constants.FAILURE_SERVICE_RESULT, errorMessage,null, mLocation.getLatitude(), mLocation.getLongitude());
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(Constants.SUCCESS_SERVICE_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments), address, mLocation.getLatitude(), mLocation.getLongitude());
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address adrs, double latitude, double longitude) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putParcelable("ADDRESS_DATA",adrs);
        bundle.putDouble("LATITUDE", latitude);
        bundle.putDouble("LONGITUDE", longitude);
        mReceiver.send(resultCode, bundle);
    }
}
