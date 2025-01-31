package app.askresume.domain.member.constant

enum class MemberType {

    LOCAL,
    GOOGLE,
    ;

    companion object {
        fun from(type: String): MemberType {
            return MemberType.valueOf(type.uppercase())
        }

        fun isMemberType(type: String): Boolean {
            return MemberType.values().any { it.name == type.uppercase() }
        }
    }

}