package dev.wh2sperx.api.model

import net.luckperms.api.model.user.User

data class UserData(
    val valid: Boolean,
    val user: User? = null
)