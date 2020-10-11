package com.devlabs.buycar.calculator.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MinMaxField {
    private long min;
    private long max;
    private boolean filled;

}
