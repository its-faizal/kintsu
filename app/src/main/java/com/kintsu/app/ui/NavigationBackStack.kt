package com.kintsu.app.ui

import com.kintsu.app.ui.components.KintsuNavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationBackStack(initialTab: KintsuNavTab = KintsuNavTab.HOME) {
    private val _stack = MutableStateFlow(listOf(initialTab))
    val stack: StateFlow<List<KintsuNavTab>> = _stack.asStateFlow()

    val currentTab: KintsuNavTab
        get() = _stack.value.lastOrNull() ?: KintsuNavTab.HOME

    fun selectTab(tab: KintsuNavTab) {
        if (_stack.value.lastOrNull() == tab) return
        val current = _stack.value.toMutableList()
        if (tab == KintsuNavTab.HOME) {
            _stack.value = listOf(KintsuNavTab.HOME)
        } else {
            current.removeAll { it == tab }
            current.add(tab)
            _stack.value = current
        }
    }

    fun pop(): Boolean {
        val current = _stack.value
        if (current.size > 1) {
            _stack.value = current.dropLast(1)
            return true
        }
        return false
    }
}
