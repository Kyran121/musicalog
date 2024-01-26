package com.thevirtualforge.musicalog.validation.constraint;

import com.thevirtualforge.musicalog.validation.ValueOfEnum;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ValueOfEnumValidatorTest {

    @Mock
    private ValueOfEnum valueOfEnum;

    private ValueOfEnumValidator validator;

    @BeforeEach
    void setup() {
        doReturn(DummyEnum.class)
            .when(valueOfEnum).enumClass();

        validator = new ValueOfEnumValidator();
        validator.initialize(valueOfEnum);
    }

    @Test
    void isValid() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
        assertThat(validator.isValid("ONE", null)).isTrue();
        assertThat(validator.isValid("TWO", null)).isTrue();
        assertThat(validator.isValid("one", null)).isTrue();
        assertThat(validator.isValid("two", null)).isTrue();

        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
        doReturn("").when(ctx).getDefaultConstraintMessageTemplate();
        assertThat(validator.isValid("THREE", ctx)).isFalse();
    }

    public enum DummyEnum {
        ONE,
        TWO
    }
}