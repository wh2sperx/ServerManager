package dev.wh2sperx.manager.model

import net.luckperms.api.model.user.User

data class IsValid(
    val valid: Boolean,
    val user: User? = null
)