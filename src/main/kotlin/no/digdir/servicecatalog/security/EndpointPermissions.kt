package no.digdir.servicecatalog.security

import no.digdir.servicecatalog.model.OpEnum
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

private const val ROLE_ROOT_ADMIN = "system:root:admin"
private fun roleOrgAdmin(orgnr: String) = "organization:$orgnr:admin"
private fun roleOrgWrite(orgnr: String) = "organization:$orgnr:write"
private fun roleOrgRead(orgnr: String) = "organization:$orgnr:read"

@Service
class EndpointPermissions {
    fun hasOrgReadPermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            authorities.contains(roleOrgRead(orgnr)) -> true
            authorities.contains(ROLE_ROOT_ADMIN) -> true
            else -> false
        }
    }

    fun hasOrgWritePermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            else -> false
        }
    }
    fun hasSysAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROLE_ROOT_ADMIN) ?: false
    }

    fun getOrgsByPermissions(jwt: Jwt, permission: OpEnum): Set<String> {
        val authorities: String? = jwt.claims["authorities"] as? String
        val regex = when(permission){
            OpEnum.READ -> Regex("""[0-9]{9}""")
            else -> Regex("""[0-9]{9}:$permission""")
        }

        return authorities
            ?.let { regex.findAll(it)}
            ?.map { matchResult -> matchResult.value
                .replace(Regex("[A-Za-z:]"), "")}
            ?.toSet()
            ?: emptySet()
    }
}
