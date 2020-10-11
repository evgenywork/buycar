package com.devlabs.buycar.calculator.data;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculateResult {
    private double contractRate;
    private double kaskoCost;
    private double lastPayment;
    private double loanAmount;
    private double residualPayment;
    private double payment;
    private double subsidy;
    private int term;
}


/*   "result": {
            "contractRate": 10.1,
            "kaskoCost": 10000,
            "lastPayment": 30.43284064,
            "loanAmount": 1000000,
            "payment": 50000,
            "subsidy": 9.41472693,
            "term": 5
            }*/