/*
* 위치를 직접 검색해서 목적지로 설정한다.
 */


package com.example.searchmap2;


import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class SearchMapActivity extends AppCompatActivity
        implements View.OnClickListener,OnMapReadyCallback, PlacesListener {


    GoogleMap mMap;

    List<Marker> previous_marker = null;

    private static final String TAG = SearchMapActivity.class.getSimpleName();
    private String apiKey = getString(R.string.api_key);

    private String address=null;
    private LatLng arrival;

    Button btn_route,btn_category,btn_loading;
    private View layout_search;

    int category; //선택한 카테고리 넘버


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_search_map);


        previous_marker = new ArrayList<Marker>();

        layout_search=findViewById(R.id.layout_search);
        btn_route = findViewById(R.id.btn_route);
        btn_category = findViewById(R.id.btn_category);
        //btn_loading=findViewById(R.id.btn_loading);

        btn_route.setOnClickListener(this);
        //btn_loading.setOnClickListener(this);
        btn_category.setOnClickListener(this);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.search_map);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                address=place.getName();
                System.out.println("address: "+address);

                mMap.clear();
                SearchMapActivity.Point p = getPointFromGeoCoder(address);
                arrival = new LatLng(p.X_value(),p.Y_value());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(arrival);
                markerOptions.title("목적지");
                markerOptions.snippet(address);
                mMap.addMarker(markerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arrival, 11));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.show_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onClick(View v){

        switch(v.getId()){
            case R.id.btn_route:
                try {
                    if(address==null){
                        final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }else{
                        Intent i = new Intent(this, DirectionActivity.class);
                        i.putExtra("address", address);
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_category:
                if (address == null) {
                    final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }else{
                    mMap.clear();
                    if (previous_marker != null)
                        previous_marker.clear();//지역정보 마커 클리어
                    show();
                    showPlaceInformation(arrival);
                    break;
                    }
        }
    }


    void show() //카테고리 보여주기
    {
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("모두");
        ListItems.add("음식점");
        ListItems.add("카페");
        ListItems.add("버스 정류장");
        ListItems.add("은행");
        ListItems.add("공원");

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        final List SelectedItems  = new ArrayList();
        int defaultItem = 0;
        SelectedItems.add(defaultItem);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("장소 카테고리 선택");
        builder.setSingleChoiceItems(items, defaultItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedItems.clear();
                        SelectedItems.add(which);
                    }
                });
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String msg="";

                        if (!SelectedItems.isEmpty()) {
                            int index = (int) SelectedItems.get(0);
                            msg = ListItems.get(index);
                            category=index; /////////
                        }
                        Toast.makeText(getApplicationContext(),
                                "Items Selected.\n"+ msg , Toast.LENGTH_LONG)
                                .show();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void showPlaceInformation(LatLng location)
    {

        switch(category){//카테고리별 검색
            case 0: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .build()
                    .execute();
                break;
            case 1: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .type(PlaceType.RESTAURANT) //음식점
                    .build()
                    .execute();
                break;
            case 2: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .type(PlaceType.CAFE) //카페
                    .build()
                    .execute();
                break;
            case 3: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .type(PlaceType.BUS_STATION) //버스 정류장
                    .build()
                    .execute();
                break;
            case 4: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .type(PlaceType.BANK) //은행
                    .build()
                    .execute();
                break;
            case 5: new NRPlaces.Builder()
                    .listener(SearchMapActivity.this)
                    .key(apiKey)
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(500) //500 미터 내에서 검색
                    .type(PlaceType.PARK) //공원
                    .build()
                    .execute();
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);
        MarkerOptions marker = new MarkerOptions();
        marker.position(SEOUL);
        marker.visible(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 8));
        mMap.clear();
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    //주소
                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    }
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    @Override
    public void onPlacesFinished() {

    }


    class Point {
        // 위도
        public double x;
        // 경도
        public double y;
        public String addr;
        // 포인트를 받았는지 여부
        public boolean havePoint;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("x : ");
            builder.append(x);
            builder.append(" y : ");
            builder.append(y);
            builder.append(" addr : ");
            builder.append(addr);

            return builder.toString();
        }
        public double X_value(){
            return x;
        }
        public double Y_value(){
            return y;
        }
    }

    private SearchMapActivity.Point getPointFromGeoCoder(String addr){
        SearchMapActivity.Point point = new SearchMapActivity.Point();
        point.addr = addr;

        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;

        try{
            listAddress = geocoder.getFromLocationName(addr,1);
        } catch (IOException e) {
            e.printStackTrace();
            point.havePoint=false;
            return point;
        }

        if(listAddress.isEmpty()){
            point.havePoint=false;
            return point;
        }

        point.havePoint=true;
        point.y=listAddress.get(0).getLongitude();
        point.x=listAddress.get(0).getLatitude();
        return point;
    }
}
