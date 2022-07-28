package com.iha.olmega_mobilesoftware_v2


enum class States {
    Undefined,
    Init,
    Connecting,
    Connected,
    RequestDisconnection
}

enum class BatteryStates {
    Undefined, Normal, Warning, Critical
}

enum class StageManagerStates {
    Undefined, NoConfigSelected, ConfigFileNotValid, Running
}

enum class QuestionnaireMotivation {
    Manual, Auto
}

enum class ActiviyRequestCode {
    MainActivity, HelpActiviy, QuestionnaireActivity, PreferencesActivity, LinkDeviceHelper, DEVICE_ADMIN
}

enum class LinkHelperBluetoothStates {
    Connecting, Connected, Disconnecting, Disconnected
}

class AcitivyStates {
    var isCharging = false
    var questionaireEnabled = false
    var isAutomaticQuestionaireActive = false
    var InfoText = ""
    var NextQuestText = ""
    var BatteryState = BatteryStates.Undefined
    var batteryLevel = -1.0f
    var profileState = States.Undefined
    var InputProfile = ""
    var showCalibrationValuesError = false
}