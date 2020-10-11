package com.devlabs.buycar.calculator.data;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateRequest {
    private ArrayList<String> clientTypes; // Типы клиента
    private long cost; // default: 850000 minimum:1500000 maximum:10000000 Стоимость автомобиля
    private long initialFee; // default: 200000 Первоначальный взнос, руб
    private long kaskoValue;
    private String language;
    private double residualPayment; // Остаточный платеж, руб
    private String settingsName; // Наименование калькулятора
    private ArrayList<String> specialConditions; // Специальные условия
    private int term; //Срок кредита, default: 5 minimum:1 maximum:7
}

/*
'{"clientTypes":["ac43d7e4-cd8c-4f6f-b18a-5ccbc1356f75"],
  "cost":850000,
  "initialFee":200000,
  "kaskoValue":64412219,
  "language":"en",
  "residualPayment":86.96731273,
  "settingsName":"Haval",
  "specialConditions":["57ba0183-5988-4137-86a6-3d30a4ed8dc9",
                       "b907b476-5a26-4b25-b9c0-8091e9d5c65f",
                       "cbfc4ef3-af70-4182-8cf6-e73f361d1e68"
                       ],
  "term":5}'
 */