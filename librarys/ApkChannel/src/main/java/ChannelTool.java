import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ChannelTool {
    private static String OFFICAL_APK;//源apk
    private static String APK_TOOL; //apktool工具
    private static String APK_SIGNER; //签名工具
    private static String APK_SOURCE_DIR; //渠道包目录
    //签名配置
    private static String KEYSTORE; //签名文件
    private static String KEYSTORE_PASS;
    private static String KEY_ALIAS;
    private static String KEY_PASS;

    static List<String> channels; //渠道列表

    public static void main(String[] args) throws Exception {
        System.out.println("------开始执行");
        loadProperties();
        doTask();
        System.out.println("------执行完毕");
    }

    private static void doTask() throws Exception {
        File sourceApk = new File(APK_SOURCE_DIR, OFFICAL_APK);
        if (!sourceApk.exists()) {
            System.err.println("未找到" + OFFICAL_APK + "文件");
            return;
        }

        //删除除了OFFICAL_APK之外的文件
        File sourceDir = new File(APK_SOURCE_DIR);
        File[] files = sourceDir.listFiles();
        if (null != files) {
            for (File file : files) {
                if (!file.getName().contains(OFFICAL_APK)) {
                    file.delete();
                }
            }
        }

        //1、复制offical官方包
        File copyApk = new File(APK_SOURCE_DIR, "target.apk");
        if (copyApk.exists()) copyApk.delete();
        copyFile(sourceApk, copyApk);
        System.out.println("拷贝" + OFFICAL_APK + "到target.apk");

        //2、解压复制包 offical_copy
        File targetDir = new File(APK_SOURCE_DIR, "target");
        deleteFile(targetDir.getAbsolutePath());
        targetDir.mkdirs();
        String cmd = "java -jar " + APK_TOOL + " d -f " + copyApk.getAbsolutePath() + " -o " + targetDir.getAbsolutePath();
        System.out.println("解压target.apk：" + cmd);
        exec(cmd);


        long totalStart = System.currentTimeMillis();
        for (String channel : channels) {
            long start = System.currentTimeMillis();
            long statTime = System.currentTimeMillis();
            System.out.println("###################" + channel + "#####################");

            //修改 manifest.xml中的渠道
            File manifestFile = new File(targetDir, "AndroidManifest.xml");
            changeChannel(manifestFile, channel);

            long spend = (System.currentTimeMillis() - statTime) / 1000;
            System.err.println("修改manifest.xml花费 " + spend + "秒");
            statTime = System.currentTimeMillis();

            //重新打包
            String unsignDir = targetDir.getAbsolutePath();
            String unsignApk = unsignDir + "_unsign.apk";
            File file = new File(unsignApk);
            if (file.exists()) file.delete();
            cmd = "java -jar " + APK_TOOL + " b " + unsignDir + " -o " + unsignApk;
            System.err.println("打包：" + cmd);
            exec(cmd);

            spend = (System.currentTimeMillis() - statTime) / 1000;
            System.err.println("重新打包花费 " + spend + "秒");
            statTime = System.currentTimeMillis();

            //重新签名
            String dstDirName = "xlt_" + channel;
            String signApk = APK_SOURCE_DIR + File.separator + dstDirName + ".apk";
            cmd = APK_SIGNER + " sign  --ks " + KEYSTORE + "  --ks-key-alias " + KEY_ALIAS + "  --ks-pass pass:" + KEYSTORE_PASS + "  --key-pass pass:" + KEY_PASS + "  --out " + signApk + " " + unsignApk;
            System.err.println("签名：" + cmd);
            exec(cmd);

            spend = (System.currentTimeMillis() - statTime) / 1000;
            System.err.println("重新签名花费 " + spend + "秒");
            statTime = System.currentTimeMillis();

            spend = (System.currentTimeMillis() - start) / 1000;
            System.err.println("总共花费 " + spend + "秒");
            start = System.currentTimeMillis();

            //删除未签名文件
            deleteFile(file.getAbsolutePath());
        }

        long spend = (System.currentTimeMillis() - totalStart) / 1000;
        long avg = spend / channels.size();
        System.err.println("全部" + channels.size() + "个渠道花费 " + spend + "秒,平均花费 " + avg + "秒");

        copyApk.delete();
        deleteFile(targetDir.getAbsolutePath());

    }

    private static void loadProperties() throws Exception {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = ChannelTool.class.getClassLoader().getResourceAsStream("apk_channel.properties");
            properties.load(resourceAsStream);
            String channel = properties.getProperty("channel");
            String[] params = channel.split("\\|");
            channels = Arrays.asList(params);
            OFFICAL_APK = properties.getProperty("OFFICAL_APK");
            APK_TOOL = properties.getProperty("APK_TOOL");
            APK_SIGNER = properties.getProperty("APK_SIGNER");
            APK_SOURCE_DIR = properties.getProperty("APK_SOURCE_DIR");
            KEYSTORE = properties.getProperty("KEYSTORE");
            KEYSTORE_PASS = properties.getProperty("KEYSTORE_PASS");
            KEY_ALIAS = properties.getProperty("KEY_ALIAS");
            KEY_PASS = properties.getProperty("KEY_PASS");

            System.out.println("channels=" + channels);
            System.out.println("OFFICAL_APK=" + OFFICAL_APK);
            System.out.println("APK_TOOL=" + APK_TOOL);
            System.out.println("APK_SIGNER=" + APK_SIGNER);
            System.out.println("APK_SOURCE_DIR=" + APK_SOURCE_DIR);
            System.out.println("KEYSTORE=" + KEYSTORE);
            System.out.println("KEYSTORE_PASS=" + KEYSTORE_PASS);
            System.out.println("KEY_ALIAS=" + KEY_ALIAS);
            System.out.println("KEY_PASS=" + KEY_PASS);

            resourceAsStream.close();
        } catch (Exception e) {
            System.out.println("读取apk_channel.properties出错");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 修改渠道
     */
    private static void changeChannel(File manifestFile, String channel) throws Exception {
        BufferedReader br = null;
        PrintWriter pw = null;
        StringBuffer buff = new StringBuffer();//临时容器!
        String line = System.getProperty("line.separator");//平台换行!
        String str = null;
        try {
            br = new BufferedReader(new FileReader(manifestFile));
            while ((str = br.readLine()) != null) {
                if (str.contains("UMENG_CHANNEL")) { // <meta-data android:name="UMENG_CHANNEL" android:value="offical"/>
                    str = "<meta-data android:name=\"UMENG_CHANNEL\" android:value=\"" + channel + "\"/>";
                    System.out.println("替换渠道: " + str);
                }
                buff.append(str + line);
            }

            pw = new PrintWriter(new FileWriter(manifestFile), true);
            pw.println(buff);
        } finally {
            if (br != null) br.close();
            if (pw != null) pw.close();
        }
    }

    private static void copyFile(File source, File dest) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            if (null != input) input.close();
            if (null != output) output.close();
        }

    }


    public static void dirCopy(String srcPath, String destPath) {
        File src = new File(srcPath);
        if (!new File(destPath).exists()) {
            new File(destPath).mkdirs();
        }
        for (File s : src.listFiles()) {
            if (s.isFile()) {
                fileCopy(s.getPath(), destPath + File.separator + s.getName());
            } else {
                dirCopy(s.getPath(), destPath + File.separator + s.getName());
            }
        }
    }

    public static void fileCopy(String srcPath, String destPath) {
        File src = new File(srcPath);
        File dest = new File(destPath);
        //使用jdk1.7 try-with-resource 释放资源，并添加了缓存流
        try (InputStream is = new BufferedInputStream(new FileInputStream(src));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
            byte[] flush = new byte[1024];
            int len = -1;
            while ((len = is.read(flush)) != -1) {
                out.write(flush, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void exec(String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd);
        InputStream inputStream = exec.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
        exec.destroy();
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteFile(f.getAbsolutePath());
            }
        }
        file.delete();
    }
}
