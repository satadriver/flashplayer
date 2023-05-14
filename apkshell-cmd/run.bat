


set path = "./"
rd /q /s .\test
java -jar apkshell.jar ./apkunshell.apk ./app-release.apk ./test jy 47.101.204.4
copy .\apkunshell.apk_new.apk .\autosign\update.apk

java -jar ./autosign/signapk.jar ./autosign/testkey.x509.pem ./autosign/testkey.pk8 ./autosign/update.apk ./autosign/update_signed.apk

copy .\autosign\update_signed.apk .\mytest.apk

del .\apkUnshell.apk_new.apk
del .\autosign\update.apk
del .\autosign\update_signed.apk

rd /q /s .\test
pause