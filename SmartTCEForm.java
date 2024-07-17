package com.my.forms;

import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.util.MessageBox;
import java.io.PrintStream;
import java.util.Date;

public class SmartTCEForm {
   TCComponentForm form;
  
   public SmartTCEForm(TCComponentForm theForm) {
      System.out.println("SmartTCEForm");
      this.form = theForm;
   }
  
   public Object getFormPropertyObj(String sName) throws TCException {
      TCProperty prop = this.form.getTCProperty(sName);	
      if (prop == null) {
         String s = "Не найден атрибут формы " + this.form + ": '" + sName + "'";
         throw new TCException(s);
      }		
      TCPropertyDescriptor propDesc = prop.getDescriptor();
      switch (propDesc.getType()) {
         case 8: 
            return prop.getStringValue();
         case 2: 
            return prop.getDateValue();
         case 3: 
            return new Double(prop.getDoubleValue());
         case 4: 
            return new Float(prop.getFloatValue());
         case 6: 
            return new Boolean(prop.getLogicalValue());
      }
      return null;
   }
  
   public void setFormPropertySafe(String sName, Object Value) throws TCException {
      TCProperty prop = this.form.getFormTCProperty(sName);
      if (prop == null)
         throw new TCException("Не найден атрибут формы " + this.form + ": '" + sName + "'");
      TCPropertyDescriptor propDesc = prop.getDescriptor();	
      switch (propDesc.getType()) {
         case 8: 
            int len = propDesc.getMaxStringLength();
            String sValue = (String)Value;
            if (sValue.length() > len) {
               MessageBox.post("Значение '" + sValue + "' свойства '" +
                  sName + "' превышает допустимую длину поля = " + len + 
                  "\nСтрока урезана до допустимой длины!", 
                  "Сохранение формы " + this.form.toString(), 4);
               sValue = sValue.substring(0, len);
            }
            prop.setStringValue(sValue);
            break;
         case 2: 
            prop.setDateValue((Date)Value);
            break;
         case 3: 
            prop.setDoubleValue(((Double)Value).doubleValue());
            break;
         case 4: 
            prop.setFloatValue(((Double)Value).floatValue());
      }
   }
  
   public void setFormPropertySafe(String sName, boolean Value) throws TCException {
      this.form.getFormTCProperty(sName).setLogicalValue(Value);
   }
}