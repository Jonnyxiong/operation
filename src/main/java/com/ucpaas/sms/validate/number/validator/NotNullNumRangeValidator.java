package com.ucpaas.sms.validate.number.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ucpaas.sms.validate.number.NotNullNumberRange;

/**
 * Created by lpjLiu on 2017/6/26.
 */
public class NotNullNumRangeValidator implements ConstraintValidator<NotNullNumberRange, String> {
	private int min;
	private int max;

	@Override
	public void initialize(NotNullNumberRange parameters) {
		min = parameters.min();
		max = parameters.max();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null || "".equals(value)) {
			return false;
		}

		int num = 0;
		try {
			num = Integer.parseInt(value);
		} catch (ClassCastException e) {
			return false;
		} catch (Exception e) {
			return false;
		}

		return num >= min && num <= max;
	}

}
