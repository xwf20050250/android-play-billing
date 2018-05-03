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

package com.example.subscriptions.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class SubscriptionStatusTest {

    @Test
    fun alreadyOwnedIsNotValid() {
        val sub = SubscriptionStatus.alreadyOwnedSubscription(
                sku = "TEST_SKU",
                purchaseToken = "TEST_PURCHASE_TOKEN"
        )
        assertEquals(sub.sku, "TEST_SKU")
        assertEquals(sub.purchaseToken, "TEST_PURCHASE_TOKEN")
        assertFalse("Sub must not be active", sub.isEntitlementActive)
        assertTrue("Sub must be already owned", sub.subAlreadyOwned)
    }

    @Test
    fun parseEmptyFromJson() {
        val testData = readTestData("empty.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return
        assertTrue("Empty data must create an empty list", subList.isEmpty())
    }

    @Test
    fun parseEmptyFromHashMap() {
        val testData = readTestData("empty.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertTrue("Empty data must create an empty list", subList.isEmpty())
    }

    @Test
    fun parseInvalidJson() {
        val testData = readTestData("invalid.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNull("Invalid data must return null", subList)
    }

    @Test
    fun doNotCrashWithUnrecognizedKeyFromJson() {
        val testData = readTestData("unrecognized.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNull("Unrecognized data must return null", subList)
    }

    @Test
    fun doNotCrashWithUnrecognizedKeyFromHashMap() {
        val map = HashMap<String, Any>().apply {
            put("unrecognized_key", "should not crash")
        }
        val subList = SubscriptionStatus.listFromMap(map)
        assertNull("Unrecognized data must return null", subList)
    }

    @Test
    fun parseBasicSubscriptionFromJson() {
        val testData = readTestData("basic.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be basic", "basic_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseBasicSubscriptionFromHashMap() {
        val testData = readTestData("basic.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be basic", "basic_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseBasicSubscriptionFromJsonWithUnrecognizedKey() {
        val testData = readTestData("unrecognizedbasic.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be basic", "basic_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseBasicSubscriptionFromHashMapWithUnrecognizedKey() {
        val testData = readTestData("unrecognizedbasic.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }
        map["unrecognized_key"] = "should not crash"

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be basic", "basic_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parsePremiumSubscriptionFromHashMap() {
        val testData = readTestData("premium.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be premium", "premium_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parsePremiumSubscriptionFromJson() {
        val testData = readTestData("premium.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must renew", willRenew)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1LJ" +
                    "SgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertEquals("Expiry time must match", 1523347054184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertEquals("Subscription must be premium", "premium_subscription", sku)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseAccountHoldFromJson() {
        val testData = readTestData("accounthold.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertEquals("SKU must match", "basic_subscription", sku)
            assertEquals("Token must match", "jlbjaefklclbngobohijodfa." +
                    "AO-J1Ozh_NnSw4YWqcGNaVXBndXhUz1zS-6v_NDW6BaVTLCM9VOSfjLXTVgT2AlJdQD7" +
                    "TTxxe_kWV6U3mklXZgoJ7HxYEqtpkUtFsOfVjM0hf10tKXHZ1CPH4dVgGbCpFy2eI6ctYVgG",
                    purchaseToken)
            assertFalse("Subscription must not be active", isEntitlementActive)
            assertTrue("Renewal must match", willRenew)
            assertEquals("Expiry time must match", 1523324641475L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseAccountHoldFromHashMap() {
        val testData = readTestData("accounthold.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertEquals("SKU must match", "basic_subscription", sku)
            assertEquals("Token must match", "jlbjaefklclbngobohijodfa." +
                    "AO-J1Ozh_NnSw4YWqcGNaVXBndXhUz1zS-6v_NDW6BaVTLCM9VOSfjLXTVgT2AlJdQD7" +
                    "TTxxe_kWV6U3mklXZgoJ7HxYEqtpkUtFsOfVjM0hf10tKXHZ1CPH4dVgGbCpFy2eI6ctYVgG",
                    purchaseToken)
            assertFalse("Subscription must not be active", isEntitlementActive)
            assertTrue("Renewal must match", willRenew)
            assertEquals("Expiry time must match", 1523324641475L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertFalse("Grace period must match", isGracePeriod)
            assertTrue("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseGracePeriodFromJson() {
        val testData = readTestData("graceperiod.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must be active", isEntitlementActive)
            assertTrue("Subscription must be grace period", isGracePeriod)
        }
    }

    @Test
    fun parseGracePeriodFromHashMap() {
        val testData = readTestData("graceperiod.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertTrue("Subscription must be active", isEntitlementActive)
            assertTrue("Subscription must be grace period", isGracePeriod)
        }
    }

    @Test
    fun parseCanceledSubscriptionFromJson() {
        val testData = readTestData("canceled.json")
        val subList = SubscriptionStatus.listFromJsonString(testData)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertEquals("SKU must match", "cheap_subscription", sku)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1L" +
                    "JSgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertFalse("Subscription must not renew", willRenew)
            assertEquals("Expiry time must match", 1523346934184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertFalse("Grace period must match", isGracePeriod)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    @Test
    fun parseCanceledSubscriptionFromHashMap() {
        val testData = readTestData("canceled.json")
        val map = mapFromJson(testData) ?: HashMap<String, Any>().also {
            fail("Could not run test because HashMap could not be created")
        }

        val subList = SubscriptionStatus.listFromMap(map)
        assertNotNull("List must not be null", subList)
        if (subList == null) return

        assertFalse("Subscription must not be empty", subList.isEmpty())
        subList[0].run {
            assertEquals("SKU must match", "cheap_subscription", sku)
            assertEquals("Token must match", "onhhinbenecfbpohlgpkpica." +
                    "AO-J1OyHDwGfi22SvG2VdeGdrR9nz0D3WY_YPda6qr7yssmdQ6oX2PKEiKfaN4B9LVx1L" +
                    "JSgkLVfuWbho2ReugyGThDgNy66a4EltFlLGVwZ4JK_CfTE5ypYz7E0SGuyO4wQNItR4hUP",
                    purchaseToken)
            assertTrue("Subscription must be active", isEntitlementActive)
            assertFalse("Subscription must not renew", willRenew)
            assertEquals("Expiry time must match", 1523346934184L, activeUntilMillisec)
            assertFalse("Free trial must match", isFreeTrial)
            assertFalse("Grace period must match", isGracePeriod)
            assertFalse("Account hold must match", isAccountHold)
        }
    }

    /**
     * Read JSON file data for testing.
     */
    private fun readTestData(filename: String): String {
        val inputStream = javaClass.getResourceAsStream(filename)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        do {
            line = reader.readLine()
            stringBuilder.append(line ?: "")
        } while (line != null)
        return stringBuilder.toString()
    }

    /**
     * Create HashMap from JSON string for testing.
     */
    private fun mapFromJson(data: String): HashMap<String, Any>? {
        val subscriptions = SubscriptionStatus.listFromJsonString(data) ?: return null
        return HashMap<String, Any>().apply {
            val subList = ArrayList<HashMap<String, Any>>()
            for (subscriptionStatus in subscriptions) {
                subList.add(
                        HashMap<String, Any>().apply {
                            (subscriptionStatus.sku as? Any)?.let {
                                put(SubscriptionStatus.SKU_KEY, it)
                            }
                            (subscriptionStatus.purchaseToken as? Any)?.let {
                                put(SubscriptionStatus.PURCHASE_TOKEN_KEY, it)
                            }
                            (subscriptionStatus.isEntitlementActive as? Any)?.let {
                                put(SubscriptionStatus.IS_ENTITLEMENT_ACTIVE_KEY, it)
                            }
                            (subscriptionStatus.willRenew as? Any)?.let {
                                put(SubscriptionStatus.WILL_RENEW_KEY, it)
                            }
                            (subscriptionStatus.activeUntilMillisec as? Any)?.let {
                                put(SubscriptionStatus.ACTIVE_UNTIL_MILLISEC_KEY, it)
                            }
                            (subscriptionStatus.isFreeTrial as? Any)?.let {
                                put(SubscriptionStatus.IS_FREE_TRIAL_KEY, it)
                            }
                            (subscriptionStatus.isGracePeriod as? Any)?.let {
                                put(SubscriptionStatus.IS_GRACE_PERIOD_KEY, it)
                            }
                            (subscriptionStatus.isAccountHold as? Any)?.let {
                                put(SubscriptionStatus.IS_ACCOUNT_HOLD_KEY, it)
                            }
                        }
                )
            }
            put(SubscriptionStatus.SUBSCRIPTIONS_KEY, subList)
        }
    }

}
