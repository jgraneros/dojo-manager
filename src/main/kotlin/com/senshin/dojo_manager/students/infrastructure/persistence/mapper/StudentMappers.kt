package com.senshin.dojo_manager.students.infrastructure.persistence.mapper

import com.senshin.dojo_manager.students.domain.model.PersonalInfo
import com.senshin.dojo_manager.students.domain.model.PromotionRecord
import com.senshin.dojo_manager.students.domain.model.Rank
import com.senshin.dojo_manager.students.domain.model.Student
import com.senshin.dojo_manager.students.domain.model.StudentId
import com.senshin.dojo_manager.students.infrastructure.persistence.entity.PromotionRecordJpaEntity
import com.senshin.dojo_manager.students.infrastructure.persistence.entity.StudentJpaEntity

// --- De Dominio a JPA Entity ---

fun Student.toJpaEntity(): StudentJpaEntity {
    val entity = StudentJpaEntity(
        id = this.id.value,
        firstName = this.personalInfo.firstName,
        lastName = this.personalInfo.lastName,
        identificationNumber = this.personalInfo.identificationNumber,
        birthDate = this.personalInfo.birthDate,
        phone = this.personalInfo.phone,
        emergencyContactName = this.personalInfo.emergencyContactName,
        emergencyContactPhone = this.personalInfo.emergencyContactPhone,
        medicalNotes = this.personalInfo.medicalNotes,
        status = this.status,
        currentRankType = this.currentRank.type,
        currentRankLevel = this.currentRank.level,
        currentRankColor = this.currentRank.color,
        currentRankOrder = this.currentRank.orderIndex
    )

    entity.promotionHistory = this.promotionHistory.map { it.toJpaEntity(entity) }.toMutableList()
    return entity
}

fun PromotionRecord.toJpaEntity(studentEntity: StudentJpaEntity): PromotionRecordJpaEntity {
    return PromotionRecordJpaEntity(
        id = this.id,
        student = studentEntity,
        fromRankType = this.fromRank.type,
        fromRankLevel = this.fromRank.level,
        fromRankColor = this.fromRank.color,
        fromRankOrder = this.fromRank.orderIndex,
        toRankType = this.toRank.type,
        toRankLevel = this.toRank.level,
        toRankColor = this.toRank.color,
        toRankOrder = this.toRank.orderIndex,
        promotionDate = this.promotionDate,
        examiner = this.examiner,
        observations = this.observations
    )
}

// --- De JPA Entity a Dominio ---

fun StudentJpaEntity.toDomain(): Student {
    val student = Student(
        id = StudentId(this.id),
        personalInfo = PersonalInfo(
            firstName = this.firstName,
            lastName = this.lastName,
            identificationNumber = this.identificationNumber,
            birthDate = this.birthDate,
            phone = this.phone,
            emergencyContactName = this.emergencyContactName,
            emergencyContactPhone = this.emergencyContactPhone,
            medicalNotes = this.medicalNotes
        ),
        status = this.status,
        currentRank = Rank(
            type = this.currentRankType,
            level = this.currentRankLevel,
            color = this.currentRankColor,
            orderIndex = this.currentRankOrder
        )
    )

    // Reconstruimos el historial marcial existente sin disparar validaciones de nuevo examen
    this.promotionHistory
        .sortedBy { it.promotionDate }
        .forEach { recordEntity ->
            val domainRecord = PromotionRecord(
                id = recordEntity.id,
                fromRank = Rank(
                    recordEntity.fromRankType,
                    recordEntity.fromRankLevel,
                    recordEntity.fromRankColor,
                    recordEntity.fromRankOrder
                ),
                toRank = Rank(
                    recordEntity.toRankType,
                    recordEntity.toRankLevel,
                    recordEntity.toRankColor,
                    recordEntity.toRankOrder
                ),
                promotionDate = recordEntity.promotionDate,
                examiner = recordEntity.examiner,
                observations = recordEntity.observations
            )
            // Agregamos al historial (podés exponer un método reconstructHistory si _promotionHistory es privado)
            student.loadPromotion(domainRecord)
        }

    return student
}