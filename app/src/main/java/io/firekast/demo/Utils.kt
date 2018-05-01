package io.firekast.demo

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

fun <T> observing(initialValue: T,
                  willSet: () -> Unit = { },
                  didSet: () -> Unit = { }
) = object : ObservableProperty<T>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean =
            true.apply { willSet() }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = didSet()
}