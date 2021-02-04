#coding:utf8

import email
import smtplib
import os
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.utils import formataddr

class EmailSender:
    def __init__(self):
        self.user = 'ci@medlinker.com'
        self.passwd = 'Med123'
        # group_chanpin@medlinker.net 产品群邮箱
        # group_front@medlinker.com 前端群邮箱
        # group_dcm_test@medlinker.com 测试群邮箱
        # group_ggyw@medlinker.com 公共业务技术群
        self.to_list = ['group_front@medlinker.com', 'group_dcm_test@medlinker.com', 'group_ggyw@medlinker.com']
        self.cc_list = []
        self.tag = "安卓最新测试包(本邮件是程序自动下发的，请勿回复！)"
        self.doc = None
        


    def send(self, content):
        try:
            server = smtplib.SMTP_SSL("smtp.exmail.qq.com",port=465)
            server.login(self.user,self.passwd)
            server.sendmail("<%s>"%self.user, self.to_list, self._get_attach(content))
            server.close()
            print("send email successful")
        except Exception as e:
            print("send email failed %s"%e)

    
    def sendApkByEmail(self, content, apkPath, email):
        self.tag = "安卓最新正式包(本邮件是程序自动下发的，请勿回复！)"
        self.to_list = [email]
        self.doc = apkPath
        self.send(content)


    def _get_attach(self, content):
        '''
        构造邮件内容
        '''
        attach = MIMEMultipart()

        #添加邮件内容
        #txt = MIMEText(self.content, 'plain', 'utf-8')
        txt = MIMEText(content, 'html', 'utf-8')
        attach.attach(txt)  

        if self.tag is not None:
            #主题,最上面的一行
            attach["Subject"] = self.tag
        if self.user is not None:
            #显示在发件人
            #attach["From"] = "前端-安卓组<%s>"%self.user
            attach['From'] = formataddr(["前端-安卓组", self.user])
        if self.to_list:
            #收件人列表
            attach["To"] = ";".join(self.to_list)
        if self.cc_list:
            #抄送列表
            attach["Cc"] = ";".join(self.cc_list)
        if self.doc:
            #估计任何文件都可以用base64，比如rar等
            name = os.path.basename(self.doc)
            f = open(self.doc,'rb')
            doc = MIMEText(f.read(), "base64", "gb2312")
            doc["Content-Type"] = 'application/octet-stream'
            doc["Content-Disposition"] = 'attachment; filename="'+name+'"'
            attach.attach(doc)
            f.close()
        return attach.as_string()


