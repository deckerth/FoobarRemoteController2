package com.deckerth.thomas.foobarremotecontroller2.model;

public interface IVolumeControl {

/*
            "volume": {
                "isMuted": false,
                "max": 0,
                "min": -100,
                "type": "db",
                "value": 0
*/

    Boolean getMuted();
    Integer getMin();
    Integer getMax();
    String getType();
    Integer getValue();

    void setMuted(Boolean isMuted);
    void setMin(Integer min);
    void setMax(Integer max);
    void setType(String type);
    void setValue(Integer value);

    Integer getModifiedValuePercent(Integer deltaPercent);
    Integer getCurrentValuePercent();
    Integer getDecibelValue(Integer percent);
}
