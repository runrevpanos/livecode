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


public class AndroidMarketBillingProvider
{    
    private BillingService mBilling = null;
	private PurchaseObserver mPurchaseObserver;
    private static Class mBillingServiceClass = null;
    
    boolean canMakePurchase()
    {
        if (mBilling == null)
			return false;
        
		return mBilling.checkBillingSupported();
    }
    
    boolean enableUpdates()
    {
        if (mBilling == null)
			return false;
        
        ResponseHandler.register(mPurchaseObserver);
        return true;
    }
    
    boolean disableUpdates()
    {
        if (mBilling == null)
			return false;
        
        ResponseHandler.unregister(mPurchaseObserver);
        return true;
    }
    
    boolean restorePurchases()
    {
        if (mBilling == null)
			return false;
        
		return mBilling.restoreTransactions();
    }
    
    //boolean sendRequest(int purchaseId, String productId, Map<String, String> properties){}
    boolean sendRequest(int purchaseId, String productId, String developerPayload)
    {
        if (mBilling == null)
			return false;
        
		Log.i(TAG, "purchaseSendRequest(" + purchaseId + ", " + productId + ")");
		return mBilling.requestPurchase(purchaseId, productId, developerPayload);
    }
    
    //boolean confirmDelivery(int purchaseId){}
    boolean confirmDelivery(int purchaseId, String notificationId)
    {
        if (mBilling == null)
			return false;
        
		return mBilling.confirmNotification(purchaseId, notificationId);
    }
    
    void setPurchaseObserver(PurchaseObserver observer)
    {
        mPurchaseObserver = observer;
    }
    
    
	public static Class getBillingServiceClass()
	{
		return mBillingServiceClass;
	}
    
	private static void setBillingServiceClass(Class pClass)
	{
		mBillingServiceClass = pClass;
	}

    void initBilling()
    {
        String t_public_key = Engine.doGetCustomPropertyValue("cREVStandaloneSettings", "android,storeKey");
        if (t_public_key != null && t_public_key.length() > 0)
            Security.setPublicKey(t_public_key);
        
        String classFqn = Engine.getEngine().getContext().getPackageName() + ".AppService";
		try
		{
			Class tClass = Class.forName(classFqn);
			setBillingServiceClass(tClass);
            
			mBilling = (BillingService)tClass.newInstance();
		}
		catch (Exception e)
		{
			return;
		}
        
		mBilling.setContext(Engine.getEngine().getContext());
        
		ResponseHandler.register(mPurchaseObserver);
        
    }
    
    
}