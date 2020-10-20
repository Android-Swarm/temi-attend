package com.zetzaus.temiattend.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Database entity for the external visitor travel declaration form.
 *
 * @property id The primary key of this entity.
 * @property name The name of the visitor.
 * @property company The name of the visitor's company.
 * @property contactNo The contact number of the visitor.
 * @property dateOfVisit The date of visit of the visitor to Schaeffler.
 * @property host The host name that the visitor is visiting.
 * @property traveledLastTwoWeeks `true` if the visitor has travelled abroad in the last 15 days.
 * @property travelStartDate The starting date of travel. Only exists if [traveledLastTwoWeeks] is `true`.
 * @property travelEndDate The ending date of travel. Only exists if [traveledLastTwoWeeks] is `true`.
 * @property haveFeverOrSymptoms `true` if the visitor has fever and/or related-symptoms.
 * @property contactWithTraveler `true` if the visitor has been in contact with someone who traveled
 *                                abroad in the last 15 days.
 * @property contactWithConfirmed `true` if the visitor has been in contact with a confirmed COVID-19 case.
 * @property contactWithSuspected `true` if the visitor has been in contact with a suspected COVID-19 case.
 * @property contactWithQuarantine `true if the visitor has been in contact with a quarantined/SHN or recently
 *                                  released from quarantine/SHN.
 */
@Entity
data class Visitor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val company: String,
    val contactNo: String,
    val dateOfVisit: Date,
    val host: String,
    val traveledLastTwoWeeks: Boolean,
    val travelStartDate: Date?,
    val travelEndDate: Date?,
    val haveFeverOrSymptoms: Boolean,
    val contactWithTraveler: Boolean,
    val contactWithConfirmed: Boolean,
    val contactWithSuspected: Boolean,
    val contactWithQuarantine: Boolean,
)

@Dao
interface VisitorDao {
    @Insert
    suspend fun saveVisitor(visitor: Visitor)

    @Query("SELECT * FROM Visitor")
    fun getAllVisitors(): Flow<List<Visitor>>
}