package com.beebeeoii.snapsolvesudoku.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView


private lateinit var appBar: MaterialToolbar
private lateinit var donateCoffee: MaterialCardView
private lateinit var donateCake: MaterialCardView
private lateinit var donatePizza: MaterialCardView
private lateinit var donateWine: MaterialCardView
private lateinit var billingProcessor: BillingProcessor

class DonateFragment : Fragment(), BillingProcessor.IBillingHandler {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_donate, container, false)

        appBar = rootView.findViewById(R.id.appBar)
        donateCoffee = rootView.findViewById(R.id.donateCoffee)
        donateCake = rootView.findViewById(R.id.donateCake)
        donatePizza = rootView.findViewById(R.id.donatePizza)
        donateWine = rootView.findViewById(R.id.donateWine)

        billingProcessor = BillingProcessor(requireContext(), "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiXZHiSjbFSJreSuzZsIUmWhvyTdudAADa6b2eHz6C9Miu9pvkhkJod2fi4dGlt64yN2Vgo6XOJi/1gQm5E4T4vRmL9Wk7gJEGY3leHYZ65YFXPitE97lp0VcDvlPQuZl//H9dQi0cXoosZ6xprfvqcr7vLphpVtG31FTbYWjVm2pkXuEIZdpSoBOXVdTD70eY5ZTBtoUawfu53Gr0CXhmUK/wwLTdaxYkYN83/oGij6b2HIJGzvq7CXgc3GdulouQ+YXX/D3PZhxd6XzSr4CkcGgT6HM8hHKaaASJTDsGpJ4ZUTkqqYs0OGQ6CtJKnH0FFymBvoCSNyiHKWNdlm8EwIDAQAB", this)
        billingProcessor.initialize()

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        donateCoffee.setOnClickListener {
            billingProcessor.purchase(requireActivity(), "coffee", "devTest")
            billingProcessor.consumePurchase("coffee")
        }

        donateCake.setOnClickListener {
            billingProcessor.purchase(requireActivity(), "cake", "devTest")
            billingProcessor.consumePurchase("cake")
        }

        donatePizza.setOnClickListener {
            billingProcessor.purchase(requireActivity(), "pizza", "devTest")
            billingProcessor.consumePurchase("pizza")
        }

        donateWine.setOnClickListener {
            billingProcessor.purchase(requireActivity(), "wine", "devTest")
            billingProcessor.consumePurchase("wine")
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBillingInitialized() {

    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }
}