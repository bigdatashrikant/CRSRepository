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
 * </ORACLECOPYRIGHT>
 */
//TODO move to package atg.commerce.order
package atg.commerce.order;


import atg.commerce.order.CreditCard;
import atg.commerce.order.RepositoryContactInfo;
import atg.core.util.Address;

/**
 * Credit card implementation to store token generated by Payment Gateway to link credit card
 * details entered by user.
 * 
 * @author Santosh Gokak
 * @version $$Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/src/atg/commerce/order/TokenizedCreditCard.java#2 $$$$Change: 889821 $$
 * @updated $$DateTime: 2014/05/19 07:34:51 $$$$Author: kmutyala $$
 * 
 */

public class TokenizedCreditCard extends CreditCard {

  //-------------------------------------
  /** Class version string */

  public static String CLASS_VERSION = "$Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/src/atg/commerce/order/TokenizedCreditCard.java#2 $$Change: 889821 $";

  //-------------------------------------
  /**
   * Class version string
   */

  
  //----------------------------------------
  // Constants
  //----------------------------------------

  //----------------------------------------
  // Member Variables
  //----------------------------------------

  //----------------------------------------
  // Properties
  //----------------------------------------
  //Payment processor provided unique token.
  private String mToken;

  /**
   * @return Payment processor provided unique token.
   */
  public String getToken() {
    return mToken;
  }

  public void setToken(final String pToken) {
    mToken = pToken;
  }


  /** Get the phone number associated with the credit card. Note
   * that this can also be accessed via getBillingAddress().getPhoneNumber(),
   * but since the rest of billing address is unused for TokenizedCreditCard
   * makes sense to expose it as a direct property.
   *
   * @return the phone number, or null.
   */
  public String getPhoneNumber() {
    Address address = getBillingAddress();
    if (address instanceof RepositoryContactInfo) {
      return ((RepositoryContactInfo)address).getPhoneNumber();
    }
    return null;
  }

  //----------------------------------------
  // Constructors
  //----------------------------------------

  //----------------------------------------

  //----------------------------------------
  // Object Methods 
  //----------------------------------------

  //----------------------------------------
  // Static Methods 
  //----------------------------------------

  //-------------------------------------
  // java.lang.Object method overrides
  //-------------------------------------

  /**
   * @return a String representation of this object
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClass().getName());
    buf.append('[');
    buf.append("token=");
    buf.append(getToken());
    buf.append(']');
    return buf.toString();
  }

  //-------------------------------------
}
