/*
 * Copyright 2013, 2014 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.brq.wallet.activity.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.Toast;

import com.brq.wallet.Utils;
import com.brq.wallet.lt.LocalTraderEventSubscriber;
import com.google.common.base.Preconditions;
import com.brq.wallet.MbwManager;
import com.brq.wallet.R;
import com.brq.wallet.lt.LocalTraderManager;
import com.brq.wallet.lt.activity.LtMainActivity;

public class LocalTraderFragment extends Fragment {
   private MbwManager _mbwManager;
   private LocalTraderManager _ltManager;
   private View _root;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      _root = Preconditions.checkNotNull(inflater.inflate(R.layout.main_lt_fragment, container, false));
      return _root;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setHasOptionsMenu(false);
      super.onCreate(savedInstanceState);
   }

   @Override
   public void onAttach(Activity activity) {
      _mbwManager = MbwManager.getInstance(activity);
      _ltManager = _mbwManager.getLocalTraderManager();
      super.onAttach(activity);
   }

   @Override
   public void onResume() {
      for (int id: new int[]{R.id.ivMyceliumLogo, R.id.llMyceliumBuySell}) {
         _root.findViewById(id).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
               localTraderSelected();
            }
         });
      }
      _mbwManager.getLocalTraderManager().subscribe(ltSubscriber);
      updateUi();
      super.onResume();
   }

   @Override
   public void onPause() {
      _mbwManager.getLocalTraderManager().unsubscribe(ltSubscriber);
      super.onPause();
   }

   private void localTraderSelected() {
      if (!_mbwManager.getSelectedAccount().canSpend()) {
         Toast.makeText(LocalTraderFragment.this.getActivity(), R.string.lt_warning_watch_only_account, Toast.LENGTH_LONG).show();
         return;
      }
      if (!Utils.isAllowedForLocalTrader(_mbwManager.getSelectedAccount())) {
         Toast.makeText(LocalTraderFragment.this.getActivity(), R.string.lt_warning_wrong_account_type, Toast.LENGTH_LONG).show();
         return;
      }

      LocalTraderManager ltManager = _mbwManager.getLocalTraderManager();
      boolean newActivity = ltManager.hasLocalTraderAccount() && ltManager.needsTraderSynchronization();
      LtMainActivity.callMe(getActivity(), newActivity ? LtMainActivity.TAB_TYPE.ACTIVE_TRADES
            : LtMainActivity.TAB_TYPE.DEFAULT);
   }

   private void updateUi() {
      if (!isAdded()) {
         return;
      }
      // Hide/Show Local Trader trade button
      if (_ltManager.isLocalTraderDisabled()) {
         _root.setVisibility(View.GONE);
      } else {
         // HACK: This should be handled in the outer element, where it's known which options exist but in the old wallet, there will be no more options
         if(!_mbwManager.getMetadataStorage().getGlideraIsEnabled()) {
            // if there is no glidera available but LT, yes, skip asking the user.
            localTraderSelected();
            getActivity().finish();
         }
         _root.setVisibility(View.VISIBLE);
         // Local Trader update dot
         boolean showDot = _ltManager.hasLocalTraderAccount() && _ltManager.needsTraderSynchronization();
         _root.findViewById(R.id.ivDot).setVisibility(showDot ? View.VISIBLE : View.GONE);
      }
   }

   private LocalTraderEventSubscriber ltSubscriber = new LocalTraderEventSubscriber(new Handler()) {
      @Override
      public void onLtError(int errorCode) {
         // Ignore
      }

      @Override
      public void onLtTraderActicityNotification(long timestamp) {
         updateUi();
      }
   };
}
