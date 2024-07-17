package com.my.forms;

import com.teamcenter.rac.stylesheet.*;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Date;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.commands.newitem.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.util.Registry;

public class MyItemRevisionForm extends AbstractRendering {

   TCComponentForm form = null;
   SmartTCEForm sform = null;    
   
   TCComponentForm rev_form = null;
   SmartTCEForm sform_rev = null;
   
   TCSession session;
   InterfaceFormPanel innerPanel;
   DSEUserData data;
   
   // Класс используется для сохранения свойств формы в класс хранения
   class SaveOperation extends AbstractAIFOperation {
      protected void doSave(TCComponentForm form1, SmartTCEForm sform1) throws Exception {
         form1.lock();
         sform1.setFormPropertySafe("HR100", data.diametr);
         sform1.setFormPropertySafe(NR.fld_DSE_CODE, data.code_dse);
         sform1.setFormPropertySafe(NR.fld_FORMAT, data.format);
         sform1.setFormPropertySafe(NR.fld_MASSA, data.massa);
         // ... заполняются поля в соответствии с NameResolver
         sform1.setFormPropertySafe(NR.fld_MARKA_MAT, data.mater_mark);
         sform1.setFormPropertySafe(NR.fld_VID_ZAGOT, data.mater_zagot);
         form1.save();
         form1.unlock();
      }

      public void executeOperation() throws Exception {
         innerPanel.saveToUserData();
         if (sform_rev == null) {
            // открывается для просмотра существующая уже ревизия
            doSave(form, sform);
         } else {
            // создается новая ревизия для нового объекта
            doSave(rev_form, sform_rev);
         }  	
   }
   
   // Конструктор
   public MyItemRevisionForm(TCComponentForm theForm) throws Exception {
      super(theForm);
      form = theForm; 
      session = (TCSession) form.getSession();
      loadRendering();
   }
   
   public void saveRendering() {
      
      // если форма используется для просмотра существующей ревизии то data.item_rev будет заполнена в loadRendering
      // если data.item_rev=null то значит форма либо используется в мастере создания, либо при создании нового Изделия,
      // либо при пересмотре существующего (создании новой Модификации)
      if (data.item_rev == null) {
         // проходим вверх по элементам ГПИ чтобы получить item
         JPanel parentPanel = (JPanel) ((JPanel) this).getParent();
         JViewport enclosing = (JViewport) parentPanel.getParent();
         JScrollPane scrolpan1 = (JScrollPane) enclosing.getParent();
         ItemRevMasterFormPanel irmfp1 = (ItemRevMasterFormPanel) scrolpan1.getParent();
         NewItemPanel itemPanel1 = (NewItemPanel) irmfp1.controlWizardPanel;
         data.item = (TCComponentItem) itemPanel1.newComponent; // получаем item
         // если null, то значит создается новая модификация (пересмотр), берем форму из текущего компонента
         if (data.item == null) {
            rev_form = (TCComponentForm) this.component;
            sform_rev = new SmartTCEForm(rev_form);			
         } else {
            try {
               data.item_rev = data.item.getLatestItemRevision();
            } catch (Exception e) {
               // TODO: Можно тут описать обработчик
            }
            if (data.item_rev != null) {
               try {
                  rev_form = (TCComponentForm) data.item_rev.getRelatedComponent("IMAN_master_form_rev");
                  sform_rev = new SmartTCEForm(rev_form);
               } catch (TCException e) {
                  e.printStackTrace();
               }
            } //if
         } //else
      } //if
      
      SaveOperation operation = new SaveOperation(); // класс SaveOperation описан выше
      try {
         session.queueOperation(operation); // не ждет конца операции, чтобы не было фризов
      } catch (Exception ex) {
         MessageBox.post(ex);
      }     
   } //saveRendering
   
}   