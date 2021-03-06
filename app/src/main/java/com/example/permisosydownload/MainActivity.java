package com.example.permisosydownload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MagicalPermissions magicalPermissions;
    private final static int RESIZE_PHOTO_PIXELS_PERCENTAGE=50;
    private MagicalCamera magicalCamera;
    private ImageView imageViewFoto;
    private TextView txtRUTA;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // permisos per = new permisos(this);
        // per.getPermission();
        // findViewById(R.id.btnCamara).setOnClickListener(this);
        imageViewFoto=findViewById(R.id.imageView);
        txtRUTA=findViewById(R.id.txtRUTA);
        ArrayList<String> permisos = new ArrayList<String>();
        permisos.add(Manifest.permission.CAMERA);
        permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permisos.add(Manifest.permission.WRITE_CALENDAR);

        getPermission(permisos);

     /*   String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        getPermission(permisos);*/
        magicalPermissions = new MagicalPermissions(this, permisos.toArray(new String[permisos.size()]));
        magicalCamera = new MagicalCamera(this, RESIZE_PHOTO_PIXELS_PERCENTAGE,magicalPermissions);
    }

    public void onClick(View v)
    {
        magicalCamera.takePhoto();
    }

    public void getPermission(ArrayList<String> permisosSolicitados){
        ArrayList<String> listPermisosNOAprob = getPermisosNoAprobados(permisosSolicitados);
        if (listPermisosNOAprob.size()>0)
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(listPermisosNOAprob.toArray(new String[listPermisosNOAprob.size()]), 1);
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String>  listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        for(String permiso: listaPermisos) {
            if (Build.VERSION.SDK_INT >= 23)
                if(checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED)
                    list.add(permiso);

        }
        return list;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String s="";
        if(requestCode==1)    {
            for(int i =0; i<permissions.length;i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    s=s + "OK " + permissions[i] + "\n";
                else
                    s=s + "NO  " + permissions[i] + "\n";
            }
            Toast.makeText(this.getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            magicalCamera.resultPhoto(requestCode,resultCode,data);
            imageViewFoto.setImageBitmap(magicalCamera.getPhoto());
        }

        switch(requestCode) {
            case 10:
                //path= data.getDataString();
                Uri uri = data.getData();
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri));
                path = uri.getPath();
                String[] rutas = path.split(":");
                String ruta1 = rutas[0];
                String ruta2 = rutas[1];

                txtRUTA.setText(ruta2);
                Toast.makeText(this.getApplicationContext(),"RUTA: "  + ruta2,Toast.LENGTH_LONG).show();

                String url = "http://tierra.rediris.es/hidrored/ebooks/miguel/AguaFuenteVida.pdf";
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("PDF");
                request.setTitle("Pdf");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }

                request.setDestinationInExternalPublicDir(ruta2, "filedownload.pdf");
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                try {
                    manager.enqueue(request);
                } catch (Exception e) {
                    Toast.makeText(this.getApplicationContext(),"Error: "  + e.getMessage(),Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    public void BajarDoc(View view){
        Intent myFile;
        myFile = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        myFile.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(myFile, "Elija un directorio"),10);

    }


}