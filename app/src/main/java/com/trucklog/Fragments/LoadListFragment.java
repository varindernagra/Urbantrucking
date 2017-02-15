package com.trucklog.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.trucklog.Adapters.LoadListAdapter;
import com.trucklog.HomeActivity;
import com.trucklog.Interfaces.AsyncTaskResponse;
import com.trucklog.Interfaces.ServiceInstanceListener;
import com.trucklog.Models.LoadModel;
import com.trucklog.R;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.NewLoadManage;
import com.trucklog.Utils.TokenManage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.trucklog.Utils.Constants.JSON;


public class LoadListFragment extends BaseFragment {
    /**
     * Listener for radio button group
     */
    static final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);

            }
        }
    };

    /**
     * Listener for toggle button click
     */
    View.OnClickListener toggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((ToggleButton)view).setChecked(true);
            onToggleClick(view);
            initListBasedOnSelectedItem();
        }
    };

    ToggleButton toggle_all, toggle_active, toggle_inactive;
    RadioGroup radio_group;
    ListView list_loads;
    ProgressBar pg_indicator;
    int current_active_toggle_index = -1;
    ArrayList<LoadModel> load_array_list;
    LoadListAdapter adapter_load;

    public LoadListFragment() {
        // Required empty public constructor
    }

    public static LoadListFragment newInstance() {
        LoadListFragment fragment = new LoadListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_load_list, container, false);
        pg_indicator = (ProgressBar)rootView.findViewById(R.id.pg_indicator);
        radio_group = (RadioGroup)rootView.findViewById(R.id.radiogroup_home);
        toggle_all = (ToggleButton)rootView.findViewById(R.id.toggle_home_all);
        toggle_active = (ToggleButton)rootView.findViewById(R.id.toggle_home_active);
        toggle_inactive = (ToggleButton)rootView.findViewById(R.id.toggle_home_inactive);
        list_loads = (ListView)rootView.findViewById(R.id.list_home_load);
        radio_group.setOnCheckedChangeListener(ToggleListener);
        toggle_all.setOnClickListener(toggleClickListener);
        toggle_inactive.setOnClickListener(toggleClickListener);
        toggle_active.setOnClickListener(toggleClickListener);
        initLoadList();
        return rootView;
    }

    private void initLoadList(){
        load_array_list = new ArrayList<>();
        pg_indicator.setVisibility(View.VISIBLE);
        String token = TokenManage.getToken(getContext());
        LoadJsonArrayService service = new LoadJsonArrayService(createLoadJsonObject(), token, new AsyncTaskResponse() {
            @Override
            public void response(Object o) {
                try {
                    JSONArray result_array = new JSONArray((String) o);
                    for(int i = 0 ; i < result_array.length() ; i++){
                        LoadModel model = new LoadModel(result_array.getJSONObject(i));
                        model.setSeq_index(i+1);
                        load_array_list.add(model);
                    }
                    checkNewLoad();
                    parent.saveLoadList(load_array_list);

                    initListBasedOnSelectedItem();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        service.execute();
    }

    public void checkNewLoad(){
        int[] new_load_list = NewLoadManage.getNewLoads(getContext());
        if(new_load_list != null) {
            for (int i = 0; i < new_load_list.length; i++) {
                for (int j = 0; j < load_array_list.size(); j++) {
                    if (Integer.parseInt(load_array_list.get(j).getUid()) == new_load_list[i]) {
                        load_array_list.get(j).setIs_new(true);
                    }
                }
            }
        }
    }

    public void initListBasedOnSelectedItem(){
        int new_active_toggle_index = getCurrentActiveTogglePosition();
        if(new_active_toggle_index != current_active_toggle_index) {
            load_array_list = parent.getLoadList();
            current_active_toggle_index = new_active_toggle_index;
            displayListView();
        }
    }

    public void displayListView(){
        filterArrayList();
        adapter_load = new LoadListAdapter(getContext(), load_array_list);
        list_loads.setAdapter(adapter_load);
        list_loads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    NewLoadManage.removeNewLoad(getContext(), Integer.parseInt(load_array_list.get(i).getUid()));
                    load_array_list.get(i).setIs_new(false);
                    parent.saveObjectToParent(load_array_list.get(i));
                    parent.addFragment(Constants.FRAGMENT_DRIVER_LIST);

                }
        });
        pg_indicator.setVisibility(View.GONE);
    }

    public void filterArrayList(){
        ArrayList<LoadModel> tmp_list = new ArrayList<>();

        if(current_active_toggle_index == Constants.TOGGLE_INACTIVE){
            //filter for inactive loads
            for(int i = 0 ; i < load_array_list.size(); i++){
                if(load_array_list.get(i).getStatus() == 3){
                    tmp_list.add(load_array_list.get(i));
                }
            }
            load_array_list = tmp_list;
        }
        else if(current_active_toggle_index == Constants.TOGGLE_ACTIVE){
            //filter for active loads
            for(int i = 0 ; i < load_array_list.size(); i++){
                if(load_array_list.get(i).getStatus() != 3){
                    tmp_list.add(load_array_list.get(i));
                }
            }
            load_array_list = tmp_list;
        }
        tmp_list = null;
    }

    public void onToggleClick(View view){
        ((RadioGroup)view.getParent()).check(view.getId());
    }

    public int getCurrentActiveTogglePosition(){
        int ret_val = 0;
        for (int index = 0; index < radio_group.getChildCount(); index++) {
            final ToggleButton view = (ToggleButton) radio_group.getChildAt(index);
            if(view.isChecked() == true) {
                ret_val = index;
            }
        }
        return ret_val;
    }

    @Override
    public void serviceUpdated() {
        super.serviceUpdated();
        current_active_toggle_index = -1;
        list_loads.setAdapter(null);
        list_loads.setOnItemClickListener(null);
        initLoadList();
    }

    /******
     * AsyncTask For load list
     */
    private JSONObject createLoadJsonObject() {
        JSONObject request_content = new JSONObject();
        try {
            request_content.put("pitch", "loads");
            request_content.put("data", "");
        }catch (JSONException e){

        }
        return request_content;
    }
    public class LoadJsonArrayService extends AsyncTask {
        AsyncTaskResponse response;
        JSONObject request_object;
        String token;
        OkHttpClient okhttp;
        public LoadJsonArrayService(JSONObject request_object, String token, AsyncTaskResponse response){
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

}
