package com.example.busnusantara.googleapi;

import com.google.android.gms.maps.model.PolylineOptions;

public interface DirectionPointListener {
    void onPath(PolylineOptions polyLine);
}
