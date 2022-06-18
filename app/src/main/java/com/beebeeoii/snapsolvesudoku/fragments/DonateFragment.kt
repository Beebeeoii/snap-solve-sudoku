package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.beebeeoii.snapsolvesudoku.databinding.FragmentDonateBinding
import com.google.android.material.snackbar.Snackbar

private lateinit var billingProcessor: BillingProcessor

class DonateFragment : Fragment(), BillingProcessor.IBillingHandler {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDonateBinding.inflate(inflater, container, false)

        // TODO Move license key to string constants
        billingProcessor = BillingProcessor.newBillingProcessor(requireContext(), "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiXZHiSjbFSJreSuzZsIUmWhvyTdudAADa6b2eHz6C9Miu9pvkhkJod2fi4dGlt64yN2Vgo6XOJi/1gQm5E4T4vRmL9Wk7gJEGY3leHYZ65YFXPitE97lp0VcDvlPQuZl//H9dQi0cXoosZ6xprfvqcr7vLphpVtG31FTbYWjVm2pkXuEIZdpSoBOXVdTD70eY5ZTBtoUawfu53Gr0CXhmUK/wwLTdaxYkYN83/oGij6b2HIJGzvq7CXgc3GdulouQ+YXX/D3PZhxd6XzSr4CkcGgT6HM8hHKaaASJTDsGpJ4ZUTkqqYs0OGQ6CtJKnH0FFymBvoCSNyiHKWNdlm8EwIDAQAB", this)
        billingProcessor.initialize()

        binding.appBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.donateCoffee.setOnClickListener {
            if (checkBillingAvailability()) {
                billingProcessor.purchase(requireActivity(), "coffee")
            }
        }

        binding.donateCake.setOnClickListener {
            if (checkBillingAvailability()) {
                billingProcessor.purchase(requireActivity(), "cake")
            }
        }

        binding.donatePizza.setOnClickListener {
            if (checkBillingAvailability()) {
                billingProcessor.purchase(requireActivity(), "pizza")
            }
        }

        binding.donateWine.setOnClickListener {
            if (checkBillingAvailability()) {
                billingProcessor.purchase(requireActivity(), "wine")
            }
        }

        return binding.root
    }

    override fun onBillingInitialized() {
    }

    override fun onPurchaseHistoryRestored() {
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    private fun checkBillingAvailability(): Boolean {
        if (!billingProcessor.isConnected) {
            Snackbar.make(
                requireView(),
                "Google Play Market services is unavailable. Please try again later.",
                Snackbar.LENGTH_SHORT
            ).show()

            return false
        }

        return true
    }
}