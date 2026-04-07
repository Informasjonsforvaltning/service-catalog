package no.digdir.servicecatalog.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "services")
class ServiceEntity(
    @Id
    @Column(name = "id", nullable = false, length = 255)
    val id: String = "",
    @Column(name = "catalog_id", nullable = false, length = 50)
    val catalogId: String = "",
    @Column(name = "published", nullable = false)
    val published: Boolean = false,
    @Column(name = "service_type", nullable = false, length = 50)
    val serviceType: String = ServiceType.SERVICE.name,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    val data: Map<String, Any?>? = null,
)
