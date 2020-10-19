package com.zetzaus.temiattend.ext

import com.zetzaus.temiattend.OfficeName

/** Mapping of [OfficeName] enum members to its display name. */
private val officeNames = mapOf(
    OfficeName.TAI_SENG to "18 Tai Seng",
    OfficeName.SHARE_NTU to "SHARELAB@NTU",
    OfficeName.AEROSPACE to "Aerospace",
    OfficeName.UNDEFINED to "Location not set",
    OfficeName.UNRECOGNIZED to "Unrecognized location"
)

/**
 * Converts [OfficeName] to a [String] suitable to be displayed to the user.
 *
 */
fun OfficeName.toStringRepresentation() =
    officeNames[this] ?: throw IllegalArgumentException("Invalid OfficeName enum supplied!")

/**
 * Converts [String] to [OfficeName]. Only [String] returned by [OfficeName.toStringRepresentation]
 * will be valid. Invalid [String] will return [OfficeName.UNDEFINED]
 *
 */
fun String.toOfficeName() = officeNames.entries
    .find { (_, officeLabel) -> officeLabel == this }
    ?.key
    ?: OfficeName.UNDEFINED