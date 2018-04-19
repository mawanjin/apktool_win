import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 恢复图标
 */
public class RecoverSplitApk {
	String curPath;
	String apkDir;
	String keyFile;
	String keyName;
	String keyPasswd;
	String outPath;
//	File fDistribute;
	File sourceF = new File("apks");
	String destDir = "SignedAPKs";
	String prefix = "";
	String suffix;
	String packageName;
	Pattern pattern = Pattern.compile("^android:launchMode=\".*\"");

	public RecoverSplitApk(String apkDir, String keyFile, String keyName, String keyPasswd) {
		this.curPath = new File("").getAbsolutePath();
		this.apkDir = apkDir;
		this.keyFile = keyFile;
		this.keyName = keyName;
		this.keyPasswd = keyPasswd;

		Calendar calendar = Calendar.getInstance();
		String month = calendar.get(2) + 1 + "";
		if (Integer.parseInt(month) < 10) {
			month = "0" + month;
		}
		this.suffix = (new StringBuilder().append(calendar.get(1)).append("").toString().substring(2) + month);
	}

	public void mySplit() {
	    if ((sourceF.exists()) && (sourceF.isDirectory())){
			File[] fileList = sourceF.listFiles();
			for (File file : fileList) {
				if (file.isFile()&&file.getName().endsWith(".apk")) {
//					file.getName().trim().replace(" ", "_");
					try {
						modifyLauncher(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		    System.out.println("apk.size = " + fileList.length);
		}
		moveSignedApks();
	}
	String dir;

	// apktool
	private void modifyLauncher(File apk) throws Exception {
		String apkName = apk.getName();
		System.out.println("apkName: " + apkName);
		this.dir = (apk.getAbsolutePath().replace(apkName, ""));
		this.outPath = this.dir + apk.getName().replace(".apk", "");
		System.out.println("this.dir :: " + this.dir);
		System.out.println("this.outPath :: " + this.outPath);
		// 解包
		String cmdUnpack = "cmd.exe /c  java -jar apktool.jar d  " +this.dir+apkName;
		System.out.println("run proc :: " + cmdUnpack);
		runCmd(cmdUnpack);
		System.out.println("run proc complete,ready to backup AndroidManifest.xml");
		System.out.println("packDir=" + apk.getAbsolutePath());

		// 替换AndroidManifest.xml

		String f_mani = this.curPath+File.separator+ apk.getName().replace(".apk", "") + File.separator + "AndroidManifest.xml";
		String f_mani_bak = this.dir + "AndroidManifest.xml";
		File manifest = new File(f_mani);
		File manifest_bak = new File(f_mani_bak);
		if (manifest_bak.exists()) {
			FileUtils.forceDelete(manifest_bak);
		}
		System.out.println("bak AndroidManifest.xml: " + f_mani + "->" + f_mani_bak);

		FileUtils.copyFile(manifest, manifest_bak);
		// manifest.renameTo(manifest_bak);

		System.out.println("generate package: " + this.dir + ":");

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(manifest_bak),"utf-8"));
		List<String> lines = new ArrayList<String>(0);

		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
//			lines.add(new String(line.getBytes(),Charset.forName("unicode")));
			lines.add(line);
		}
		br.close();
		String preLine = "";
		for (int i = 0; i < lines.size(); i++) {
			String str = (String) lines.get(i);

			if (str.toLowerCase().contains("android.intent.action.main")) {
				if(lines.get(i+1).toLowerCase().contains("android.intent.category.default")){
					System.out.println("start recover.....launcher::"+lines.get(i+1));
					lines.set(i+1, lines.get(i+1).toLowerCase().replace("android.intent.category.default", "android.intent.category.LAUNCHER"));
					System.out.println("after recover.....launcher::"+lines.get(i+1));
				}

				int startIndex = i;
				do {
					startIndex--;
					if (startIndex <= 0) {
						break;
					}
					preLine = (String) lines.get(startIndex);
				} while (!preLine.contains("<activity"));


			}
		}
		if(new File(f_mani).exists()){
			new File(f_mani).delete();
		}

		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f_mani),"UTF-8")));


//		FileWriter fw = new FileWriter(f_mani);
		for (String s1 : lines) {
//			System.out.println(new String(s1.getBytes(), Charset.forName("utf-8")));
//			System.out.println(s1);
//			fw.write(new String(s1.getBytes(), Charset.forName("utf8")) + "\n");
//			fw.write(s1 + "\n");
			out.write(s1+ "\n");

		}
		out.flush();
		out.close();
//		fw.flush();
//		fw.close();
//		fw = null;

		System.out.println("complete recover androidmanifest.xml .");
		runCmd("cmd.exe /c echo abc");


		// 打包
		String unsignApk = this.dir + apk.getName().replace(".apk", "_Unsigned.apk");

		// String cmdPack = String.format("cmd.exe /C java -jar apktool.jar b %s %s", dir, unsignApk);
//		String buildStr = new String(("cmd.exe /c java -jar apktool.jar b -o "+unsignApk+" "+ apk.getName().replace(".apk", "")).getBytes(),Charset.forName("utf-8"));
		String buildStr = "cmd.exe /c java -jar apktool.jar b -o "+unsignApk+" "+ apk.getName().replace(".apk", "");
		String cmdPack = String.format(buildStr);
		System.out.println("start package.run[" + cmdPack + "] ");
		runCmd(cmdPack);

		// sign apk
		String signApk = this.dir + apk.getName().replace(".apk", "_Signed.apk");// _Signed.apk
		 String cmdKey = String.format("cmd.exe /C jarsigner -verbose -keystore %s -storepass %s -signedjar %s -digestalg SHA1 -sigalg MD5withRSA  %s %s", keyFile,keyPasswd, signApk, unsignApk, keyName);
//		 String cmdKey = String.format("cmd.exe /C jarsigner -verbose -keystore %s -storepass %s -signedjar %s -digestalg SHA1 -sigalg MD5withRSA -tsa https://timestamp.geotrust.com/tsa %s %s", keyFile,keyPasswd, signApk, unsignApk, keyName);
//		 String cmdKey = String.format("cmd.exe /C jarsigner -verbose -keystore %s -storepass %s -signedjar %s -digestalg SHA1 -sigalg MD5withRSA  %s %s",
//						new Object[] { this.keyFile, this.keyPasswd, signApk, unsignApk, this.keyName });
		 //jarsigner -digestalg SHA1 -sigalg MD5withRSA -tsa https://timestamp.geotrust.com/tsa -keystore test.keystore -storepass test123 -signedjar signed_client.apk client.apk test
		System.out.println("start sign apk[" + cmdKey + "]");
		runCmd(cmdKey);

		System.out.println("delete unsign apk[" + unsignApk + "]");
		// delete unsign apk
		File unApk = new File(unsignApk);
		unApk.delete();

		FileUtils.forceDelete(new File(f_mani));
		FileUtils.copyFile(manifest_bak, manifest);

		System.out.println("OK");
	}

	 private void moveSignedApks()
	 {
		if ((this.destDir == null) || (this.destDir.equals(""))) {
		      return;
		}
		File targetF = new File(destDir);
		if (!targetF.exists()) {
			targetF.mkdir();
		}
		
		if (sourceF.exists()&&sourceF.isDirectory()&&sourceF.length()>0) {
			File[] apkList = sourceF.listFiles();
			for (File file : apkList) {
				if (file.isFile()&&file.getName().endsWith("_Signed.apk")) {
					System.out.println("move [" + sourceF.getAbsolutePath() + "] to [" + targetF.getAbsolutePath() + "]");
					String cmdPack = String.format("cmd.exe /C move %s %s", file.getAbsolutePath(), targetF);
					System.out.println("start package.run[" + cmdPack + "] ");
					runCmd(cmdPack);

				}
			}
		}
		System.out.println("move APKS to " + this.destDir);
		System.out.println("move APKS done.");
	 }
	
//=============The Following Three Methods are Runners of Command.=============
	
	public void runCmd(String cmd) {
		try {
			System.out.println("method runCmd() called." + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
//			p.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String msg = null;
			while ((msg = br.readLine()) != null) {
				System.out.println(msg);
			}
		} catch (Exception e) {
			System.out.println("runcmd error.....");
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public void execShell(String shell) {
		try {
			Runtime.getRuntime().exec(shell);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	public void runShell(String shell) {
//	    try {
//	      Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", shell }, null, null);
//	      InputStreamReader ir = new InputStreamReader(process.getInputStream());
//	      LineNumberReader input = new LineNumberReader(ir);
//	      String line;
//	      while ((line = input.readLine()) != null) {
//	        System.out.println(line);
//	      }
//	      process.waitFor();
//	    }
//	    catch (Exception e)
//	    {
//	      e.printStackTrace();
//	    }
//	}
}
