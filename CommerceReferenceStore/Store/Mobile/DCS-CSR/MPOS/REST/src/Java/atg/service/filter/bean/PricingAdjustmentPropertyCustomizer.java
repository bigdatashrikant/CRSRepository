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

package atg.service.filter.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.commerce.csr.util.CSRAgentTools;
import atg.commerce.pricing.priceLists.Constants;
import atg.commerce.pricing.PricingAdjustment;
import atg.core.i18n.LayeredResourceBundle;
import atg.core.util.StringUtils;

/**
 * 
 * Bean filter property customizer for outputting the adjustments property of atg.commerce.pricing.AmountInfo,
 * which is a list of atg.commerce.pricing.PricingAdjustment. It allows certain adjustments to be filtered out. 
 * 
 * Optional attribute parameters default to true:
 * 
 * includeListPrice - include the List price adjustment
 * includeSalePrice - include the Sale price adjustment
 * includeManualAdjustments - include manual price adjustments
 * includeEmptyDescription - include adjustments with an empty description
 * includeFirstAdjustment - include the first adjustment in the list, which is usually the base price
 * 
 * @author ATG
 * @version $Change: 889821 $$DateTime: 2014/05/19 07:34:51 $$Author: kmutyala $
 *
 */
public class PricingAdjustmentPropertyCustomizer implements PropertyCustomizer {
  /** Class version string */
  public static String CLASS_VERSION = "$Id: //hosting-blueprint/B2CBlueprint/version/11.1/Mobile/DCS-CSR/MPOS/REST/src/atg/service/filter/bean/PricingAdjustmentPropertyCustomizer.java#2 $$Change: 889821 $";
  public static final long serialVersionUID = CLASS_VERSION.hashCode();
  
  private static final String ERROR_EXPECTED_LIST = "atg.service.filter.bean.RemoveNullsFromListPropertyCustomizer.errorExpectedList";
  
  public static final String ATTR_INCLUDE_LIST_PRICE = "includeListPrice";
  public static final String ATTR_INCLUDE_SALE_PRICE = "includeSalePrice";
  public static final String ATTR_INCLUDE_MANUAL_ADJUSTMENTS = "includeManualAdjustments";
  public static final String ATTR_INCLUDE_EMPTY_DESCRIPTION = "includeEmptyDescription";
  public static final String ATTR_INCLUDE_FIRST_ADJUSTMENT = "includeFirstAdjustment";
  
  @Override
  public Object getPropertyValue(Object pTargetObject, String pPropertyName,
      Map<String, String> pAttributes) throws BeanFilterException {
    
    Object propertyVal;
    try {
      propertyVal = DynamicBeans.getSubPropertyValue(pTargetObject, pPropertyName);
    } catch (PropertyNotFoundException e) {
      throw new RuntimeException(e);
    }
    
    if (propertyVal == null) {
      return null;
    }
    
    if (!(propertyVal instanceof List)) {
      String msg = Messages.getString(ERROR_EXPECTED_LIST,new Object[]{propertyVal.getClass().getName()});
      throw new IllegalArgumentException(msg);
    }
    
    List<?> listValue = (List<?>) propertyVal;
    List<Object> output = new ArrayList<Object>(listValue.size());
    
    boolean includeListPrice = true;
    boolean includeSalePrice = true;
    boolean includeManualAdjustments = true;
    boolean includeEmptyDescription = true;
    boolean includeFirstAdjustment = true;
    
    if (pAttributes != null) {
      String value = null;
      if ((value = pAttributes.get(ATTR_INCLUDE_LIST_PRICE)) != null) includeListPrice = new Boolean(value);
      if ((value = pAttributes.get(ATTR_INCLUDE_SALE_PRICE)) != null) includeSalePrice = new Boolean(value);
      if ((value = pAttributes.get(ATTR_INCLUDE_MANUAL_ADJUSTMENTS)) != null) includeManualAdjustments = new Boolean(value);
      if ((value = pAttributes.get(ATTR_INCLUDE_EMPTY_DESCRIPTION)) != null) includeEmptyDescription = new Boolean(value);
      if ((value = pAttributes.get(ATTR_INCLUDE_FIRST_ADJUSTMENT)) != null) includeFirstAdjustment = new Boolean(value);
    }
    
    String manualAdjustmentDescription = null;
    ResourceBundle agentResources = LayeredResourceBundle.getBundle(
        CSRAgentTools.CSR_ORDER_RESOURCE_NAME, atg.service.dynamo.LangLicense.getLicensedDefault());
    if (agentResources != null) manualAdjustmentDescription = agentResources.getString("csrPriceAdjustmentDescription");
    
    for(int i = 0; i < listValue.size(); i++) {
      if (!includeFirstAdjustment && i == 0) continue;
      Object o = listValue.get(i);
      if (o != null && o instanceof PricingAdjustment) {
        PricingAdjustment adjustment = (PricingAdjustment)o; 
        if (StringUtils.isEmpty(adjustment.getAdjustmentDescription())) {
          if (includeEmptyDescription) output.add(adjustment);
          continue;
        }
        if (!includeListPrice && 
            adjustment.getAdjustmentDescription().equals(Constants.LIST_PRICE_ADJUSTMENT_DESCRIPTION)) {
          continue;
        } 
        if (!includeSalePrice && 
            adjustment.getAdjustmentDescription().equals(Constants.SALE_PRICE_ADJUSTMENT_DESCRIPTION)) {
          continue;
        }
        if (!includeManualAdjustments && 
            adjustment.getAdjustmentDescription().equals(manualAdjustmentDescription)) {
          continue; 
        }
        output.add(adjustment);
      }
    }
    
    return output;
  }

}
