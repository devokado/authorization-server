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
import java.net.URL
import java.util.*

/**
 * [String] Extensions.
 *
 * @author  Hamidreza Etebarian
 * @version 1.0.0
 * @since   2020-03-20
 */

fun generateUUID() = UUID.randomUUID().toString()

fun String?.isNotNullOrEmpty() = !isNullOrEmpty()

/**
 * Trim a string with check nullability.
 * If input is null return empty string.
 * @return trimmed of string.
 */
fun String?.trimOrEmpty() = this?.trim() ?: ""

/**
 * Remove all spaces in a string with check nullability.
 * If input is null return empty string.
 * @return a string without any spaces.
 */
fun String?.trimAllSpaces() = this?.replace("\\s+".toRegex(), "") ?: ""

/**
 * Check a string that is empty with remove spaces.
 * If input is null return true.
 * @return a boolean describes a string is empty or not.
 */
fun String?.isEmptyTrimAllSpaces(): Boolean {
    var s: String? = this ?: return true
    s = s.trimAllSpaces()
    return s == "" || s.isEmpty()
}

/**
 * create a string from array.
 * If input is null return blocked string.
 * @return a string describes array.
 */
fun Array<String>?.createStringFromArray(): String {
    if (this == null)
        return "{}"
    return avoidException {
        if (size == 0)
            return "{}"

        val sb = StringBuilder()
        sb.append("{ ")
        for (i in indices) {
            sb.append(this[i])
            if (i != size - 1)
                sb.append(", ")
        }
        sb.append(" }")
        sb.toString()
    } ?: "{}"
}

/**
 * Normalize a url.
 * If input is empty return empty string.
 * @return a string describes normalized url.
 */
fun String?.normalizeUrl(): String {
    if (isEmptyTrimAllSpaces())
        return ""

    val pieces = this?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
    val textParts = ArrayList<String>()

    pieces?.let {
        for (piece in pieces) {
            avoidException(
                tryBlock = {
                    val isURL = URL(piece)
                    val protocol = isURL.protocol
                    val host = isURL.host
                    var query: String? = isURL.query
                    var path = isURL.path
                    var questionMark = "?"

                    if (path == "") {
                        path = "/"
                    }

                    if (query == null) {
                        query = ""
                        questionMark = ""
                    }

                    val url = "$protocol://$host$path$questionMark$query"
                    textParts.add(url)
                },
                exceptionBlock = {
                    textParts.add(piece)
                }
            )
        }
    }

    val resultString = StringBuilder()
    for (s in textParts) {
        resultString.append(s).append(" ")
    }

    return resultString.toString().trimOrEmpty()
}

/**
 * Capitalize first character in string.
 * If input is empty return empty string.
 * @return a string that capitalized first character.
 */
fun String?.capitalizeFirst(): String {
    if (isEmptyTrimAllSpaces())
        return ""

    val first = this?.get(0)
    return if (Character.isUpperCase(first!!)) {
        this!!
    } else {
        Character.toUpperCase(first) + this!!.substring(1)
    }
}

/**
 * Capitalize all characters in string.
 * If input is empty return empty string.
 * @return a string that capitalized all characters.
 */
fun String?.capitalizeAll(): String {
    if (isEmptyTrimAllSpaces())
        return this ?: ""

    val ch = this!!.toCharArray()
    for (i in ch.indices)
        ch[i] = Character.toUpperCase(ch[i])
    return String(ch)
}

/**
 * Capitalize per word in string.
 * If input is empty return empty string.
 * @return a string that capitalized per word.
 */
fun String?.capitalizeFirstSpace(): String {
    if (isEmptyTrimAllSpaces())
        return this ?: ""

    val ch = this!!.toCharArray()
    ch[0] = Character.toUpperCase(ch[0])
    for (i in 1 until ch.size) {
        if (i != ch.size - 1) {
            if (ch[i - 1] == ' ')
                ch[i] = Character.toUpperCase(ch[i])
        }
    }
    return String(ch)
}


/**
 * Convert numbers in string to persian format.
 * If input is empty return false.
 * @return a string with persian numbers.
 */
@Suppress("UnnecessaryVariable")
fun String?.toPersianNumber(): String {
    val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    if (isNullOrEmpty())
        return ""
    val out = StringBuilder()
    for (element in this!!) {
        when (val c = element) {
            in '0'..'9' -> out.append(persianNumbers[Integer.parseInt(c.toString())])
            '٫' -> out.append('،')
            else -> out.append(c)
        }
    }
    return out.toString()
}

/**
 * Convert a string to regex pattern.
 * If input is empty return empty string.
 * @param original is a string that
 * @return a string that contains only digits.
 */
fun String?.toPatternRegex(original: String, rx: String): String {
    if (this == null)
        return ""
    return replace(Regex(original), rx)
}


/**
 * Convert a string to integer if it is possible.
 * If input is empty return zero value.
 * @return a integer describes value in a string.
 */
fun String?.convertToInt() = avoidException {
    if (this == null || isEmptyTrimAllSpaces())
        return 0

    toInt()
} ?: 0


/**
 * Remove extra spaces in a string.
 * If input is empty return empty string.
 * @return a string without extra spaces.
 */
fun String?.removeExtraSpaces(): String {
    if (isNullOrEmpty())
        return ""
    return this!!.trim().replace(" +".toRegex(), " ")
}

/**
 * Add http to first of url if it has been not exist.
 * If input is empty return empty string.
 * @return a string that start with http.
 */
fun String?.addHttpIfNeed(isHttps: Boolean = false): String {
    if (this == null)
        return ""
    if (this.startsWith("http", false) || this.startsWith("https", false))
        return this

    val protocol = if (isHttps) "https" else "http"

    return "$protocol://$this"
}