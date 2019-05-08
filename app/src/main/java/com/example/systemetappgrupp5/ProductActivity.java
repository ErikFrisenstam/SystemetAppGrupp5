package com.example.systemetappgrupp5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;


public class ProductActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProductActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.product_activity);

      // extract the Product pass in the bundle
      Bundle extras = getIntent().getExtras();
      Product p = (Product) extras.get("product");
      // display the product
      displayProduct(p);
    }

    private void setViewText(int viewId, String label, String text) {
      TextView tv = findViewById(viewId);
      tv.setText(Html.fromHtml("<b>"+label+"</b>: " + text));
      Log.d(LOG_TAG, " * " + label + " | " + text);
    }

    private void displayProduct(Product product) {
      setViewText(R.id.product_name, "Namn", product.name());
      setViewText(R.id.product_volume, "Volym", String.valueOf(product.volume()) + " ML");
      setViewText(R.id.product_alcohol, "Alkohol", String.valueOf(product.alcohol()) + " %");
      setViewText(R.id.product_price, "Pris", String.valueOf(product.price()) + " SEK");
    }

  }

