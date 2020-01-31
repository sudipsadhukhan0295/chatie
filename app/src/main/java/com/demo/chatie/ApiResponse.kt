package com.demo.chatie


class ApiResponse<T> {
    var responseBody: T? = null
    var throwable: Throwable? = null

    constructor(responseBody: T?) {
        this.responseBody = responseBody
    }

    constructor(throwable: Throwable?) {
        this.throwable = throwable
    }
}