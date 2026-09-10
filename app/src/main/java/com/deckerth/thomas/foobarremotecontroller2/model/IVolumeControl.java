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

    void setMuted(Boolean isMuted);

    Integer getMin();

    void setMin(Integer min);

    Integer getMax();

    void setMax(Integer max);

    String getType();

    void setType(String type);

    Integer getValue();

    void setValue(Integer value);

    Integer getModifiedValuePercent(Integer deltaPercent);

    Integer getCurrentValuePercent();

    Integer getDecibelValue(Integer percent);
}
