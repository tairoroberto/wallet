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

package com.brq.wallet.lt.activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.brq.wallet.R;

public class TraderInfoAdapter extends ArrayAdapter<TraderInfoAdapter.InfoItem> {
   
   public static class InfoItem {
      final String label;
      final String value;
      final Float rating;

      public InfoItem(String label, String value) {
         this.label = label;
         this.value = value;
         this.rating = null;
      }

      public InfoItem(String label, Float rating) {
         this.label = label;
         this.value = null;
         this.rating = rating;
      }
   }
   
   private Context _context;

   
   public TraderInfoAdapter(Context context, List<InfoItem> objects) {
      super(context, R.layout.lt_trader_info_row, objects);
      _context = context;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      InfoItem o = getItem(position);
      LayoutInflater vi = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (o.value != null) {
         // Set String value
         v = Preconditions.checkNotNull(vi.inflate(R.layout.lt_trader_info_row, null));
         ((TextView) v.findViewById(R.id.tvDisplayValue)).setText(o.value);
      } else if (o.rating != null) {
         // Set Rating
         v = Preconditions.checkNotNull(vi.inflate(R.layout.lt_trader_info_rating_row, null));
         RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
         ratingBar.setRating(o.rating);
      } else {
         throw new RuntimeException("Invalid TraderInfoItem: null value or rating");
      }
      // Set label
      ((TextView) v.findViewById(R.id.tvLabel)).setText(o.label);
      return v;
   }
}
