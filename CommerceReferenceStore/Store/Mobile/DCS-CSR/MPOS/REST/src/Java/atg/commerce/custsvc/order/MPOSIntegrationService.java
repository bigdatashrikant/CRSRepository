/*<ORACLECOPYRIGHT>
 * Copyright (C) 1994-2014 Oracle and/or its affiliates. All rights reserved.
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * UNIX is a registered trademark of The Open Group.
 *
 * This software and related documentation are provided under a license agreement 
 * containing restrictions on use and disclosure and are protected by intellectual property laws. 
 * Except as expressly permitted in your license agreement or allowed by law, you may not use, copy, 
 * reproduce, translate, broadcast, modify, license, transmit, distribute, exhibit, perform, publish, 
 * or display any part, in any form, or by any means. Reverse engineering, disassembly, 
 * or decompilation of this software, unless required by law for interoperability, is prohibited.
 *
 * The information contained herein is subject to change without notice and is not warranted to be error-free. 
 * If you find any errors, please report them to us in writing.
 *
 * U.S. GOVERNMENT RIGHTS Programs, software, databases, and related documentation and technical data delivered to U.S. 
 * Government customers are "commercial computer software" or "commercial technical data" pursuant to the applicable 
 * Federal Acquisition Regulation and agency-specific supplemental regulations. 
 * As such, the use, duplication, disclosure, modification, and adaptation shall be subject to the restrictions and 
 * license terms set forth in the applicable Government contract, and, to the extent applicable by the terms of the 
 * Government contract, the additional rights set forth in FAR 52.227-19, Commercial Computer Software License 
 * (December 2007). Oracle America, Inc., 500 Oracle Parkway, Redwood City, CA 94065.
 *
 * This software or hardware is developed for general use in a variety of information management applications. 
 * It is not developed or intended for use in any inherently dangerous applications, including applications that 
 * may create a risk of personal injury. If you use this software or hardware in dangerous applications, 
 * then you shall be responsible to take all appropriate fail-safe, backup, redundancy, 
 * and other measures to ensure its safe use. Oracle Corporation and its affiliates disclaim any liability for any 
 * damages caused by use of this software or hardware in dangerous applications.
 *
 * This software or hardware and documentation may provide access to or information on content, 
 * products, and services from third parties. Oracle Corporation and its affiliates are not responsible for and 
 * expressly disclaim all warranties of any kind with respect to third-party content, products, and services. 
 * Oracle Corporation and its affiliates will not be responsible for any loss, costs, 
 * or damages incurred due to your access to or use of third-party content, products, or services.
 </ORACLECOPYRIGHT>*/

package atg.commerce.custsvc.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import atg.commerce.CommerceException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupManager;
import atg.commerce.order.StoreCredit;
import atg.commerce.states.OrderStates;
import atg.commerce.states.StateDefinitions;
import atg.core.util.StringUtils;
import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.nucleus.GenericService;
import atg.projects.store.mobile.states.MobileCSROrderStates;
import atg.service.lockmanager.ClientLockManager;
import atg.service.lockmanager.DeadlockException;
import atg.service.pipeline.PipelineResult;

/**
* @author ATG
* @version $Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/REST/src/atg/commerce/custsvc/order/MPOSIntegrationService.java#5 $$Change: 891992 $
* @updated $DateTime: 2014/05/30 06:58:41 $$Author: pmacrory $
*/
public class MPOSIntegrationService extends GenericService {
  //-------------------------------------
  /** Class version string */
  public static String CLASS_VERSION = "$Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/REST/src/atg/commerce/custsvc/order/MPOSIntegrationService.java#5 $$Change: 891992 $";

  /* output parameter name for an error message */
  static final String ERROR = "errorMessage";
  /* output parameter name for a successful request */
  static final String SUCCESS = "success";

  /* request parameter names */
  static final String AMOUNT = "amount";
  static final String TYPE = "type";
  static final String SUBTYPE = "subtype";
  static final String CURRENCY_CODE = "currencyCode";

  /* used by synchronizeOnOrder to determine which method to call after synchronized on order */
  enum WebServiceCommand {
    PAID, CANCEL
  };
  
  ClientLockManager mLocalLockManager;
  
  public ClientLockManager getLocalLockManager() {
    return mLocalLockManager;
  }

  public void setLocalLockManager(ClientLockManager pLocalLockManager) {
    mLocalLockManager = pLocalLockManager;
  }
  
  TransactionManager mTransactionManager;

  public TransactionManager getTransactionManager() {
    return mTransactionManager;
  }

  public void setTransactionManager(TransactionManager pTransactionManager) {
    mTransactionManager = pTransactionManager;
  }

  OrderManager mOrderManager;
  
  public OrderManager getOrderManager() {
    return mOrderManager;
  }

  public void setOrderManager(OrderManager pOrderManager) {
    mOrderManager = pOrderManager;
  }

  PaymentGroupManager mPaymentGroupManager;
  
  public PaymentGroupManager getPaymentGroupManager() {
    return mPaymentGroupManager;
  }

  public void setPaymentGroupManager(PaymentGroupManager pPaymentGroupManager) {
    mPaymentGroupManager = pPaymentGroupManager;
  }
  
  Map<String,String> mPaymentTypesMapping;

  /**
   * @return the mapping of payment TYPEs to handled/expected types
   */
  public Map<String,String> getPaymentTypesMapping() {
    if (mPaymentTypesMapping == null) {
      mPaymentTypesMapping = new HashMap<String,String>();
    }
    return mPaymentTypesMapping;
  }

  public void setPaymentTypesMapping(Map<String,String> pPaymentTypesMapping) {
    mPaymentTypesMapping = pPaymentTypesMapping;
  }

  Map<String,String> mPaymentSubtypesMapping;
  
  /**
   * @return the mapping of payment sub-types, e.g. VISA -> Visa
   */
  public Map<String,String> getPaymentSubtypesMapping() {
    if (mPaymentSubtypesMapping == null) {
      mPaymentSubtypesMapping = new HashMap<String,String>();
    }
    return mPaymentSubtypesMapping;
  }

  public void setPaymentSubtypesMapping(Map<String,String> pPaymentSubtypesMapping) {
    mPaymentSubtypesMapping = pPaymentSubtypesMapping;
  }
  
  
  /* ------------------------------- */
  /* paid web service call methods   */  
  /* ------------------------------- */

  /**
   * The 'paid' web service invoked by the actor--executed by MPOS when an order 
   * (possibly suspended) has been fully paid for. Create and adds payments to the order,
   * and submits it for processing.
   * 
   * @param pOrderId
   * @param pPayments
   * @return
   */
  public Map<String, ?> paid(String pOrderId, List<?> pPayments) {
    try {
      vlogDebug("paid called for orderId {0}", pOrderId);
      
      if (pPayments == null || pPayments.size() < 1) {
        return createErrorMap("'payments' required");
      }
      if (StringUtils.isBlank(pOrderId)) {
        return createErrorMap("'orderId' required");
      }
      
      verifyPayments(pPayments);
      
      OrderImpl order = (OrderImpl) getOrderManager().loadOrder(pOrderId);
      
      verifyOrder(order);

      synchronizeOnOrder(order, WebServiceCommand.PAID, pPayments);
     
      
    } catch (Exception e) {
      if (isLoggingError())
        logError(e);
      return createErrorMap(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }
    return createSuccessMap();
  }
  
  /**
   * Executed after synchronized on order. Payments are added to the order, the order is updated
   * and submitted for processing.
   * 
   * @param pOrder
   * @param pPayments
   * @throws CommerceException
   */
  void paidSynchronized(Order pOrder, List<?> pPayments) throws CommerceException {
    addPaymentsToOrder(pOrder,pPayments);

    getOrderManager().updateOrder(pOrder);

    PipelineResult pipelineResult = getOrderManager().processOrder(pOrder);
    
    if (pipelineResult.hasErrors()) {
      throw new RuntimeException(pipelineResult.getErrors()[0].toString());
    }
  }

  /**
   * Verifies the order is in the correct state. It must be priced, INCOMPLETE or SUSPENDED,
   * and have no payment groups--existing payment groups are removed here.
   * @param pOrder
   * @return
   * @throws CommerceException
   */
  void verifyOrder(Order pOrder) throws CommerceException {
    OrderImpl order = (OrderImpl)pOrder;
   // make sure the order has been priced
    if (order.getPriceInfo() == null) {
      throw new CommerceException("Order has not been priced");
    }
    
    // validate order state
    if (!OrderStates.INCOMPLETE.toLowerCase().equals(order.getStateAsString().toLowerCase()) && 
        !MobileCSROrderStates.SUSPENDED.toLowerCase().equals(order.getStateAsString().toLowerCase())) {
      throw new CommerceException("Invalid order state");
    }
    
    // check for existing payment groups
    if (order.getPaymentGroupCount() > 0) {
      List<?> paymentGroups = order.getPaymentGroups();
      List<PaymentGroup> paymentGroupExceptions = new ArrayList<>();
      // find any store credit payment groups
      //
      for (Object obj : paymentGroups) {
        PaymentGroup pg = (PaymentGroup) obj;
        if (pg instanceof StoreCredit)
          paymentGroupExceptions.add(pg);
      }

      // if we have payment groups on the order that are not Store Credit payment groups.
      //
      getPaymentGroupManager().removeAllPaymentGroupsFromOrder(order, paymentGroupExceptions);
    }
  }
  
  /**
   * Verify that the payments list is in the expected format. Must contain AMOUNT,
   * TYPE, and CURRENCY_CODE.
   * 
   * @param pPayments
   * @return
   */
  void verifyPayments(List<?> pPayments) throws CommerceException {
    for (Object obj : pPayments) {
      if (!(obj instanceof Map)) throw new CommerceException("unexpected format for 'payments'");
      Map payment = (Map)obj;
      String amountString = payment.get(AMOUNT).toString();
      if (StringUtils.isBlank(amountString)) {
        throw new CommerceException("payment requires an amount");
      }
      String type = payment.get(TYPE).toString();
      if (StringUtils.isBlank(type)) {
        throw new CommerceException("payment requires a type");
      }
      String currencyCode = payment.get(CURRENCY_CODE).toString();
      if (StringUtils.isBlank(currencyCode)) {
        throw new CommerceException("payment requires a currencyCode");
      }
    }
  }
  
  /**
   * Creates payment groups from the PAYMENTS parameter. The payment group type is determined by
   * looking up the given TYPE in the paymentTypesMapping property. The payment groups are added
   * to the order, along with a payment group relationship for the given amount.
   * 
   * @param pOrder
   * @param pPayments
   * @throws CommerceException
   */
  void addPaymentsToOrder(Order pOrder, List<?> pPayments) throws CommerceException {
    for (Object obj : pPayments) {
      Map payment = (Map)obj;
      String type = payment.get(TYPE).toString();
      if (!StringUtils.isBlank(getPaymentTypesMapping().get(type))) {
        type = getPaymentTypesMapping().get(type);
      }
      if (type == null) type = ""; // create default payment group
      
      PaymentGroup paymentGroup = null;
      switch (type) {
        case "creditCard":
        case "tokenizedCreditCard":
          paymentGroup = getPaymentGroupManager().createPaymentGroup("tokenizedCreditCard");
          String subType = (String) payment.get(SUBTYPE);
          if (StringUtils.isNotBlank(subType)) {
            ((CreditCard) paymentGroup).setCreditCardType(subType);
          }
          break;
        case "cash":
          paymentGroup = getPaymentGroupManager().createPaymentGroup("cash");
          break;
        case "storeCredit":
          // we use the existing ATG store credits payment group, so we don't need to do anything here.
          continue;
        default:
          paymentGroup = getPaymentGroupManager().createPaymentGroup("default");
          break;
      }
      // already checked that AMOUNT and CURRENCY_CODE are set in verifyPayments
      String amountString = payment.get(AMOUNT).toString();
      double amount = Double.parseDouble(amountString);
      paymentGroup.setAmount(amount);

      String currencyCode = payment.get(CURRENCY_CODE).toString();
      paymentGroup.setCurrencyCode(currencyCode);

      getPaymentGroupManager().addPaymentGroupToOrder(pOrder, paymentGroup);
      getOrderManager().addOrderAmountToPaymentGroup(pOrder, paymentGroup.getId(), amount);        
    }
  }
  
  /* ------------------------------- */
  /* cancel web service call methods   */  
  /* ------------------------------- */
  
  /**
   * Cancel order call. Executed by MPOS when a transaction is cancelled, or not paid for by
   * the end of the day. Removes payment groups from order and sets state to INCOMPLETE.
   * 
   * @param pOrderId
   */
  public Map<String,Object> cancel(String pOrderId) {
    try {
      vlogDebug("cancel order {0}", pOrderId);
      
      if (StringUtils.isBlank(pOrderId)) {
        return createErrorMap("orderId must not be blank");
      }
      
      OrderImpl order = (OrderImpl) getOrderManager().loadOrder(pOrderId);
      synchronizeOnOrder(order, WebServiceCommand.CANCEL, null);
      
    } catch (Exception e) {
      return createErrorMap(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    
    return createSuccessMap();
  }
  
  /**
   * Executed after synchronizing on the order. Removes payment groups and sets order
   * back to INCOMPLETE.
   * 
   * @param pOrder
   */
  void cancelSynchronized(Order pOrder) throws CommerceException {
    getPaymentGroupManager().removeAllPaymentGroupsFromOrder(pOrder);
    pOrder.setState(StateDefinitions.ORDERSTATES.getStateFromString(OrderStates.INCOMPLETE));
  }
  
  /* ------------------------------- */
  
  /**
   * Creates a map containing an error message for error response
   * @param pMessage error message
   * @return map of ERROR->pMessage
   */
  Map<String,Object> createErrorMap(String pMessage) {
    // if the message is null, default to a generic error message.
    if (pMessage == null)
      pMessage = "unknown";

    vlogDebug("error: {0}", pMessage);
    Map<String, Object> errorMap = new HashMap<String, Object>();
    errorMap.put(ERROR, pMessage);
    return errorMap;
  }
  
  /**
   * Creates a map for success response.
   * @return SUCCESS->true
   */
  Map<String,Object> createSuccessMap() {
    vlogDebug("success");
    Map<String, Object> successMap = new HashMap<String, Object>();
    successMap.put(SUCCESS, true);
    return successMap; 
  }
  
  /**
   * Follows order modification pattern of acquiring write lock on the profileId, ensuring transaction,
   * and synchronizing on the order. Then it will execute the paidSynchronized or cancelSynchronized method.
   * 
   * @param pOrder order to lock/synchronize on
   * @param pWebServiceCommand enum of which method to execute after synchronizing
   * @param pParams parameters to pass to the method determined by pWebServiceCommand
   */
  void synchronizeOnOrder(Order pOrder, WebServiceCommand pWebServiceCommand, Object pParams) {
    ClientLockManager lockManager = getLocalLockManager();
    TransactionManager transactionManager = getTransactionManager();
    String profileId = pOrder.getProfileId();
    boolean acquireLock = false;
    try
    {
      acquireLock = !lockManager.hasWriteLock(profileId,Thread.currentThread());
      if (acquireLock) {
        lockManager.acquireWriteLock(profileId,Thread.currentThread());
      }
      TransactionDemarcation td = new TransactionDemarcation();
      td.begin(transactionManager);
      boolean shouldRollback = false;
      try {
        synchronized(pOrder) {
          switch(pWebServiceCommand) {
            case PAID:
              paidSynchronized(pOrder, (List<?>) pParams);
              break;
            case CANCEL:
              cancelSynchronized(pOrder);
              break;
          }
        }
      } catch (Exception e) {
        shouldRollback = true;
        throw new RuntimeException(e);
      } finally {
        try {
          td.end(shouldRollback);
        } catch (TransactionDemarcationException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (DeadlockException e) {
      throw new RuntimeException(e);
    } catch (TransactionDemarcationException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (acquireLock) {
          lockManager.releaseWriteLock(profileId, Thread.currentThread(), true);
        }
      } catch(Throwable th) {
        logError(th);
      }
    }
  }
}
