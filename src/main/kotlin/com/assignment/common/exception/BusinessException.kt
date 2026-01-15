package com.assignment.common.exception

class BusinessException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
