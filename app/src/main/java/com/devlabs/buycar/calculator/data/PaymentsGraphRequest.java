package com.devlabs.buycar.calculator.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsGraphRequest {
    private double contractRate; //Базовая процентная ставка, %
    private double lastPayment; //Остаточный платёж, руб
    private double loanAmount; //Сумма кредита, руб
    private double payment;
    private int term;
}
/*{"contractRate":10.1,"lastPayment":41.3378801,"loanAmount":1000000,"payment":50000,"term":5}*/