package com.example.eric.lbstest.classes;

import cn.bmob.v3.BmobObject;

/**
 * Created by APLEE on 2017/5/7.
 * 自定义电子眼类
 */

public class ElectricEye extends BmobObject {

    private int Direction; //拍照角度,正北为0,顺时针方向为正

    private int StartDirection; //起拍角度,正北为0,顺时针方向为正

    private int SpeedLimit; //限速，0表示不限速

    private int BigSpeedLimit; //大车限速，0表示不限速

    //摄像头类型,1,监控探头 2,固定测速 4,流动测速 5,闯红灯拍照
    //7,区间测速 9,逆行拍照 10,禁止违章环道 11,违章停车拍照 12,占用公交车道拍照
    private int Type;

    private double StartX; //起拍点经度

    private double StartY; //起拍点纬度

    private double EndX; //探头实际点经度

    private double EndY; //探头实际点纬度

    private double SEEyeToEndX; //区间测速专用,记录区间专用

    private double SEEyeToEndY; //区间测速专用,记录区间专用

    private int RoadDis; //区间测速专用,区间道路长度

    private String FormattedAddress; //地址

    private String country; //国家

    private String province; //省份

    private String city; //城市

    private String district; //区县

    private String township; //镇

    public ElectricEye(int type, double endX, double endY) {
        Type = type;
        EndX = endX;
        EndY = endY;
    }

    public ElectricEye(int type, double endX, double endY, String formattedAddress) {
        Type = type;
        EndX = endX;
        EndY = endY;
        FormattedAddress = formattedAddress;
    }

    public int getBigSpeedLimit() {
        return BigSpeedLimit;
    }

    public void setBigSpeedLimit(int bigSpeedLimit) {
        BigSpeedLimit = bigSpeedLimit;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getDirection() {
        return Direction;
    }

    public void setDirection(int direction) {
        Direction = direction;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public double getEndX() {
        return EndX;
    }

    public void setEndX(double endX) {
        EndX = endX;
    }

    public double getEndY() {
        return EndY;
    }

    public void setEndY(double endY) {
        EndY = endY;
    }

    public String getFormattedAddress() {
        return FormattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        FormattedAddress = formattedAddress;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getRoadDis() {
        return RoadDis;
    }

    public void setRoadDis(int roadDis) {
        RoadDis = roadDis;
    }

    public double getSEEyeToEndX() {
        return SEEyeToEndX;
    }

    public void setSEEyeToEndX(double SEEyeToEndX) {
        this.SEEyeToEndX = SEEyeToEndX;
    }

    public double getSEEyeToEndY() {
        return SEEyeToEndY;
    }

    public void setSEEyeToEndY(double SEEyeToEndY) {
        this.SEEyeToEndY = SEEyeToEndY;
    }

    public int getSpeedLimit() {
        return SpeedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        SpeedLimit = speedLimit;
    }

    public int getStartDirection() {
        return StartDirection;
    }

    public void setStartDirection(int startDirection) {
        StartDirection = startDirection;
    }

    public double getStartX() {
        return StartX;
    }

    public void setStartX(double startX) {
        StartX = startX;
    }

    public double getStartY() {
        return StartY;
    }

    public void setStartY(double startY) {
        StartY = startY;
    }

    public String getTownship() {
        return township;
    }

    public void setTownship(String township) {
        this.township = township;
    }

    public int getType() {
        return Type;
    }

    public static int getType(String typeName) {
        switch (typeName) {
            case "监控探头":
                return 1;

            case "固定测速":
                return 2;

            case "流动测速":
                return 4;

            case "闯红灯拍照":
                return 5;

            case "区间测速":
                return 7;

            case "逆行拍照":
                return 9;

            case "禁止违章环道":
                return 10;

            case "违章停车拍照":
                return 11;

            case "占用公交车道拍照":
                return 12;

            default:
                break;
        }

        return 0;
    }

    public static String getTypeName(int type) {
        switch (type) {
            case 1:
                return "监控探头";

            case 2:
                return "固定测速";

            case 4:
                return "流动测速";

            case 5:
                return "闯红灯拍照";

            case 7:
                return "区间测速";

            case 9:
                return "逆行拍照";

            case 10:
                return "禁止违章环道";

            case 11:
                return "违章停车拍照";

            case 12:
                return "占用公交车道拍照";

            default:
                break;
        }

        return "";
    }

    public void setType(int type) {
        Type = type;
    }
}
