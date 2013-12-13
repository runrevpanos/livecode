/* Copyright (C) 2003-2013 Runtime Revolution Ltd.
 
 This file is part of LiveCode.
 
 LiveCode is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License v3 as published by the Free
 Software Foundation.
 
 LiveCode is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 for more details.
 
 You should have received a copy of the GNU General Public License
 along with LiveCode.  If not see <http://www.gnu.org/licenses/>.  */

package com.runrev.android.billing;
import com.amazon.inapp.purchasing.*;


public class AmazonBillingProvider
{
    
    class MyPurchasingObserver extends BasePurchasingObserver
    {
        
        private boolean rvsProductionMode = false;
        private String currentUserID = null;
        
        public void onSDKAvailable(boolean isSandboxMode)
        {
            // Switch RVS URL from test to production
            rvsProductionMode = !isSandboxMode;
        }
        

        public void onGetUserIdResponse(final GetUserIdResponse response)
        {
            if (response.getUserIdRequestStatus() ==
                GetUserIdResponse.GetUserIdRequestStatus.SUCCESSFUL)
            {
                currentUserID = response.getUserId();
            }
            else
            {
                // Fail gracefully.
            }
        }
        
        public static Offset getPersistedOffset()
        {
            // Retrieve the offset you have previously persisted.
            // If no offset exists or the app is dealing exclusively with consumables
            // use Offset.BEGINNING.
        }
        
        
        public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse response)
        {
            // No implementation required when dealing solely with consumables
            switch (response.getPurchaseUpdatesRequestStatus())
            {
                case SUCCESSFUL:
                    // Check for revoked SKUs
                    for (final String sku : response.getRevokedSkus())
                    {
                        Log.v(TAG, "Revoked Sku:" + sku);
                    }
                    
                    // Process receipts
                    for (final Receipt receipt : response.getReceipts())
                    {
                        switch (receipt.getItemType())
                        {
                            case ENTITLED:
                                // If the receipt is for an entitlement,the customer is re-entitled.
                                // Add re-entitlement code here
                                break;
                            case SUBSCRIPTION:
                                // Purchase Updates forsubscriptions can be done here in one of two ways:
                                // 1. Use the receipts to determineif the user currently has an active subscription
                                // 2. Use the receipts to create asubscription history for your customer.
                                break;
                        }
                    }
                    
                    final Offset newOffset = response.getOffset();
                    if (response.isMore()) {
                        Log.v(TAG, "Initiating Another Purchase Updates with offset: "
                              + newOffset.toString());
                        PurchasingManager.initiatePurchaseUpdatesRequest(newOffset);
                    }
                    break;
                case FAILED:
                    // Provide the user access to any previously persisted entitlements.
                    break;
            }
        }
        
        public void onPurchaseResponse(PurchaseResponse response)
        {
            final String status = response.getPurchaseRequestStatus();
            
            if (status == PurchaseResponse.getPurchaseRequestStatus.SUCCESSFUL)
            {
                Receipt receipt = response.getReceipt();
                String itemType = receipt.getItemType();
                String sku = receipt.getSku();
                String purchaseToken = receipt.getPurchaseToken();
                
                // Store receipt and enable access to consumable
            }
        }
    }
    
    private MyPurchasingObserver mPurchasingObserver = null;
    private PurchaseObserver mPurchaseObserver;
    
    /*Always equals true. The Amazon Appstore allows a customer to disable In-App Purchasing, the IAP workflow
     will reflect this when the user is prompted to buy an item. There is no way for your app to know if a 
     user has disabled Amazon In-App Purchasing */
    boolean canMakePurchase()
    {
        return true;
    }
    
    boolean enableUpdates()
    {
        PurchasingManager.registerObserver(mPurchasingObserver);
        return true;
    }
    
    boolean disableUpdates()
    {
        return false;
    }
    
    boolean restorePurchases()
    {
        PurchasingManager.initiatePurchaseUpdatesRequest(MyPurchasingObserver.getPersistedOffset());
        return true;
    }
    
    //boolean sendRequest(int purchaseId, String productId, Map<String, String> properties){}
    boolean sendRequest(int purchaseId, String productId, String developerPayload)
    {
        PurchasingManager.initiatePurchaseRequest(productId);
    }
    
    //boolean confirmDelivery(int purchaseId){}
    boolean confirmDelivery(int purchaseId, String notificationId)
    {
        // Amazon client is responsible for validating purchase receipts. Does that mean that this method does nothing?
    }
    
    void setPurchaseObserver(PurchaseObserver observer)
    {
        mPurchaseObserver = observer;
    }
    
    void initBilling()
    {
        PurchasingManager.initiateGetUserIdRequest();
        //the <receiver> sections of the manifest are added by the standalone builder
    }
}