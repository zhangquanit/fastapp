#coding:utf8

import os
import sys
import requests
import subprocess
from emailModule import EmailSender

class PgyUploader:
    def uploadPgyer(self, uploadApkPatch, buildEnv, message):
        if('local' in sys.argv):
            print('complete package work, without upload pgyer，without send a email！😊😊')
            return
        # start upload pgyer
        print('start upload pgyer !!!')

        url = 'https://www.pgyer.com/apiv2/app/upload'

        path = uploadApkPatch
        print(path)

        buildDesc = '测试环境'
        if buildEnv == 'online':
            buildDesc = '生产环境'
        elif buildEnv == 'dev':
            buildDesc = 'DEV环境'
        buildDesc = buildDesc + ','+ message

        #  params
        params = {
            "uKey": (None, "dad6308763eece8035c49ea33e676138"),
            "_api_key": (None, "87a96feb51f5ecdfafc2bc4c9eeb045a"),
            "file":('app-online-release.apk', open(path,'rb'),'application/x-zip-compressed'),
            "buildUpdateDescription": (None, buildDesc),
            #"buildInstallType": (None, 2), # 2：密码安装，3：邀请安装
            #"buildPassword": (None, "med123456")
        }

        response = requests.post(url, files=params)

        #  deal response
        jsonData = response.json()
        print(jsonData)
        dataObj = jsonData.get('data')
        urlKey =  jsonData.get('data').get('buildShortcutUrl')
        shortcutUrl = 'https://www.pgyer.com/'+urlKey
        appQRCodeURL = jsonData.get('data').get('buildQRCodeURL')
        buildVersion = dataObj.get('buildVersion')
        buildBuildVersion = dataObj.get('buildBuildVersion')
        print('upload pgyer success!!! appShortcutUrl :')
        print(shortcutUrl)
        print(appQRCodeURL)

        gitCmd = 'git log -20 --pretty=format:%h--%s--%an--%cr --no-merges' # 最近20条提交日志
        popen = subprocess.Popen(gitCmd, stdout=subprocess.PIPE, shell=True)
        originStr = popen.stdout.read() # 得到的是bytes字符串
        gitRecentlyCommitMsg = str(originStr, encoding='utf-8')
        print(gitRecentlyCommitMsg)

        formatCommitMsg = gitRecentlyCommitMsg.replace('\n','<br/>')

        if('noemail' in sys.argv):
            print('complete package work, without send a email.')
            return

        print('start send emails !!!')
        email_content = """
            <p>  hi all:</p>
            <p>医联APP-安卓最新测试包，请点击下面链接查看详情，或扫描二维码直接下载。</p>
            <p> 蒲公英build 版本号：%s</p>
            <p> App 版本：%s, buildDesc : %s </p>
            <p><a href=%s>App详情页</a></p>
            <p> App二维码：</p>
            <p><img src=%s></p>
            <p>最近20条提交日志，<b>格式：简短hash--commitMsg--anthor--time</b></p>
            <p>%s</p>
            """%(buildBuildVersion, buildVersion, buildDesc, shortcutUrl, appQRCodeURL,
                formatCommitMsg)

        sender = EmailSender()
        sender.send(email_content)
        print('complete package work!!!')