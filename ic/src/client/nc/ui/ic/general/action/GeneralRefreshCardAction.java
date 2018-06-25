package nc.ui.ic.general.action;

import java.awt.event.ActionEvent;

import nc.bs.framework.common.NCLocator;
import nc.bs.ic.pub.util.BillQueryUtils;
import nc.itf.pubapp.pub.smart.IBillQueryService;
import nc.ui.ml.NCLangRes;
import nc.ui.pubapp.uif2app.actions.RefreshSingleAction;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.pub.Constructor;

/**
 * <p>
 * <b>��Ƭ�µ�ˢ�°�ť��</b>
 * <ul>
 * <li>
 * </ul>
 * <p>
 * <p>
 * 
 * @version 6.0
 * @since 6.0
 * @author yangb
 * @time 2010-9-2 ����02:45:28
 */
public class GeneralRefreshCardAction extends RefreshSingleAction {

  private static final long serialVersionUID = 2010090214460001L;

  /**
   * RefreshCardAction �Ĺ�����
   */
  public GeneralRefreshCardAction() {
    super();
  }
  @Override
  public void doAction(ActionEvent e) throws Exception {
    Object obj = this.model.getSelectedData();
    if (obj != null) {
      AbstractBill oldVO = (AbstractBill) obj;
      String pk = oldVO.getParentVO().getPrimaryKey();
      IBillQueryService billQuery =
          NCLocator.getInstance().lookup(IBillQueryService.class);
      AggregatedValueObject newVO =
          billQuery.querySingleBillByPk(oldVO.getClass(), pk);
      // ���ݱ�ɾ��֮��Ӧ�ûص��б������ˢ��
      if (newVO == null) {
        // �����Ѿ���ɾ��
        throw new BusinessException(NCLangRes.getInstance().getStrByID("uif2",
            "RefreshSingleAction-000000")/*�����Ѿ���ɾ�����뷵���б���棡*/);
      }
      AbstractBill bill = Constructor.construct(oldVO.getClass());
      BillQueryUtils<AbstractBill> utils =
          new BillQueryUtils<AbstractBill>(bill);
      AbstractBill[] newbills = new AbstractBill[] {
        (AbstractBill) newVO
      };
      utils.fillBatchCalcAttrs(newbills);
      this.model.directlyUpdate(newVO);

    }

    this.showQueryInfo();
  }

}
