package com.hasani.moein.taan.events;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import DataBaseHandler.DataBaseHandler;
import DataBaseHandler.marker_model;
import Map_Utilities.DbBitmapUtility;

public class Main_MAP extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int idplus = 0;
    private int id;

    //bringing all markers back from db when mapactivity reopens
    public void reload() {
        DataBaseHandler dbh = new DataBaseHandler(getApplicationContext());
        ArrayList<marker_model> onOpen_array = dbh.getObjects();
        if (!onOpen_array.isEmpty()) //vojood in if baraye in ast ke bebinim aya chizi az ghabl dar db zakhire kardim
                                     //agar nakardim ke kare khasi lazem nis agar kardebashim hame marker hara dobare misazim
                                    //ba estefade azetelaat mojood dar db

        {
            idplus = onOpen_array.size();

            for (int i = 0; i < idplus; i++) {
                if(onOpen_array.get(i).getImageaddress().equals(Uri.parse("null")))
                {
                    mMap.addMarker(new MarkerOptions().position(onOpen_array.get(i).getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap
                                                    (resizeMapIcons("bluemarker",60,100))));
                }else{
                    mMap.addMarker(new MarkerOptions().position(onOpen_array.get(i).getLatLng())
                        .icon(BitmapDescriptorFactory.fromBitmap
                                (getRoundedBitmap(resizeMapIcons_bitmap(DbBitmapUtility.getImage(onOpen_array.get(i).getBitmap()), 130, 160)))));
                    }

            }
        }
    }
    ////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_map);
        SupportMapFragment MmapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        MmapFragment.getMapAsync(this);
        //Find_me();
    }

    //find me on map
    private void Find_me(){

        try{
            mMap.setMyLocationEnabled(true);

            setUpMapIfNeeded();


        } catch(SecurityException e){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            Toast.makeText(getApplicationContext(),"App doesn't have the permission to gps... ",Toast.LENGTH_SHORT).show();
        }
    }
    public void setUpMapIfNeeded() {

        try{
            final LocationManager lm = (LocationManager) this.getSystemService(
                    Context.LOCATION_SERVICE);
            final Location myLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (myLoc != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLoc.getLatitude(),
                        myLoc.getLongitude()), 13));
            }
        } catch(SecurityException e){

            Toast.makeText(getApplicationContext(),"Please give the app GPS Access ",Toast.LENGTH_SHORT).show();
        }
    }
    //////////////////////////////////////////////////////////


    @Override
    protected void onResume() {
        SupportMapFragment MmapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        MmapFragment.getMapAsync(this);
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        reload();
        Find_me();

        /////////////////////////////////


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent i = new Intent(Main_MAP.this, marker_get_info.class);

                //sending Marker LatLng to getinfo acticity
                Bundle bundle = new Bundle();
                bundle.putParcelable("from_position", latLng);
                i.putExtra("bundle", bundle);
                ////////////////////////////////////////////

                startActivity(i);
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                DataBaseHandler dbh = new DataBaseHandler(getApplicationContext());

                //getting marker id based on its LatLng from db(choon marker ra darim na object shamel etelaat ra)
                Intent intent = new Intent(Main_MAP.this, display_info.class);
                id = dbh.Marker_Id(marker.getPosition());
                intent.putExtra("id", id);
                ///////////////////////////////////////////////////

                startActivity(intent);
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                View v;
                LayoutInflater inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                v = inflator.inflate(R.layout.info, null);
                TextView text = (TextView) v.findViewById(R.id.title);

                //setting info on infowindows by getting the right info from db
                DataBaseHandler dbh = new DataBaseHandler(getApplicationContext());
                ArrayList<marker_model> onMapclickList = dbh.getObjects();
                marker_model markerModel = onMapclickList.get(dbh.Marker_Id(marker.getPosition()));
                text.setText(markerModel.getTitle());
                ///////////////////////////////////////////////////////////////////
                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View view = getLayoutInflater().inflate(R.layout.bg, null);

                return view;
            }
        });


    }

    //////////////////////////////////////////////////////taghire size bitmap ha ba gereftan address anha (bar asas name peyda mikond)
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    //////////////////////////////////////////////////////taghire size bit map ha ba gereftan anha be onvan argument
    public Bitmap resizeMapIcons_bitmap(Bitmap imageBitmap, int width, int height) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    //////////////////////////////////////////////////////aks ba gooshe zavie dar
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    //////////////////////////////////////////////////////aks be soorat dayere ee
    public Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}