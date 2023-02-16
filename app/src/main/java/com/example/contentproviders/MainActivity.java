package com.example.contentproviders;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.provider.UserDictionary.Words;
import android.text.Layout;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.bumptech.glide.Glide;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vamos a intentar ver las fotos de MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        // Permisos: <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

        //Hago que se pidan los permisos necesarios al usuario.
        ActivityCompat.requestPermissions(this, new String[] {"android.permission.READ_EXTERNAL_STORAGE"}, 0);

        // Especifico las columnas a traer.
        String[] mProjection =
                {
                        MediaStore.Images.Media._ID

                };

        // Defines a string to contain the selection clause
        String selectionClause = null;

        // Initializes an array to contain selection arguments
        String[] selectionArgs = null;

        // Sort order.
        String sortOrder = null;

        Cursor cursor = null;

        try {

            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    mProjection,
                    selectionClause,
                    selectionArgs,
                    sortOrder);

            while (cursor.moveToNext()){

                    String idContent = cursor.getString(cursor.getColumnIndex( MediaStore.Images.Media._ID));

                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                    LinearLayoutCompat layOut =  findViewById(R.id.mylayout);
                    layOut.addView(imageView);

                    // Uso de Glide cambiando el tamaño de la imagen.
                    // Se debe inlcuir en Gradle:
                    //  implementation 'com.github.bumptech.glide:glide:4.14.2'
                    
                    Glide.with(imageView)
                         .load(MediaStore.Images.Media.getContentUri("external", Integer.valueOf(idContent)))
                         .sizeMultiplier(0.2f)
                         .into(imageView);

                    //  Solucion sin usar Glide creando un Thread por usar ImageDecoder.
                    //cargarEnBackground(imageView,idContent);
            }
        } catch (Exception ex) {

                System.out.println(ex.toString());
        }
    }

    private void cargarEnBackground(ImageView imageView, String idContent) {

        /*
        Esta linea de abajo da error por que deberia ejecutarse en background pero Asyntask esta deprecated
        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), MediaStore.Images.Media.getContentUri("external", Integer.valueOf(idContent)));
        */

        new Thread(new Runnable() {
            public void run() {

                imageView.post(new Runnable() {
                    public void run() {

                        //Obtenemos el source del fichero de la imagen.
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), MediaStore.Images.Media.getContentUri("external", Integer.valueOf(idContent)));

                        try {
                            // Utilizamos un decoder para reducir la imagen.
                            Drawable drawable = ImageDecoder.decodeDrawable(source, (decoder, info, src) -> {
                                decoder.setTargetSampleSize(10);
                            });

                            imageView.setImageDrawable(drawable);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

    }
}