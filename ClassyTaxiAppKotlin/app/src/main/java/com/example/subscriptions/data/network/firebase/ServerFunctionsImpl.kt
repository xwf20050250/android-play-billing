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

package com.example.subscriptions.data.network.firebase

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.example.subscriptions.data.ContentResource
import com.example.subscriptions.data.SubscriptionStatus
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Implementation of [ServerFunctions] using Firebase Callable Functions.
 * https://firebase.google.com/docs/functions/callable
 */
class ServerFunctionsImpl : ServerFunctions {

    /**
     * Track the number of pending server requests.
     */
    private val pendingRequestCount = AtomicInteger()

    /**
     * Live data is true when there are pending network requests.
     */
    override val loading = MutableLiveData<Boolean>()

    /**
     * Increment request count and update loading value.
     * Must plan on calling [decrementRequestCount] when the request completes.
     */
    private fun incrementRequestCount() {
        pendingRequestCount.incrementAndGet().also {
            Log.d(TAG, "Pending Server Requests: $it")
            if (it <= 0) {
                Log.wtf(TAG, "Unexpectedly low request count after new request: $it")
            } else {
                loading.postValue(true)
            }
        }
    }

    /**
     * Decrement request count and update loading value.
     * Must call [decrementRequestCount] once, and before, each time you call this method.
     */
    private fun decrementRequestCount() {
        pendingRequestCount.decrementAndGet().also {
            Log.d(TAG, "Pending Server Requests: $it")
            if (it < 0) {
                Log.wtf(TAG, "Unexpected negative request count: $it")
            } else if (it == 0) {
                loading.postValue(false)
            } else {
                // Do nothing.
            }
        }
    }

    /**
     * The latest subscription data from the Firebase server.
     *
     * Use this class by observing the subscriptions LiveData.
     * Any server updates will be communicated through this LiveData.
     */
    override val subscriptions = MutableLiveData<List<SubscriptionStatus>>()

    /**
     * The basic content URL.
     */
    override val basicContent = MutableLiveData<ContentResource>()

    /**
     * The premium content URL.
     */
    override val premiumContent = MutableLiveData<ContentResource>()

    /**
     * Expected errors.
     *
     * NOT_FOUND: Invalid SKU or purchase token.
     * ALREADY_OWNED: Subscription is claimed by a different user.
     * INTERNAL: Server error.
     */
    enum class ServerError {
        NOT_FOUND, ALREADY_OWNED, PERMISSION_DENIED, INTERNAL
    }

    /**
     * Singleton instance of the Firebase Functions API.
     */
    private val firebaseFunctions = FirebaseFunctions.getInstance()

    /**
     * Fetch basic content and post results to [basicContent].
     * This will fail if the user does not have a basic subscription.
     */
    override fun updateBasicContent() {
        incrementRequestCount()
        Log.d(TAG, "Calling: $BASIC_CONTENT_CALLABLE")
        firebaseFunctions
                .getHttpsCallable(BASIC_CONTENT_CALLABLE)
                .call(null)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.i(TAG, "Basic content update successful")
                        val result = (task.result?.data as? Map<String, Any>)?.let {
                            ContentResource.listFromMap(it)
                        }
                        if (result == null) {
                            Log.e(TAG, "Invalid basic subscription data")
                        } else {
                            basicContent.postValue(result)
                        }
                    } else {
                        when (serverErrorFromFirebaseException(task.exception)) {
                            ServerError.PERMISSION_DENIED -> {
                                basicContent.postValue(null)
                                Log.e(TAG, "Basic subscription permission denied")
                            }
                            ServerError.INTERNAL -> {
                                Log.e(TAG, "Basic subscription server error")
                            }
                            else -> {
                                Log.e(TAG, "Unknown error during basic content update")
                            }
                        }
                    }
                }
    }

    /**
     * Fetch premium content and post results to [premiumContent].
     * This will fail if the user does not have a premium subscription.
     */
    override fun updatePremiumContent() {
        incrementRequestCount()
        Log.d(TAG, "Calling: $PREMIUM_CONTENT_CALLABLE")
        firebaseFunctions
                .getHttpsCallable(PREMIUM_CONTENT_CALLABLE)
                .call(null)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.i(TAG, "Premium content update successful")
                        val result = (task.result?.data as? Map<String, Any>)?.let {
                            ContentResource.listFromMap(it)
                        }
                        if (result == null) {
                            Log.e(TAG, "Invalid premium subscription data")
                        } else {
                            premiumContent.postValue(result)
                        }
                    } else {
                        when (serverErrorFromFirebaseException(task.exception)) {
                            ServerError.PERMISSION_DENIED -> {
                                premiumContent.postValue(null)
                                Log.e(TAG, "Premium permission denied")
                            }
                            ServerError.INTERNAL -> {
                                Log.e(TAG, "Premium server error")
                            }
                            else -> {
                                Log.e(TAG, "Unknown error during premium content update")
                            }
                        }
                    }
                }
    }

    /**
     * Fetches subscription data from the server and posts successful results to [subscriptions].
     */
    override fun updateSubscriptionStatus() {
        incrementRequestCount()
        Log.d(TAG, "Calling: $SUBSCRIPTION_STATUS_CALLABLE")
        firebaseFunctions
                .getHttpsCallable(SUBSCRIPTION_STATUS_CALLABLE)
                .call(null)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.i(TAG, "Subscription status update successful")
                        val result = (task.result?.data as? Map<String, Any>)?.let {
                            SubscriptionStatus.listFromMap(it)
                        }
                        if (result == null) {
                            Log.e(TAG, "Invalid subscription data")
                        } else {
                            subscriptions.postValue(result)
                        }
                    } else {
                        when (serverErrorFromFirebaseException(task.exception)) {
                            ServerError.INTERNAL -> {
                                Log.e(TAG, "Subscription server error")
                            }
                            else -> {
                                Log.e(TAG, "Unknown error during subscription status update")
                            }
                        }
                    }
                }
    }

    /**
     * Register a subscription with the server and posts successful results to [subscriptions].
     */
    override fun registerSubscription(sku: String, purchaseToken: String) {
        incrementRequestCount()
        Log.d(TAG, "Calling: $REGISTER_SUBSCRIPTION_CALLABLE")
        val data = HashMap<String, String>().apply {
            put(SKU_KEY, sku)
            put(PURCHASE_TOKEN_KEY, purchaseToken)
        }
        firebaseFunctions
                .getHttpsCallable(REGISTER_SUBSCRIPTION_CALLABLE)
                .call(data)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.i(TAG, "Subscription registration successful")
                        val result = (task.result?.data as? Map<String, Any>)?.let {
                            SubscriptionStatus.listFromMap(it)
                        }
                        if (result == null) {
                            Log.e(TAG, "Invalid subscription registration data")
                        } else {
                            subscriptions.postValue(result)
                        }
                    } else {
                        when (serverErrorFromFirebaseException(task.exception)) {
                            ServerError.NOT_FOUND -> {
                                Log.e(TAG, "Invalid SKU or purchase token during registration")
                            }
                            ServerError.ALREADY_OWNED -> {
                                Log.i(TAG, "Subscription already owned by another user")
                                val oldSubscriptions = subscriptions.value
                                val newSubscription = SubscriptionStatus.alreadyOwnedSubscription(
                                        sku = sku,
                                        purchaseToken = purchaseToken
                                )
                                val newSubscriptions =
                                        insertOrUpdateSubscription(oldSubscriptions, newSubscription)
                                subscriptions.postValue(newSubscriptions)
                            }
                            ServerError.INTERNAL -> {
                                Log.e(TAG, "Subscription registration server error")
                            }
                            else -> {
                                Log.e(TAG, "Unknown error during subscription registration")
                            }
                        }
                    }
                }
    }

    /**
     * Insert or update the subscription to the list of existing subscriptions.
     *
     * If none of the existing subscriptions have a SKU that matches, insert this SKU.
     * If a subscription exists with the matching SKU, the output list will contain the new
     * subscription instead of the old subscription.
     */
    private fun insertOrUpdateSubscription(
            oldSubscriptions: List<SubscriptionStatus>?,
            newSubscription: SubscriptionStatus
    ): List<SubscriptionStatus> {
        if (oldSubscriptions == null || oldSubscriptions.isEmpty()) {
            return listOf(newSubscription)
        }
        return ArrayList<SubscriptionStatus>().apply {
            var subscriptionAdded = false
            for (subscription in oldSubscriptions) {
                if (subscription.sku == newSubscription.sku) {
                    add(newSubscription)
                    subscriptionAdded = true
                } else {
                    add(subscription)
                }
            }
            if (!subscriptionAdded) {
                add(newSubscription)
            }
        }
    }

    /**
     * Transfer subscription to this account posts successful results to [subscriptions].
     */
    override fun transferSubscription(sku: String, purchaseToken: String) {
        incrementRequestCount()
        Log.d(TAG, "Calling: $TRANSFER_SUBSCRIPTION_CALLABLE")
        val data = HashMap<String, String>().apply {
            put(SKU_KEY, sku)
            put(PURCHASE_TOKEN_KEY, purchaseToken)
        }
        firebaseFunctions
                .getHttpsCallable(TRANSFER_SUBSCRIPTION_CALLABLE)
                .call(data)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.i(TAG, "Subscription transfer successful")
                        val result = (task.result?.data as? Map<String, Any>)?.let {
                            SubscriptionStatus.listFromMap(it)
                        }
                        if (result == null) {
                            Log.e(TAG, "Invalid subscription transfer data")
                        } else {
                            subscriptions.postValue(result)
                        }
                    } else {
                        when (serverErrorFromFirebaseException(task.exception)) {
                            ServerError.NOT_FOUND -> {
                                Log.e(TAG, "Invalid SKU or purchase token during transfer")
                            }
                            ServerError.INTERNAL -> {
                                Log.e(TAG, "Subscription transfer server error")
                            }
                            else -> {
                                Log.e(TAG, "Unknown error during subscription transfer")
                            }
                        }
                    }
                }
    }

    /**
     * Register Instance ID for Firebase Cloud Messaging.
     */
    override fun registerInstanceId(instanceId: String) {
        incrementRequestCount()
        Log.d(TAG, "Calling: $REGISTER_INSTANCE_ID_CALLABLE")
        firebaseFunctions
                .getHttpsCallable(REGISTER_INSTANCE_ID_CALLABLE)
                .call(null)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.d(TAG, "Instance ID registration successful")
                    } else {
                        Log.e(TAG, "Unknown error during Instance ID registration")
                    }
                }
    }

    /**
     * Unregister Instance ID for Firebase Cloud Messaging.
     */
    override fun unregisterInstanceId(instanceId: String) {
        incrementRequestCount()
        Log.d(TAG, "Calling: $UNREGISTER_INSTANCE_ID_CALLABLE")
        firebaseFunctions
                .getHttpsCallable(UNREGISTER_INSTANCE_ID_CALLABLE)
                .call(null)
                .addOnCompleteListener { task ->
                    decrementRequestCount()
                    if (task.isSuccessful) {
                        Log.d(TAG, "Instance ID un-registration successful")
                    } else {
                        Log.e(TAG, "Unknown error during Instance ID un-registration")
                    }
                }
    }

    /**
     * Convert Firebase error codes to the app-specific meaning.
     */
    private fun serverErrorFromFirebaseException(exception: Exception?) : ServerError? {
        if (exception !is FirebaseFunctionsException) {
            Log.d(TAG, "Unrecognized Exception: $exception")
            return null
        }
        val code = exception.code
        return when (code) {
            FirebaseFunctionsException.Code.NOT_FOUND -> ServerError.NOT_FOUND
            FirebaseFunctionsException.Code.ALREADY_EXISTS -> ServerError.ALREADY_OWNED
            FirebaseFunctionsException.Code.PERMISSION_DENIED -> ServerError.PERMISSION_DENIED
            FirebaseFunctionsException.Code.INTERNAL -> ServerError.INTERNAL
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> {
                Log.e(TAG, "RESOURCE_EXHAUSTED: Check your server quota")
                ServerError.INTERNAL
            }
            else -> {
                Log.d(TAG, "Unexpected Firebase Exception: $code")
                null
            }
        }
    }

    companion object {
        private const val TAG = "ServerImpl"

        private const val SKU_KEY = "sku"
        private const val PURCHASE_TOKEN_KEY = "token"
        private const val BASIC_CONTENT_CALLABLE = "content_basic"
        private const val PREMIUM_CONTENT_CALLABLE = "content_premium"
        private const val SUBSCRIPTION_STATUS_CALLABLE = "subscription_status"
        private const val REGISTER_SUBSCRIPTION_CALLABLE = "subscription_register"
        private const val TRANSFER_SUBSCRIPTION_CALLABLE = "subscription_transfer"
        private const val REGISTER_INSTANCE_ID_CALLABLE = "instanceId_register"
        private const val UNREGISTER_INSTANCE_ID_CALLABLE = "instanceId_unregister"
    }

}
