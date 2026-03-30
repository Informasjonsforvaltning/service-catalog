package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(
    name = "services",
    indexes = [
        Index(name = "idx_services_catalog_id", columnList = "catalog_id"),
        Index(name = "idx_services_catalog_id_published", columnList = "catalog_id, published")
    ]
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Service (
    @Id
    val id: String,

    @Column(name = "catalog_id", nullable = false)
    val catalogId: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "nb", column = Column(name = "title_nb")),
        AttributeOverride(name = "nn", column = Column(name = "title_nn")),
        AttributeOverride(name = "en", column = Column(name = "title_en"))
    )
    val title: LocalizedStrings?,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "nb", column = Column(name = "description_nb")),
        AttributeOverride(name = "nn", column = Column(name = "description_nn")),
        AttributeOverride(name = "en", column = Column(name = "description_en"))
    )
    val description: LocalizedStrings?,

    val published: Boolean = false,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val produces: List<Output>?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_points", columnDefinition = "jsonb")
    val contactPoints: List<ContactPoint>?,

    val homepage: String?,
    val status: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val spatial: List<String>?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val subject: Set<String>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ServiceToBeCreated(
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?,
    val spatial: List<String>?,
    val subject: Set<String>?
)
