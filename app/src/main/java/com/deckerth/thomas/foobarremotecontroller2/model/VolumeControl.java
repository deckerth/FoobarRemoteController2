package com.deckerth.thomas.foobarremotecontroller2.model;

import static java.lang.Math.log;
import static java.lang.Math.log10;
import static java.lang.Math.pow;

public class VolumeControl implements IVolumeControl {
    private Boolean isMuted = false;
    private Integer min = -100;
    private Integer max = 0;
    private String type = "db";
    private Integer value = 0;

    public VolumeControl(Boolean isMuted, Integer min, Integer max, String type, Integer value) {
        this.isMuted = isMuted;
        this.min = min;
        this.max = max;
        this.type = type;
        this.value = value;
    }

    public VolumeControl() {
    }

    @Override
    public Boolean getMuted() {
        return isMuted;
    }

    @Override
    public Integer getMin() {
        return min;
    }

    @Override
    public Integer getMax() {
        return max;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }

    @Override
    public void setMin(Integer min) {
        this.min = min;
    }

    @Override
    public void setMax(Integer max) {
        this.max = max;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getDecibelValue(Integer percent) {
/*
        p = 0..1 d = -100..-1:

        d = Pow(100,1-p)*(-1) = -100/Pow(10,2*p),
        p = (-1)*Log10(-d)/2+1
*/
        double p = (double)percent / 100.0;
        if (p < 0) p = 0;
        if (p > 1) p = 1;

        // convert 0..1 -> -100..-1
        double d = -100.0/pow(10,2*p);

        // convert -100..-1 -> -99..0 -> -100..0
        int result = (int) Math.round((d+1.0)/99.0 * 100.0);

        if (result < -100) result = -100;
        if (result > 0) result = 0;

        return result;
    }

    private Integer getPercentValue(Integer decibel) {
        /*
        p = 0..1 d = -100..-1:

        d = Pow(100,1-p)*(-1) = -100/Pow(10,2*p),
        p = (-1)*Log10(-d)/2+1
        */

        double v100_0 = (double) decibel;

        //convert -100..0 -> -99..0 -> -100..-1
        double d = v100_0 / 100.0 * 99.0 - 1.0;

        if (d >= -1)
            return 100;

        double p = -log10(-d)/2.0 + 1.0;

        int result = (int)Math.round(p * 100);
        if (result < 0) result = 0;
        if (result > 100) result = 100;
        return result;
    }

    @Override
    public Integer getModifiedValuePercent(Integer deltaPercent) {
        int modifiedPercent = getCurrentValuePercent() + deltaPercent;
        if (modifiedPercent > 100) modifiedPercent = 100;
        if (modifiedPercent < 0) modifiedPercent = 0;
        return  modifiedPercent;
    }

    @Override
    public Integer getCurrentValuePercent() {
        return getPercentValue(value);
    }

}
