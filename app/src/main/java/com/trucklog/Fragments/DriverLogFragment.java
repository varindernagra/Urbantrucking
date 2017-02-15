package com.trucklog.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.trucklog.Adapters.DriverLogAdapter;
import com.trucklog.Interfaces.AsyncTaskResponse;
import com.trucklog.Models.LoadModel;
import com.trucklog.R;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.ConvertUtils;
import com.trucklog.Utils.ShowProgressDialog;
import com.trucklog.Utils.TokenManage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.GONE;
import static com.trucklog.Utils.Constants.JSON;

public class DriverLogFragment extends BaseFragment {

    /**
     * Constansts for choose photo
     */
    private static final int CHOOSE_PHOTO_GALLERY = 0;
    private static final int CHOOSE_PHOTO_CAMERA = 1;
    private int CURRENT_SELECT_CHOOSE = -1;
    private static int REQUEST_CAMERA = 900;
    private static int SELECT_FILE = 901;

    private byte[] BITMAP_BYTE_ARRAY;
    /**
     * End for choose photo
     */


    LoadModel load_model;

    private int TOGGLE_CURRENT_SELECT;
    /**
     * UI for driver model
     */
    TextView txt_status;
    ToggleButton toggle_driver_atyard, toggle_driver_pick, toggle_driver_drop;

    TextView txt_driver_workorder, txt_driver_container, txt_driver_po, txt_driver_ramp, txt_driver_company, txt_driver_startaddr, txt_driver_destaddr, txt_driver_datetime, txt_driver_dispatcher, txt_driver_comment;
    Button bt_driver_driver, bt_driver_pdo_doc, bt_driver_loaddoc;

    TextView txt_driver_choosefile;
    Button bt_driver_choosefile;

    LinearLayout layout_driver_logs;

    View.OnClickListener toggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            allToggleUncheckStatus();
            ((ToggleButton)view).setChecked(true);
            int id = view.getId();
            int tmp_current_selection_toggle = CalculateSelectedToggleIndex(id);
            if(tmp_current_selection_toggle == TOGGLE_CURRENT_SELECT)
                return;
            /**
             * do something when user select other toggle
             */
            TOGGLE_CURRENT_SELECT = tmp_current_selection_toggle;
            txt_status.setText(Constants.TITLE_DRIVER_TOGGLE[TOGGLE_CURRENT_SELECT]);
            updateLoadStatus();
        }
    };

    /***
     * Update Load Status method.
     */
    private void updateLoadStatus(){
        String token = TokenManage.getToken(getContext().getApplicationContext());
        ShowProgressDialog.showProgressDialog(getContext(),"Updating");
        UpdateLoadStatusService service = new UpdateLoadStatusService(createJsonObject(), token, new AsyncTaskResponse() {
            @Override
            public void response(Object o) {
                //it doesn work now.
                // should update api here
                try {
                    JSONObject result_object = new JSONObject((String) o);
                    load_model = new LoadModel(result_object);
                    initAllNecessaryViews();
                    updateServiceLoadmodel(load_model);
                }catch (Exception e){
                    e.printStackTrace();
                }
                ShowProgressDialog.hideProgressDialog();
            }
        });
        service.execute();
    }

    private void updateServiceLoadmodel(LoadModel model){
        parent.updateLoadList(model);
    }

    private void allToggleUncheckStatus(){
        toggle_driver_pick.setChecked(false);
        toggle_driver_drop.setChecked(false);
        toggle_driver_atyard.setChecked(false);
    }

    private int CalculateSelectedToggleIndex(int id){
        if(id == R.id.toggle_driver_dropped)
            return Constants.TOGGLE_DRIVER_DROPPED;
        if(id == R.id.toggle_driver_atyard)
            return Constants.TOGGLE_DRIVER_YARD;
        if(id == R.id.toggle_driver_pickup)
            return Constants.TOGGLE_DRIVER_PICK;

        return Constants.TOGGLE_DRIVER_PICK;
    }

    public DriverLogFragment() {
        // Required empty public constructor
    }

    public static DriverLogFragment newInstance(LoadModel model) {
        DriverLogFragment fragment = new DriverLogFragment();
        Bundle args = new Bundle();
        args.putSerializable("Load", model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        load_model = (LoadModel) getArguments().getSerializable("Load");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_driver_log, container, false);
        txt_status = (TextView)rootView.findViewById(R.id.txt_driver_status);
        toggle_driver_atyard = (ToggleButton)rootView.findViewById(R.id.toggle_driver_atyard);
        toggle_driver_pick = (ToggleButton)rootView.findViewById(R.id.toggle_driver_pickup);
        toggle_driver_drop = (ToggleButton)rootView.findViewById(R.id.toggle_driver_dropped);
        initToggleButtonListener();
        initToggleButtonAndStatusText();

        txt_driver_workorder = (TextView)rootView.findViewById(R.id.txt_driver_workorder);
        txt_driver_container = (TextView)rootView.findViewById(R.id.txt_driver_container);
        txt_driver_po = (TextView)rootView.findViewById(R.id.txt_driver_po);
        txt_driver_ramp = (TextView)rootView.findViewById(R.id.txt_driver_ramp);
        txt_driver_company = (TextView)rootView.findViewById(R.id.txt_driver_company);
        txt_driver_startaddr = (TextView)rootView.findViewById(R.id.txt_driver_startaddr);
        txt_driver_destaddr = (TextView)rootView.findViewById(R.id.txt_driver_destaddr);
        txt_driver_datetime = (TextView)rootView.findViewById(R.id.txt_driver_datetime);
        txt_driver_dispatcher = (TextView)rootView.findViewById(R.id.txt_driver_dispatcher);
        txt_driver_comment = (TextView)rootView.findViewById(R.id.txt_driver_comment);
        txt_driver_choosefile = (TextView)rootView.findViewById(R.id.txt_driver_choosefile);

        bt_driver_driver = (Button)rootView.findViewById(R.id.bt_driver_driver);
        bt_driver_pdo_doc = (Button)rootView.findViewById(R.id.bt_driver_pdo_doc);
        bt_driver_loaddoc = (Button)rootView.findViewById(R.id.bt_driver_loaddoc);
        bt_driver_choosefile = (Button)rootView.findViewById(R.id.bt_driver_choosefile);

        layout_driver_logs = (LinearLayout)rootView.findViewById(R.id.layout_driver_logs);

        initAllNecessaryViews();
        return rootView;
    }

    private void initAllNecessaryViews(){
        txt_driver_workorder.setText(load_model.getWork());
        txt_driver_container.setText(load_model.getContainer());
        txt_driver_po.setText(load_model.getPo());
        txt_driver_ramp.setText(load_model.getRamp());
        txt_driver_company.setText(load_model.getCompany());
        /**
         * startaddr
         */
        txt_driver_startaddr.setText(load_model.getPoint());
        /**
         * destaddr
         */
        txt_driver_destaddr.setText(load_model.getAddress());

        txt_driver_datetime.setText(load_model.getDate().toString().isEmpty()?"Never":load_model.getDate());
        try {
            txt_driver_dispatcher.setText(load_model.getShipperModel().getName());
        }catch (Exception e){
            txt_driver_dispatcher.setText("");
        }
        txt_driver_comment.setText(load_model.getComment());
        try {
            bt_driver_driver.setText(load_model.getDriverModel().getName());
        }catch (Exception e){
            bt_driver_driver.setVisibility(GONE);
        }

        bt_driver_pdo_doc.setText("");
        bt_driver_pdo_doc.setVisibility(GONE);

        bt_driver_loaddoc.setText("");
        bt_driver_loaddoc.setVisibility(GONE);

        /**
         * choose file
         */
        bt_driver_choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickChooseFile();
            }
        });

        InitDriverLog();
    }

    /**
     * Driver Log region initialize function
     */
    private void InitDriverLog(){
        try{
            layout_driver_logs.removeAllViews();
        }catch (Exception e){

        }
        DriverLogAdapter adapter = new DriverLogAdapter(load_model.getLogModel(), getContext());
        for(int i = 0 ; i < adapter.getCount(); i++) {
            layout_driver_logs.addView(adapter.getView(i,null,null));
        }
    }

    private void onClickChooseFile(){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    CURRENT_SELECT_CHOOSE = CHOOSE_PHOTO_CAMERA;
                    cameraIntent();
                    dialog.dismiss();
                } else if (items[item].equals("Choose from Library")) {
                    CURRENT_SELECT_CHOOSE = CHOOSE_PHOTO_GALLERY;
                    galleryIntent();
                    dialog.dismiss();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        txt_driver_choosefile.setText(getFileName(data.getData()));
        getByteArrayFromBitmap(bm);
        UploadDocumentToServer();
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txt_driver_choosefile.setText(destination.getName());
        getByteArrayFromBitmap(thumbnail);
        UploadDocumentToServer();
    }

    private void getByteArrayFromBitmap(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        BITMAP_BYTE_ARRAY = baos.toByteArray();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void initToggleButtonListener(){
        toggle_driver_atyard.setOnClickListener(toggleClickListener);
        toggle_driver_drop.setOnClickListener(toggleClickListener);
        toggle_driver_pick.setOnClickListener(toggleClickListener);
    }

    private void initToggleButtonAndStatusText(){
        TOGGLE_CURRENT_SELECT = load_model.getStatus() - 1;
        if(TOGGLE_CURRENT_SELECT == Constants.TOGGLE_DRIVER_DROPPED){
            toggle_driver_drop.setChecked(true);
        }
        else if(TOGGLE_CURRENT_SELECT == Constants.TOGGLE_DRIVER_PICK){
            toggle_driver_pick.setChecked(true);
        }
        else if(TOGGLE_CURRENT_SELECT == Constants.TOGGLE_DRIVER_YARD){
            toggle_driver_atyard.setChecked(true);
        }
        txt_status.setText(Constants.TITLE_DRIVER_TOGGLE[TOGGLE_CURRENT_SELECT]);
    }

    private void UploadDocumentToServer(){
        String token = TokenManage.getToken(getContext());
        ShowProgressDialog.showProgressDialog(getContext(),"Uploading");
        UploadDocumentService service = new UploadDocumentService(createUploadDocJsonObject(), token, new AsyncTaskResponse() {
            @Override
            public void response(Object o) {
                ShowProgressDialog.hideProgressDialog();
                try {
                    bt_driver_pdo_doc.setVisibility(View.VISIBLE);
                    bt_driver_loaddoc.setVisibility(View.VISIBLE);

                    txt_driver_choosefile.setText((String) o);
                    bt_driver_pdo_doc.setText((String) o);
                    bt_driver_loaddoc.setText((String) o);
                }catch (Exception e){
                    bt_driver_pdo_doc.setVisibility(View.GONE);
                    bt_driver_loaddoc.setVisibility(View.GONE);

                }
                Log.d("**upload**","upload");
            }
        });
        service.execute();
    }

    private JSONObject createJsonObject() {
        //{"setStatus",{"id":load.id,"status":status,"location":location}}
        JSONObject request_content = new JSONObject();
        JSONObject request_object = new JSONObject();
        try {
            request_content.put("id", Integer.parseInt(load_model.getUid()));
            request_content.put("status", TOGGLE_CURRENT_SELECT + 1);
            /***
             * Get current location here.
             */
            request_content.put("location",parent.getCurrentCityName());
            request_object.put("pitch","setStatus");
            request_object.put("data", request_content);

        }catch (JSONException e){
            e.printStackTrace();
        }
        return request_object;
    }

    private JSONObject createUploadDocJsonObject(){
        //{"pitch":"uploaddoc","data":{"id":load.id,"name":filename,"data":filedata}}
        JSONObject request_content = new JSONObject();
        JSONObject request_object = new JSONObject();
        try {
            request_content.put("id", Integer.parseInt(load_model.getUid()));
            request_content.put("name", txt_driver_choosefile.getText().toString().trim());
            String encoded_image = Base64.encodeToString(BITMAP_BYTE_ARRAY, Base64.DEFAULT);
            String extension = ConvertUtils.getFileExt(txt_driver_choosefile.getText().toString().trim());

            request_content.put("data", prefixGenerate(extension) + encoded_image);
            request_object.put("pitch","uploaddoc");
            request_object.put("data", request_content);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return request_object;
    }

    public String prefixGenerate(String ext){
        return "data:image/" + ext + ";base64,";
    }

    public class UpdateLoadStatusService extends AsyncTask {
        AsyncTaskResponse response;
        JSONObject request_object;
        OkHttpClient okhttp;
        String token;
        public UpdateLoadStatusService(JSONObject request_object, String token, AsyncTaskResponse response){
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

    public class UploadDocumentService extends AsyncTask {
        AsyncTaskResponse response;
        JSONObject request_object;
        OkHttpClient okhttp;
        String token;
        public UploadDocumentService(JSONObject request_object, String token, AsyncTaskResponse response){
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
