package com.devlabs.buycar.calculator.data;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculateProgram {
    private MinMaxField cost;
    private String id;
    private String programName;
    private String programUrl;
    private String requestUrl;
}

/*    "program": {
            "cost": {
            "filled": true,
            "max": 10000000,
            "min": 1500000
            },
            "id": "d3c2acc2-b91d-4a4e-b8cb-3be3d6d6d383",
            "programName": "Haval",
            "programUrl": "/personal/avtokredity/legkovye-avtomobili/haval/",
            "requestUrl": "//anketa.vtb.ru/avtokredit/"
        },*/