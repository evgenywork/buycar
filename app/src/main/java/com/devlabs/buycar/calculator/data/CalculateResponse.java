package com.devlabs.buycar.calculator.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculateResponse {
    private CalculateProgram program;
    private CalculateRanges ranges;
    private CalculateResult result;
}

/*
{
        "program": {
            "cost": {
            "filled": true,
            "max": 10000000,
            "min": 1500000
            },
            "id": "d3c2acc2-b91d-4a4e-b8cb-3be3d6d6d383",
            "programName": "Haval",
            "programUrl": "/personal/avtokredity/legkovye-avtomobili/haval/",
            "requestUrl": "//anketa.vtb.ru/avtokredit/"
        },
        "ranges": {
            "cost": {
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
                }
            },
        "result": {
            "contractRate": 10.1,
            "kaskoCost": 10000,
            "lastPayment": 30.43284064,
            "loanAmount": 1000000,
            "payment": 50000,
            "subsidy": 9.41472693,
            "term": 5
            }
        }*/