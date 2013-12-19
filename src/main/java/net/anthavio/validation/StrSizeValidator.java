package net.anthavio.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StrSizeValidator implements ConstraintValidator<StrSize, String> {

	private boolean nullIsValid;
	private boolean emptyIsValid;

	private int min;
	private int max;

	public void initialize(StrSize annotation) {
		nullIsValid = annotation.nullIsValid();
		emptyIsValid = annotation.emptyIsValid();
		min = annotation.min();
		max = annotation.max();
		validateParameters();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean result;
		if (value == null) {
			if (nullIsValid) {
				result = true;
			} else {
				result = false;
			}
		} else {
			int length = value.length();
			if (length == 0 && emptyIsValid) {
				result = true;
			} else {
				result = length >= min && length <= max;
			}
		}
		return result;
	}

	private void validateParameters() {
		if (min < 0) {
			throw new IllegalArgumentException("The min parameter cannot be negative.");
		}
		if (max < 0) {
			throw new IllegalArgumentException("The max parameter cannot be negative.");
		}
		if (max < min) {
			throw new IllegalArgumentException("The length cannot be negative.");
		}
	}
}
