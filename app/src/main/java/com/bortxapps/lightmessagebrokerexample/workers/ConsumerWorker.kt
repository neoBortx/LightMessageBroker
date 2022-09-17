package com.bortxapps.lightmessagebrokerexample.workers

/*class ConsumerWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    private var counter: Int = 0
    private var resultTimeStamp: Long = 0L
    private var numberMessages: Int = 0
    private var receivedEndSignal = false


    override suspend fun doWork(): Result {
        counter = 0
        numberMessages = inputData.getInt(NUMBER_MESSAGES, 0)

        val category = inputData.getLong(MESSAGE_CATEGORY, 0)

        val clientId = inputData.getLong(CLIENT_ID, 0L)

        attachMessageClient(
            clientId = clientId,
            supportedCategories = listOf(category, FINISH_PROCESSING_CATEGORY)
        ) { _, msgCategory, data ->


            if (msgCategory == FINISH_PROCESSING_CATEGORY) {
                receivedEndSignal = true
            } else {
                counter++
                if (counter == numberMessages - 1) {
                    resultTimeStamp = System.currentTimeMillis()
                }
            }
        }

        while (!receivedEndSignal || resultTimeStamp != 0L) {
            Thread.sleep(10)
        }

        removeHandler(clientId = clientId)
        return Result.success(workDataOf(PROCESSED_MESSAGES to counter, END_TIME_STAMP to resultTimeStamp))
    }
}*/