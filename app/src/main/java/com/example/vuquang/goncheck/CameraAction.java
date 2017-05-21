package com.example.vuquang.goncheck;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import com.example.vuquang.goncheck.DAO.CheckedPlaceDAO;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by VuQuang on 5/6/2017.
 */

public class CameraAction {
    public static final String pathAlbum = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/" + "GoNCheck/";
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    //save current place
    String curPlace;
    Address curAddr;

    //Manage Db
    CheckedPlaceDAO checkedPlaceDAO;

    public CameraAction(String curPlace, Address curAddr, CheckedPlaceDAO checkedPlaceDAO) {
        this.curPlace = curPlace;
        this.curAddr = curAddr;
        this.checkedPlaceDAO = checkedPlaceDAO;
    }

    public void makeFile(Intent cameraIntent) {
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

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    public void handleBigCameraPhoto(AppCompatActivity activity) {

        if (mCurrentPhotoPath != null) {
            //setPic();
            galleryAddPic(activity);
            mCurrentPhotoPath = null;
        }

    }

    private void galleryAddPic(AppCompatActivity activity) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public Bitmap[] loadImages(int id) {
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
                final String fileName = pathAlbum + "/" + fileNames[i];

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap mBitmap = BitmapFactory.decodeFile(fileName,options);
                bitmaps.add(mBitmap);
            }
        }

        //List -> []
        Bitmap[] mang = bitmaps.toArray(new Bitmap[bitmaps.size()]);
        return mang;

    }//Camera

    public boolean deleteFile() {
        File file = new File(mCurrentPhotoPath);
        return file.delete();
    }

}
