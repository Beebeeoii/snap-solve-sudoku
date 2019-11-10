package com.beebeeoii.snapsolvesudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;

public class Donate extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor billingProcessor;
    final private String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiXZHiSjbFSJreSuzZsIUmWhvyTdudAADa6b2eHz6C9Miu9pvkhkJod2fi4dGlt64yN2Vgo6XOJi/1gQm5E4T4vRmL9Wk7gJEGY3leHYZ65YFXPitE97lp0VcDvlPQuZl//H9dQi0cXoosZ6xprfvqcr7vLphpVtG31FTbYWjVm2pkXuEIZdpSoBOXVdTD70eY5ZTBtoUawfu53Gr0CXhmUK/wwLTdaxYkYN83/oGij6b2HIJGzvq7CXgc3GdulouQ+YXX/D3PZhxd6XzSr4CkcGgT6HM8hHKaaASJTDsGpJ4ZUTkqqYs0OGQ6CtJKnH0FFymBvoCSNyiHKWNdlm8EwIDAQAB";

    Button backButton;
    CardView coffee, icecream, burger, movie, meal, gift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        backButton = (Button) findViewById(R.id.donate_back);
        coffee = (CardView) findViewById(R.id.donate_coffee);
        icecream = (CardView) findViewById(R.id.donate_ice_cream);
        burger = (CardView) findViewById(R.id.donate_burger);
        movie = (CardView) findViewById(R.id.donate_movie);
        meal = (CardView) findViewById(R.id.donate_meal);
        gift = (CardView) findViewById(R.id.donate_gift);

        billingProcessor = BillingProcessor.newBillingProcessor(this, LICENSE_KEY, this);
        billingProcessor.initialize();

        coffee.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "coffee");
            billingProcessor.consumePurchase("coffee");
        });

        icecream.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "ice_cream");
            billingProcessor.consumePurchase("ice_cream");
        });

        burger.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "burger");
            billingProcessor.consumePurchase("burger");
        });

        movie.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "movie_ticket");
            billingProcessor.consumePurchase("movie_ticket");
        });

        meal.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "meal");
            billingProcessor.consumePurchase("meal");
        });

        gift.setOnClickListener((View v) -> {
            billingProcessor.purchase(this, "gift");
            billingProcessor.consumePurchase("gift");
        });

        backButton.setOnClickListener((View v) -> onBackPressed());
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        switch (errorCode) {
            case Constants.BILLING_RESPONSE_RESULT_USER_CANCELED:
                Toast.makeText(this, "Sad to see you go :(", Toast.LENGTH_SHORT).show();
                break;
            case Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
                Toast.makeText(this, "[Error]: Service unavailable. Ensure there is Internet connection!", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "[Error]: Donation transaction failed", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        super.onDestroy();
    }
}
