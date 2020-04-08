package com.grace.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    PDFView pdfView;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        file = new File(getApplicationContext().getExternalFilesDir(
                "attestation") + "/Attestation_de_deplacement_derogatoire.pdf");

        pdfView = findViewById(R.id.pdf_view);

        pdfView.fromFile(file)
                .defaultPage(0)
                .load();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
