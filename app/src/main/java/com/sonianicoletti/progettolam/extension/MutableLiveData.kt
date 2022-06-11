package com.sonianicoletti.progettolam.extension

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.emit() {
    postValue(value)
}
