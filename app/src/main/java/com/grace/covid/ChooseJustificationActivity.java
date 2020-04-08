package com.grace.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Objects;

public class ChooseJustificationActivity extends AppCompatActivity {

    private WebView webView;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_justification);

        // Runtime External storage permission for saving download files
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 1);
            }
        }*/

        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        webView = findViewById(R.id.webview);
        webView.getSettings().setUserAgentString("COVID_19");

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // Allocation d'espace
        webSettings.setDomStorageEnabled(true);
        // Allocation des caches
        webSettings.setAppCacheEnabled(true);
        // Reservation de 8Mo d'espace dans le téléphone
        //webSettings.setAppCacheMaxSize(1024*1024*8);
        // Mode de cache par defaut
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setLoadsImagesAutomatically(true);

        // Force links and redirects to open in the WebView instead of in a browser
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i("WEB_VIEW_TEST","error code" + errorCode + " Desc: " + description);
                //Localise l'erreur et donne nous sa source
                showError(errorCode);
                //Ici j'allais mettre un code HTML pour gérer le vide
                //mais trop tard
                String htmlData ="<!DOCTYPE html>" +
                        "<head>" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">" +
                        "</head>" +
                        "<body style=\"background:#ebedf4;padding:5%;font-family: 'Comic Sans MS', cursive;\">" +
                        "<div style=\"margin-top:20%;text-align:center\" ><h3> :-( Erreur de Connexion<br> Vérifier que votre appareil est connecté sur internet, si non Veuillez réessayer plutard...</h3></div>" +
                        "</body>" +
                        "</html>";
                webView.loadUrl("about:blank");
                webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
                webView.invalidate();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // return super.shouldOverrideUrlLoading(view, url);
                if (url.startsWith("mailto:") || url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                view.loadUrl(url);
                return true;
            }

            /*@Override
            public boolean shouldOverrideUrlLoading(WebView view, Uri request) {
                if (Objects.equals(request.getScheme(), "blod")) {
                    return true;
                }
            }*/

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String firstname = sharedPreferences.getString(getString(R.string.pref_key_firstname), "");
                String lastname = sharedPreferences.getString(getString(R.string.pref_key_lastname), "");
                String birthday = sharedPreferences.getString(getString(R.string.pref_key_birthday), "");
                String lieunaissance = sharedPreferences.getString(getString(R.string.pref_key_lieunaissance), "");
                String address = sharedPreferences.getString(getString(R.string.pref_key_address), "");
                String town = sharedPreferences.getString(getString(R.string.pref_key_town), "");
                String zipcode = sharedPreferences.getString(getString(R.string.pref_key_zipcode), "");
                view.loadUrl(
                        "javascript:window.onload = (function() {" +
                                "var selectElementName = document.querySelector('input[name=\\\"firstname\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + firstname +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"lastname\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + lastname +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"birthday\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + birthday +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"lieunaissance\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + lieunaissance +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"address\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + address +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"town\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + town +"';}" +
                                "var selectElementName = document.querySelector('input[name=\\\"zipcode\\\"]');" +
                                "if(selectElementName){selectElementName.value =  '" + zipcode +"';}" +
                                "})()"
                );
            }
        });


        if (!isConnected()) {
            // Tu peux utiliser celui ci qui est par defaut
            // il rappelle les pages en caches
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webView.loadUrl("https://media.interieur.gouv.fr/deplacement-covid-19/");


        // handle downloading
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s/*url*/, String s1/*userAgent*/,
                                        String s2/*contentDisposition*/, String s3/*mimeType*/, long l/*contentLength*/) {
                webView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(s));
                /*DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s.replace("blob:", "")));
                request.setMimeType(s3);
                String cookies = CookieManager.getInstance().getCookie(s);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", s1);
                request.setDescription("Downloading File...");
                request.setTitle(URLUtil.guessFileName(s, s2, s3));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(s, s2, s3)
                );
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                assert downloadManager != null;
                downloadManager.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();*/
            }
        });
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
    }

    private boolean isConnected() {
        ConnectivityManager con = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert con != null;
        NetworkInfo networkInfo = con.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     Detecteur d'erreur
     */
    public void showError(int errorCode) {
        String message = null;
        String title = null;

        if (errorCode == WebViewClient.ERROR_AUTHENTICATION) {
            message = "Impossible de s'authentifier";
            title = "Echec de connexion";
        } else if (errorCode == WebViewClient.ERROR_TIMEOUT) {
            message = "Impossible d'atteindre la cible processus trop lente";
            title = "Temps ecoulé";
        } else if (errorCode == WebViewClient.ERROR_TOO_MANY_REQUESTS) {
            message = "Surcharge des requêtes pendant le chargement";
            title = "Surcharge";
        } else if (errorCode == WebViewClient.ERROR_UNKNOWN) {
            message = "Erreur générique";
            title = "Inconnue";
        } else if (errorCode == WebViewClient.ERROR_BAD_URL) {
            message = "Adresse URL incorrecte";
            title = "Url erroné";
        } else if ((errorCode == WebViewClient.ERROR_CONNECT) || errorCode == WebViewClient.ERROR_IO) {
            message = "Impossible de se connecter au serveur";
            title = "Echec de connexion";
        } else if (errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
            message = "Impossible de vérifier la securité SSL";
            title = "Echec SSL";
        } else if (errorCode == WebViewClient.ERROR_REDIRECT_LOOP) {
            message = "Boucle de redirection insupportable";
            title = "Echec de redirection";
        }

        /*
         POP - UP pour afficher une message à l'écran
         */
        if(message !=null){
            AlertDialog alertdialog = new AlertDialog.Builder(getApplicationContext()).create();
            alertdialog.setMessage(message);
            alertdialog.setTitle(title);
            alertdialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertdialog.setCancelable(false);
            alertdialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertdialog.show();
        }
    }
}
