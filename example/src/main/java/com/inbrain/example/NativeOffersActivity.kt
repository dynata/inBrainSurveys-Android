package com.inbrain.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inbrain.sdk.InBrain
import com.inbrain.sdk.callback.GetNativeOffersCallback
import com.inbrain.sdk.callback.OpenOfferCallback
import com.inbrain.sdk.model.offers.InBrainNativeOffer
import com.inbrain.sdk.model.offers.InBrainOfferFilter
import com.inbrain.sdk.model.offers.InBrainOfferType

class NativeOffersActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "InBrainExample"

        fun intent(context: Context): Intent = Intent(context, NativeOffersActivity::class.java)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: NativeOffersAdapter

    private var offers: List<InBrainNativeOffer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_offers)

        title = getString(R.string.native_offers_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerOffers)
        progressBar = findViewById(R.id.progressBar)

        adapter = NativeOffersAdapter { offer -> openOffer(offer) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadOffers()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadOffers() {
        setLoading(true)
        recyclerView.animate().alpha(0f).setDuration(300).start()

        InBrain.getInstance().getNativeOffers(
            InBrainOfferFilter(type = InBrainOfferType.DEFAULT),
            object : GetNativeOffersCallback {
                override fun onSuccess(result: List<InBrainNativeOffer>) {
                    offers = result
                    adapter.submit(result)
                    setLoading(false)

                    if (result.isEmpty()) {
                        showToast(getString(R.string.error_no_offers))
                        recyclerView.animate().alpha(0f).setDuration(300).start()
                    } else {
                        recyclerView.animate().alpha(1f).setDuration(300).start()
                    }
                }

                override fun onFailure(error: Throwable) {
                    setLoading(false)
                    showToast(error.message ?: getString(R.string.error_generic))
                }
            }
        )
    }

    private fun openOffer(offer: InBrainNativeOffer) {
        setLoading(true)
        InBrain.getInstance().sessionId = null
        InBrain.getInstance().openOfferWithId(offer.id, this, object : OpenOfferCallback {
            override fun onSuccess() {
                setLoading(false)
                Log.d(LOG_TAG, "Successfully opened offer")
            }

            override fun onFailure(error: Throwable) {
                setLoading(false)
                showToast(error.message ?: getString(R.string.error_generic))
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
