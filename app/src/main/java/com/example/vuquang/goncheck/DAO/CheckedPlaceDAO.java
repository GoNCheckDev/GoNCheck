package com.example.vuquang.goncheck.DAO;

import android.location.Address;

import com.example.vuquang.goncheck.model.CheckedPlace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by VuQuang on 4/25/2017.
 */

public class CheckedPlaceDAO {

    private Realm realm;

    public CheckedPlaceDAO() {
        try {
            realm = Realm.getDefaultInstance();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Get a place
    public List<CheckedPlace> getAllCheckedPlace() {
        RealmResults<CheckedPlace> places = realm.where(CheckedPlace.class).findAll();
        Iterator<CheckedPlace> iterators = places.iterator();
        List<CheckedPlace> list = new ArrayList<CheckedPlace>();
        while (iterators.hasNext())
            list.add(iterators.next());
        return list;
    }

    public CheckedPlace getCheckedPlace(final String curPlace) {
        RealmResults<CheckedPlace> places = realm.where(CheckedPlace.class)
                .equalTo("placeAddr",curPlace).findAll();
        Iterator<CheckedPlace> iterators = places.iterator();
        if (iterators.hasNext())
            return iterators.next();
        return null;
    }

    //add place to db
    public void addCheckedPlace(final String place,final Address address) {
        try {
            realm.beginTransaction();

            int nextId = 0;
            if (realm.where(CheckedPlace.class).findAll().size() > 0) {
                nextId  = (realm.where(CheckedPlace.class).max("id")).intValue() + 1;
            }
            CheckedPlace checkedPlace = realm.createObject(CheckedPlace.class,nextId);
            checkedPlace.setPlaceAddr(place);
            checkedPlace.setLatitude(address.getLatitude());
            checkedPlace.setLongitude(address.getLongitude());

            realm.commitTransaction();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //update place
    public void updateCheckedPlace(final CheckedPlace checkedPlace,final String place) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                checkedPlace.setPlaceAddr(place);
            }
        });
    }

    // Delete person
    public void deleteCheckPlace(final int id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CheckedPlace> result = realm.where(CheckedPlace.class)
                                                        .equalTo("id",id).findAll();
                result.deleteAllFromRealm();
            }
        });
    }

    // Delete all
    public void deleteAllCheckPlace() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CheckedPlace> result = realm.where(CheckedPlace.class).findAll();
                result.deleteAllFromRealm();
            }
        });
    }
}
