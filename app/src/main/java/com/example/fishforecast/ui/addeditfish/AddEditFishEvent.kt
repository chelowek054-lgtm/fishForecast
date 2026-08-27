package com.example.fishforecast.ui.addeditfish

sealed class AddEditFishEvent {
    data class EnteredName(val value: String) : AddEditFishEvent()
    data class EnteredDescription(val value: String) : AddEditFishEvent()
    data class EnteredOptMinTemp(val value: String) : AddEditFishEvent()
    data class EnteredOptMaxTemp(val value: String) : AddEditFishEvent()
    data class EnteredAbsMinTemp(val value: String) : AddEditFishEvent()
    data class EnteredAbsMaxTemp(val value: String) : AddEditFishEvent()
    data class EnteredMinPressure(val value: String) : AddEditFishEvent()
    data class EnteredMaxPressure(val value: String) : AddEditFishEvent()
    object SaveFish : AddEditFishEvent()
}