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

import java.util.*;
import com.sec.android.iap.sample.helper.SamsungIapHelper;

public class SamsungBillingProvider
{
    class iapHelper implements OnInitIapListener, OnGetItemListListener, OnGetInboxListListener
    {
        public Activity mActivity;
        
        public static final int iapMode = SamsungIapHelper.IAP_MODE_COMMERCIAL;
        /* private static final int iapMode = SamsungIapHelper.IAP_MODE_TEST_SUCCESS; */
        private boolean isInitialized = false;
        private String pendingPurchaseItemId = null;
        private int transactionCounter = 0;
        private String itemGroupId = null;
        
        public void init_iapHelper()
        {
            
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SamsungIapHelper helper = SamsungIapHelper.getInstance(getActivity(), iapMode);
                    helper.setOnInitIapListener(iapHelper.this);
                    helper.setOnGetItemListListener(iapHelper.this);
                    helper.setOnGetInboxListListener(iapHelper.this);
                    
                    if (helper.isInstalledIapPackage(getActivity()))
                    {
                        if (!helper.isValidIapPackage(getActivity()))
                        {
                            helper.showIapDialog(
                                                 getActivity(),
                                                 helper.getValueString(getActivity(), "title_iap"),
                                                 helper.getValueString(getActivity(), "msg_invalid_iap_package"),
                                                 true,
                                                 null
                                                 );
                        }
                    }
                    else
                    {
                        helper.installIapPackage(getActivity());
                    }
                }
            });
        }
        
        public void startPurchase(String itemId)
        {
            if (!isInitialized)
            {
                isInitialized = true;
                pendingPurchaseItemId = itemId;
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SamsungIapHelper helper = SamsungIapHelper.getInstance(getActivity(), iapMode);
                        helper.showProgressDialog(getActivity());
                        helper.startAccountActivity(getActivity());
                    }
                });
                return;
            }
            
            SamsungIapHelper helper = SamsungIapHelper.getInstance(getActivity(), iapMode);
            helper.showProgressDialog(getActivity());
            ++transactionCounter;
            helper.startPurchase(
                                 getActivity(),
                                 SamsungIapHelper.REQUEST_CODE_IS_IAP_PAYMENT,
                                 itemGroupId,
                                 itemId
                                 );
        }
        
        public Activity getActivity()
        {
            return mActivity;
        }
        
        public void setActivity(Activity activity)
        {
            mActivity = activity;
        }
        
        public void bindIapService()
        {
            SamsungIapHelper helper = SamsungIapHelper.getInstance(getActivity(), iapMode);
            helper.bindIapService(new SamsungIapHelper.OnIapBindListener() {
                @Override
                public void onBindIapFinished(int result)
                {
                    SamsungIapHelper helper = SamsungIapHelper.getInstance(getActivity(), iapMode);
                    if (pendingPurchaseItemId == null) {
                        helper.dismissProgressDialog();
                    }
                    
                    if (result == SamsungIapHelper.IAP_RESPONSE_RESULT_OK) {
                        helper.safeInitIap(getActivity());
                    } else {
                        helper.showIapDialog(
                                             getActivity(),
                                             helper.getValueString(getActivity(), "title_iap"),
                                             helper.getValueString(getActivity(), "msg_iap_service_bind_failed"),
                                             false,
                                             null
                                             );
                    }
                }
            });
        }

    }
    
    public iapHelper iaphelper = new iapHelper();
    private PurchaseObserver mPurchaseObserver;
    
    boolean canMakePurchase()
    {
        
    }
    
    boolean enableUpdates()
    {
        iaphelper.bindIapService();
        return true;
    }
    
    boolean disableUpdates()
    {
        iaphelper.dispose();
        return true;
        
    }
    
    boolean restorePurchases()
    {
        
    }
    
    //boolean sendRequest(int purchaseId, String productId, Map<String, String> properties){}
    boolean sendRequest(int purchaseId, String productId, String developerPayload)
    {
        iaphelper.startPurchase(productId);
        return true;
    }
    
    //boolean confirmDelivery(int purchaseId){}
    boolean confirmDelivery(int purchaseId, String notificationId)
    {
        
    }
    
    void setPurchaseObserver(PurchaseObserver observer)
    {
        mPurchaseObserver = observer;
    }
    
    void initBilling()
    {
        String t_itemGroupId = Engine.doGetCustomPropertyValue("cREVStandaloneSettings", "android,itemGroupId");
        iaphelper.itemGroupId = t_itemGroupId;
        iaphelper.init_iapHelper();
    }
}