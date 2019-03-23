package sample.mail.ru.httpsample.request

interface RequestExecutor {
    suspend fun execute() : Pair<String, Int>
}