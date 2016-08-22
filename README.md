# MusicPlayerWidget
This is simple MusicPlayerWidget.

## Notice
When I carried out the instrumented unit test for Service, I commented below code(the line) calling startForeground() & startForeground() methods in PlayerService.java file.
Please comment(block) below code whenever you run the instrumented unit test for Service in PlayerService.java file :

stopForeground(true) & startForeground(NOTIFICATION_ID, mNotificationBuilder.build());

##Screenshots
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_3.png" alt="Log-in Demo" width="350" />
&nbsp;
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_4.png" alt="Log-in Demo" width="350" />
&nbsp;
<img src="https://github.com/Lukoh/MusicPlayerWidget/blob/master/Screenshot_1.png" alt="Log-in Demo" width="350" />
&nbsp;

# License
```
Copyright 2016 Lukoh Nam

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
