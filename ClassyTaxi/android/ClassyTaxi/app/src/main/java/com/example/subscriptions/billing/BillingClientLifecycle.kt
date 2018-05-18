/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.subscriptions.billing

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.example.subscriptions.ui.SingleLiveEvent

class BillingClientLifecycle private constructor(
        private val app: Application
) : LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    /**
     * The purchase event is observable. Only one oberver will be notified.
     */
    val purchaseUpdateEvent = SingleLiveEvent<List<Purchase>>()

    /**
     * Purchases are observable. This list will be updated when the Billing Library
     * detects new or existing purchases. All observers will be notified.
     */
    val purchases = MutableLiveData<List<Purchase>>()

    /**
     * Instantiate a new BillingClient instance.
     */
    lateinit private var billingClient: BillingClient

    companion object {
        private const val TAG = "BillingLifecycle"

        @Volatile
        private var INSTANCE: BillingClientLifecycle? = null

        fun getInstance(app: Application): BillingClientLifecycle =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: BillingClientLifecycle(app).also { INSTANCE = it }
                }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        // Create a new BillingClient in onCreate().
        // Since the BillingClient can only be used once, we need to create a new instance
        // after ending the previous connection to the Google Play Store in onDestroy().
        billingClient = BillingClient.newBuilder(app.applicationContext).setListener(this).build()
        if (!billingClient.isReady) {
            Log.d(TAG, "BillingClient: Start connection...")
            billingClient.startConnection(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing")
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            billingClient.endConnection()
        }
    }

    override fun onBillingSetupFinished(billingResponseCode: Int) {
        Log.d(TAG, "onBillingSetupFinished: $billingResponseCode")
        if (billingResponseCode == BillingClient.BillingResponse.OK) {
            // The billing client is ready. You can query purchases here.
            updatePurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        // TODO: Try connecting again with exponential backoff.
        // billingClient.startConnection(this)
    }

    /**
     * Query Google Play Billing for existing purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    fun updatePurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "BillingClient is not ready to query for existing purchases")
        }
        val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result == null) {
            Log.i(TAG, "Update purchase: Null purchase result")
            handlePurchases(null)
        } else {
            if (result.purchasesList == null) {
                Log.i(TAG, "Update purchase: Null purchase list")
                handlePurchases(null)
            } else {
                handlePurchases(result.purchasesList)
            }
        }
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(responseCode: Int, purchasesList: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated, response code: $responseCode")
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                if (purchasesList == null) {
                    Log.d(TAG, "Purchase update: No purchases")
                    handlePurchases(null)
                } else {
                    handlePurchases(purchasesList)
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                Log.i(TAG, "User canceled the purchase")
            }
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "The user already owns this item")
            }
            BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                Log.e(TAG, "Developer error means that Google Play does not recognize the " +
                        "configuration. If you are just getting started, make sure you have " +
                        "configured the application correctly in the Google Play Console. " +
                        "The SKU product ID must match and the APK you are using must be " +
                        "signed with release keys.")
            }
            else -> {
                Log.e(TAG, "See error code in BillingClient.BillingResponse: $responseCode")
            }
        }
    }

    /**
     * Check if purchases have changed before updating other part of the app.
     */
    private fun handlePurchases(purchasesList: List<Purchase>?) {
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG, "Same ${purchasesList?.size} purchase(s), " +
                    "no need to post an update to the live data")
        } else {
            Log.d(TAG, "Handling ${purchasesList?.size} purchase(s)")
            updatePurchases(purchasesList)
        }
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>?): Boolean {
        // TODO: Optimize to avoid updates with identical data.
        return false
    }

    /**
     * Send purchase SingleLiveEvent and update purchases LiveData.
     *
     * The SingleLiveEvent will trigger network call to verify the subscriptions on the sever.
     * The LiveData will allow Google Play settings UI to update based on the latest purchase data.
     */
    private fun updatePurchases(purchasesList: List<Purchase>?) {
        Log.i(TAG, "updatePurchases: ${purchasesList?.size} purchase(s)")
        purchaseUpdateEvent.postValue(purchasesList)
        purchases.postValue(purchasesList)
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        val sku = params.sku
        val oldSkus = params.oldSkus
        Log.i(TAG, "Launching billing flow wth sku: $sku, oldSkus: $oldSkus")
        if (!billingClient.isReady) {
            Log.e(TAG, "BillingClient is not ready to start billing flow")
        }
        val responseCode = billingClient.launchBillingFlow(activity, params)
        Log.d(TAG, "Launch Billing Flow Response Code: $responseCode")
        return responseCode
    }

}
