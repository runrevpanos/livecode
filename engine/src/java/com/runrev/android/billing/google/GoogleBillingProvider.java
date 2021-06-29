

package com.runrev.android.billing.google;

import com.runrev.android.billing.google.Purchase;
import com.runrev.android.billing.*;
import com.runrev.android.Engine;

import android.app.*;
import android.util.*;
import android.content.*;

import java.util.*;



public class GoogleBillingProvider implements BillingProvider
{
    public static final String TAG = "GoogleBillingProvider";
    private Activity mActivity;
    private Boolean started = false;
    private PurchaseObserver mPurchaseObserver;
    private Map<String,String> types = new HashMap<String,String>();
    private Map<String,Map<String,String>> itemProps = new HashMap<String, Map<String,String>>();
    
    private Map<String,Purchase> purchasesMap = new HashMap<String,Purchase>();
	
	private List<SkuDetails> knownItems = new ArrayList<SkuDetails>();
    private Set<String> ownedItems = new HashSet<String>();
    
    private SkuDetails mSkuDetails;
    /* 
     Temp var for holding the productId, to pass it in onIabPurchaseFinished(IabResult result, Purchase purchase), in case purchase is null.
     Thus, purchase.getSku() will not work
    */
    private String pendingPurchaseSku = "";
    
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    
    // The helper object
    IabHelper mHelper = null;
    
    
    
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            // Do stuff when purchase is acknowledged
            Log.d(TAG, "Purchase acknowledged");
        }
    };
    
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        // To be implemented in a later section.
        }
    };
    
    private BillingClient billingClient = BillingClient.newBuilder(activity)
    .setListener(purchasesUpdatedListener)
    .enablePendingPurchases()
    .build();
    
    public void initBilling()
    {
        /*
        String t_public_key = Engine.doGetCustomPropertyValue("cREVStandaloneSettings", "android,storeKey");
        
        if (t_public_key != null && t_public_key.length() > 0)
            Security.setPublicKey(t_public_key);
        
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getActivity(), t_public_key);
        
        // TODO enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);
        
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
                           {
            public void onIabSetupFinished(IabResult result)
            {
                Log.d(TAG, "Setup finished.");
                
                if (!result.isSuccess())
                {
                    // Oh no, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }
                
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                
                // IAB is fully set up.
                Log.d(TAG, "Setup successful.");
                
                //mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
         */
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
        
    }
	
	public void onDestroy()
	{
        /*
		if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
         */
	}
    
    public boolean canMakePurchase()
    {
        /*
        if (mHelper == null)
            return false;
	
        else
            return mHelper.is_billing_supported;
         */
        return true;
    }
    
    public boolean enableUpdates()
    {
        /*
        if (mHelper == null)
            return false;
        */
        return true;
    }
    
    public boolean disableUpdates()
    {
        /*
        if (mHelper == null)
            return false;
        */
		return true;
    }
    
    public boolean restorePurchases()
    {
        if (billingClient == null)
			return false;

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onPurchaseResponse(int responseCode, List<Purchase> purchases)
            {
                if (responseCode == BillingClient.BillingResponse.OK && purchases != null)
                {
                    boolean t_did_restore;
                    t_did_restore = false;

                    for (Purchase purchase : purchases)
                    {
                        addPurchaseToLocalInventory(purchase);
                        ownedItems.add(purchase.getSku());
                        // onPurchaseStateChanged to be called with state = 5 (restored)
                        mPurchaseObserver.onPurchaseStateChanged(purchase.getSku(), 5);
                        t_did_restore = true;
                    }

                    if(!t_did_restore)
                    {
                        // PM-2015-02-12: [[ Bug 14402 ]] When there are no previous purchases to restore, send a purchaseStateUpdate msg with state=restored and productID=""
                        mPurchaseObserver.onPurchaseStateChanged("",5);
                    }
                }
                else
                {
                    Log.d(TAG, "Failed to restore purchases.");
                }
            }
        });
        return true;
    }
    
    public boolean sendRequest(int purchaseId, String productId, String developerPayload)
    {
        /*
        if (mHelper == null)
            return false;
        
        String type = productGetType(productId);
        if (type == null)
        {
            Log.i(TAG, "Item type is null (not specified). Exiting..");
            return false;
        }
        
        pendingPurchaseSku = productId;
        
        Log.i(TAG, "purchaseSendRequest(" + purchaseId + ", " + productId + ", " + type + ")");
        
        if (type.equals("subs"))
        {
            mHelper.launchSubscriptionPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
			return true;
        }
        else if (type.equals("inapp"))
        {
            mHelper.launchPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
			return true;
        }
		else
		{
			Log.i(TAG, "Item type is not recognized. Exiting..");
            return false;
		}
         */

        if (billingClient == null)
            return false;
        
        String type = productGetType(productId);
        if (type == null)
        {
            Log.i(TAG, "Item type is null (not specified). Exiting..");
            return false;
        }
        
        pendingPurchaseSku = productId;
        
        Log.i(TAG, "purchaseSendRequest(" + purchaseId + ", " + productId + ", " + type + ")");
        
        
        String skuToSell = productId;
        List<String> skuList = new ArrayList<> ();
        skuList.add(skuToSell);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        
        if (type.equals("inapp"))
            params.setSkusList(skuList).setType(SkuType.INAPP);
        else
            params.setSkusList(skuList).setType(SkuType.SUBS);
        
        billingClient.querySkuDetailsAsync(params.build(),
                                           new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult,
                                             List<SkuDetails> skuDetailsList) {
                // Process the result.
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        if (skuDetailsList == null || skuDetailsList.isEmpty())
                        {
                            Log.e(TAG, "onSkuDetailsResponse: " +
                                  "Found null or empty SkuDetails. " +
                                  "Check to see if the SKUs you requested are correctly published " +
                                  "in the Google Play Console.");
                        }
                        else
                        {
                            for (SkuDetails skuDetails : skuDetailsList)
                            {
                                String sku = skuDetails.getSku();
                                
                                if (sku.equals(productId))
                                {
                                    mSkuDetails = skuDetails;
                                }
                                
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                        // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });
        
        // An activity reference from which the billing flow will be launched.
        Activity activity = getActivity();
        
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(mSkuDetails).build();
        
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
        
        // Handle the result.
        
        /*
        if (type.equals("subs"))
        {
            mHelper.launchSubscriptionPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
            return true;
        }
        else if (type.equals("inapp"))
        {
            mHelper.launchPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, developerPayload);
            return true;
        }
        else
        {
            Log.i(TAG, "Item type is not recognized. Exiting..");
            return false;
        }
         */
    }
    
    public boolean makePurchase(String productId, String quantity, String payload)
    {
        if (mHelper == null)
            return false;
        
        String type = productGetType(productId);
        if (type == null)
        {
            Log.i(TAG, "Item type is null (not specified). Exiting..");
            return false;
        }
        
        pendingPurchaseSku = productId;
		setPurchaseProperty(productId, "developerPayload", payload);
        
        Log.i(TAG, "purchaseSendRequest("  + productId + ", " + type + ")");
        
        if (type.equals("subs"))
        {
            mHelper.launchSubscriptionPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, payload);
			return true;
        }
        else if (type.equals("inapp"))
		{
            mHelper.launchPurchaseFlow(getActivity(), productId, RC_REQUEST, mPurchaseFinishedListener, payload);
			return true;
        }
		else
		{
			Log.i(TAG, "Item type is not recognized. Exiting..");
            return false;
		}
        
    }
    
    public boolean productSetType(String productId, String productType)
    {
        Log.d(TAG, "Setting type for productId" + productId + ", type is : " + productType);
        types.put(productId, productType);
        Log.d(TAG, "Querying HashMap, type is " + types.get(productId));
        return true;
    }
    
    private String productGetType(String productId)
    {
        return types.get(productId);
    }
    
    public boolean consumePurchase(final String productId)
    {
        Purchase purchase = getPurchaseFromMap(productId);
        handleConsumablePurchase(purchase);
        return true;
        /*
        mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener()
                                    {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                
                if (result.isFailure())
                {
                    return;
                }
                else
                {
                    Purchase purchase = inventory.getPurchase(productId);
                    // Do this check to avoid a NullPointerException
                    if (purchase == null)
                    {
                        Log.d(TAG, "You cannot consume item : " + productId + ", since you don't own it!");
                        return;
                    }
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
            }
        });
        return true;
         */
    }
    
    public boolean requestProductDetails(final String productId)
    {
        //arbitrary initial capacity
        int capacity = 25; 
        List<String> productList = new ArrayList<String>(capacity);
        productList.add(productId);
        mHelper.queryInventoryAsync(true, productList, new IabHelper.QueryInventoryFinishedListener()
                                    {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                
                if (result.isFailure())
                {
                    return;
                }
                else
                {
                    SkuDetails skuDetails = inventory.getSkuDetails(productId);
                    // Do this check to avoid a NullPointerException
                    if (skuDetails == null)
                    {
                        Log.d(TAG, "No product found with the specified ID : " + productId + " !");
						mPurchaseObserver.onProductDetailsError(productId, "No product found with the specified ID");
                        return;
                    }
                   
					knownItems.add(skuDetails);
					loadKnownItemToLocalInventory(skuDetails);
                    Log.d(TAG, "Details for requested product : " + skuDetails.toString());
                    
					mPurchaseObserver.onProductDetailsReceived(productId);
					
                }
            }
        });
		
		return true;
    }
	
	public String receiveProductDetails(String productId)
    {
		for (SkuDetails skuDetails : knownItems)
		{
			if (productId.equals(skuDetails.getSku()))
			{
				return skuDetails.toString();
			}
		}
		
        return "Product ID not found";
	}
    
    public boolean confirmDelivery(String productId)
    {
        /*
        if (mHelper == null)
            return false;
        
        else
            return true;
         */
        Purchase purchase = getPurchaseFromMap(productId);
        handleNonConsumablePurchase(purchase);
        return true;
    }
    
    public void setPurchaseObserver(PurchaseObserver observer)
    {
        mPurchaseObserver = observer;
    }
    
	
    Activity getActivity()
    {
        return mActivity;
    }
    
    public void setActivity(Activity activity)
    {
        mActivity = activity;
    }
    
    public boolean setPurchaseProperty(String productId, String propertyName, String propertyValue)
    {
        if (!itemProps.containsKey(productId))
            itemProps.put(productId, new HashMap<String,String>());
        (itemProps.get(productId)).put(propertyName, propertyValue);
                
        return true;
    }
        
    public boolean addPurchaseToMap(String productId, Purchase purchase)
    {
        if (!purchasesMap.containsKey(productId))
            purchasesMap.put(productId, purchase);
        
        return true;
    }
    
    public Purchase getPurchaseFromMap(String productId)
    {
        if (!purchasesMap.containsKey(productId))
            return null;
            
        return purchasesMap.get(productId);
    }
    
    void removePurchaseFromMap(Purchase purchase)
    {
        purchasesMap.remove(purchase.getSku());
    }
    
    
    public String getPurchaseProperty(String productId, String propName)
    {
        Log.d(TAG, "Stored properties for productId :" + productId);
        Map<String,String> map = itemProps.get(productId);
        if (map != null)
            return map.get(propName);
        else
            return "";
    }
    
    public String getPurchaseList()
    {
        return ownedItems.toString();
    }
    
    //some helper methods
    
    boolean addPurchaseToLocalInventory(Purchase purchase)
    {
        boolean success = true;
		if (success)
            success = setPurchaseProperty(purchase.getSku(), "productId", purchase.getSku());
        if (success)
            success = setPurchaseProperty(purchase.getSku(), "itemType", purchase.getItemType());
        
        if (success)
            success = setPurchaseProperty(purchase.getSku(), "orderId", purchase.getOrderId());
        
        if (success)
            success = setPurchaseProperty(purchase.getSku(), "packageName", purchase.getPackageName());

        if (success)
            success = setPurchaseProperty(purchase.getSku(), "purchaseToken", purchase.getToken());

        if (success)
            success = setPurchaseProperty(purchase.getSku(), "signature", purchase.getSignature());

		if (success)
            success = setPurchaseProperty(purchase.getSku(), "developerPayload", purchase.getDeveloperPayload());

        if (success)
            success = setPurchaseProperty(purchase.getSku(), "purchaseTime", new Long(purchase.getPurchaseTime()).toString());
			
        return success;

    }
	
	boolean loadKnownItemToLocalInventory(SkuDetails skuDetails)
	{
		boolean success = true;
		if (success)
            success = setPurchaseProperty(skuDetails.getSku(), "productId", skuDetails.getSku());
        if (success)
            success = setPurchaseProperty(skuDetails.getSku(), "itemType", skuDetails.getType());
        
        if (success)
            success = setPurchaseProperty(skuDetails.getSku(), "price", skuDetails.getPrice());
        
        if (success)
            success = setPurchaseProperty(skuDetails.getSku(), "title", skuDetails.getTitle());
		
        if (success)
            success = setPurchaseProperty(skuDetails.getSku(), "description", skuDetails.getDescription());
		
        return success;
	}
    
    void removePurchaseFromLocalInventory(Purchase purchase)
    {
        ownedItems.remove(purchase.getSku());
        
    }
    
    void complain(String message)
    {
        Log.d(TAG, "**** Error: " + message);
        alert("Error: " + message);
    }
    
    void alert(String message)
    {
        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    
    // Listeners
    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        // parameter "purchase" is null if purchase failed
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            
            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
            {
                return;
            }
    
            if (result.isFailure())
            {
				// PM-2015-01-27: [[ Bug 14450 ]] [Removed code] No need to display an alert with the error message, since this information is also contained in the purchaseStateUpdate message
                mPurchaseObserver.onPurchaseStateChanged(pendingPurchaseSku, mapResponseCode(result.getResponse()));
                pendingPurchaseSku = "";
                return;
            }
            
            if (!verifyDeveloperPayload(purchase))
            {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }
            
            Log.d(TAG, "Purchase successful.");
            pendingPurchaseSku = "";
			ownedItems.add(purchase.getSku());
            addPurchaseToLocalInventory(purchase);
            offerPurchasedItems(purchase);
                
        }
    };

    void offerPurchasedItems(Purchase purchase)
    {
        if (purchase != null)
            mPurchaseObserver.onPurchaseStateChanged(purchase.getSku(), mapResponseCode(purchase.getPurchaseState()));

    }

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished(Purchase purchase, IabResult result)
        {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (result.isSuccess())
            {
                Log.d(TAG, "Consumption successful. Provisioning.");
                removePurchaseFromLocalInventory(purchase);
            }
            else
            {
                complain("Error while consuming: " + result);
            }

            Log.d(TAG, "End consumption flow.");
        }
    };

    // Called when we finish querying the items we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            if (result.isFailure())
            {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");

			// PM-2015-02-05: [[ Bug 14402 ]] Handle case when calling mobileStoreRestorePurchases but there are no previous purchases to restore
			boolean t_did_restore;
			t_did_restore = false;

            List<Purchase> purchaseList = inventory.getallpurchases();

            for (Purchase p : purchaseList)
            {
                addPurchaseToLocalInventory(p);
				ownedItems.add(p.getSku());
				// onPurchaseStateChanged to be called with state = 5 (restored)
				mPurchaseObserver.onPurchaseStateChanged(p.getSku(), 5);
				t_did_restore = true;
            }

			if(!t_did_restore)
			{
				// PM-2015-02-12: [[ Bug 14402 ]] When there are no previous purchases to restore, send a purchaseStateUpdate msg with state=restored and productID=""
				mPurchaseObserver.onPurchaseStateChanged("",5);
			}
        }
    };

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p)
    {
        String payload = p.getDeveloperPayload();

    /*
     * TODO: verify that the developer payload of the purchase is correct. It will be
     * the same one that you sent when initiating the purchase.
     *
     * WARNING: Locally generating a random string when starting a purchase and
     * verifying it here might seem like a good approach, but this will fail in the
     * case where the user purchases an item on one device and then uses your app on
     * a different device, because on the other device you will not have access to the
     * random string you originally generated.
     *
     * So a good developer payload has these characteristics:
     *
     * 1. If two different users purchase an item, the payload is different between them,
     *    so that one user's purchase can't be replayed to another user.
     *
     * 2. The payload must be such that you can verify it even when the app wasn't the
     *    one who initiated the purchase flow (so that items purchased by the user on
     *    one device work on other devices owned by the user).
     *
     * Using your own server to store and verify developer payloads across app
     * installations is recommended.
     */

        return true;
    }

    @Override
    void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases)
    {
        // Maybe it needs BillingClient.BillingResponseCode.OK
        if (billingResult.getResponseCode() == BillingResponseCode.OK
            && purchases != null)
        {
            for (Purchase purchase : purchases)
            {
                //handleConsumablePurchase(purchase);
                mPurchaseObserver.onPurchaseStateChanged(purchase.getSku(), mapResponseCode(billingResult.getResponseCode));
            }
        }
        /*else if (billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED)
        {
            // Handle an error caused by a user cancelling the purchase flow.
        }*/
        else
        {
            // Handle any other error codes.
            mPurchaseObserver.onPurchaseStateChanged(pendingPurchaseSku, mapResponseCode(billingResult.getResponseCode));
            pendingPurchaseSku = "";

        }
    }

    void handleConsumablePurchase(Purchase purchase)
    {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
        //Purchase purchase = ...;

        //PANOS: Check type. If consumable, then consume. Otherwise acknowledge the purchase

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken)
            {
                if (billingResult.getResponseCode() == BillingResponseCode.OK)
                {
                    // Handle the success of the consume operation.
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);

        Log.d(TAG, "Consuming purchase successful.");
        pendingPurchaseSku = "";
        removePurchaseFromMap(purchase);
        removePurchaseFromLocalInventory(purchase);

        //offerPurchasedItems(purchase);
    }

    void handleNonConsumablePurchase(Purchase purchase)
    {
        if (purchase.getPurchaseState() == PurchaseState.PURCHASED)
        {
            if (!purchase.isAcknowledged())
            {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            // not handled
            Log.d(TAG, "onActivityResult NOT handled by IABUtil.");
        }
        else
        {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    // Should match the order of enum MCAndroidPurchaseState (mblandroidstore.cpp)
    int mapResponseCode(int responseCode)
    {
        /*
        BILLING_UNAVAILABLE
        Billing API version is not supported for the type requested.

        DEVELOPER_ERROR
        Invalid arguments provided to the API.

        ERROR
        Fatal error during the API action.

        FEATURE_NOT_SUPPORTED
        Requested feature is not supported by Play Store on the current device.

        ITEM_ALREADY_OWNED
        Failure to purchase since item is already owned.

        ITEM_NOT_OWNED
        Failure to consume since item is not owned.

        ITEM_UNAVAILABLE
        Requested product is not available for purchase.

        OK
        Success.

        SERVICE_DISCONNECTED
        Play Store service is not connected now - potentially transient state.

        SERVICE_TIMEOUT
        The request has reached the maximum timeout before Google Play responds.

        SERVICE_UNAVAILABLE
        Network connection is down.

        USER_CANCELED
        User pressed back or canceled a dialog.
        */

        int result;
        switch(responseCode)
        {
            case BillingResponseCode.OK:
                result = 0;
                break;

            case BillingResponseCode.USER_CANCELED:
                result = 1;
                break;

			case BillingResponseCode.ITEM_UNAVAILABLE:
				result = 2;
				break;

            case BillingResponseCode.ITEM_ALREADY_OWNED:
                result = 3;
                break;

            default:
                result = 1;
                break;
        }
        return result;
    }

}
