package com.trucklog.Interfaces;

import com.trucklog.LoadService;
import com.trucklog.Models.LoadModel;

import java.util.ArrayList;

/**
 * Created by rock on 1/31/17.
 */

public interface HomeActivityInterface {
    void addFragment(int fragment_index);
    void saveObjectToParent(Object object);
    String getCurrentCityName();
    double getCurrentLatitude();
    double getCurrentLongitude();

    void saveLoadList(ArrayList list);
    ArrayList getLoadList();
    void updateLoadList(LoadModel model);
}
