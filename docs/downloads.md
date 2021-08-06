# Downloads
There are multiple ways of downloading the app, some are easier, some are harder, some are more efficient and some have special benefits. Please note that this section is meant for users, not contributors. If you want to download the project and contribute to it, refer to the [contributing](./contributing/) section

## Release download
The easiest way to download the app is trough the [Releases](https://github.com/Benji377/SocyMusic/releases/latest) on Github. Before you download anything, make sure the version is the correct one. If you want to download the latest release, download the latest release with a beta tag in it, else download one without it. Beta-releases might not be stable and cause some issues while using, normal releases are better.
After you go to the link above, scroll to the bottom of the page, click on Assets and download your prefered file format. APK is the recommend one. After downloading the file, transfer it to your phone and then click on it, a pop-up should appear and the installation start.
**Note:** Sometimes phones block the installation because they don't recognize the app, you can safely ignore it and continue the download

## Source download
This is a more advanced method of installing the app. It will require you to have some coding knowledge. Start by going to the [Github repository](https://github.com/Benji377/SocyMusic) and download the whole source. You could do this with GIT or just download the ZIP file. Please refer to this great guide on how to do it: [Instructables](https://www.instructables.com/Downloading-Code-From-GitHub/).
After downloading the project, navigate to the folder where the project is located and use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to build the project. You will need to open the command line and write `gradlew assembleDebug` to create an apk. Now you can just transfer the apk to your phone again and install it as usual.

**Note:** Even if you need to build the file from scratch in the source download, it does have the advantage of giving you the newest version of the app possible. This is especially useful if you plan on [testing the app](./contributing/).