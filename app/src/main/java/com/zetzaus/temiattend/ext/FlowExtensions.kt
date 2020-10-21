package com.zetzaus.temiattend.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Uses the [second] flow as a trigger. If the [second] flow emits `true`, do [filterIf] to the
 * first flow.
 *
 * @param T The type of item.
 * @param second The flow for trigger.
 * @param predicate The filter predicate.
 */
fun <T> Flow<Collection<T>>.combineAndFilterIf(second: Flow<Boolean>, predicate: (T) -> Boolean) =
    combine(second) { collection, trigger -> collection.filterIf(trigger, predicate) }