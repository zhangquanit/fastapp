#coding:utf8
import os
import sys

class JiaGuUtil:
    def startJiaGu(self, apkPath):
        print("apkPath = " + apkPath)
        SecretId = 'AKIDoZ9vY2w1Um06ILi3JOODyv7RocnawW4U'
        SecretKey = 'tlMNZ4Ti1Ma8XC5CXM8116A5b4Pdj6LO'

        (filepath, tempfilename) = os.path.split(apkPath)
        (filename, extension) = os.path.splitext(tempfilename)
        print('apkFileName = ' + filename)
        apkResignerForWallePath = 'ToolMakeChannelApk'
        downloadPath = apkResignerForWallePath + '/apk-jiagu'

        cmdLeguStr = (
            ' rm -rf ' + downloadPath + '/*.apk'
            + '\n rm -rf ' + downloadPath
            + '\n mkdir ' + downloadPath
            + '\n java -Dfile.encoding=utf-8 -jar scripts/lib/ms-shield.jar -sid %s -skey %s -uploadPath %s -downloadPath %s' %(SecretId, SecretKey, apkPath, downloadPath)
        )
        print(cmdLeguStr)
        print('开始上传，并加固，请耐心等待～')
        os.system(cmdLeguStr)

        leguAppPath = ''
        appVersionName = ''
        for fpath, dirname, fnames in os.walk(downloadPath):
            print(fnames)
            print(fpath)
            fname = fnames[0]
            leguApkPath = fpath
            leguAppPath = leguApkPath + '/' + fname

            newName = '%s-legu.apk' %(filename)
            oldPath = os.path.join(fpath, fname)
            newPath = os.path.join(fpath, newName)
            os.rename(oldPath, newPath)
            leguAppPath = newPath
            print('leguAppPath = ' + leguAppPath)
            break