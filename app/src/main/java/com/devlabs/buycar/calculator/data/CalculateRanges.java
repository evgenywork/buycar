package com.devlabs.buycar.calculator.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculateRanges {
    private MinMaxField cost;
    private MinMaxField initialFee;
    private MinMaxField residualPayment;
    private MinMaxField term;
}

/*       "cost": {
            "filled": true,
            "max": 10000000,
            "min": 1500000
            },
            "initialFee": {
            "filled": true,
            "max": 100,
            "min": 20
            },
            "residualPayment": {
            "filled": true,
            "max": 10000000,
            "min": 0
            },
            "term": {
            "filled": true,
            "max": 7,
            "min": 1
            }*/