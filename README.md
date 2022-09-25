# LightMessageBroker
This is an example of how to build a tiny simple and very fast message broker to communicate many services in your Android application avoiding code and login coupling between them.

The project is splitted in two parts, a module that acts as a library that handles the logic of the mailboxes (create, delete, send and read them) and a small application that uses that module.

The application made using jetpack compose, coruotines and workers, is quite simple and is made just to show some of the possibilities of the applications, I encourage you to see how the application uses the library before importing it into your project. 


## How the library works

Think the library as a list of mailboxes, each atached client has his own mail box with an unique identifier that allows him to read messages that has been sent to him. Also think that each mail box is subscribed to receive some newsletters about a specific topic (category), so it is bassiaclly what this library does:

![mail boxes 001](https://user-images.githubusercontent.com/52082881/192149631-8979180c-0a73-4b9a-bf4e-d9a4ec10599b.jpeg)




## How to use the library

I try to use a functional approach when I design the API to access the library, also I try to make the clients agnostic about the underlying data structures and mechanism of the mail box system. If you prefer to publish the internal data structures feel free modify in your project.

### Attach and process messages

Well, the use of this library is quite straight forward, no interfaces neither inheritance of classes is needed, just call *attachMessageClient* function sending a long as an unique identifier (see [Uuid](https://developer.android.com/reference/kotlin/java/util/UUID)) and a list of categories that is going to be listening. Categories acts a filter. The client will receive only messages that have been categorized inside on the configured in the client. It is optional and you can define clients without categories and send message without categories for broadcast comunication, but this will provoke an overhead in the CPU.

```kotlin
import com.bortxapps.lightmessagebroker.messagehandler.attachMessageClient
.
.
.
attachMessageClient(
    clientId = uuid,
    supportedCategories = listOf(supportedCategory)
) { clientId, messageKey, messageCategory, messagePayload ->
    processMessage(clientId, messageKey, messageCategory, messagePayload)
}
```

When the client receives a message in its mail box, the callback function passed here will be triggered with the following parameters that helps you to process the data:

- **Client ID**: This is de unique identifier of the client that received the message (useful when you defined several message handles in the same class.
- **MessaheKey**: This is the identifier of the message, needed if you design a communication protocol with several messages, it helps you to speed-up the processing time.
- **MessageCategory**: The scope of the message
- **MessagePayload**: The data carried by the message, By design empty message are not allowed.

### Send messages

There are three different ways of sending messages.
- Send messages broadcast
- Send messages multicast
- Send a message to one specified client

### Send broadcast messages

```kotlin
sendBroadcastMessage(
    senderId = clientID,
    messageKey = messageKey,
    payload = messageData
)
````

Using that method, the message is not categorised, so it is send to **all clients attached to the messaging system** (except the client that sends that message).

#### Send Multicast messages

```kotlin
sendBroadcastMessage(
    senderId = clientID,
    messageKey = messageKey,
    categoryKey = Random.nextLong(numberCostumers.toLong()),
    payload = messageData
)
```

This is similar than the previous method unlike the message is categorised. That means that **the message is only send to clients that are listening the category of the message**. It is the fastest method if you want to send a message to a group of clients.

#### Send unicast messages

```kotlin
sendMessageToClient(
      targetClientId = clientID.toLong(),
      messageKey = messageKey,
      payload = messageData
  )
```

This method just send messages to the specified client, it is fastest way to send a message individually.

### Stop receiving messages

If the client doesn't want to process more messages or is going to be disposed (due it is an Android Architetcural element or whatever) it has to be removed from the queue system in orde to free resources:

```kotlin
removeHandler(clientId = 1L)
```

### Clear the messaging system

It is quite simple, if you want to free al resources handled by the messaging system and remove all clients, just call:

```kotlin
clearAllHandlers()
```


### Precautions

- If you attach an Android architectural element you need to control the lifecycle of the element and remove it from the queue system to avoid memory leaks.

- The concurrency strategy to manage and send messages depends on your application. The example that I give here is just a way to implement it. You can use Services instead of handlers, javaRX, Threads, Handlers, etc...

- You have to control the message flow to avoid breaking the memory heap of the application. The CPU and memory usage depends if you send messages individually and the way to process the message, and the payload. Primitive types have better performance, avoid broadcast messages whitout category, process messages in different threads... Consider on apply some producer-consumer estrategy and take in mind how to deal with overload scenarios

            
