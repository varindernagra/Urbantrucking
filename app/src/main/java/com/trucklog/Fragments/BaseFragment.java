package com.trucklog.Fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.trucklog.HomeActivity;
import com.trucklog.Interfaces.HomeActivityInterface;
import com.trucklog.Interfaces.ServiceInstanceListener;

/**
 * Created by rock on 1/31/17.
 */

public class BaseFragment extends Fragment implements ServiceInstanceListener{
    HomeActivityInterface parent;
    @Override
    public void serviceUpdated() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
            parent = (HomeActivityInterface)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }
}
