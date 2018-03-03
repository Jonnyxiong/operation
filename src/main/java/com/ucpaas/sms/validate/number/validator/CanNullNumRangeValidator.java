package com.ucpaas.sms.validate.number.validator;

import com.ucpaas.sms.validate.number.CanNullNumberRange;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by lpjLiu on 2017/6/26.
 */
public class CanNullNumRangeValidator implements ConstraintValidator<CanNullNumberRange, String> {
	private int min;
	private int max;

	@Override
	public void initialize(CanNullNumberRange parameters) {
		min = parameters.min();
		max = parameters.max();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null || "".equals(value)) {
			return true;
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
