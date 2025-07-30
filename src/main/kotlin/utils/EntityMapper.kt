package com.aatech.utils

interface EntityMapper<DomainModel, Entity> {
    fun mapFromDomainModel(domainModel: DomainModel): Entity
    fun mapToDomainModel(entity: Entity): DomainModel
}