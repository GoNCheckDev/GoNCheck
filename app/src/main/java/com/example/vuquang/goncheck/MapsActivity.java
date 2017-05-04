package com.example.vuquang.goncheck;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.graphics.*;
import android.widget.*;
import android.view.*;

import com.example.directionlibrary.DirectionCallback;
import com.example.directionlibrary.GoogleDirection;
import com.example.directionlibrary.constant.TransportMode;
import com.example.directionlibrary.model.Direction;
import com.example.directionlibrary.util.DirectionConverter;
import com.example.library.SlidingUpPanelLayout;
import com.example.library.SlidingUpPanelLayout.PanelSlideListener;
import com.example.library.SlidingUpPanelLayout.PanelState;


import com.example.vuquang.goncheck.DAO.CheckedPlaceDAO;
import com.example.vuquang.goncheck.model.CheckedPlace;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,DirectionCallback{
    private static final String TAG = "DemoActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1889;
    private static final int S_WIDTH = 120;
    private static final int S_HEIGHT = 120;
    private static final int B_WIDTH = 600;
    private static final int B_HEIGHT = 600;

    public static final String pathAlbum = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/" + "GoNCheck/";
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    //Spinner items
    List<String> itemsShort;
    List<String> itemsLong;

    //Get map
    private GoogleMap mMap;
    LocationManager locationManager;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    //Get addr from Latit and Longit
    Geocoder geocoder;

    //save current place
    String curPlace;
    Address curAddr;

    //Manage Db
    CheckedPlaceDAO checkedPlaceDAO;

//    Button btnLocation;
    private SlidingUpPanelLayout mLayout;
    ViewGroup scrollViewgroup;
    TextView textNamePlace;
    ImageView imageSelected;
    //Small image
    Bitmap[] thumbnails = null;

    //Direction
    private LatLng origin;
    private LatLng destination;


    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //updateMyCurrentLoc(location);
            locationManager.removeUpdates(locationListener);
        }

        public void onProviderDisabled(String provider){
            //updateMyCurrentLoc(null);
            locationManager.removeUpdates(locationListener);
        }

        public void onProviderEnabled(String provider){ }
        public void onStatusChanged(String provider, int status,Bundle extras){ }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.ENGLISH);
        //Create realm db
        Realm.init(this);
        checkedPlaceDAO = new CheckedPlaceDAO();

        //Add PlaceAutoComplete
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("VN")
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        textNamePlace = (TextView) findViewById(R.id.name);

        imageSelected = (ImageView) findViewById(R.id. imageSelected);

        scrollViewgroup = (ViewGroup) findViewById(R.id. viewgroup);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        });

    }

    private void loadSlidePanel(){
        if (thumbnails != null) {
            scrollViewgroup.removeAllViews();
            for (int i = 0; i < thumbnails.length; i++) {
                //create single frames [icon] using XML inflater
                final View singleFrame = getLayoutInflater().inflate(
                        R.layout.frame_icon_scroll_view, null);
                singleFrame.setId(i);
                ImageView icon = (ImageView) singleFrame.findViewById(R.id.icon);
                icon.setImageBitmap(Bitmap.createScaledBitmap(thumbnails[i], S_WIDTH, S_HEIGHT, false));
                //add frame to the scrollView
                scrollViewgroup.addView(singleFrame);
                //each single frame gets its own click listener
                singleFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeImage(singleFrame.getId());
                    }
                }); // listener
            }// for – populating ScrollView
        }
    }

    //display a high-quality version of the image selected using thumbnails
    protected void showLargeImage(int frameId) {
        Drawable selectedLargeImage = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(thumbnails[frameId], B_WIDTH, B_HEIGHT, false));
        imageSelected.setBackground(selectedLargeImage);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, this, -1).show();
            finish();
        } else {
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // no network provider is enabled
                } else {
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            getLocation();
                            return true;
                        }
                    });
                }
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
            }
        }
        //click marker event
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getTitle();
                textNamePlace.setText(title);
                if(checkedPlaceDAO.getCheckedPlace(title)!= null) {
                    loadPic(checkedPlaceDAO.getCheckedPlace(title).getId());
                    loadSlidePanel();
                }
                marker.showInfoWindow();
                //set des to marker
                destination =marker.getPosition();
                return true;
            }
        });
        getLocation();
    }

    //Load items
    private void loadItemsForSpinner(Spinner spinner){
        List<CheckedPlace> listPlace = loadListPlace();
        itemsLong = new ArrayList<>();
        itemsShort = new ArrayList<>();
        for(CheckedPlace place:listPlace) {
            itemsLong.add(place.getPlaceAddr());
            itemsShort.add(place.getPlaceAddr().substring(0,12));
        }
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                itemsShort)); // set the adapter

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        //Load items
        loadItemsForSpinner(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CheckedPlace checkedPlace = checkedPlaceDAO.getCheckedPlace(itemsLong.get(position));
                moveCameraTo(checkedPlace.getLatitude(),checkedPlace.getLongitude());
                destination = new LatLng(checkedPlace.getLatitude(),checkedPlace.getLongitude());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// user clicked a menu-item from ActionBar
        switch (item.getItemId()){
            case R.id.action_camera: {
                getLocation();
                cameraAct();
                return true;
            }//Camera
            case R.id.action_search: {
                placeAutoCompleteAct();
                return true;
            }//AutoComplete
            case R.id.action_share: {
                //Direction
                requestDirection();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }//SlidePanel

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            handleBigCameraPhoto();
            loadListPlace();
            loadPic(checkedPlaceDAO.getCheckedPlace(curPlace).getId());
            textNamePlace.setText(curPlace.toString());
        }
        else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                moveCameraToPlace(place);
                destination = place.getLatLng();
                Log.i(TAG, "Place: " + place.getName());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void moveCameraToPlace(Place place) {
        LatLng latLng = place.getLatLng();
        final CharSequence name = place.getName();
        // tạo marker trên map
        MarkerOptions optionX=new MarkerOptions();
        optionX.position(latLng);
        optionX.title(name.toString());

        //optionX.title();
        optionX.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        Marker curMarker = mMap.addMarker(optionX);
        // chuyển camera đến marker vừa được chọn
        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(15)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        curMarker.showInfoWindow();

    }

    private Address getAddrCurrentLocation(double latitide, double longitude) {
        Address myAddress = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitide, longitude, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                myAddress = returnedAddress;
            }
            else {
                //Do sth
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return myAddress;
    }

    private void moveCameraTo(double latitude,double longitude) {
            LatLng latLng = new LatLng(latitude, longitude);
            //Move camera to pos
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void markLocation(CheckedPlace checkedPlace) {
        //Get addr
        LatLng latLng=new LatLng(checkedPlace.getLatitude(), checkedPlace.getLongitude());
        Address addr = getAddrCurrentLocation(checkedPlace.getLatitude(),checkedPlace.getLongitude());

        MarkerOptions option=new MarkerOptions();
        String name = checkedPlace.getPlaceAddr();
        String diaChi = "";
        for(int i=1;i<addr.getMaxAddressLineIndex();i++) {
            diaChi += addr.getAddressLine(i);
            if(i < addr.getMaxAddressLineIndex()-1) {
                diaChi+=", ";
            }
        }
        option.title(name);
        option.snippet(diaChi);
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        option.position(latLng);
        Marker currentMarker= mMap.addMarker(option);
        currentMarker.showInfoWindow();
    }

    private void updateMyCurrentLoc(Location location) {
        if (location != null) {
            //Get addr
            Address addr = getAddrCurrentLocation(location.getLatitude(),location.getLongitude());

            String name = addr.getAddressLine(0);
            moveCameraTo(location.getLatitude(),location.getLongitude());
            curPlace = name;
            curAddr = addr;
        }

    }

    //only for camera
    public Location getLocation() {
        Location location = null;
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // no network provider is enabled
            } else {
                //this.canGetLocation = true;
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            LOCATION_UPDATE_MIN_TIME,
                            LOCATION_UPDATE_MIN_DISTANCE,
                            locationListener);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            updateMyCurrentLoc(location);
                            origin = new LatLng(location.getLatitude(),location.getLongitude());
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if(location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                LOCATION_UPDATE_MIN_TIME,
                                LOCATION_UPDATE_MIN_DISTANCE,
                                locationListener);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                updateMyCurrentLoc(location);
                                origin = new LatLng(location.getLatitude(),location.getLongitude());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    private void placeAutoCompleteAct() {
        Intent intent =null;
        try {
            intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void loadPic(int id) {
        thumbnails = null;
        thumbnails = loadImages(id);
        loadSlidePanel();
    }

    private Bitmap[] loadImages(int id) {
        File path = new File(pathAlbum);
        String[] fileNames = null;
        if(path.exists())
        {
            fileNames = path.list();
        }
        else {//thoat
            return null;
        }
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for(int i = 0; i < fileNames.length; i++)
        {
            //Ktra id hinh == id dia diem thi add vao
            if(fileNames[i].charAt(4)== Character.forDigit(id,10)) {
                Bitmap mBitmap = BitmapFactory.decodeFile(pathAlbum + "/" + fileNames[i]);
                bitmaps.add(mBitmap);
            }
        }

        Bitmap[] mang = bitmaps.toArray(new Bitmap[bitmaps.size()]);
        return mang;

    }//Camera

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            //setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;
        }

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {
        //Add place to db if not
        if(checkedPlaceDAO.getCheckedPlace(curPlace) == null)
            checkedPlaceDAO.addCheckedPlace(curPlace,curAddr);
        int id = checkedPlaceDAO.getCheckedPlace(curPlace).getId();
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + id +"_"+ timeStamp + "_";
        //create folder
        File albumF = new File(pathAlbum);
        albumF.mkdirs();
        //create file
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private void cameraAct(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }//Camera


    public List<CheckedPlace> loadListPlace() {
        //load dbs
        List<CheckedPlace> list = checkedPlaceDAO.getAllCheckedPlace();
        for(CheckedPlace checkedPlace:list) {
            //Toast.makeText(this,checkedPlace.getId()+":"+checkedPlace.getPlaceAddr(),Toast.LENGTH_SHORT).show();
            markLocation(checkedPlace);
        }
        return list;
    }

    //Direction
    public void requestDirection() {
        mMap.clear();
        Toast.makeText(this, "Direction Requesting...", Toast.LENGTH_SHORT).show();
        Location location = mMap.getMyLocation();
        origin = new LatLng(location.getLatitude(),location.getLongitude());
        if(origin == null) {
            Toast.makeText(this,"There is no current place",Toast.LENGTH_SHORT);
        }
        else
        if(destination == null) {
            Toast.makeText(this,"There is no destination place",Toast.LENGTH_SHORT);
        }
        else {
            GoogleDirection.withServerKey(getString(R.string.server_key))
                    .from(origin)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
        loadListPlace();
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Toast.makeText(this, "Success with status : " + direction.getStatus(), Toast.LENGTH_SHORT).show();
        if (direction.isOK()) {
            //mMap.addMarker(new MarkerOptions().position(origin));
            mMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));

        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
