package com.devlabs.buycar;

public class Car {

    private int carId;
    private String carPhotoImageView;
    private String carAlias;
    private String carModelsBodiesAlias;
    private String carModelAlias;
    private String carCountryCode;
    private int carMinprice;

    public Car(int carId, String carAlias, String carModelAlias, int carMinprice) {
        this.carId = carId;
        this.carAlias = carAlias;
        this.carModelAlias = carModelAlias;
        this.carMinprice = carMinprice;
    }

    public int getCarId() {
        return carId;
    }

    public String getCarPhotoImageView() {
        return carPhotoImageView;
    }

    public void setCarPhotoImageView(String carPhotoImageView) {
        this.carPhotoImageView = carPhotoImageView;
    }

    public String getCarModelsBodiesAlias() {
        return carModelsBodiesAlias;
    }

    public void setCarModelsBodiesAlias(String carModelsBodiesAlias) {
        this.carModelsBodiesAlias = carModelsBodiesAlias;
    }

    public String getCarAlias() {
        return carAlias;
    }

    public String getCarModelAlias() {
        return carModelAlias;
    }

    public String getCarCountryCode() {
        return carCountryCode;
    }

    public void setCarCountryCode(String carCountryCode) {
        this.carCountryCode = carCountryCode;
    }

    public int getCarMinprice() {
        return carMinprice;
    }
}
