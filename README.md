# EmotionBasedMusicPlayer

### Introduction
We all go through many emotions throughout the day and sometimes we just need a friend who can understand us, support us, tell us that the emotions we are feeling are normal. Many times Music plays the role of our empathetic friend. Emosic is one such friend that we have for you and like your real friends Emosic can detect your emotions just by reading your face. This ability to detect your emotion is not something inherited in it as your real friend has but as friends do Emosic put all its efforts to do what it can in detecting your emotion and then fetch cool songs for you from the backend that we have deployed to help your friend.

Fascinating?? Let's have a closure look at what we have to offer.

### Brief Description
Scratching your head over how your new friend detects your emotions?? Let us help you in understanding how a Machine detects emotions...

Have you seen drawings in which we are given various dots and when we connect those dots we surprisingly see a picture. Just like this AI marks various dots on our face with the help of facial landmarks like position of the eyes, mouth, eyebrows. and then it calculates the distances between various dots, using this calculation along with information about the general facial contours, AI guesses what should be the emotion we are feeling.

Once Emosic guesses how you are feeling, then based on its guess, Emosic fetches the songs from the database using the backend that we have deployed on heroku and voila that's how things work here.

### Tech Stacks
- Control Flow
  * Kotlin
  * Java

- UI
  * XML
  * Android Navigation Components
- Backend and API
  * Retrofit + Moshi
  * Firebase
  * Heroku
  * Express + Node JS
  * PostgreSQL
  * Nodemon

- Microsoft Azure Services for Emotion Detection
- Various other android dependencies


### Features Implemented
- Emotion Detection using Microsoft Azure Services
- Fetching Songs and other Data from Backend deployed on Heroku
- User authentication using Firebase
- SQL database to store songs data deployed using PostgreSQL
- Nosql database for user related data like their favorite songs deployed on Firebase
- Media Player to play the fetched song.
- Notification featuring the current playing song
- Users Profile window
- Dedicated window for favorite songs of the user
- Edge cases where user is not connected to the internet or the picture of the user is not clear to detect emotion
- Changing the songs by sliding over the music player for better user experience.


