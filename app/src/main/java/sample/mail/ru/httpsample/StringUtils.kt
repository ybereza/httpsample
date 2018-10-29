package sample.mail.ru.httpsample

import java.io.InputStream

fun String.join(s : Collection<CharSequence>, delimiter: String) : String {
    var capacity = 0
    val delimLength = delimiter.length
    var iter = s.iterator()
    if (iter.hasNext()) {
        capacity += iter.next().length + delimLength
    }

    val buffer = StringBuilder(capacity)
    iter = s.iterator()
    if (iter.hasNext()) {
        buffer.append(iter.next())
        while (iter.hasNext()) {
            buffer.append(delimiter)
            buffer.append(iter.next())
        }
    }
    return buffer.toString()
}

fun InputStream.asString() : String {
    return bufferedReader(Charsets.UTF_8).use { it.readText() }
}
