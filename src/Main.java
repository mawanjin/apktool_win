import java.io.File;

public class Main {

    public static void main(String[] args) {

//        if(args.length != 4) {
//            System.out.println("usage:java -jar RyApkTool.jar apkName keyFile keyName  keyPasswd");
//            System.out.println("Example: java -jar RyApkTool.jar test.apk myAndroidkey rydiy 123456");
//            System.out.println("invalid numbers of parameter,needs 4.");
//            return;
//        }
//
        String apkDir = args[0];
        String keyFile = args[1];
        String keyName = args[2];
        String keyPasswd = args[3];
////        String vNumber = args[4];
//        System.out.println("apkDir="+apkDir+";keyFile="+keyFile+";keyName="+keyName+";keyPasswd="+keyPasswd);

//        String apkDir = "apks";
//        String keyFile = "C:\\Users\\xucx1\\workspace\\MakeJar\\pa_crack.keystore";
//        String keyName = "pa_crack.keystore";
//        String keyPasswd = "123456";
 //java -jar reapktool.jar apks pa_crack.keystore pa_crack.keystore 123456 
//        if (args.length == 4)
//        {
//            apk = args[0];
//            keyFile = args[1];
//            keyName = args[2];
//            keyPasswd = args[3];
////            distribute = Boolean.parseBoolean(args[4]);
//        }else{
//        	System.out.println("parameters is not correct!");
//            System.exit(0);
//        }
		if(!new File(apkDir).exists()){
			System.out.println("no apkDir specified!");
            System.exit(0);
        }
        SplitApk sp = new SplitApk(apkDir, keyFile, keyName, keyPasswd);
        sp.mySplit();
    }
}
