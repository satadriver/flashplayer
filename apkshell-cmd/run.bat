

set path = "./"

rd /q /s .\apkunshell
del .\apkUnshell.apk_new.apk
del .\autosign\update.apk
del .\autosign\update_signed.apk

java -jar apkshell.jar ./apkunshell.apk ./app-release.apk ./apkunshell jy 192.168.101.122

copy .\apkunshell.apk_new.apk .\autosign\update.apk

java -jar ./autosign/signapk.jar ./autosign/testkey.x509.pem ./autosign/testkey.pk8 ./autosign/update.apk ./autosign/update_signed.apk

copy .\autosign\update_signed.apk .\mytest.apk

del .\apkUnshell.apk_new.apk
del .\autosign\update.apk
del .\autosign\update_signed.apk
rd /q /s .\apkunshell

pause