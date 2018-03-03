package com.ucpaas.sms.domain.message;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.enums.OrderStatusEnum;
import static com.ucpaas.sms.enums.OrderStatusEnum.*;
import com.ucpaas.sms.exception.OrderStateException;

/**
 * 使用状态模式和领域驱动来设计订单状态
 * 
 * @author huangwenjie
 *
 */
abstract class OrderState {
	protected String stateName;

	public OrderState(String stateName) {
		this.stateName = stateName;
	}

	abstract ResultVO operateTo(OrderStatusEnum orderStateEnum);
}

public class OrderDomain extends OrderListPo {

	/**
	 * 不得直接修改orderState字段
	 */
	private OrderState orderState;

	/**
	 * 用来做防重复更新的
	 */
	private Date lastUpdateTime;

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public OrderState getOrderState() {
		return orderState;
	}

	/**
	 * 不得直接修改orderState字段
	 * 
	 * @param orderState
	 */
	public void setOrderState(OrderState orderState) {
		int s = getState();
		if (s == 0)
			orderState = new Daipeidan();
		else if (s == 1)
			orderState = new Daipipei();
		else if (s == 2)
			orderState = new Daishenpi();
		else if (s == 3)
			orderState = new Tuidan();
		else if (s == 4)
			orderState = new Chedan();
		else if (s == 5)
			orderState = new Pipeichenggong();
		else if (s == 6)
			orderState = new Xuziyuan();
		else if (s == 7)// （销售总监）
			orderState = new DaishenheXX();
		else if (s == 8)// （订单运营）
			orderState = new DaishenheYY();
	}

	public void setOrderState() {
		setOrderState(null);
	}

	public Integer getState() {
		return super.getState();
	}

	public void setState(Integer state) {
		super.setState(state);
		int s = state;
		if (s == 0)
			orderState = new Daipeidan();
		else if (s == 1)
			orderState = new Daipipei();
		else if (s == 2)
			orderState = new Daishenpi();
		else if (s == 3)
			orderState = new Tuidan();
		else if (s == 4)
			orderState = new Chedan();
		else if (s == 5)
			orderState = new Pipeichenggong();
		else if (s == 6)
			orderState = new Xuziyuan();
		else if (s == 7)// （销售总监）
			orderState = new DaishenheXX();
		else if (s == 8)// （订单运营）
			orderState = new DaishenheYY();
	}

	@JsonIgnoreType
	class Daipeidan extends OrderState {

		public Daipeidan() {
			super("待配单");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			if (!getState().equals(待配单.getValue())) {
				throw new OrderStateException("当前状态不是待配单，请确认后再操作");
			}
			switch (orderStateEnum) {
			case 退单:
				setState(退单.getValue());
				break;
			case 待匹配:
				setState(待匹配.getValue());
				break;
			case 匹配成功:
				setState(匹配成功.getValue());
				break;
			case 撤单:
				setState(撤单.getValue());
				break;
			case 寻资源:
				setState(寻资源.getValue());
				break;
			default:
				throw new OrderStateException(
						"当前状态【" + stateName + "】不能变更为状态【" + orderStateEnum.getName() + "】，请确认后再操作");
			}
			return ResultVO.successDefault();
		}

	}

	@JsonIgnoreType
	class Daipipei extends OrderState {

		public Daipipei() {
			super("待匹配");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			if (!getState().equals(待匹配.getValue())) {
				throw new OrderStateException("当前状态不是待匹配，请确认后再操作");
			}
			switch (orderStateEnum) {
			case 退单:
				setState(退单.getValue());
				break;
			case 匹配成功:
				setState(匹配成功.getValue());
				break;
			case 撤单:
				setState(撤单.getValue());
				break;
			case 寻资源:
				setState(寻资源.getValue());
				break;
			default:
				throw new OrderStateException(
						"当前状态【" + stateName + "】不能变更为状态【" + orderStateEnum.getName() + "】，请确认后再操作");
			}
			return ResultVO.successDefault();
		}

	}

	@JsonIgnoreType
	class Daishenpi extends OrderState {

		public Daishenpi() {
			super("待审批");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			throw new UnsupportedOperationException("暂无此状态");
		}

	}

	@JsonIgnoreType
	class Tuidan extends OrderState {

		public Tuidan() {
			super("退单");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			if (!getState().equals(退单.getValue())) {
				throw new OrderStateException("当前状态不是退单，请确认后再操作");
			}
			switch (orderStateEnum) {
			case 待配单:
				setState(待配单.getValue());
				break;
			default:
				throw new OrderStateException(
						"当前状态【" + stateName + "】不能变更为状态【" + orderStateEnum.getName() + "】，请确认后再操作");
			}
			return ResultVO.successDefault();
		}

	}

	@JsonIgnoreType
	class Chedan extends OrderState {

		public Chedan() {
			super("撤单");
		}

		@Override
		public ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			throw new UnsupportedOperationException("非法操作，撤销已经是订单的最终状态");
		}

	}

	@JsonIgnoreType
	class Pipeichenggong extends OrderState {

		public Pipeichenggong() {

			super("匹配成功");
		}

		@Override
		public ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			throw new UnsupportedOperationException("非法操作，匹配成功已经是订单的最终状态");
		}

	}

	@JsonIgnoreType
	class Xuziyuan extends OrderState {

		public Xuziyuan() {
			super("寻资源");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			if (!getState().equals(寻资源.getValue())) {
				throw new OrderStateException("当前状态不是寻资源，请确认后再操作");
			}
			switch (orderStateEnum) {
			case 待配单:
				setState(待配单.getValue());
				break;
			case 撤单:
				setState(撤单.getValue());
				break;
			default:
				throw new OrderStateException(
						"当前状态【" + stateName + "】不能变更为状态【" + orderStateEnum.getName() + "】，请确认后再操作");
			}
			return ResultVO.successDefault();
		}

	}

	@JsonIgnoreType
	class DaishenheXX extends OrderState {

		public DaishenheXX() {
			super("待审核");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			throw new UnsupportedOperationException("暂无此状态");
		}

	}

	@JsonIgnoreType
	class DaishenheYY extends OrderState {

		public DaishenheYY() {
			super("待审核");
		}

		@Override
		ResultVO operateTo(OrderStatusEnum orderStateEnum) {
			throw new UnsupportedOperationException("暂无此状态");
		}

	}

	public void goToStatus(OrderStatusEnum orderStateEnum) {
		getOrderState().operateTo(orderStateEnum);
	}

}
