/*
 * Copyright (C) 2020 Hamidreza Etebarian & Ali Modares.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idco.sample.util

import com.idco.sample.util.ExceptionExt.avoidException
import java.util.regex.Pattern

/**
 * Validate Extensions.
 *
 * @author  Hamidreza Etebarian
 * @version 1.0.0
 * @since   2020-03-20
 */
class ValidateExt {
    /**
     * Check validation of Email.
     * If input is empty return false.
     * @return a boolean describes validation of Email.
     */
    @Suppress("RegExpRedundantEscape")
    fun String?.isValidEmail() = avoidException {
        if (isNullOrEmpty())
            return false
        Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE)
                .matcher(this!!).matches()
    } ?: false

    /**
     * Check validation of Legacy Mobile.
     * If input is empty return false.
     * @return a boolean describes validation of Legacy Mobile.
     */
    fun String?.isValidMobileLegacy() = avoidException {
        if (isNullOrEmpty())
            return false
        val chars = this.toCharArray()
        for (c in chars) {
            if (!Character.isDigit(c)) {
                return false
            }
        }
        this.length == 11 && this.startsWith("09")
    } ?: false

    /**
     * Check validation of Identity Code Iran Country.
     * If input is empty return false.
     * @return a boolean describes validation of Identity Code.
     */
    fun String?.isValidIdentityCodeIran() = avoidException {
        if (isNullOrEmpty())
            return false

        if (this == "0000000000")
            return false

        val a = IntArray(11)
        var sum = 0
        val m: Int
        val b: Int
        a[0] = 0
        val arr = this!!.toCharArray()

        for (i in arr.indices) {
            a[10 - i] = arr[i].toInt() - 48
            if (i != 9)
                sum += a[10 - i] * (10 - i)
        }

        m = a[1]
        b = sum % 11

        b == m || 11 - b == m
    } ?: false
}