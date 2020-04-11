package com.grace.covid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnQRCode;
    private LinearLayout btnGoOut;
    private LinearLayout btnSetProfile;
    private LinearLayout btnSeePDF;
    private LinearLayout btnGoOfficialWebSite;
    private Bitmap bitmap;
    private ImageView ccc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewById();
        setDataField();
        actionView();
    }

    protected void saveQRCode(Bitmap bitmap) {
        File dir = new File(getApplicationContext().getExternalFilesDir(
                "attestation") + "/qrcode.png");
        FileOutputStream os;
        try {
            os = new FileOutputStream(dir, false);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, 400, 400);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (Exception ignored) {}
    }

    /**
     * https://www.tutorialspoint.com/javaexamples/extract_image_from_pdf.htm
     */
    public boolean extractQRCodeFromPdf() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0XFF000000);
        // Loading an existing PDF document
        File file = new File(getApplicationContext().getExternalFilesDir(
                "attestation") + "/Attestation_de_deplacement_derogatoire.pdf");
        if (file.exists()) {
            // Instantiating the PDFRenderer class
            PdfRenderer renderer = null;
            try {
                renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Rendering an image from the PDF document
            assert renderer != null;
            PdfRenderer.Page page = renderer.openPage(1);
            //bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            // Paint bitmap before rendering
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawRect(45, 45, 355, 355, paint);//
            canvas.drawBitmap(bitmap, 0, 0, paint);

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            //canvas.saveLayer(new RectF(45, 45, 355, 355), paint);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, 400, 400);
            saveQRCode(bitmap);
            // Closing the document
            page.close();
            renderer.close();
            return true;
        }
        return false;
    }

    private void actionView() {
        btnQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean qr_code = extractQRCodeFromPdf();
                if (qr_code) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                    @SuppressLint("InflateParams") View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.sample_popup_qrcode, null);
                    ccc = view1.findViewById(R.id.cccccc);
                    ccc.setImageBitmap(bitmap);
                    builder.setView(view1)
                            .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                    ;
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                    builder.setMessage(getString(R.string.msg_no_attestation));
                    builder.setPositiveButton(getString(R.string.btn_create_attestation), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            startActivity(new Intent(MainActivity.this, ChooseJustificationActivity.class));
                        }
                    });
                    builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        });

        btnGoOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ChooseJustificationActivity.class));
            }
        });

        btnSetProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SetProfileActivity.class));
            }
        });

        btnSeePDF.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                File file = new File(getApplicationContext().getExternalFilesDir(
                        "attestation") + "/Attestation_de_deplacement_derogatoire.pdf");
                if (file.exists()) {
                    //startActivity(new Intent(MainActivity.this, PdfViewerActivity.class));
                    file = new File(getApplicationContext().getExternalFilesDir(
                            "attestation") + "/Attestation_de_deplacement_derogatoire.pdf");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName()+".fileprovider", file);

                    intent.setDataAndType(uri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                    builder.setMessage(getString(R.string.msg_no_attestation));
                    builder.setPositiveButton(getString(R.string.btn_create_attestation), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            startActivity(new Intent(MainActivity.this, ChooseJustificationActivity.class));
                        }
                    });
                    builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        btnGoOfficialWebSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://media.interieur.gouv.fr/deplacement-covid-19/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void setDataField() {
    }

    private void initViewById() {
        btnQRCode = findViewById(R.id.btn_qrcode);
        btnGoOut = findViewById(R.id.btn_go_out);
        btnSetProfile = findViewById(R.id.btn_set_profile);
        btnSeePDF = findViewById(R.id.btn_see_pdf);
        btnGoOfficialWebSite = findViewById(R.id.btn_website);

        ccc = findViewById(R.id.cccccc);
    }
}
