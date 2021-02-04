#coding:utf8

import os
import re
import time
import sys, getopt
import platform
import subprocess
import shutil
from pgyModule import PgyUploader
from emailModule import EmailSender
from jiaguModule import JiaGuUtil

# 命令行参数参考文章
# http://www.runoob.com/python/python-command-line-arguments.html

def exeShellCmd(cmd):
    popen = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True)
    cmdRetBytes = popen.stdout.read()
    cmdRetStr = str(cmdRetBytes, encoding='utf-8')
    print(cmdRetStr)
    return cmdRetStr

def updateGitRepo():
    print('update git repo start')
    ret = exeShellCmd('git pull')
    hasError = 'error' in ret
    if(hasError):
        raise ValueError('git pull failed')
    ret = exeShellCmd('cd ../ && git pull')
    hasError = 'error' in ret
    if(hasError):
        raise ValueError('git pull failed')

    print('update git repo end')
    return

def jsBundleFileCopyToAssets():
    jsbundleName = "android.jsbundle"
    assetsPath = "./app/src/main/assets"
    jsBundlesPath = "./jsBundles"

    assetsJsBundleFile = os.path.join(assetsPath, jsbundleName)
    if os.path.exists(assetsJsBundleFile):
        os.remove(assetsJsBundleFile)

    shutil.copyfile(os.path.join(jsBundlesPath, jsbundleName), assetsJsBundleFile)
    return

def deleteAssetsJsBundle():
    assetsJsBundleFile = os.path.join("./app/src/main/assets/android.jsbundle")
    if os.path.exists(assetsJsBundleFile):
        os.remove(assetsJsBundleFile)
    return

def exeGradleCmd(buildEnv, isLiteApp):
    #防止RN原生脚本输出静态资源
    rnResCmd = "cd ../ && " + "sed -i \"\" 's/\"--assets-dest\", resourcesDir,//;/packageTask.dependsOn(currentAssetsCopyTask)/d' node_modules/react-native/react.gradle"
    print(rnResCmd)
    os.system(rnResCmd)

    jsBundleFileCopyToAssets()

    print('gradle release apk file all channels ? ', 'all' in sys.argv)
    sysstr = platform.system()
    cmdPrefix = './gradlew'
    if(sysstr =="Windows"):
         print ("Call Windows tasks")
         cmdPrefix = 'gradlew'
    elif(sysstr == "Linux"):
         print ("Call Linux tasks")
    else:
         print ("Other System tasks")

    suffix = '-PhostType=4'
    assemble = 'assembleOnlineReleaseChannels -PchannelList=medlinker'
    if buildEnv == 'online':
        suffix = '-PhostType=3'
        assemble = 'assembleRelease'
    elif buildEnv == 'dev':
        suffix = '-PhostType=0'
    liteAppfix = ''
    if(isLiteApp != None):
        liteAppfix = '-PisLiteApp=' + isLiteApp
    cmdStr = '%s clean %s %s %s' %(cmdPrefix, assemble, suffix, liteAppfix)
    print (cmdStr)
    os.system(cmdStr)

    deleteAssetsJsBundle()
    return

def checkMaster():
  re1 = os.system('./scripts/checkIfBehind.sh') >> 8
  if (re1 != 0):
      print('\033[0;31m current feature is not the latest with master %s \033[0m' % re1)
      raise ValueError("current feature is not the latest with master")

  re2 = os.system('cd ../ && ./android/scripts/checkIfBehind.sh')
  if (re2 != 0):
      print('\033[0;31m current feature is not the latest with master %s \033[0m' % re2)
      raise ValueError("current feature is not the latest with master")


def getShellCmdValue(cmd):
    popen = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True)
    originStr = popen.stdout.read() # 得到的是bytes字符串
    value = str(originStr, encoding='utf-8')
    return value

def getUsageStr():
    tipStr = ('Usage: \n releaseApk.py -m <message> -e <env> -r <apkReceiverEmail>\n'
            + ' -m, --message <message> 打包日志信息 \n'
            + ' -e, --env <env>  打包环境：[qa,online,dev], 不传默认为qa。\n'
            + ' -r, --apkReceiverEmail <apkReceiverEmail> '
            + '基准包的接收者的邮件（乐固加固后的未签名apk，由发包者用脚本打渠道包）,不传则生成medlinker渠道上传到蒲公英\n'
            + ' -l, --isLiteApp <isLiteApp> 是否是执业版：[ture,false]，不传用android项目默认配置 。\n'
            + ' noemail  不发邮件，默认要发送（上传蒲公英后）。\n'
            + ' local  不发邮件，不上传蒲公英。\n'
            + ' jiagu  打包完成后，自动上传加固并下载\n'
            + ' haveOutputApk  已经打包apk成功，不需要再次打包（qa、online有效，online包需要加固）。\n'
            + ' haveLeguApk 已经打包apk成功，并已获取加固未签名的apk（online有效）。')
    return(tipStr)

def exeLeGuCmd():
    return

def getApkVersionName(apkpath):
    #检查版本号等信息
    output = os.popen("./scripts/lib/aapt d badging %s" % apkpath).read()
    match = re.search("versionCode='(\d+)' versionName='(\d+.\d+.\d+)'", output)
    if not match:
        raise Exception("can't get packageinfo")
    versionName = match.group(2)
    return versionName

def dealApkAndSendPgy(buildEnv, message):
    bakApkPath = 'app/build/bakApk/'
    for fpath, dirname, fnames in os.walk(bakApkPath):
        print(dirname)
        break
    appPath = dirname[0]

    apkResignerForWallePath = 'ToolMakeChannelApk'
    apkOriginPath = bakApkPath + appPath + '/online/app-online-release.apk'
    downloadPath = apkResignerForWallePath + '/apk'

    cmdTransApkStr = (
            ' rm -rf ' + downloadPath + '/*.apk'
            + '\n rm -rf ' + downloadPath
            + '\n mkdir ' + downloadPath)
    exeShellCmd(cmdTransApkStr)

    currentTime = time.strftime("%Y-%m-%d_%H%M%S", time.localtime())
    appVersionName = getApkVersionName(apkOriginPath)
    newName = '医联-v%s-%s.apk' %(appVersionName, currentTime)
    apkNewPath = os.path.join(downloadPath, newName)

    shutil.copyfile(apkOriginPath, apkNewPath)

    #上传到蒲公英
    uploader = PgyUploader()
    uploader.uploadPgyer(apkNewPath, buildEnv, message)

    return

def startJiaGu():
    apkResignerForWallePath = 'ToolMakeChannelApk'
    downloadPath = apkResignerForWallePath + '/apk'
    appPath = ''
    for fpath, dirname, fnames in os.walk(downloadPath):
        print(dirname)
        break
    appPath = downloadPath + '/' + fnames[0]

    jiagu = JiaGuUtil()
    jiagu.startJiaGu(appPath)

#腾讯加固出问题了，使用360加固
def jiaGuAndSendApk(apkReceiverEmail, buildEnv, message):
    SecretId = 'AKIDoZ9vY2w1Um06ILi3JOODyv7RocnawW4U'
    SecretKey = 'tlMNZ4Ti1Ma8XC5CXM8116A5b4Pdj6LO'

    bakApkPath = 'app/build/bakApk/'
    appPath = ''
    for fpath, dirname, fnames in os.walk(bakApkPath):
        print(dirname)
        break
    appPath = dirname[0]

    apkResignerForWallePath = 'ToolMakeChannelApk'
    tinkerApkPath = bakApkPath + appPath + '/online/app-online-release.apk'
    downloadPath = apkResignerForWallePath + '/apk'

    cmdLeguStr = (
        ' rm -rf ' + downloadPath + '/*.apk'
        + '\n rm -rf ' + downloadPath
        + '\n mkdir ' + downloadPath
        + '\n java -Dfile.encoding=utf-8 -jar scripts/lib/ms-shield.jar -sid %s -skey %s -uploadPath %s -downloadPath %s' %(SecretId, SecretKey, tinkerApkPath, downloadPath)
    )
    if ('haveLeguApk' in sys.argv):
        print("已有加固未签名的apk，不需要加固")
    else:
        print(cmdLeguStr)
        print('开始上传，并加固，请耐心等待～')
        exeShellCmd(cmdLeguStr)

    leguAppPath = ''
    appVersionName = ''
    currentTime = time.strftime("%Y-%m-%d_%H%M%S", time.localtime())
    for fpath, dirname, fnames in os.walk(downloadPath):
        print(fnames)
        print(fpath)
        fname = fnames[0]
        leguApkPath = fpath
        leguAppPath = leguApkPath + '/' + fname
        appVersionName = getApkVersionName(leguAppPath)
        if ('haveLeguApk' in sys.argv):
            print('已有加固后未签名的apk，不需要重命名')
        else:
            newName = 'app-release-v%s-%s-legu.apk' %(appVersionName, currentTime)
            oldPath = os.path.join(fpath, fname)
            newPath = os.path.join(fpath, newName)
            os.rename(oldPath, newPath)
            leguAppPath = newPath
        print('leguAppPath = ' + leguAppPath)
        break

    if(apkReceiverEmail == 'no'):
        # 生成渠道包
        cmdWalleStr = 'cd %s && python ApkResigner.py' %(apkResignerForWallePath)
        print(cmdWalleStr)
        os.system(cmdWalleStr)
        # 将medlinker渠道apk上传蒲公英
        medlinkerLeguAlignedSignedApk = ''
        for fpath, dirname, fnames in os.walk(apkResignerForWallePath + '/channels'):
            for fname in fnames:
                if('medlinker' in fname):
                    medlinkerLeguAlignedSignedApk = fname
                    break

        medlinkerApkPath = apkResignerForWallePath + '/channels/' + medlinkerLeguAlignedSignedApk
        print(medlinkerApkPath)
        createChannelsApk(tinkerApkPath, appVersionName, currentTime, '_360appmarket')
        uploader = PgyUploader()
        uploader.uploadPgyer(medlinkerApkPath, buildEnv, message)
    else:#加固后未签名基础包apk发给需要渠道包的同学
        createChannelsApk(tinkerApkPath, appVersionName, currentTime, '_360appmarket')
        print('start send apk emails !!!')
        email_content = """
            <p>hello:</p>
            <p>安卓最新基础包（正式包）</p>
            <p> 版本号：%s ，buildDesc：%s</p>
            <p> 注意：附件中的apk已经使用“乐固”加固，但未签名，该包不能使用！！！</p>
            <p> 你需要做的：</p>
            <p> 使用工具生成渠道包，生成后的渠道包直接上传各个渠道，不需再次加固，工具下载连接：</p>
            <p> 链接:https://pan.baidu.com/s/1GOxrdPYUs_h4e6G3IWs44w  密码:kywn</p>
            """%(appVersionName, message)
        sender = EmailSender()
        sender.sendApkByEmail(email_content, leguAppPath, apkReceiverEmail)
        createChannelsApk(tinkerApkPath, appVersionName, currentTime, '_360appmarket')


    return

def createChannelsApk(baseApkPath, appVersionName, currentTime, channelNames):#使用walle生成渠道包（360应用市场需要强制用360加固）
    print('！！！！！！！！！！执行walle生成渠道包：' + channelNames)
    apkResignerForWallePath = 'ToolMakeChannelApk/'
    libPath = apkResignerForWallePath + 'lib/'
    walleChannelWritterPath = libPath + 'walle-cli-all.jar'
    channelsOutputFilePath = apkResignerForWallePath + 'apk'

    newName = 'app-release-v%s-%s.apk' %(appVersionName, currentTime)
    newBaseApkPath = apkResignerForWallePath + 'apk/' + newName
    shutil.copyfile(baseApkPath, newBaseApkPath)
    writeChannelShell = "java -jar " + walleChannelWritterPath + " batch -c " + channelNames + " " + newBaseApkPath + " " + channelsOutputFilePath
    os.system(writeChannelShell)
    print('！！！！！！！！！！生成渠道包完成')
    os.remove(newBaseApkPath)


if __name__ == '__main__':

    argv = sys.argv[1:]
    print (argv)
    message = ''
    buildEnv = 'qa'
    apkReceiverEmail = 'no'
    isLiteApp = None
    try:
        # 这里的 h 就表示该选项无参数，冒号(:)表示该选项必须有附加的参数，不带冒号表示该选项不附加参数。
        opts, args = getopt.getopt(argv, "hm:e:r:l:",["message=", "env=", "apkReceiverEmail=", "isLiteApp"])
    except getopt.GetoptError:
        print (getUsageStr())
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print (getUsageStr())
            sys.exit()
        elif opt in ("-m", "--message"):
            message = arg
            print('-m arg =%s'%arg)
        elif opt in ("-e", "--env"):
            buildEnv = arg
            print('-e arg =%s'%arg)
        elif opt in ("-r", "--apkReceiverEmail"):
            apkReceiverEmail = arg
            print('-r arg =%s'%arg)
        elif opt in ("-l", "--isLiteApp"):
            isLiteApp = arg
            print('-l arg =%s'%arg)

    print('message = %s, buildEnv = %s, apkReceiverEmail = %s, isLiteApp = %s'%(message, buildEnv, apkReceiverEmail, isLiteApp))

    if ('haveOutputApk' in sys.argv):
        print("已经打包apk成功，不重新打包")
    elif ('haveLeguApk' in sys.argv):
        print("已经获取加固apk，不重新打包")
    elif ("jiagu" in sys.argv):
        startJiaGu()
    else:
        updateGitRepo()
        checkMaster()
        exeGradleCmd(buildEnv, isLiteApp)

    if ("jiagu" not in sys.argv):
        # online包需要加固
        if buildEnv == 'online':
            dealApkAndSendPgy(buildEnv, message)
        elif buildEnv == 'qa' or buildEnv == 'dev':
            uploadApkPatch = 'app/build/outputs/apk/online/release/app-online-release.apk'
            uploader = PgyUploader()
            uploader.uploadPgyer(uploadApkPatch, buildEnv, message)

