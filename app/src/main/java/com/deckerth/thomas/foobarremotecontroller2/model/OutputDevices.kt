package com.deckerth.thomas.foobarremotecontroller2.model

class OutputDevices() {
    private val devices: ArrayList<OutputDevice> = ArrayList()
    private var activeDevice: OutputDevice? = null
    var valid: Boolean = true

    fun addDevice(device: OutputDevice) {
        devices.add(device)
    }

    fun getDevices(): ArrayList<OutputDevice> {
        return devices
    }

    fun invalidate() {
        valid = false
    }

    fun getActiveDevice(): OutputDevice? {
        return devices.find { it.deviceId == activeDevice!!.deviceId && it.typeId == activeDevice!!.typeId }
    }

    fun setActiveDevice(device: OutputDevice?) {
        activeDevice = device
    }

    fun getActiveDeviceName(): String {
        if (valid && activeDevice != null) {
            val device = getActiveDevice()
                if (device != null) {
                return device.name
            }
        }
        return ""
    }
}