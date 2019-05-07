package com.example.newtest.RxLocaitor;

import android.location.Address;
import android.location.Location;


public interface MainView {
    void onLocationUpdate(Location location);
    void onAddressUpdate(Address address);
    void onLocationSettingsUnsuccessful();
}
