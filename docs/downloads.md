# Downloads
There are multiple ways of downloading the app, some are easier, some are harder, some are more efficient and some have special benefits. Please note that this section is meant for users, not contributors. If you want to download the project and contribute to it, refer to the [contributing](./contributing/) section

## Release download
The easiest way to download the app is through the [Releases](https://github.com/Benji377/SocyMusic/releases/latest) on Github. Before you download anything, make sure the version is the correct one. If you want to download the latest release, download the latest release with a beta tag in it, else download one without it. Beta-releases might not be stable and cause some issues while using, normal releases are better.
Here is a step by step tutorial:
1. Go to the [Github repository](https://github.com/Benji377/SocyMusic), you should now see something like the image below:
![first_tutorial](https://user-images.githubusercontent.com/50681275/128526424-b7bb1d02-16ae-4bb4-a050-a23d0569bd11.PNG)
2. Click on the Release tab, it's circled in red in the image above.
3. Now you should be on a page that at the top looks like this:
![second_tutorial](https://user-images.githubusercontent.com/50681275/128526662-d72dfd35-55e2-4321-855f-8aa899be7eb4.PNG)
4. Now just scroll to the end of the post where you will find a section called Assets, like this:
![third_tutorial](https://user-images.githubusercontent.com/50681275/128526780-e8634b3e-5b26-4063-9ec3-fc442dda5b47.PNG)
5. Now all you need to do is click on the SocyMusic.apk file and the download will start automatically
6. Now you need to transfer the file to your phone. You can do this by sending the file to yourself or using a cable
7. When on the phone click on it and the download process will start.
8. Congratulations! You have installed SocyMusic. If anything went wrong, please refer to the [Contact section](./other/#contact)

**Note:** Sometimes phones block the installation because they don't recognize the app, you can safely ignore it and continue the download.

## Source download
This is a more advanced method of installing the app. It will require you to have some coding knowledge. Start by going to the [Github repository](https://github.com/Benji377/SocyMusic) and download the whole source. You could do this with GIT or just download the ZIP file. Please refer to this great guide on how to do it: [Instructables](https://www.instructables.com/Downloading-Code-From-GitHub/).
After downloading the project, navigate to the folder where the project is located and use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to build the project. You will need to open the command line and write `gradlew assembleDebug` to create a .apk file. Now you can just transfer the .apk file to your phone again and install it as usual.

**Note:** Even if you need to build the file from scratch in the source download, it does have the advantage of giving you the newest version of the app possible. This is especially useful if you plan on [testing the app](./contributing/).
