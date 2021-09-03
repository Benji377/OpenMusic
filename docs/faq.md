# Frequently asked questions (FAQ)
As SocyMusic slowly but steadly grows with new functionality and users, new questions arise. And because users often ask the same questions, in thi section we llist the most common one and explain solutions and answers for it.

- ### SocyMusic doesn't find any songs
    This can happen because when the app starts for the first time after installation or after an update, settings may be resetted. Because of this, no folder is set in the settings and the app doesn't know where to search for songs.
    To fix this you can simply navigate to the settings tab and set the path to the folder where you have your songs saved in. Then restart the app and you should see your songs all listed in the main screen of the app.

- ### My SDcard or folder is not showing up as an available option to load songs from
    When choosing a location from where to load songs from, your folder or sdcard might not be showing up. If its an external hard drive (USB stick, SDcard, etc...) you should see if it has been plugged in correctly and recognized by the system. Try navigating to it from your devices file explorer and see if you can find it. If instead it is a folder that can't be recognized it may be because the foilder is hidden or locked. You need to move your files out of there, unlock or unhide your folder. If it still doesn't recognize it, please report this issue and remember to specify what device you are using and in what conditions the issue arises.

- ### My songs are not showing up even after placing them in the correct folder and setting the correct path in the settings
    There can be two reasons for this. The first and easier one is that the format you ahve your songs in is not supported by SocyMusic. As of now, SocyMusic only supports two file formats: MP3 and WAV.If your file is not any of these formats, it will not be recognized by the app
    The second and harder to fix reason is that your songs are corrupt. When songs are corrupt it may happen that the app is unable to load the songs or play them. To fix corupted files you will need to download them again, maybe from a different source to avoid the same mistake again.

- ### App seemingly crashes when changing theme
    We are aware of this, but it is not an issue, the app is not crashing. When you change a theme, we need to apply it to the app, but sibce that would require the user to restart the appp, we just restart it programmatically. The side-effect is that for a second or two the screen turns balck, but the app should reappear shortly after. If the app doesn't restart, then it obviously crashed and you should please [report it](#I-found-an-issue-and-want-to-report-it).

- ### I am unable to drag the player downwards
    If you are listening to music and want to switch track, you will probably know that you can drag down the player to minimize it. This is a great feature, but you will also have noticed that if you try to drag with the finger on the song icon it won't work. It will only allow you to drag left and right. This is a known bug, you can read about it [here](https://github.com/Benji377/SocyMusic/issues/125).

- ### Help, [insert anything here] doesn't work
    Sometimes odd things happen, this could be because of the phone you are using, the songs are corrupted or other factors. Most of the times, restarting the app solves it.If your issue is not listed here or in the [issue tab on Github](https://github.com/Benji377/SocyMusic/issues) and still persists after an app restart, you might want to [report it](#I-found-an-issue-and-want-to-report-it).

- ### I have a new idea for the app
    That's great news! As an open-source project, SocyMusic is open for all kind of ideas and we would be really happy to hear yours. Just as by submitting a report, you have two possibilities. You can either [write us an email](mailto:socymusic@gmail.com?subject=Idea%20suggestion) or submit it directly on [Github discussions](https://github.com/Benji377/SocyMusic/discussions/categories/ideas). Please be as detailed as possible about describing what you have in mind. You can also [draw it](https://sketch.io/sketchpad/), if you think it might be helpfull
    After submitting your thoughts, standby as we might need to investigate further and ask you some questions and clarifications about it.

- ### I found an issue and want to report it
    The team behind SocyMusic (by the time of writing this) is actually very small and we therefore don't always find all possible bugs and issues in the app. If you find one we are really sorry for the inconvinience.
    To report an issue there are two ways you can go about it. You can send an [email to SocyMusic](mailto:socymusic@gmail.com?subject=Issue%20report) directly and we will then insert it as issue in Github, or do it [directly in Github](https://github.com/Benji377/SocyMusic/issues/new/choose) if you have an account for it.
    Please note that the more specific you are in describing your issue, the better we will be able to assist you. Once you have submitted your report either by email or by Github please standby as we might have questions to better understand your issue.