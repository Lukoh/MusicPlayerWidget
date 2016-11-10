# MusicPlayerWidget
This is simple MusicPlayerWidget. It supports the shuffle function to play a random mp3 file which is chosen by MusicPlayerWidget among all mp3 files in the primary external storage directory on Android device.

## Notice
You have to allow the permission to access the primary external storage for Android 6.0 supported device. 
Please refer to the first screen-shot picture.

When I carried out the instrumented unit test for Service, I commented below code(the line) calling startForeground() & startForeground() methods in PlayerService.java file.
Please comment(block) below code whenever you run the instrumented unit test for Service in PlayerService.java file :

stopForeground(true) & startForeground(NOTIFICATION_ID, mNotificationBuilder.build());

## Demo Video
Here is [demo video](https://youtu.be/hjvP-Nrx50g). Please watch this demo video if you'd like to know how MusicPlayerWidget runs.

##Screenshots
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_20.png" alt="Log-in Demo" width="350" />
&nbsp;
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_21.png" alt="Log-in Demo" width="350" />
&nbsp;
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_22.png" alt="Log-in Demo" width="350" />
&nbsp;
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_23.png" alt="Log-in Demo" width="350" />

# License
```
Copyright 2016 Lukoh Nam, goForer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
