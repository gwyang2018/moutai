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
 * NCpubapp层，原MetaDataBasedPrintAction的继承类； 支持打印次数检查功能
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
      // 刘晨伟提示，用applet作为父窗体，对于联查打开后预览时会有问题
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
      
      checkDataPermission(obj);// 权限校验
      this.printEntry.setDataSource(this.getDataSource());
      this.printEntry.setAdjustable(isAdjustable());
      list.add(this.getDataSource());
    } else {
      Object obj = getDatas();
      // add by yanggwd at 2018-06-25
      voObj = obj;
      
      checkDataPermission(obj);// 权限校验
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
    // 设置默认打印监听，wushzh
    setDefaultPrintListener(list.toArray(new IMetaDataDataSource[0]));
    if (this.isPreview()) {
      this.printEntry.preview();
    } else {
      this.printEntry.print();
    }
    
    // 采购入库单打印完毕后，刷新单据界面信息 add by yanggw at 2018-06-25 start
    if(voObj != null && ((Object[])voObj).length > 0 && ((Object[])voObj)[0] instanceof nc.vo.ic.m45.entity.PurchaseInVO){
    	GeneralRefreshCardAction action = new GeneralRefreshCardAction();
    	action.setModel((AbstractAppModel) getModel());
    	action.actionPerformed(e);
    }
    // 采购入库单打印完毕后，刷新单据界面信息 add by yanggw at 2018-06-25 end
  }

  /**
   * 若打印按钮内，没有注册打印监听器，设置默认的打印监听器
   * 
   * @throws BusinessException
   */
  protected void setDefaultPrintListener(IMetaDataDataSource[] list) throws BusinessException {
    // 获取数据源，若数据源为空，那么不需要设置默认的打印监听类了
    if (ArrayUtils.isEmpty(list))
      return;
    // 若没有默认的监听类，且需要检查打印次数，那么设置一个默认的监听类
    // String templatid = this.printEntry.getTemplateID();
    String funnode = getModel().getContext().getNodeCode();
    if (StringUtils.isEmpty(funnode))
      return;
    if (getPrintListener() == null && isPrintLimit(funnode))
      getDefaultPrintListener();
    // 若是默认的监听类，需要重新设置数据源
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
   * 获取默认的监听类，并注入到printEntry内
   */
  private void getDefaultPrintListener() {
    this.setPrintListener(new Printlistenner());
    this.printEntry.setPrintListener(getPrintListener());

  }
}
