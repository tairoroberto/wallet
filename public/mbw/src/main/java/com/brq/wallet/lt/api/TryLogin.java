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

package com.brq.wallet.lt.api;

import com.mrd.bitlib.crypto.InMemoryPrivateKey;
import com.mrd.bitlib.model.Address;
import com.mrd.bitlib.model.NetworkParameters;
import com.mycelium.lt.ApiUtils;
import com.mycelium.lt.api.LtApi;
import com.mycelium.lt.api.LtApiException;
import com.mycelium.lt.api.params.LoginParameters;
import com.brq.wallet.lt.LocalTraderEventSubscriber;
import com.brq.wallet.lt.LocalTraderManager.LocalManagerApiContext;

import java.util.Collection;
import java.util.UUID;

public class TryLogin extends Request {
   private static final long serialVersionUID = 1L;

   private InMemoryPrivateKey _privateKey;
   private NetworkParameters _network;

   public TryLogin(InMemoryPrivateKey privateKey, NetworkParameters network) {
      super(true, false);
      _privateKey = privateKey;
      _network = network;
   }

   @Override
   public void execute(LocalManagerApiContext context, LtApi api, UUID sessionId,
         Collection<LocalTraderEventSubscriber> subscribers) {

      // Sign session ID with private key
      String signedMessage = ApiUtils.generateUuidHashSignature(_privateKey, sessionId);
      try {

         // Call function
         Address address = _privateKey.getPublicKey().toAddress(_network);
         LoginParameters params = new LoginParameters(address, signedMessage);
         final String nickname = api.traderLogin(sessionId, params).getResult();

         // Notify Login success
         synchronized (subscribers) {
            for (final LocalTraderEventSubscriber s : subscribers) {
               s.getHandler().post(new Runnable() {

                  @Override
                  public void run() {
                     s.onLtLogin(nickname, TryLogin.this);
                  }
               });
            }
         }

      } catch (LtApiException e) {
         // Handle errors
         if (e.errorCode == LtApi.ERROR_CODE_TRADER_DOES_NOT_EXIST) {
            // Notify Login failure
            synchronized (subscribers) {
               for (final LocalTraderEventSubscriber s : subscribers) {
                  s.getHandler().post(new Runnable() {

                     @Override
                     public void run() {
                        s.onLtNoTraderAccount();
                     }
                  });
               }
            }
         } else {
            // Handle other errors
            context.handleErrors(this, e.errorCode);
         }
      }

   }
}