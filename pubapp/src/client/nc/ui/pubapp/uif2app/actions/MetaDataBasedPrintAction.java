package nc.ui.pubapp.uif2app.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.itf.bd.printcheck.IPrintLog;
import nc.ui.bd.print.printlog.Printlistenner;
import nc.ui.ic.general.action.GeneralRefreshCardAction;
import nc.ui.pub.print.IMetaDataDataSource;
import nc.ui.uif2.model.AbstractAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.trade.checkrule.VOChecker;
import nc.vo.uif2.LoginContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * NCpubapp�㣬ԭMetaDataBasedPrintAction�ļ̳��ࣻ ֧�ִ�ӡ������鹦��
 * 
 * @author wushzh
 * 
 */
public class MetaDataBasedPrintAction extends BaseMetaDataBasedPrintAction {

  private nc.ui.pub.print.PrintEntry printEntry;

  public interface IBeforePrintDataProcess extends
      BaseMetaDataBasedPrintAction.IBeforePrintDataProcess {
    Object[] processData(Object[] datas);
  }

  public interface IDataSplit extends BaseMetaDataBasedPrintAction.IDataSplit {
    Object[] splitData(Object[] datas);
  }

  @Override
  public BaseMetaDataBasedPrintAction.IBeforePrintDataProcess getBeforePrintDataProcess() {
    if (beforePrintDataProcess == null) {
      return super.getBeforePrintDataProcess();
    }
    return beforePrintDataProcess;
  }

  public void setBeforePrintDataProcess(
      MetaDataBasedPrintAction.IBeforePrintDataProcess beforeProcessor) {
    if (beforeProcessor instanceof BaseMetaDataBasedPrintAction.IBeforePrintDataProcess) {
      super.setBeforePrintDataProcess(beforeProcessor);
    } else {
      this.beforePrintDataProcess =
          (MetaDataBasedPrintAction.IBeforePrintDataProcess) beforeProcessor;
    }
  }

  @Override
  public BaseMetaDataBasedPrintAction.IDataSplit getDataSplit() {
    if (this.dataSplit == null) {
      return super.dataSplit;
    }
    return dataSplit;
  }


  public void setDataSplit(MetaDataBasedPrintAction.IDataSplit dataSplit) {
    if (dataSplit instanceof BaseMetaDataBasedPrintAction.IDataSplit) {
      super.setDataSplit(dataSplit);
    } else {
      this.dataSplit = (MetaDataBasedPrintAction.IDataSplit) dataSplit;
    }

  }

  protected nc.ui.pub.print.PrintEntry getPrintEntry() {
    // if (null == this.printEntry) {
    if (this.getParent() == null) {
      // ����ΰ��ʾ����applet��Ϊ�����壬��������򿪺�Ԥ��ʱ��������
      // this.setParent(WorkbenchEnvironment.getClientApplet());
      this.setParent(this.getModel().getContext().getEntranceUI());
    }
    this.printEntry = new nc.ui.pub.print.PrintEntry(this.getParent(), null);
    LoginContext ctx = this.getModel().getContext();
    this.printEntry.setTemplateID(ctx.getPk_group(), ctx.getNodeCode(), ctx.getPk_loginUser(),
        null, this.getNodeKey());
    // }

    if (getPrintListener() != null) {
      this.printEntry.setPrintListener(getPrintListener());
    }
    return this.printEntry;

  }

  @Override
  public void doAction(ActionEvent e) throws Exception {
	// add by yanggwd at 2018-06-25
	Object voObj = null;
	
    if (this.getPrintEntry().selectTemplate() != 1) {
      return;
    }
    List<IMetaDataDataSource> list = new ArrayList<IMetaDataDataSource>();
    if (this.getDataSource() != null) {
      Object obj = this.getDataSource().getMDObjects();
      // add by yanggwd at 2018-06-25
      voObj = obj;
      
      checkDataPermission(obj);// Ȩ��У��
      this.printEntry.setDataSource(this.getDataSource());
      this.printEntry.setAdjustable(isAdjustable());
      list.add(this.getDataSource());
    } else {
      Object obj = getDatas();
      // add by yanggwd at 2018-06-25
      voObj = obj;
      
      checkDataPermission(obj);// Ȩ��У��
      IMetaDataDataSource[] defaultDataSource = this.getDefaultMetaDataSource();
      if (!VOChecker.isEmpty(defaultDataSource)) {
        for (IMetaDataDataSource dataSourceItem : defaultDataSource) {
          this.printEntry.setDataSource(dataSourceItem);
          this.printEntry.setAdjustable(isAdjustable());
          list.add(dataSourceItem);
        }
      } else {
        return;
      }

    }
    // ����Ĭ�ϴ�ӡ������wushzh
    setDefaultPrintListener(list.toArray(new IMetaDataDataSource[0]));
    if (this.isPreview()) {
      this.printEntry.preview();
    } else {
      this.printEntry.print();
    }
    
    // �ɹ���ⵥ��ӡ��Ϻ�ˢ�µ��ݽ�����Ϣ add by yanggw at 2018-06-25 start
    if(voObj != null && ((Object[])voObj).length > 0 && ((Object[])voObj)[0] instanceof nc.vo.ic.m45.entity.PurchaseInVO){
    	GeneralRefreshCardAction action = new GeneralRefreshCardAction();
    	action.setModel((AbstractAppModel) getModel());
    	action.actionPerformed(e);
    }
    // �ɹ���ⵥ��ӡ��Ϻ�ˢ�µ��ݽ�����Ϣ add by yanggw at 2018-06-25 end
  }

  /**
   * ����ӡ��ť�ڣ�û��ע���ӡ������������Ĭ�ϵĴ�ӡ������
   * 
   * @throws BusinessException
   */
  protected void setDefaultPrintListener(IMetaDataDataSource[] list) throws BusinessException {
    // ��ȡ����Դ��������ԴΪ�գ���ô����Ҫ����Ĭ�ϵĴ�ӡ��������
    if (ArrayUtils.isEmpty(list))
      return;
    // ��û��Ĭ�ϵļ����࣬����Ҫ����ӡ��������ô����һ��Ĭ�ϵļ�����
    // String templatid = this.printEntry.getTemplateID();
    String funnode = getModel().getContext().getNodeCode();
    if (StringUtils.isEmpty(funnode))
      return;
    if (getPrintListener() == null && isPrintLimit(funnode))
      getDefaultPrintListener();
    // ����Ĭ�ϵļ����࣬��Ҫ������������Դ
    if (this.getPrintListener() != null && this.getPrintListener() instanceof Printlistenner) {
      ((Printlistenner) getPrintListener()).setDatasource(list);
      ((Printlistenner) getPrintListener()).setTemplatid(null);
      ((Printlistenner) getPrintListener()).setFuncode(funnode);
    }
  }

  private boolean isPrintLimit(String funnode) throws BusinessException {
    return NCLocator.getInstance().lookup(IPrintLog.class).isAddPrintListenerByTemplatid(funnode);
  }

  /**
   * ��ȡĬ�ϵļ����࣬��ע�뵽printEntry��
   */
  private void getDefaultPrintListener() {
    this.setPrintListener(new Printlistenner());
    this.printEntry.setPrintListener(getPrintListener());

  }
}
