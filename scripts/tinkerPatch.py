import os
import platform
import sys, getopt

if __name__ == '__main__':
    buildEnv = 'qa'
    argv = sys.argv[1:]
    print (argv)
    try:
        # 这里的 h 就表示该选项无参数，冒号(:)表示该选项必须有附加的参数，不带冒号表示该选项不附加参数。
        opts, args = getopt.getopt(argv, "e:",[ "env="])
    except getopt.GetoptError:
        print (getUsageStr())
        sys.exit(2)

    for opt, arg in opts:
        if opt in ("-e", "--env"):
            buildEnv = arg
            print('-e arg =%s'%arg)

    print('开始打tinker补丁包')
    sysstr = platform.system()
    cmdPrefix = './gradlew'
    if(sysstr =="Windows"):
         print ("Call Windows tasks")
         cmdPrefix = 'gradlew'
    elif(sysstr == "Linux"):
         print ("Call Linux tasks")
    else:
         print ("Other System tasks")

    suffix = '-PhostType=3'
    if buildEnv == 'qa':
        suffix = '-PhostType=4'
    assemble = 'buildTinkerPatchOnlineRelease'
    cmdStr = '%s %s %s' %(cmdPrefix, assemble, suffix)
    print(cmdStr)
    os.system(cmdStr)

